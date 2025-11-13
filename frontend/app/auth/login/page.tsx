import React from "react";
import Header from "../../components/Header";
import Container from "../../components/Container";

export default function LoginPage() {
  return (
    <div className="min-h-screen bg-page font-sans text-primary">
      <Header />
      
      {/* Main Content - Login Form */}
      <Container>
        <div className="flex items-center justify-center py-12">
          <div className="w-full max-w-lg rounded-lg bg-surface p-8 text-primary md:p-12">
          {/* Title */}
          <h1 className="mb-8 text-center text-4xl font-bold uppercase">
            ĐĂNG NHẬP
          </h1>

          <form>
            {/* Email Input */}
            <div className="mb-6">
              <label
                htmlFor="email"
                className="mb-2 block text-sm font-medium text-secondary"
              >
                Email
              </label>
              <input
                type="email"
                id="email"
                className="w-full rounded bg-base px-4 py-3 text-primary transition focus:bg-base focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]"
                placeholder="abc@gmail.com"
              />
            </div>

            {/* Password Input */}
            <div className="mb-6">
              <label
                htmlFor="password"
                className="mb-2 block text-sm font-medium text-secondary"
              >
                Mật Khẩu
              </label>
              <input
                type="password"
                id="password"
                className="w-full rounded bg-base px-4 py-3 text-primary transition focus:bg-base focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]"
                placeholder="••••••••"
              />
            </div>

            {/* Remember Me and Forgot Password */}
            <div className="mb-8 flex items-center justify-between">
              <label className="flex cursor-pointer items-center text-sm text-secondary">
                <input
                  type="checkbox"
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

            {/* Login Button */}
            <button
              type="submit"
              className="w-full rounded bg-primary px-4 py-3 font-bold uppercase text-on-primary transition duration-200 hover:bg-primary-hover focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)] focus:ring-offset-2 focus:ring-offset-[var(--color-page-background)]"
            >
              ĐĂNG NHẬP
            </button>

            {/* Registration Link */}
            <p className="mt-6 text-center text-sm text-secondary">
              Không có tài khoản?{" "}
              <a
                href="/auth/register"
                className="font-medium underline transition hover:text-[var(--color-primary)]"
              >
                Đăng ký Ngay.
              </a>
            </p>
          </form>
        </div>
      </div>
      </Container>
    </div>
  );
}
