package server;

import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

import static com.mongodb.client.model.Filters.*;

public class DataImporter {
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "library_db";
    
    public static void main(String[] args) {
        MongoClient mongoClient = null;
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            
            MongoCollection<Document> usersCollection = database.getCollection("users");
            MongoCollection<Document> booksCollection = database.getCollection("books");
            MongoCollection<Document> bookCopiesCollection = database.getCollection("book_copies");
            MongoCollection<Document> borrowRecordsCollection = database.getCollection("borrow_records");
            MongoCollection<Document> categoriesCollection = database.getCollection("categories");
            MongoCollection<Document> finesCollection = database.getCollection("fines");
            
            System.out.println("Starting data import to MongoDB...");
            
            // Clear existing data (optional - comment out if you want to keep existing data)
            System.out.println("Clearing existing data...");
            usersCollection.deleteMany(new Document());
            booksCollection.deleteMany(new Document());
            bookCopiesCollection.deleteMany(new Document());
            borrowRecordsCollection.deleteMany(new Document());
            categoriesCollection.deleteMany(new Document());
            finesCollection.deleteMany(new Document());
            
            // Initialize Categories
            List<Document> categories = new ArrayList<>();
            categories.add(new Document("categoryId", "CAT001").append("name", "Công nghệ thông tin").append("description", "Sách về lập trình, thuật toán, cấu trúc dữ liệu"));
            categories.add(new Document("categoryId", "CAT002").append("name", "Văn học").append("description", "Tiểu thuyết, truyện ngắn, thơ"));
            categories.add(new Document("categoryId", "CAT003").append("name", "Lịch sử").append("description", "Sách lịch sử Việt Nam và thế giới"));
            categories.add(new Document("categoryId", "CAT004").append("name", "Kinh tế").append("description", "Sách về kinh tế học, quản trị kinh doanh"));
            categories.add(new Document("categoryId", "CAT005").append("name", "Y học").append("description", "Sách về y học, chăm sóc sức khỏe"));
            categories.add(new Document("categoryId", "CAT006").append("name", "Kỹ thuật").append("description", "Sách về kỹ thuật, công nghệ kỹ thuật"));
            categories.add(new Document("categoryId", "CAT007").append("name", "Tâm lý học").append("description", "Sách về tâm lý học, hành vi con người"));
            categoriesCollection.insertMany(categories);
            System.out.println("Categories imported: " + categories.size() + " categories");
            
            // Initialize Admin User
            Document admin = new Document("userId", "ADMIN001")
                .append("email", "dainam@dnu.edu.vn")
                .append("password", "dainam")
                .append("firstName", "Đại")
                .append("lastName", "Nam")
                .append("phone", "0123456789")
                .append("address", "Số 1, Phú Lãm, Hà Đông, Hà Nội")
                .append("role", "ADMIN")
                .append("status", "ACTIVE")
                .append("studentId", "1671020001")
                .append("faculty", "Công nghệ thông tin")
                .append("yearOfStudy", "2024")
                .append("totalBorrowed", 0)
                .append("currentBorrowed", 0)
                .append("totalFines", 0.0)
                .append("registrationDate", new java.util.Date())
                .append("lastLogin", null)
                .append("isOnline", false);
            usersCollection.insertOne(admin);
            System.out.println("Admin user imported: dainam@dnu.edu.vn/dainam");
            
            // Initialize Sample Users
            List<Document> users = new ArrayList<>();
            users.add(new Document("userId", "USER001")
                .append("email", "nghia@dnu.edu.vn")
                .append("password", "nghia123")
                .append("firstName", "Nghĩa")
                .append("lastName", "Đỗ Ngọc")
                .append("phone", "0912345678")
                .append("address", "Tây Mỗ, Nam Từ Liêm, Hà Nội")
                .append("role", "USER")
                .append("status", "ACTIVE")
                .append("studentId", "1671020220")
                .append("faculty", "Công nghệ thông tin")
                .append("yearOfStudy", "2025")
                .append("totalBorrowed", 5) // 5 borrow records total
                .append("currentBorrowed", 5) // 5 active borrows: COPY001-13, COPY005-14, COPY001-14, COPY002-12, COPY001-15
                .append("totalFines", 0.0)
                .append("registrationDate", new java.util.Date(System.currentTimeMillis() - 86400000L * 30))
                .append("lastLogin", null)
                .append("isOnline", false));
            
            users.add(new Document("userId", "USER002")
                .append("email", "ngoc@dnu.edu.vn")
                .append("password", "ngoc123")
                .append("firstName", "Ngọc")
                .append("lastName", "Lê Thị")
                .append("phone", "0923456789")
                .append("address", "Số 456, Đường XYZ, Hà Nội")
                .append("role", "USER")
                .append("status", "ACTIVE")
                .append("studentId", "1671020221")
                .append("faculty", "Kinh tế")
                .append("yearOfStudy", "2024")
                .append("totalBorrowed", 2) // 2 borrow records total (both returned)
                .append("currentBorrowed", 0) // 0 active borrows (all returned)
                .append("totalFines", 50000.0)
                .append("registrationDate", new java.util.Date(System.currentTimeMillis() - 86400000L * 60))
                .append("lastLogin", null)
                .append("isOnline", false));
            
            users.add(new Document("userId", "USER003")
                .append("email", "minh@dnu.edu.vn")
                .append("password", "minh123")
                .append("firstName", "Minh")
                .append("lastName", "Phạm Văn")
                .append("phone", "0934567890")
                .append("address", "Số 789, Đường DEF, Hà Nội")
                .append("role", "USER")
                .append("status", "ACTIVE")
                .append("studentId", "1671020222")
                .append("faculty", "Y học")
                .append("yearOfStudy", "2023")
                .append("totalBorrowed", 3) // 3 borrow records total
                .append("currentBorrowed", 1) // 1 active borrow: COPY004-14
                .append("totalFines", 0.0)
                .append("registrationDate", new java.util.Date(System.currentTimeMillis() - 86400000L * 15))
                .append("lastLogin", null)
                .append("isOnline", false));
            
            users.add(new Document("userId", "USER004")
                .append("email", "linh@dnu.edu.vn")
                .append("password", "linh123")
                .append("firstName", "Linh")
                .append("lastName", "Vũ Thị")
                .append("phone", "0945678901")
                .append("address", "Số 321, Đường GHI, Hà Nội")
                .append("role", "USER")
                .append("status", "ACTIVE")
                .append("studentId", "1671020223")
                .append("faculty", "Kỹ thuật")
                .append("yearOfStudy", "2024")
                .append("totalBorrowed", 12)
                .append("currentBorrowed", 1)
                .append("totalFines", 30000.0)
                .append("registrationDate", new java.util.Date(System.currentTimeMillis() - 86400000L * 90))
                .append("lastLogin", null)
                .append("isOnline", false));
            
            users.add(new Document("userId", "USER005")
                .append("email", "hoang@dnu.edu.vn")
                .append("password", "hoang123")
                .append("firstName", "Hoàng")
                .append("lastName", "Nguyễn Văn")
                .append("phone", "0956789012")
                .append("address", "Số 654, Đường JKL, Hà Nội")
                .append("role", "USER")
                .append("status", "LOCKED")
                .append("studentId", "1671020224")
                .append("faculty", "Khoa học xã hội")
                .append("yearOfStudy", "2023")
                .append("totalBorrowed", 7)
                .append("currentBorrowed", 2)
                .append("totalFines", 0.0)
                .append("registrationDate", new java.util.Date(System.currentTimeMillis() - 86400000L * 45))
                .append("lastLogin", null)
                .append("isOnline", false));
            
            users.add(new Document("userId", "USER006")
                .append("email", "thao@dnu.edu.vn")
                .append("password", "thao123")
                .append("firstName", "Thảo")
                .append("lastName", "Trần Thị")
                .append("phone", "0967890123")
                .append("address", "Số 987, Đường MNO, Hà Nội")
                .append("role", "USER")
                .append("status", "ACTIVE")
                .append("studentId", "1671020225")
                .append("faculty", "Nghệ thuật")
                .append("yearOfStudy", "2024")
                .append("totalBorrowed", 5)
                .append("currentBorrowed", 0)
                .append("totalFines", 0.0)
                .append("registrationDate", new java.util.Date(System.currentTimeMillis() - 86400000L * 20))
                .append("lastLogin", null)
                .append("isOnline", false));
            
            users.add(new Document("userId", "USER007")
                .append("email", "tuan@dnu.edu.vn")
                .append("password", "tuan123")
                .append("firstName", "Tuấn")
                .append("lastName", "Lê Văn")
                .append("phone", "0978901234")
                .append("address", "Số 111, Đường PQR, Hà Nội")
                .append("role", "USER")
                .append("status", "ACTIVE")
                .append("studentId", "1671020226")
                .append("faculty", "Công nghệ thông tin")
                .append("yearOfStudy", "2024")
                .append("totalBorrowed", 6)
                .append("currentBorrowed", 2)
                .append("totalFines", 0.0)
                .append("registrationDate", new java.util.Date(System.currentTimeMillis() - 86400000L * 25))
                .append("lastLogin", null)
                .append("isOnline", false));
            
            users.add(new Document("userId", "USER008")
                .append("email", "duc@dnu.edu.vn")
                .append("password", "duc123")
                .append("firstName", "Đức")
                .append("lastName", "Trần Văn")
                .append("phone", "0990123456")
                .append("address", "Số 333, Đường VWX, Hà Nội")
                .append("role", "USER")
                .append("status", "ACTIVE")
                .append("studentId", "1671020228")
                .append("faculty", "Y học")
                .append("yearOfStudy", "2024")
                .append("totalBorrowed", 9)
                .append("currentBorrowed", 0)
                .append("totalFines", 0.0)
                .append("registrationDate", new java.util.Date(System.currentTimeMillis() - 86400000L * 50))
                .append("lastLogin", null)
                .append("isOnline", false));
            
            usersCollection.insertMany(users);
            System.out.println("Users imported: " + (users.size() + 1) + " users (1 admin + " + users.size() + " users)");
            
            // Initialize Books
            List<Document> books = new ArrayList<>();
            books.add(new Document("bookId", "BOOK001")
                .append("title", "Lập trình Java từ cơ bản đến nâng cao")
                .append("author", "Nguyễn Văn Minh")
                .append("isbn", "978-604-1-00123-4")
                .append("category", "Công nghệ thông tin")
                .append("publishYear", 2023)
                .append("pages", 450)
                .append("price", 250000.0)
                .append("totalCopies", 8)
                .append("availableCopies", 5) // 8 total - 3 borrowed (COPY001-6, 7, 8)
                .append("description", "Giáo trình lập trình Java toàn diện từ cơ bản đến nâng cao"));
            
            books.add(new Document("bookId", "BOOK002")
                .append("title", "Cấu trúc dữ liệu và giải thuật")
                .append("author", "Trần Thị Hương")
                .append("isbn", "978-604-1-00234-5")
                .append("category", "Công nghệ thông tin")
                .append("publishYear", 2022)
                .append("pages", 380)
                .append("price", 280000.0)
                .append("totalCopies", 7)
                .append("availableCopies", 6) // 7 total - 1 borrowed (COPY002-7)
                .append("description", "Giáo trình về cấu trúc dữ liệu và các thuật toán cơ bản"));
            
            books.add(new Document("bookId", "BOOK003")
                .append("title", "Machine Learning cơ bản")
                .append("author", "Lê Văn Đức")
                .append("isbn", "978-604-1-00345-6")
                .append("category", "Công nghệ thông tin")
                .append("publishYear", 2024)
                .append("pages", 520)
                .append("price", 350000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6) // 6 total - 0 borrowed
                .append("description", "Giới thiệu về machine learning và các ứng dụng thực tế"));
            
            books.add(new Document("bookId", "BOOK004")
                .append("title", "Lập trình Web với HTML, CSS, JavaScript")
                .append("author", "Phạm Thị Lan")
                .append("isbn", "978-604-1-00456-7")
                .append("category", "Công nghệ thông tin")
                .append("publishYear", 2023)
                .append("pages", 420)
                .append("price", 220000.0)
                .append("totalCopies", 7)
                .append("availableCopies", 6) // 7 total - 1 borrowed (COPY004-7)
                .append("description", "Hướng dẫn lập trình web frontend với HTML, CSS và JavaScript"));
            
            books.add(new Document("bookId", "BOOK005")
                .append("title", "Cơ sở dữ liệu MySQL")
                .append("author", "Vũ Văn Tùng")
                .append("isbn", "978-604-1-00567-8")
                .append("category", "Công nghệ thông tin")
                .append("publishYear", 2023)
                .append("pages", 380)
                .append("price", 200000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 5) // 6 total - 1 borrowed (COPY005-6)
                .append("description", "Giáo trình về cơ sở dữ liệu MySQL và SQL"));
            
            books.add(new Document("bookId", "BOOK006")
                .append("title", "Truyện Kiều")
                .append("author", "Nguyễn Du")
                .append("isbn", "978-604-1-00678-9")
                .append("category", "Văn học")
                .append("publishYear", 2020)
                .append("pages", 320)
                .append("price", 150000.0)
                .append("totalCopies", 7)
                .append("availableCopies", 6) // 7 total - 1 borrowed (COPY006-7)
                .append("description", "Tác phẩm văn học kinh điển Việt Nam"));
            
            books.add(new Document("bookId", "BOOK007")
                .append("title", "Chí Phèo")
                .append("author", "Nam Cao")
                .append("isbn", "978-604-1-00789-0")
                .append("category", "Văn học")
                .append("publishYear", 2021)
                .append("pages", 280)
                .append("price", 120000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6) // 6 total - 0 borrowed
                .append("description", "Tuyển tập truyện ngắn Nam Cao"));
            
            books.add(new Document("bookId", "BOOK008")
                .append("title", "Lịch Sử Việt Nam")
                .append("author", "Lê Thành Khôi")
                .append("isbn", "978-604-1-00890-1")
                .append("category", "Lịch sử")
                .append("publishYear", 2014)
                .append("pages", 1200)
                .append("price", 280000.0)
                .append("totalCopies", 5)
                .append("availableCopies", 5) // 5 total - 0 borrowed
                .append("description", "Công trình nghiên cứu toàn diện về lịch sử Việt Nam"));
            
            books.add(new Document("bookId", "BOOK009")
                .append("title", "Kinh tế học vĩ mô")
                .append("author", "Nguyễn Thị Hoa")
                .append("isbn", "978-604-1-00901-2")
                .append("category", "Kinh tế")
                .append("publishYear", 2023)
                .append("pages", 450)
                .append("price", 240000.0)
                .append("totalCopies", 7)
                .append("availableCopies", 6) // 7 total - 1 borrowed (COPY009-7)
                .append("description", "Giáo trình về kinh tế học vĩ mô"));
            
            books.add(new Document("bookId", "BOOK010")
                .append("title", "Tư Bản Trong Thế Kỷ 21")
                .append("author", "Thomas Piketty")
                .append("isbn", "978-604-1-01012-3")
                .append("category", "Kinh tế")
                .append("publishYear", 2013)
                .append("pages", 685)
                .append("price", 320000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 5) // 6 total - 1 borrowed (COPY010-6)
                .append("description", "Phân tích về bất bình đẳng thu nhập và của cải"));
            
            // Thêm sách Văn học
            books.add(new Document("bookId", "BOOK016")
                .append("title", "Số đỏ")
                .append("author", "Vũ Trọng Phụng")
                .append("isbn", "978-604-1-01678-9")
                .append("category", "Văn học")
                .append("publishYear", 2020)
                .append("pages", 300)
                .append("price", 130000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6) // 6 total - 0 borrowed
                .append("description", "Tiểu thuyết trào phúng nổi tiếng của Vũ Trọng Phụng"));
            
            books.add(new Document("bookId", "BOOK017")
                .append("title", "Dế Mèn phiêu lưu ký")
                .append("author", "Tô Hoài")
                .append("isbn", "978-604-1-01789-0")
                .append("category", "Văn học")
                .append("publishYear", 2021)
                .append("pages", 250)
                .append("price", 110000.0)
                .append("totalCopies", 7)
                .append("availableCopies", 7) // 7 total - 0 borrowed
                .append("description", "Truyện thiếu nhi kinh điển của nhà văn Tô Hoài"));
            
            books.add(new Document("bookId", "BOOK018")
                .append("title", "Những ngôi sao xa xôi")
                .append("author", "Lê Minh Khuê")
                .append("isbn", "978-604-1-01890-1")
                .append("category", "Văn học")
                .append("publishYear", 2022)
                .append("pages", 280)
                .append("price", 125000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6) // 6 total - 0 borrowed
                .append("description", "Tuyển tập truyện ngắn về cuộc sống và chiến tranh"));
            
            // Thêm sách Lịch sử
            books.add(new Document("bookId", "BOOK020")
                .append("title", "Lịch sử Đảng Cộng sản Việt Nam")
                .append("author", "Nguyễn Văn Tài")
                .append("isbn", "978-604-1-02012-3")
                .append("category", "Lịch sử")
                .append("publishYear", 2023)
                .append("pages", 600)
                .append("price", 250000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6) // 6 total - 0 borrowed
                .append("description", "Giáo trình về lịch sử Đảng Cộng sản Việt Nam"));
            
            books.add(new Document("bookId", "BOOK021")
                .append("title", "Lịch sử thế giới cổ đại")
                .append("author", "Trần Thị Hoa")
                .append("isbn", "978-604-1-02123-4")
                .append("category", "Lịch sử")
                .append("publishYear", 2022)
                .append("pages", 550)
                .append("price", 270000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6) // 6 total - 0 borrowed
                .append("description", "Khái quát về lịch sử các nền văn minh cổ đại"));
            
            books.add(new Document("bookId", "BOOK022")
                .append("title", "Chiến tranh Việt Nam - Những trang sử bi hùng")
                .append("author", "Lê Văn Đức")
                .append("isbn", "978-604-1-02234-5")
                .append("category", "Lịch sử")
                .append("publishYear", 2023)
                .append("pages", 800)
                .append("price", 300000.0)
                .append("totalCopies", 5)
                .append("availableCopies", 5) // 5 total - 0 borrowed
                .append("description", "Nghiên cứu về cuộc chiến tranh giải phóng dân tộc"));
            
            books.add(new Document("bookId", "BOOK023")
                .append("title", "Lịch sử văn minh Trung Quốc")
                .append("author", "Phạm Văn Minh")
                .append("isbn", "978-604-1-02345-6")
                .append("category", "Lịch sử")
                .append("publishYear", 2024)
                .append("pages", 480)
                .append("price", 240000.0)
                .append("totalCopies", 5)
                .append("availableCopies", 5) // 5 total - 0 borrowed
                .append("description", "Tìm hiểu về nền văn minh lâu đời của Trung Quốc"));
            
            // Thêm sách Kinh tế
            books.add(new Document("bookId", "BOOK024")
                .append("title", "Kinh tế học vi mô")
                .append("author", "Nguyễn Văn Hùng")
                .append("isbn", "978-604-1-02456-7")
                .append("category", "Kinh tế")
                .append("publishYear", 2023)
                .append("pages", 420)
                .append("price", 230000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6) // 6 total - 0 borrowed
                .append("description", "Giáo trình về kinh tế học vi mô"));
            
            books.add(new Document("bookId", "BOOK025")
                .append("title", "Quản trị kinh doanh hiện đại")
                .append("author", "Trần Thị Lan")
                .append("isbn", "978-604-1-02567-8")
                .append("category", "Kinh tế")
                .append("publishYear", 2024)
                .append("pages", 500)
                .append("price", 280000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6) // 6 total - 0 borrowed
                .append("description", "Các phương pháp quản trị kinh doanh hiện đại"));
            
            books.add(new Document("bookId", "BOOK026")
                .append("title", "Tài chính doanh nghiệp")
                .append("author", "Lê Văn Tuấn")
                .append("isbn", "978-604-1-02678-9")
                .append("category", "Kinh tế")
                .append("publishYear", 2023)
                .append("pages", 450)
                .append("price", 260000.0)
                .append("totalCopies", 7)
                .append("availableCopies", 7) // 7 total - 0 borrowed
                .append("description", "Giáo trình về quản lý tài chính trong doanh nghiệp"));
            
            // Thêm sách Y học (5 quyển)
            books.add(new Document("bookId", "BOOK027")
                .append("title", "Giải phẫu học cơ bản")
                .append("author", "Nguyễn Văn Hùng")
                .append("isbn", "978-604-1-02789-0")
                .append("category", "Y học")
                .append("publishYear", 2023)
                .append("pages", 600)
                .append("price", 350000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6)
                .append("description", "Giáo trình giải phẫu học cơ bản cho sinh viên y khoa"));
            
            books.add(new Document("bookId", "BOOK028")
                .append("title", "Sinh lý học người")
                .append("author", "Trần Thị Lan")
                .append("isbn", "978-604-1-02890-1")
                .append("category", "Y học")
                .append("publishYear", 2024)
                .append("pages", 550)
                .append("price", 320000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6)
                .append("description", "Giáo trình về sinh lý học người và các chức năng cơ thể"));
            
            books.add(new Document("bookId", "BOOK029")
                .append("title", "Dược lý học")
                .append("author", "Lê Văn Minh")
                .append("isbn", "978-604-1-02901-2")
                .append("category", "Y học")
                .append("publishYear", 2023)
                .append("pages", 500)
                .append("price", 300000.0)
                .append("totalCopies", 7)
                .append("availableCopies", 7)
                .append("description", "Giáo trình về dược lý học và tác dụng của thuốc"));
            
            books.add(new Document("bookId", "BOOK030")
                .append("title", "Bệnh lý học nội khoa")
                .append("author", "Phạm Thị Hoa")
                .append("isbn", "978-604-1-03012-3")
                .append("category", "Y học")
                .append("publishYear", 2024)
                .append("pages", 650)
                .append("price", 380000.0)
                .append("totalCopies", 5)
                .append("availableCopies", 5)
                .append("description", "Giáo trình về các bệnh lý nội khoa thường gặp"));
            
            books.add(new Document("bookId", "BOOK031")
                .append("title", "Chăm sóc sức khỏe cộng đồng")
                .append("author", "Vũ Văn Tuấn")
                .append("isbn", "978-604-1-03123-4")
                .append("category", "Y học")
                .append("publishYear", 2023)
                .append("pages", 420)
                .append("price", 250000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6)
                .append("description", "Giáo trình về chăm sóc sức khỏe cộng đồng và y tế công cộng"));
            
            // Thêm sách Kỹ thuật (5 quyển)
            books.add(new Document("bookId", "BOOK032")
                .append("title", "Kỹ thuật điện tử cơ bản")
                .append("author", "Nguyễn Văn An")
                .append("isbn", "978-604-1-03234-5")
                .append("category", "Kỹ thuật")
                .append("publishYear", 2023)
                .append("pages", 480)
                .append("price", 280000.0)
                .append("totalCopies", 7)
                .append("availableCopies", 7)
                .append("description", "Giáo trình về kỹ thuật điện tử cơ bản"));
            
            books.add(new Document("bookId", "BOOK033")
                .append("title", "Cơ khí chế tạo máy")
                .append("author", "Trần Văn Bình")
                .append("isbn", "978-604-1-03345-6")
                .append("category", "Kỹ thuật")
                .append("publishYear", 2024)
                .append("pages", 520)
                .append("price", 300000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6)
                .append("description", "Giáo trình về cơ khí chế tạo máy và công nghệ gia công"));
            
            books.add(new Document("bookId", "BOOK034")
                .append("title", "Kỹ thuật xây dựng")
                .append("author", "Lê Thị Mai")
                .append("isbn", "978-604-1-03456-7")
                .append("category", "Kỹ thuật")
                .append("publishYear", 2023)
                .append("pages", 600)
                .append("price", 320000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6)
                .append("description", "Giáo trình về kỹ thuật xây dựng và thiết kế công trình"));
            
            books.add(new Document("bookId", "BOOK035")
                .append("title", "Điện công nghiệp")
                .append("author", "Phạm Văn Hùng")
                .append("isbn", "978-604-1-03567-8")
                .append("category", "Kỹ thuật")
                .append("publishYear", 2024)
                .append("pages", 450)
                .append("price", 270000.0)
                .append("totalCopies", 7)
                .append("availableCopies", 7)
                .append("description", "Giáo trình về điện công nghiệp và hệ thống điện"));
            
            books.add(new Document("bookId", "BOOK036")
                .append("title", "Cơ học kỹ thuật")
                .append("author", "Nguyễn Thị Lan")
                .append("isbn", "978-604-1-03678-9")
                .append("category", "Kỹ thuật")
                .append("publishYear", 2023)
                .append("pages", 500)
                .append("price", 290000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6)
                .append("description", "Giáo trình về cơ học kỹ thuật và ứng dụng"));
            
            // Thêm sách Tâm lý học (5 quyển)
            books.add(new Document("bookId", "BOOK037")
                .append("title", "Tâm lý học đại cương")
                .append("author", "Trần Thị Hương")
                .append("isbn", "978-604-1-03789-0")
                .append("category", "Tâm lý học")
                .append("publishYear", 2023)
                .append("pages", 400)
                .append("price", 240000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6)
                .append("description", "Giáo trình tâm lý học đại cương và các khái niệm cơ bản"));
            
            books.add(new Document("bookId", "BOOK038")
                .append("title", "Tâm lý học phát triển")
                .append("author", "Lê Văn Đức")
                .append("isbn", "978-604-1-03890-1")
                .append("category", "Tâm lý học")
                .append("publishYear", 2024)
                .append("pages", 450)
                .append("price", 260000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6)
                .append("description", "Giáo trình về tâm lý học phát triển con người qua các giai đoạn"));
            
            books.add(new Document("bookId", "BOOK039")
                .append("title", "Tâm lý học xã hội")
                .append("author", "Phạm Thị An")
                .append("isbn", "978-604-1-03901-2")
                .append("category", "Tâm lý học")
                .append("publishYear", 2023)
                .append("pages", 420)
                .append("price", 250000.0)
                .append("totalCopies", 7)
                .append("availableCopies", 7)
                .append("description", "Giáo trình về tâm lý học xã hội và hành vi nhóm"));
            
            books.add(new Document("bookId", "BOOK040")
                .append("title", "Tâm lý học nhận thức")
                .append("author", "Nguyễn Văn Tài")
                .append("isbn", "978-604-1-04012-3")
                .append("category", "Tâm lý học")
                .append("publishYear", 2024)
                .append("pages", 480)
                .append("price", 280000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6)
                .append("description", "Giáo trình về tâm lý học nhận thức và quá trình tư duy"));
            
            books.add(new Document("bookId", "BOOK041")
                .append("title", "Tâm lý học lâm sàng")
                .append("author", "Trần Văn Minh")
                .append("isbn", "978-604-1-04123-4")
                .append("category", "Tâm lý học")
                .append("publishYear", 2023)
                .append("pages", 500)
                .append("price", 300000.0)
                .append("totalCopies", 6)
                .append("availableCopies", 6)
                .append("description", "Giáo trình về tâm lý học lâm sàng và điều trị tâm lý"));
            
            booksCollection.insertMany(books);
            System.out.println("Books imported: " + books.size() + " books");
            
            // Initialize Book Copies
            List<Document> copies = new ArrayList<>();
            
            // BOOK001 - 8 copies
            // Active borrows: COPY001-6, COPY001-7, COPY001-8
            for (int i = 1; i <= 8; i++) {
                boolean isBorrowed = (i == 6 || i == 7 || i == 8);
                copies.add(new Document("copyId", "COPY001-" + i)
                    .append("bookId", "BOOK001")
                    .append("status", isBorrowed ? "BORROWED" : "AVAILABLE")
                    .append("location", "Tầng 3 - Kệ 10 - Ngăn " + (10 + i))
                    .append("notes", i == 3 ? "Bìa sách hơi cũ" : ""));
            }
            
            // BOOK002 - 7 copies
            // Active borrows: COPY002-7
            for (int i = 1; i <= 7; i++) {
                boolean isBorrowed = (i == 7);
                copies.add(new Document("copyId", "COPY002-" + i)
                    .append("bookId", "BOOK002")
                    .append("status", isBorrowed ? "BORROWED" : "AVAILABLE")
                    .append("location", "Tầng 3 - Kệ 11 - Ngăn " + (5 + i))
                    .append("notes", ""));
            }
            
            // BOOK003 - 6 copies
            // Tất cả đều AVAILABLE
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY003-" + i)
                    .append("bookId", "BOOK003")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 3 - Kệ 12 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK004 - 7 copies
            // Active borrows: COPY004-7
            for (int i = 1; i <= 7; i++) {
                boolean isBorrowed = (i == 7);
                copies.add(new Document("copyId", "COPY004-" + i)
                    .append("bookId", "BOOK004")
                    .append("status", isBorrowed ? "BORROWED" : "AVAILABLE")
                    .append("location", "Tầng 3 - Kệ 13 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK005 - 6 copies
            // Active borrows: COPY005-6
            for (int i = 1; i <= 6; i++) {
                boolean isBorrowed = (i == 6);
                copies.add(new Document("copyId", "COPY005-" + i)
                    .append("bookId", "BOOK005")
                    .append("status", isBorrowed ? "BORROWED" : "AVAILABLE")
                    .append("location", "Tầng 3 - Kệ 14 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK006 - 7 copies
            // Active borrows: COPY006-7
            for (int i = 1; i <= 7; i++) {
                boolean isBorrowed = (i == 7);
                copies.add(new Document("copyId", "COPY006-" + i)
                    .append("bookId", "BOOK006")
                    .append("status", isBorrowed ? "BORROWED" : "AVAILABLE")
                    .append("location", "Tầng 4 - Kệ 21 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK007 - 6 copies
            // Tất cả đều AVAILABLE
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY007-" + i)
                    .append("bookId", "BOOK007")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 4 - Kệ 22 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK008 - 5 copies
            for (int i = 1; i <= 5; i++) {
                copies.add(new Document("copyId", "COPY008-" + i)
                    .append("bookId", "BOOK008")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 4 - Kệ 23 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK009 - 7 copies
            // Active borrows: COPY009-7
            for (int i = 1; i <= 7; i++) {
                boolean isBorrowed = (i == 7);
                copies.add(new Document("copyId", "COPY009-" + i)
                    .append("bookId", "BOOK009")
                    .append("status", isBorrowed ? "BORROWED" : "AVAILABLE")
                    .append("location", "Tầng 2 - Kệ 8 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK010 - 6 copies
            // Active borrows: COPY010-6
            for (int i = 1; i <= 6; i++) {
                boolean isBorrowed = (i == 6);
                copies.add(new Document("copyId", "COPY010-" + i)
                    .append("bookId", "BOOK010")
                    .append("status", isBorrowed ? "BORROWED" : "AVAILABLE")
                    .append("location", "Tầng 2 - Kệ 9 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK016 - 6 copies (all available)
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY016-" + i)
                    .append("bookId", "BOOK016")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 4 - Kệ 24 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK017 - 7 copies (all available)
            for (int i = 1; i <= 7; i++) {
                copies.add(new Document("copyId", "COPY017-" + i)
                    .append("bookId", "BOOK017")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 4 - Kệ 25 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK018 - 6 copies (all available)
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY018-" + i)
                    .append("bookId", "BOOK018")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 4 - Kệ 26 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK020 - 6 copies (all available)
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY020-" + i)
                    .append("bookId", "BOOK020")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 5 - Kệ 30 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK021 - 6 copies (all available)
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY021-" + i)
                    .append("bookId", "BOOK021")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 5 - Kệ 31 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK022 - 5 copies (all available)
            for (int i = 1; i <= 5; i++) {
                copies.add(new Document("copyId", "COPY022-" + i)
                    .append("bookId", "BOOK022")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 5 - Kệ 32 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK023 - 5 copies (all available)
            for (int i = 1; i <= 5; i++) {
                copies.add(new Document("copyId", "COPY023-" + i)
                    .append("bookId", "BOOK023")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 5 - Kệ 33 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK024 - 6 copies (all available)
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY024-" + i)
                    .append("bookId", "BOOK024")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 2 - Kệ 10 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK025 - 6 copies (all available)
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY025-" + i)
                    .append("bookId", "BOOK025")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 2 - Kệ 11 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK026 - 7 copies (all available)
            for (int i = 1; i <= 7; i++) {
                copies.add(new Document("copyId", "COPY026-" + i)
                    .append("bookId", "BOOK026")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 2 - Kệ 12 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK027 - 6 copies (all available)
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY027-" + i)
                    .append("bookId", "BOOK027")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 1 - Kệ 1 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK028 - 6 copies (all available)
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY028-" + i)
                    .append("bookId", "BOOK028")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 1 - Kệ 2 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK029 - 7 copies (all available)
            for (int i = 1; i <= 7; i++) {
                copies.add(new Document("copyId", "COPY029-" + i)
                    .append("bookId", "BOOK029")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 1 - Kệ 3 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK030 - 5 copies (all available)
            for (int i = 1; i <= 5; i++) {
                copies.add(new Document("copyId", "COPY030-" + i)
                    .append("bookId", "BOOK030")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 1 - Kệ 4 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK031 - 6 copies (all available)
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY031-" + i)
                    .append("bookId", "BOOK031")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 1 - Kệ 5 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK032 - 7 copies (all available)
            for (int i = 1; i <= 7; i++) {
                copies.add(new Document("copyId", "COPY032-" + i)
                    .append("bookId", "BOOK032")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 1 - Kệ 6 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK033 - 6 copies (all available)
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY033-" + i)
                    .append("bookId", "BOOK033")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 1 - Kệ 7 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK034 - 6 copies (all available)
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY034-" + i)
                    .append("bookId", "BOOK034")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 1 - Kệ 8 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK035 - 7 copies (all available)
            for (int i = 1; i <= 7; i++) {
                copies.add(new Document("copyId", "COPY035-" + i)
                    .append("bookId", "BOOK035")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 1 - Kệ 9 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK036 - 6 copies (all available)
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY036-" + i)
                    .append("bookId", "BOOK036")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 1 - Kệ 10 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK037 - 6 copies (all available)
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY037-" + i)
                    .append("bookId", "BOOK037")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 1 - Kệ 11 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK038 - 6 copies (all available)
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY038-" + i)
                    .append("bookId", "BOOK038")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 1 - Kệ 12 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK039 - 7 copies (all available)
            for (int i = 1; i <= 7; i++) {
                copies.add(new Document("copyId", "COPY039-" + i)
                    .append("bookId", "BOOK039")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 1 - Kệ 13 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK040 - 6 copies (all available)
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY040-" + i)
                    .append("bookId", "BOOK040")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 1 - Kệ 14 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            // BOOK041 - 6 copies (all available)
            for (int i = 1; i <= 6; i++) {
                copies.add(new Document("copyId", "COPY041-" + i)
                    .append("bookId", "BOOK041")
                    .append("status", "AVAILABLE")
                    .append("location", "Tầng 1 - Kệ 15 - Ngăn " + i)
                    .append("notes", ""));
            }
            
            bookCopiesCollection.insertMany(copies);
            System.out.println("Book copies imported: " + copies.size() + " copies");
            
            // Initialize Borrow Records
            List<Document> borrows = new ArrayList<>();
            long dayInMs = 24 * 60 * 60 * 1000;
            Calendar cal = Calendar.getInstance();
            cal.set(2025, 9, 12, 0, 0, 0); // Month is 0-based, so 9 = October
            cal.set(Calendar.MILLISECOND, 0);
            java.util.Date referenceDate = cal.getTime();
            
            java.util.function.Function<Integer, java.util.Date> refDate = (days) -> {
                return new java.util.Date(referenceDate.getTime() + days * dayInMs);
            };
            
            borrows.add(new Document("recordId", "record_e0b1f02c")
                .append("userId", "USER001")
                .append("bookId", "BOOK001")
                .append("copyId", "COPY001-6")
                .append("borrowDate", refDate.apply(-1))
                .append("dueDate", refDate.apply(13))
                .append("returnDate", null)
                .append("status", "BORROWING")
                .append("fine", 0.0));
            
            borrows.add(new Document("recordId", "record_ce3b9ba5")
                .append("userId", "USER001")
                .append("bookId", "BOOK005")
                .append("copyId", "COPY005-6")
                .append("borrowDate", refDate.apply(0))
                .append("dueDate", refDate.apply(14))
                .append("returnDate", null)
                .append("status", "BORROWING")
                .append("fine", 0.0));
            
            borrows.add(new Document("recordId", "record_8cda8fc0")
                .append("userId", "USER001")
                .append("bookId", "BOOK001")
                .append("copyId", "COPY001-7")
                .append("borrowDate", refDate.apply(0))
                .append("dueDate", refDate.apply(14))
                .append("returnDate", null)
                .append("status", "BORROWING")
                .append("fine", 0.0));
            
            borrows.add(new Document("recordId", "record_2375f616")
                .append("userId", "USER001")
                .append("bookId", "BOOK002")
                .append("copyId", "COPY002-7")
                .append("borrowDate", refDate.apply(-21))
                .append("dueDate", refDate.apply(-3))
                .append("returnDate", null)
                .append("status", "BORROWING")
                .append("fine", 0.0));
            
            borrows.add(new Document("recordId", "record_488f3bbf")
                .append("userId", "USER003")
                .append("bookId", "BOOK004")
                .append("copyId", "COPY004-7")
                .append("borrowDate", refDate.apply(-8))
                .append("dueDate", refDate.apply(6))
                .append("returnDate", null)
                .append("status", "BORROWING")
                .append("fine", 0.0));
            
            borrows.add(new Document("recordId", "record_b1c2d3e4")
                .append("userId", "USER001")
                .append("bookId", "BOOK001")
                .append("copyId", "COPY001-8")
                .append("borrowDate", refDate.apply(-5))
                .append("dueDate", refDate.apply(9))
                .append("returnDate", null)
                .append("status", "BORROWING")
                .append("fine", 0.0));
            
            borrows.add(new Document("recordId", "record_v1w2x3y4")
                .append("userId", "USER004")
                .append("bookId", "BOOK006")
                .append("copyId", "COPY006-7")
                .append("borrowDate", refDate.apply(-10))
                .append("dueDate", refDate.apply(4))
                .append("returnDate", null)
                .append("status", "BORROWING")
                .append("fine", 0.0));
            
            borrows.add(new Document("recordId", "record_h3i4j5k6")
                .append("userId", "USER007")
                .append("bookId", "BOOK009")
                .append("copyId", "COPY009-7")
                .append("borrowDate", refDate.apply(-7))
                .append("dueDate", refDate.apply(7))
                .append("returnDate", null)
                .append("status", "BORROWING")
                .append("fine", 0.0));
            
            borrows.add(new Document("recordId", "record_l7m8n9o0")
                .append("userId", "USER007")
                .append("bookId", "BOOK010")
                .append("copyId", "COPY010-6")
                .append("borrowDate", refDate.apply(-5))
                .append("dueDate", refDate.apply(9))
                .append("returnDate", null)
                .append("status", "BORROWING")
                .append("fine", 0.0));
            
            borrows.add(new Document("recordId", "record_t5u6v7w8")
                .append("userId", "USER008")
                .append("bookId", "BOOK008")
                .append("copyId", "COPY008-1")
                .append("borrowDate", refDate.apply(-30))
                .append("dueDate", refDate.apply(-10))
                .append("returnDate", refDate.apply(-10))
                .append("status", "RETURNED")
                .append("fine", 0.0));
            
            borrowRecordsCollection.insertMany(borrows);
            System.out.println("Borrow records imported: " + borrows.size() + " records");
            
            // Initialize Fines
            List<Document> fines = new ArrayList<>();
            java.util.Date now = new java.util.Date();
            
            fines.add(new Document("fineId", "FINE001")
                .append("recordId", "record_2375f616")
                .append("userId", "USER001")
                .append("bookId", "BOOK002")
                .append("reason", "Quá hạn trả sách")
                .append("amount", 80000.0)
                .append("status", "UNPAID")
                .append("dueDate", new java.util.Date(now.getTime() + 30 * dayInMs))
                .append("paidDate", null)
                .append("createdDate", new java.util.Date(System.currentTimeMillis() - 2L * 24 * 60 * 60 * 1000)));
            
            finesCollection.insertMany(fines);
            System.out.println("Fines imported: " + fines.size() + " fines");
            
            System.out.println("\n=== Data import completed successfully! ===");
            System.out.println("Summary:");
            System.out.println("- Categories: " + categories.size());
            System.out.println("- Users: " + (users.size() + 1));
            System.out.println("- Books: " + books.size());
            System.out.println("- Book Copies: " + copies.size());
            System.out.println("- Borrow Records: " + borrows.size());
            System.out.println("- Fines: " + fines.size());
            
        } catch (Exception e) {
            System.err.println("Error importing data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (mongoClient != null) {
                mongoClient.close();
            }
        }
    }
}

