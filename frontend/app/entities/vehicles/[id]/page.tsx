"use client";

import React from "react";
import VehicleDetailContent from "../../../components/vehicle/VehicleDetailContent";
import ProtectedRoute from "../../../components/auth/ProtectedRoute";

export default function VehicleDetailPage() {
  return (
    <ProtectedRoute>
      <VehicleDetailContent />
    </ProtectedRoute>
  );
}

