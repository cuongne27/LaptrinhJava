"use client";

import React, { useState, FormEvent } from "react";
import { useAuth } from "../../contexts/AuthContext";
import Link from "next/link";

export default function RegisterForm() {
  const [username, setUsername] = useState("");
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [role, setRole] = useState("DEALER_STAFF");
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const { register } = useAuth();

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);

    if (password !== confirmPassword) {
      setError("Mật khẩu xác nhận không khớp");
      return;
    }

    setIsLoading(true);

    try {
      await register(username, fullName, email, password, role);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Đăng ký thất bại");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="w-full max-w-lg rounded-lg bg-surface p-8 text-primary md:p-12">
      <h1 className="mb-8 text-center text-4xl font-bold uppercase">ĐĂNG KÝ</h1>

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
            minLength={3}
            maxLength={20}
            pattern="^[a-zA-Z0-9._-]{3,20}$"
            className="w-full rounded bg-base px-4 py-3 text-primary transition focus:bg-base focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]"
            placeholder="Nhập tên đăng nhập"
          />
          <p className="mt-1 text-xs text-muted">
            Chỉ được chứa chữ cái, số, dấu chấm, gạch dưới và gạch ngang (3-20 ký tự)
          </p>
        </div>

        <div className="mb-6">
          <label htmlFor="fullName" className="mb-2 block text-sm font-medium text-secondary">
            Họ và tên
          </label>
          <input
            type="text"
            id="fullName"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            required
            minLength={3}
            maxLength={20}
            className="w-full rounded bg-base px-4 py-3 text-primary transition focus:bg-base focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]"
            placeholder="Nhập họ và tên"
          />
        </div>

        <div className="mb-6">
          <label htmlFor="email" className="mb-2 block text-sm font-medium text-secondary">
            Email
          </label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            maxLength={50}
            className="w-full rounded bg-base px-4 py-3 text-primary transition focus:bg-base focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]"
            placeholder="abc@gmail.com"
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
            minLength={6}
            maxLength={40}
            className="w-full rounded bg-base px-4 py-3 text-primary transition focus:bg-base focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]"
            placeholder="••••••••"
          />
          <p className="mt-1 text-xs text-muted">
            Phải chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt (6-40 ký tự)
          </p>
        </div>

        <div className="mb-6">
          <label
            htmlFor="confirmPassword"
            className="mb-2 block text-sm font-medium text-secondary"
          >
            Xác nhận Mật Khẩu
          </label>
          <input
            type="password"
            id="confirmPassword"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
            className="w-full rounded bg-base px-4 py-3 text-primary transition focus:bg-base focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]"
            placeholder="••••••••"
          />
        </div>

        <div className="mb-6">
          <label htmlFor="role" className="mb-2 block text-sm font-medium text-secondary">
            Vai trò
          </label>
          <select
            id="role"
            value={role}
            onChange={(e) => setRole(e.target.value)}
            required
            className="w-full rounded bg-base px-4 py-3 text-primary transition focus:bg-base focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]"
          >
            <option value="DEALER_STAFF">DEALER_STAFF</option>
            <option value="BRAND_MANAGER">BRAND_MANAGER</option>
            <option value="ADMIN">ADMIN</option>
          </select>
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="w-full rounded bg-primary px-4 py-3 font-bold uppercase text-on-primary transition duration-200 hover:bg-primary-hover focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)] focus:ring-offset-2 focus:ring-offset-[var(--color-page-background)] disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isLoading ? "Đang đăng ký..." : "ĐĂNG KÝ"}
        </button>

        <p className="mt-6 text-center text-sm text-secondary">
          Đã có tài khoản?{" "}
          <Link
            href="/auth/login"
            className="font-medium underline transition hover:text-[var(--color-primary)]"
          >
            Đăng nhập ngay.
          </Link>
        </p>
      </form>
    </div>
  );
}

