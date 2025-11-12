import React from "react";
import OverviewCard from "./OverviewCard";

const DEFAULT_OVERVIEW_ITEMS = [
  {
    title: "Doanh thu",
    cardClassName: "bg-blue-500/90 text-white",
    contentClassName: "bg-white/10",
    placeholderClassName: "text-white/80",
  },
  {
    title: "Tồn kho",
    cardClassName: "bg-yellow-400/90 text-zinc-900",
    contentClassName: "bg-white/70",
    placeholderClassName: "text-zinc-600",
  },
  {
    title: "Đơn hàng",
    cardClassName: "bg-orange-500/90 text-white",
    contentClassName: "bg-white/10",
    placeholderClassName: "text-white/80",
  },
  {
    title: "Lịch hẹn",
    cardClassName: "bg-emerald-400/90 text-white",
    contentClassName: "bg-white/10",
    placeholderClassName: "text-white/80",
  },
];

type OverviewItem = (typeof DEFAULT_OVERVIEW_ITEMS)[number];

interface OverviewSectionProps {
  title?: string;
  items?: OverviewItem[];
  className?: string;
}

export default function OverviewSection({
  title = "Tổng quan",
  items = DEFAULT_OVERVIEW_ITEMS,
  className = "",
}: OverviewSectionProps) {
  return (
    <section
      className={[
        "rounded-3xl border border-rose-200 bg-rose-50/80 p-8 shadow-sm backdrop-blur",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      <header className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <h2 className="text-xl font-semibold uppercase tracking-wide text-rose-500">
          {title}
        </h2>
      </header>

      <div className="mt-6 grid gap-6 md:grid-cols-2 xl:grid-cols-4">
        {items.map((item) => (
          <OverviewCard
            key={item.title}
            title={item.title}
            cardClassName={item.cardClassName}
            contentClassName={item.contentClassName}
            placeholderClassName={item.placeholderClassName}
          />
        ))}
      </div>
    </section>
  );
}

