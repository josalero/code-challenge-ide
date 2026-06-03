import { Alert, Button, Form, Input } from "antd";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { apiFetch, ApiError } from "../api/client";
import type { RegistrationInfoResponse } from "../api/types";
import { useAuth } from "../auth/useAuth";
import AuthShell from "../components/ui/AuthShell";
import { ApiPaths } from "../domain/constants";

type LoginForm = {
  email: string;
  password: string;
};

export default function LoginPage() {
  const { login, token } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const registrationQuery = useQuery({
    queryKey: ["registration-info"],
    queryFn: () => apiFetch<RegistrationInfoResponse>(ApiPaths.AUTH_REGISTRATION_INFO),
  });

  const from = (location.state as { from?: string } | null)?.from ?? "/challenges";
  const registrationOpen = registrationQuery.data?.registrationOpen ?? false;

  if (token) {
    return <Navigate to={from} replace />;
  }

  const onFinish = async (values: LoginForm) => {
    setError(null);
    setSubmitting(true);
    try {
      await login(values.email.trim().toLowerCase(), values.password);
      navigate(from, { replace: true });
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Sign in failed");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthShell
      title="Welcome back"
      subtitle="Sign in to continue your challenges and pick up where you left off."
    >
      {error && (
        <Alert type="error" message={error} showIcon className="mb-4" role="alert" />
      )}
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
          rules={[{ required: true, message: "Password is required" }]}
        >
          <Input.Password autoComplete="current-password" placeholder="••••••••" />
        </Form.Item>
        <Button type="primary" htmlType="submit" block loading={submitting}>
          Sign in
        </Button>
      </Form>
      {registrationOpen && (
        <p className="mb-0 mt-6 text-center text-sm text-muted-foreground">
          {registrationQuery.data?.bootstrap ? (
            <>
              First time here?{" "}
              <Link
                to="/register"
                className="font-medium text-emerald-600 hover:text-emerald-700 dark:text-emerald-400 dark:hover:text-emerald-300"
              >
                Set up admin account
              </Link>
            </>
          ) : (
            <>
              No account?{" "}
              <Link
                to="/register"
                className="font-medium text-emerald-600 hover:text-emerald-700 dark:text-emerald-400 dark:hover:text-emerald-300"
              >
                Create one
              </Link>
            </>
          )}
        </p>
      )}
    </AuthShell>
  );
}
