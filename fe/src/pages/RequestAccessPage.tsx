import { Alert, Button, Form, Input, Spin } from "antd";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { apiFetch, ApiError } from "../api/client";
import type { AccessRequestResponse, RegistrationInfoResponse } from "../api/types";
import { useAuth } from "../auth/useAuth";
import AuthShell from "../components/ui/AuthShell";
import { ApiPaths } from "../domain/constants";

type RequestAccessForm = {
  fullName: string;
  email: string;
  message?: string;
};

export default function RequestAccessPage() {
  const { token } = useAuth();
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<AccessRequestResponse | null>(null);
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
      <AuthShell title="Request access" subtitle="Checking availability…">
        <div className="flex justify-center py-10" role="status">
          <Spin />
        </div>
      </AuthShell>
    );
  }

  const accessRequestsEnabled = registrationQuery.data?.accessRequestsEnabled ?? false;
  const accessRequestsConfigured = registrationQuery.data?.accessRequestsConfigured ?? false;

  if (!accessRequestsEnabled) {
    return (
      <AuthShell
        title="Request access unavailable"
        subtitle="This instance is not accepting new access requests right now."
      >
        <Alert
          type="info"
          showIcon
          className="mb-4"
          message="Need an account?"
          description="Contact the site administrator if you would like to try Code Training Lab."
        />
        <Link to="/login">
          <Button type="primary" block>
            Back to sign in
          </Button>
        </Link>
      </AuthShell>
    );
  }

  const onFinish = async (values: RequestAccessForm) => {
    setError(null);
    setSubmitting(true);
    try {
      const response = await apiFetch<AccessRequestResponse>(ApiPaths.AUTH_ACCESS_REQUEST, {
        method: "POST",
        body: JSON.stringify({
          email: values.email.trim().toLowerCase(),
          fullName: values.fullName.trim(),
          message: values.message?.trim() || undefined,
        }),
      });
      setSuccess(response);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Could not submit your request");
    } finally {
      setSubmitting(false);
    }
  };

  if (success) {
    return (
      <AuthShell
        title="Request received"
        subtitle="Thank you — an administrator will review your request."
      >
        <Alert type="success" showIcon message={success.message} className="mb-4" />
        <p className="mb-4 text-sm text-muted-foreground">
          If approved, you will receive an email with sign-in instructions. Until then, you can
          close this page and check back later.
        </p>
        <Link to="/login">
          <Button type="primary" block>
            Back to sign in
          </Button>
        </Link>
      </AuthShell>
    );
  }

  return (
    <AuthShell
      title="Request access"
      subtitle="Tell us a little about yourself. An administrator will review your request and email you if approved."
    >
      {!accessRequestsConfigured && (
        <Alert
          type="warning"
          showIcon
          className="mb-4"
          message="Requests may not be delivered yet"
          description="The site owner still needs to finish email setup. You can submit the form, but contact the administrator directly if you do not hear back."
        />
      )}
      {error && (
        <Alert type="error" message={error} showIcon className="mb-4" role="alert" />
      )}
      <Form layout="vertical" onFinish={onFinish} requiredMark={false} size="large">
        <Form.Item
          label={<span className="text-muted-foreground">Full name</span>}
          name="fullName"
          rules={[
            { required: true, message: "Full name is required" },
            { max: 200, message: "Name is too long" },
          ]}
        >
          <Input autoComplete="name" placeholder="Jane Learner" />
        </Form.Item>
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
          label={<span className="text-muted-foreground">Why do you want access? (optional)</span>}
          name="message"
          rules={[{ max: 2000, message: "Message is too long" }]}
        >
          <Input.TextArea
            rows={4}
            placeholder="Briefly describe how you plan to use the training lab."
          />
        </Form.Item>
        <Button type="primary" htmlType="submit" block loading={submitting}>
          Send request
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
