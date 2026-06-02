import { Navigate, Route, Routes } from "react-router-dom";
import AdminRoute from "./auth/AdminRoute";
import ProtectedRoute from "./auth/ProtectedRoute";
import AdminOpsPage from "./pages/AdminOpsPage";
import ChallengeWorkspacePage from "./pages/ChallengeWorkspacePage";
import ChallengesPage from "./pages/ChallengesPage";
import CreateChallengePage from "./pages/CreateChallengePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/challenges" element={<ChallengesPage />} />
        <Route
          path="/challenges/new"
          element={
            <AdminRoute>
              <CreateChallengePage />
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
      <Route path="/" element={<Navigate to="/challenges" replace />} />
      <Route path="*" element={<Navigate to="/challenges" replace />} />
    </Routes>
  );
}
