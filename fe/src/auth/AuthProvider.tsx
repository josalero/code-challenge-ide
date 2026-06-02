import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { apiFetch } from "../api/client";
import type { AuthResponse, MeResponse } from "../api/types";
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
    setUser({ id: response.userId, email: response.email, role: response.role });
    setLoading(false);
  }, []);

  const login = useCallback(
    async (email: string, password: string) => {
      const response = await apiFetch<AuthResponse>(ApiPaths.AUTH_LOGIN, {
        method: "POST",
        body: JSON.stringify({ email, password }),
      });
      applyAuth(response);
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

  const logout = useCallback(() => {
    clearAccessToken();
    setToken(null);
    setUser(null);
  }, []);

  const isAdmin = user?.role === "ADMIN";

  const value = useMemo(
    () => ({ token, user, loading, isAdmin, login, register, logout }),
    [token, user, loading, isAdmin, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
