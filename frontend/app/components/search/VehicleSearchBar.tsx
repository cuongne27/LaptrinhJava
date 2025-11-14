"use client";

import React, { ChangeEvent, FormEvent, useEffect, useState } from "react";
import Image from "next/image";
import searchIcon from "@/public/search-icon.svg";

interface VehicleSearchBarProps {
  placeholder?: string;
  defaultValue?: string;
  onSearch: (value: string) => void;
  className?: string;
  inputClassName?: string;
}

export default function VehicleSearchBar({
  placeholder = "Tìm kiếm...",
  defaultValue = "",
  onSearch,
  className = "",
  inputClassName = "",
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
        className={[
          "placeholder-muted w-full rounded-full bg-search px-5 py-3 pl-12 text-sm font-medium text-[var(--color-text-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]",
          inputClassName,
        ]
          .filter(Boolean)
          .join(" ")}
      />
      <Image
        src={searchIcon}
        alt="Search"
        width={20}
        height={20}
        className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 transform"
      />
    </form>
  );
}

