package server;

import model.Book;
import model.BookCopy;
import model.BorrowRecord;
import model.User;
import util.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Server {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private DatabaseManager dbManager;
    private boolean running;
    
    public Server() {
        dbManager = new DatabaseManager();
        running = false;
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("Server started on port " + PORT);
            System.out.println("Waiting for clients...");
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                ClientHandler handler = new ClientHandler(clientSocket, dbManager);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            stop();
        }
    }
    
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (dbManager != null) {
                dbManager.close();
            }
            System.out.println("Server stopped");
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
    
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private DatabaseManager dbManager;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private User currentUser;
        
        public ClientHandler(Socket socket, DatabaseManager dbManager) {
            this.socket = socket;
            this.dbManager = dbManager;
            this.currentUser = null;
        }
        
        @Override
        public void run() {
            try {
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());
                
                while (!socket.isClosed()) {
                    Message request = (Message) input.readObject();
                    Message response = handleRequest(request);
                    output.writeObject(response);
                    output.flush();
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Client disconnected: " + socket.getInetAddress());
            } finally {
                close();
            }
        }
        
        private Message handleRequest(Message request) {
            Message response = new Message();
            response.setType(request.getType());
            
            // Check if user is locked (except for LOGIN, REGISTER, LOGOUT)
            if (currentUser != null && !request.getType().equals(Message.LOGIN) && 
                !request.getType().equals(Message.REGISTER) && !request.getType().equals(Message.LOGOUT)) {
                // Re-check user status from database
                User userCheck = dbManager.getUserByEmail(currentUser.getEmail());
                if (userCheck != null && "LOCKED".equals(userCheck.getStatus())) {
                    // Force logout - send FORCE_LOGOUT message
                    response.setType(Message.FORCE_LOGOUT);
                    response.setSuccess(false);
                    response.setMessage("Tài khoản của bạn đã bị khóa. Bạn sẽ bị đăng xuất.");
                    currentUser = null;
                    return response;
                }
            }
            
            try {
                switch (request.getType()) {
                    case Message.LOGIN:
                        handleLogin(request, response);
                        break;
                    case Message.REGISTER:
                        handleRegister(request, response);
                        break;
                    case Message.LOGOUT:
                        currentUser = null;
                        response.setSuccess(true);
                        response.setMessage("Logged out successfully");
                        break;
                    case Message.GET_ALL_BOOKS:
                        if (checkAuth()) {
                            List<Book> books = dbManager.getAllBooks();
                            response.setData(books);
                            response.setSuccess(true);
                        }
                        break;
                    case Message.SEARCH_BOOKS:
                        if (checkAuth()) {
                            String keyword = (String) request.getData();
                            List<Book> books = dbManager.searchBooks(keyword);
                            response.setData(books);
                            response.setSuccess(true);
                        }
                        break;
                    case Message.ADD_BOOK:
                        if (checkAdminAuth()) {
                            Book book = (Book) request.getData();
                            boolean result = dbManager.addBook(book);
                            response.setSuccess(result);
                            response.setMessage(result ? "Thêm sách thành công" : "Thêm sách thất bại");
                        }
                        break;
                    case Message.UPDATE_BOOK:
                        if (checkAdminAuth()) {
                            Book book = (Book) request.getData();
                            boolean result = dbManager.updateBook(book);
                            response.setSuccess(result);
                            response.setMessage(result ? "Cập nhật sách thành công" : "Cập nhật sách thất bại");
                        }
                        break;
                    case Message.DELETE_BOOK:
                        if (checkAdminAuth()) {
                            String bookId = (String) request.getData();
                            boolean result = dbManager.deleteBook(bookId);
                            response.setSuccess(result);
                            response.setMessage(result ? "Xóa sách thành công" : "Xóa sách thất bại");
                        }
                        break;
                    case Message.GET_ALL_USERS:
                        if (checkAdminAuth()) {
                            List<User> users = dbManager.getAllUsers();
                            response.setData(users);
                            response.setSuccess(true);
                        }
                        break;
                    case Message.ADD_USER:
                        if (checkAdminAuth()) {
                            User user = (User) request.getData();
                            boolean result = dbManager.addUser(user);
                            response.setSuccess(result);
                            response.setMessage(result ? "Thêm người dùng thành công" : "Thêm người dùng thất bại");
                        }
                        break;
                    case Message.UPDATE_USER:
                        if (checkAuth()) {
                            User user = (User) request.getData();
                            // Check if user is updating their own profile or is admin
                            if (currentUser.getRole() != null && currentUser.getRole().equals("ADMIN")) {
                                // Admin can update any user
                                boolean result = dbManager.updateUser(user);
                                response.setSuccess(result);
                                response.setMessage(result ? "Cập nhật người dùng thành công" : "Cập nhật người dùng thất bại");
                            } else {
                                // Regular user can only update their own profile
                                if (user.getUserId() != null && user.getUserId().equals(currentUser.getUserId())) {
                                    // Don't allow user to change role or status
                                    user.setRole(currentUser.getRole());
                                    user.setStatus(currentUser.getStatus());
                                    boolean result = dbManager.updateUser(user);
                                    response.setSuccess(result);
                                    response.setMessage(result ? "Cập nhật thông tin thành công" : "Cập nhật thông tin thất bại");
                                } else {
                                    response.setSuccess(false);
                                    response.setMessage("Bạn không có quyền cập nhật thông tin người dùng khác!");
                                }
                            }
                        }
                        break;
                    case Message.DELETE_USER:
                        if (checkAdminAuth()) {
                            String userId = (String) request.getData();
                            boolean result = dbManager.deleteUser(userId);
                            response.setSuccess(result);
                            response.setMessage(result ? "Xóa người dùng thành công" : "Xóa người dùng thất bại");
                        }
                        break;
                    case Message.LOCK_USER:
                        if (checkAdminAuth()) {
                            String userId = (String) request.getData();
                            boolean result = dbManager.lockUser(userId);
                            response.setSuccess(result);
                            response.setMessage(result ? "Khóa tài khoản thành công" : "Khóa tài khoản thất bại");
                        }
                        break;
                    case Message.UNLOCK_USER:
                        if (checkAdminAuth()) {
                            String userId = (String) request.getData();
                            boolean result = dbManager.unlockUser(userId);
                            response.setSuccess(result);
                            response.setMessage(result ? "Mở khóa tài khoản thành công" : "Mở khóa tài khoản thất bại");
                        }
                        break;
                    case Message.RESET_PASSWORD:
                        if (checkAdminAuth()) {
                            Object[] params = (Object[]) request.getData();
                            String userId = (String) params[0];
                            String newPassword = (String) params[1];
                            boolean result = dbManager.resetPassword(userId, newPassword);
                            response.setSuccess(result);
                            response.setMessage(result ? "Đặt lại mật khẩu thành công" : "Đặt lại mật khẩu thất bại");
                        }
                        break;
                    case Message.BORROW_BOOK:
                        if (checkAuth()) {
                            Object[] params = (Object[]) request.getData();
                            String userId = (String) params[0];
                            String bookId = (String) params[1];
                            boolean result = dbManager.borrowBook(userId, bookId);
                            if (result) {
                                response.setSuccess(true);
                                response.setMessage("Mượn sách thành công");
                            } else {
                                // Get settings to check max borrow limit
                                org.bson.Document settings = dbManager.getSettings();
                                int maxBorrowBooks = settings != null ? settings.getInteger("maxBorrowBooks", 5) : 5;
                                response.setSuccess(false);
                                response.setMessage("Mượn sách thất bại (không còn sách hoặc đã đạt giới hạn " + maxBorrowBooks + " quyển)");
                            }
                        }
                        break;
                    case Message.RETURN_BOOK:
                        if (checkAuth()) {
                            String recordId = (String) request.getData();
                            boolean result = dbManager.returnBook(recordId);
                            response.setSuccess(result);
                            response.setMessage(result ? "Trả sách thành công" : "Trả sách thất bại");
                        }
                        break;
                    case Message.RENEW_BOOK:
                        if (checkAuth()) {
                            String recordId = (String) request.getData();
                            boolean result = dbManager.renewBook(recordId);
                            if (result) {
                                // Get renewal days from settings for message
                                org.bson.Document settings = dbManager.getSettings();
                                int renewalDays = settings != null ? settings.getInteger("renewalDays", 7) : 7;
                                response.setSuccess(true);
                                response.setMessage("Gia hạn sách thành công (thêm " + renewalDays + " ngày)");
                            } else {
                                response.setSuccess(false);
                                response.setMessage("Gia hạn sách thất bại (sách không tồn tại hoặc không thể gia hạn)");
                            }
                        }
                        break;
                    case Message.MARK_LOST:
                        if (checkAuth()) {
                            String recordId = (String) request.getData();
                            // Check if user owns this record (if not admin)
                            if (currentUser.getRole() != null && !currentUser.getRole().equals("ADMIN")) {
                                if (!dbManager.isRecordOwnedByUser(recordId, currentUser.getUserId())) {
                                    response.setSuccess(false);
                                    response.setMessage("Bạn không có quyền thực hiện thao tác này!");
                                    break;
                                }
                            }
                            boolean result = dbManager.markAsLost(recordId);
                            response.setSuccess(result);
                            response.setMessage(result ? "Báo mất sách thành công" : "Báo mất sách thất bại");
                        }
                        break;
                    case Message.MARK_DAMAGED:
                        if (checkAuth()) {
                            String recordId = (String) request.getData();
                            // Check if user owns this record (if not admin)
                            if (currentUser.getRole() != null && !currentUser.getRole().equals("ADMIN")) {
                                if (!dbManager.isRecordOwnedByUser(recordId, currentUser.getUserId())) {
                                    response.setSuccess(false);
                                    response.setMessage("Bạn không có quyền thực hiện thao tác này!");
                                    break;
                                }
                            }
                            boolean result = dbManager.markAsDamaged(recordId);
                            response.setSuccess(result);
                            response.setMessage(result ? "Báo hỏng sách thành công" : "Báo hỏng sách thất bại");
                        }
                        break;
                    case Message.FORCE_RETURN:
                        if (checkAdminAuth()) {
                            String recordId = (String) request.getData();
                            boolean result = dbManager.forceReturn(recordId);
                            response.setSuccess(result);
                            response.setMessage(result ? "Bắt buộc trả thành công. Người dùng đã nhận được thông báo." : "Bắt buộc trả thất bại");
                        }
                        break;
                    case Message.GET_USER_NOTIFICATIONS:
                        if (checkAuth()) {
                            String userId = currentUser.getUserId();
                            List<org.bson.Document> notifications = dbManager.getUserNotifications(userId);
                            response.setData(notifications);
                            response.setSuccess(true);
                        }
                        break;
                    case Message.MARK_NOTIFICATION_READ:
                        if (checkAuth()) {
                            String notificationId = (String) request.getData();
                            boolean result = dbManager.markNotificationAsRead(notificationId);
                            response.setSuccess(result);
                            response.setMessage(result ? "Đã đánh dấu đã đọc" : "Lỗi đánh dấu đã đọc");
                        }
                        break;
                    case Message.CHECK_USER_STATUS:
                        if (checkAuth()) {
                            // Re-check user status from database
                            User userCheck = dbManager.getUserByEmail(currentUser.getEmail());
                            if (userCheck != null) {
                                if ("LOCKED".equals(userCheck.getStatus())) {
                                    // User is locked, force logout
                                    response.setType(Message.FORCE_LOGOUT);
                                    response.setSuccess(false);
                                    response.setMessage("Tài khoản của bạn đã bị khóa. Bạn sẽ bị đăng xuất.");
                                    currentUser = null;
                                } else {
                                    response.setSuccess(true);
                                    response.setMessage("ACTIVE");
                                }
                            } else {
                                response.setSuccess(false);
                                response.setMessage("Không tìm thấy thông tin người dùng");
                            }
                        }
                        break;
                    case Message.GET_SETTINGS:
                        // Allow both admin and regular users to read settings (for display purposes)
                        if (checkAuth()) {
                            org.bson.Document settings = dbManager.getSettings();
                            if (settings != null) {
                                response.setData(settings);
                                response.setSuccess(true);
                            } else {
                                response.setSuccess(false);
                                response.setMessage("Không thể tải cài đặt hệ thống");
                            }
                        }
                        break;
                    case Message.UPDATE_SETTINGS:
                        if (checkAdminAuth()) {
                            org.bson.Document settings = (org.bson.Document) request.getData();
                            boolean result = dbManager.updateSettings(settings);
                            response.setSuccess(result);
                            response.setMessage(result ? "Cập nhật cài đặt thành công" : "Cập nhật cài đặt thất bại");
                        }
                        break;
                    case Message.GET_USER_BORROW_RECORDS:
                        if (checkAuth()) {
                            String userId = (String) request.getData();
                            List<BorrowRecord> records = dbManager.getUserBorrowRecords(userId);
                            response.setData(records);
                            response.setSuccess(true);
                        }
                        break;
                    case Message.GET_ALL_BORROW_RECORDS:
                        if (checkAdminAuth()) {
                            List<BorrowRecord> records = dbManager.getAllBorrowRecords();
                            response.setData(records);
                            response.setSuccess(true);
                        }
                        break;
                    case Message.GET_DASHBOARD_STATS:
                        if (checkAdminAuth()) {
                            org.bson.Document stats = dbManager.getDashboardStats();
                            response.setData(stats);
                            response.setSuccess(true);
                        }
                        break;
                    case Message.GET_BOOK_REPORT:
                        if (checkAdminAuth()) {
                            List<org.bson.Document> report = dbManager.getBookReport();
                            response.setData(report);
                            response.setSuccess(true);
                        }
                        break;
                    case Message.GET_USER_REPORT:
                        if (checkAdminAuth()) {
                            List<org.bson.Document> report = dbManager.getUserReport();
                            response.setData(report);
                            response.setSuccess(true);
                        }
                        break;
                    case Message.GET_BORROW_REPORT:
                        if (checkAdminAuth()) {
                            org.bson.Document report = dbManager.getBorrowReport();
                            response.setData(report);
                            response.setSuccess(true);
                        }
                        break;
                    case Message.GET_PENALTY_REPORT:
                        if (checkAdminAuth()) {
                            List<org.bson.Document> report = dbManager.getPenaltyReport();
                            response.setData(report);
                            response.setSuccess(true);
                        }
                        break;
                    case Message.GET_BOOK_BY_ID:
                        if (checkAdminAuth()) {
                            String bookId = (String) request.getData();
                            Book book = dbManager.getBookById(bookId);
                            response.setData(book);
                            response.setSuccess(book != null);
                            response.setMessage(book != null ? "OK" : "Không tìm thấy sách");
                        }
                        break;
                    case Message.GET_BOOK_COPIES:
                        if (checkAdminAuth()) {
                            String bookId = (String) request.getData();
                            List<BookCopy> copies = dbManager.getBookCopies(bookId);
                            response.setData(copies);
                            response.setSuccess(true);
                        }
                        break;
                    case Message.ADD_BOOK_COPY:
                        if (checkAdminAuth()) {
                            BookCopy copy = (BookCopy) request.getData();
                            boolean result = dbManager.addBookCopy(copy);
                            response.setSuccess(result);
                            response.setMessage(result ? "Thêm bản sao thành công" : "Thêm bản sao thất bại");
                        }
                        break;
                    case Message.DELETE_BOOK_COPY:
                        if (checkAdminAuth()) {
                            String copyId = (String) request.getData();
                            boolean result = dbManager.deleteBookCopy(copyId);
                            response.setSuccess(result);
                            response.setMessage(result ? "Xóa bản sao thành công" : "Xóa bản sao thất bại");
                        }
                        break;
                    default:
                        response.setSuccess(false);
                        response.setMessage("Unknown request type");
                }
            } catch (Exception e) {
                response.setSuccess(false);
                response.setMessage("Error: " + e.getMessage());
                e.printStackTrace();
            }
            
            return response;
        }
        
        private void handleLogin(Message request, Message response) {
            try {
                Object[] credentials = (Object[]) request.getData();
                String email = (String) credentials[0];
                String password = (String) credentials[1];
                
                // Check if user exists and is locked
                User user = dbManager.login(email, password);
                if (user != null) {
                    // Double check status in case it was changed
                    if ("LOCKED".equals(user.getStatus())) {
                        response.setSuccess(false);
                        response.setMessage("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
                        return;
                    }
                    currentUser = user;
                    response.setSuccess(true);
                    response.setData(user);
                    response.setMessage("Đăng nhập thành công");
                } else {
                    // Check if email exists but password is wrong or account is locked
                    User checkUser = dbManager.getUserByEmail(email);
                    if (checkUser != null && "LOCKED".equals(checkUser.getStatus())) {
                        response.setSuccess(false);
                        response.setMessage("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
                    } else {
                        response.setSuccess(false);
                        response.setMessage("Email hoặc mật khẩu không đúng");
                    }
                }
            } catch (Exception e) {
                response.setSuccess(false);
                response.setMessage("Lỗi đăng nhập: " + e.getMessage());
            }
        }
        
        private void handleRegister(Message request, Message response) {
            try {
                User user = (User) request.getData();
                boolean result = dbManager.register(user);
                if (result) {
                    response.setSuccess(true);
                    response.setMessage("Đăng ký thành công");
                } else {
                    response.setSuccess(false);
                    response.setMessage("Email đã tồn tại hoặc có lỗi xảy ra");
                }
            } catch (Exception e) {
                response.setSuccess(false);
                response.setMessage("Lỗi đăng ký: " + e.getMessage());
            }
        }
        
        private boolean checkAuth() {
            if (currentUser == null) {
                return false;
            }
            // Check if user is locked - if so, they should be logged out
            if ("LOCKED".equals(currentUser.getStatus())) {
                return false;
            }
            return true;
        }
        
        private boolean checkAdminAuth() {
            if (currentUser == null || !currentUser.isAdmin()) {
                return false;
            }
            return true;
        }
        
        private void close() {
            try {
                if (input != null) input.close();
                if (output != null) output.close();
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing client connection: " + e.getMessage());
            }
        }
    }
}

