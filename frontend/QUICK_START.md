# Quick Start Guide - EVM Frontend

## Bước 1: Cài đặt

```bash
cd frontend
npm install
```

## Bước 2: Cấu hình

Tạo file `.env.local`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## Bước 3: Chạy

```bash
npm run dev
```

Mở trình duyệt tại: http://localhost:3000

## Đăng nhập

- Username: (tùy theo backend của bạn)
- Password: (tùy theo backend của bạn)

## Cấu trúc đã tạo

### ✅ Hoàn thành
- Login page
- Dashboard với statistics
- Products list
- Customers list
- Orders list
- Inventory list
- Brands list
- Dealers list
- Sidebar navigation
- API client
- Authentication flow

### 🚧 Cần phát triển tiếp
- Detail pages
- Create/Edit forms
- Advanced features
- Charts và reports

## Next Steps

Xem file `SUMMARY.md` để biết chi tiết những gì cần làm tiếp.

## Troubleshooting

### Lỗi "Cannot find module"
```bash
rm -rf node_modules .next
npm install
```

### Lỗi kết nối API
- Kiểm tra backend đang chạy tại port 8080
- Kiểm tra CORS settings trong backend
- Kiểm tra file `.env.local`

### Lỗi build
```bash
npm run build
```

## Support

Xem các file:
- `SETUP.md` - Hướng dẫn setup chi tiết
- `DESIGN_DOCUMENT.md` - Tài liệu thiết kế
- `SUMMARY.md` - Tổng kết và roadmap

