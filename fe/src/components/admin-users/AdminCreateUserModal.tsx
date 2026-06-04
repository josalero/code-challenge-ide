import { Alert, Button, Form, Input, Modal, Select } from "antd";
import { useState } from "react";
import { apiFetch, ApiError } from "../../api/client";
import type { CreateUserRequest, CreateUserResponse, UserRole } from "../../api/types";
import { ApiPaths } from "../../domain/constants";
import { generateTemporaryPassword } from "../../utils/temporaryPassword";
import UserCreatedResultAlert from "./UserCreatedResultAlert";

type CreateUserForm = {
  email: string;
  fullName: string;
  role: UserRole;
  temporaryPassword: string;
};

type Props = {
  open: boolean;
  onClose: () => void;
  onCreated: () => void;
};

export default function AdminCreateUserModal({ open, onClose, onCreated }: Props) {
  const [form] = Form.useForm<CreateUserForm>();
  const [error, setError] = useState<string | null>(null);
  const [created, setCreated] = useState<CreateUserResponse | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const resetState = () => {
    setError(null);
    setCreated(null);
    form.resetFields();
  };

  const handleClose = () => {
    resetState();
    onClose();
  };

  const onGeneratePassword = () => {
    form.setFieldValue("temporaryPassword", generateTemporaryPassword());
  };

  const onFinish = async (values: CreateUserForm) => {
    setError(null);
    setCreated(null);
    setSubmitting(true);
    try {
      const body: CreateUserRequest = {
        email: values.email.trim().toLowerCase(),
        fullName: values.fullName.trim(),
        role: values.role,
        temporaryPassword: values.temporaryPassword,
      };
      const response = await apiFetch<CreateUserResponse>(ApiPaths.ADMIN_USERS, {
        method: "POST",
        body: JSON.stringify(body),
      });
      setCreated(response);
      form.resetFields();
      onCreated();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Could not create user");
    } finally {
      setSubmitting(false);
    }
  };

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
  };

  return (
    <Modal
      title="Create user"
      open={open}
      onCancel={handleClose}
      footer={null}
      destroyOnClose
      width={520}
    >
      {error && (
        <Alert type="error" message={error} showIcon className="mb-4" role="alert" />
      )}

      {created && (
        <UserCreatedResultAlert
          created={created}
          title="User created"
          onCopy={copyCredentials}
        />
      )}

      <Form
        form={form}
        layout="vertical"
        onFinish={onFinish}
        requiredMark={false}
        initialValues={{ role: "USER" as UserRole }}
      >
        <Form.Item
          label="Email (username)"
          name="email"
          rules={[
            { required: true, message: "Email is required" },
            { type: "email", message: "Enter a valid email address" },
          ]}
        >
          <Input placeholder="learner@company.com" autoComplete="off" />
        </Form.Item>
        <Form.Item
          label="Full name"
          name="fullName"
          rules={[{ required: true, message: "Full name is required" }]}
        >
          <Input placeholder="Ada Lovelace" autoComplete="off" />
        </Form.Item>
        <Form.Item
          label="Role"
          name="role"
          rules={[{ required: true, message: "Role is required" }]}
        >
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
          <Input.Password placeholder="Generate or type a temporary password" />
        </Form.Item>
        <div className="mb-4 flex flex-wrap gap-2">
          <Button onClick={onGeneratePassword}>Generate password</Button>
        </div>
        <p className="mb-4 text-sm text-muted-foreground">
          When mail is configured, a welcome email with the temporary password is sent to the new
          user automatically.
        </p>
        <div className="flex justify-end gap-2">
          <Button onClick={handleClose}>Close</Button>
          <Button type="primary" htmlType="submit" loading={submitting}>
            Create user
          </Button>
        </div>
      </Form>
    </Modal>
  );
}
