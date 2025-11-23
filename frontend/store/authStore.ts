import { create } from "zustand";
import type { User, AuthResponse } from "@/types";

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (authResponse: AuthResponse) => void;
  logout: () => void;
  setUser: (user: User) => void;
}

// Load from localStorage on init
const loadAuthFromStorage = () => {
  if (typeof window === "undefined") return { user: null, token: null };
  
  const token = localStorage.getItem("token");
  const userStr = localStorage.getItem("user");
  const user = userStr ? JSON.parse(userStr) : null;
  
  return { user, token, isAuthenticated: !!token && !!user };
};

const initialState = loadAuthFromStorage();

export const useAuthStore = create<AuthState>((set) => ({
  ...initialState,
  login: (authResponse: AuthResponse) => {
    const user: User = {
      id: authResponse.id,
      username: authResponse.username,
      email: authResponse.email,
      fullName: authResponse.username, // Adjust based on your response
      roles: authResponse.roles,
    };
    localStorage.setItem("token", authResponse.token);
    localStorage.setItem("user", JSON.stringify(user));
    set({
      user,
      token: authResponse.token,
      isAuthenticated: true,
    });
  },
  logout: () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    set({
      user: null,
      token: null,
      isAuthenticated: false,
    });
  },
  setUser: (user: User) => {
    localStorage.setItem("user", JSON.stringify(user));
    set({ user });
  },
}));

