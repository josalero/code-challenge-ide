import {
  PlayCircleOutlined,
  ReloadOutlined,
  SaveOutlined,
  StopOutlined,
} from "@ant-design/icons";
import { Button, Popconfirm, Select, Space, Tooltip, Typography } from "antd";
import type { ChallengeDetail } from "../api/types";
import { formatLanguageLabel, formatRuntimeLabel } from "../utils/languageRuntimes";

type Props = {
  challenge: ChallengeDetail;
  runtimeVersion: string;
  onRuntimeChange: (version: string) => void;
  isRunning: boolean;
  onRunTests: () => void;
  onCancel?: () => void;
  cancelLoading?: boolean;
  showCancel: boolean;
  onResetStarter: () => void;
  onSaveCustomTests?: () => void;
  saveCustomTestsLoading?: boolean;
  activeTab: "solution" | "custom";
};

export default function WorkspaceToolbar({
  challenge,
  runtimeVersion,
  onRuntimeChange,
  isRunning,
  onRunTests,
  onCancel,
  cancelLoading,
  showCancel,
  onResetStarter,
  onSaveCustomTests,
  saveCustomTestsLoading,
  activeTab,
}: Props) {
  return (
    <div
      className="sticky top-0 z-20 -mx-4 mb-4 border-b border-slate-800/80 bg-slate-950/95 px-4 py-3 shadow-sm shadow-black/20 backdrop-blur md:-mx-6 md:px-6"
      role="toolbar"
      aria-label="Workspace actions"
    >
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Space wrap>
          <Select
            value={runtimeVersion}
            onChange={onRuntimeChange}
            disabled={isRunning}
            options={challenge.runtimes
              .filter((r) => r.active)
              .map((r) => ({
                value: r.version,
                label: formatRuntimeLabel(challenge.language, r.version),
              }))}
            aria-label={`${formatLanguageLabel(challenge.language)} runtime version`}
          />
          <Tooltip title="Runs public + hidden tests with coverage in Docker (⌘/Ctrl + Enter)">
            <Button
              type="primary"
              size="middle"
              icon={<PlayCircleOutlined />}
              loading={isRunning}
              disabled={isRunning}
              onClick={onRunTests}
            >
              {isRunning ? "Running…" : "Run tests"}
            </Button>
          </Tooltip>
          {showCancel && onCancel && (
            <Button
              danger
              icon={<StopOutlined />}
              loading={cancelLoading}
              onClick={onCancel}
            >
              Cancel
            </Button>
          )}
          {activeTab === "custom" && onSaveCustomTests && (
            <Button
              icon={<SaveOutlined />}
              loading={saveCustomTestsLoading}
              disabled={isRunning}
              onClick={onSaveCustomTests}
            >
              Save custom tests
            </Button>
          )}
          <Popconfirm
            title="Reset solution to starter code?"
            description="Your current edits will be lost."
            onConfirm={onResetStarter}
            okText="Reset"
            cancelText="Keep editing"
            disabled={isRunning}
          >
            <Button icon={<ReloadOutlined />} disabled={isRunning}>
              Reset starter
            </Button>
          </Popconfirm>
        </Space>
        <Typography.Text className="!text-slate-500 text-xs hidden sm:inline">
          <kbd className="rounded border border-slate-700 bg-slate-900 px-1.5 py-0.5 font-mono text-slate-400">
            ⌘
          </kbd>
          <span className="mx-0.5">/</span>
          <kbd className="rounded border border-slate-700 bg-slate-900 px-1.5 py-0.5 font-mono text-slate-400">
            Ctrl
          </kbd>
          <span className="ml-1">+ Enter to run</span>
        </Typography.Text>
      </div>
    </div>
  );
}
