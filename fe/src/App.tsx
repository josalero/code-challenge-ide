import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "./auth/ProtectedRoute";
import ChallengeWorkspacePage from "./pages/ChallengeWorkspacePage";
import ChallengesPage from "./pages/ChallengesPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/challenges" element={<ChallengesPage />} />
        <Route path="/challenges/:slug" element={<ChallengeWorkspacePage />} />
      </Route>
      <Route path="/" element={<Navigate to="/challenges" replace />} />
      <Route path="*" element={<Navigate to="/challenges" replace />} />
    </Routes>
  );
}
