import { createContext } from "react";
import type { MeResponse } from "../api/types";

export type AuthContextValue = {
  token: string | null;
  user: MeResponse | null;
  loading: boolean;
  isAdmin: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string) => Promise<void>;
  logout: () => void;
};

export const AuthContext = createContext<AuthContextValue | null>(null);
