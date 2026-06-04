import { Alert, Button, Form, Input } from "antd";
import { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { ApiError } from "../api/client";
import { useAuth } from "../auth/useAuth";
import PasswordRequirements from "../components/PasswordRequirements";
import AuthShell from "../components/ui/AuthShell";

type ChangePasswordForm = {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
};

export default function ChangePasswordPage() {
  const { user, changePassword, logout } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  const onFinish = async (values: ChangePasswordForm) => {
    setError(null);
    if (values.newPassword !== values.confirmPassword) {
      setError("New password and confirmation do not match");
      return;
    }
    setSubmitting(true);
    try {
      await changePassword(values.currentPassword, values.newPassword);
      navigate("/challenges", { replace: true });
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Could not update password");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthShell
      title="Set a new password"
      subtitle={
        user.mustChangePassword
          ? "Your account uses a temporary password. Choose a new password before continuing."
          : "Update your password to keep your account secure."
      }
    >
      {error && (
        <Alert type="error" message={error} showIcon className="mb-4" role="alert" />
      )}
      <PasswordRequirements className="mb-4" />
      <Form layout="vertical" onFinish={onFinish} requiredMark={false} size="large">
        <Form.Item
          label={<span className="text-muted-foreground">Current password</span>}
          name="currentPassword"
          rules={[{ required: true, message: "Current password is required" }]}
        >
          <Input.Password autoComplete="current-password" />
        </Form.Item>
        <Form.Item
          label={<span className="text-muted-foreground">New password</span>}
          name="newPassword"
          rules={[{ required: true, message: "New password is required" }]}
        >
          <Input.Password autoComplete="new-password" />
        </Form.Item>
        <Form.Item
          label={<span className="text-muted-foreground">Confirm new password</span>}
          name="confirmPassword"
          rules={[{ required: true, message: "Please confirm your new password" }]}
        >
          <Input.Password autoComplete="new-password" />
        </Form.Item>
        <Button type="primary" htmlType="submit" block loading={submitting}>
          Save new password
        </Button>
        <Button
          type="link"
          block
          className="mt-2"
          onClick={() => {
            logout();
            navigate("/login");
          }}
        >
          Sign out
        </Button>
      </Form>
    </AuthShell>
  );
}
