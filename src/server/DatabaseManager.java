package server;

import model.Book;
import model.BookCopy;
import model.BorrowRecord;
import model.User;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

import com.mongodb.client.model.Updates;
import static com.mongodb.client.model.Updates.combine;

public class DatabaseManager {
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "library_db";
    
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> usersCollection;
    private MongoCollection<Document> booksCollection;
    private MongoCollection<Document> bookCopiesCollection;
    private MongoCollection<Document> borrowRecordsCollection;
    private MongoCollection<Document> categoriesCollection;
    private MongoCollection<Document> finesCollection;
    private MongoCollection<Document> notificationsCollection;
    private MongoCollection<Document> settingsCollection;
    
    public DatabaseManager() {
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
            
            usersCollection = database.getCollection("users");
            booksCollection = database.getCollection("books");
            bookCopiesCollection = database.getCollection("book_copies");
            borrowRecordsCollection = database.getCollection("borrow_records");
            categoriesCollection = database.getCollection("categories");
            finesCollection = database.getCollection("fines");
            notificationsCollection = database.getCollection("notifications");
            settingsCollection = database.getCollection("settings");
            
            // Initialize default settings if collection is empty
            initializeDefaultSettings();
            
            // Create indexes
            createIndexes();
            
            // Data will be loaded from MongoDB Compass, not initialized here
            // initializeData();
            
            System.out.println("Connected to MongoDB successfully!");
            System.out.println("Loading data from MongoDB Compass (library_db)...");
        } catch (Exception e) {
            System.err.println("Error connecting to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createIndexes() {
        try {
            // Create unique index on email
            com.mongodb.client.model.IndexOptions indexOptions = 
                new com.mongodb.client.model.IndexOptions().unique(true);
            usersCollection.createIndex(new Document("email", 1), indexOptions);
            usersCollection.createIndex(new Document("userId", 1), indexOptions);
            booksCollection.createIndex(new Document("bookId", 1), indexOptions);
            booksCollection.createIndex(new Document("isbn", 1));
        } catch (Exception e) {
            // Index might already exist, ignore
        }
    }
    
    // Data initialization disabled - data will be loaded from MongoDB Compass
    /*
    private void initializeData() {
        try {
            // Initialize Categories (4 categories as shown in MongoDB Compass)
            if (categoriesCollection.countDocuments() == 0) {
                List<Document> categories = new ArrayList<>();
                categories.add(new Document("categoryId", "CAT001").append("name", "Công nghệ thông tin").append("description", "Sách về lập trình, thuật toán, cấu trúc dữ liệu"));
                categories.add(new Document("categoryId", "CAT002").append("name", "Văn học").append("description", "Tiểu thuyết, truyện ngắn, thơ"));
                categories.add(new Document("categoryId", "CAT003").append("name", "Lịch sử").append("description", "Sách lịch sử Việt Nam và thế giới"));
                categories.add(new Document("categoryId", "CAT004").append("name", "Kinh tế").append("description", "Sách về kinh tế học, quản trị kinh doanh"));
                categoriesCollection.insertMany(categories);
                System.out.println("Categories initialized: 4 categories");
            }
            
            // Initialize Admin User
            if (usersCollection.countDocuments(eq("role", "ADMIN")) == 0) {
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
                System.out.println("Default admin created: dainam@dnu.edu.vn/dainam");
            }
            
            // Initialize Sample Users (4 users total: 1 admin + 3 regular users as shown in MongoDB Compass)
            if (usersCollection.countDocuments(eq("role", "USER")) == 0) {
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
                
                // Users: USER001-USER008 (8 users) + 1 admin = 9 users total
                
                usersCollection.insertMany(users);
                System.out.println("Sample users initialized: " + users.size() + " users (1 admin + " + users.size() + " users = " + (users.size() + 1) + " total)");
            }
            
            // Initialize Books (27 books total)
            if (booksCollection.countDocuments() == 0) {
                List<Document> books = new ArrayList<>();
                
                books.add(new Document("bookId", "BOOK001")
                    .append("title", "Lập trình Java từ cơ bản đến nâng cao")
                    .append("author", "Nguyễn Văn Minh")
                    .append("isbn", "978-604-1-00123-4")
                    .append("category", "Công nghệ thông tin")
                    .append("publishYear", 2023)
                    .append("pages", 450)
                    .append("price", 250000.0)
                    .append("totalCopies", 16)
                    .append("availableCopies", 13) // 16 total - 3 borrowed (COPY001-13, 14, 15)
                    .append("description", "Giáo trình lập trình Java toàn diện từ cơ bản đến nâng cao"));
                
                books.add(new Document("bookId", "BOOK002")
                    .append("title", "Cấu trúc dữ liệu và giải thuật")
                    .append("author", "Trần Thị Hương")
                    .append("isbn", "978-604-1-00234-5")
                    .append("category", "Công nghệ thông tin")
                    .append("publishYear", 2022)
                    .append("pages", 380)
                    .append("price", 280000.0)
                    .append("totalCopies", 13)
                    .append("availableCopies", 12) // 13 total - 1 borrowed (COPY002-12)
                    .append("description", "Giáo trình về cấu trúc dữ liệu và các thuật toán cơ bản"));
                
                books.add(new Document("bookId", "BOOK003")
                    .append("title", "Machine Learning cơ bản")
                    .append("author", "Lê Văn Đức")
                    .append("isbn", "978-604-1-00345-6")
                    .append("category", "Công nghệ thông tin")
                    .append("publishYear", 2024)
                    .append("pages", 520)
                    .append("price", 350000.0)
                    .append("totalCopies", 10)
                    .append("availableCopies", 10) // 10 total - 0 borrowed (all returned)
                    .append("description", "Giới thiệu về machine learning và các ứng dụng thực tế"));
                
                books.add(new Document("bookId", "BOOK004")
                    .append("title", "Lập trình Web với HTML, CSS, JavaScript")
                    .append("author", "Phạm Thị Lan")
                    .append("isbn", "978-604-1-00456-7")
                    .append("category", "Công nghệ thông tin")
                    .append("publishYear", 2023)
                    .append("pages", 420)
                    .append("price", 220000.0)
                    .append("totalCopies", 15)
                    .append("availableCopies", 14) // 15 total - 1 borrowed (COPY004-14)
                    .append("description", "Hướng dẫn lập trình web frontend với HTML, CSS và JavaScript"));
                
                books.add(new Document("bookId", "BOOK005")
                    .append("title", "Cơ sở dữ liệu MySQL")
                    .append("author", "Vũ Văn Tùng")
                    .append("isbn", "978-604-1-00567-8")
                    .append("category", "Công nghệ thông tin")
                    .append("publishYear", 2023)
                    .append("pages", 380)
                    .append("price", 200000.0)
                    .append("totalCopies", 14)
                    .append("availableCopies", 13)
                    .append("description", "Giáo trình về cơ sở dữ liệu MySQL và SQL"));
                
                // Thêm sách Văn học
                books.add(new Document("bookId", "BOOK006")
                    .append("title", "Truyện Kiều")
                    .append("author", "Nguyễn Du")
                    .append("isbn", "978-604-1-00678-9")
                    .append("category", "Văn học")
                    .append("publishYear", 2020)
                    .append("pages", 320)
                    .append("price", 150000.0)
                    .append("totalCopies", 15)
                    .append("availableCopies", 14)
                    .append("description", "Tác phẩm văn học kinh điển Việt Nam"));
                
                books.add(new Document("bookId", "BOOK007")
                    .append("title", "Chí Phèo")
                    .append("author", "Nam Cao")
                    .append("isbn", "978-604-1-00789-0")
                    .append("category", "Văn học")
                    .append("publishYear", 2021)
                    .append("pages", 280)
                    .append("price", 120000.0)
                    .append("totalCopies", 12)
                    .append("availableCopies", 12) // 12 total - 0 borrowed (COPY007-12 đã trả)
                    .append("description", "Tuyển tập truyện ngắn Nam Cao"));
                
                // Thêm sách Lịch sử
                books.add(new Document("bookId", "BOOK008")
                    .append("title", "Lịch Sử Việt Nam")
                    .append("author", "Lê Thành Khôi")
                    .append("isbn", "978-604-1-00890-1")
                    .append("category", "Lịch sử")
                    .append("publishYear", 2014)
                    .append("pages", 1200)
                    .append("price", 280000.0)
                    .append("totalCopies", 4)
                    .append("availableCopies", 4)
                    .append("description", "Công trình nghiên cứu toàn diện về lịch sử Việt Nam"));
                
                // Thêm sách Kinh tế
                books.add(new Document("bookId", "BOOK009")
                    .append("title", "Kinh tế học vĩ mô")
                    .append("author", "Nguyễn Thị Hoa")
                    .append("isbn", "978-604-1-00901-2")
                    .append("category", "Kinh tế")
                    .append("publishYear", 2023)
                    .append("pages", 450)
                    .append("price", 240000.0)
                    .append("totalCopies", 12)
                    .append("availableCopies", 11)
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
                    .append("availableCopies", 5)
                    .append("description", "Phân tích về bất bình đẳng thu nhập và của cải"));
                
                // Thêm sách Công nghệ thông tin
                books.add(new Document("bookId", "BOOK011")
                    .append("title", "Trí tuệ nhân tạo")
                    .append("author", "Lê Thị Mai")
                    .append("isbn", "978-604-1-01123-4")
                    .append("category", "Công nghệ thông tin")
                    .append("publishYear", 2023)
                    .append("pages", 480)
                    .append("price", 380000.0)
                    .append("totalCopies", 9)
                    .append("availableCopies", 8)
                    .append("description", "Giáo trình về trí tuệ nhân tạo và các ứng dụng"));
                
                books.add(new Document("bookId", "BOOK012")
                    .append("title", "An toàn thông tin mạng")
                    .append("author", "Trần Văn Bình")
                    .append("isbn", "978-604-1-01234-5")
                    .append("category", "Công nghệ thông tin")
                    .append("publishYear", 2024)
                    .append("pages", 400)
                    .append("price", 320000.0)
                    .append("totalCopies", 10)
                    .append("availableCopies", 9)
                    .append("description", "Giáo trình về an toàn thông tin và bảo mật mạng"));
                
                // Thêm sách Công nghệ thông tin
                books.add(new Document("bookId", "BOOK013")
                    .append("title", "Lập trình Python cho người mới bắt đầu")
                    .append("author", "Nguyễn Thị An")
                    .append("isbn", "978-604-1-01345-6")
                    .append("category", "Công nghệ thông tin")
                    .append("publishYear", 2024)
                    .append("pages", 350)
                    .append("price", 180000.0)
                    .append("totalCopies", 12)
                    .append("availableCopies", 12)
                    .append("description", "Giáo trình Python cơ bản dành cho người mới học lập trình"));
                
                books.add(new Document("bookId", "BOOK014")
                    .append("title", "Lập trình Android với Kotlin")
                    .append("author", "Phạm Văn Hùng")
                    .append("isbn", "978-604-1-01456-7")
                    .append("category", "Công nghệ thông tin")
                    .append("publishYear", 2023)
                    .append("pages", 500)
                    .append("price", 300000.0)
                    .append("totalCopies", 11)
                    .append("availableCopies", 11)
                    .append("description", "Hướng dẫn phát triển ứng dụng Android sử dụng Kotlin"));
                
                books.add(new Document("bookId", "BOOK015")
                    .append("title", "Blockchain và Bitcoin cơ bản")
                    .append("author", "Lê Văn Thành")
                    .append("isbn", "978-604-1-01567-8")
                    .append("category", "Công nghệ thông tin")
                    .append("publishYear", 2024)
                    .append("pages", 380)
                    .append("price", 260000.0)
                    .append("totalCopies", 8)
                    .append("availableCopies", 8)
                    .append("description", "Giới thiệu về công nghệ blockchain và tiền điện tử"));
                
                // Thêm sách Văn học
                books.add(new Document("bookId", "BOOK016")
                    .append("title", "Số đỏ")
                    .append("author", "Vũ Trọng Phụng")
                    .append("isbn", "978-604-1-01678-9")
                    .append("category", "Văn học")
                    .append("publishYear", 2020)
                    .append("pages", 300)
                    .append("price", 130000.0)
                    .append("totalCopies", 14)
                    .append("availableCopies", 14)
                    .append("description", "Tiểu thuyết trào phúng nổi tiếng của Vũ Trọng Phụng"));
                
                books.add(new Document("bookId", "BOOK017")
                    .append("title", "Dế Mèn phiêu lưu ký")
                    .append("author", "Tô Hoài")
                    .append("isbn", "978-604-1-01789-0")
                    .append("category", "Văn học")
                    .append("publishYear", 2021)
                    .append("pages", 250)
                    .append("price", 110000.0)
                    .append("totalCopies", 16)
                    .append("availableCopies", 16)
                    .append("description", "Truyện thiếu nhi kinh điển của nhà văn Tô Hoài"));
                
                books.add(new Document("bookId", "BOOK018")
                    .append("title", "Những ngôi sao xa xôi")
                    .append("author", "Lê Minh Khuê")
                    .append("isbn", "978-604-1-01890-1")
                    .append("category", "Văn học")
                    .append("publishYear", 2022)
                    .append("pages", 280)
                    .append("price", 125000.0)
                    .append("totalCopies", 13)
                    .append("availableCopies", 13)
                    .append("description", "Tuyển tập truyện ngắn về cuộc sống và chiến tranh"));
                
                books.add(new Document("bookId", "BOOK019")
                    .append("title", "Vợ nhặt")
                    .append("author", "Kim Lân")
                    .append("isbn", "978-604-1-01901-2")
                    .append("category", "Văn học")
                    .append("publishYear", 2021)
                    .append("pages", 200)
                    .append("price", 100000.0)
                    .append("totalCopies", 15)
                    .append("availableCopies", 15)
                    .append("description", "Truyện ngắn nổi tiếng về nạn đói năm 1945"));
                
                // Thêm sách Lịch sử
                books.add(new Document("bookId", "BOOK020")
                    .append("title", "Lịch sử Đảng Cộng sản Việt Nam")
                    .append("author", "Nguyễn Văn Tài")
                    .append("isbn", "978-604-1-02012-3")
                    .append("category", "Lịch sử")
                    .append("publishYear", 2023)
                    .append("pages", 600)
                    .append("price", 250000.0)
                    .append("totalCopies", 8)
                    .append("availableCopies", 8)
                    .append("description", "Giáo trình về lịch sử Đảng Cộng sản Việt Nam"));
                
                books.add(new Document("bookId", "BOOK021")
                    .append("title", "Lịch sử thế giới cổ đại")
                    .append("author", "Trần Thị Hoa")
                    .append("isbn", "978-604-1-02123-4")
                    .append("category", "Lịch sử")
                    .append("publishYear", 2022)
                    .append("pages", 550)
                    .append("price", 270000.0)
                    .append("totalCopies", 7)
                    .append("availableCopies", 7)
                    .append("description", "Khái quát về lịch sử các nền văn minh cổ đại"));
                
                books.add(new Document("bookId", "BOOK022")
                    .append("title", "Chiến tranh Việt Nam - Những trang sử bi hùng")
                    .append("author", "Lê Văn Đức")
                    .append("isbn", "978-604-1-02234-5")
                    .append("category", "Lịch sử")
                    .append("publishYear", 2023)
                    .append("pages", 800)
                    .append("price", 300000.0)
                    .append("totalCopies", 6)
                    .append("availableCopies", 6)
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
                    .append("availableCopies", 5)
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
                    .append("totalCopies", 10)
                    .append("availableCopies", 10)
                    .append("description", "Giáo trình về kinh tế học vi mô"));
                
                books.add(new Document("bookId", "BOOK025")
                    .append("title", "Quản trị kinh doanh hiện đại")
                    .append("author", "Trần Thị Lan")
                    .append("isbn", "978-604-1-02567-8")
                    .append("category", "Kinh tế")
                    .append("publishYear", 2024)
                    .append("pages", 500)
                    .append("price", 280000.0)
                    .append("totalCopies", 9)
                    .append("availableCopies", 9)
                    .append("description", "Các phương pháp quản trị kinh doanh hiện đại"));
                
                books.add(new Document("bookId", "BOOK026")
                    .append("title", "Tài chính doanh nghiệp")
                    .append("author", "Lê Văn Tuấn")
                    .append("isbn", "978-604-1-02678-9")
                    .append("category", "Kinh tế")
                    .append("publishYear", 2023)
                    .append("pages", 450)
                    .append("price", 260000.0)
                    .append("totalCopies", 11)
                    .append("availableCopies", 11)
                    .append("description", "Giáo trình về quản lý tài chính trong doanh nghiệp"));
                
                books.add(new Document("bookId", "BOOK027")
                    .append("title", "Marketing căn bản")
                    .append("author", "Nguyễn Thị Mai")
                    .append("isbn", "978-604-1-02789-0")
                    .append("category", "Kinh tế")
                    .append("publishYear", 2024)
                    .append("pages", 380)
                    .append("price", 220000.0)
                    .append("totalCopies", 13)
                    .append("availableCopies", 13)
                    .append("description", "Nhập môn về marketing và các chiến lược tiếp thị"));
                
                booksCollection.insertMany(books);
                System.out.println("Sample books initialized: " + books.size() + " books");
            }
            
            // Initialize Book Copies (85 copies total as shown in MongoDB Compass)
            // Distribution: BOOK001=16, BOOK002=13, BOOK003=10, BOOK004=15, BOOK005=14, BOOK006=8, BOOK007=10, BOOK008=9 = 95 copies
            // But MongoDB Compass shows 85, so we adjust: BOOK001=16, BOOK002=13, BOOK003=10, BOOK004=15, BOOK005=14, BOOK006=8, BOOK007=9, BOOK008=4 = 89
            // Actually, based on books: BOOK001=16, BOOK002=13, BOOK003=10, BOOK004=15, BOOK005=14 = 68 copies
            // To reach 85: BOOK001=16, BOOK002=13, BOOK003=10, BOOK004=15, BOOK005=14, plus some from other books
            // Let's match exactly: BOOK001=16, BOOK002=13, BOOK003=10, BOOK004=15, BOOK005=14, BOOK006=8, BOOK007=10, BOOK008=9 = 85
            if (bookCopiesCollection.countDocuments() == 0) {
                List<Document> copies = new ArrayList<>();
                
                // BOOK001 - 16 copies (12 available, 4 borrowed to match availableCopies=12, totalCopies=16)
                for (int i = 1; i <= 16; i++) {
                    copies.add(new Document("copyId", "COPY001-" + i)
                        .append("bookId", "BOOK001")
                        .append("status", i <= 12 ? "AVAILABLE" : "BORROWED")
                        .append("location", "Tầng 3 - Kệ 10 - Ngăn " + (10 + i))
                        .append("notes", i == 3 ? "Bìa sách hơi cũ" : ""));
                }
                
                // BOOK002 - 13 copies (11 available, 2 borrowed to match availableCopies=11, totalCopies=13)
                for (int i = 1; i <= 13; i++) {
                    copies.add(new Document("copyId", "COPY002-" + i)
                        .append("bookId", "BOOK002")
                        .append("status", i <= 11 ? "AVAILABLE" : "BORROWED")
                        .append("location", "Tầng 3 - Kệ 11 - Ngăn " + (5 + i))
                        .append("notes", ""));
                }
                
                // BOOK003 - 10 copies (8 available, 2 borrowed to match availableCopies=8, totalCopies=10)
                for (int i = 1; i <= 10; i++) {
                    copies.add(new Document("copyId", "COPY003-" + i)
                        .append("bookId", "BOOK003")
                        .append("status", i <= 8 ? "AVAILABLE" : "BORROWED")
                        .append("location", "Tầng 3 - Kệ 12 - Ngăn " + i)
                        .append("notes", ""));
                }
                
                // BOOK004 - 15 copies (13 available, 2 borrowed to match availableCopies=13, totalCopies=15)
                for (int i = 1; i <= 15; i++) {
                    copies.add(new Document("copyId", "COPY004-" + i)
                        .append("bookId", "BOOK004")
                        .append("status", i <= 13 ? "AVAILABLE" : "BORROWED")
                        .append("location", "Tầng 3 - Kệ 13 - Ngăn " + i)
                        .append("notes", ""));
                }
                
                // BOOK005 - 14 copies (13 available, 1 borrowed to match availableCopies=13, totalCopies=14)
                for (int i = 1; i <= 14; i++) {
                    copies.add(new Document("copyId", "COPY005-" + i)
                        .append("bookId", "BOOK005")
                        .append("status", i <= 13 ? "AVAILABLE" : "BORROWED")
                        .append("location", "Tầng 3 - Kệ 14 - Ngăn " + i)
                        .append("notes", ""));
                }
                
                // BOOK006 - 15 copies (14 available, 1 borrowed)
                for (int i = 1; i <= 15; i++) {
                    copies.add(new Document("copyId", "COPY006-" + i)
                        .append("bookId", "BOOK006")
                        .append("status", i <= 14 ? "AVAILABLE" : "BORROWED")
                        .append("location", "Tầng 4 - Kệ 21 - Ngăn " + i)
                        .append("notes", ""));
                }
                
                // BOOK007 - 12 copies (11 available, 1 borrowed)
                for (int i = 1; i <= 12; i++) {
                    copies.add(new Document("copyId", "COPY007-" + i)
                        .append("bookId", "BOOK007")
                        .append("status", i <= 11 ? "AVAILABLE" : "BORROWED")
                        .append("location", "Tầng 4 - Kệ 22 - Ngăn " + i)
                        .append("notes", ""));
                }
                
                // BOOK008 - 4 copies (all available)
                for (int i = 1; i <= 4; i++) {
                    copies.add(new Document("copyId", "COPY008-" + i)
                        .append("bookId", "BOOK008")
                        .append("status", "AVAILABLE")
                        .append("location", "Tầng 4 - Kệ 23 - Ngăn " + i)
                        .append("notes", ""));
                }
                
                // BOOK009 - 12 copies (11 available, 1 borrowed)
                for (int i = 1; i <= 12; i++) {
                    copies.add(new Document("copyId", "COPY009-" + i)
                        .append("bookId", "BOOK009")
                        .append("status", i <= 11 ? "AVAILABLE" : "BORROWED")
                        .append("location", "Tầng 2 - Kệ 8 - Ngăn " + i)
                        .append("notes", ""));
                }
                
                // BOOK010 - 6 copies (5 available, 1 borrowed)
                for (int i = 1; i <= 6; i++) {
                    copies.add(new Document("copyId", "COPY010-" + i)
                        .append("bookId", "BOOK010")
                        .append("status", i <= 5 ? "AVAILABLE" : "BORROWED")
                        .append("location", "Tầng 2 - Kệ 9 - Ngăn " + i)
                        .append("notes", ""));
                }
                
                // BOOK011 - 9 copies (8 available, 1 borrowed)
                for (int i = 1; i <= 9; i++) {
                    copies.add(new Document("copyId", "COPY011-" + i)
                        .append("bookId", "BOOK011")
                        .append("status", i <= 8 ? "AVAILABLE" : "BORROWED")
                        .append("location", "Tầng 2 - Kệ 7 - Ngăn " + i)
                        .append("notes", ""));
                }
                
                // BOOK012 - 10 copies (9 available, 1 borrowed)
                for (int i = 1; i <= 10; i++) {
                    copies.add(new Document("copyId", "COPY012-" + i)
                        .append("bookId", "BOOK012")
                        .append("status", i <= 9 ? "AVAILABLE" : "BORROWED")
                        .append("location", "Tầng 2 - Kệ 6 - Ngăn " + i)
                        .append("notes", ""));
                }
                
                // Total: 16 + 13 + 10 + 15 + 14 + 15 + 12 + 4 + 12 + 6 + 9 + 10 = 138 copies
                
                bookCopiesCollection.insertMany(copies);
                System.out.println("Book copies initialized: " + copies.size() + " copies");
            }
            
            // Initialize Borrow Records (11 records as shown in MongoDB Compass)
            if (borrowRecordsCollection.countDocuments() == 0) {
                List<Document> borrows = new ArrayList<>();
                long dayInMs = 24 * 60 * 60 * 1000;
                // Reference date: 12/10/2025
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(2025, 9, 12, 0, 0, 0); // Month is 0-based, so 9 = October
                cal.set(java.util.Calendar.MILLISECOND, 0);
                java.util.Date referenceDate = cal.getTime();
                
                // Helper function to create date relative to reference
                java.util.function.Function<Integer, java.util.Date> refDate = (days) -> {
                    return new java.util.Date(referenceDate.getTime() + days * dayInMs);
                };
                
                // USER001 (nghia) - 4 active borrows
                borrows.add(new Document("recordId", "record_e0b1f02c")
                    .append("userId", "USER001")
                    .append("bookId", "BOOK001")
                    .append("copyId", "COPY001-13")
                    .append("borrowDate", refDate.apply(-1)) // 11/10/2025
                    .append("dueDate", refDate.apply(13)) // 25/10/2025
                    .append("returnDate", null)
                    .append("status", "BORROWING")
                    .append("fine", 0.0));
                
                borrows.add(new Document("recordId", "record_ce3b9ba5")
                    .append("userId", "USER001")
                    .append("bookId", "BOOK005")
                    .append("copyId", "COPY005-14")
                    .append("borrowDate", refDate.apply(0)) // 12/10/2025
                    .append("dueDate", refDate.apply(14)) // 26/10/2025
                    .append("returnDate", null)
                    .append("status", "BORROWING")
                    .append("fine", 0.0));
                
                borrows.add(new Document("recordId", "record_8cda8fc0")
                    .append("userId", "USER001")
                    .append("bookId", "BOOK001")
                    .append("copyId", "COPY001-14")
                    .append("borrowDate", refDate.apply(0)) // 12/10/2025
                    .append("dueDate", refDate.apply(14)) // 26/10/2025
                    .append("returnDate", null)
                    .append("status", "BORROWING")
                    .append("fine", 0.0));
                
                borrows.add(new Document("recordId", "record_2375f616")
                    .append("userId", "USER001")
                    .append("bookId", "BOOK002")
                    .append("copyId", "COPY002-12")
                    .append("borrowDate", refDate.apply(-21)) // 21/09/2025
                    .append("dueDate", refDate.apply(-3)) // 19/10/2025 (overdue)
                    .append("returnDate", null)
                    .append("status", "BORROWING")
                    .append("fine", 0.0));
                
                // USER002 (ngoc) - 1 returned
                borrows.add(new Document("recordId", "record_a140c990")
                    .append("userId", "USER002")
                    .append("bookId", "BOOK003")
                    .append("copyId", "COPY003-9")
                    .append("borrowDate", refDate.apply(-25)) // 17/09/2025
                    .append("dueDate", refDate.apply(14)) // 26/10/2025
                    .append("returnDate", refDate.apply(14)) // 26/10/2025
                    .append("status", "RETURNED")
                    .append("fine", 0.0));
                
                // USER003 (minh) - 1 active borrow
                borrows.add(new Document("recordId", "record_488f3bbf")
                    .append("userId", "USER003")
                    .append("bookId", "BOOK004")
                    .append("copyId", "COPY004-14")
                    .append("borrowDate", refDate.apply(-8)) // 04/10/2025
                    .append("dueDate", refDate.apply(6)) // 18/10/2025
                    .append("returnDate", null)
                    .append("status", "BORROWING")
                    .append("fine", 0.0));
                
                // Additional records to reach 11 total (as shown in MongoDB Compass)
                // USER001 - 1 more borrow
                borrows.add(new Document("recordId", "record_b1c2d3e4")
                    .append("userId", "USER001")
                    .append("bookId", "BOOK001")
                    .append("copyId", "COPY001-15")
                    .append("borrowDate", refDate.apply(-5)) // 07/10/2025
                    .append("dueDate", refDate.apply(9)) // 21/10/2025
                    .append("returnDate", null)
                    .append("status", "BORROWING")
                    .append("fine", 0.0));
                
                // USER002 - 1 more returned
                borrows.add(new Document("recordId", "record_f5g6h7i8")
                    .append("userId", "USER002")
                    .append("bookId", "BOOK002")
                    .append("copyId", "COPY002-13")
                    .append("borrowDate", refDate.apply(-30)) // 12/09/2025
                    .append("dueDate", refDate.apply(-10)) // 02/10/2025
                    .append("returnDate", refDate.apply(-10)) // 02/10/2025
                    .append("status", "RETURNED")
                    .append("fine", 0.0));
                
                // USER003 - 1 more returned
                borrows.add(new Document("recordId", "record_j9k0l1m2")
                    .append("userId", "USER003")
                    .append("bookId", "BOOK003")
                    .append("copyId", "COPY003-10")
                    .append("borrowDate", refDate.apply(-20)) // 22/09/2025
                    .append("dueDate", refDate.apply(14)) // 26/10/2025
                    .append("returnDate", refDate.apply(14)) // 26/10/2025
                    .append("status", "RETURNED")
                    .append("fine", 0.0));
                
                // USER003 - 1 more returned
                borrows.add(new Document("recordId", "record_n3o4p5q6")
                    .append("userId", "USER003")
                    .append("bookId", "BOOK005")
                    .append("copyId", "COPY005-13")
                    .append("borrowDate", refDate.apply(-15)) // 27/09/2025
                    .append("dueDate", refDate.apply(14)) // 26/10/2025
                    .append("returnDate", refDate.apply(14)) // 26/10/2025
                    .append("status", "RETURNED")
                    .append("fine", 0.0));
                
                // USER001 - 1 more returned
                borrows.add(new Document("recordId", "record_r7s8t9u0")
                    .append("userId", "USER001")
                    .append("bookId", "BOOK004")
                    .append("copyId", "COPY004-15")
                    .append("borrowDate", refDate.apply(-35)) // 07/09/2025
                    .append("dueDate", refDate.apply(-15)) // 27/09/2025
                    .append("returnDate", refDate.apply(-15)) // 27/09/2025
                    .append("status", "RETURNED")
                    .append("fine", 0.0));
                
                // USER004 (linh) - borrow records
                borrows.add(new Document("recordId", "record_v1w2x3y4")
                    .append("userId", "USER004")
                    .append("bookId", "BOOK006")
                    .append("copyId", "COPY006-15")
                    .append("borrowDate", refDate.apply(-10)) // 02/10/2025
                    .append("dueDate", refDate.apply(4)) // 16/10/2025
                    .append("returnDate", null)
                    .append("status", "BORROWING")
                    .append("fine", 0.0));
                
                // USER005 (hoang) - borrow records
                borrows.add(new Document("recordId", "record_z5a6b7c8")
                    .append("userId", "USER005")
                    .append("bookId", "BOOK011")
                    .append("copyId", "COPY011-9")
                    .append("borrowDate", refDate.apply(-12)) // 30/09/2025
                    .append("dueDate", refDate.apply(2)) // 14/10/2025
                    .append("returnDate", null)
                    .append("status", "BORROWING")
                    .append("fine", 0.0));
                
                borrows.add(new Document("recordId", "record_d9e0f1g2")
                    .append("userId", "USER005")
                    .append("bookId", "BOOK012")
                    .append("copyId", "COPY012-10")
                    .append("borrowDate", refDate.apply(-15)) // 27/09/2025
                    .append("dueDate", refDate.apply(-1)) // 11/10/2025
                    .append("returnDate", null)
                    .append("status", "BORROWING")
                    .append("fine", 0.0));
                
                // USER007 (tuan) - borrow records
                borrows.add(new Document("recordId", "record_h3i4j5k6")
                    .append("userId", "USER007")
                    .append("bookId", "BOOK009")
                    .append("copyId", "COPY009-12")
                    .append("borrowDate", refDate.apply(-7)) // 05/10/2025
                    .append("dueDate", refDate.apply(7)) // 19/10/2025
                    .append("returnDate", null)
                    .append("status", "BORROWING")
                    .append("fine", 0.0));
                
                borrows.add(new Document("recordId", "record_l7m8n9o0")
                    .append("userId", "USER007")
                    .append("bookId", "BOOK010")
                    .append("copyId", "COPY010-6")
                    .append("borrowDate", refDate.apply(-5)) // 07/10/2025
                    .append("dueDate", refDate.apply(9)) // 21/10/2025
                    .append("returnDate", null)
                    .append("status", "BORROWING")
                    .append("fine", 0.0));
                
                // USER008 (duc) - returned records
                borrows.add(new Document("recordId", "record_p1q2r3s4")
                    .append("userId", "USER008")
                    .append("bookId", "BOOK007")
                    .append("copyId", "COPY007-12")
                    .append("borrowDate", refDate.apply(-40)) // 02/09/2025
                    .append("dueDate", refDate.apply(-20)) // 22/09/2025
                    .append("returnDate", refDate.apply(-18)) // 24/09/2025
                    .append("status", "RETURNED")
                    .append("fine", 0.0));
                
                borrows.add(new Document("recordId", "record_t5u6v7w8")
                    .append("userId", "USER008")
                    .append("bookId", "BOOK008")
                    .append("copyId", "COPY008-1")
                    .append("borrowDate", refDate.apply(-30)) // 12/09/2025
                    .append("dueDate", refDate.apply(-10)) // 02/10/2025
                    .append("returnDate", refDate.apply(-10)) // 02/10/2025
                    .append("status", "RETURNED")
                    .append("fine", 0.0));
                
                // Total: 11 + 7 = 18 records
                
                borrowRecordsCollection.insertMany(borrows);
                System.out.println("Borrow records initialized: " + borrows.size() + " records");
            }
            
            // Initialize Fines (4 fines as shown in MongoDB Compass)
            if (finesCollection.countDocuments() == 0) {
                List<Document> fines = new ArrayList<>();
                long dayInMs = 24 * 60 * 60 * 1000;
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
                
                fines.add(new Document("fineId", "FINE002")
                    .append("recordId", "record_a140c990")
                    .append("userId", "USER002")
                    .append("bookId", "BOOK003")
                    .append("reason", "Quá hạn trả sách - 2 ngày")
                    .append("amount", 20000.0)
                    .append("status", "PAID")
                    .append("dueDate", new java.util.Date(now.getTime() - 16 * dayInMs))
                    .append("paidDate", new java.util.Date(now.getTime() - 14 * dayInMs))
                    .append("createdDate", new java.util.Date(now.getTime() - 16 * dayInMs)));
                
                fines.add(new Document("fineId", "FINE003")
                    .append("recordId", "record_j9k0l1m2")
                    .append("userId", "USER003")
                    .append("bookId", "BOOK003")
                    .append("reason", "Quá hạn trả sách - 1 ngày")
                    .append("amount", 10000.0)
                    .append("status", "PAID")
                    .append("dueDate", new java.util.Date(now.getTime() - 8 * dayInMs))
                    .append("paidDate", new java.util.Date(now.getTime() - 7 * dayInMs))
                    .append("createdDate", new java.util.Date(now.getTime() - 8 * dayInMs)));
                
                fines.add(new Document("fineId", "FINE004")
                    .append("recordId", "record_r7s8t9u0")
                    .append("userId", "USER001")
                    .append("bookId", "BOOK004")
                    .append("reason", "Quá hạn trả sách - 3 ngày")
                    .append("amount", 30000.0)
                    .append("status", "PAID")
                    .append("dueDate", new java.util.Date(now.getTime() - 20 * dayInMs))
                    .append("paidDate", new java.util.Date(now.getTime() - 17 * dayInMs))
                    .append("createdDate", new java.util.Date(now.getTime() - 20 * dayInMs)));
                
                // Thêm fines cho các users khác
                fines.add(new Document("fineId", "FINE005")
                    .append("recordId", "record_d9e0f1g2")
                    .append("userId", "USER005")
                    .append("bookId", "BOOK012")
                    .append("reason", "Quá hạn trả sách - 1 ngày")
                    .append("amount", 10000.0)
                    .append("status", "UNPAID")
                    .append("dueDate", new java.util.Date(now.getTime() + 30 * dayInMs))
                    .append("paidDate", null)
                    .append("createdDate", new java.util.Date(now.getTime() - 1 * dayInMs)));
                
                fines.add(new Document("fineId", "FINE006")
                    .append("recordId", "record_p1q2r3s4")
                    .append("userId", "USER008")
                    .append("bookId", "BOOK007")
                    .append("reason", "Quá hạn trả sách - 2 ngày")
                    .append("amount", 20000.0)
                    .append("status", "PAID")
                    .append("dueDate", new java.util.Date(now.getTime() - 18 * dayInMs))
                    .append("paidDate", new java.util.Date(now.getTime() - 16 * dayInMs))
                    .append("createdDate", new java.util.Date(now.getTime() - 20 * dayInMs)));
                
                // Total: 6 fines
                
                finesCollection.insertMany(fines);
                System.out.println("Fines initialized: " + fines.size() + " fines");
            }
            
        } catch (Exception e) {
            System.err.println("Error initializing data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    */
    
    // User operations
    public User login(String email, String password) {
        try {
            Document doc = usersCollection.find(
                and(eq("email", email), eq("password", password))
            ).first();
            
            if (doc != null) {
                // Check if user is locked
                String status = doc.getString("status");
                if (status != null && "LOCKED".equals(status)) {
                    // User is locked, return null to deny login
                    return null;
                }
                
                User user = documentToUser(doc);
                // Update last login
                usersCollection.updateOne(eq("_id", doc.getObjectId("_id")), 
                    set("lastLogin", new java.util.Date()));
                usersCollection.updateOne(eq("_id", doc.getObjectId("_id")), 
                    set("isOnline", true));
                return user;
            }
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
        }
        return null;
    }
    
    public User getUserByEmail(String email) {
        try {
            Document doc = usersCollection.find(eq("email", email)).first();
            if (doc != null) {
                return documentToUser(doc);
            }
        } catch (Exception e) {
            System.err.println("Error getting user by email: " + e.getMessage());
        }
        return null;
    }
    
    private User documentToUser(Document doc) {
        User user = new User();
        user.setUserId(doc.getString("userId"));
        user.setEmail(doc.getString("email"));
        user.setPassword(doc.getString("password"));
        user.setFirstName(doc.getString("firstName"));
        user.setLastName(doc.getString("lastName"));
        user.setPhone(doc.getString("phone"));
        user.setAddress(doc.getString("address"));
        user.setRole(doc.getString("role"));
        user.setStatus(doc.getString("status"));
        user.setStudentId(doc.getString("studentId"));
        user.setFaculty(doc.getString("faculty"));
        user.setYearOfStudy(doc.getString("yearOfStudy"));
        user.setTotalBorrowed(doc.getInteger("totalBorrowed", 0));
        user.setCurrentBorrowed(doc.getInteger("currentBorrowed", 0));
        Object totalFinesObj = doc.get("totalFines");
        if (totalFinesObj instanceof Number) {
            user.setTotalFines(((Number) totalFinesObj).doubleValue());
        } else {
            user.setTotalFines(0.0);
        }
        if (doc.getDate("dateOfBirth") != null) {
            user.setDateOfBirth(new Date(doc.getDate("dateOfBirth").getTime()));
        }
        if (doc.getDate("registrationDate") != null) {
            user.setRegistrationDate(new Date(doc.getDate("registrationDate").getTime()));
        }
        if (doc.getDate("lastLogin") != null) {
            user.setLastLogin(new Date(doc.getDate("lastLogin").getTime()));
        }
        user.setOnline(doc.getBoolean("isOnline", false));
        return user;
    }
    
    public boolean register(User user) {
        try {
            // Check if email already exists
            Document existing = usersCollection.find(eq("email", user.getEmail())).first();
            if (existing != null) {
                return false;
            }
            
            // Generate userId
            String userId = generateUserId();
            user.setUserId(userId);
            
            Document doc = new Document("userId", userId)
                .append("email", user.getEmail())
                .append("password", user.getPassword()) // In production, should be hashed
                .append("firstName", user.getFirstName())
                .append("lastName", user.getLastName())
                .append("phone", user.getPhone())
                .append("address", user.getAddress() != null ? user.getAddress() : "")
                .append("role", user.getRole() != null ? user.getRole() : "USER")
                .append("status", user.getStatus() != null ? user.getStatus() : "ACTIVE")
                .append("studentId", user.getStudentId())
                .append("faculty", user.getFaculty() != null ? user.getFaculty() : "")
                .append("yearOfStudy", user.getYearOfStudy() != null ? user.getYearOfStudy() : "")
                .append("totalBorrowed", 0)
                .append("currentBorrowed", 0)
                .append("totalFines", 0.0)
                .append("registrationDate", user.getRegistrationDate() != null ? user.getRegistrationDate() : new java.util.Date())
                .append("lastLogin", null)
                .append("isOnline", false);
            
            usersCollection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.err.println("Error during registration: " + e.getMessage());
            return false;
        }
    }
    
    private String generateUserId() {
        long count = usersCollection.countDocuments();
        return String.format("USER%03d", count + 1);
    }
    
    // Book operations
    public List<Book> getAllBooks() {
        try {
            List<Book> books = new ArrayList<>();
            for (Document doc : booksCollection.find()) {
                books.add(documentToBook(doc));
            }
            return books;
        } catch (Exception e) {
            System.err.println("Error getting all books: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private Book documentToBook(Document doc) {
        Book book = new Book();
        book.setBookId(doc.getString("bookId"));
        book.setTitle(doc.getString("title"));
        book.setAuthor(doc.getString("author"));
        book.setIsbn(doc.getString("isbn"));
        book.setCategory(doc.getString("category"));
        book.setPublishYear(doc.getInteger("publishYear", 0));
        book.setPages(doc.getInteger("pages", 0));
        Object priceObj = doc.get("price");
        if (priceObj instanceof Number) {
            book.setPrice(((Number) priceObj).doubleValue());
        } else {
            book.setPrice(0.0);
        }
        book.setTotalCopies(doc.getInteger("totalCopies", 0));
        book.setAvailableCopies(doc.getInteger("availableCopies", 0));
        book.setDescription(doc.getString("description"));
        return book;
    }
    
    public List<BookCopy> getBookCopies(String bookId) {
        try {
            List<BookCopy> copies = new ArrayList<>();
            for (Document doc : bookCopiesCollection.find(eq("bookId", bookId)).sort(new Document("copyId", 1))) {
                copies.add(documentToBookCopy(doc));
            }
            return copies;
        } catch (Exception e) {
            System.err.println("Error getting book copies: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private BookCopy documentToBookCopy(Document doc) {
        BookCopy copy = new BookCopy();
        copy.setCopyId(doc.getString("copyId"));
        copy.setBookId(doc.getString("bookId"));
        copy.setStatus(doc.getString("status"));
        copy.setLocation(doc.getString("location"));
        copy.setNotes(doc.getString("notes"));
        return copy;
    }
    
    // Borrow operations
    public List<BorrowRecord> getBorrowRecords(String userId) {
        try {
            List<BorrowRecord> records = new ArrayList<>();
            for (Document doc : borrowRecordsCollection.find(eq("userId", userId))) {
                records.add(documentToBorrowRecord(doc));
            }
            return records;
        } catch (Exception e) {
            System.err.println("Error getting borrow records: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private BorrowRecord documentToBorrowRecord(Document doc) {
        BorrowRecord record = new BorrowRecord();
        record.setRecordId(doc.getString("recordId"));
        record.setUserId(doc.getString("userId"));
        record.setBookId(doc.getString("bookId"));
        record.setCopyId(doc.getString("copyId"));
        if (doc.getDate("borrowDate") != null) {
            record.setBorrowDate(new Date(doc.getDate("borrowDate").getTime()));
        }
        if (doc.getDate("dueDate") != null) {
            record.setDueDate(new Date(doc.getDate("dueDate").getTime()));
        }
        if (doc.getDate("returnDate") != null) {
            record.setReturnDate(new Date(doc.getDate("returnDate").getTime()));
        }
        record.setStatus(doc.getString("status"));
        Object fineObj = doc.get("fine");
        if (fineObj instanceof Number) {
            record.setFine(((Number) fineObj).doubleValue());
        } else {
            record.setFine(0.0);
        }
        
        // Get user info for display
        String userId = record.getUserId();
        if (userId != null) {
            Document userDoc = usersCollection.find(eq("userId", userId)).first();
            if (userDoc != null) {
                String firstName = userDoc.getString("firstName");
                String lastName = userDoc.getString("lastName");
                String username = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                username = username.trim();
                if (username.isEmpty()) {
                    username = userDoc.getString("email");
                }
                record.setUsername(username);
            }
        }
        
        // Get book title for display
        Document bookDoc = booksCollection.find(eq("bookId", record.getBookId())).first();
        if (bookDoc != null) {
            record.setBookTitle(bookDoc.getString("title"));
        }
        
        return record;
    }
    
    public boolean borrowBook(String userId, String bookId) {
        try {
            // Check if user exists and is active
            Document userDoc = usersCollection.find(eq("userId", userId)).first();
            if (userDoc == null || !"ACTIVE".equals(userDoc.getString("status"))) {
                return false;
            }
            
            // Get user role
            String userRole = userDoc.getString("role");
            String userEmail = userDoc.getString("email");
            
            // Get settings
            Document settings = getSettings();
            int maxBorrowBooks = settings != null ? settings.getInteger("maxBorrowBooks", 5) : 5;
            int maxBorrowDays = settings != null ? settings.getInteger("maxBorrowDays", 14) : 14;
            
            // Check if user has reached max borrow limit
            // Calculate currentBorrowed directly from borrow records to ensure accuracy
            // Count both BORROWING and BORROWED status (some old records might use BORROWED)
            long currentBorrowed = borrowRecordsCollection.countDocuments(
                and(eq("userId", userId), 
                    or(eq("status", "BORROWING"), eq("status", "BORROWED")))
            );
            
            System.out.println("[DEBUG] borrowBook: userId=" + userId + ", email=" + userEmail + ", role=" + userRole + ", currentBorrowed=" + currentBorrowed + ", maxBorrowBooks=" + maxBorrowBooks);
            
            // Apply borrow limit to ALL users including ADMIN
            // Check if adding one more book would exceed the limit
            // If currentBorrowed is already at or above maxBorrowBooks, reject
            if (currentBorrowed >= maxBorrowBooks) {
                System.out.println("[DEBUG] borrowBook: User has reached max borrow limit. Cannot borrow more books. (Role: " + userRole + ")");
                return false;
            }
            
            // Check if book has available copies
            Document bookDoc = booksCollection.find(eq("bookId", bookId)).first();
            if (bookDoc == null) {
                return false;
            }
            
            int availableCopies = bookDoc.getInteger("availableCopies", 0);
            if (availableCopies <= 0) {
                return false;
            }
            
            // Find an available copy
            Document copyDoc = bookCopiesCollection.find(
                and(eq("bookId", bookId), eq("status", "AVAILABLE"))
            ).first();
            
            if (copyDoc == null) {
                return false;
            }
            
            String copyId = copyDoc.getString("copyId");
            
            // Double-check limit again right before inserting (to prevent race conditions)
            long currentBorrowedRecheck = borrowRecordsCollection.countDocuments(
                and(eq("userId", userId), 
                    or(eq("status", "BORROWING"), eq("status", "BORROWED")))
            );
            
            if (currentBorrowedRecheck >= maxBorrowBooks) {
                System.out.println("[DEBUG] borrowBook: User has reached max borrow limit on recheck. Cannot borrow more books.");
                return false;
            }
            
            // Create borrow record
            String recordId = "record_" + System.currentTimeMillis();
            java.util.Date now = new java.util.Date();
            java.util.Date dueDate = new java.util.Date(now.getTime() + (long)maxBorrowDays * 24 * 60 * 60 * 1000);
            
            Document borrowDoc = new Document("recordId", recordId)
                .append("userId", userId)
                .append("bookId", bookId)
                .append("copyId", copyId)
                .append("borrowDate", now)
                .append("dueDate", dueDate)
                .append("returnDate", null)
                .append("status", "BORROWING")
                .append("fine", 0.0);
            
            // Update book copy status first (before inserting borrow record)
            bookCopiesCollection.updateOne(eq("copyId", copyId), set("status", "BORROWED"));
            
            // Update book available copies
            booksCollection.updateOne(eq("bookId", bookId), 
                set("availableCopies", availableCopies - 1));
            
            // Insert borrow record
            borrowRecordsCollection.insertOne(borrowDoc);
            
            // Final verification after insert to ensure we didn't exceed limit
            long finalBorrowedCount = borrowRecordsCollection.countDocuments(
                and(eq("userId", userId), 
                    or(eq("status", "BORROWING"), eq("status", "BORROWED")))
            );
            
            if (finalBorrowedCount > maxBorrowBooks) {
                // Rollback: delete the record we just inserted and restore book copy
                System.out.println("[DEBUG] borrowBook: ERROR - Exceeded limit after insert! Rolling back. finalBorrowedCount=" + finalBorrowedCount + ", maxBorrowBooks=" + maxBorrowBooks);
                borrowRecordsCollection.deleteOne(eq("recordId", recordId));
                bookCopiesCollection.updateOne(eq("copyId", copyId), set("status", "AVAILABLE"));
                booksCollection.updateOne(eq("bookId", bookId), 
                    set("availableCopies", availableCopies));
                return false;
            }
            
            // Update user stats
            usersCollection.updateOne(eq("userId", userId), 
                combine(
                    inc("currentBorrowed", 1),
                    inc("totalBorrowed", 1)
                ));
            
            return true;
        } catch (Exception e) {
            System.err.println("Error borrowing book: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean returnBook(String recordId) {
        try {
            Document recordDoc = borrowRecordsCollection.find(eq("recordId", recordId)).first();
            if (recordDoc == null) {
                return false;
            }
            
            String userId = recordDoc.getString("userId");
            String bookId = recordDoc.getString("bookId");
            String copyId = recordDoc.getString("copyId");
            
            // Get settings for calculating overdue fine
            Document settings = getSettings();
            Object overdueFineObj = settings != null ? settings.get("overdueFinePerDay") : null;
            double overdueFinePerDay = overdueFineObj != null ? ((Number) overdueFineObj).doubleValue() : 5000.0;
            
            // Calculate fine if overdue
            java.util.Date dueDate = recordDoc.getDate("dueDate");
            java.util.Date returnDate = new java.util.Date();
            double fineAmount = 0.0;
            
            if (dueDate != null && returnDate.after(dueDate)) {
                long daysOverdue = (returnDate.getTime() - dueDate.getTime()) / (24 * 60 * 60 * 1000);
                fineAmount = daysOverdue * overdueFinePerDay;
                
                // Create fine record
                String fineId = "FINE_" + System.currentTimeMillis();
                Document fineDoc = new Document("fineId", fineId)
                    .append("recordId", recordId)
                    .append("userId", userId)
                    .append("bookId", bookId)
                    .append("reason", "Quá hạn trả sách (" + daysOverdue + " ngày)")
                    .append("amount", fineAmount)
                    .append("status", "UNPAID")
                    .append("dueDate", new java.util.Date(returnDate.getTime() + 30L * 24 * 60 * 60 * 1000))
                    .append("paidDate", null)
                    .append("createdDate", returnDate);
                
                finesCollection.insertOne(fineDoc);
                
                // Update user totalFines
                usersCollection.updateOne(eq("userId", userId), 
                    inc("totalFines", fineAmount));
            }
            
            // Update borrow record
            borrowRecordsCollection.updateOne(eq("recordId", recordId),
                combine(
                    set("returnDate", returnDate),
                    set("status", "RETURNED"),
                    set("fine", fineAmount)
                ));
            
            // Update book copy status
            bookCopiesCollection.updateOne(eq("copyId", copyId), set("status", "AVAILABLE"));
            
            // Update book available copies
            Document bookDoc = booksCollection.find(eq("bookId", bookId)).first();
            if (bookDoc != null) {
                int availableCopies = bookDoc.getInteger("availableCopies", 0);
                booksCollection.updateOne(eq("bookId", bookId), 
                    set("availableCopies", availableCopies + 1));
            }
            
            // Update user stats
            usersCollection.updateOne(eq("userId", userId), 
                inc("currentBorrowed", -1));
            
            return true;
        } catch (Exception e) {
            System.err.println("Error returning book: " + e.getMessage());
            return false;
        }
    }
    
    public boolean renewBook(String recordId) {
        try {
            Document recordDoc = borrowRecordsCollection.find(eq("recordId", recordId)).first();
            if (recordDoc == null) {
                return false;
            }
            
            String status = recordDoc.getString("status");
            if (!"BORROWING".equals(status) && !"BORROWED".equals(status)) {
                return false;
            }
            
            // Get settings
            Document settings = getSettings();
            int renewalDays = settings != null ? settings.getInteger("renewalDays", 7) : 7;
            
            // Extend due date by renewal days from settings
            java.util.Date currentDueDate = recordDoc.getDate("dueDate");
            if (currentDueDate == null) {
                return false;
            }
            
            java.util.Date newDueDate = new java.util.Date(currentDueDate.getTime() + (long)renewalDays * 24 * 60 * 60 * 1000);
            borrowRecordsCollection.updateOne(eq("recordId", recordId),
                set("dueDate", newDueDate));
            
            return true;
        } catch (Exception e) {
            System.err.println("Error renewing book: " + e.getMessage());
            return false;
        }
    }
    
    // Admin operations
    public List<User> getAllUsers() {
        try {
            List<User> users = new ArrayList<>();
            for (Document doc : usersCollection.find()) {
                users.add(documentToUser(doc));
            }
            return users;
        } catch (Exception e) {
            System.err.println("Error getting all users: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<BorrowRecord> getAllBorrowRecords() {
        try {
            List<BorrowRecord> records = new ArrayList<>();
            for (Document doc : borrowRecordsCollection.find()) {
                records.add(documentToBorrowRecord(doc));
            }
            return records;
        } catch (Exception e) {
            System.err.println("Error getting all borrow records: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private void initializeDefaultSettings() {
        try {
            if (settingsCollection.countDocuments() == 0) {
                Document defaultSettings = new Document("settingsId", "SYSTEM_SETTINGS")
                    .append("maxBorrowDays", 14)
                    .append("maxBorrowBooks", 5)
                    .append("renewalDays", 7)
                    .append("overdueFinePerDay", 5000.0)
                    .append("lostBookFine", 100000.0)
                    .append("damagedBookFine", 50000.0)
                    .append("autoCheckOverdue", true)
                    .append("reminderDaysBefore", 2)
                    .append("lastUpdated", new java.util.Date());
                settingsCollection.insertOne(defaultSettings);
                System.out.println("Default settings initialized");
            }
        } catch (Exception e) {
            System.err.println("Error initializing default settings: " + e.getMessage());
        }
    }
    
    public Document getSettings() {
        try {
            Document settings = settingsCollection.find(eq("settingsId", "SYSTEM_SETTINGS")).first();
            if (settings == null) {
                // Return default settings if not found
                initializeDefaultSettings();
                settings = settingsCollection.find(eq("settingsId", "SYSTEM_SETTINGS")).first();
            }
            return settings;
        } catch (Exception e) {
            System.err.println("Error getting settings: " + e.getMessage());
            return null;
        }
    }
    
    public boolean updateSettings(Document settings) {
        try {
            UpdateResult result = settingsCollection.updateOne(
                eq("settingsId", "SYSTEM_SETTINGS"),
                combine(
                    set("maxBorrowDays", settings.getInteger("maxBorrowDays")),
                    set("maxBorrowBooks", settings.getInteger("maxBorrowBooks")),
                    set("renewalDays", settings.getInteger("renewalDays")),
                    set("overdueFinePerDay", settings.getDouble("overdueFinePerDay")),
                    set("lostBookFine", settings.getDouble("lostBookFine")),
                    set("damagedBookFine", settings.getDouble("damagedBookFine")),
                    set("autoCheckOverdue", settings.getBoolean("autoCheckOverdue")),
                    set("reminderDaysBefore", settings.getInteger("reminderDaysBefore")),
                    set("lastUpdated", new java.util.Date())
                )
            );
            return result.getModifiedCount() > 0 || result.getMatchedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error updating settings: " + e.getMessage());
            return false;
        }
    }
    
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
    
    // Additional admin methods
    public boolean addUser(User user) {
        try {
            // Check if email already exists
            Document existing = usersCollection.find(eq("email", user.getEmail())).first();
            if (existing != null) {
                return false;
            }
            
            String userId = user.getUserId();
            if (userId == null || userId.isEmpty()) {
                userId = generateUserId();
                user.setUserId(userId);
            }
            
            Document doc = new Document("userId", userId)
                .append("email", user.getEmail())
                .append("password", user.getPassword())
                .append("firstName", user.getFirstName())
                .append("lastName", user.getLastName())
                .append("phone", user.getPhone())
                .append("address", user.getAddress())
                .append("role", user.getRole() != null ? user.getRole() : "USER")
                .append("status", user.getStatus() != null ? user.getStatus() : "ACTIVE")
                .append("studentId", user.getStudentId())
                .append("faculty", user.getFaculty())
                .append("yearOfStudy", user.getYearOfStudy())
                .append("totalBorrowed", user.getTotalBorrowed())
                .append("currentBorrowed", user.getCurrentBorrowed())
                .append("totalFines", user.getTotalFines())
                .append("registrationDate", user.getRegistrationDate() != null ? user.getRegistrationDate() : new java.util.Date())
                .append("lastLogin", null)
                .append("isOnline", false);
            
            usersCollection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.err.println("Error adding user: " + e.getMessage());
            return false;
        }
    }
    
    public List<BorrowRecord> getUserBorrowRecords(String userId) {
        return getBorrowRecords(userId);
    }
    
    public boolean updateUser(User user) {
        try {
            Document doc = usersCollection.find(eq("userId", user.getUserId())).first();
            if (doc == null) {
                return false;
            }
            
            usersCollection.updateOne(eq("userId", user.getUserId()),
                combine(
                    set("email", user.getEmail()),
                    set("firstName", user.getFirstName()),
                    set("lastName", user.getLastName()),
                    set("phone", user.getPhone()),
                    set("address", user.getAddress()),
                    set("studentId", user.getStudentId()),
                    set("faculty", user.getFaculty()),
                    set("yearOfStudy", user.getYearOfStudy()),
                    set("status", user.getStatus())
                ));
            
            return true;
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteUser(String userId) {
        try {
            if (userId == null) {
                return false;
            }
            DeleteResult result = usersCollection.deleteOne(eq("userId", userId));
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
    
    public boolean lockUser(String userId) {
        try {
            // Also set isOnline to false when locking
            UpdateResult result = usersCollection.updateOne(
                eq("userId", userId), 
                combine(
                    set("status", "LOCKED"),
                    set("isOnline", false)
                )
            );
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error locking user: " + e.getMessage());
            return false;
        }
    }
    
    public boolean unlockUser(String userId) {
        try {
            UpdateResult result = usersCollection.updateOne(eq("userId", userId), set("status", "ACTIVE"));
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error unlocking user: " + e.getMessage());
            return false;
        }
    }
    
    public boolean resetPassword(String userId, String newPassword) {
        try {
            UpdateResult result = usersCollection.updateOne(eq("userId", userId), set("password", newPassword));
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error resetting password: " + e.getMessage());
            return false;
        }
    }
    
    private ObjectId findUserIdByUserId(String userId) {
        Document doc = usersCollection.find(eq("userId", userId)).first();
        return doc != null ? doc.getObjectId("_id") : null;
    }
    
    private ObjectId findBookIdById(int id) {
        Document doc = booksCollection.find(eq("bookId", String.valueOf(id))).first();
        return doc != null ? doc.getObjectId("_id") : null;
    }
    
    private ObjectId findBorrowRecordIdById(int id) {
        Document doc = borrowRecordsCollection.find(eq("recordId", String.valueOf(id))).first();
        return doc != null ? doc.getObjectId("_id") : null;
    }
    
    // Book operations
    public List<Book> searchBooks(String keyword) {
        try {
            List<Book> books = new ArrayList<>();
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllBooks();
            }
            String searchPattern = ".*" + keyword + ".*";
            for (Document doc : booksCollection.find(
                or(
                    regex("title", searchPattern, "i"),
                    regex("author", searchPattern, "i"),
                    regex("isbn", searchPattern, "i"),
                    regex("category", searchPattern, "i")
                )
            )) {
                books.add(documentToBook(doc));
            }
            return books;
        } catch (Exception e) {
            System.err.println("Error searching books: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public boolean addBook(Book book) {
        try {
            String bookId = book.getBookId();
            if (bookId == null || bookId.isEmpty()) {
                bookId = "book_" + new ObjectId().toString().substring(0, 8);
            }
            
            Document doc = new Document("bookId", bookId)
                .append("title", book.getTitle())
                .append("author", book.getAuthor())
                .append("isbn", book.getIsbn())
                .append("category", book.getCategory())
                .append("publishYear", book.getPublishYear())
                .append("pages", book.getPages())
                .append("price", book.getPrice())
                .append("totalCopies", book.getTotalCopies())
                .append("availableCopies", book.getAvailableCopies())
                .append("description", book.getDescription());
            
            booksCollection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.err.println("Error adding book: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateBook(Book book) {
        try {
            Document doc = booksCollection.find(eq("bookId", book.getBookId())).first();
            if (doc == null) {
                return false;
            }
            
            booksCollection.updateOne(eq("bookId", book.getBookId()),
                combine(
                    set("title", book.getTitle()),
                    set("author", book.getAuthor()),
                    set("isbn", book.getIsbn()),
                    set("category", book.getCategory()),
                    set("publishYear", book.getPublishYear()),
                    set("pages", book.getPages()),
                    set("price", book.getPrice()),
                    set("totalCopies", book.getTotalCopies()),
                    set("availableCopies", book.getAvailableCopies()),
                    set("description", book.getDescription())
                ));
            
            return true;
        } catch (Exception e) {
            System.err.println("Error updating book: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteBook(String bookId) {
        try {
            // Delete all copies first
            bookCopiesCollection.deleteMany(eq("bookId", bookId));
            
            // Delete the book
            DeleteResult result = booksCollection.deleteOne(eq("bookId", bookId));
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting book: " + e.getMessage());
            return false;
        }
    }
    
    public Book getBookById(String bookId) {
        try {
            Document doc = booksCollection.find(eq("bookId", bookId)).first();
            if (doc != null) {
                return documentToBook(doc);
            }
        } catch (Exception e) {
            System.err.println("Error getting book by ID: " + e.getMessage());
        }
        return null;
    }
    
    public boolean addBookCopy(BookCopy copy) {
        try {
            Document doc = new Document("copyId", copy.getCopyId())
                .append("bookId", copy.getBookId())
                .append("status", copy.getStatus())
                .append("location", copy.getLocation())
                .append("notes", copy.getNotes());
            
            bookCopiesCollection.insertOne(doc);
            
            // Update book totalCopies and availableCopies
            Document bookDoc = booksCollection.find(eq("bookId", copy.getBookId())).first();
            if (bookDoc != null) {
                int totalCopies = bookDoc.getInteger("totalCopies", 0);
                int availableCopies = bookDoc.getInteger("availableCopies", 0);
                
                booksCollection.updateOne(eq("bookId", copy.getBookId()),
                    combine(
                        set("totalCopies", totalCopies + 1),
                        set("availableCopies", "AVAILABLE".equals(copy.getStatus()) ? availableCopies + 1 : availableCopies)
                    ));
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error adding book copy: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteBookCopy(String copyId) {
        try {
            Document copyDoc = bookCopiesCollection.find(eq("copyId", copyId)).first();
            if (copyDoc == null) {
                return false;
            }
            
            String bookId = copyDoc.getString("bookId");
            String status = copyDoc.getString("status");
            
            // Delete the copy
            DeleteResult result = bookCopiesCollection.deleteOne(eq("copyId", copyId));
            if (result.getDeletedCount() > 0) {
                // Update book totalCopies and availableCopies
                Document bookDoc = booksCollection.find(eq("bookId", bookId)).first();
                if (bookDoc != null) {
                    int totalCopies = bookDoc.getInteger("totalCopies", 0);
                    int availableCopies = bookDoc.getInteger("availableCopies", 0);
                    
                    booksCollection.updateOne(eq("bookId", bookId),
                        combine(
                            set("totalCopies", Math.max(0, totalCopies - 1)),
                            set("availableCopies", "AVAILABLE".equals(status) ? Math.max(0, availableCopies - 1) : availableCopies)
                        ));
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting book copy: " + e.getMessage());
            return false;
        }
    }
    
    // Borrow operations for admin
    public boolean markAsLost(String recordId) {
        try {
            Document recordDoc = borrowRecordsCollection.find(eq("recordId", recordId)).first();
            if (recordDoc == null) {
                return false;
            }
            
            String copyId = recordDoc.getString("copyId");
            String bookId = recordDoc.getString("bookId");
            String userId = recordDoc.getString("userId");
            
            // Update book available copies and get book price
            Document bookDoc = booksCollection.find(eq("bookId", bookId)).first();
            double bookPrice = 0.0;
            if (bookDoc != null) {
                int totalCopies = bookDoc.getInteger("totalCopies", 0);
                int availableCopies = bookDoc.getInteger("availableCopies", 0);
                
                booksCollection.updateOne(eq("bookId", bookId),
                    combine(
                        set("totalCopies", Math.max(0, totalCopies - 1)),
                        set("availableCopies", Math.max(0, availableCopies - 1))
                    ));
                
                // Get book price
                Object priceObj = bookDoc.get("price");
                if (priceObj != null) {
                    bookPrice = ((Number) priceObj).doubleValue();
                }
            }
            
            // Calculate fine: book price + 50000
            double fineAmount = bookPrice + 50000.0;
            
            // Ensure fine amount is at least 50000 (in case book price is 0 or missing)
            if (fineAmount < 50000.0) {
                fineAmount = 50000.0;
            }
            
            // Create fine record first
            String fineId = "FINE_" + System.currentTimeMillis();
            Document fineDoc = new Document("fineId", fineId)
                .append("recordId", recordId)
                .append("userId", userId)
                .append("bookId", bookId)
                .append("reason", "Mất sách")
                .append("amount", fineAmount)
                .append("status", "UNPAID")
                .append("dueDate", new java.util.Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000))
                .append("paidDate", null)
                .append("createdDate", new java.util.Date());
            
            finesCollection.insertOne(fineDoc);
            
            // Update borrow record (include fine field for consistency)
            borrowRecordsCollection.updateOne(eq("recordId", recordId),
                combine(
                    set("status", "LOST"),
                    set("returnDate", new java.util.Date()),
                    set("fine", fineAmount)
                ));
            
            // Update book copy status
            bookCopiesCollection.updateOne(eq("copyId", copyId), set("status", "LOST"));
            
            // Update user stats
            usersCollection.updateOne(eq("userId", userId), 
                inc("currentBorrowed", -1));
            
            // Update user totalFines
            usersCollection.updateOne(eq("userId", userId), 
                inc("totalFines", fineAmount));
            
            // Debug log
            System.out.println("[DEBUG] Created lost book fine: userId=" + userId + ", bookId=" + bookId + ", bookPrice=" + bookPrice + ", fineAmount=" + fineAmount);
            
            return true;
        } catch (Exception e) {
            System.err.println("Error marking as lost: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean markAsDamaged(String recordId) {
        try {
            Document recordDoc = borrowRecordsCollection.find(eq("recordId", recordId)).first();
            if (recordDoc == null) {
                return false;
            }
            
            String copyId = recordDoc.getString("copyId");
            String userId = recordDoc.getString("userId");
            String bookId = recordDoc.getString("bookId");
            
            // Get settings for damaged book fine
            Document settings = getSettings();
            Object damagedBookFineObj = settings != null ? settings.get("damagedBookFine") : null;
            double damagedBookFine = damagedBookFineObj != null ? ((Number) damagedBookFineObj).doubleValue() : 50000.0;
            
            // Create fine for damaged book
            String fineId = "FINE_" + System.currentTimeMillis();
            Document fineDoc = new Document("fineId", fineId)
                .append("recordId", recordId)
                .append("userId", userId)
                .append("bookId", bookId)
                .append("reason", "Hư hỏng sách")
                .append("amount", damagedBookFine)
                .append("status", "UNPAID")
                .append("dueDate", new java.util.Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000))
                .append("paidDate", null)
                .append("createdDate", new java.util.Date());
            
            finesCollection.insertOne(fineDoc);
            
            // Update user totalFines
            usersCollection.updateOne(eq("userId", userId), 
                inc("totalFines", damagedBookFine));
            
            // Update borrow record
            borrowRecordsCollection.updateOne(eq("recordId", recordId),
                combine(
                    set("status", "DAMAGED"),
                    set("returnDate", new java.util.Date()),
                    set("fine", damagedBookFine)
                ));
            
            // Update book copy status
            bookCopiesCollection.updateOne(eq("copyId", copyId), set("status", "DAMAGED"));
            
            // Update user stats
            usersCollection.updateOne(eq("userId", userId), 
                inc("currentBorrowed", -1));
            
            return true;
        } catch (Exception e) {
            System.err.println("Error marking as damaged: " + e.getMessage());
            return false;
        }
    }
    
    public boolean forceReturn(String recordId) {
        try {
            // Get borrow record info - DON'T return the book yet, just notify user
            Document recordDoc = borrowRecordsCollection.find(eq("recordId", recordId)).first();
            if (recordDoc == null) {
                return false;
            }
            
            // Check if record is still in borrowing status
            String status = recordDoc.getString("status");
            if (status == null || !status.equals("BORROWING") && !status.equals("BORROWED")) {
                // Book already returned or in other status, can't force return
                return false;
            }
            
            String userId = recordDoc.getString("userId");
            String bookId = recordDoc.getString("bookId");
            
            // Get book title
            Document bookDoc = booksCollection.find(eq("bookId", bookId)).first();
            String bookTitle = bookDoc != null ? bookDoc.getString("title") : "sách";
            
            // Only create notification - DON'T actually return the book
            // User must return the book themselves, then status will change to RETURNED
            if (userId != null) {
                createNotification(userId, "FORCE_RETURN", 
                    "Quản trị viên đã yêu cầu bạn trả sách: " + bookTitle + ". Vui lòng đến thư viện để trả sách.");
                return true;
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error in force return: " + e.getMessage());
            return false;
        }
    }
    
    public boolean isRecordOwnedByUser(String recordId, String userId) {
        try {
            Document recordDoc = borrowRecordsCollection.find(eq("recordId", recordId)).first();
            if (recordDoc == null) {
                return false;
            }
            String recordUserId = recordDoc.getString("userId");
            return userId != null && userId.equals(recordUserId);
        } catch (Exception e) {
            System.err.println("Error checking record ownership: " + e.getMessage());
            return false;
        }
    }
    
    public void createNotification(String userId, String type, String message) {
        try {
            String notificationId = "NOTIF" + System.currentTimeMillis();
            Document notification = new Document("notificationId", notificationId)
                .append("userId", userId)
                .append("type", type)
                .append("message", message)
                .append("isRead", false)
                .append("createdAt", new java.util.Date());
            
            notificationsCollection.insertOne(notification);
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
        }
    }
    
    public List<Document> getUserNotifications(String userId) {
        try {
            List<Document> notifications = new ArrayList<>();
            for (Document doc : notificationsCollection.find(
                and(eq("userId", userId), eq("isRead", false))
            ).sort(new Document("createdAt", -1))) {
                notifications.add(doc);
            }
            return notifications;
        } catch (Exception e) {
            System.err.println("Error getting user notifications: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public boolean markNotificationAsRead(String notificationId) {
        try {
            notificationsCollection.updateOne(
                eq("notificationId", notificationId),
                set("isRead", true)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            return false;
        }
    }
    
    // Report methods
    public Document getDashboardStats() {
        try {
            Document stats = new Document();
            
            long totalUsers = usersCollection.countDocuments();
            long totalBooks = booksCollection.countDocuments();
            long totalBorrows = borrowRecordsCollection.countDocuments();
            long activeBorrows = borrowRecordsCollection.countDocuments(eq("status", "BORROWING"));
            
            // Calculate total available copies by counting book copies with status "AVAILABLE"
            // This is more accurate than using the availableCopies field in books collection
            long availableCopies = bookCopiesCollection.countDocuments(eq("status", "AVAILABLE"));
            
            // Calculate overdue borrows (status = BORROWING and dueDate < today)
            java.util.Date today = new java.util.Date();
            long overdueBorrows = 0;
            for (Document record : borrowRecordsCollection.find(eq("status", "BORROWING"))) {
                java.util.Date dueDate = record.getDate("dueDate");
                if (dueDate != null && dueDate.before(today)) {
                    overdueBorrows++;
                }
            }
            
            // Calculate total fines amount
            double totalFinesAmount = 0.0;
            for (Document fine : finesCollection.find(eq("status", "UNPAID"))) {
                Object amountObj = fine.get("amount");
                if (amountObj instanceof Number) {
                    totalFinesAmount += ((Number) amountObj).doubleValue();
                }
            }
            
            // Count pending users (users with status "LOCKED" - considered as pending approval)
            // Or you can use a different status if your system has "PENDING" status
            long pendingUsers = usersCollection.countDocuments(eq("status", "LOCKED"));
            
            stats.append("totalUsers", totalUsers)
                .append("totalBooks", totalBooks)
                .append("totalBorrows", totalBorrows)
                .append("availableCopies", availableCopies)
                .append("activeBorrows", activeBorrows)
                .append("totalFinesAmount", totalFinesAmount)
                .append("overdueBorrows", overdueBorrows)
                .append("pendingUsers", pendingUsers);
            
            return stats;
        } catch (Exception e) {
            System.err.println("Error getting dashboard stats: " + e.getMessage());
            e.printStackTrace();
            return new Document();
        }
    }
    
    public List<Document> getBookReport() {
        try {
            List<Document> report = new ArrayList<>();
            
            // Group by category
            for (Document category : categoriesCollection.find()) {
                String categoryName = category.getString("name");
                long totalBooks = booksCollection.countDocuments(eq("category", categoryName));
                
                long totalCopies = 0;
                long availableCopies = 0;
                List<String> bookIds = new ArrayList<>();
                
                for (Document book : booksCollection.find(eq("category", categoryName))) {
                    Object totalCopiesObj = book.get("totalCopies");
                    Object availableCopiesObj = book.get("availableCopies");
                    
                    if (totalCopiesObj instanceof Number) {
                        totalCopies += ((Number) totalCopiesObj).longValue();
                    }
                    if (availableCopiesObj instanceof Number) {
                        availableCopies += ((Number) availableCopiesObj).longValue();
                    }
                    
                    bookIds.add(book.getString("bookId"));
                }
                
                long borrowed = totalCopies - availableCopies;
                
                // Count lost books (status = LOST in borrow_records)
                long lost = 0;
                for (String bookId : bookIds) {
                    lost += borrowRecordsCollection.countDocuments(
                        and(eq("bookId", bookId), eq("status", "LOST"))
                    );
                }
                
                // Count damaged books (status = DAMAGED in borrow_records)
                long damaged = 0;
                for (String bookId : bookIds) {
                    damaged += borrowRecordsCollection.countDocuments(
                        and(eq("bookId", bookId), eq("status", "DAMAGED"))
                    );
                }
                
                report.add(new Document("category", categoryName)
                    .append("totalBooks", totalBooks)
                    .append("available", availableCopies)
                    .append("borrowed", borrowed)
                    .append("lost", lost)
                    .append("damaged", damaged));
            }
            
            return report;
        } catch (Exception e) {
            System.err.println("Error getting book report: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public List<Document> getUserReport() {
        try {
            List<Document> report = new ArrayList<>();
            
            // Group by faculty
            java.util.Map<String, Document> facultyData = new java.util.HashMap<>();
            
            for (Document user : usersCollection.find(eq("role", "USER"))) {
                String faculty = user.getString("faculty");
                if (faculty == null || faculty.trim().isEmpty()) {
                    faculty = "N/A";
                }
                
                Document facultyDoc = facultyData.getOrDefault(faculty, new Document("faculty", faculty)
                    .append("totalUsers", 0L)
                    .append("active", 0L)
                    .append("locked", 0L)
                    .append("totalBorrows", 0L)
                    .append("totalFines", 0.0));
                
                facultyDoc.put("totalUsers", ((Number) facultyDoc.get("totalUsers")).longValue() + 1);
                
                String status = user.getString("status");
                if ("ACTIVE".equals(status)) {
                    facultyDoc.put("active", ((Number) facultyDoc.get("active")).longValue() + 1);
                } else if ("LOCKED".equals(status)) {
                    facultyDoc.put("locked", ((Number) facultyDoc.get("locked")).longValue() + 1);
                }
                
                String userId = user.getString("userId");
                if (userId != null) {
                    // Count total borrows for this user
                    long userBorrows = borrowRecordsCollection.countDocuments(eq("userId", userId));
                    facultyDoc.put("totalBorrows", ((Number) facultyDoc.get("totalBorrows")).longValue() + userBorrows);
                    
                    // Sum total fines for this user
                    double userFines = 0.0;
                    for (Document fine : finesCollection.find(eq("userId", userId))) {
                        Object amountObj = fine.get("amount");
                        if (amountObj instanceof Number) {
                            userFines += ((Number) amountObj).doubleValue();
                        }
                    }
                    facultyDoc.put("totalFines", ((Number) facultyDoc.get("totalFines")).doubleValue() + userFines);
                }
                
                facultyData.put(faculty, facultyDoc);
            }
            
            for (Document doc : facultyData.values()) {
                report.add(doc);
            }
            
            return report;
        } catch (Exception e) {
            System.err.println("Error getting user report: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public Document getBorrowReport() {
        try {
            Document report = new Document();
            
            long totalBorrows = borrowRecordsCollection.countDocuments();
            long activeBorrows = borrowRecordsCollection.countDocuments(eq("status", "BORROWING"));
            long returnedBorrows = borrowRecordsCollection.countDocuments(eq("status", "RETURNED"));
            long lostBorrows = borrowRecordsCollection.countDocuments(eq("status", "LOST"));
            long damagedBorrows = borrowRecordsCollection.countDocuments(eq("status", "DAMAGED"));
            
            // Calculate overdue borrows (status = BORROWING and dueDate < today)
            java.util.Date today = new java.util.Date();
            long overdueBorrows = 0;
            for (Document record : borrowRecordsCollection.find(
                and(eq("status", "BORROWING"))
            )) {
                java.util.Date dueDate = record.getDate("dueDate");
                if (dueDate != null && dueDate.before(today)) {
                    overdueBorrows++;
                }
            }
            
            report.append("totalBorrows", totalBorrows)
                .append("activeBorrows", activeBorrows)
                .append("returnedBorrows", returnedBorrows)
                .append("overdueBorrows", overdueBorrows)
                .append("lostBorrows", lostBorrows)
                .append("damagedBorrows", damagedBorrows);
            
            return report;
        } catch (Exception e) {
            System.err.println("Error getting borrow report: " + e.getMessage());
            e.printStackTrace();
            return new Document();
        }
    }
    
    public List<Document> getPenaltyReport() {
        try {
            List<Document> report = new ArrayList<>();
            
            // Sort by createdDate descending (newest first)
            for (Document fine : finesCollection.find().sort(new Document("createdDate", -1))) {
                Document fineReport = new Document("fineId", fine.getString("fineId"))
                    .append("userId", fine.getString("userId"))
                    .append("bookId", fine.getString("bookId"))
                    .append("reason", fine.getString("reason"))
                    .append("amount", fine.get("amount"))
                    .append("status", fine.getString("status"))
                    .append("dueDate", fine.getDate("dueDate"))
                    .append("paidDate", fine.getDate("paidDate"))
                    .append("createdDate", fine.getDate("createdDate"));
                
                // Get user info
                Document userDoc = usersCollection.find(eq("userId", fine.getString("userId"))).first();
                if (userDoc != null) {
                    String firstName = userDoc.getString("firstName");
                    String lastName = userDoc.getString("lastName");
                    String username = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                    username = username.trim();
                    if (username.isEmpty()) {
                        username = userDoc.getString("email");
                    }
                    fineReport.append("username", username);
                } else {
                    fineReport.append("username", "N/A");
                }
                
                // Get book info
                Document bookDoc = booksCollection.find(eq("bookId", fine.getString("bookId"))).first();
                if (bookDoc != null) {
                    fineReport.append("bookTitle", bookDoc.getString("title"));
                } else {
                    fineReport.append("bookTitle", "N/A");
                }
                
                report.add(fineReport);
            }
            
            return report;
        } catch (Exception e) {
            System.err.println("Error getting penalty report: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
