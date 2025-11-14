"use client";

import HomePageContent from "./components/HomePageContent";
import ProtectedRoute from "./components/auth/ProtectedRoute";

export default function Home() {
  return (
    <ProtectedRoute>
      <div className="flex min-h-screen items-center justify-center bg-zinc-50 font-sans dark:bg-black">
        <HomePageContent />
      </div>
    </ProtectedRoute>
  );
}
