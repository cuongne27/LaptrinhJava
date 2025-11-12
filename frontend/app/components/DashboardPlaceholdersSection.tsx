import React from "react";

interface DashboardPlaceholdersSectionProps {
  panels?: number;
  className?: string;
  placeholderClassName?: string;
}

export default function DashboardPlaceholdersSection({
  panels = 2,
  className = "",
  placeholderClassName = "",
}: DashboardPlaceholdersSectionProps) {
  const sectionClasses = ["grid gap-6 lg:grid-cols-2", className]
    .filter(Boolean)
    .join(" ");
  const panelClasses = [
    "min-h-[260px]",
    "rounded-3xl",
    "border",
    "border-rose-200",
    "bg-white/60",
    "shadow-inner",
    placeholderClassName,
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <section className={sectionClasses}>
      {Array.from({ length: panels }).map((_, index) => (
        <div key={index} className={panelClasses} />
      ))}
    </section>
  );
}

