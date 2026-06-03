import { Fragment, type ReactNode } from "react";
import { parseCoachMarkdown } from "./parseCoachMarkdown";

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
