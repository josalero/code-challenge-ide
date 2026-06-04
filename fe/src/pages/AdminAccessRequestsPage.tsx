import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  App,
  Button,
  Form,
  Input,
  Modal,
  Segmented,
  Select,
  Table,
  Tag,
  Typography,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import { Check, X } from "lucide-react";
import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { apiFetch, ApiError } from "../api/client";
import type {
  AccessRequestSummary,
  ApproveAccessRequestRequest,
  CreateUserResponse,
  UserRole,
} from "../api/types";
import AppLayout from "../components/AppLayout";
import UserCreatedResultAlert from "../components/admin-users/UserCreatedResultAlert";
import CtlCard from "../components/ui/CtlCard";
import PageHeader from "../components/ui/PageHeader";
import { ApiPaths } from "../domain/constants";
import { generateTemporaryPassword } from "../utils/temporaryPassword";

type StatusFilter = "PENDING" | "ALL";

type ApproveForm = {
  temporaryPassword: string;
  role: UserRole;
};

function statusTag(status: string) {
  switch (status) {
    case "PENDING":
      return <Tag color="gold">Pending</Tag>;
    case "APPROVED":
      return <Tag color="green">Approved</Tag>;
    case "REJECTED":
      return <Tag color="default">Rejected</Tag>;
    default:
      return <Tag>{status}</Tag>;
  }
}

function formatWhen(value: string | null) {
  if (!value) {
    return "—";
  }
  return new Date(value).toLocaleString();
}

export default function AdminAccessRequestsPage() {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const [filter, setFilter] = useState<StatusFilter>("PENDING");
  const [approveTarget, setApproveTarget] = useState<AccessRequestSummary | null>(null);
  const [rejectTarget, setRejectTarget] = useState<AccessRequestSummary | null>(null);
  const [created, setCreated] = useState<CreateUserResponse | null>(null);
  const [approveForm] = Form.useForm<ApproveForm>();
  const [rejectNotes, setRejectNotes] = useState("");

  const listQuery = useQuery({
    queryKey: ["admin", "access-requests", filter],
    queryFn: () =>
      apiFetch<AccessRequestSummary[]>(
        filter === "PENDING"
          ? `${ApiPaths.ADMIN_ACCESS_REQUESTS}?status=PENDING`
          : ApiPaths.ADMIN_ACCESS_REQUESTS,
      ),
  });

  const pendingCountQuery = useQuery({
    queryKey: ["admin", "access-requests", "pending-count"],
    queryFn: () =>
      apiFetch<{ pending: number }>(`${ApiPaths.ADMIN_ACCESS_REQUESTS}/pending-count`),
  });

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ["admin", "access-requests"] });
  };

  const approveMutation = useMutation({
    mutationFn: ({
      id,
      body,
    }: {
      id: string;
      body: ApproveAccessRequestRequest;
    }) =>
      apiFetch<CreateUserResponse>(`${ApiPaths.ADMIN_ACCESS_REQUESTS}/${id}/approve`, {
        method: "POST",
        body: JSON.stringify(body),
      }),
    onSuccess: (response) => {
      setCreated(response);
      setApproveTarget(null);
      approveForm.resetFields();
      message.success("Access approved and user account created");
      invalidate();
    },
    onError: (error) =>
      message.error(error instanceof ApiError ? error.message : "Could not approve request"),
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reviewNotes }: { id: string; reviewNotes?: string }) =>
      apiFetch<AccessRequestSummary>(`${ApiPaths.ADMIN_ACCESS_REQUESTS}/${id}/reject`, {
        method: "POST",
        body: JSON.stringify({ reviewNotes: reviewNotes?.trim() || undefined }),
      }),
    onSuccess: () => {
      setRejectTarget(null);
      setRejectNotes("");
      message.success("Access request rejected");
      invalidate();
    },
    onError: (error) =>
      message.error(error instanceof ApiError ? error.message : "Could not reject request"),
  });

  const columns: ColumnsType<AccessRequestSummary> = useMemo(
    () => [
      {
        title: "Submitted",
        dataIndex: "createdAt",
        key: "createdAt",
        align: "center",
        render: (value: string) => formatWhen(value),
        width: 180,
      },
      {
        title: "Name",
        dataIndex: "fullName",
        key: "fullName",
        align: "center",
      },
      {
        title: "Email",
        dataIndex: "email",
        key: "email",
        align: "center",
      },
      {
        title: "Message",
        dataIndex: "message",
        key: "message",
        align: "center",
        render: (value: string | null) =>
          value ? (
            <Typography.Paragraph className="!mb-0 mx-auto max-w-md whitespace-pre-wrap text-center text-sm">
              {value}
            </Typography.Paragraph>
          ) : (
            <span className="text-muted-foreground">—</span>
          ),
      },
      {
        title: "Status",
        dataIndex: "status",
        key: "status",
        align: "center",
        render: (value: string) => statusTag(value),
        width: 110,
      },
      {
        title: "Reviewed",
        dataIndex: "reviewedAt",
        key: "reviewedAt",
        align: "center",
        render: (value: string | null) => formatWhen(value),
        width: 180,
      },
      {
        title: "Actions",
        key: "actions",
        width: 200,
        align: "center",
        render: (_, row) =>
          row.status === "PENDING" ? (
            <div className="flex flex-wrap justify-center gap-2">
              <Button
                type="primary"
                size="small"
                icon={<Check className="size-3.5" aria-hidden />}
                onClick={() => {
                  setCreated(null);
                  setApproveTarget(row);
                  approveForm.setFieldsValue({
                    role: "USER",
                    temporaryPassword: generateTemporaryPassword(),
                  });
                }}
              >
                Approve
              </Button>
              <Button
                size="small"
                danger
                icon={<X className="size-3.5" aria-hidden />}
                onClick={() => {
                  setRejectTarget(row);
                  setRejectNotes("");
                }}
              >
                Reject
              </Button>
            </div>
          ) : (
            <span className="text-xs text-muted-foreground">
              {row.reviewNotes ? row.reviewNotes : "Reviewed"}
            </span>
          ),
      },
    ],
    [approveForm],
  );

  const copyCredentials = async () => {
    if (!created) {
      return;
    }
    const text = [
      "Code Training Lab account",
      `Email (username): ${created.email}`,
      `Role: ${created.role}`,
      `Temporary password: ${created.temporaryPassword}`,
      "",
      "Sign in and you will be asked to set a new password.",
    ].join("\n");
    await navigator.clipboard.writeText(text);
    message.success("Sign-in details copied");
  };

  const pendingCount = pendingCountQuery.data?.pending ?? 0;

  return (
    <AppLayout>
      <PageHeader
        title="Access requests"
        description="Review people who asked to try the lab. Approve to create their account and send welcome email, or reject with optional notes."
        extra={
          <Link to="/admin/users">
            <Button>Create user manually</Button>
          </Link>
        }
      />

      {created && (
        <UserCreatedResultAlert
          created={created}
          title="User created from access request"
          onCopy={copyCredentials}
        />
      )}

      <CtlCard>
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <Segmented
            value={filter}
            onChange={(value) => setFilter(value as StatusFilter)}
            options={[
              { label: `Pending (${pendingCount})`, value: "PENDING" },
              { label: "All requests", value: "ALL" },
            ]}
          />
          <Button onClick={() => void listQuery.refetch()} loading={listQuery.isFetching}>
            Refresh
          </Button>
        </div>

        <Table
          rowKey="id"
          loading={listQuery.isLoading}
          columns={columns}
          dataSource={listQuery.data ?? []}
          pagination={{ pageSize: 10, showSizeChanger: false }}
          locale={{
            emptyText:
              filter === "PENDING"
                ? "No pending access requests."
                : "No access requests yet.",
          }}
        />
      </CtlCard>

      <Modal
        title={`Approve ${approveTarget?.fullName ?? "request"}`}
        open={Boolean(approveTarget)}
        onCancel={() => setApproveTarget(null)}
        footer={null}
        destroyOnHidden
      >
        <p className="mb-4 text-sm text-muted-foreground">
          Creates a learner account for <strong>{approveTarget?.email}</strong> and sends the
          welcome email with the temporary password.
        </p>
        <Form
          form={approveForm}
          layout="vertical"
          onFinish={(values) => {
            if (!approveTarget) {
              return;
            }
            approveMutation.mutate({
              id: approveTarget.id,
              body: {
                temporaryPassword: values.temporaryPassword,
                role: values.role,
              },
            });
          }}
        >
          <Form.Item label="Role" name="role" rules={[{ required: true }]}>
            <Select
              options={[
                { value: "USER", label: "Learner" },
                { value: "ADMIN", label: "Administrator" },
              ]}
            />
          </Form.Item>
          <Form.Item
            label="Temporary password"
            name="temporaryPassword"
            rules={[
              { required: true, message: "Temporary password is required" },
              { min: 8, message: "At least 8 characters" },
            ]}
          >
            <Input.Password />
          </Form.Item>
          <Button
            className="mb-4"
            onClick={() =>
              approveForm.setFieldValue("temporaryPassword", generateTemporaryPassword())
            }
          >
            Generate password
          </Button>
          <div className="flex justify-end gap-2">
            <Button onClick={() => setApproveTarget(null)}>Cancel</Button>
            <Button type="primary" htmlType="submit" loading={approveMutation.isPending}>
              Approve and create user
            </Button>
          </div>
        </Form>
      </Modal>

      <Modal
        title={`Reject ${rejectTarget?.fullName ?? "request"}`}
        open={Boolean(rejectTarget)}
        onCancel={() => setRejectTarget(null)}
        onOk={() => {
          if (!rejectTarget) {
            return;
          }
          rejectMutation.mutate({ id: rejectTarget.id, reviewNotes: rejectNotes });
        }}
        okText="Reject request"
        okButtonProps={{ danger: true, loading: rejectMutation.isPending }}
        destroyOnHidden
      >
        <p className="mb-3 text-sm text-muted-foreground">
          Optional note for your records (not emailed to the requester automatically).
        </p>
        <Input.TextArea
          rows={4}
          value={rejectNotes}
          onChange={(event) => setRejectNotes(event.target.value)}
          placeholder="Reason or internal note"
          maxLength={2000}
        />
      </Modal>
    </AppLayout>
  );
}
