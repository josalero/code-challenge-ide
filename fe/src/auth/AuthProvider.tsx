import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { apiFetch } from "../api/client";
import type { AuthResponse, MeResponse } from "../api/types";
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
      const me = await apiFetch<MeResponse>("/api/v1/me");
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
    setUser({ id: response.userId, email: response.email });
    setLoading(false);
  }, []);

  const login = useCallback(
    async (email: string, password: string) => {
      const response = await apiFetch<AuthResponse>("/api/v1/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
      });
      applyAuth(response);
    },
    [applyAuth],
  );

  const register = useCallback(
    async (email: string, password: string) => {
      const response = await apiFetch<AuthResponse>("/api/v1/auth/register", {
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

  const value = useMemo(
    () => ({ token, user, loading, login, register, logout }),
    [token, user, loading, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
