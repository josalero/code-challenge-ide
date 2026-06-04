import { Link } from "react-router-dom";
import { cn } from "../lib/utils";
import { ADMIN_NAV_ITEMS, PRIMARY_NAV_ITEMS } from "./appNavItems";

type Props = {
  selectedKey: string;
  isAdmin: boolean;
};

function FooterNavLink({
  to,
  label,
  active,
}: {
  to: string;
  label: string;
  active: boolean;
}) {
  return (
    <Link
      to={to}
      className={cn(
        "no-underline transition-colors hover:text-foreground",
        active ? "font-medium text-emerald-600 dark:text-emerald-300" : "text-muted-foreground",
      )}
      aria-current={active ? "page" : undefined}
    >
      {label}
    </Link>
  );
}

export default function AppFooterNav({ selectedKey, isAdmin }: Props) {
  const navItems = isAdmin
    ? [...PRIMARY_NAV_ITEMS, ...ADMIN_NAV_ITEMS]
    : PRIMARY_NAV_ITEMS;

  return (
    <nav
      className="flex flex-wrap items-center gap-x-2 gap-y-1"
      aria-label="Site navigation"
    >
      {navItems.map(({ key, to, label }, index) => (
        <span key={key} className="inline-flex items-center gap-2">
          {index > 0 && (
            <span className="text-border" aria-hidden>
              ·
            </span>
          )}
          <FooterNavLink to={to} label={label} active={selectedKey === key} />
        </span>
      ))}
    </nav>
  );
}
