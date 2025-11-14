"use client";

import React, { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { useRouter } from "next/navigation";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

interface User {
  id: number;
  username: string;
  email: string;
  roles: string[];
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, fullName: string, email: string, password: string, role: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    // Check for stored token on mount
    const storedToken = typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;
    const storedUser = typeof window !== "undefined" ? localStorage.getItem("user") : null;

    if (storedToken && storedUser) {
      try {
        setToken(storedToken);
        setUser(JSON.parse(storedUser));
      } catch (error) {
        console.error("Failed to parse stored user", error);
        localStorage.removeItem("accessToken");
        localStorage.removeItem("user");
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (username: string, password: string) => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({ username, password }),
      });

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(errorData || "Đăng nhập thất bại");
      }

      const data = await response.json();
      const userData: User = {
        id: data.id,
        username: data.username,
        email: data.email,
        roles: data.roles || [],
      };

      setToken(data.token);
      setUser(userData);

      if (typeof window !== "undefined") {
        localStorage.setItem("accessToken", data.token);
        localStorage.setItem("user", JSON.stringify(userData));
      }

      router.push("/");
    } catch (error) {
      throw error;
    }
  };

  const register = async (
    username: string,
    fullName: string,
    email: string,
    password: string,
    role: string
  ) => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/sign-up`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({ username, fullName, email, password, role }),
      });

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(errorData || "Đăng ký thất bại");
      }

      const data = await response.text();
      // After successful registration, redirect to login
      router.push("/auth/login");
    } catch (error) {
      throw error;
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    if (typeof window !== "undefined") {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("user");
    }
    router.push("/auth/login");
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}

