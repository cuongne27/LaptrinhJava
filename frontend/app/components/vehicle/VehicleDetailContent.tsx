"use client";

import React, { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Header from "../layout/Header";
import Container from "../layout/Container";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
const DEFAULT_IMAGE_PLACEHOLDER = "https://via.placeholder.com/600x400?text=No+Image";

interface VehicleDetailResponse {
  id: string;
  vin?: string;
  batterySerial?: string;
  color?: string;
  manufactureDate?: string;
  status?: string;
  productId?: number;
  productName?: string;
  productVersion?: string;
  productImageUrl?: string;
  dealerId?: number;
  dealerName?: string;
  dealerAddress?: string;
  salesOrderId?: number;
  customerName?: string;
  soldDate?: string;
  totalSupportTickets?: number;
  openTickets?: number;
  closedTickets?: number;
}

interface TechnicalSpecsResponse {
  batteryCapacity?: string;
  productRange?: string;
  power?: string;
  maxSpeed?: string;
  chargingTime?: string;
  dimensions?: string;
  weight?: string;
  seatingCapacity?: string;
}

interface ProductDetailResponse {
  id: number;
  productName?: string;
  version?: string;
  msrp?: number;
  imageUrl?: string;
  technicalSpecs?: TechnicalSpecsResponse;
}

export default function VehicleDetailContent() {
  const params = useParams();
  const router = useRouter();
  const vehicleId = params?.id as string;

  const [vehicle, setVehicle] = useState<VehicleDetailResponse | null>(null);
  const [product, setProduct] = useState<ProductDetailResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!vehicleId) return;

    const fetchVehicleDetail = async () => {
      setIsLoading(true);
      setError(null);

      try {
        const token =
          typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;

        // Fetch vehicle details
        const vehicleResponse = await fetch(`${API_BASE_URL}/api/vehicles/${vehicleId}`, {
          headers: {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
          credentials: "include",
        });

        if (!vehicleResponse.ok) {
          if (vehicleResponse.status === 401) {
            throw new Error("Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn.");
          }
          if (vehicleResponse.status === 404) {
            throw new Error("Không tìm thấy thông tin xe.");
          }
          throw new Error("Không thể tải thông tin xe.");
        }

        const vehicleData: VehicleDetailResponse = await vehicleResponse.json();
        setVehicle(vehicleData);

        // Try to fetch product details if productId exists
        // Note: This might require different endpoint based on user role
        if (vehicleData.productId) {
          try {
            // Try dealer endpoint first
            const productResponse = await fetch(
              `${API_BASE_URL}/api/dealer/products/${vehicleData.productId}`,
              {
                headers: {
                  "Content-Type": "application/json",
                  ...(token ? { Authorization: `Bearer ${token}` } : {}),
                },
                credentials: "include",
              }
            );

            if (productResponse.ok) {
              const productData: ProductDetailResponse = await productResponse.json();
              setProduct(productData);
            }
          } catch (err) {
            console.error("Failed to fetch product details", err);
            // Continue without product details - we'll use vehicle data only
          }
        }
      } catch (err) {
        console.error("Failed to fetch vehicle detail", err);
        setError(err instanceof Error ? err.message : "Đã xảy ra lỗi.");
      } finally {
        setIsLoading(false);
      }
    };

    fetchVehicleDetail();
  }, [vehicleId]);

  if (isLoading) {
    return (
      <div className="flex min-h-screen w-full flex-col bg-page">
        <Header />
        <main className="flex-1 py-10">
          <Container>
            <div className="flex items-center justify-center py-20">
              <div className="text-center">
                <div className="mb-4 text-lg text-secondary">Đang tải thông tin xe...</div>
              </div>
            </div>
          </Container>
        </main>
      </div>
    );
  }

  if (error || !vehicle) {
    return (
      <div className="flex min-h-screen w-full flex-col bg-page">
        <Header />
        <main className="flex-1 py-10">
          <Container>
            <div className="flex items-center justify-center py-20">
              <div className="text-center">
                <div className="mb-4 text-lg text-error">{error || "Không tìm thấy thông tin xe"}</div>
                <button
                  onClick={() => router.back()}
                  className="rounded-lg bg-primary px-6 py-2 text-on-primary transition hover:bg-primary-hover"
                >
                  Quay lại
                </button>
              </div>
            </div>
          </Container>
        </main>
      </div>
    );
  }

  const specs = product?.technicalSpecs;
  const modelName = `${vehicle.productName || ""} ${vehicle.productVersion || ""}`.trim();
  const price = product?.msrp ? new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    minimumFractionDigits: 0,
  }).format(product.msrp) : null;

  // Parse dimensions if available
  const parseDimensions = (dimensions?: string) => {
    if (!dimensions) return null;
    // Try to parse format like "4750x1934x1667 mm" or "185.8\" x 76.1\" x 56.7\""
    const parts = dimensions.split(/[x×]/).map((p) => p.trim());
    if (parts.length >= 3) {
      return {
        length: parts[0],
        width: parts[1],
        height: parts[2],
      };
    }
    return null;
  };

  const dims = parseDimensions(specs?.dimensions);

  return (
    <div className="flex min-h-screen w-full flex-col bg-page">
      <Header />
      <main className="flex-1 py-10">
        <Container>
          <div className="mb-6">
            <button
              onClick={() => router.back()}
              className="text-secondary transition hover:text-primary"
            >
              ← Quay lại
            </button>
          </div>

          <div className="rounded-3xl bg-white p-8 shadow-lg">
            {/* Header */}
            <div className="mb-8 flex items-start justify-between">
              <div>
                <h1 className="mb-2 text-4xl font-bold uppercase text-primary">
                  {modelName || "TESLO MODEL"}
                </h1>
                <div className="text-sm text-secondary">
                  VIN: {vehicle.vin || "N/A"} | Serial: {vehicle.batterySerial || "N/A"}
                </div>
              </div>
              {price && (
                <div className="flex items-center gap-2 rounded-lg bg-primary px-6 py-3 text-on-primary">
                  <span className="text-xl font-bold">{price}</span>
                  <span className="text-sm">USD</span>
                </div>
              )}
            </div>

            <div className="grid grid-cols-1 gap-8 lg:grid-cols-3">
              {/* Left Column - Specifications */}
              <div className="lg:col-span-2 space-y-8">
                {/* DIMENSIONS */}
                <section>
                  <h2 className="mb-4 text-2xl font-bold uppercase text-primary">DIMENSIONS</h2>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    {specs?.weight && (
                      <div>
                        <div className="font-semibold text-secondary">WEIGHT (CURB MASS)</div>
                        <div className="text-primary">{specs.weight}</div>
                      </div>
                    )}
                    {specs?.seatingCapacity && (
                      <div>
                        <div className="font-semibold text-secondary">SEATING</div>
                        <div className="text-primary">{specs.seatingCapacity}</div>
                      </div>
                    )}
                    {dims && (
                      <>
                        <div>
                          <div className="font-semibold text-secondary">OVERALL WIDTH</div>
                          <div className="text-primary">FOLDED MIRRORS: {dims.width}</div>
                          <div className="text-primary">EXTENDED MIRRORS: {dims.width}</div>
                        </div>
                        <div>
                          <div className="font-semibold text-secondary">CARGO</div>
                          <div className="text-primary">24 CU FT</div>
                        </div>
                        <div>
                          <div className="font-semibold text-secondary">DISPLAYS</div>
                          <div className="text-primary">15.4" CENTER TOUCHSCREEN</div>
                        </div>
                        <div>
                          <div className="font-semibold text-secondary">OVERALL HEIGHT</div>
                          <div className="text-primary">{dims.height}</div>
                        </div>
                        <div>
                          <div className="font-semibold text-secondary">WHEELS</div>
                          <div className="text-primary">18" OR 19"</div>
                        </div>
                        <div>
                          <div className="font-semibold text-secondary">GROUND CLEARANCE</div>
                          <div className="text-primary">5.4"</div>
                        </div>
                        <div>
                          <div className="font-semibold text-secondary">OVERALL LENGTH</div>
                          <div className="text-primary">{dims.length}</div>
                        </div>
                      </>
                    )}
                  </div>
                </section>

                {/* PERFORMANCE AND RANGE */}
                <section>
                  <h2 className="mb-4 text-2xl font-bold uppercase text-primary">
                    PERFORMANCE AND RANGE
                  </h2>
                  <div className="space-y-3 text-sm">
                    {specs?.productRange && (
                      <div>
                        <div className="font-semibold text-secondary">Range:</div>
                        <div className="text-primary">
                          {specs.productRange} (WLTP) For Rear-Wheel Drive And Long Range Models.
                        </div>
                      </div>
                    )}
                    {specs?.power && (
                      <div>
                        <div className="font-semibold text-secondary">0-60 Mph:</div>
                        <div className="text-primary">
                          From 5.6 Seconds For Rear-Wheel Drive To As Fast As 3.3 Seconds For The
                          Performance Model.
                        </div>
                      </div>
                    )}
                    <div>
                      <div className="font-semibold text-secondary">Drivetrain:</div>
                      <div className="text-primary">
                        Dual Motor All-Wheel Drive Is Standard On Long Range And Performance Models,
                        While A Rear-Wheel Drive Option Is Also Available.
                      </div>
                    </div>
                  </div>
                </section>

                {/* CHARGING */}
                <section>
                  <h2 className="mb-4 text-2xl font-bold uppercase text-primary">CHARGING</h2>
                  <div className="space-y-3 text-sm">
                    {specs?.chargingTime && (
                      <div>
                        <div className="font-semibold text-secondary">Max Charge Speed:</div>
                        <div className="text-primary">Up To 250 KW DC Charging.</div>
                      </div>
                    )}
                    {specs?.chargingTime && (
                      <div>
                        <div className="font-semibold text-secondary">DC Fast Charging Time:</div>
                        <div className="text-primary">
                          Approximately 27 Minutes To Go From 0-80% With A 250 KW Charger.
                        </div>
                      </div>
                    )}
                    {specs?.chargingTime && (
                      <div>
                        <div className="font-semibold text-secondary">AC Charging Time:</div>
                        <div className="text-primary">
                          Up To 8 Hours And 15 Minutes For A Full Charge On An 11 KW AC Charger.
                        </div>
                      </div>
                    )}
                  </div>
                </section>

                {/* Vehicle Information */}
                <section>
                  <h2 className="mb-4 text-2xl font-bold uppercase text-primary">
                    VEHICLE INFORMATION
                  </h2>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <div className="font-semibold text-secondary">Color</div>
                      <div className="text-primary">{vehicle.color || "N/A"}</div>
                    </div>
                    <div>
                      <div className="font-semibold text-secondary">Status</div>
                      <div className="text-primary">{vehicle.status || "N/A"}</div>
                    </div>
                    {vehicle.manufactureDate && (
                      <div>
                        <div className="font-semibold text-secondary">Manufacture Date</div>
                        <div className="text-primary">
                          {new Date(vehicle.manufactureDate).toLocaleDateString("vi-VN")}
                        </div>
                      </div>
                    )}
                    {vehicle.dealerName && (
                      <div>
                        <div className="font-semibold text-secondary">Dealer</div>
                        <div className="text-primary">{vehicle.dealerName}</div>
                      </div>
                    )}
                  </div>
                </section>
              </div>

              {/* Right Column - Image and Colors */}
              <div className="space-y-6">
                {/* Car Image */}
                <div className="relative">
                  <div className="aspect-video w-full overflow-hidden rounded-lg bg-gray-100">
                    {vehicle.productImageUrl || product?.imageUrl ? (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img
                        src={vehicle.productImageUrl || product?.imageUrl || DEFAULT_IMAGE_PLACEHOLDER}
                        alt={modelName}
                        className="h-full w-full object-contain"
                      />
                    ) : (
                      <div className="flex h-full items-center justify-center text-gray-400">
                        No Image
                      </div>
                    )}
                  </div>
                </div>

                {/* Color Options */}
                <div>
                  <h3 className="mb-3 text-lg font-semibold text-primary">COLOR OPTIONS</h3>
                  <div className="flex flex-wrap gap-3">
                    {vehicle.color && (
                      <div className="flex flex-col items-center gap-2">
                        <div
                          className="h-12 w-12 rounded-full border-2 border-gray-300"
                          style={{
                            backgroundColor:
                              vehicle.color.toLowerCase() === "black"
                                ? "#000"
                                : vehicle.color.toLowerCase() === "white"
                                ? "#fff"
                                : vehicle.color.toLowerCase() === "blue" ||
                                  vehicle.color.toLowerCase() === "xanh"
                                ? "#3b82f6"
                                : "#9ca3af",
                          }}
                        />
                        <span className="text-xs text-secondary">{vehicle.color}</span>
                      </div>
                    )}
                  </div>
                </div>

                {/* Additional Info */}
                {vehicle.totalSupportTickets !== undefined && (
                  <div className="rounded-lg border border-gray-200 p-4">
                    <h3 className="mb-2 text-sm font-semibold text-primary">SUPPORT TICKETS</h3>
                    <div className="space-y-1 text-xs text-secondary">
                      <div>Total: {vehicle.totalSupportTickets}</div>
                      <div>Open: {vehicle.openTickets || 0}</div>
                      <div>Closed: {vehicle.closedTickets || 0}</div>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </Container>
      </main>
    </div>
  );
}

