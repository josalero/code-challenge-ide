import { Avatar, Button, Layout, Typography } from "antd";
import {
  BarChart3,
  Code2,
  ListChecks,
  LogOut,
  Plus,
  ServerCog,
  ShieldCheck,
} from "lucide-react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/useAuth";
import ThemeToggle from "./ThemeToggle";
import { cn } from "../lib/utils";

const { Header, Content, Footer } = Layout;

type ContentLayout = "default" | "wide";

function shellLayoutClasses(contentLayout: ContentLayout, workspace: boolean) {
  if (workspace) {
    return { maxWidth: "max-w-none", paddingX: "px-4 md:px-6" };
  }
  if (contentLayout === "wide") {
    return {
      maxWidth: "max-w-[1720px]",
      paddingX: "px-3 md:px-4 lg:px-10 xl:px-14",
    };
  }
  return { maxWidth: "max-w-7xl", paddingX: "px-4 md:px-6" };
}

export default function AppLayout({
  children,
  variant = "default",
  focused = false,
  contentLayout = "default",
}: {
  children: React.ReactNode;
  variant?: "default" | "workspace";
  /** Hides primary nav and distracting chrome while a timed challenge session is active. */
  focused?: boolean;
  /** Wide catalog pages use tighter side padding and no max-width cap. */
  contentLayout?: ContentLayout;
}) {
  const { user, logout, isAdmin } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const selectedKey = location.pathname.startsWith("/admin/ops")
    ? "ops"
    : location.pathname.startsWith("/challenges/new")
      ? "create"
      : location.pathname.startsWith("/metrics")
        ? "metrics"
        : location.pathname.startsWith("/challenges")
          ? "challenges"
          : "";
  const initials = user?.email?.slice(0, 2).toUpperCase() ?? "?";
  const isWorkspace = variant === "workspace";
  const hideChrome = isWorkspace && focused;
  const shell = shellLayoutClasses(contentLayout, isWorkspace);

  const navItems = [
    {
      key: "challenges",
      to: "/challenges",
      label: "Challenges",
      icon: ListChecks,
    },
    {
      key: "metrics",
      to: "/metrics",
      label: "Metrics",
      icon: BarChart3,
    },
    ...(isAdmin
      ? [
          {
            key: "create",
            to: "/challenges/new",
            label: "Create",
            icon: Plus,
          },
          {
            key: "ops",
            to: "/admin/ops",
            label: "Ops",
            icon: ServerCog,
          },
        ]
      : []),
  ];

  return (
    <Layout
      className={cn(
        "ctl-app-bg flex flex-col overflow-hidden",
        isWorkspace ? "ctl-workspace-layout h-dvh max-h-dvh" : "min-h-dvh",
      )}
    >
      <a href="#main-content" className="ctl-skip-link">
        Skip to main content
      </a>
      <Header
        className={cn(
          "z-30 shrink-0 !h-auto border-b border-border bg-background !px-0 !leading-normal shadow-sm dark:bg-background/90 dark:shadow-none",
          isWorkspace ? "sticky top-0" : "sticky top-0",
        )}
      >
        <div
          className={cn(
            "mx-auto flex min-h-16 w-full flex-wrap items-center gap-3 py-2",
            shell.maxWidth,
            shell.paddingX,
          )}
        >
          {hideChrome ? (
            <div
              className="flex shrink-0 items-center gap-2.5 text-foreground"
              aria-label="Code Training Lab"
            >
              <span className="flex size-9 items-center justify-center rounded-md bg-emerald-50 ring-1 ring-emerald-200 dark:bg-emerald-500/15 dark:ring-emerald-500/25">
                <Code2 className="size-5 text-emerald-700 dark:text-emerald-400" aria-hidden />
              </span>
              <Typography.Text className="!text-foreground text-sm font-semibold leading-tight">
                Code Training Lab
              </Typography.Text>
            </div>
          ) : (
            <Link
              to="/challenges"
              className="flex shrink-0 items-center gap-2.5 text-foreground no-underline"
              aria-label="Code Training Lab home"
            >
              <span className="flex size-9 items-center justify-center rounded-md bg-emerald-50 ring-1 ring-emerald-200 dark:bg-emerald-500/15 dark:ring-emerald-500/25">
                <Code2 className="size-5 text-emerald-700 dark:text-emerald-400" aria-hidden />
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
          )}

          {!hideChrome && (
          <div className="ml-auto flex min-w-0 items-center gap-1 sm:gap-2">
            <nav
              className="flex min-w-0 shrink gap-0.5 overflow-x-auto sm:gap-1"
              aria-label="Primary navigation"
            >
              {navItems.map(({ key, to, label, icon: Icon }) => (
                <Link
                  key={key}
                  to={to}
                  className={cn(
                    "inline-flex min-h-10 shrink-0 items-center gap-1.5 rounded-md px-2.5 text-sm font-medium no-underline transition-colors sm:gap-2 sm:px-3",
                    selectedKey === key
                      ? "bg-emerald-500/10 text-emerald-600 ring-1 ring-emerald-500/25 dark:text-emerald-300 dark:ring-emerald-500/20"
                      : "text-muted-foreground hover:bg-muted hover:text-foreground",
                  )}
                  aria-current={selectedKey === key ? "page" : undefined}
                >
                  <Icon className="size-4" aria-hidden />
                  {label}
                </Link>
              ))}
            </nav>

            <span
              className="mx-0.5 hidden h-6 w-px shrink-0 bg-border sm:mx-1 sm:block"
              aria-hidden
            />

            <ThemeToggle />

            {user && (
              <div
                className="hidden min-w-0 items-center gap-2.5 rounded-lg border border-border bg-muted/60 px-2.5 py-1 md:flex"
                aria-label="Signed in as"
              >
                <Avatar
                  size="small"
                  className={cn(
                    "!flex !shrink-0 !items-center !justify-center",
                    "!bg-emerald-600 !text-[11px] !font-semibold !leading-none !text-white",
                    "dark:!bg-emerald-500/30 dark:!text-emerald-100",
                  )}
                >
                  {initials}
                </Avatar>
                <Typography.Text className="!text-slate-800 dark:!text-slate-300 max-w-[200px] truncate text-sm font-medium">
                  {user.email}
                </Typography.Text>
              </div>
            )}
            <Button
              type="text"
              icon={<LogOut className="size-4" aria-hidden />}
              className="!min-h-10 !text-muted-foreground hover:!bg-muted hover:!text-foreground"
              onClick={() => {
                logout();
                navigate("/login");
              }}
              aria-label="Sign out"
            >
              <span className="hidden sm:inline">Sign out</span>
            </Button>
          </div>
          )}
        </div>
      </Header>
      <Content
        id="main-content"
        className={
          isWorkspace
            ? "flex min-h-0 flex-1 flex-col overflow-hidden p-0"
            : cn(
                "mx-auto flex w-full flex-1 flex-col py-6 md:py-8 lg:py-10",
                shell.maxWidth,
                shell.paddingX,
              )
        }
      >
        {children}
      </Content>
      {!isWorkspace && (
      <Footer className="border-t border-border bg-background/95 !px-0 !py-0">
        <div
          className={cn(
            "mx-auto flex w-full flex-col gap-2 py-3 text-xs text-muted-foreground sm:flex-row sm:items-center sm:justify-between",
            shell.maxWidth,
            shell.paddingX,
          )}
        >
          <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
            <span className="font-medium text-foreground">Code Training Lab</span>
            <span className="hidden text-border sm:inline" aria-hidden>
              ·
            </span>
            <span>Docker sandbox · hidden tests · AI coach</span>
          </div>
          <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
            <span className="inline-flex items-center gap-1.5">
              <ShieldCheck className="size-3.5 text-emerald-500/80" aria-hidden />
              Private lab instance
            </span>
            {user?.role && <span>{user.role.toLowerCase()}</span>}
          </div>
        </div>
      </Footer>
      )}
    </Layout>
  );
}
