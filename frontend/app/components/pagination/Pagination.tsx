'use client';

import React from "react";

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange?: (page: number) => void;
}

export default function Pagination({
  currentPage,
  totalPages,
  onPageChange,
}: PaginationProps) {
  const pages = Array.from({ length: totalPages }, (_, index) => index + 1);

  return (
    <nav
      className="flex items-center justify-center gap-6 rounded-3xl border-soft bg-base px-6 py-4 text-sm font-semibold text-secondary shadow-inner"
      aria-label="Pagination"
    >
      <button
        type="button"
        onClick={() => onPageChange?.(Math.max(1, currentPage - 1))}
        disabled={currentPage === 1}
        className="text-muted transition hover:text-[var(--color-primary)] disabled:cursor-not-allowed disabled:text-muted"
      >
        Previous
      </button>
      {pages.map((page) => (
        <button
          key={page}
          type="button"
          onClick={() => onPageChange?.(page)}
          className={[
            "rounded-full px-3 py-1 transition",
            page === currentPage
              ? "bg-primary text-on-primary shadow"
              : "text-secondary hover:bg-surface",
          ].join(" ")}
        >
          {page}
        </button>
      ))}
      <button
        type="button"
        onClick={() => onPageChange?.(Math.min(totalPages, currentPage + 1))}
        disabled={currentPage === totalPages}
        className="text-muted transition hover:text-[var(--color-primary)] disabled:cursor-not-allowed disabled:text-muted"
      >
        Next
      </button>
    </nav>
  );
}

