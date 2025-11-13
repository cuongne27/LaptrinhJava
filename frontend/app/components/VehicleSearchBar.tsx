"use client";

import React, { ChangeEvent, FormEvent, useEffect, useState } from "react";
import SearchIcon from "@/public/search-icon.svg";
interface VehicleSearchBarProps {
  placeholder?: string;
  defaultValue?: string;
  onSearch: (value: string) => void;
  className?: string;
}

export default function VehicleSearchBar({
  placeholder = "Tìm kiếm...",
  defaultValue = "",
  onSearch,
  className = "",
}: VehicleSearchBarProps) {
  const [value, setValue] = useState(defaultValue);

  useEffect(() => {
    setValue(defaultValue);
  }, [defaultValue]);

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    const newValue = event.target.value;
    setValue(newValue);
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    onSearch(value.trim());
  };

  const handleBlur = () => {
    onSearch(value.trim());
  };

  return (
    <form
      onSubmit={handleSubmit}
      className={["relative flex w-full items-center", className]
        .filter(Boolean)
        .join(" ")}
    >
      <input
        type="search"
        value={value}
        onChange={handleChange}
        onBlur={handleBlur}
        placeholder={placeholder}
        className="placeholder-muted w-full rounded-full bg-search px-5 py-3 pl-12 text-sm font-medium text-[var(--color-text-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]"
      />
      <span className="pointer-events-none absolute left-4 text-lg text-[var(--color-text-muted)]">
        🔍
      </span>
    </form>
  );
}
