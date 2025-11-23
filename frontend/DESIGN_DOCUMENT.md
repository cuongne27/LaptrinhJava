# EVM Frontend - Design Document

## Tổng quan

Hệ thống frontend EVM được thiết kế với giao diện hiện đại, responsive và dễ sử dụng. Sử dụng Next.js 14 với App Router, TypeScript, Tailwind CSS và các component từ shadcn/ui.

## Cấu trúc trang

### 1. Authentication Pages (2 trang)
- **Login** (`/login`): Trang đăng nhập với form username/password
- **Signup** (`/signup`): Trang đăng ký (nếu cần)

### 2. Dashboard (1 trang)
- **Dashboard** (`/dashboard`): 
  - Thống kê tổng quan (doanh thu, đơn hàng, tồn kho)
  - Top sản phẩm bán chạy
  - Cảnh báo tồn kho
  - Charts và biểu đồ

### 3. Product Management (3 trang)
- **Danh sách sản phẩm** (`/products`): Grid view với search, filter, pagination
- **Chi tiết sản phẩm** (`/products/[id]`): Thông tin đầy đủ, variants, features
- **Tạo/Sửa sản phẩm** (`/products/new`, `/products/[id]/edit`): Form với validation

### 4. Customer Management (3 trang)
- **Danh sách khách hàng** (`/customers`): List view với search
- **Chi tiết khách hàng** (`/customers/[id]`): Thông tin, lịch sử đơn hàng, tickets
- **Tạo/Sửa khách hàng** (`/customers/new`, `/customers/[id]/edit`): Form

### 5. Order Management (3 trang)
- **Danh sách đơn hàng** (`/orders`): List view với filter theo status
- **Chi tiết đơn hàng** (`/orders/[id]`): Thông tin đầy đủ, payments, vehicle
- **Tạo đơn hàng** (`/orders/new`): Form tạo đơn hàng mới

### 6. Quotation Management (3 trang)
- **Danh sách báo giá** (`/quotations`): List view
- **Chi tiết báo giá** (`/quotations/[id]`): Xem và export PDF
- **Tạo báo giá** (`/quotations/new`): Form tạo báo giá

### 7. Payment Management (2 trang)
- **Danh sách thanh toán** (`/payments`): List với filter
- **Chi tiết thanh toán** (`/payments/[id]`): Thông tin chi tiết

### 8. Appointment Management (3 trang)
- **Danh sách lịch hẹn** (`/appointments`): Calendar view + List view
- **Chi tiết lịch hẹn** (`/appointments/[id]`): Thông tin và actions
- **Tạo lịch hẹn** (`/appointments/new`): Form với date picker

### 9. Inventory Management (2 trang)
- **Danh sách tồn kho** (`/inventory`): Table với filter, low stock alerts
- **Chi tiết tồn kho** (`/inventory/[id]`): Thông tin và điều chỉnh số lượng

### 10. Vehicle Management (3 trang)
- **Danh sách xe** (`/vehicles`): List với filter theo status, dealer
- **Chi tiết xe** (`/vehicles/[id]`): Thông tin VIN, lịch sử
- **Tạo/Sửa xe** (`/vehicles/new`, `/vehicles/[id]/edit`): Form

### 11. Dealer Management (3 trang)
- **Danh sách đại lý** (`/dealers`): List với filter theo brand, level
- **Chi tiết đại lý** (`/dealers/[id]`): Thông tin và statistics
- **Tạo/Sửa đại lý** (`/dealers/new`, `/dealers/[id]/edit`): Form

### 12. Brand Management (3 trang)
- **Danh sách thương hiệu** (`/brands`): List view
- **Chi tiết thương hiệu** (`/brands/[id]`): Thông tin và statistics
- **Tạo/Sửa thương hiệu** (`/brands/new`, `/brands/[id]/edit`): Form

### 13. Contract Management (3 trang)
- **Danh sách hợp đồng** (`/contracts`): List với filter theo status
- **Chi tiết hợp đồng** (`/contracts/[id]`): Thông tin và timeline
- **Tạo hợp đồng** (`/contracts/new`): Form

### 14. Sell-in Request Management (2 trang)
- **Danh sách yêu cầu** (`/sell-in-requests`): List với filter
- **Chi tiết yêu cầu** (`/sell-in-requests/[id]`): Thông tin và approve/reject

### 15. Promotion Management (3 trang)
- **Danh sách khuyến mãi** (`/promotions`): List với filter active/expired
- **Chi tiết khuyến mãi** (`/promotions/[id]`): Thông tin và usage stats
- **Tạo/Sửa khuyến mãi** (`/promotions/new`, `/promotions/[id]/edit`): Form

### 16. Support Ticket Management (3 trang)
- **Danh sách tickets** (`/support-tickets`): List với filter priority, status
- **Chi tiết ticket** (`/support-tickets/[id]`): Thông tin và comments
- **Tạo ticket** (`/support-tickets/new`): Form

### 17. Reports (2 trang)
- **Báo cáo** (`/reports`): Dashboard với các loại báo cáo
- **Chi tiết báo cáo** (`/reports/[type]`): Sales, Inventory, Revenue reports

### 18. Product Comparison (1 trang)
- **So sánh sản phẩm** (`/product-comparison`): So sánh 2-3 sản phẩm

### 19. User Management (3 trang) - Admin only
- **Danh sách người dùng** (`/users`): List với filter role
- **Chi tiết người dùng** (`/users/[id]`): Thông tin và permissions
- **Tạo/Sửa người dùng** (`/users/new`, `/users/[id]/edit`): Form

## Tổng cộng: ~45-50 trang

## Design System

### Colors
- Primary: Blue (#3B82F6)
- Secondary: Gray
- Success: Green
- Warning: Yellow
- Error: Red
- Info: Blue

### Typography
- Headings: Inter, Bold
- Body: Inter, Regular
- Code: Monospace

### Components
- Buttons: Primary, Secondary, Outline, Ghost, Destructive
- Cards: Default, Elevated
- Forms: Input, Select, Checkbox, Radio, DatePicker
- Tables: Sortable, Filterable, Paginated
- Modals: Dialog, Alert Dialog
- Navigation: Sidebar, Breadcrumbs, Tabs

### Layout
- Sidebar navigation (collapsible)
- Top header với user menu
- Main content area
- Responsive breakpoints: sm, md, lg, xl

## Features

### 1. Authentication & Authorization
- JWT-based authentication
- Role-based access control (RBAC)
- Protected routes
- Auto logout on token expiry

### 2. Data Management
- CRUD operations cho tất cả entities
- Real-time updates (nếu cần)
- Optimistic updates
- Error handling và retry logic

### 3. Search & Filter
- Global search
- Advanced filters
- Sort options
- Pagination

### 4. Forms
- Validation với Zod
- Error messages
- Loading states
- Success feedback

### 5. Charts & Analytics
- Revenue charts
- Sales trends
- Inventory levels
- Performance metrics

### 6. Export
- Export to PDF (Quotations, Reports)
- Export to Excel (Reports, Inventory)

### 7. Notifications
- Toast notifications
- Alert dialogs
- Success/Error messages

## Responsive Design

- Mobile (< 640px): Stack layout, hamburger menu
- Tablet (640px - 1024px): Sidebar collapsible
- Desktop (> 1024px): Full sidebar, multi-column layouts

## Performance

- Code splitting
- Lazy loading
- Image optimization
- Caching strategies
- API request optimization

## Accessibility

- Keyboard navigation
- Screen reader support
- ARIA labels
- Focus management
- Color contrast

## Security

- XSS protection
- CSRF protection
- Secure token storage
- Input sanitization
- API security headers

