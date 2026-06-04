import { Spin } from "antd";
import { useQuery } from "@tanstack/react-query";
import { Navigate, useLocation } from "react-router-dom";
import { apiFetch } from "../api/client";
import type { RegistrationInfoResponse } from "../api/types";
import { ApiPaths } from "../domain/constants";
import AdminBootstrapSetupPage from "../pages/AdminBootstrapSetupPage";
import AdminCreateUserPage from "../pages/AdminCreateUserPage";
import { useAuth } from "./useAuth";

export default function AdminUsersRoute() {
  const location = useLocation();
  const { token, user, loading: authLoading } = useAuth();

  const registrationQuery = useQuery({
    queryKey: ["registration-info"],
    queryFn: () => apiFetch<RegistrationInfoResponse>(ApiPaths.AUTH_REGISTRATION_INFO),
  });

  if (registrationQuery.isLoading || authLoading) {
    return (
      <div className="ctl-app-bg flex min-h-screen items-center justify-center">
        <Spin size="large" />
      </div>
    );
  }

  if (registrationQuery.data?.bootstrap) {
    return <AdminBootstrapSetupPage />;
  }

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (user?.mustChangePassword) {
    return <Navigate to="/change-password" replace />;
  }

  if (user?.role !== "ADMIN") {
    return <Navigate to="/challenges" replace />;
  }

  return <AdminCreateUserPage />;
}
