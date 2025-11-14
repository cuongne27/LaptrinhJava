"use client";

import React, { useState, FormEvent } from "react";
import { useAuth } from "../../contexts/AuthContext";
import Link from "next/link";

export default function LoginForm() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      await login(username, password);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Đăng nhập thất bại");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="w-full max-w-lg rounded-lg bg-surface p-8 text-primary md:p-12">
      <h1 className="mb-8 text-center text-4xl font-bold uppercase">ĐĂNG NHẬP</h1>

      {error && (
        <div className="mb-6 rounded-lg border border-red-300 bg-red-50 p-4 text-sm text-red-800">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="mb-6">
          <label htmlFor="username" className="mb-2 block text-sm font-medium text-secondary">
            Tên đăng nhập
          </label>
          <input
            type="text"
            id="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            className="w-full rounded bg-base px-4 py-3 text-primary transition focus:bg-base focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]"
            placeholder="Nhập tên đăng nhập"
          />
        </div>

        <div className="mb-6">
          <label htmlFor="password" className="mb-2 block text-sm font-medium text-secondary">
            Mật Khẩu
          </label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            className="w-full rounded bg-base px-4 py-3 text-primary transition focus:bg-base focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]"
            placeholder="••••••••"
          />
        </div>

        <div className="mb-8 flex items-center justify-between">
          <label className="flex cursor-pointer items-center text-sm text-secondary">
            <input
              type="checkbox"
              checked={rememberMe}
              onChange={(e) => setRememberMe(e.target.checked)}
              className="mr-2 h-4 w-4 rounded border-soft text-[var(--color-primary)] focus:ring-[var(--color-primary)]"
            />
            Nhớ tôi
          </label>
          <a
            href="#"
            className="text-sm text-secondary underline transition hover:text-[var(--color-primary)]"
          >
            Quên mật khẩu?
          </a>
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="w-full rounded bg-primary px-4 py-3 font-bold uppercase text-on-primary transition duration-200 hover:bg-primary-hover focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)] focus:ring-offset-2 focus:ring-offset-[var(--color-page-background)] disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isLoading ? "Đang đăng nhập..." : "ĐĂNG NHẬP"}
        </button>

        <p className="mt-6 text-center text-sm text-secondary">
          Không có tài khoản?{" "}
          <Link
            href="/auth/register"
            className="font-medium underline transition hover:text-[var(--color-primary)]"
          >
            Đăng ký Ngay.
          </Link>
        </p>
      </form>
    </div>
  );
}

