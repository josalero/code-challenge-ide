import { Alert, Button, Form, Input, Spin } from "antd";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { apiFetch, ApiError } from "../api/client";
import type { RegistrationInfoResponse } from "../api/types";
import { useAuth } from "../auth/useAuth";
import PasswordRequirements from "../components/PasswordRequirements";
import AuthShell from "../components/ui/AuthShell";
import { ApiPaths } from "../domain/constants";

type RegisterForm = {
  email: string;
  password: string;
  confirm: string;
};

export default function RegisterPage() {
  const { register, token } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const registrationQuery = useQuery({
    queryKey: ["registration-info"],
    queryFn: () => apiFetch<RegistrationInfoResponse>(ApiPaths.AUTH_REGISTRATION_INFO),
  });

  if (token) {
    return <Navigate to="/challenges" replace />;
  }

  if (registrationQuery.isLoading) {
    return (
      <AuthShell title="Create your account" subtitle="Checking registration status…">
        <div className="flex justify-center py-10" role="status">
          <Spin />
        </div>
      </AuthShell>
    );
  }

  const info = registrationQuery.data;
  if (!info?.bootstrap) {
    return (
      <AuthShell
        title="Registration closed"
        subtitle="Accounts are created by administrators. Contact your admin for access."
      >
        <Alert type="info" showIcon message="Self-service registration is not available." className="mb-4" />
        <Link to="/login">
          <Button type="primary" block>
            Sign in
          </Button>
        </Link>
      </AuthShell>
    );
  }

  const onFinish = async (values: RegisterForm) => {
    setError(null);
    setSubmitting(true);
    try {
      await register(values.email.trim().toLowerCase(), values.password);
      navigate("/challenges", { replace: true });
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Registration failed");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthShell
      title="Set up administrator"
      subtitle="No users exist yet. This account becomes the platform administrator and can publish challenges."
    >
      <Alert
        type="info"
        showIcon
        className="mb-4"
        message="First-time setup"
        description="You are creating the initial admin account for this deployment."
      />
      {error && (
        <Alert type="error" message={error} showIcon className="mb-4" role="alert" />
      )}
      <PasswordRequirements className="mb-4" />
      <Form layout="vertical" onFinish={onFinish} requiredMark={false} size="large">
        <Form.Item
          label={<span className="text-muted-foreground">Email</span>}
          name="email"
          rules={[
            { required: true, message: "Email is required" },
            { type: "email", message: "Enter a valid email" },
          ]}
        >
          <Input autoComplete="email" placeholder="you@example.com" />
        </Form.Item>
        <Form.Item
          label={<span className="text-muted-foreground">Password</span>}
          name="password"
          rules={[
            { required: true, message: "Password is required" },
            { min: 8, message: "At least 8 characters" },
          ]}
        >
          <Input.Password autoComplete="new-password" placeholder="••••••••" />
        </Form.Item>
        <Form.Item
          label={<span className="text-muted-foreground">Confirm password</span>}
          name="confirm"
          dependencies={["password"]}
          rules={[
            { required: true, message: "Confirm your password" },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue("password") === value) {
                  return Promise.resolve();
                }
                return Promise.reject(new Error("Passwords do not match"));
              },
            }),
          ]}
        >
          <Input.Password autoComplete="new-password" placeholder="••••••••" />
        </Form.Item>
        <Button type="primary" htmlType="submit" block loading={submitting}>
          Create admin account
        </Button>
      </Form>
      <p className="mb-0 mt-6 text-center text-sm text-muted-foreground">
        Already have an account?{" "}
        <Link
          to="/login"
          className="font-medium text-emerald-600 hover:text-emerald-700 dark:text-emerald-400 dark:hover:text-emerald-300"
        >
          Sign in
        </Link>
      </p>
    </AuthShell>
  );
}
