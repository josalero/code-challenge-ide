import { Typography } from "antd";
import { cn } from "../lib/utils";

type AppLogoProps = {
  showTagline?: boolean;
  className?: string;
  iconClassName?: string;
};

export default function AppLogo({
  showTagline = true,
  className,
  iconClassName,
}: AppLogoProps) {
  return (
    <span className={cn("flex min-w-0 items-center gap-2.5", className)}>
      <img
        src="/logo.svg"
        alt=""
        width={36}
        height={36}
        className={cn("size-9 shrink-0 rounded-md", iconClassName)}
        aria-hidden
      />
      <span className="min-w-0">
        <Typography.Text className="!text-foreground block text-sm font-semibold leading-tight">
          Code Training Lab
        </Typography.Text>
        {showTagline && (
          <Typography.Text className="!text-muted-foreground hidden text-xs sm:block">
            Practice · test · improve
          </Typography.Text>
        )}
      </span>
    </span>
  );
}
