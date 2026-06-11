#!/usr/bin/env python3
"""Warm LSP Docker images: spawn each server once and complete an LSP initialize handshake."""

from __future__ import annotations

import argparse
import json
import os
import select
import subprocess
import sys
import tempfile
import threading
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from dataclasses import dataclass
from pathlib import Path
from typing import Callable

_SCRIPTS_DIR = Path(__file__).resolve().parent
if str(_SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(_SCRIPTS_DIR))

from docker_image_utils import resolve_docker_image

HEADER_END = b"\r\n\r\n"
ROOT = Path(__file__).resolve().parent.parent
STAMP_NAME = ".ctl-lsp-warm-stamp"


def stamp_path() -> Path:
    """Match Java RunnerOpsPaths: ctl.ops-data-dir when set, else repo root."""
    ops = os.environ.get("CTL_OPS_DATA_DIR", "").strip()
    base = Path(ops).expanduser().resolve() if ops else ROOT
    return base / STAMP_NAME


def frame(message: dict) -> bytes:
    body = json.dumps(message, separators=(",", ":")).encode("utf-8")
    header = f"Content-Length: {len(body)}\r\n\r\n".encode("utf-8")
    return header + body


def drain_lsp_buffer(buffer: bytearray) -> list[dict]:
    messages: list[dict] = []
    while True:
        header_end = buffer.find(HEADER_END)
        if header_end < 0:
            break
        header_block = buffer[:header_end].decode("utf-8", errors="replace")
        content_length = None
        for line in header_block.split("\r\n"):
            if line.lower().startswith("content-length:"):
                content_length = int(line.split(":", 1)[1].strip())
                break
        if content_length is None:
            raise ValueError(f"LSP header missing Content-Length: {header_block!r}")
        body_start = header_end + len(HEADER_END)
        if len(buffer) - body_start < content_length:
            break
        body = bytes(buffer[body_start : body_start + content_length])
        del buffer[: body_start + content_length]
        messages.append(json.loads(body.decode("utf-8")))
    return messages


def write_java_workspace(root: Path) -> None:
    main_dir = root / "src/main/java/com/challenge"
    main_dir.mkdir(parents=True, exist_ok=True)
    (root / "pom.xml").write_text(
        """<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.challenge</groupId>
  <artifactId>workspace</artifactId>
  <version>1.0-SNAPSHOT</version>
  <properties>
    <maven.compiler.release>21</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>
</project>
""",
        encoding="utf-8",
    )
    (main_dir / "Solution.java").write_text(
        "package com.challenge;\n\npublic class Solution {\n}\n",
        encoding="utf-8",
    )


def write_python_workspace(root: Path) -> None:
    (root / "solution.py").write_text("def solve():\n    pass\n", encoding="utf-8")


def write_go_workspace(root: Path) -> None:
    (root / "go.mod").write_text(
        "module challenge\n\ngo 1.23\n",
        encoding="utf-8",
    )
    (root / "solution.go").write_text(
        "package main\n\nfunc Solve() {}\n",
        encoding="utf-8",
    )


def write_js_stack_workspace(root: Path, file_name: str, content: str) -> None:
    (root / "package.json").write_text(
        """{
  "name": "workspace",
  "private": true,
  "devDependencies": {
    "typescript": "5.7.3",
    "@types/node": "22.13.10"
  }
}
""",
        encoding="utf-8",
    )
    (root / "tsconfig.json").write_text(
        """{
  "compilerOptions": {
    "target": "ES2022",
    "module": "ESNext",
    "moduleResolution": "bundler",
    "strict": true,
    "jsx": "react-jsx",
    "skipLibCheck": true,
    "noEmit": true,
    "allowJs": true
  },
  "include": ["*.ts", "*.tsx", "*.js", "*.vue"]
}
""",
        encoding="utf-8",
    )
    (root / file_name).write_text(content, encoding="utf-8")


def write_csharp_workspace(root: Path) -> None:
    (root / "workspace.csproj").write_text(
        """<Project Sdk="Microsoft.NET.Sdk">
  <PropertyGroup>
    <TargetFramework>net8.0</TargetFramework>
    <ImplicitUsings>enable</ImplicitUsings>
    <Nullable>enable</Nullable>
  </PropertyGroup>
</Project>
""",
        encoding="utf-8",
    )
    (root / "Solution.cs").write_text(
        "public static class Solution {\n}\n",
        encoding="utf-8",
    )


def write_rust_workspace(root: Path) -> None:
    src = root / "src"
    src.mkdir(parents=True, exist_ok=True)
    (root / "Cargo.toml").write_text(
        """[package]
name = "workspace"
version = "0.1.0"
edition = "2021"

[lib]
path = "src/lib.rs"
""",
        encoding="utf-8",
    )
    (src / "lib.rs").write_text("pub fn solve() {}\n", encoding="utf-8")


def write_cpp_workspace(root: Path) -> None:
    (root / "compile_flags.txt").write_text("-std=c++20\n-I.\n", encoding="utf-8")
    (root / "solution.cpp").write_text("int solve() { return 0; }\n", encoding="utf-8")


WORKSPACE_WRITERS: dict[str, Callable[[Path], None]] = {
    "java": write_java_workspace,
    "python": write_python_workspace,
    "go": write_go_workspace,
    "node": lambda root: write_js_stack_workspace(
        root, "solution.js", "export function solve() {}\n"
    ),
    "typescript": lambda root: write_js_stack_workspace(
        root, "solution.ts", "export function solve(): void {}\n"
    ),
    "react": lambda root: write_js_stack_workspace(
        root,
        "solution.tsx",
        "export function Solution() {\n  return null;\n}\n",
    ),
    "vue": lambda root: write_js_stack_workspace(
        root,
        "solution.vue",
        "<script setup lang=\"ts\">\n</script>\n\n<template>\n  <div />\n</template>\n",
    ),
    "angular": lambda root: write_js_stack_workspace(
        root, "solution.ts", "export function solve(): void {}\n"
    ),
    "csharp": write_csharp_workspace,
    "rust": write_rust_workspace,
    "cpp": write_cpp_workspace,
}


@dataclass(frozen=True)
class WarmTarget:
    label: str
    language: str
    image_env: str
    default_image: str
    timeout_seconds: int = 45
    smoke: bool = False
    smoke_entrypoint: str = ""
    smoke_args: tuple[str, ...] = ()


# One target per unique image. Vue shares lsp-typescript (entrypoint switch only) — skipped by default.
WARM_TARGETS: tuple[WarmTarget, ...] = (
    # JDT LS JVM + initialize handshake; slow on small VPS (often 60–120s, sometimes longer).
    WarmTarget("java", "java", "LSP_JAVA_IMAGE", "code-challenge-ide-lsp-java:local", 180),
    WarmTarget("python", "python", "LSP_PYTHON_IMAGE", "code-challenge-ide-lsp-python:local", 30),
    WarmTarget(
        "go",
        "go",
        "LSP_GO_IMAGE",
        "code-challenge-ide-lsp-go:local",
        20,
        smoke=True,
        smoke_entrypoint="gopls",
        smoke_args=("version",),
    ),
    WarmTarget("typescript", "typescript", "LSP_TYPESCRIPT_IMAGE", "code-challenge-ide-lsp-typescript:local", 45),
    WarmTarget(
        "csharp",
        "csharp",
        "LSP_DOTNET_IMAGE",
        "code-challenge-ide-lsp-dotnet:local",
        20,
        smoke=True,
        smoke_entrypoint="csharp-ls",
        smoke_args=("--version",),
    ),
    WarmTarget(
        "rust",
        "rust",
        "LSP_RUST_IMAGE",
        "code-challenge-ide-lsp-rust:local",
        20,
        smoke=True,
        smoke_entrypoint="rust-analyzer",
        smoke_args=("--version",),
    ),
    WarmTarget(
        "cpp",
        "cpp",
        "LSP_CPP_IMAGE",
        "code-challenge-ide-lsp-cpp:local",
        20,
        smoke=True,
        smoke_entrypoint="clangd-14",
        smoke_args=("--version",),
    ),
)

OPTIONAL_WARM_TARGETS: tuple[WarmTarget, ...] = (
    WarmTarget(
        "vue",
        "vue",
        "LSP_TYPESCRIPT_IMAGE",
        "code-challenge-ide-lsp-typescript:local",
        20,
        smoke=True,
        smoke_entrypoint="vue-language-server",
        smoke_args=("--version",),
    ),
)


def log(message: str) -> None:
    print(message, flush=True)


def resolve_image(target: WarmTarget) -> str | None:
    image = resolve_docker_image(target.image_env, target.default_image)
    explicit = os.environ.get(target.image_env, "").strip()
    if image and explicit and image != explicit:
        log(f"  Note: {target.image_env}={explicit} unavailable locally; using {image}")
    return image


def image_id(image: str) -> str:
    result = subprocess.run(
        ["docker", "image", "inspect", image, "--format", "{{.Id}}"],
        check=True,
        capture_output=True,
        text=True,
    )
    return result.stdout.strip()


def current_stamp(targets: tuple[WarmTarget, ...]) -> dict[str, str]:
    stamp: dict[str, str] = {}
    for target in targets:
        image = resolve_image(target)
        if not image:
            continue
        stamp[f"{target.label}:{image}"] = image_id(image)
    return stamp


def stamp_file_candidates() -> tuple[Path, ...]:
    primary = stamp_path()
    fallback = Path("/app") / STAMP_NAME
    if fallback == primary:
        return (primary,)
    return (primary, fallback)


def load_stamp() -> dict[str, str] | None:
    for path in stamp_file_candidates():
        if not path.is_file():
            continue
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
            return data if isinstance(data, dict) else None
        except (json.JSONDecodeError, OSError):
            continue
    return None


def stamp_write_path() -> Path:
    """Prefer CTL_OPS_DATA_DIR; fall back to /app when the volume is not writable (uid 10001)."""
    primary = stamp_path()
    fallback = Path("/app") / STAMP_NAME
    for candidate in (primary, fallback):
        try:
            candidate.parent.mkdir(parents=True, exist_ok=True)
            probe = candidate.parent / ".write-probe"
            probe.write_text("", encoding="utf-8")
            probe.unlink(missing_ok=True)
            return candidate
        except OSError:
            continue
    return primary


def merge_and_save_stamp(new_entries: dict[str, str]) -> None:
    """Merge into existing stamp so partial --only warms do not erase other languages."""
    existing = load_stamp() or {}
    merged = {**existing, **new_entries}
    path = stamp_write_path()
    path.write_text(json.dumps(merged, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    if path != stamp_path():
        log(f"  stamp saved to {path} (ops data dir not writable)")


def cleanup_orphan_warm_containers() -> None:
    result = subprocess.run(
        [
            "docker",
            "ps",
            "-q",
            "--filter",
            "label=ctl.lsp-warm=true",
        ],
        capture_output=True,
        text=True,
        check=False,
    )
    ids = [line.strip() for line in result.stdout.splitlines() if line.strip()]
    if not ids:
        return
    subprocess.run(["docker", "rm", "-f", *ids], check=False, capture_output=True)
    print(f"Cleaned {len(ids)} orphaned LSP warm container(s).")


def drain_stderr(process: subprocess.Popen[bytes], sink: list[str], stop: threading.Event) -> None:
    if process.stderr is None:
        return
    try:
        while not stop.is_set():
            if process.poll() is not None:
                break
            ready, _, _ = select.select([process.stderr], [], [], 0.2)
            if not ready:
                continue
            chunk = process.stderr.read(4096)
            if not chunk:
                break
            sink.append(chunk.decode("utf-8", errors="replace"))
    except (OSError, ValueError):
        pass


def stop_process(
    process: subprocess.Popen[bytes],
    *,
    stderr_lines: list[str] | None = None,
) -> str:
    if process.poll() is None:
        try:
            os.killpg(process.pid, 15)
        except ProcessLookupError:
            process.terminate()
        try:
            process.wait(timeout=3)
        except subprocess.TimeoutExpired:
            try:
                os.killpg(process.pid, 9)
            except ProcessLookupError:
                process.kill()
            try:
                process.wait(timeout=2)
            except subprocess.TimeoutExpired:
                process.kill()
                process.wait(timeout=2)

    if process.stderr is not None:
        try:
            remaining = process.stderr.read()
            if remaining and stderr_lines is not None:
                stderr_lines.append(remaining.decode("utf-8", errors="replace"))
        except (OSError, ValueError):
            pass

    return "".join(stderr_lines) if stderr_lines else ""


def smoke_warm_one(target: WarmTarget, image: str, dry_run: bool) -> tuple[str, float]:
    started = time.monotonic()
    if not target.smoke_entrypoint:
        raise ValueError(f"smoke warm target {target.label} missing smoke_entrypoint")
    command = [
        "docker",
        "run",
        "--rm",
        "--init",
        "--label",
        "ctl.lsp-warm=true",
        "--network",
        "none",
        "--stop-timeout",
        "2",
        "--entrypoint",
        target.smoke_entrypoint,
        image,
        *target.smoke_args,
    ]
    if dry_run:
        print(f"[dry-run] {' '.join(command)}")
        return target.label, 0.0
    subprocess.run(
        command,
        check=True,
        capture_output=True,
        text=True,
        timeout=target.timeout_seconds,
    )
    return target.label, time.monotonic() - started


def warm_one(target: WarmTarget, image: str, dry_run: bool) -> tuple[str, float]:
    if target.smoke:
        return smoke_warm_one(target, image, dry_run)

    started = time.monotonic()
    writer = WORKSPACE_WRITERS[target.language]
    with tempfile.TemporaryDirectory(prefix="ctl-lsp-warm-") as workspace_dir:
        workspace = Path(workspace_dir)
        writer(workspace)
        command = [
            "docker",
            "run",
            "--rm",
            "-i",
            "--init",
            "--label",
            "ctl.lsp-warm=true",
            "--stop-timeout",
            "1",
            "--network",
            "none",
            "--cap-drop",
            "ALL",
            "--cap-add",
            "DAC_OVERRIDE",
            "--cap-add",
            "FOWNER",
            "--security-opt",
            "no-new-privileges:true",
            "-v",
            f"{workspace}:/workspace",
            "-e",
            f"CTL_LSP_LANGUAGE={target.language}",
            image,
        ]
        if dry_run:
            log(f"[dry-run] {' '.join(command)}")
            return target.label, 0.0

        log(
            f"  Starting {target.label} LSP container ({image}), "
            f"timeout {target.timeout_seconds}s (JVM startup can take 1–3 min on a small VPS)…"
        )
        process = subprocess.Popen(
            command,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            start_new_session=True,
        )
        assert process.stdin is not None
        assert process.stdout is not None

        stderr_lines: list[str] = []
        stderr_stop = threading.Event()
        stderr_thread = threading.Thread(
            target=drain_stderr,
            args=(process, stderr_lines, stderr_stop),
            daemon=True,
        )
        stderr_thread.start()

        initialize = {
            "jsonrpc": "2.0",
            "id": 1,
            "method": "initialize",
            "params": {
                "processId": os.getpid(),
                "rootUri": "file:///workspace",
                "capabilities": {},
                "trace": "off",
                "workspaceFolders": [{"uri": "file:///workspace", "name": "workspace"}],
            },
        }
        initialized = {"jsonrpc": "2.0", "method": "initialized", "params": {}}

        process.stdin.write(frame(initialize))
        process.stdin.write(frame(initialized))
        process.stdin.flush()

        buffer = bytearray()
        deadline = time.monotonic() + target.timeout_seconds
        initialize_ok = False
        last_progress = started
        while time.monotonic() < deadline:
            if process.poll() is not None:
                break
            now = time.monotonic()
            if now - last_progress >= 15:
                log(f"  … still waiting for {target.label} initialize ({int(now - started)}s elapsed)")
                last_progress = now
            ready, _, _ = select.select([process.stdout], [], [], 0.2)
            if not ready:
                continue
            chunk = process.stdout.read(4096)
            if not chunk:
                break
            buffer.extend(chunk)
            for message in drain_lsp_buffer(buffer):
                if message.get("id") == 1 and "result" in message:
                    initialize_ok = True
                    break
            if initialize_ok:
                break

        stderr_stop.set()
        stderr_thread.join(timeout=1)
        stderr = stop_process(process, stderr_lines=stderr_lines)
        elapsed = time.monotonic() - started
        if not initialize_ok:
            tail = "\n".join(stderr.strip().splitlines()[-8:])
            detail = tail or f"exit code {process.returncode}"
            raise RuntimeError(detail)
        return target.label, elapsed


def warm_targets(
    targets: tuple[WarmTarget, ...],
    *,
    dry_run: bool,
    parallel: int,
) -> list[str]:
    failures: list[str] = []
    if parallel <= 1 or dry_run:
        for target in targets:
            image = resolve_image(target)
            if not image:
                log(f"SKIP {target.label}: no local image for {target.image_env} or {target.default_image}")
                continue
            log(f"Warming LSP {target.label} ({image})…")
            try:
                label, elapsed = warm_one(target, image, dry_run)
                if not dry_run:
                    log(f"  OK: {label} ({elapsed:.1f}s)")
            except Exception as exc:  # noqa: BLE001
                failures.append(f"{target.label}: {exc}")
                log(f"  FAIL: {target.label}: {exc}")
        return failures

    runnable = [(target, resolve_image(target)) for target in targets]
    skipped = [target.label for target, image in runnable if not image]
    for label in skipped:
        log(f"SKIP {label}: no local image available")
    runnable = [(target, image) for target, image in runnable if image]
    if not runnable:
        return failures

    log(f"Warming {len(runnable)} LSP images in parallel (workers={parallel})…")
    with ThreadPoolExecutor(max_workers=parallel) as pool:
        futures = {
            pool.submit(warm_one, target, image, dry_run): target
            for target, image in runnable
        }
        for future in as_completed(futures):
            target = futures[future]
            try:
                label, elapsed = future.result()
                log(f"  OK: {label} ({elapsed:.1f}s)")
            except Exception as exc:  # noqa: BLE001
                failures.append(f"{target.label}: {exc}")
                log(f"  FAIL: {target.label}: {exc}")
    return failures


def main() -> int:
    parser = argparse.ArgumentParser(description="Warm LSP Docker images.")
    parser.add_argument(
        "--only",
        action="append",
        metavar="LABEL",
        help="Warm only these labels (java, python, typescript, vue, …). Repeatable.",
    )
    parser.add_argument(
        "--include-vue",
        action="store_true",
        help="Also warm the Vue entrypoint (same image as typescript).",
    )
    parser.add_argument(
        "--force",
        action="store_true",
        help="Run even when image IDs match the last successful warm stamp.",
    )
    parser.add_argument(
        "--parallel",
        type=int,
        default=int(os.environ.get("CTL_LSP_WARM_PARALLEL", "2")),
        help="Concurrent warm workers (default: 4, or CTL_LSP_WARM_PARALLEL).",
    )
    parser.add_argument("--dry-run", action="store_true", help="Print docker commands only.")
    args = parser.parse_args()

    selected = {label.lower() for label in args.only} if args.only else None
    targets = list(WARM_TARGETS)
    if args.include_vue or (selected is not None and "vue" in selected):
        targets.extend(OPTIONAL_WARM_TARGETS)
    if selected is not None:
        targets = [target for target in targets if target.label in selected]

    if not targets:
        print("No LSP warm targets selected.")
        return 0

    if not args.dry_run:
        cleanup_orphan_warm_containers()

    if not args.force and not args.dry_run and selected is None and not args.include_vue:
        try:
            expected = current_stamp(tuple(targets))
            recorded = load_stamp()
            if recorded == expected:
                print("LSP images already warm (unchanged since last run). Skipping.")
                print(f"  stamp: {stamp_path()}")
                print("  Force: make lsp-warm-force  or  CTL_FORCE_LSP_WARM=1 make lsp-warm")
                return 0
        except subprocess.CalledProcessError:
            pass

    failures = warm_targets(tuple(targets), dry_run=args.dry_run, parallel=max(1, args.parallel))

    if failures:
        print("LSP warm completed with failures:", file=sys.stderr)
        for item in failures:
            print(f"  - {item}", file=sys.stderr)
        return 1

    if not args.dry_run:
        merge_and_save_stamp(current_stamp(tuple(targets)))
    print("All LSP images warmed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
