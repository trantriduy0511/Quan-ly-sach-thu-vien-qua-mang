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
        <img src="images/aiotlab_logo.png" alt="AIoTLab Logo" width="170"/>
        <img src="images/fitdnu_logo.png" alt="AIoTLab Logo" width="180"/>
        <img src="images/dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)


</div>

## 📖 1. Giới thiệu
Ứng dụng Quản lý sách – thư viện qua mạng được xây dựng trên mô hình Client – Server sử dụng:
- Java RMI (Remote Method Invocation) để trao đổi dữ liệu qua mạng.
- Java Swing để xây dựng giao diện người dùng.
  
📌Mục tiêu 

Tạo ra một hệ thống thư viện trực tuyến, trong đó Server quản lý dữ liệu (sách, người dùng, mượn/trả), còn Client cung cấp giao diện cho người dùng đăng nhập, tìm kiếm và mượn sách từ xa.

  ### ✨ Các chức năng chính
- **Đăng nhập/Đăng ký** người dùng.
- **Quản lý sách (Server)**: thêm, sửa, xóa sách.
- **Tìm kiếm sách (Client)**: theo tên, tác giả, thể loại.
- **Mượn và trả sách**: ghi nhận trên server.
- **Lưu trữ dữ liệu**: bằng File hoặc Database (MySQL/SQLite).
- **Trao đổi dữ liệu** qua RMI.

## 🔧 2. Ngôn ngữ lập trình sử dụng: [![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
- **Ngôn ngữ lập trình**: Java  
- **Giao diện**: Java Swing  
- **Mạng**: Java RMI (ServerSocket, Socket)  
- **Lưu trữ dữ liệu**:
  - File (Object Serialization/JSON) hoặc
  - Database (MySQL/SQLite qua JDBC)  
- **Môi trường phát triển**: Eclipse IDE
- **Hệ điều hành**: Windows
  
## 🚀 3. Hình ảnh giao diện

 <img width="1103" height="740" alt="image" src="https://github.com/user-attachments/assets/9c7a02e3-f633-416c-81d0-3bc79eb68003" />

 <img width="1107" height="738" alt="image" src="https://github.com/user-attachments/assets/cfeb157d-0d59-45a5-b5b5-7516614ee375" />



## 📝 4. Hướng dẫn cài đặt và sử dụng

### 🔧 Yêu cầu hệ thống
- **Java Development Kit (JDK)**: Phiên bản 8 trở lên  
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

2. **Tải mã nguồn dự án**:  
   - Clone repo bằng Git:  
     ```bash
     https://github.com/trantriduy0511/Quan-ly-sach-thu-vien-qua-mang/tree/main
     ```
   - Hoặc tải file `.zip` và giải nén.

---

#### 🔹 Bước 2: Biên dịch mã nguồn
Di chuyển đến Project
#### 🔹 Bước 3: Chạy file LibraryServer.java

#### 🔹 Bước 4: Chạy file ClientUI.java
- Giao diện thư viện sẽ hiện ra

## 👤 5. Liên hệ
**Họ tên**: Trần Trí Duy.  
**Lớp**: CNTT 16-03.  
**Email**: trantriduy2004ss@gmail.com.

© 2025 Faculty of Information Technology, DaiNam University. All rights reserved.

---
