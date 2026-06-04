import { Avatar, Button, Layout } from "antd";
import { LogOut, ShieldCheck } from "lucide-react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { useAuth } from "../auth/useAuth";
import AdminNavMenu from "./AdminNavMenu";
import AppFooterNav from "./AppFooterNav";
import AppLogo from "./AppLogo";
import PoweredByFooter from "./PoweredByFooter";
import ThemeToggle from "./ThemeToggle";
import { cn } from "../lib/utils";
import { PRIMARY_NAV_ITEMS, resolveSelectedNavKey } from "./appNavItems";

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

function accountInitials(email: string, fullName: string | null | undefined): string {
  const name = fullName?.trim();
  if (name) {
    const parts = name.split(/\s+/).filter(Boolean);
    if (parts.length >= 2) {
      return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
  }
  return email.slice(0, 2).toUpperCase();
}

export default function AppLayout({
  children,
  variant = "default",
  focused = false,
  contentLayout = "wide",
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

  const selectedKey = resolveSelectedNavKey(location.pathname);
  const initials = user ? accountInitials(user.email, user.fullName) : "?";
  const accountName = user?.fullName?.trim() || null;
  const isWorkspace = variant === "workspace";
  const hideChrome = isWorkspace && focused;
  const shell = shellLayoutClasses(contentLayout, isWorkspace);

  const primaryNavItems = PRIMARY_NAV_ITEMS;

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
          "z-30 shrink-0 !h-auto border-b border-border bg-card !px-0 !leading-normal shadow-sm dark:bg-background/90 dark:shadow-none",
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
            <AppLogo showTagline={false} className="shrink-0 text-foreground" />
          ) : (
            <Link
              to={isAdmin ? "/admin/dashboard" : "/challenges"}
              className="shrink-0 text-foreground no-underline"
              aria-label="Code Training Lab home"
            >
              <AppLogo />
            </Link>
          )}

          {!hideChrome && (
          <div className="ml-auto flex min-w-0 items-center gap-1 sm:gap-2">
            <nav
              className="flex min-w-0 shrink items-center gap-0.5 overflow-x-auto sm:gap-1"
              aria-label="Primary navigation"
            >
              {primaryNavItems.map(({ key, to, label, icon: Icon }) => (
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

              {isAdmin && (
                <>
                  <span
                    className="mx-0.5 hidden h-6 w-px shrink-0 bg-border sm:mx-1 md:inline-block"
                    aria-hidden
                  />
                  <AdminNavMenu selectedKey={selectedKey} />
                </>
              )}
            </nav>

            <span
              className="mx-0.5 hidden h-6 w-px shrink-0 bg-border sm:mx-1 sm:block"
              aria-hidden
            />

            <ThemeToggle />

            {user && (
              <Tooltip>
                <TooltipTrigger
                  render={
                    <div
                      className="hidden min-w-0 cursor-default items-center gap-2.5 rounded-lg border border-border bg-muted/60 px-2.5 py-1 md:flex"
                      aria-label={
                        accountName
                          ? `Signed in as ${accountName}, ${user.email}`
                          : `Signed in as ${user.email}`
                      }
                    />
                  }
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
                  {accountName && (
                    <span className="max-w-[160px] truncate text-sm font-medium text-foreground">
                      {accountName}
                    </span>
                  )}
                </TooltipTrigger>
                <TooltipContent side="bottom" align="end">
                  <span className="block">{user.email}</span>
                </TooltipContent>
              </Tooltip>
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
      <Footer className="border-t border-border bg-background/95 !px-0 !py-0 dark:bg-background/95">
        <div
          className={cn(
            "mx-auto flex w-full flex-col gap-3 py-3 text-xs text-muted-foreground",
            shell.maxWidth,
            shell.paddingX,
          )}
        >
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
              <span className="font-medium text-foreground">Code Training Lab</span>
              <span className="hidden text-border sm:inline" aria-hidden>
                ·
              </span>
              <span>Docker sandbox · hidden tests · AI coach</span>
            </div>
            <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
              <PoweredByFooter />
              <span className="hidden text-border sm:inline" aria-hidden>
                ·
              </span>
              <span className="inline-flex items-center gap-1.5">
                <ShieldCheck className="size-3.5 text-emerald-500/80" aria-hidden />
                Private lab instance
              </span>
              {user?.role && <span>{user.role.toLowerCase()}</span>}
            </div>
          </div>

          {!hideChrome && (
            <AppFooterNav selectedKey={selectedKey} isAdmin={isAdmin} />
          )}
        </div>
      </Footer>
      )}
    </Layout>
  );
}
