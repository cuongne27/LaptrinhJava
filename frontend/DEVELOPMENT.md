# Development Guide

## Vô hiệu hóa Authentication Protection

Để vô hiệu hóa chức năng bảo vệ route (cho phép truy cập các trang mà không cần đăng nhập) trong quá trình development:

### Cách 1: Sử dụng Environment Variable

Tạo file `.env.local` trong thư mục `frontend/` với nội dung:

```env
NEXT_PUBLIC_DISABLE_AUTH_PROTECTION=true
```

Sau đó restart dev server.

### Cách 2: Sửa trực tiếp trong code

Mở file `frontend/app/components/auth/ProtectedRoute.tsx` và thay đổi dòng 13:

```typescript
// Thay đổi từ:
const DISABLE_PROTECTION = process.env.NEXT_PUBLIC_DISABLE_AUTH_PROTECTION === "true";

// Thành:
const DISABLE_PROTECTION = true; // Disable protection for development
```

**Lưu ý:** Nhớ đổi lại thành `false` hoặc sử dụng environment variable trước khi commit code!

