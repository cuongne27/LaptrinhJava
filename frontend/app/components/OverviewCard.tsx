import React from "react";

interface OverviewCardProps {
  title: string;
  cardClassName?: string;
  contentClassName?: string;
  placeholderText?: string;
  placeholderClassName?: string;
  children?: React.ReactNode;
}

export default function OverviewCard({
  title,
  cardClassName = "",
  contentClassName = "",
  placeholderText = "Chưa có dữ liệu",
  placeholderClassName = "",
  children,
}: OverviewCardProps) {
  const cardClasses = ["rounded-2xl", "p-6", "shadow", cardClassName]
    .filter(Boolean)
    .join(" ");
  const contentClasses = ["mt-4", "h-24", "rounded-xl", "p-4", contentClassName]
    .filter(Boolean)
    .join(" ");
  const placeholderClasses = ["text-sm", placeholderClassName]
    .filter(Boolean)
    .join(" ");

  return (
    <div className={cardClasses}>
      <h3 className="text-lg font-semibold uppercase tracking-wide">{title}</h3>
      <div className={contentClasses}>
        {children ?? <span className={placeholderClasses}>{placeholderText}</span>}
      </div>
    </div>
  );
}

