"use client";

import React, { useEffect, useId, useRef, useState } from "react";
import { FiFilter } from "react-icons/fi";

export interface FilterOption {
  value: string;
  label: string;
}

interface VehicleFilterButtonProps {
  options: FilterOption[];
  selectedValue?: string;
  onOptionSelect: (value: string) => void;
  icon?: React.ReactNode;
  label?: string;
  className?: string;
}

export default function VehicleFilterButton({
  options,
  selectedValue,
  onOptionSelect,
  icon,
  label = "Lọc",
  className = "",
}: VehicleFilterButtonProps) {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement | null>(null);
  const listboxId = useId();

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        containerRef.current &&
        event.target instanceof Node &&
        !containerRef.current.contains(event.target)
      ) {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [isOpen]);

  const handleSelect = (value: string) => {
    onOptionSelect(value);
    setIsOpen(false);
  };

  const selectedLabel =
    options.find((option) => option.value === selectedValue)?.label ?? label;

  return (
    <div
      ref={containerRef}
      className={["relative", className].filter(Boolean).join(" ")}
      title="Chọn cách sắp xếp xe">
      <button
        type="button"
        onClick={() => setIsOpen((open) => !open)}
        className="flex h-11 items-center gap-2 rounded-2xl border-soft bg-base px-4 py-2 text-sm font-semibold text-secondary shadow-md transition hover:border-[var(--color-primary)] hover:text-[var(--color-primary)]"
        aria-haspopup="listbox"
        aria-expanded={isOpen}
        aria-controls={listboxId}
      >
        {icon ?? <FiFilter className="h-5 w-5" />}
        <span>{selectedLabel}</span>
      </button>

      {isOpen ? (
        <ul
          id={listboxId}
          role="listbox"
          className="absolute right-0 z-20 mt-2 w-48 rounded-2xl border-soft bg-base py-2 shadow-xl"
        >
          {options.map((option) => (
            <li key={option.value}>
              <button
                type="button"
                role="option"
                aria-selected={option.value === selectedValue}
                onClick={() => handleSelect(option.value)}
                className={[
                  "flex w-full items-center justify-between px-4 py-2 text-sm text-secondary transition",
                  option.value === selectedValue
                    ? "bg-surface-strong text-[var(--color-primary)]"
                    : "hover:bg-surface",
                ]
                  .filter(Boolean)
                  .join(" ")}
              >
                <span>{option.label}</span>
                {option.value === selectedValue ? <span>✓</span> : null}
              </button>
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}
