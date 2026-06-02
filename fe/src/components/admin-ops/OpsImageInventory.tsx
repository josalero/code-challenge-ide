import { Collapse, Table, Tag } from "antd";
import { Box, CheckCircle2 } from "lucide-react";
import type { LanguageWarmStatus, RunnerImageStatus, RunnerOpsStatus } from "@/api/types";

function builtTag(present: boolean) {
  return present ? (
    <Tag color="success">On disk</Tag>
  ) : (
    <Tag color="error">Missing</Tag>
  );
}

function warmTag(warmed: boolean | null | undefined) {
  if (warmed == null) {
    return <Tag color="default">—</Tag>;
  }
  return warmed ? (
    <Tag color="success">Preloaded</Tag>
  ) : (
    <Tag color="warning">Cold</Tag>
  );
}

function imageTableColumns(kind: "runner" | "lsp") {
  return [
    { title: "Runtime", dataIndex: "label", key: "label", width: 140 },
    {
      title: "Docker image",
      dataIndex: "image",
      key: "image",
      ellipsis: true,
      render: (image: string) => (
        <code className="text-xs text-slate-400">{image}</code>
      ),
    },
    {
      title: "Built",
      key: "present",
      width: 100,
      render: (_: unknown, row: RunnerImageStatus) => builtTag(row.present),
    },
    {
      title: kind === "runner" ? "Run tests preload" : "Editor preload",
      key: "warmed",
      width: 130,
      render: (_: unknown, row: RunnerImageStatus) => warmTag(row.warmed),
    },
  ];
}

type Props = {
  status: RunnerOpsStatus;
  languageRows: LanguageWarmStatus[];
  readyLanguageCount: number;
  totalLanguages: number;
};

export default function OpsImageInventory({
  status,
  languageRows,
  readyLanguageCount,
  totalLanguages,
}: Props) {
  return (
    <Collapse
      className="!border-slate-800/80 !bg-transparent [&_.ant-collapse-item]:!border-slate-800/80"
      items={[
        {
          key: "inventory",
          label: (
            <span className="text-sm font-medium text-slate-300">
              Technical details — images & runtimes
            </span>
          ),
          children: (
            <div className="space-y-4">
              <div className="flex flex-wrap gap-4 rounded-md border border-slate-800/60 bg-slate-950/40 px-3 py-2 text-xs text-slate-500">
                <span className="inline-flex items-center gap-1.5">
                  <CheckCircle2 className="size-3.5 text-emerald-400" aria-hidden />
                  On disk = <code className="text-[10px]">make runners</code> built the image
                </span>
                <span className="inline-flex items-center gap-1.5">
                  <Box className="size-3.5 text-amber-400" aria-hidden />
                  Preloaded = warm job finished for this image tag
                </span>
              </div>
              <Table
                size="small"
                pagination={false}
                title={() => (
                  <span className="text-xs font-medium text-slate-400">
                    By runtime ({readyLanguageCount}/{totalLanguages} languages fully ready)
                  </span>
                )}
                rowKey={(row) =>
                  `${row.language}-${row.version ?? "none"}-${row.runnerImage ?? row.label}`
                }
                dataSource={languageRows}
                columns={[
                  {
                    title: "Language",
                    dataIndex: "language",
                    key: "language",
                    width: 100,
                    render: (lang: string) => (
                      <span className="font-medium capitalize text-slate-200">{lang}</span>
                    ),
                  },
                  { title: "Runtime", dataIndex: "label", key: "label", width: 120 },
                  {
                    title: "Run tests",
                    dataIndex: "runnerReady",
                    key: "runnerReady",
                    width: 110,
                    render: (v: boolean | null, row: LanguageWarmStatus) =>
                      row.runnerPresent ? warmTag(v) : <Tag color="error">No image</Tag>,
                  },
                  {
                    title: "IntelliSense",
                    dataIndex: "editorReady",
                    key: "editorReady",
                    width: 110,
                    render: (v: boolean | null) => warmTag(v),
                  },
                ]}
              />
              <Table
                size="small"
                pagination={false}
                title={() => (
                  <span className="text-xs font-medium text-slate-400">Runner images</span>
                )}
                rowKey="image"
                dataSource={status.runnerImages}
                columns={imageTableColumns("runner")}
              />
              <Table
                size="small"
                pagination={false}
                title={() => (
                  <span className="text-xs font-medium text-slate-400">Editor (LSP) images</span>
                )}
                rowKey={(row) => `${row.label}-${row.image}`}
                dataSource={status.lspImages}
                columns={imageTableColumns("lsp")}
              />
              <p className="text-[10px] leading-relaxed text-slate-600">
                Warm state is stored in the database and survives API restarts. Legacy stamp files
                under <code className="text-slate-500">{status.opsDataDir}</code> are imported once on
                startup.
              </p>
            </div>
          ),
        },
      ]}
    />
  );
}
