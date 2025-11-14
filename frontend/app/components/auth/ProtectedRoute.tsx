"use client";

import React, { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useAuth } from "../../contexts/AuthContext";
import Link from "next/link";

interface ProtectedRouteProps {
  children: React.ReactNode;
}

// ============================================
// DISABLE PROTECTION FOR DEVELOPMENT/DEBUGGING
// ============================================
// Option 1: Set environment variable in .env.local:
//   NEXT_PUBLIC_DISABLE_AUTH_PROTECTION=true
//
// Option 2: Change the line below to:
//   const DISABLE_PROTECTION = true;
//
// Remember to set it back to false before committing!
// ============================================
// const DISABLE_PROTECTION = process.env.NEXT_PUBLIC_DISABLE_AUTH_PROTECTION === "false";
const DISABLE_PROTECTION = true;
export default function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { isAuthenticated, isLoading } = useAuth();
  const router = useRouter();
  const pathname = usePathname();
  const [shouldRedirect, setShouldRedirect] = useState(false);

  // Disable protection if flag is set
  if (DISABLE_PROTECTION) {
    return <>{children}</>;
  }

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      // Don't redirect if already on auth pages
      if (!pathname?.startsWith("/auth")) {
        // Delay redirect to show message first
        const timer = setTimeout(() => {
          setShouldRedirect(true);
          router.push("/auth/login");
        }, 2000); // 2 second delay to show message

        return () => clearTimeout(timer);
      }
    }
  }, [isAuthenticated, isLoading, router, pathname]);

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-page">
        <div className="text-center">
          <div className="mb-4 text-lg text-secondary">Đang tải...</div>
        </div>
      </div>
    );
  }

  if (!isAuthenticated && !shouldRedirect) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-page">
        <div className="text-center">
          <h1 className="mb-4 text-2xl font-bold text-primary">Bạn cần đăng nhập</h1>
          <p className="mb-6 text-secondary">
            Vui lòng đăng nhập để truy cập trang này.
          </p>
          <div className="flex flex-col items-center gap-4">
            <Link
              href="/auth/login"
              className="inline-block rounded-lg bg-primary px-6 py-3 font-semibold text-on-primary transition hover:bg-primary-hover"
            >
              Đi tới trang đăng nhập
            </Link>
            <p className="text-xs text-muted">
              Tự động chuyển hướng sau 2 giây...
            </p>
          </div>
        </div>
      </div>
    );
  }

  if (shouldRedirect) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-page">
        <div className="text-center">
          <div className="mb-4 text-lg text-secondary">Đang chuyển hướng...</div>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}

