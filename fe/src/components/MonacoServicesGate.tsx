import { Loader2 } from "lucide-react";
import { useEffect, useState } from "react";
import { ensureMonacoEditorServices } from "../monacoEditorServices";

type Props = {
  children: React.ReactNode;
  label?: string;
};

export default function MonacoServicesGate({
  children,
  label = "Loading editor…",
}: Props) {
  const [ready, setReady] = useState(false);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setReady(false);
    setFailed(false);

    const timeoutId = setTimeout(() => {
      if (!cancelled) {
        setFailed(true);
        if (import.meta.env.DEV) {
          console.error("Monaco editor services timed out after 30s");
        }
      }
    }, 30_000);

    ensureMonacoEditorServices()
      .then(() => {
        if (!cancelled) {
          clearTimeout(timeoutId);
          setReady(true);
        }
      })
      .catch((error) => {
        clearTimeout(timeoutId);
        if (!cancelled) {
          setFailed(true);
          if (import.meta.env.DEV) {
            console.error("Monaco editor services failed", error);
          }
        }
      });

    return () => {
      cancelled = true;
      clearTimeout(timeoutId);
    };
  }, []);

  if (failed) {
    return (
      <div className="flex h-full min-h-0 items-center justify-center px-4 text-center text-sm text-amber-200">
        Could not initialize the code editor.
      </div>
    );
  }

  if (!ready) {
    return (
      <div className="flex h-full min-h-0 items-center justify-center gap-2 text-sm text-slate-400">
        <Loader2 className="size-4 animate-spin text-sky-400" aria-hidden />
        {label}
      </div>
    );
  }

  return children;
}
