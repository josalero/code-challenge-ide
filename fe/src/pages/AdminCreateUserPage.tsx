import { Alert, Button, Form, Input, Select, Typography } from "antd";
import { Copy } from "lucide-react";
import { useState } from "react";
import { apiFetch, ApiError } from "../api/client";
import type { CreateUserRequest, CreateUserResponse, UserRole } from "../api/types";
import AppLayout from "../components/AppLayout";
import CtlCard from "../components/ui/CtlCard";
import PageHeader from "../components/ui/PageHeader";
import { ApiPaths } from "../domain/constants";

type CreateUserForm = {
  email: string;
  fullName: string;
  role: UserRole;
  temporaryPassword: string;
};

function generateTemporaryPassword(): string {
  const upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
  const lower = "abcdefghjkmnpqrstuvwxyz";
  const digits = "23456789";
  const all = upper + lower + digits;
  const pick = (chars: string) => chars[Math.floor(Math.random() * chars.length)];
  const base = [pick(upper), pick(lower), pick(digits)];
  while (base.length < 12) {
    base.push(pick(all));
  }
  for (let i = base.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [base[i], base[j]] = [base[j], base[i]];
  }
  return base.join("");
}

export default function AdminCreateUserPage() {
  const [form] = Form.useForm<CreateUserForm>();
  const [error, setError] = useState<string | null>(null);
  const [created, setCreated] = useState<CreateUserResponse | null>(null);
  const [submitting, setSubmitting] = useState(false);

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
      `Code Training Lab account`,
      `Email (username): ${created.email}`,
      `Role: ${created.role}`,
      `Temporary password: ${created.temporaryPassword}`,
      ``,
      `Sign in and you will be asked to set a new password.`,
    ].join("\n");
    await navigator.clipboard.writeText(text);
  };

  return (
    <AppLayout>
      <PageHeader
        title="Create user"
        description="Provision accounts with email as username, a role, and a temporary password. New users must set a permanent password on first sign-in."
      />

      {error && (
        <Alert type="error" message={error} showIcon className="mb-4" role="alert" />
      )}

      {created && (
        <Alert
          type="success"
          showIcon
          className="mb-4"
          message="User created"
          description={
            <div className="space-y-2 text-sm">
              <p>
                <strong>{created.fullName}</strong> ({created.email}) was created as{" "}
                <strong>{created.role === "ADMIN" ? "administrator" : "learner"}</strong>. Share
                the temporary password below securely — it is shown only once.
              </p>
              <p>
                Temporary password:{" "}
                <Typography.Text code copyable>
                  {created.temporaryPassword}
                </Typography.Text>
              </p>
              <Button
                type="default"
                size="small"
                icon={<Copy className="size-4" aria-hidden />}
                onClick={() => void copyCredentials()}
              >
                Copy sign-in details
              </Button>
            </div>
          }
        />
      )}

      <CtlCard>
        <Form
          form={form}
          layout="vertical"
          onFinish={onFinish}
          requiredMark={false}
          size="large"
          className="max-w-lg"
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
            extra="Administrators can manage challenges, users, and operations. Learners can practice challenges only."
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
            extra="Minimum 8 characters; must not match the email. User will replace it on first login."
          >
            <Input.Password placeholder="Generate or type a temporary password" />
          </Form.Item>
          <div className="mb-4 flex flex-wrap gap-2">
            <Button onClick={onGeneratePassword}>Generate password</Button>
          </div>
          <Button type="primary" htmlType="submit" loading={submitting}>
            Create user
          </Button>
        </Form>
      </CtlCard>
    </AppLayout>
  );
}
