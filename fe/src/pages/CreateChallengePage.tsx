import { MinusCircleOutlined, PlusOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, Button, Form, Input, InputNumber, Modal, Select, Typography } from "antd";
import { AlertTriangle, CheckCircle2, CircleDashed, Eye, Terminal } from "lucide-react";
import type { Components } from "react-markdown";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, ApiError } from "../api/client";
import type {
  ChallengeSummary,
  ChallengeValidationResponse,
  CreateChallengeRequest,
  LanguageRuntimeOption,
  ValidateChallengeRequest,
} from "../api/types";
import AppLayout from "../components/AppLayout";
import ChallengeCodeEditor, {
  type SyntaxSummary,
} from "../components/create-challenge/ChallengeCodeEditor";
import CtlCard from "../components/ui/CtlCard";
import PageHeader from "../components/ui/PageHeader";
import { ApiPaths } from "../domain/constants";
import {
  activeLanguages,
  activeRuntimesForLanguage,
  formatLanguageLabel,
  formatRuntimeLabel,
} from "../utils/languageRuntimes";

type TemplateLanguage =
  | "java"
  | "python"
  | "go"
  | "node"
  | "csharp"
  | "typescript"
  | "rust"
  | "cpp"
  | "react"
  | "vue"
  | "angular";

type FormValues = {
  slug: string;
  title: string;
  descriptionMd: string;
  difficulty: string;
  language: string;
  defaultRuntimeVersion: string;
  starterCode: string;
  lineCoveragePercent: number;
  sessionDurationMinutes: number;
  publicTests: { name: string; source: string }[];
  hiddenTests: { name: string; source: string }[];
};

type StatusKind = "ok" | "warn" | "missing";

type ValidationState = {
  result: ChallengeValidationResponse;
  fingerprint: string;
};

type PreviewValues = CreateChallengeRequest;

type StatusLineProps = {
  label: string;
  detail: string;
  state: StatusKind;
};

const EMPTY_TESTS: FormValues["publicTests"] = [];
const VALID_SLUG_PATTERN = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;
const DESCRIPTION_TEMPLATE = `## Goal
Describe the task in one or two sentences.

## Function contract
Explain the expected function, class, component, or query behavior.

## Examples
- Input: ...
  Output: ...
  Why: ...

## Constraints
- ...

## Edge cases
- ...
`;

const previewMarkdownComponents: Components = {
  h2: ({ children }) => (
    <h2 className="mb-2 mt-5 border-b border-border pb-1.5 text-xs font-semibold uppercase tracking-wide text-muted-foreground first:mt-0">
      {children}
    </h2>
  ),
  h3: ({ children }) => (
    <h3 className="mb-1.5 mt-4 text-sm font-semibold text-foreground first:mt-0">
      {children}
    </h3>
  ),
  p: ({ children }) => <p className="mb-3 leading-relaxed text-foreground">{children}</p>,
  ul: ({ children }) => (
    <ul className="my-3 list-disc space-y-1.5 pl-5 text-foreground marker:text-muted-foreground">
      {children}
    </ul>
  ),
  ol: ({ children }) => (
    <ol className="my-3 list-decimal space-y-1.5 pl-5 text-foreground marker:text-muted-foreground">
      {children}
    </ol>
  ),
  li: ({ children }) => <li className="leading-relaxed">{children}</li>,
  strong: ({ children }) => <strong className="font-semibold text-foreground">{children}</strong>,
  em: ({ children }) => <em className="text-muted-foreground">{children}</em>,
  blockquote: ({ children }) => (
    <blockquote className="my-3 border-l-2 border-emerald-500/40 bg-muted/40 py-1 pl-3 text-muted-foreground">
      {children}
    </blockquote>
  ),
  code: ({ className, children }) => {
    const isBlock = Boolean(className?.includes("language-"));
    if (isBlock) {
      return <code className={className}>{children}</code>;
    }
    return (
      <code className="rounded bg-muted px-1 py-0.5 text-[0.9em] text-foreground">
        {children}
      </code>
    );
  },
  pre: ({ children }) => (
    <pre className="my-3 overflow-auto rounded-md border border-border bg-muted p-3 text-xs text-foreground">
      {children}
    </pre>
  ),
};

function hasWrittenCode(value: unknown): boolean {
  return typeof value === "string" && value.trim().length > 0;
}

function countWrittenTests(tests: { name?: string; source?: string }[] | undefined): number {
  return (tests ?? []).filter((test) => hasWrittenCode(test.name) && hasWrittenCode(test.source))
    .length;
}

function statusForCollection(written: number, total: number): StatusKind {
  if (total === 0 || written === 0) {
    return "missing";
  }
  return written === total ? "ok" : "warn";
}

function writtenDetail(written: number, total: number): string {
  return total === 0 ? "Missing" : `${written}/${total} written`;
}

function syntaxDetail(summary: SyntaxSummary, editorCount: number): StatusLineProps {
  if (summary.errors > 0) {
    return {
      label: "Syntax",
      detail: `${summary.errors} error${summary.errors === 1 ? "" : "s"}`,
      state: "warn",
    };
  }
  if (summary.warnings > 0) {
    return {
      label: "Syntax",
      detail: `${summary.warnings} warning${summary.warnings === 1 ? "" : "s"}`,
      state: "warn",
    };
  }
  return {
    label: "Syntax",
    detail: editorCount > 0 ? "No markers" : "Pending",
    state: editorCount > 0 ? "ok" : "missing",
  };
}

function StatusLine({ label, detail, state }: StatusLineProps) {
  const Icon =
    state === "ok" ? CheckCircle2 : state === "warn" ? AlertTriangle : CircleDashed;
  const tone =
    state === "ok"
      ? "text-emerald-600 dark:text-emerald-300"
      : state === "warn"
        ? "text-amber-600 dark:text-amber-300"
        : "text-muted-foreground";
  return (
    <div className="flex items-start gap-2">
      <Icon className={`mt-0.5 size-4 shrink-0 ${tone}`} aria-hidden />
      <div className="min-w-0">
        <p className="mb-0 text-sm font-medium text-foreground">{label}</p>
        <p className="mb-0 text-xs leading-relaxed text-muted-foreground">{detail}</p>
      </div>
    </div>
  );
}

function toChallengeRequest(values: FormValues): CreateChallengeRequest {
  const publicTests = values.publicTests ?? [];
  const hiddenTests = values.hiddenTests ?? [];
  return {
    slug: (values.slug ?? "").trim().toLowerCase(),
    title: (values.title ?? "").trim(),
    descriptionMd: (values.descriptionMd ?? "").trim(),
    difficulty: values.difficulty,
    language: values.language,
    defaultRuntimeVersion: values.defaultRuntimeVersion,
    starterCode: values.starterCode ?? "",
    lineCoveragePercent: values.lineCoveragePercent,
    sessionDurationMinutes: values.sessionDurationMinutes,
    publicTests: publicTests.map((test) => ({
      name: (test.name ?? "").trim(),
      source: test.source ?? "",
    })),
    hiddenTests: hiddenTests.map((test) => ({
      name: (test.name ?? "").trim(),
      source: test.source ?? "",
    })),
  };
}

function toValidationRequest(values: FormValues): ValidateChallengeRequest {
  const publicTests = values.publicTests ?? [];
  const hiddenTests = values.hiddenTests ?? [];
  const normalizedSlug = values.slug?.trim().toLowerCase();
  return {
    slug: normalizedSlug && VALID_SLUG_PATTERN.test(normalizedSlug) ? normalizedSlug : undefined,
    language: values.language,
    defaultRuntimeVersion: values.defaultRuntimeVersion,
    starterCode: values.starterCode ?? "",
    publicTests: publicTests.map((test) => ({
      name: (test.name ?? "").trim(),
      source: test.source ?? "",
    })),
    hiddenTests: hiddenTests.map((test) => ({
      name: (test.name ?? "").trim(),
      source: test.source ?? "",
    })),
  };
}

function buildAnalysisFingerprint(values: FormValues): string {
  return JSON.stringify(toValidationRequest(values));
}

function testSummary(result: ChallengeValidationResponse): string {
  if (result.tests.length === 0) {
    return "No tests reported";
  }
  const passed = result.tests.filter((test) => test.status === "PASS").length;
  return `${passed}/${result.tests.length} passed`;
}

function compileDetail(result: ChallengeValidationResponse): string {
  if (!result.compiled) {
    return "Build failed";
  }
  if (result.compile.warnings > 0) {
    return `Compiled with ${result.compile.warnings} warning${
      result.compile.warnings === 1 ? "" : "s"
    }`;
  }
  return "Compiled";
}

function validateMutationStatusLine(
  validationState: ValidationState | null,
  isCurrent: boolean,
): StatusLineProps {
  if (!validationState) {
    return {
      label: "Build analysis",
      detail: "Not run",
      state: "missing",
    };
  }
  if (!isCurrent) {
    return {
      label: "Build analysis",
      detail: "Changed since last run",
      state: "warn",
    };
  }
  return {
    label: "Build analysis",
    detail: compileDetail(validationState.result),
    state: validationState.result.compiled ? "ok" : "warn",
  };
}

function BuildAnalysisPanel({
  result,
  isCurrent,
}: {
  result: ChallengeValidationResponse;
  isCurrent: boolean;
}) {
  const failingTests = result.tests.filter((test) => test.status !== "PASS").slice(0, 3);
  const logs = [result.logs.stderrTruncated, result.logs.stdoutTruncated]
    .filter(Boolean)
    .join("\n\n");
  return (
    <div className="rounded-lg border border-border bg-muted/30 px-3 py-3">
      <div className="mb-3 flex items-center gap-2">
        <Terminal className="size-4 text-muted-foreground" aria-hidden />
        <p className="mb-0 text-sm font-medium text-foreground">Build analysis</p>
      </div>
      <div className="flex flex-col gap-3">
        {!isCurrent && (
          <Alert
            type="warning"
            showIcon
            message="Changed since last analysis"
            className="!py-2"
          />
        )}
        <StatusLine
          label="Compile"
          detail={compileDetail(result)}
          state={result.compiled ? "ok" : "warn"}
        />
        <StatusLine
          label="Tests"
          detail={testSummary(result)}
          state={result.tests.length === 0 ? "missing" : result.passed ? "ok" : "warn"}
        />
        {result.message && (
          <p className="mb-0 rounded-md bg-background px-3 py-2 text-xs leading-relaxed text-muted-foreground">
            {result.message}
          </p>
        )}
        {result.compile.messages.length > 0 && (
          <div className="rounded-md bg-background px-3 py-2">
            <p className="mb-2 text-xs font-medium text-foreground">Compiler messages</p>
            <ul className="m-0 flex list-none flex-col gap-2 p-0">
              {result.compile.messages.slice(0, 4).map((message) => (
                <li
                  key={`${message.file}:${message.line}:${message.message}`}
                  className="text-xs leading-relaxed text-muted-foreground"
                >
                  <span className="font-medium text-foreground">
                    {message.file}:{message.line}
                  </span>{" "}
                  {message.message}
                </li>
              ))}
            </ul>
          </div>
        )}
        {failingTests.length > 0 && (
          <div className="rounded-md bg-background px-3 py-2">
            <p className="mb-2 text-xs font-medium text-foreground">Failing tests</p>
            <ul className="m-0 flex list-none flex-col gap-2 p-0">
              {failingTests.map((test) => (
                <li key={test.name} className="text-xs leading-relaxed text-muted-foreground">
                  <span className="font-medium text-foreground">{test.name}</span>
                  {test.message ? ` - ${test.message}` : ""}
                </li>
              ))}
            </ul>
          </div>
        )}
        {logs && (
          <details className="rounded-md bg-background px-3 py-2">
            <summary className="cursor-pointer text-xs font-medium text-foreground">
              Runner logs
            </summary>
            <pre className="mt-2 max-h-44 overflow-auto whitespace-pre-wrap break-words text-[11px] leading-relaxed text-muted-foreground">
              {logs}
            </pre>
          </details>
        )}
      </div>
    </div>
  );
}

function DescriptionGuidelines() {
  const items = [
    "Start with the learner goal, not the implementation.",
    "State the expected input/output contract and match the starter code.",
    "Include 1-3 examples with input, output, and a short reason.",
    "Name constraints and edge cases that public tests should cover.",
    "Avoid revealing hidden-test specifics or the intended solution strategy.",
  ];

  return (
    <div className="mt-3 border-l-2 border-emerald-500/40 pl-3">
      <p className="mb-2 text-sm font-medium text-foreground">Description guidelines</p>
      <ul className="m-0 flex list-disc flex-col gap-1 pl-4 text-xs leading-relaxed text-muted-foreground">
        {items.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
    </div>
  );
}

function MetadataTile({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-md border border-border bg-muted/30 px-3 py-2">
      <p className="mb-1 text-xs font-medium uppercase tracking-wide text-muted-foreground">
        {label}
      </p>
      <p className="mb-0 text-sm font-medium text-foreground">{value}</p>
    </div>
  );
}

function ChallengePreviewModal({
  open,
  values,
  publishing,
  onClose,
  onPublish,
}: {
  open: boolean;
  values: PreviewValues | null;
  publishing: boolean;
  onClose: () => void;
  onPublish: () => void;
}) {
  const visiblePublicTests = values?.publicTests.filter((test) => hasWrittenCode(test.name)) ?? [];
  const hiddenTests = values?.hiddenTests.filter((test) => hasWrittenCode(test.name)) ?? [];

  return (
    <Modal
      open={open}
      title="Preview challenge"
      width={960}
      onCancel={onClose}
      footer={[
        <Button key="edit" onClick={onClose}>
          Edit
        </Button>,
        <Button
          key="publish"
          type="primary"
          loading={publishing}
          onClick={onPublish}
          className="!bg-emerald-600 hover:!bg-emerald-700 dark:hover:!bg-emerald-500"
        >
          Publish challenge
        </Button>,
      ]}
    >
      {values && (
        <div className="max-h-[72vh] overflow-y-auto pr-1">
          <div className="mb-4 rounded-lg border border-border bg-background p-4">
            <p className="mb-1 text-xs font-medium uppercase tracking-wide text-muted-foreground">
              Learner view
            </p>
            <h2 className="mb-2 text-xl font-semibold text-foreground">{values.title}</h2>
            <p className="mb-0 break-all text-sm text-muted-foreground">/{values.slug}</p>
          </div>

          <div className="mb-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            <MetadataTile label="Language" value={formatLanguageLabel(values.language)} />
            <MetadataTile
              label="Runtime"
              value={formatRuntimeLabel(values.language, values.defaultRuntimeVersion)}
            />
            <MetadataTile label="Difficulty" value={values.difficulty} />
            <MetadataTile
              label="Session"
              value={`${values.sessionDurationMinutes ?? 30} min`}
            />
          </div>

          <div className="mb-4 rounded-lg border border-border bg-background p-4">
            <p className="mb-3 text-sm font-semibold text-foreground">Problem statement</p>
            <div className="rounded-md border border-border bg-card px-4 py-3">
              <ReactMarkdown remarkPlugins={[remarkGfm]} components={previewMarkdownComponents}>
                {values.descriptionMd}
              </ReactMarkdown>
            </div>
          </div>

          <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_280px]">
            <div className="rounded-lg border border-border bg-background p-4">
              <p className="mb-3 text-sm font-semibold text-foreground">Starter code</p>
              <pre className="max-h-72 overflow-auto rounded-md border border-border bg-muted p-3 text-xs leading-relaxed text-foreground">
                {values.starterCode}
              </pre>
            </div>

            <div className="flex flex-col gap-4">
              <div className="rounded-lg border border-border bg-background p-4">
                <p className="mb-3 text-sm font-semibold text-foreground">Public tests</p>
                {visiblePublicTests.length > 0 ? (
                  <ul className="m-0 flex list-none flex-col gap-2 p-0">
                    {visiblePublicTests.map((test) => (
                      <li
                        key={test.name}
                        className="rounded-md border border-border bg-muted/30 px-3 py-2 text-sm text-foreground"
                      >
                        {test.name}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="mb-0 text-sm text-muted-foreground">No public tests named.</p>
                )}
              </div>

              <div className="rounded-lg border border-border bg-background p-4">
                <p className="mb-1 text-sm font-semibold text-foreground">Hidden tests</p>
                <p className="mb-0 text-sm text-muted-foreground">
                  {hiddenTests.length} hidden test{hiddenTests.length === 1 ? "" : "s"} will run
                  on submit.
                </p>
              </div>

              <div className="rounded-lg border border-border bg-background p-4">
                <p className="mb-1 text-sm font-semibold text-foreground">Coverage gate</p>
                <p className="mb-0 text-sm text-muted-foreground">
                  {values.lineCoveragePercent}% minimum line coverage.
                </p>
              </div>
            </div>
          </div>
        </div>
      )}
    </Modal>
  );
}

function isTemplateLanguage(language: string): language is TemplateLanguage {
  return (
    language === "java"
    || language === "python"
    || language === "go"
    || language === "node"
    || language === "csharp"
    || language === "typescript"
    || language === "rust"
    || language === "cpp"
    || language === "react"
    || language === "vue"
    || language === "angular"
  );
}

const JAVA_STARTER = `package com.challenge;

public class Solution {

    public static String reverse(String input) {
        throw new UnsupportedOperationException("TODO");
    }
}
`;

const JAVA_PUBLIC_TEST = `package com.challenge.tests;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExamplePublicTest {

    @Test
    void sampleCase() {
        assertEquals("expected", Solution.reverse("input"));
    }
}
`;

const JAVA_HIDDEN_TEST = `package com.challenge.hidden;

import com.challenge.Solution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExampleHiddenTest {

    @Test
    void hiddenCase() {
        assertEquals("expected", Solution.reverse("input"));
    }
}
`;

const PYTHON_STARTER = `def solve(value: str) -> str:
    raise NotImplementedError("TODO")
`;

const PYTHON_PUBLIC_TEST = `from solution import solve


def test_sample():
    assert solve("input") == "expected"
`;

const PYTHON_HIDDEN_TEST = `from solution import solve


def test_hidden():
    assert solve("input") == "expected"
`;

const GO_STARTER = `package solution

func Solve(value string) string {
	panic("TODO")
}
`;

const GO_PUBLIC_TEST = `package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestSample(t *testing.T) {
	if solution.Solve("input") != "expected" {
		t.Fatal("expected match")
	}
}
`;

const GO_HIDDEN_TEST = `package solution_test

import (
	"testing"

	"challenge/solution"
)

func TestHidden(t *testing.T) {
	if solution.Solve("other") != "expected" {
		t.Fatal("expected match")
	}
}
`;

const NODE_STARTER = `function solve(value) {
  throw new Error("TODO");
}

module.exports = { solve };
`;

const NODE_PUBLIC_TEST = `const { test } = require("node:test");
const assert = require("node:assert/strict");
const { solve } = require("../solution.js");

test("sample", () => {
  assert.equal(solve("input"), "expected");
});
`;

const NODE_HIDDEN_TEST = `const { test } = require("node:test");
const assert = require("node:assert/strict");
const { solve } = require("../solution.js");

test("hidden", () => {
  assert.equal(solve("other"), "expected");
});
`;

const CSHARP_STARTER = `namespace Challenge;

public static class Solution
{
    public static string Solve(string input)
    {
        throw new NotImplementedException();
    }
}
`;

const CSHARP_PUBLIC_TEST = `using Challenge;
using Xunit;

namespace Challenge.Tests;

public class ExampleTests
{
    [Fact]
    public void Sample()
    {
        Assert.Equal("expected", Solution.Solve("input"));
    }
}
`;

const CSHARP_HIDDEN_TEST = `using Challenge;
using Xunit;

namespace Challenge.Tests;

public class HiddenTests
{
    [Fact]
    public void Hidden()
    {
        Assert.Equal("expected", Solution.Solve("other"));
    }
}
`;

const TYPESCRIPT_STARTER = `export function solve(value: string): string {
  throw new Error("TODO");
}
`;

const TYPESCRIPT_PUBLIC_TEST = `import { test } from "node:test";
import assert from "node:assert/strict";
import { solve } from "../solution.ts";

test("sample", () => {
  assert.equal(solve("input"), "expected");
});
`;

const TYPESCRIPT_HIDDEN_TEST = `import { test } from "node:test";
import assert from "node:assert/strict";
import { solve } from "../solution.ts";

test("hidden", () => {
  assert.equal(solve("other"), "expected");
});
`;

const RUST_STARTER = `pub fn solve(value: &str) -> String {
    todo!()
}
`;

const RUST_PUBLIC_TEST = `use challenge::solve;

#[test]
fn sample() {
    assert_eq!(solve("input"), "expected");
}
`;

const RUST_HIDDEN_TEST = `use challenge::solve;

#[test]
fn hidden() {
    assert_eq!(solve("other"), "expected");
}
`;

const CPP_STARTER = `#include <stdexcept>
#include <string>

std::string solve(const std::string& input) {
    throw std::runtime_error("TODO");
}
`;

const CPP_PUBLIC_TEST = `#include <catch2/catch_test_macros.hpp>
#include <string>

extern std::string solve(const std::string& input);

TEST_CASE("sample") {
    REQUIRE(solve("input") == "expected");
}
`;

const CPP_HIDDEN_TEST = `#include <catch2/catch_test_macros.hpp>
#include <string>

extern std::string solve(const std::string& input);

TEST_CASE("hidden") {
    REQUIRE(solve("other") == "expected");
}
`;

const REACT_STARTER = `export function Placeholder() {
  return <p>TODO</p>;
}
`;

const REACT_PUBLIC_TEST = `import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Placeholder } from "../solution";

describe("Placeholder", () => {
  it("renders", () => {
    render(<Placeholder />);
    expect(screen.getByText("TODO")).toBeInTheDocument();
  });
});
`;

const REACT_HIDDEN_TEST = REACT_PUBLIC_TEST;

const VUE_STARTER = `<script setup lang="ts">
</script>
<template>
  <p>TODO</p>
</template>
`;

const VUE_PUBLIC_TEST = `import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import Placeholder from "../solution.vue";

describe("Placeholder", () => {
  it("renders", () => {
    const wrapper = mount(Placeholder);
    expect(wrapper.text()).toContain("TODO");
  });
});
`;

const VUE_HIDDEN_TEST = VUE_PUBLIC_TEST;

const ANGULAR_STARTER = `import { Pipe, PipeTransform } from "@angular/core";

@Pipe({ name: "sample", standalone: true })
export class SamplePipe implements PipeTransform {
  transform(value: string): string {
    throw new Error("TODO");
  }
}
`;

const ANGULAR_PUBLIC_TEST = `import { describe, expect, it } from "vitest";
import { SamplePipe } from "../solution";

describe("SamplePipe", () => {
  it("works", () => {
    const pipe = new SamplePipe();
    expect(pipe.transform("x")).toBe("x");
  });
});
`;

const ANGULAR_HIDDEN_TEST = ANGULAR_PUBLIC_TEST;

function templatesFor(language: TemplateLanguage) {
  if (language === "python") {
    return {
      starterCode: PYTHON_STARTER,
      publicTests: [{ name: "test_sample", source: PYTHON_PUBLIC_TEST }],
      hiddenTests: [{ name: "test_hidden", source: PYTHON_HIDDEN_TEST }],
    };
  }
  if (language === "go") {
    return {
      starterCode: GO_STARTER,
      publicTests: [{ name: "sample_test", source: GO_PUBLIC_TEST }],
      hiddenTests: [{ name: "hidden_test", source: GO_HIDDEN_TEST }],
    };
  }
  if (language === "node") {
    return {
      starterCode: NODE_STARTER,
      publicTests: [{ name: "sample.test", source: NODE_PUBLIC_TEST }],
      hiddenTests: [{ name: "hidden.test", source: NODE_HIDDEN_TEST }],
    };
  }
  if (language === "csharp") {
    return {
      starterCode: CSHARP_STARTER,
      publicTests: [{ name: "ExampleTests", source: CSHARP_PUBLIC_TEST }],
      hiddenTests: [{ name: "HiddenTests", source: CSHARP_HIDDEN_TEST }],
    };
  }
  if (language === "typescript") {
    return {
      starterCode: TYPESCRIPT_STARTER,
      publicTests: [{ name: "sample.test", source: TYPESCRIPT_PUBLIC_TEST }],
      hiddenTests: [{ name: "hidden.test", source: TYPESCRIPT_HIDDEN_TEST }],
    };
  }
  if (language === "rust") {
    return {
      starterCode: RUST_STARTER,
      publicTests: [{ name: "public_tests", source: RUST_PUBLIC_TEST }],
      hiddenTests: [{ name: "hidden_tests", source: RUST_HIDDEN_TEST }],
    };
  }
  if (language === "cpp") {
    return {
      starterCode: CPP_STARTER,
      publicTests: [{ name: "public_tests", source: CPP_PUBLIC_TEST }],
      hiddenTests: [{ name: "hidden_tests", source: CPP_HIDDEN_TEST }],
    };
  }
  if (language === "react") {
    return {
      starterCode: REACT_STARTER,
      publicTests: [{ name: "sample.test", source: REACT_PUBLIC_TEST }],
      hiddenTests: [{ name: "hidden.test", source: REACT_HIDDEN_TEST }],
    };
  }
  if (language === "vue") {
    return {
      starterCode: VUE_STARTER,
      publicTests: [{ name: "sample.test", source: VUE_PUBLIC_TEST }],
      hiddenTests: [{ name: "hidden.test", source: VUE_HIDDEN_TEST }],
    };
  }
  if (language === "angular") {
    return {
      starterCode: ANGULAR_STARTER,
      publicTests: [{ name: "sample.test", source: ANGULAR_PUBLIC_TEST }],
      hiddenTests: [{ name: "hidden.test", source: ANGULAR_HIDDEN_TEST }],
    };
  }
  return {
    starterCode: JAVA_STARTER,
    publicTests: [{ name: "ExamplePublic", source: JAVA_PUBLIC_TEST }],
    hiddenTests: [{ name: "ExampleHidden", source: JAVA_HIDDEN_TEST }],
  };
}

export default function CreateChallengePage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [form] = Form.useForm<FormValues>();
  const [error, setError] = useState<string | null>(null);
  const [syntaxByModel, setSyntaxByModel] = useState<Record<string, SyntaxSummary>>({});
  const [validationState, setValidationState] = useState<ValidationState | null>(null);
  const [previewValues, setPreviewValues] = useState<PreviewValues | null>(null);
  const slug = Form.useWatch("slug", form) ?? "";
  const language = Form.useWatch("language", form) ?? "";
  const defaultRuntimeVersion = Form.useWatch("defaultRuntimeVersion", form) ?? "";
  const lineCoveragePercent = Form.useWatch("lineCoveragePercent", form) ?? 80;
  const starterCode = Form.useWatch("starterCode", form) ?? "";
  const publicTests = Form.useWatch("publicTests", form) ?? EMPTY_TESTS;
  const hiddenTests = Form.useWatch("hiddenTests", form) ?? EMPTY_TESTS;

  const handleSyntaxChange = useCallback((modelId: string, summary: SyntaxSummary) => {
    setSyntaxByModel((current) => {
      const previous = current[modelId];
      if (previous?.errors === summary.errors && previous.warnings === summary.warnings) {
        return current;
      }
      return { ...current, [modelId]: summary };
    });
  }, []);

  const activeSyntaxModelIds = useMemo(
    () => [
      "starterCode",
      ...publicTests.map((_, index) => `publicTests.${index}.source`),
      ...hiddenTests.map((_, index) => `hiddenTests.${index}.source`),
    ],
    [hiddenTests, publicTests],
  );

  useEffect(() => {
    const activeIds = new Set(activeSyntaxModelIds);
    setSyntaxByModel((current) =>
      Object.fromEntries(Object.entries(current).filter(([key]) => activeIds.has(key))),
    );
  }, [activeSyntaxModelIds]);

  const writtenPublicTests = useMemo(() => countWrittenTests(publicTests), [publicTests]);
  const writtenHiddenTests = useMemo(() => countWrittenTests(hiddenTests), [hiddenTests]);
  const syntaxSummary = useMemo(
    () =>
      Object.values(syntaxByModel).reduce<SyntaxSummary>(
        (total, next) => ({
          errors: total.errors + next.errors,
          warnings: total.warnings + next.warnings,
        }),
        { errors: 0, warnings: 0 },
      ),
    [syntaxByModel],
  );
  const analysisFingerprint = useMemo(
    () =>
      buildAnalysisFingerprint({
        slug,
        title: "",
        descriptionMd: "",
        difficulty: "easy",
        language,
        defaultRuntimeVersion,
        starterCode,
        lineCoveragePercent,
        sessionDurationMinutes: 30,
        publicTests,
        hiddenTests,
      }),
    [
      defaultRuntimeVersion,
      hiddenTests,
      language,
      lineCoveragePercent,
      publicTests,
      slug,
      starterCode,
    ],
  );
  const isValidationCurrent = validationState?.fingerprint === analysisFingerprint;

  const readinessItems = useMemo<StatusLineProps[]>(
    () => [
      {
        label: "Starter code",
        detail: hasWrittenCode(starterCode) ? "Written" : "Missing",
        state: hasWrittenCode(starterCode) ? "ok" : "missing",
      },
      {
        label: "Public tests",
        detail: writtenDetail(writtenPublicTests, publicTests.length),
        state: statusForCollection(writtenPublicTests, publicTests.length),
      },
      {
        label: "Hidden tests",
        detail: writtenDetail(writtenHiddenTests, hiddenTests.length),
        state: statusForCollection(writtenHiddenTests, hiddenTests.length),
      },
      syntaxDetail(syntaxSummary, Object.keys(syntaxByModel).length),
      validateMutationStatusLine(validationState, isValidationCurrent),
    ],
    [
      hiddenTests.length,
      isValidationCurrent,
      publicTests.length,
      starterCode,
      syntaxByModel,
      syntaxSummary,
      validationState,
      writtenHiddenTests,
      writtenPublicTests,
    ],
  );

  const languagesQuery = useQuery({
    queryKey: ["languages"],
    queryFn: () => apiFetch<LanguageRuntimeOption[]>(ApiPaths.LANGUAGES),
  });

  const languageOptions = useMemo(
    () =>
      activeLanguages(languagesQuery.data ?? []).map((lang) => ({
        value: lang,
        label: formatLanguageLabel(lang),
      })),
    [languagesQuery.data],
  );

  const runtimeOptions = useMemo(
    () =>
      activeRuntimesForLanguage(languagesQuery.data ?? [], language).map((r) => ({
        value: r.version,
        label: formatRuntimeLabel(r.language, r.version),
      })),
    [languagesQuery.data, language],
  );

  useEffect(() => {
    const runtimes = languagesQuery.data;
    if (!runtimes?.length) {
      return;
    }
    const langs = activeLanguages(runtimes);
    if (langs.length === 0) {
      return;
    }
    const currentLang = form.getFieldValue("language");
    const nextLang = langs.includes(currentLang) ? currentLang : langs[0];
    const versions = activeRuntimesForLanguage(runtimes, nextLang);
    const currentVersion = form.getFieldValue("defaultRuntimeVersion");
    const nextVersion =
      versions.find((v) => v.version === currentVersion)?.version
      ?? versions[0]?.version
      ?? "";
    const patch: Partial<FormValues> = {
      language: nextLang,
      defaultRuntimeVersion: nextVersion,
    };
    if (isTemplateLanguage(nextLang) && nextLang !== currentLang) {
      Object.assign(patch, templatesFor(nextLang));
    }
    form.setFieldsValue(patch);
  }, [languagesQuery.data, form]);

  const defaults = useMemo(() => {
    const lang: TemplateLanguage = "java";
    const t = templatesFor(lang);
    return {
      slug: "",
      title: "",
      descriptionMd: DESCRIPTION_TEMPLATE,
      difficulty: "easy",
      language: lang,
      defaultRuntimeVersion: "",
      lineCoveragePercent: 80,
      sessionDurationMinutes: 30,
      ...t,
    };
  }, []);

  const createMutation = useMutation({
    mutationFn: (body: CreateChallengeRequest) =>
      apiFetch<ChallengeSummary>(ApiPaths.CHALLENGES, {
        method: "POST",
        body: JSON.stringify(body),
    }),
    onSuccess: (created) => {
      setPreviewValues(null);
      void queryClient.invalidateQueries({ queryKey: ["challenges"] });
      navigate(`/challenges/${created.slug}`, { replace: true });
    },
    onError: (e) => {
      setPreviewValues(null);
      setError(e instanceof ApiError ? e.message : "Could not create challenge");
    },
  });

  const validateMutation = useMutation({
    mutationFn: ({
      body,
    }: {
      body: ValidateChallengeRequest;
      fingerprint: string;
    }) =>
      apiFetch<ChallengeValidationResponse>(ApiPaths.CHALLENGES_VALIDATE, {
        method: "POST",
        body: JSON.stringify(body),
      }),
    onSuccess: (result, variables) => {
      setValidationState({ result, fingerprint: variables.fingerprint });
    },
    onError: (e) => {
      setValidationState(null);
      setError(e instanceof ApiError ? e.message : "Could not run build analysis");
    },
  });

  const handleValidate = async () => {
    setError(null);
    try {
      await form.validateFields([
        "language",
        "defaultRuntimeVersion",
        "starterCode",
        "publicTests",
        "hiddenTests",
      ]);
      const values = form.getFieldsValue(true) as FormValues;
      validateMutation.mutate({
        body: toValidationRequest(values),
        fingerprint: buildAnalysisFingerprint(values),
      });
    } catch {
      // Ant Design has already marked the invalid fields.
    }
  };

  const onFinish = (values: FormValues) => {
    setError(null);
    setPreviewValues(toChallengeRequest(values));
  };

  const handlePublishPreview = () => {
    if (!previewValues) {
      return;
    }
    setError(null);
    createMutation.mutate(previewValues);
  };

  return (
    <AppLayout>
      <PageHeader
        title="Create challenge"
        description="Compose the prompt, starter code, and test suites in one publishing flow."
        extra={
          <Link
            to="/challenges"
            className="inline-flex min-h-10 items-center text-sm font-medium text-emerald-600 no-underline hover:text-emerald-700 dark:text-emerald-400 dark:hover:text-emerald-300"
          >
            Back to list
          </Link>
        }
      />

      <div className="flex flex-col gap-4">
        {error && <Alert type="error" showIcon message={error} role="alert" />}

        {languagesQuery.isLoading && (
          <Alert
            type="info"
            showIcon
            message="Loading available languages…"
            className="border-border"
          />
        )}
        {languagesQuery.error && (
          <Alert
            type="error"
            showIcon
            message={(languagesQuery.error as Error).message}
            role="alert"
          />
        )}

        <Form<FormValues>
          form={form}
          layout="vertical"
          initialValues={defaults}
          onFinish={onFinish}
          requiredMark={false}
          disabled={languagesQuery.isLoading || languageOptions.length === 0}
          onValuesChange={(changed) => {
            if (changed.language && languagesQuery.data) {
              const lang = changed.language;
              const versions = activeRuntimesForLanguage(languagesQuery.data, lang);
              const patch: Partial<FormValues> = {
                defaultRuntimeVersion: versions[0]?.version ?? "", // newest first (see languageRuntimes)
              };
              if (isTemplateLanguage(lang)) {
                Object.assign(patch, templatesFor(lang));
              }
              form.setFieldsValue(patch);
            }
          }}
        >
          <div className="flex flex-col gap-6">
            <CtlCard
              title="Challenge setup"
              extra={
                <Typography.Text className="!text-muted-foreground text-xs">
                  Basics, runtime, and prompt
                </Typography.Text>
              }
            >
              <div className="grid gap-6 lg:grid-cols-[minmax(0,1.1fr)_minmax(280px,0.9fr)]">
                <div className="min-w-0">
                  <div className="grid gap-4 md:grid-cols-2">
                    <Form.Item
                      label={<span className="text-foreground">Slug</span>}
                      name="slug"
                      rules={[
                        { required: true, message: "Slug is required" },
                        {
                          pattern: /^[a-z0-9]+(?:-[a-z0-9]+)*$/,
                          message: "Use lowercase letters, numbers, and hyphens",
                        },
                      ]}
                      extra={
                        <Typography.Text className="!text-muted-foreground text-xs">
                          URL-safe id, e.g. two-sum
                        </Typography.Text>
                      }
                    >
                      <Input placeholder="my-challenge" />
                    </Form.Item>
                    <Form.Item
                      label={<span className="text-foreground">Title</span>}
                      name="title"
                      rules={[{ required: true, message: "Title is required" }]}
                    >
                      <Input placeholder="Two Sum" />
                    </Form.Item>
                  </div>

                  <Form.Item
                    label={<span className="text-foreground">Description (Markdown)</span>}
                    name="descriptionMd"
                    rules={[{ required: true, message: "Description is required" }]}
                    className="!mb-0"
                  >
                    <Input.TextArea
                      rows={12}
                      placeholder="Explain the task, examples, constraints..."
                    />
                  </Form.Item>
                  <DescriptionGuidelines />
                </div>

                <div className="grid min-w-0 gap-4 sm:grid-cols-2">
                  <Form.Item
                    label={<span className="text-foreground">Difficulty</span>}
                    name="difficulty"
                    rules={[{ required: true }]}
                  >
                    <Select
                      options={[
                        { value: "easy", label: "Easy" },
                        { value: "medium", label: "Medium" },
                        { value: "hard", label: "Hard" },
                      ]}
                    />
                  </Form.Item>
                  <Form.Item
                    label={<span className="text-foreground">Language</span>}
                    name="language"
                    rules={[{ required: true, message: "Select a language" }]}
                  >
                    <Select
                      options={languageOptions}
                      placeholder="Select language"
                      aria-label="Challenge language"
                    />
                  </Form.Item>
                  <Form.Item
                    label={<span className="text-foreground">Default runtime</span>}
                    name="defaultRuntimeVersion"
                    rules={[{ required: true, message: "Select a runtime version" }]}
                    extra={
                      <Typography.Text className="!text-muted-foreground text-xs">
                        {defaultRuntimeVersion
                          ? formatRuntimeLabel(language, defaultRuntimeVersion)
                          : "Select a runtime after choosing language"}
                      </Typography.Text>
                    }
                  >
                    <Select
                      options={runtimeOptions}
                      disabled={runtimeOptions.length === 0}
                      placeholder={language ? "Select runtime" : "Select language first"}
                      aria-label="Default runtime version"
                    />
                  </Form.Item>
                  <Form.Item
                    label={<span className="text-foreground">Line coverage %</span>}
                    name="lineCoveragePercent"
                    rules={[{ required: true }]}
                  >
                    <InputNumber min={0} max={100} className="w-full" />
                  </Form.Item>
                  <Form.Item
                    label={<span className="text-foreground">Session time (min)</span>}
                    name="sessionDurationMinutes"
                    rules={[{ required: true, message: "Session duration is required" }]}
                    extra={
                      <Typography.Text className="!text-muted-foreground text-xs">
                        Allotted workspace time after the learner starts Run or Submit (5-480 min).
                      </Typography.Text>
                    }
                  >
                    <InputNumber min={5} max={480} className="w-full" />
                  </Form.Item>
                </div>
              </div>
            </CtlCard>

            <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_340px]">
              <div className="flex min-w-0 flex-col gap-6">
                <CtlCard title="Starter code">
                <Form.Item
                  label={<span className="text-foreground">Solution template</span>}
                  name="starterCode"
                  rules={[
                    {
                      validator: async (_, value) => {
                        if (!hasWrittenCode(value)) {
                          return Promise.reject(new Error("Starter code is required"));
                        }
                      },
                    },
                  ]}
                  className="!mb-0"
                >
                  <ChallengeCodeEditor
                    ariaLabel="Starter code editor"
                    language={language}
                    modelId="starterCode"
                    height={360}
                    onSyntaxChange={handleSyntaxChange}
                  />
                </Form.Item>
              </CtlCard>

              <CtlCard
                title="Public tests"
                extra={
                  <Typography.Text className="!text-muted-foreground text-xs">
                    Visible in workspace
                  </Typography.Text>
                }
              >
                <Typography.Paragraph className="!mb-4 !text-muted-foreground text-sm">
                  Java public tests can use package{" "}
                  <code className="text-emerald-700 dark:text-emerald-400">
                    com.challenge.tests
                  </code>
                  .
                </Typography.Paragraph>
                <Form.List
                  name="publicTests"
                  rules={[
                    {
                      validator: async (_, value) => {
                        if (!value || value.length < 1) {
                          return Promise.reject(new Error("Add at least one public test"));
                        }
                      },
                    },
                  ]}
                >
                  {(fields, { add, remove }) => (
                    <div className="flex flex-col gap-4">
                      {fields.map(({ key, name, ...rest }) => (
                        <div
                          key={key}
                          className="rounded-md border border-border bg-muted/30 p-4"
                        >
                          <div className="mb-3 flex items-center justify-between gap-2">
                            <Form.Item
                              {...rest}
                              name={[name, "name"]}
                              label={<span className="text-foreground">Test name</span>}
                              rules={[{ required: true, message: "Name required" }]}
                              className="!mb-0 flex-1"
                            >
                              <Input placeholder="ExamplePublic" />
                            </Form.Item>
                            <Button
                              type="text"
                              danger
                              icon={<MinusCircleOutlined aria-hidden />}
                              onClick={() => remove(name)}
                              disabled={fields.length <= 1}
                              aria-label="Remove public test"
                            />
                          </div>
                          <Form.Item
                            {...rest}
                            name={[name, "source"]}
                            label={<span className="text-foreground">Source</span>}
                            rules={[
                              {
                                validator: async (_, value) => {
                                  if (!hasWrittenCode(value)) {
                                    return Promise.reject(new Error("Source required"));
                                  }
                                },
                              },
                            ]}
                            className="!mb-0"
                          >
                            <ChallengeCodeEditor
                              ariaLabel="Public test source editor"
                              language={language}
                              modelId={`publicTests.${name}.source`}
                              height={250}
                              onSyntaxChange={handleSyntaxChange}
                            />
                          </Form.Item>
                        </div>
                      ))}
                      <Button
                        type="dashed"
                        onClick={() => add({ name: "", source: "" })}
                        icon={<PlusOutlined aria-hidden />}
                        block
                      >
                        Add public test
                      </Button>
                    </div>
                  )}
                </Form.List>
              </CtlCard>

              <CtlCard
                title="Hidden tests"
                extra={
                  <Typography.Text className="!text-muted-foreground text-xs">
                    Submit only
                  </Typography.Text>
                }
              >
                <Typography.Paragraph className="!mb-4 !text-muted-foreground text-sm">
                  Java hidden tests use package{" "}
                  <code className="text-emerald-700 dark:text-emerald-400">
                    com.challenge.hidden
                  </code>
                  .
                </Typography.Paragraph>
                <Form.List
                  name="hiddenTests"
                  rules={[
                    {
                      validator: async (_, value) => {
                        if (!value || value.length < 1) {
                          return Promise.reject(new Error("Add at least one hidden test"));
                        }
                      },
                    },
                  ]}
                >
                  {(fields, { add, remove }) => (
                    <div className="flex flex-col gap-4">
                      {fields.map(({ key, name, ...rest }) => (
                        <div
                          key={key}
                          className="rounded-md border border-border bg-muted/30 p-4"
                        >
                          <div className="mb-3 flex items-center justify-between gap-2">
                            <Form.Item
                              {...rest}
                              name={[name, "name"]}
                              label={<span className="text-foreground">Test name</span>}
                              rules={[{ required: true, message: "Name required" }]}
                              className="!mb-0 flex-1"
                            >
                              <Input placeholder="ExampleHidden" />
                            </Form.Item>
                            <Button
                              type="text"
                              danger
                              icon={<MinusCircleOutlined aria-hidden />}
                              onClick={() => remove(name)}
                              disabled={fields.length <= 1}
                              aria-label="Remove hidden test"
                            />
                          </div>
                          <Form.Item
                            {...rest}
                            name={[name, "source"]}
                            label={<span className="text-foreground">Source</span>}
                            rules={[
                              {
                                validator: async (_, value) => {
                                  if (!hasWrittenCode(value)) {
                                    return Promise.reject(new Error("Source required"));
                                  }
                                },
                              },
                            ]}
                            className="!mb-0"
                          >
                            <ChallengeCodeEditor
                              ariaLabel="Hidden test source editor"
                              language={language}
                              modelId={`hiddenTests.${name}.source`}
                              height={250}
                              onSyntaxChange={handleSyntaxChange}
                            />
                          </Form.Item>
                        </div>
                      ))}
                      <Button
                        type="dashed"
                        onClick={() => add({ name: "", source: "" })}
                        icon={<PlusOutlined aria-hidden />}
                        block
                      >
                        Add hidden test
                      </Button>
                    </div>
                  )}
                </Form.List>
              </CtlCard>
            </div>

            <aside className="xl:sticky xl:top-24 xl:self-start" aria-label="Publishing">
              <CtlCard title="Publish">
                <div className="flex flex-col gap-4">
                  <div className="rounded-lg border border-border bg-muted/30 px-3 py-3">
                    <p className="mb-1 text-sm font-medium text-foreground">
                      Runtime target
                    </p>
                    <p className="mb-0 text-xs leading-relaxed text-muted-foreground">
                      {defaultRuntimeVersion
                        ? formatRuntimeLabel(language, defaultRuntimeVersion)
                        : "Select language and runtime"}
                    </p>
                  </div>

                  <div className="rounded-lg border border-border bg-muted/30 px-3 py-3">
                    <p className="mb-1 text-sm font-medium text-foreground">
                      Coverage gate
                    </p>
                    <p className="mb-0 text-xs leading-relaxed text-muted-foreground">
                      {lineCoveragePercent}% minimum line coverage.
                    </p>
                  </div>

                  <div className="rounded-lg border border-border bg-muted/30 px-3 py-3">
                    <p className="mb-3 text-sm font-medium text-foreground">
                      Readiness
                    </p>
                    <div className="flex flex-col gap-3">
                      {readinessItems.map((item) => (
                        <StatusLine
                          key={item.label}
                          label={item.label}
                          detail={item.detail}
                          state={item.state}
                        />
                      ))}
                    </div>
                  </div>

                  <Button
                    htmlType="button"
                    onClick={handleValidate}
                    loading={validateMutation.isPending}
                    icon={<Terminal className="size-4" aria-hidden />}
                    block
                  >
                    Run build analysis
                  </Button>

                  {validationState && (
                    <BuildAnalysisPanel
                      result={validationState.result}
                      isCurrent={isValidationCurrent}
                    />
                  )}

                  <Button
                    type="primary"
                    htmlType="submit"
                    disabled={createMutation.isPending}
                    icon={<Eye className="size-4" aria-hidden />}
                    className="!bg-emerald-600 hover:!bg-emerald-700 dark:hover:!bg-emerald-500"
                    block
                  >
                    Preview challenge
                  </Button>
                  <Button onClick={() => navigate("/challenges")} block>
                    Cancel
                  </Button>
                </div>
              </CtlCard>
            </aside>
          </div>
          </div>
        </Form>
        <ChallengePreviewModal
          open={previewValues !== null}
          values={previewValues}
          publishing={createMutation.isPending}
          onClose={() => setPreviewValues(null)}
          onPublish={handlePublishPreview}
        />
      </div>
    </AppLayout>
  );
}
