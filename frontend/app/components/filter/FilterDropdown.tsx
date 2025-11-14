"use client";

import React, { useEffect, useId, useRef, useState } from "react";
import { FiChevronDown } from "react-icons/fi";

export interface FilterOption {
  value: string;
  label: string;
}

interface FilterDropdownProps {
  options: FilterOption[];
  selectedValue?: string;
  onOptionSelect: (value: string) => void;
  placeholder?: string;
  label?: string;
  className?: string;
  showIcon?: boolean;
}

export default function FilterDropdown({
  options,
  selectedValue,
  onOptionSelect,
  placeholder = "Chọn...",
  label,
  className = "",
  showIcon = true,
}: FilterDropdownProps) {
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

  const selectedOption = options.find((option) => option.value === selectedValue);
  const displayText = selectedOption ? selectedOption.label : placeholder;

  return (
    <div
      ref={containerRef}
      className={["relative", className].filter(Boolean).join(" ")}
    >
      {label && (
        <label className="mb-1 block text-xs font-semibold text-secondary">
          {label}
        </label>
      )}
      <button
        type="button"
        onClick={() => setIsOpen((open) => !open)}
        className="flex h-11 w-full items-center justify-between gap-2 rounded-2xl border border-soft bg-base px-4 py-2 text-sm font-semibold text-secondary shadow-md transition hover:border-[var(--color-primary)] hover:text-[var(--color-primary)]"
        aria-haspopup="listbox"
        aria-expanded={isOpen}
        aria-controls={listboxId}
      >
        <span className={selectedOption ? "" : "text-muted"}>{displayText}</span>
        {showIcon && <FiChevronDown className="h-4 w-4" />}
      </button>

      {isOpen ? (
        <ul
          id={listboxId}
          role="listbox"
          className="absolute left-0 right-0 z-[100] mt-2 max-h-60 overflow-auto rounded-2xl border border-soft bg-base py-2 shadow-xl"
        >
          <li>
            <button
              type="button"
              role="option"
              aria-selected={!selectedValue}
              onClick={() => handleSelect("")}
              className={[
                "flex w-full items-center justify-between px-4 py-2 text-sm text-secondary transition",
                !selectedValue
                  ? "bg-surface-strong text-[var(--color-primary)]"
                  : "hover:bg-surface",
              ]
                .filter(Boolean)
                .join(" ")}
            >
              <span>Tất cả</span>
              {!selectedValue ? <span>✓</span> : null}
            </button>
          </li>
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

