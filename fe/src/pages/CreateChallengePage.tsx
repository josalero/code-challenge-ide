import { MinusCircleOutlined, PlusOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, Button, Form, Input, InputNumber, Select, Typography } from "antd";
import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, ApiError } from "../api/client";
import type { ChallengeSummary, CreateChallengeRequest, LanguageRuntimeOption } from "../api/types";
import AppLayout from "../components/AppLayout";
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

const JAVA_PUBLIC_TEST = `package com.challenge.public_;

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
  const language = Form.useWatch("language", form) ?? "";
  const defaultRuntimeVersion = Form.useWatch("defaultRuntimeVersion", form) ?? "";
  const lineCoveragePercent = Form.useWatch("lineCoveragePercent", form) ?? 80;

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
      descriptionMd: "",
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
      void queryClient.invalidateQueries({ queryKey: ["challenges"] });
      navigate(`/challenges/${created.slug}`, { replace: true });
    },
    onError: (e) => {
      setError(e instanceof ApiError ? e.message : "Could not create challenge");
    },
  });

  const onFinish = (values: FormValues) => {
    setError(null);
    createMutation.mutate({
      slug: values.slug.trim().toLowerCase(),
      title: values.title.trim(),
      descriptionMd: values.descriptionMd.trim(),
      difficulty: values.difficulty,
      language: values.language,
      defaultRuntimeVersion: values.defaultRuntimeVersion,
      starterCode: values.starterCode,
      lineCoveragePercent: values.lineCoveragePercent,
      sessionDurationMinutes: values.sessionDurationMinutes,
      publicTests: values.publicTests.map((t) => ({
        name: t.name.trim(),
        source: t.source,
      })),
      hiddenTests: values.hiddenTests.map((t) => ({
        name: t.name.trim(),
        source: t.source,
      })),
    });
  };

  return (
    <AppLayout>
      <PageHeader
        title="Create challenge"
        description="Compose the prompt, starter code, and test suites in one publishing flow."
        extra={
          <Link
            to="/challenges"
            className="inline-flex min-h-10 items-center text-sm font-medium text-emerald-400 no-underline hover:text-emerald-300"
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
            className="border-slate-700/80"
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
          <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px]">
            <div className="flex min-w-0 flex-col gap-6">
              <CtlCard title="Challenge details">
                <div className="grid gap-4 md:grid-cols-2">
                  <Form.Item
                    label={<span className="text-slate-300">Slug</span>}
                    name="slug"
                    rules={[
                      { required: true, message: "Slug is required" },
                      {
                        pattern: /^[a-z0-9]+(?:-[a-z0-9]+)*$/,
                        message: "Use lowercase letters, numbers, and hyphens",
                      },
                    ]}
                    extra={
                      <Typography.Text className="!text-slate-500 text-xs">
                        URL-safe id, e.g. two-sum
                      </Typography.Text>
                    }
                  >
                    <Input placeholder="my-challenge" />
                  </Form.Item>
                  <Form.Item
                    label={<span className="text-slate-300">Title</span>}
                    name="title"
                    rules={[{ required: true, message: "Title is required" }]}
                  >
                    <Input placeholder="Two Sum" />
                  </Form.Item>
                </div>

                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                  <Form.Item
                    label={<span className="text-slate-300">Difficulty</span>}
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
                    label={<span className="text-slate-300">Language</span>}
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
                    label={<span className="text-slate-300">Default runtime</span>}
                    name="defaultRuntimeVersion"
                    rules={[{ required: true, message: "Select a runtime version" }]}
                    extra={
                      <Typography.Text className="!text-slate-500 text-xs">
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
                    label={<span className="text-slate-300">Line coverage %</span>}
                    name="lineCoveragePercent"
                    rules={[{ required: true }]}
                  >
                    <InputNumber min={0} max={100} className="w-full" />
                  </Form.Item>
                  <Form.Item
                    label={<span className="text-slate-300">Session time (min)</span>}
                    name="sessionDurationMinutes"
                    rules={[{ required: true, message: "Session duration is required" }]}
                    extra={
                      <Typography.Text className="!text-slate-500 text-xs">
                        Allotted workspace time after the learner starts Run or Submit (5–480 min).
                      </Typography.Text>
                    }
                  >
                    <InputNumber min={5} max={480} className="w-full" />
                  </Form.Item>
                </div>

                <Form.Item
                  label={<span className="text-slate-300">Description (Markdown)</span>}
                  name="descriptionMd"
                  rules={[{ required: true, message: "Description is required" }]}
                  className="!mb-0"
                >
                  <Input.TextArea
                    rows={7}
                    placeholder="Explain the task, examples, constraints…"
                  />
                </Form.Item>
              </CtlCard>

              <CtlCard title="Starter code">
                <Form.Item
                  label={<span className="text-slate-300">Solution template</span>}
                  name="starterCode"
                  rules={[{ required: true, message: "Starter code is required" }]}
                  className="!mb-0"
                >
                  <Input.TextArea
                    rows={12}
                    className="font-mono text-sm"
                    placeholder="Starter solution template"
                  />
                </Form.Item>
              </CtlCard>

              <CtlCard
                title="Public tests"
                extra={
                  <Typography.Text className="!text-slate-500 text-xs">
                    Visible in workspace
                  </Typography.Text>
                }
              >
                <Typography.Paragraph className="!mb-4 !text-slate-500 text-sm">
                  Java public tests use package{" "}
                  <code className="text-emerald-400">com.challenge.public_</code>.
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
                          className="rounded-lg border border-slate-700/80 bg-slate-950/30 p-4"
                        >
                          <div className="mb-3 flex items-center justify-between gap-2">
                            <Form.Item
                              {...rest}
                              name={[name, "name"]}
                              label={<span className="text-slate-300">Test name</span>}
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
                            label={<span className="text-slate-300">Source</span>}
                            rules={[{ required: true, message: "Source required" }]}
                            className="!mb-0"
                          >
                            <Input.TextArea rows={8} className="font-mono text-sm" />
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
                  <Typography.Text className="!text-slate-500 text-xs">
                    Submit only
                  </Typography.Text>
                }
              >
                <Typography.Paragraph className="!mb-4 !text-slate-500 text-sm">
                  Java hidden tests use package{" "}
                  <code className="text-emerald-400">com.challenge.hidden</code>.
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
                          className="rounded-lg border border-slate-700/80 bg-slate-950/30 p-4"
                        >
                          <div className="mb-3 flex items-center justify-between gap-2">
                            <Form.Item
                              {...rest}
                              name={[name, "name"]}
                              label={<span className="text-slate-300">Test name</span>}
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
                            label={<span className="text-slate-300">Source</span>}
                            rules={[{ required: true, message: "Source required" }]}
                            className="!mb-0"
                          >
                            <Input.TextArea rows={8} className="font-mono text-sm" />
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

                  <Button
                    type="primary"
                    htmlType="submit"
                    loading={createMutation.isPending}
                    className="!bg-emerald-600 hover:!bg-emerald-500"
                    block
                  >
                    Publish challenge
                  </Button>
                  <Button onClick={() => navigate("/challenges")} block>
                    Cancel
                  </Button>
                </div>
              </CtlCard>
            </aside>
          </div>
        </Form>
      </div>
    </AppLayout>
  );
}
