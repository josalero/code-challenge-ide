import { Bot, Code2, ShieldCheck } from "lucide-react";
import { Typography } from "antd";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import ThemeToggle from "../ThemeToggle";

type Props = {
  title: string;
  subtitle: string;
  children: ReactNode;
};

const FEATURES = [
  {
    icon: ShieldCheck,
    title: "Sandboxed runs",
    text: "Docker runners evaluate unit tests, coverage, and style gates.",
  },
  {
    icon: Code2,
    title: "Multi-language catalog",
    text: "Java, Python, Go, TypeScript, React, Vue, Angular, and more.",
  },
  {
    icon: Bot,
    title: "Guided feedback",
    text: "AI coaching explains failures without handing over full solutions.",
  },
];

export default function AuthShell({ title, subtitle, children }: Props) {
  return (
    <div className="ctl-auth-bg flex min-h-dvh flex-col">
      <header className="border-b border-border bg-background/90 backdrop-blur">
        <div className="mx-auto flex min-h-16 w-full max-w-7xl items-center justify-between gap-4 px-4 py-2 md:px-6">
          <Link
            to="/login"
            className="inline-flex min-w-0 items-center gap-2.5 text-foreground no-underline"
            aria-label="Code Training Lab"
          >
            <span className="flex size-9 items-center justify-center rounded-md bg-emerald-500/15 ring-1 ring-emerald-500/25">
              <Code2 className="size-5 text-emerald-600 dark:text-emerald-400" aria-hidden />
            </span>
            <span className="min-w-0">
              <Typography.Text className="!text-foreground block text-sm font-semibold leading-tight">
                Code Training Lab
              </Typography.Text>
              <Typography.Text className="!text-muted-foreground hidden text-xs sm:block">
                Practice · test · improve
              </Typography.Text>
            </span>
          </Link>
          <div className="flex items-center gap-2">
            <ThemeToggle />
            <Typography.Text className="!text-muted-foreground hidden text-xs sm:block">
              Private training workspace
            </Typography.Text>
          </div>
        </div>
      </header>

      <main className="mx-auto grid w-full max-w-7xl flex-1 gap-8 px-4 py-8 md:px-6 lg:grid-cols-[minmax(0,1fr)_minmax(360px,480px)] lg:items-center lg:py-12">
        <section className="hidden min-h-[520px] flex-col justify-between lg:flex">
          <div>
            <Typography.Title
              level={1}
              className="!mb-4 !mt-0 max-w-2xl !text-4xl !font-semibold !leading-tight !text-foreground xl:!text-5xl"
            >
              Build confidence by running code against the same gates every time.
            </Typography.Title>
            <Typography.Paragraph className="!mb-0 max-w-xl !text-base !leading-relaxed !text-muted-foreground">
              A focused lab for practicing challenges, publishing tests, and reading clear feedback from real execution.
            </Typography.Paragraph>
          </div>

          <div className="grid gap-3 xl:grid-cols-3">
            {FEATURES.map(({ icon: Icon, title: featureTitle, text }) => (
              <article
                key={featureTitle}
                className="rounded-lg border border-border bg-card p-4 shadow-sm dark:border-slate-800/80 dark:bg-slate-900/60 dark:shadow-none"
              >
                <span className="mb-3 flex size-9 items-center justify-center rounded-md bg-emerald-500/10 text-emerald-600 ring-1 ring-emerald-500/20 dark:text-emerald-400">
                  <Icon className="size-4" aria-hidden />
                </span>
                <h2 className="mb-1 text-sm font-semibold text-foreground">
                  {featureTitle}
                </h2>
                <p className="mb-0 text-sm leading-relaxed text-muted-foreground">{text}</p>
              </article>
            ))}
          </div>
        </section>

        <section className="flex min-h-[calc(100dvh-10rem)] items-center justify-center lg:min-h-0">
          <div className="w-full max-w-md">
            <div className="ctl-auth-panel rounded-lg border border-border bg-card p-6 shadow-lg sm:p-8 dark:border-slate-800/80 dark:bg-slate-900/95 dark:shadow-xl dark:shadow-black/20">
              <Typography.Title level={2} className="!mb-1 !mt-0 !text-foreground">
                {title}
              </Typography.Title>
              <Typography.Paragraph className="!mb-6 !text-muted-foreground">
                {subtitle}
              </Typography.Paragraph>
              {children}
            </div>
          </div>
        </section>
      </main>

      <footer className="border-t border-border bg-background/90">
        <div className="mx-auto flex w-full max-w-7xl flex-col gap-2 px-4 py-3 text-xs text-muted-foreground sm:flex-row sm:items-center sm:justify-between md:px-6">
          <span className="font-medium text-foreground">Code Training Lab</span>
          <span>Docker sandbox · hidden tests · feedback reports</span>
        </div>
      </footer>
    </div>
  );
}
