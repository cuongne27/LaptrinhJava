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
  // Use useCallback to memoize the function for event listener stability
  const handleEscape = useCallback((event: KeyboardEvent) => {
    if (event.key === "Escape") {
      onClose();
    }
  }, [onClose]);

  useEffect(() => {
    if (open) {
      // 1. Manage Body Scroll: Disable scroll when open
      document.body.style.overflow = "hidden";

      // 2. Escape Key Handling: Add event listener for 'Escape' key press
      document.addEventListener("keydown", handleEscape);
    } else {
      // Restore scroll when closed
      document.body.style.overflow = "unset";
      document.removeEventListener("keydown", handleEscape);
    }

    // 3. Cleanup: Ensure the scroll is restored and listener is removed on unmount or when 'open' changes
    return () => {
      document.body.style.overflow = "unset";
      document.removeEventListener("keydown", handleEscape);
    };
  }, [open, handleEscape]); // Dependency array includes 'open' and the memoized 'handleEscape'

  if (!open) return null;

  // Function to close the modal when clicking on the overlay backdrop
  const handleOverlayClick = (e: React.MouseEvent<HTMLDivElement>) => {
    // Only close if the click occurred directly on the backdrop div (the first child div)
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    // Add onClick handler to the main fixed container for overlay closing
    <div
      className="fixed inset-0 z-50 flex items-center justify-center animate-fade-in"
      onClick={handleOverlayClick}
    >
      {/* Backdrop removed as the main container handles the click and styling */}
      <div className="absolute inset-0 bg-[rgba(12,16,24,0.65)] dark:bg-[rgba(4,6,9,0.78)] backdrop-blur-sm" />
      
      {/* Modal content container - this is where the click should NOT close the modal */}
      <div className="relative z-10 w-full max-w-2xl px-4 sm:px-6">
        <Card className="max-h-[90vh] overflow-hidden border border-border/60 bg-card/95 dark:bg-card/80 shadow-2xl rounded-2xl modal-pop">
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
          <CardContent className="space-y-4 overflow-y-auto max-h-[70vh] px-6 py-6">
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