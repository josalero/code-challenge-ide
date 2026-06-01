import { Navigate } from "react-router-dom";
import { useAuth } from "./useAuth";

type Props = {
  children: React.ReactNode;
};

export default function AdminRoute({ children }: Props) {
  const { user, loading } = useAuth();

  if (loading) {
    return null;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (user.role !== "ADMIN") {
    return <Navigate to="/challenges" replace />;
  }

  return children;
}
