import type { Components } from "react-markdown";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { cn } from "@/lib/utils";

type Props = {
  markdown: string;
  className?: string;
};

const markdownComponents: Components = {
  h2: ({ children }) => (
    <h2 className="mb-2 mt-5 border-b border-slate-700/60 pb-1.5 text-xs font-semibold uppercase tracking-wider text-slate-200 first:mt-0">
      {children}
    </h2>
  ),
  h3: ({ children }) => (
    <h3 className="mb-1.5 mt-4 text-sm font-semibold text-slate-100 first:mt-0">{children}</h3>
  ),
  p: ({ children }) => <p>{children}</p>,
  ul: ({ children }) => (
    <ul className="my-2 list-disc space-y-1.5 pl-5 marker:text-slate-500">{children}</ul>
  ),
  ol: ({ children }) => (
    <ol className="my-2 list-decimal space-y-1.5 pl-5 marker:text-slate-500">{children}</ol>
  ),
  li: ({ children }) => <li className="text-slate-300">{children}</li>,
  strong: ({ children }) => (
    <strong className="font-semibold text-slate-100">{children}</strong>
  ),
  em: ({ children }) => <em className="text-slate-200">{children}</em>,
  blockquote: ({ children }) => (
    <blockquote className="my-3 border-l-2 border-emerald-500/40 bg-slate-800/30 py-1 pl-3 text-slate-400">
      {children}
    </blockquote>
  ),
  hr: () => <hr className="my-4 border-slate-700/60" />,
  a: ({ href, children }) => (
    <a
      href={href}
      className="text-emerald-400 underline decoration-emerald-500/30 underline-offset-2 hover:text-emerald-300"
      target="_blank"
      rel="noopener noreferrer"
    >
      {children}
    </a>
  ),
  code: ({ className, children }) => {
    const isBlock = Boolean(className?.includes("language-"));
    if (isBlock) {
      return <code className={className}>{children}</code>;
    }
    return <code>{children}</code>;
  },
  pre: ({ children }) => <pre>{children}</pre>,
};

export default function ChallengeDescriptionMarkdown({ markdown, className }: Props) {
  if (!markdown.trim()) {
    return null;
  }

  return (
    <div className={cn("ctl-workspace-prose", className)}>
      <ReactMarkdown remarkPlugins={[remarkGfm]} components={markdownComponents}>
        {markdown}
      </ReactMarkdown>
    </div>
  );
}
