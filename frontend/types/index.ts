// Common types
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  roles: string[];
}

// Auth types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  password: string;
  email: string;
  fullName: string;
  role: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
}

// Product types
export interface Product {
  id: number;
  productName: string;
  version: string;
  msrp: number;
  imageUrl?: string;
  brandName: string;
  brandId: number;
  isActive: boolean;
  availableColors?: string[];
  availableQuantity?: number;
}

export interface ProductDetail extends Product {
  description?: string;
  videoUrl?: string;
  variants: ProductVariant[];
  features: ProductFeature[];
  technicalSpecs?: TechnicalSpecs;
}

export interface ProductVariant {
  color: string;
  availableQuantity: number;
  colorCode?: string;
}

export interface ProductFeature {
  featureName: string;
  description?: string;
  iconUrl?: string;
}

export interface TechnicalSpecs {
  batteryCapacity?: string;
  productRange?: string;
  power?: string;
  maxSpeed?: string;
  chargingTime?: string;
  dimensions?: string;
  weight?: string;
  seatingCapacity?: string;
}

// Customer types
export interface Customer {
  id: number;
  fullName: string;
  phoneNumber: string;
  email: string;
  address?: string;
  customerType?: string;
  createdAt: string;
  totalOrders?: number;
  totalSupportTickets?: number;
}

// Order types
export interface SalesOrder {
  id: number;
  orderDate: string;
  totalPrice: number;
  status: string;
  vehicleId?: string;
  vehicleModel?: string;
  vehicleBrand?: string;
  customerId: number;
  customerName: string;
  salesPersonId: number;
  salesPersonName: string;
  daysFromOrder?: number;
  isPaid?: boolean;
  paidAmount?: number;
  remainingAmount?: number;
}

// Inventory types
export interface Inventory {
  inventoryId: number;
  productId: number;
  productName: string;
  dealerId?: number;
  dealerName?: string;
  totalQuantity: number;
  availableQuantity: number;
  reservedQuantity: number;
  inTransitQuantity: number;
  stockPercentage?: number;
  isLowStock?: boolean;
}

// Dealer types
export interface Dealer {
  id: number;
  dealerName: string;
  address?: string;
  phoneNumber?: string;
  email?: string;
  dealerLevel?: string;
  brandId: number;
  brandName: string;
}

// Brand types
export interface Brand {
  id: number;
  brandName: string;
  headquartersAddress?: string;
  taxCode?: string;
  contactInfo?: string;
  totalDealers?: number;
  totalProducts?: number;
}

// Dashboard types
export interface DashboardSummary {
  currentDate: string;
  totalRevenue: number;
  totalOrders: number;
  growthRate: number;
  lowStockCount: number;
  outOfStockCount: number;
  totalPendingPayment: number;
  topProducts: Array<{
    productId: number;
    productName: string;
    unitsSold: number;
    revenue: number;
  }>;
  recentAlerts: Array<{
    productId: number;
    productName: string;
    alertType: string;
    message: string;
  }>;
}

// Vehicle types
export interface VehicleListResponse {
  id: string;
  vin: string;
  batterySerial?: string;
  color?: string;
  manufactureDate?: string;
  status: string;
  productId: number;
  productName: string;
  dealerId: number;
  dealerName?: string;
}

// Add more types as needed...

