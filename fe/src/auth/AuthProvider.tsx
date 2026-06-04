import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { apiFetch } from "../api/client";
import type { AuthResponse, ChangePasswordRequest, MeResponse } from "../api/types";
import { ApiPaths } from "../domain/constants";
import { clearAccessToken, getAccessToken, setAccessToken } from "./authStorage";
import { AuthContext, type AuthContextValue } from "./authContext";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => getAccessToken());
  const [user, setUser] = useState<AuthContextValue["user"]>(null);
  const [loading, setLoading] = useState(Boolean(getAccessToken()));

  const loadUser = useCallback(async () => {
    if (!getAccessToken()) {
      setUser(null);
      setLoading(false);
      return;
    }
    try {
      const me = await apiFetch<MeResponse>(ApiPaths.ME);
      setUser(me);
    } catch {
      clearAccessToken();
      setToken(null);
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadUser();
  }, [loadUser, token]);

  const applyAuth = useCallback((response: AuthResponse) => {
    setAccessToken(response.accessToken);
    setToken(response.accessToken);
    setUser({
      id: response.userId,
      email: response.email,
      fullName: null,
      role: response.role,
      mustChangePassword: response.mustChangePassword,
    });
    setLoading(false);
  }, []);

  const login = useCallback(
    async (email: string, password: string) => {
      const response = await apiFetch<AuthResponse>(ApiPaths.AUTH_LOGIN, {
        method: "POST",
        body: JSON.stringify({ email, password }),
      });
      applyAuth(response);
      return response.mustChangePassword;
    },
    [applyAuth],
  );

  const register = useCallback(
    async (email: string, password: string) => {
      const response = await apiFetch<AuthResponse>(ApiPaths.AUTH_REGISTER, {
        method: "POST",
        body: JSON.stringify({ email, password }),
      });
      applyAuth(response);
    },
    [applyAuth],
  );

  const changePassword = useCallback(
    async (currentPassword: string, newPassword: string) => {
      const body: ChangePasswordRequest = { currentPassword, newPassword };
      const response = await apiFetch<AuthResponse>(ApiPaths.ME_PASSWORD, {
        method: "POST",
        body: JSON.stringify(body),
      });
      applyAuth(response);
      const me = await apiFetch<MeResponse>(ApiPaths.ME);
      setUser(me);
    },
    [applyAuth],
  );

  const logout = useCallback(() => {
    clearAccessToken();
    setToken(null);
    setUser(null);
  }, []);

  const isAdmin = user?.role === "ADMIN";

  const value = useMemo(
    () => ({ token, user, loading, isAdmin, login, register, changePassword, logout }),
    [token, user, loading, isAdmin, login, register, changePassword, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
