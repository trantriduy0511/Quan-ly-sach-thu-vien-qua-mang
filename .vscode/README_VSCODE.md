# Hướng dẫn chạy dự án trên VS Code

## Yêu cầu

1. **Cài đặt Extension Pack for Java** trong VS Code:
   - Mở VS Code
   - Vào Extensions (Ctrl+Shift+X)
   - Tìm và cài đặt "Extension Pack for Java" (Microsoft)

2. **Đảm bảo đã cài đặt:**
   - JDK 11 hoặc cao hơn
   - Maven
   - MongoDB đang chạy trên localhost:27017

## Cách chạy dự án

### Bước 1: Mở dự án trong VS Code
- Mở thư mục dự án trong VS Code

### Bước 2: Build dự án
- Nhấn `Ctrl+Shift+P` (hoặc `Cmd+Shift+P` trên Mac)
- Gõ "Tasks: Run Task"
- Chọn "Maven: Clean Compile" để build dự án

Hoặc sử dụng terminal:
```bash
mvn clean compile
```

### Bước 3: Chạy Server
Có 2 cách:

**Cách 1: Sử dụng Debug/Run (Khuyến nghị)**
- Nhấn `F5` hoặc vào tab "Run and Debug" (Ctrl+Shift+D)
- Chọn "Run Server" từ dropdown
- Nhấn nút Run (▶️)

**Cách 2: Sử dụng Terminal**
- Mở terminal trong VS Code (Ctrl+`)
- Chạy lệnh:
```bash
mvn exec:java -Dexec.mainClass="server.Server"
```

### Bước 4: Chạy Client
Sau khi Server đã chạy, mở terminal/cửa sổ mới:

**Cách 1: Sử dụng Debug/Run**
- Nhấn `F5` hoặc vào tab "Run and Debug"
- Chọn "Run Client" từ dropdown
- Nhấn nút Run (▶️)

**Cách 2: Sử dụng Terminal**
```bash
mvn exec:java -Dexec.mainClass="client.LoginFrame"
```

## Debug

Để debug, chọn:
- "Debug Server" để debug server
- "Debug Client" để debug client

Sau đó đặt breakpoint và chạy như bình thường.

## Chạy đồng thời Server và Client

- Chọn "Run Server and Client" từ dropdown trong Run and Debug
- Cả hai sẽ chạy đồng thời

## Lưu ý

- Phải chạy Server trước khi chạy Client
- Đảm bảo MongoDB đang chạy
- Nếu gặp lỗi, kiểm tra:
  - Java version (phải >= 11)
  - Maven đã được cài đặt và trong PATH
  - MongoDB đang chạy

