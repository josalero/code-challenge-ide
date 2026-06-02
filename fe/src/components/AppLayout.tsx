import { Avatar, Button, Layout, Typography } from "antd";
import { Code2, ListChecks, LogOut, Plus, ServerCog, ShieldCheck } from "lucide-react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/useAuth";
import { cn } from "../lib/utils";

const { Header, Content, Footer } = Layout;

export default function AppLayout({
  children,
  variant = "default",
}: {
  children: React.ReactNode;
  variant?: "default" | "workspace";
}) {
  const { user, logout, isAdmin } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const selectedKey = location.pathname.startsWith("/admin/ops")
    ? "ops"
    : location.pathname.startsWith("/challenges/new")
    ? "create"
    : location.pathname.startsWith("/challenges")
      ? "challenges"
      : "";
  const initials = user?.email?.slice(0, 2).toUpperCase() ?? "?";
  const isWorkspace = variant === "workspace";

  const navItems = [
    {
      key: "challenges",
      to: "/challenges",
      label: "Challenges",
      icon: ListChecks,
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
          "z-30 shrink-0 !h-auto border-b border-slate-800/80 bg-slate-950/90 !px-0 !leading-normal backdrop-blur",
          isWorkspace ? "sticky top-0" : "sticky top-0",
        )}
      >
        <div
          className={cn(
            "mx-auto flex min-h-16 w-full flex-wrap items-center gap-3 px-4 py-2 md:px-6",
            isWorkspace ? "max-w-none" : "max-w-7xl",
          )}
        >
          <Link
            to="/challenges"
            className="flex shrink-0 items-center gap-2.5 text-white no-underline"
            aria-label="Code Training Lab home"
          >
            <span className="flex size-9 items-center justify-center rounded-md bg-emerald-500/15 ring-1 ring-emerald-500/25">
              <Code2 className="size-5 text-emerald-400" aria-hidden />
            </span>
            <span className="min-w-0">
              <Typography.Text className="!text-white block text-sm font-semibold leading-tight">
                Code Training Lab
              </Typography.Text>
              <Typography.Text className="!text-slate-500 hidden text-xs sm:block">
                Practice · test · improve
              </Typography.Text>
            </span>
          </Link>

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
                      ? "bg-emerald-500/10 text-emerald-300 ring-1 ring-emerald-500/20"
                      : "text-slate-400 hover:bg-slate-800/70 hover:text-slate-100",
                  )}
                  aria-current={selectedKey === key ? "page" : undefined}
                >
                  <Icon className="size-4" aria-hidden />
                  {label}
                </Link>
              ))}
            </nav>

            <span
              className="mx-0.5 hidden h-6 w-px shrink-0 bg-slate-800 sm:mx-1 sm:block"
              aria-hidden
            />

            {user && (
              <div className="hidden min-w-0 items-center gap-2 md:flex">
                <Avatar
                  size="small"
                  className="!bg-emerald-600/30 !text-emerald-300 text-xs"
                  aria-hidden
                >
                  {initials}
                </Avatar>
                <Typography.Text className="!text-slate-400 max-w-[180px] truncate text-sm">
                  {user.email}
                </Typography.Text>
              </div>
            )}
            <Button
              type="text"
              icon={<LogOut className="size-4" aria-hidden />}
              className="!min-h-10 !text-slate-300 hover:!bg-slate-800/80 hover:!text-white"
              onClick={() => {
                logout();
                navigate("/login");
              }}
              aria-label="Sign out"
            >
              <span className="hidden sm:inline">Sign out</span>
            </Button>
          </div>
        </div>
      </Header>
      <Content
        id="main-content"
        className={
          isWorkspace
            ? "flex min-h-0 flex-1 flex-col overflow-hidden p-0"
            : "mx-auto flex w-full max-w-7xl flex-1 flex-col px-4 py-6 md:px-6 md:py-8 lg:py-10"
        }
      >
        {children}
      </Content>
      {!isWorkspace && (
      <Footer className="border-t border-slate-800/80 bg-slate-950/95 !px-0 !py-0">
        <div
          className={cn(
            "mx-auto flex w-full flex-col gap-2 px-4 py-3 text-xs text-slate-500 sm:flex-row sm:items-center sm:justify-between md:px-6",
            isWorkspace ? "max-w-none" : "max-w-7xl",
          )}
        >
          <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
            <span className="font-medium text-slate-400">Code Training Lab</span>
            <span className="hidden text-slate-700 sm:inline" aria-hidden>
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
