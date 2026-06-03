import { Moon, Sun } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useTheme } from "@/theme/useTheme";

export default function ThemeToggle() {
  const { mode, toggleMode } = useTheme();
  const isDark = mode === "dark";

  return (
    <Button
      type="button"
      variant="ghost"
      size="icon"
      className="size-9 shrink-0 text-muted-foreground hover:bg-muted hover:text-foreground"
      onClick={toggleMode}
      aria-label={isDark ? "Switch to light theme" : "Switch to dark theme"}
      title={isDark ? "Light mode" : "Dark mode"}
    >
      {isDark ? <Sun className="size-4" aria-hidden /> : <Moon className="size-4" aria-hidden />}
    </Button>
  );
}
