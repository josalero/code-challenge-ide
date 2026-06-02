import type { PublicTestInfo } from "../api/types";

export type ChallengeExample = {
  input: string;
  output: string;
  label?: string;
};

/** Curated when tests or meta cannot express the return type clearly. */
const SLUG_EXAMPLES: Record<string, ChallengeExample[]> = {
  "anagram-groups": [
    {
      input: '["eat", "tea", "tan", "ate", "nat", "bat"]',
      output: '[["bat"], ["nat", "tan"], ["eat", "tea", "ate"]]',
    },
    { input: "[]", output: "[]" },
    { input: '["a"]', output: '[["a"]]' },
  ],
};

const VAGUE_DESC = /^(verify behavior|verify expected|checks that\b)/i;

function javaIntArrayToJson(bracedContent: string): string {
  const inner = bracedContent.replace(/^\{|\}$/g, "").trim();
  if (!inner) {
    return "[]";
  }
  const nums = inner.split(",").map((part) => part.trim()).filter(Boolean);
  return JSON.stringify(nums.map((n) => Number(n)));
}

function javaStringArrayToJson(bracedContent: string): string {
  const inner = bracedContent.replace(/^\{|\}$/g, "").trim();
  if (!inner) {
    return "[]";
  }
  const words = inner
    .split(",")
    .map((part) => part.trim().replace(/^"(.*)"$/, "$1"))
    .filter((part) => part.length > 0);
  return JSON.stringify(words);
}

function splitTopLevelArgs(text: string): string[] {
  const parts: string[] = [];
  let depth = 0;
  let current = "";
  for (const char of text) {
    if ("([{".includes(char)) {
      depth += 1;
    } else if (")]}".includes(char)) {
      depth -= 1;
    }
    if (char === "," && depth === 0) {
      parts.push(current);
      current = "";
      continue;
    }
    current += char;
  }
  if (current) {
    parts.push(current);
  }
  return parts;
}

function normalizeCallArgs(raw: string): string {
  const text = raw.trim();
  if (!text) {
    return "—";
  }
  const parts: string[] = [];
  for (const segment of splitTopLevelArgs(text)) {
    const trimmed = segment.trim();
    const intArr = trimmed.match(/^new int\[\]\s*(\{[^}]*\})$/);
    if (intArr) {
      parts.push(javaIntArrayToJson(intArr[1]));
      continue;
    }
    const strArr = trimmed.match(/^new String\[\]\s*(\{[^}]*\})?$/);
    if (strArr) {
      parts.push(javaStringArrayToJson(strArr[1] ?? "{}"));
      continue;
    }
    parts.push(trimmed);
  }
  return parts.join(", ");
}

function normalizeOutput(raw: string): string {
  const trimmed = raw.trim();
  if (trimmed === "List.of()" || trimmed === "[]") {
    return "[]";
  }
  const listOf = trimmed.match(/^List\.of\((.*)\)$/s);
  if (listOf) {
    const inner = listOf[1].trim();
    if (!inner) {
      return "[]";
    }
    if (inner.includes("List.of")) {
      const groups: string[][] = [];
      const re = /List\.of\(([^)]*)\)/g;
      let match: RegExpExecArray | null;
      while ((match = re.exec(inner)) !== null) {
        const words = match[1]
          .split(",")
          .map((part) => part.trim().replace(/^"(.*)"$/, "$1"))
          .filter(Boolean);
        groups.push(words);
      }
      return JSON.stringify(groups);
    }
    const nums = inner.split(",").map((part) => part.trim()).filter(Boolean);
    if (nums.every((n) => /^-?\d+$/.test(n))) {
      return JSON.stringify(nums.map((n) => Number(n)));
    }
  }
  if (/^\[[^\]]*\]$/.test(trimmed)) {
    return trimmed.replace(/\s+/g, "");
  }
  return trimmed.replace(/\s+/g, " ").trim();
}

function isClearRow(input: string, output: string): boolean {
  if (!input || !output || input === "—" || output === "—") {
    return false;
  }
  if (VAGUE_DESC.test(input) || VAGUE_DESC.test(output)) {
    return false;
  }
  if (input.length > 120 || output.length > 160) {
    return false;
  }
  return true;
}

/** Parse learner-facing test descriptions into input/output rows when possible. */
export function parseExampleFromDescription(description: string): ChallengeExample | null {
  const text = description.trim();
  if (!text || VAGUE_DESC.test(text)) {
    return null;
  }

  const gcd = text.match(/^GCD\((\d+),\s*(\d+)\)\s+should equal\s+(\d+)$/i);
  if (gcd) {
    return { input: `${gcd[1]}, ${gcd[2]}`, output: gcd[3] };
  }

  const patterns: Array<{
    re: RegExp;
    map: (m: RegExpMatchArray) => ChallengeExample;
  }> = [
    {
      re: /^Expect\s+[\w.]+\(new String\[\]\s*(\{[^}]*\})?\)\s+to equal\s+(.+)$/i,
      map: (m) => ({
        input: javaStringArrayToJson(m[1] ? `{${m[1].replace(/^\{|\}$/g, "")}}` : "{}"),
        output: normalizeOutput(m[2]),
      }),
    },
    {
      re: /^Expect\s+[\w.]+\((.*)\)\s+to equal\s+(.+)$/i,
      map: (m) => ({
        input: normalizeCallArgs(m[1]),
        output: normalizeOutput(m[2]),
      }),
    },
    {
      re: /^Expect\s+[\w.]+\((.*)\)\s+to return\s+(.+)$/i,
      map: (m) => ({
        input: normalizeCallArgs(m[1]),
        output: normalizeOutput(m[2]),
      }),
    },
    {
      re: /^Expect\s+[\w.]+\((.*)\)\s+to be\s+(true|false)$/i,
      map: (m) => ({
        input: normalizeCallArgs(m[1]),
        output: m[2].toLowerCase(),
      }),
    },
    {
      re: /^[\w.]+\((.*)\)\s+should equal\s+(.+)$/i,
      map: (m) => ({
        input: normalizeCallArgs(m[1]),
        output: normalizeOutput(m[2]),
      }),
    },
    {
      re: /^[\w.]+\((.*)\)\s+should return\s+(.+)$/i,
      map: (m) => ({
        input: normalizeCallArgs(m[1]),
        output: normalizeOutput(m[2]),
      }),
    },
    {
      re: /^[\w.]+\((.*)\)\s+should return index\s+(-?\d+)$/i,
      map: (m) => ({
        input: normalizeCallArgs(m[1]),
        output: m[2],
      }),
    },
    {
      re: /^[\w.]+\((.*)\)\s+should be\s+(true|false)$/i,
      map: (m) => ({
        input: normalizeCallArgs(m[1]),
        output: m[2].toLowerCase(),
      }),
    },
    {
      re: /^reverse(?:String)?\("([^"]*)"\)\s+should be\s+"([^"]*)"$/i,
      map: (m) => ({
        input: `"${m[1]}"`,
        output: `"${m[2]}"`,
      }),
    },
  ];

  for (const { re, map } of patterns) {
    const match = text.match(re);
    if (match) {
      const row = map(match);
      return isClearRow(row.input, row.output) ? row : null;
    }
  }

  return null;
}

/** Parse `## Examples` or `**Examples**` bullets from challenge description markdown. */
export function examplesFromDescription(descriptionMd: string): ChallengeExample[] {
  const rows: ChallengeExample[] = [];
  const seen = new Set<string>();
  for (const line of descriptionMd.split("\n")) {
    const bullet = line.match(/^\s*[-*]\s+`([^`]+)`\s*→\s*`([^`]+)`\s*$/);
    if (!bullet) {
      continue;
    }
    const input = bullet[1].trim();
    const output = bullet[2].trim();
    if (!isClearRow(input, output)) {
      continue;
    }
    const key = `${input}→${output}`;
    if (seen.has(key)) {
      continue;
    }
    seen.add(key);
    rows.push({ input, output });
  }
  return rows;
}

export function challengeHasExamplesSection(descriptionMd: string): boolean {
  return /^#{1,3}\s+examples?\b/im.test(descriptionMd)
    || /\*\*examples?\*\*/i.test(descriptionMd);
}

/** Remove Examples section from description when shown in the table above. */
export function stripExamplesFromDescription(descriptionMd: string): string {
  if (!challengeHasExamplesSection(descriptionMd)) {
    return descriptionMd;
  }
  // Do not use /m with $ in the lookahead — in JS, $ matches end-of-line and would stop right after the heading.
  const nextSection = String.raw`(?=\n#{1,3}\s+(?!examples?\b)|\n\*\*[A-Za-z])`;
  const withoutHeading = descriptionMd.replace(
    new RegExp(String.raw`\n#{1,3}\s+examples?\b[\s\S]*?${nextSection}`, "i"),
    "",
  );
  return withoutHeading
    .replace(
      new RegExp(String.raw`\n\*\*Examples?\*\*[\s\S]*?${nextSection}`, "i"),
      "",
    )
    .trim();
}

function examplesFromPublicTests(publicTests: PublicTestInfo[]): ChallengeExample[] {
  const fromTests: ChallengeExample[] = [];
  const seen = new Set<string>();
  for (const test of publicTests) {
    const parsed = parseExampleFromDescription(test.description ?? test.name);
    if (!parsed) {
      continue;
    }
    const key = `${parsed.input}→${parsed.output}`;
    if (seen.has(key)) {
      continue;
    }
    seen.add(key);
    fromTests.push(parsed);
  }
  return fromTests;
}

export function resolveChallengeExamples(
  descriptionMd: string,
  publicTests: PublicTestInfo[],
  slug?: string,
): ChallengeExample[] {
  const curated = slug ? SLUG_EXAMPLES[slug] : undefined;

  const fromDescription = examplesFromDescription(descriptionMd);
  if (fromDescription.length > 0) {
    return fromDescription;
  }

  if (curated?.length) {
    return curated;
  }

  const fromTests = examplesFromPublicTests(publicTests);
  if (fromTests.length > 0) {
    return fromTests;
  }

  return [];
}
