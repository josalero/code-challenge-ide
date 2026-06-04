import { useMutation } from "@tanstack/react-query";
import { App, InputNumber, Modal, Radio, Space } from "antd";
import { useEffect, useState } from "react";
import { apiFetch, ApiError } from "../../api/client";
import type { AdminUserSummary, UserChallengeQuotaResponse } from "../../api/types";
import { ApiPaths } from "../../domain/constants";

type QuotaMode = "default" | "custom" | "unlimited";

type Props = {
  user: AdminUserSummary | null;
  open: boolean;
  onClose: () => void;
  onUpdated: () => void;
};

function initialMode(user: AdminUserSummary): QuotaMode {
  if (user.challengeQuotaOverride == null) {
    return "default";
  }
  if (user.challengeQuotaOverride === 0) {
    return "unlimited";
  }
  return "custom";
}

function initialCustomLimit(user: AdminUserSummary): number {
  if (user.challengeQuotaOverride != null && user.challengeQuotaOverride > 0) {
    return user.challengeQuotaOverride;
  }
  return user.platformDefaultChallengeLimit;
}

export default function AdminChallengeQuotaModal({ user, open, onClose, onUpdated }: Props) {
  const { message } = App.useApp();
  const [mode, setMode] = useState<QuotaMode>("default");
  const [customLimit, setCustomLimit] = useState(5);

  useEffect(() => {
    if (!user || !open) {
      return;
    }
    setMode(initialMode(user));
    setCustomLimit(initialCustomLimit(user));
  }, [user, open]);

  const saveMutation = useMutation({
    mutationFn: (maxStartedChallenges: number | null) =>
      apiFetch<UserChallengeQuotaResponse>(ApiPaths.adminUserChallengeQuota(user!.id), {
        method: "PATCH",
        body: JSON.stringify({ maxStartedChallenges }),
      }),
    onSuccess: () => {
      message.success("Exercise limit updated");
      onUpdated();
      onClose();
    },
    onError: (error) =>
      message.error(error instanceof ApiError ? error.message : "Could not update exercise limit"),
  });

  if (!user) {
    return null;
  }

  const handleSave = () => {
    const payload =
      mode === "default" ? null : mode === "unlimited" ? 0 : Math.max(1, customLimit);
    saveMutation.mutate(payload);
  };

  return (
    <Modal
      title="Exercise limit"
      open={open}
      okText="Save"
      confirmLoading={saveMutation.isPending}
      onOk={handleSave}
      onCancel={onClose}
      destroyOnClose
    >
      <p className="mb-4 text-sm text-muted-foreground">
        Set how many distinct exercises <strong className="text-foreground">{user.fullName}</strong>{" "}
        can start. Platform default is {user.platformDefaultChallengeLimit}. Currently started:{" "}
        {user.challengesStarted}
        {user.effectiveChallengeLimit != null
          ? ` · effective limit ${user.effectiveChallengeLimit}`
          : ""}
        .
      </p>

      <Radio.Group
        value={mode}
        onChange={(event) => setMode(event.target.value as QuotaMode)}
        className="flex w-full flex-col gap-3"
      >
        <Radio value="default">
          Use platform default ({user.platformDefaultChallengeLimit} exercises)
        </Radio>
        <Radio value="custom">
          <Space wrap>
            Extended limit
            <InputNumber
              min={1}
              max={100}
              value={customLimit}
              disabled={mode !== "custom"}
              onChange={(value) => setCustomLimit(value ?? user.platformDefaultChallengeLimit)}
            />
            exercises
          </Space>
        </Radio>
        <Radio value="unlimited">Unlimited (no cap)</Radio>
      </Radio.Group>
    </Modal>
  );
}
