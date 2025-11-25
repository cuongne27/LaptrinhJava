"use client";

import { useEffect, useCallback } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { X } from "lucide-react";

interface EntityModalProps {
  title: string;
  description?: string;
  open: boolean;
  onClose: () => void;
  children: React.ReactNode;
  footer?: React.ReactNode;
}

export function EntityModal({
  title,
  description,
  open,
  onClose,
  children,
  footer,
}: EntityModalProps) {
  const handleEscape = useCallback((event: KeyboardEvent) => {
    if (event.key === "Escape") {
      onClose();
    }
  }, [onClose]);

  useEffect(() => {
    if (open) {
      document.body.style.overflow = "hidden";
      document.addEventListener("keydown", handleEscape);
    } else {
      document.body.style.overflow = "unset";
      document.removeEventListener("keydown", handleEscape);
    }

    return () => {
      document.body.style.overflow = "unset";
      document.removeEventListener("keydown", handleEscape);
    };
  }, [open, handleEscape]);

  if (!open) return null;

  const handleOverlayClick = (e: React.MouseEvent<HTMLDivElement>) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 animate-fade-in"
      onClick={handleOverlayClick}
      style={{ margin: 0 }} // ✅ Override any parent margin
    >
      {/* ✅ Backdrop che TOÀN BỘ viewport */}
      <div 
        className="fixed inset-0 bg-black/60 dark:bg-black/80 backdrop-blur-sm" 
        style={{ margin: 0 }} // ✅ Ensure no margin
      />
      
      {/* Modal content */}
      <div 
        className="relative z-10 w-full max-w-2xl"
        onClick={(e) => e.stopPropagation()} // ✅ Prevent closing when clicking inside modal
      >
        <Card className="max-h-[90vh] overflow-hidden border border-border/60 bg-card shadow-2xl rounded-2xl animate-in zoom-in-95 duration-200">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-4 border-b border-border/60">
            <div>
              <CardTitle>{title}</CardTitle>
              {description && <CardDescription>{description}</CardDescription>}
            </div>
            <Button
              variant="ghost"
              size="icon"
              onClick={onClose}
              className="rounded-full hover:bg-primary/10 transition-colors"
            >
              <X className="h-4 w-4" />
            </Button>
          </CardHeader>
          <CardContent className="space-y-4 overflow-y-auto max-h-[calc(90vh-180px)] px-6 py-6">
            {children}
          </CardContent>
          {footer && (
            <div className="flex justify-end gap-2 px-6 pb-6 border-t pt-4 border-border/60">
              {footer}
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}