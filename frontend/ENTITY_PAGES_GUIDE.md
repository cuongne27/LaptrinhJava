# Hướng dẫn tạo Entity Pages

Mỗi entity trong backend sẽ có 1 trang tương ứng với đầy đủ CRUD và các actions.

## Pattern chung

Mỗi trang entity sẽ có:

1. **List View**: Hiển thị danh sách với search, pagination
2. **Create Modal**: Form tạo mới
3. **Edit Modal**: Form sửa
4. **Detail Modal**: Xem chi tiết
5. **Delete Action**: Xóa với confirmation
6. **Refresh Button**: Làm mới dữ liệu

## Cấu trúc code

```tsx
// 1. State management
const [items, setItems] = useState<Entity[]>([]);
const [loading, setLoading] = useState(true);
const [search, setSearch] = useState("");
const [page, setPage] = useState(0);
const [selectedItem, setSelectedItem] = useState<Entity | null>(null);
const [viewMode, setViewMode] = useState<"list" | "create" | "edit" | "detail">("list");

// 2. Form với react-hook-form + zod
const { register, handleSubmit, reset, formState: { errors } } = useForm<EntityForm>({
  resolver: zodResolver(entitySchema),
});

// 3. CRUD functions
const fetchItems = async () => { /* GET */ };
const handleCreate = () => { /* Show create modal */ };
const handleEdit = (item) => { /* Show edit modal */ };
const handleView = (item) => { /* Show detail modal */ };
const handleDelete = async (item) => { /* DELETE */ };
const onSubmit = async (data) => { /* POST/PUT */ };

// 4. UI Components
- Search input
- List/Card view
- Create/Edit/Detail modals
- Action buttons (View, Edit, Delete)
- Pagination
```

## Các entity cần tạo

1. ✅ Brands - Đã hoàn thành
2. ✅ Products - Đã hoàn thành
3. ⏳ Customers
4. ⏳ Dealers
5. ⏳ Orders (Sales Orders)
6. ⏳ Quotations
7. ⏳ Payments
8. ⏳ Appointments
9. ⏳ Inventory
10. ⏳ Vehicles
11. ⏳ Contracts (Dealer Contracts)
12. ⏳ Sell-in Requests
13. ⏳ Promotions
14. ⏳ Support Tickets
15. ⏳ Users

## Special Actions

Một số entity có actions đặc biệt:

### Orders
- Assign Vehicle
- Update Status
- Cancel Order

### Quotations
- Send Quotation
- Accept/Reject
- Convert to Order
- Export PDF

### Payments
- Confirm Payment
- Refund

### Appointments
- Cancel Appointment
- Mark Completed

### Inventory
- Adjust Quantity
- Reserve/Release
- Transfer

### Sell-in Requests
- Approve/Reject
- Mark In Transit
- Mark Delivered

### Support Tickets
- Assign Ticket
- Update Status
- Close/Reopen

## Best Practices

1. **Validation**: Luôn validate form với Zod
2. **Error Handling**: Hiển thị toast messages
3. **Loading States**: Hiển thị loading khi fetch data
4. **Confirmation**: Confirm trước khi delete
5. **Refresh**: Tự động refresh sau khi create/update/delete
6. **Pagination**: Hỗ trợ pagination cho list dài
7. **Search**: Real-time search với debounce (optional)

## Components tái sử dụng

- `EntityModal`: Modal component cho create/edit/detail
- `EntityPage`: Wrapper component (optional, có thể không dùng nếu cần custom)

## Example: Brands Page

Xem file `app/(dashboard)/brands/page.tsx` để tham khảo pattern hoàn chỉnh.

