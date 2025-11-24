# Hướng dẫn Setup Frontend EVM

## Yêu cầu hệ thống

- Node.js 18+ 
- npm hoặc yarn
- Backend API đang chạy tại `http://localhost:8080`

## Cài đặt

### 1. Cài đặt dependencies

```bash
cd frontend
npm install
```

### 2. Cấu hình môi trường

Tạo file `.env.local`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### 3. Chạy development server

```bash
npm run dev
```

Ứng dụng sẽ chạy tại [http://localhost:3000](http://localhost:3000)

## Cấu trúc dự án

```
frontend/
├── app/                      # Next.js App Router
│   ├── (auth)/              # Authentication routes
│   │   └── login/
│   ├── (dashboard)/         # Protected dashboard routes
│   │   ├── dashboard/
│   │   ├── products/
│   │   ├── customers/
│   │   └── ...
│   ├── layout.tsx           # Root layout
│   ├── page.tsx             # Home page (redirects to dashboard)
│   └── globals.css          # Global styles
├── components/              # React components
│   ├── ui/                 # shadcn/ui components
│   └── layout/             # Layout components
├── lib/                    # Utilities
│   ├── api/               # API client
│   └── utils.ts           # Helper functions
├── types/                 # TypeScript types
├── store/                 # Zustand stores
└── public/               # Static assets
```

## Tính năng chính

### ✅ Đã hoàn thành

- [x] Cấu trúc dự án Next.js 14
- [x] Authentication (Login)
- [x] Dashboard layout với Sidebar
- [x] Dashboard page với statistics
- [x] Products page (list view)
- [x] Customers page (list view)
- [x] Orders page (list view)
- [x] UI Components (Button, Input, Card)
- [x] API client với interceptors
- [x] State management (Zustand)
- [x] TypeScript types
- [x] Utility functions

### 🚧 Cần hoàn thiện

- [ ] Chi tiết các trang (detail pages)
- [ ] Form tạo/sửa (create/edit forms)
- [ ] Advanced filters
- [ ] Charts và graphs
- [ ] Export PDF/Excel
- [ ] Calendar view cho appointments
- [ ] Product comparison page
- [ ] Reports pages
- [ ] Image upload
- [ ] More UI components (Select, DatePicker, etc.)

## API Integration

Tất cả API calls được thực hiện qua `lib/api/client.ts` với:
- Automatic token injection
- Error handling
- Toast notifications
- Auto redirect on 401

## Authentication Flow

1. User đăng nhập tại `/login`
2. Token được lưu vào localStorage
3. Token được tự động thêm vào mọi API request
4. Nếu token hết hạn (401), tự động redirect về login

## Role-based Access

Sidebar menu được filter dựa trên roles của user:
- ADMIN: Tất cả menu
- BRAND_MANAGER: Hầu hết menu (trừ User Management)
- DEALER_STAFF: Menu hạn chế
- CUSTOMER: Chỉ một số menu

## Development Tips

### Thêm trang mới

1. Tạo folder trong `app/(dashboard)/[page-name]/`
2. Tạo `page.tsx` và `layout.tsx`
3. Thêm route vào Sidebar nếu cần

### Thêm API endpoint

1. Tạo function trong `lib/api/[module].ts`
2. Sử dụng `apiClient` từ `lib/api/client.ts`
3. Define types trong `types/index.ts`

### Thêm UI component

1. Tạo component trong `components/ui/`
2. Sử dụng Tailwind CSS
3. Export và sử dụng trong pages

## Troubleshooting

### Lỗi kết nối API

- Kiểm tra backend đang chạy tại `http://localhost:8080`
- Kiểm tra CORS settings trong backend
- Kiểm tra `.env.local` file

### Lỗi build

```bash
rm -rf .next node_modules
npm install
npm run build
```

### Lỗi TypeScript

```bash
npm run lint
```

## Production Build

```bash
npm run build
npm start
```

## Next Steps

1. Hoàn thiện các trang detail
2. Thêm form validation
3. Thêm charts và graphs
4. Optimize performance
5. Add tests
6. Deploy to production

