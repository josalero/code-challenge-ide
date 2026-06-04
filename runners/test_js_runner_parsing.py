from __future__ import annotations

import importlib.util
import json
import tempfile
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parent
JS_RUNNERS = ("node", "typescript", "react", "vue", "angular")


def load_runner(name: str):
    path = ROOT / name / "run.py"
    spec = importlib.util.spec_from_file_location(f"{name}_runner", path)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"Could not load {path}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class JsRunnerParsingTest(unittest.TestCase):
    def test_unknown_coverage_percent_is_treated_as_zero(self) -> None:
        for name in JS_RUNNERS:
            with self.subTest(runner=name), tempfile.TemporaryDirectory() as temp_dir:
                module = load_runner(name)
                module.WORKSPACE = Path(temp_dir)
                summary = module.WORKSPACE / "coverage" / "coverage-summary.json"
                summary.parent.mkdir(parents=True)
                summary.write_text(
                    json.dumps({"total": {"lines": {"pct": "Unknown"}}}),
                    encoding="utf-8",
                )

                parse_coverage = (
                    module.parse_c8_coverage
                    if hasattr(module, "parse_c8_coverage")
                    else module.parse_coverage
                )

                self.assertEqual(
                    parse_coverage(),
                    {"line_percent": 0.0, "branch_percent": 0.0},
                )

    def test_unknown_junit_duration_is_treated_as_zero(self) -> None:
        for name in JS_RUNNERS:
            with self.subTest(runner=name), tempfile.TemporaryDirectory() as temp_dir:
                module = load_runner(name)
                module.WORKSPACE = Path(temp_dir)
                (module.WORKSPACE / "junit.xml").write_text(
                    '<testsuites><testcase name="smoke" time="Unknown"/></testsuites>',
                    encoding="utf-8",
                )

                tests = module.parse_junit()

                self.assertEqual(tests[0]["duration_ms"], 0)
                self.assertEqual(tests[0]["status"], "PASS")


if __name__ == "__main__":
    unittest.main()
