import { Route, Routes } from "react-router-dom";
import AdminRoute from "./auth/AdminRoute";
import HomeRoute from "./auth/HomeRoute";
import ProtectedRoute from "./auth/ProtectedRoute";
import AdminUsersRoute from "./auth/AdminUsersRoute";
import AdminOpsPage from "./pages/AdminOpsPage";
import AdminAccessRequestsPage from "./pages/AdminAccessRequestsPage";
import AdminDashboardPage from "./pages/AdminDashboardPage";
import AdminUserChallengeDetailPage from "./pages/AdminUserChallengeDetailPage";
import AdminUserChallengeReportPage from "./pages/AdminUserChallengeReportPage";
import ChangePasswordPage from "./pages/ChangePasswordPage";
import ChallengeWorkspacePage from "./pages/ChallengeWorkspacePage";
import ChallengesPage from "./pages/ChallengesPage";
import MetricsPage from "./pages/MetricsPage";
import CreateChallengePage from "./pages/CreateChallengePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import RequestAccessPage from "./pages/RequestAccessPage";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/request-access" element={<RequestAccessPage />} />
      <Route path="/admin/users" element={<AdminUsersRoute />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/change-password" element={<ChangePasswordPage />} />
        <Route path="/challenges" element={<ChallengesPage />} />
        <Route path="/metrics" element={<MetricsPage />} />
        <Route
          path="/challenges/new"
          element={
            <AdminRoute>
              <CreateChallengePage />
            </AdminRoute>
          }
        />
        <Route
          path="/admin/dashboard"
          element={
            <AdminRoute>
              <AdminDashboardPage />
            </AdminRoute>
          }
        />
        <Route
          path="/admin/access-requests"
          element={
            <AdminRoute>
              <AdminAccessRequestsPage />
            </AdminRoute>
          }
        />
        <Route
          path="/admin/users/:userId/challenge-report/:challengeSlug"
          element={
            <AdminRoute>
              <AdminUserChallengeDetailPage />
            </AdminRoute>
          }
        />
        <Route
          path="/admin/users/:userId/challenge-report"
          element={
            <AdminRoute>
              <AdminUserChallengeReportPage />
            </AdminRoute>
          }
        />
        <Route
          path="/admin/ops"
          element={
            <AdminRoute>
              <AdminOpsPage />
            </AdminRoute>
          }
        />
        <Route path="/challenges/:slug" element={<ChallengeWorkspacePage />} />
      </Route>
      <Route path="/" element={<HomeRoute />} />
      <Route path="*" element={<HomeRoute />} />
    </Routes>
  );
}
