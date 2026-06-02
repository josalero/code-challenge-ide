import { Collapse } from "antd";
import { ListOrdered } from "lucide-react";
import type { RunnerOpsStatus } from "@/api/types";

type Props = {
  status: RunnerOpsStatus;
  dockerReady: boolean;
};

const STEPS = [
  {
    key: "docker",
    title: "Docker reachable from the API",
    detail: (dockerReady: boolean, enabled: boolean) =>
      dockerReady
        ? "The API can start containers for tests and IntelliSense."
        : enabled
          ? "Fix Docker socket access on the API host, then refresh."
          : "Set CTL_DOCKER_ENABLED=true and restart the API.",
  },
  {
    key: "images",
    title: "Runner images built locally",
    detail: () =>
      "On the host that runs the API, run make runners so submission and LSP images exist.",
  },
  {
    key: "warm",
    title: "Warm languages (this page)",
    detail: () =>
      "Click Warm everything or warm individual languages. First warm after deploy can take several minutes.",
  },
  {
    key: "lsp-runtime",
    title: "LSP containers while editing",
    detail: () =>
      "IntelliSense uses one pooled container per signed-in user and language (ctl-lsp-pool-<user>-<language>), "
      + "reused across editor tabs via docker exec. That is separate from test runners (ctl-runner-pool-…). "
      + "Idle pools are evicted every minute; orphaned ctl-lsp-* containers are removed on startup.",
  },
] as const;

export default function OpsQuickStart({ status, dockerReady }: Props) {
  const imagesMissing =
    status.runnerImages.some((img) => !img.present)
    || status.lspImages.some((img) => !img.present);

  return (
    <Collapse
      className="mb-6 !border-slate-800/80 !bg-slate-900/40 [&_.ant-collapse-header]:!text-slate-200"
      defaultActiveKey={dockerReady && !imagesMissing ? [] : ["guide"]}
      items={[
        {
          key: "guide",
          label: (
            <span className="inline-flex items-center gap-2 text-sm font-medium">
              <ListOrdered className="size-4 text-sky-400" aria-hidden />
              Before you warm — quick checklist
            </span>
          ),
          children: (
            <ol className="m-0 list-none space-y-3 p-0">
              {STEPS.map((step, index) => {
                let done = false;
                if (step.key === "docker") {
                  done = dockerReady;
                } else if (step.key === "images") {
                  done = !imagesMissing;
                } else if (step.key === "warm") {
                  done = status.languages.some((l) => l.ready);
                }

                return (
                  <li
                    key={step.key}
                    className="flex gap-3 rounded-md border border-slate-800/60 bg-slate-950/40 px-3 py-2.5"
                  >
                    <span
                      className={`flex size-7 shrink-0 items-center justify-center rounded-full text-xs font-semibold ${
                        done
                          ? "bg-emerald-500/20 text-emerald-300"
                          : "bg-slate-800 text-slate-400"
                      }`}
                      aria-hidden
                    >
                      {done ? "✓" : index + 1}
                    </span>
                    <div className="min-w-0">
                      <p className="text-sm font-medium text-slate-200">{step.title}</p>
                      <p className="mt-0.5 text-xs leading-relaxed text-slate-500">
                        {step.key === "docker"
                          ? step.detail(dockerReady, status.dockerEnabled)
                          : step.detail()}
                      </p>
                    </div>
                  </li>
                );
              })}
            </ol>
          ),
        },
      ]}
    />
  );
}
