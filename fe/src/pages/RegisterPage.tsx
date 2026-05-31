import { Alert, Button, Form, Input } from "antd";
import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { ApiError } from "../api/client";
import { useAuth } from "../auth/useAuth";
import AuthShell from "../components/ui/AuthShell";

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

  if (token) {
    return <Navigate to="/challenges" replace />;
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
      title="Create your account"
      subtitle="Password must be at least 8 characters and different from your email."
    >
      {error && (
        <Alert type="error" message={error} showIcon className="mb-4" role="alert" />
      )}
      <Form layout="vertical" onFinish={onFinish} requiredMark={false} size="large">
        <Form.Item
          label={<span className="text-slate-300">Email</span>}
          name="email"
          rules={[
            { required: true, message: "Email is required" },
            { type: "email", message: "Enter a valid email" },
          ]}
        >
          <Input autoComplete="email" placeholder="you@example.com" />
        </Form.Item>
        <Form.Item
          label={<span className="text-slate-300">Password</span>}
          name="password"
          rules={[
            { required: true, message: "Password is required" },
            { min: 8, message: "At least 8 characters" },
          ]}
        >
          <Input.Password autoComplete="new-password" placeholder="••••••••" />
        </Form.Item>
        <Form.Item
          label={<span className="text-slate-300">Confirm password</span>}
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
          Create account
        </Button>
      </Form>
      <p className="mb-0 mt-6 text-center text-sm text-slate-400">
        Already have an account?{" "}
        <Link to="/login" className="text-emerald-400 hover:text-emerald-300">
          Sign in
        </Link>
      </p>
    </AuthShell>
  );
}
