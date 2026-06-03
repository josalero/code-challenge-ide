export type CoachMarkdownSegment =
  | { type: "text"; content: string }
  | { type: "code"; language: string; content: string };

const FENCE_PATTERN = /```(\w*)\r?\n?([\s\S]*?)```/g;

export function parseCoachMarkdown(text: string): CoachMarkdownSegment[] {
  const segments: CoachMarkdownSegment[] = [];
  let lastIndex = 0;
  let match: RegExpExecArray | null;
  FENCE_PATTERN.lastIndex = 0;
  while ((match = FENCE_PATTERN.exec(text)) !== null) {
    if (match.index > lastIndex) {
      segments.push({ type: "text", content: text.slice(lastIndex, match.index) });
    }
    segments.push({
      type: "code",
      language: match[1] || "text",
      content: match[2].replace(/\n$/, ""),
    });
    lastIndex = match.index + match[0].length;
  }
  if (lastIndex < text.length) {
    segments.push({ type: "text", content: text.slice(lastIndex) });
  }
  if (segments.length === 0) {
    segments.push({ type: "text", content: text });
  }
  return segments;
}
