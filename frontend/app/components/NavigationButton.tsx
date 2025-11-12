import Link from "next/link";
import React from "react";
import { MdOutlineWork } from "react-icons/md";

interface NavigationButtonProps {
  label: string;
  href: string;
  isActive?: boolean;
}

export default function NavigationButton({
  label,
  href,
  isActive = false,
}: NavigationButtonProps) {
  return (
    <Link
      href={href}
      className={[
        "group flex flex-col items-center justify-center gap-2 rounded-3xl border px-6 py-5 text-center transition-all",
        "border-rose-200 bg-[#f2f1f1] text-rose-700 hover:-translate-y-0.5 hover:border-rose-400 hover:bg-rose-200/70 hover:text-rose-800",
        isActive ? "ring-2 ring-blue-400 ring-offset-2 ring-offset-rose-50" : "",
      ]
        .filter(Boolean)
        .join(" ")}
    >
      <span
        className={[
          "flex h-12 w-12 items-center justify-center rounded-full border",
          "border-rose-300 bg-white/70 text-rose-600 transition-colors group-hover:border-rose-400 group-hover:text-rose-700",
        ].join(" ")}
      >
        <MdOutlineWork className="h-6 w-6" aria-hidden="true" />
      </span>
      <span className="text-sm font-semibold tracking-wide">{label}</span>
    </Link>
  );
}
