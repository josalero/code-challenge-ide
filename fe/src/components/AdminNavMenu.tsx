import { Dropdown } from "antd";
import type { MenuProps } from "antd";
import { ChevronDown, ShieldCheck } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { cn } from "../lib/utils";
import { ADMIN_NAV_ITEMS, isAdminNavKey } from "./appNavItems";

type Props = {
  selectedKey: string;
};

export default function AdminNavMenu({ selectedKey }: Props) {
  const navigate = useNavigate();
  const adminActive = isAdminNavKey(selectedKey);

  const menuItems: MenuProps["items"] = ADMIN_NAV_ITEMS.map(({ key, to, label, icon: Icon }) => ({
    key,
    label: (
      <Link to={to} className="inline-flex items-center gap-2 text-inherit no-underline">
        <Icon className="size-4" aria-hidden />
        {label}
      </Link>
    ),
    onClick: () => navigate(to),
  }));

  return (
    <Dropdown
      menu={{
        items: menuItems,
        selectedKeys: adminActive ? [selectedKey] : [],
      }}
      trigger={["click"]}
      placement="bottomRight"
    >
      <button
        type="button"
        className={cn(
          "inline-flex min-h-10 shrink-0 items-center gap-1.5 rounded-md px-2.5 text-sm font-medium transition-colors sm:gap-2 sm:px-3",
          adminActive
            ? "bg-emerald-500/10 text-emerald-600 ring-1 ring-emerald-500/25 dark:text-emerald-300 dark:ring-emerald-500/20"
            : "text-muted-foreground hover:bg-muted hover:text-foreground",
        )}
        aria-haspopup="menu"
        aria-expanded={undefined}
        aria-current={adminActive ? "true" : undefined}
      >
        <ShieldCheck className="size-4" aria-hidden />
        <span>Admin</span>
        <ChevronDown className="size-3.5 opacity-70" aria-hidden />
      </button>
    </Dropdown>
  );
}
