<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   QUẢN LÝ SÁCH - THƯ VIỆN QUA MẠNG
</h2>
<div align="center">
    <p align="center">
        <img src="<img width="1508" height="1687" alt="aiotlab_logo" src="https://github.com/user-attachments/assets/bc3744ec-8593-45e0-9224-eb14cdcccc56" />
" alt="AIoTLab Logo" width="170"/>
        <img src="images/fitdnu_logo.png" alt="FIT Logo" width="180"/>
        <img src="images/dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>

## 📖 1. Giới thiệu

Ứng dụng Quản lý sách – thư viện qua mạng được xây dựng trên mô hình Client – Server sử dụng:
- **TCP Protocol** (ServerSocket, Socket) để trao đổi dữ liệu qua mạng.
- **Java Swing** để xây dựng giao diện người dùng.
- **MongoDB** để lưu trữ dữ liệu.
- **Maven** để quản lý dependencies và build project.

📌 **Mục tiêu**

Tạo ra một hệ thống thư viện trực tuyến, trong đó Server quản lý dữ liệu (sách, người dùng, mượn/trả), còn Client cung cấp giao diện cho người dùng đăng nhập, tìm kiếm và mượn sách từ xa với phân quyền Admin và User.

### ✨ Các chức năng chính

#### Chức năng chung
- **Đăng nhập/Đăng ký** người dùng với phân quyền (Admin/User)
- **Tìm kiếm sách** theo tên, tác giả, thể loại
- **Xem danh sách sách** với đầy đủ thông tin

#### Chức năng Admin
- **Quản lý sách**: Thêm, Sửa, Xóa sách, Quản lý Book Copies
- **Quản lý người dùng**: Thêm, Sửa, Xóa người dùng, Khóa/Mở khóa tài khoản, Đặt lại mật khẩu
- **Quản lý mượn trả**: Xem tất cả bản ghi mượn trả, Đánh dấu mất/hỏng sách, Bắt buộc trả sách
- **Dashboard & Báo cáo**: Thống kê tổng quan, Báo cáo sách, Báo cáo người dùng, Báo cáo mượn trả, Báo cáo phạt

#### Chức năng User
- **Xem trang chủ** với thống kê cá nhân
- **Tìm kiếm và xem chi tiết sách**
- **Mượn sách** (giới hạn 5 sách/người)
- **Trả sách** với tính phạt tự động
- **Gia hạn sách**
- **Xem lịch sử mượn sách** của mình
- **Xem và sửa thông tin cá nhân**

## 🔧 2. Ngôn ngữ lập trình sử dụng

[![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
[![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)

- **Ngôn ngữ lập trình**: Java (JDK 11+)  
- **Giao diện**: Java Swing  
- **Mạng**: TCP Protocol (ServerSocket, Socket)  
- **Lưu trữ dữ liệu**: MongoDB (NoSQL Database)
- **Build Tool**: Maven
- **Môi trường phát triển**: IntelliJ IDEA, Eclipse, hoặc NetBeans
- **Hệ điều hành**: Windows / macOS / Linux

## 🚀 3. Hình ảnh giao diện

<p align="center">
  <img src="images/login_screenshot.png" alt="Giao diện đăng nhập" width="700"/>
</p>

<p align="center">
  <img src="images/admin_dashboard_screenshot.png" alt="Giao diện Admin Dashboard" width="700"/>
</p>

<p align="center">
  <img src="images/user_home_screenshot.png" alt="Giao diện User Home" width="700"/>
</p>

## 📝 4. Hướng dẫn cài đặt và sử dụng

### 🔧 Yêu cầu hệ thống

- **Java Development Kit (JDK)**: Phiên bản 11 trở lên  
- **Maven**: Phiên bản 3.6+ (hoặc sử dụng Maven wrapper có sẵn)
- **MongoDB Server**: Đang chạy trên `localhost:27017`
- **MongoDB Compass**: Khuyến nghị để xem dữ liệu (tùy chọn)
- **Hệ điều hành**: Windows / macOS / Linux
- **IDE khuyến nghị**: IntelliJ IDEA, Eclipse, hoặc NetBeans
- **Bộ nhớ**: Tối thiểu 512MB RAM  

---

### 📦 Các bước triển khai

#### 🔹 Bước 1: Chuẩn bị môi trường

1. **Cài đặt JDK** nếu chưa có:  
   - Kiểm tra bằng lệnh:  
     ```bash
     java -version
     javac -version
     ```
   - Nếu chưa có, tải JDK tại [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html) hoặc [OpenJDK](https://adoptium.net/).

2. **Cài đặt MongoDB**:
   - Tải và cài đặt MongoDB từ [MongoDB Download Center](https://www.mongodb.com/try/download/community)
   - Khởi động MongoDB Server trên port mặc định 27017
   - (Tùy chọn) Cài đặt MongoDB Compass để quản lý database

3. **Cài đặt Maven** (nếu chưa có):
   - Tải Maven từ [Apache Maven](https://maven.apache.org/download.cgi)
   - Hoặc sử dụng Maven wrapper có sẵn trong project

4. **Tải mã nguồn dự án**:  
   - Clone repo bằng Git hoặc tải file `.zip` và giải nén

---

#### 🔹 Bước 2: Cấu hình MongoDB

1. Đảm bảo MongoDB Server đang chạy:
   ```bash
   # Kiểm tra MongoDB đang chạy
   mongosh
   # Hoặc sử dụng MongoDB Compass để kết nối
   ```

2. Database `library_db` sẽ được tạo tự động khi server khởi động lần đầu.

---

#### 🔹 Bước 3: Biên dịch mã nguồn

**Windows:**
```bash
build-maven.bat
```

**Linux/Mac:**
```bash
chmod +x build-maven.sh
./build-maven.sh
```

**Hoặc sử dụng Maven trực tiếp:**
```bash
mvn clean compile
```

---

#### 🔹 Bước 4: Chạy Server

**Windows:**
```bash
run-server-maven.bat
```

**Linux/Mac:**
```bash
chmod +x run-server-maven.sh
./run-server-maven.sh
```

**Hoặc sử dụng Maven trực tiếp:**
```bash
mvn exec:java -Dexec.mainClass="server.Server"
```

Server sẽ chạy trên **port 12345**. Bạn sẽ thấy thông báo:
```
Server started on port 12345
Database connected successfully
Initializing data...
```

---

#### 🔹 Bước 5: Chạy Client

Mở một terminal/cửa sổ mới:

**Windows:**
```bash
run-client-maven.bat
```

**Linux/Mac:**
```bash
chmod +x run-client-maven.sh
./run-client-maven.sh
```

**Hoặc sử dụng Maven trực tiếp:**
```bash
mvn exec:java -Dexec.mainClass="client.LoginFrame"
```

Giao diện đăng nhập sẽ hiện ra.

---

#### 🔹 Bước 6: Đăng nhập

Sau khi khởi tạo dữ liệu, hệ thống sẽ tự động tạo tài khoản mặc định:

- **Admin**: 
  - Email: `admin@library.com` (hoặc theo cấu hình trong DatabaseManager)
  - Password: `admin123`

- **User**: Có thể đăng ký tài khoản mới hoặc sử dụng tài khoản mẫu đã được tạo tự động.

---

### 📊 Cấu trúc Database MongoDB

Database: `library_db`

**Collections:**
- `users` - Thông tin người dùng
- `books` - Thông tin sách
- `book_copies` - Bản sao sách
- `borrow_records` - Lịch sử mượn trả
- `fines` - Thông tin phạt
- `categories` - Thể loại sách

Hệ thống sẽ tự động khởi tạo dữ liệu mẫu khi server khởi động lần đầu (nếu collections rỗng):
- 7 thể loại sách
- 1 admin user
- 8 sample users
- 40 quyển sách (mỗi thể loại 4 quyển)
- ~300+ bản sao sách
- Một số bản ghi mượn trả và phạt

---

### 🏗️ Cấu trúc Project

```
Quan_Ly_Sach_Thu_Vien_Qua_Mang/
├── src/
│   ├── model/
│   │   ├── Book.java          # Model sách
│   │   ├── BookCopy.java      # Model bản sao sách
│   │   ├── User.java          # Model người dùng
│   │   └── BorrowRecord.java  # Model phiếu mượn
│   ├── server/
│   │   ├── Server.java        # TCP Server với multi-threading
│   │   ├── DatabaseManager.java # Quản lý database MongoDB
│   │   └── DataImporter.java  # Import dữ liệu
│   ├── client/
│   │   ├── Client.java        # TCP Client
│   │   ├── LoginFrame.java    # Giao diện đăng nhập
│   │   ├── RegistrationFrame.java # Giao diện đăng ký
│   │   ├── AdminFrame.java    # Giao diện Admin
│   │   └── UserFrame.java     # Giao diện User
│   └── util/
│       └── Message.java       # Class Message để giao tiếp
├── lib/                       # Thư mục chứa các thư viện JAR
├── bin/                       # Thư mục chứa file .class
├── target/                    # Thư mục build của Maven
├── pom.xml                    # File cấu hình Maven
├── build-maven.bat            # Script build (Windows)
├── run-server-maven.bat       # Script chạy server (Windows)
├── run-client-maven.bat       # Script chạy client (Windows)
└── README.md                  # File hướng dẫn này
```

---

### 🔌 Giao thức giao tiếp

Ứng dụng sử dụng giao thức TCP với các loại message:

**Authentication:**
- `LOGIN`, `REGISTER`, `LOGOUT`

**Books Management:**
- `GET_ALL_BOOKS`, `SEARCH_BOOKS`, `ADD_BOOK`, `UPDATE_BOOK`, `DELETE_BOOK`
- `GET_BOOK_COPIES`, `ADD_BOOK_COPY`, `DELETE_BOOK_COPY`

**Users Management (Admin only):**
- `GET_ALL_USERS`, `ADD_USER`, `UPDATE_USER`, `DELETE_USER`
- `LOCK_USER`, `UNLOCK_USER`, `RESET_PASSWORD`

**Borrow/Return Management:**
- `BORROW_BOOK`, `RETURN_BOOK`, `RENEW_BOOK`
- `GET_USER_BORROW_RECORDS`, `GET_ALL_BORROW_RECORDS`
- `MARK_LOST`, `MARK_DAMAGED`, `FORCE_RETURN`

**Dashboard & Reports (Admin only):**
- `GET_DASHBOARD_STATS`, `GET_BOOK_REPORT`, `GET_USER_REPORT`
- `GET_BORROW_REPORT`, `GET_PENALTY_REPORT`

---

### ⚠️ Lưu ý

- Phải chạy Server trước khi chạy Client
- Có thể chạy nhiều Client cùng lúc (multi-threading)
- Database MongoDB sẽ được tạo tự động khi server khởi động lần đầu
- Dữ liệu mẫu sẽ được tạo tự động nếu collections rỗng
- Server phải được chạy liên tục để client có thể kết nối

---

## 👤 5. Liên hệ

**Dự án được phát triển cho môn học Lập Trình Mạng.**

**Faculty of Information Technology**  
**DaiNam University**

© 2025 Faculty of Information Technology, DaiNam University. All rights reserved.

---
