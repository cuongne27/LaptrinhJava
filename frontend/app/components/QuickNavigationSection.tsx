import React from "react";
import NavigationButton from "./NavigationButton";

interface QuickNavigationItem {
  name: string;
  href: string;
  isActive?: boolean;
}

interface QuickNavigationSectionProps {
  title?: string;
  description?: React.ReactNode;
  className?: string;
  items?: QuickNavigationItem[];
  children?: React.ReactNode;
}

export default function QuickNavigationSection({
  title = "Điều hướng nhanh",
  description,
  className = "",
  items,
  children,
}: QuickNavigationSectionProps) {
  const sectionClasses = [
    "rounded-3xl border border-rose-200 bg-white/80 p-8 shadow-inner",
    className,
  ]
    .filter(Boolean)
    .join(" ");

  const shouldRenderItems = Array.isArray(items) && items.length > 0;

  return (
    <section className={sectionClasses}>
      <h3 className="text-lg font-semibold uppercase tracking-wide text-rose-500">
        {title}
      </h3>
      {description ? (
        <p className="mt-2 text-sm text-rose-400">{description}</p>
      ) : null}
      <div className="mt-6 min-h-[160px] rounded-2xl border border-dashed border-rose-300 bg-rose-50/60 p-6">
        {shouldRenderItems ? (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-6">
            {items.map((item) => (
              <NavigationButton
                key={item.name}
                label={item.name}
                href={item.href}
                isActive={item.isActive}
              />
            ))}
          </div>
        ) : (
          <div className="flex h-full items-center justify-center text-rose-300">
            {children ?? "Các nút điều hướng sẽ được bổ sung sau"}
          </div>
        )}
      </div>
    </section>
  );
}

