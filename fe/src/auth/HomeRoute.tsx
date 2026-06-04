import { Spin } from "antd";
import { Navigate } from "react-router-dom";
import { useAuth } from "./useAuth";

export default function HomeRoute() {
  const { user, loading, token } = useAuth();

  if (loading) {
    return (
      <div className="ctl-app-bg flex min-h-screen items-center justify-center">
        <Spin size="large" />
      </div>
    );
  }

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (user?.role === "ADMIN") {
    return <Navigate to="/admin/dashboard" replace />;
  }

  return <Navigate to="/challenges" replace />;
}
