import { Fragment, type ReactNode } from "react";

type Segment =
  | { type: "text"; content: string }
  | { type: "code"; language: string; content: string };

const FENCE_PATTERN = /```(\w*)\r?\n?([\s\S]*?)```/g;

export function parseCoachMarkdown(text: string): Segment[] {
  const segments: Segment[] = [];
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

function renderInlineCode(text: string): ReactNode[] {
  const parts = text.split(/(`[^`\n]+`)/g);
  return parts.map((part, index) => {
    if (part.startsWith("`") && part.endsWith("`") && part.length > 2) {
      return <code key={index}>{part.slice(1, -1)}</code>;
    }
    return <Fragment key={index}>{part}</Fragment>;
  });
}

function renderTextBlock(content: string, key: number) {
  const blocks = content.split(/\n{2,}/);
  return blocks.map((block, blockIndex) => (
    <p key={`${key}-${blockIndex}`} className="whitespace-pre-wrap">
      {renderInlineCode(block.trim())}
    </p>
  ));
}

type Props = {
  text: string;
  className?: string;
};

/** Renders AI coach text with fenced code blocks and inline `code`. */
export default function CoachMarkdown({ text, className = "" }: Props) {
  const segments = parseCoachMarkdown(text);
  return (
    <div className={`ctl-workspace-prose ${className}`.trim()}>
      {segments.map((segment, index) =>
        segment.type === "code" ? (
          <figure key={index} className="m-0">
            {segment.language && segment.language !== "text" && (
              <figcaption className="mb-1 text-[10px] font-semibold uppercase tracking-wide text-slate-500">
                {segment.language}
              </figcaption>
            )}
            <pre>
              <code>{segment.content}</code>
            </pre>
          </figure>
        ) : (
          <Fragment key={index}>{renderTextBlock(segment.content, index)}</Fragment>
        ),
      )}
    </div>
  );
}
