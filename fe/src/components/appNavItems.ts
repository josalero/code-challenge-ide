import {
  BarChart3,
  Inbox,
  LayoutDashboard,
  ListChecks,
  Plus,
  ServerCog,
  Users,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";

export type AdminNavKey = "dashboard" | "create" | "users" | "requests" | "ops";

export type NavItem = {
  key: string;
  to: string;
  label: string;
  icon: LucideIcon;
};

export const PRIMARY_NAV_ITEMS: NavItem[] = [
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
];

export const ADMIN_NAV_ITEMS: NavItem[] = [
  { key: "dashboard", to: "/admin/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { key: "create", to: "/challenges/new", label: "Create challenge", icon: Plus },
  { key: "users", to: "/admin/users", label: "Users & activity", icon: Users },
  { key: "requests", to: "/admin/access-requests", label: "Access requests", icon: Inbox },
  { key: "ops", to: "/admin/ops", label: "Ops & warm-up", icon: ServerCog },
];

const ADMIN_KEYS = new Set<string>(ADMIN_NAV_ITEMS.map((item) => item.key));

export function isAdminNavKey(key: string): key is AdminNavKey {
  return ADMIN_KEYS.has(key);
}

export function resolveSelectedNavKey(pathname: string): string {
  if (pathname.startsWith("/admin/ops")) return "ops";
  if (pathname.startsWith("/admin/access-requests")) return "requests";
  if (pathname.startsWith("/admin/users")) return "users";
  if (pathname.startsWith("/admin/dashboard")) return "dashboard";
  if (pathname.startsWith("/challenges/new")) return "create";
  if (pathname.startsWith("/metrics")) return "metrics";
  if (pathname.startsWith("/challenges")) return "challenges";
  return "";
}
