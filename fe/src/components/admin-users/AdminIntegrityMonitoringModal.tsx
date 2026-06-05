import { useMutation } from "@tanstack/react-query";
import { App, Modal, Switch } from "antd";
import { useEffect, useState } from "react";
import { apiFetch, ApiError } from "../../api/client";
import type { AdminUserSummary, UserIntegrityMonitoringResponse } from "../../api/types";
import { ApiPaths } from "../../domain/constants";

type Props = {
  user: AdminUserSummary | null;
  open: boolean;
  onClose: () => void;
  onUpdated: () => void;
};

export default function AdminIntegrityMonitoringModal({
  user,
  open,
  onClose,
  onUpdated,
}: Props) {
  const { message } = App.useApp();
  const [disabled, setDisabled] = useState(false);

  useEffect(() => {
    if (!user || !open) {
      return;
    }
    setDisabled(user.integrityMonitoringDisabled);
  }, [user, open]);

  const saveMutation = useMutation({
    mutationFn: (integrityMonitoringDisabled: boolean) =>
      apiFetch<UserIntegrityMonitoringResponse>(
        ApiPaths.adminUserIntegrityMonitoring(user!.id),
        {
          method: "PATCH",
          body: JSON.stringify({ integrityMonitoringDisabled }),
        },
      ),
    onSuccess: () => {
      message.success("Integrity monitoring updated");
      onUpdated();
      onClose();
    },
    onError: (error) =>
      message.error(
        error instanceof ApiError ? error.message : "Could not update integrity monitoring",
      ),
  });

  if (!user) {
    return null;
  }

  return (
    <Modal
      title="Integrity monitoring"
      open={open}
      okText="Save"
      confirmLoading={saveMutation.isPending}
      onOk={() => saveMutation.mutate(disabled)}
      onCancel={onClose}
      destroyOnClose
    >
      <p className="mb-4 text-sm text-muted-foreground">
        Control silent integrity signals (clipboard usage, tab/focus, large edits) for{" "}
        <strong className="text-foreground">{user.fullName}</strong>. Learners are not notified;
        signals appear only in admin challenge reports. Administrators are always exempt. Disabling
        monitoring stops recording for this learner.
      </p>

      <label className="flex items-start gap-3 rounded-lg border border-border/80 bg-card/50 p-3">
        <Switch
          checked={disabled}
          onChange={setDisabled}
          aria-label="Disable integrity monitoring for this learner"
        />
        <span className="text-sm">
          <span className="font-medium text-foreground">Disable integrity monitoring</span>
          <span className="mt-1 block text-muted-foreground">
            When off, no integrity signals are recorded for this learner during timed attempts.
          </span>
        </span>
      </label>
    </Modal>
  );
}
