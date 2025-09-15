package server;

import model.Book;
import model.User;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

public class LibraryImpl extends UnicastRemoteObject implements LibraryInterface {
    private static final long serialVersionUID = 1L;

    private List<Book> books;
    private List<User> users;

    protected LibraryImpl() throws RemoteException {
        super();
        // Load from files (or start with sample data)
        books = DataStore.loadBooks();
        users = DataStore.loadUsers();

        if (books.isEmpty()) {
            // sample initial data
            books.add(new Book("B001", "Lập trình Java", "Nguyễn Văn A", "Lập trình", true));
            books.add(new Book("B002", "Mạng máy tính", "Trần Văn B", "Mạng", true));
            books.add(new Book("B003", "Cơ sở dữ liệu", "Lê Văn C", "Database", true));
            DataStore.saveBooks(books);
        }

        if (users.isEmpty()) {
            // create an admin default account
            users.add(new User("U001", "admin", "admin123", true));
            users.add(new User("U002", "user1", "password", false));
            DataStore.saveUsers(users);
        }
    }

    // ---------------- User ----------------
    @Override
    public synchronized boolean register(User user) throws RemoteException {
        // check username exists
        boolean exists = users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(user.getUsername()));
        if (exists) return false;
        user.setId(UUID.randomUUID().toString());
        users.add(user);
        DataStore.saveUsers(users);
        return true;
    }

    @Override
    public User login(String username, String password) throws RemoteException {
        return users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    // ---------------- Book CRUD ----------------
    @Override
    public synchronized boolean addBook(Book book) throws RemoteException {
        if (books.stream().anyMatch(b -> b.getId().equals(book.getId()))) return false;
        books.add(book);
        DataStore.saveBooks(books);
        return true;
    }

    @Override
    public synchronized boolean updateBook(Book book) throws RemoteException {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId().equals(book.getId())) {
                books.set(i, book);
                DataStore.saveBooks(books);
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean deleteBook(String bookId) throws RemoteException {
        boolean removed = books.removeIf(b -> b.getId().equals(bookId));
        if (removed) DataStore.saveBooks(books);
        return removed;
    }

    // ---------------- Query ----------------
    @Override
    public synchronized List<Book> getAllBooks() throws RemoteException {
        // return a copy to avoid remote modification
        return new ArrayList<>(books);
    }

    @Override
    public synchronized List<Book> searchBooks(String title, String author, String category) throws RemoteException {
        return books.stream()
                .filter(b -> (title == null || title.isEmpty() || b.getTitle().toLowerCase().contains(title.toLowerCase())) &&
                             (author == null || author.isEmpty() || b.getAuthor().toLowerCase().contains(author.toLowerCase())) &&
                             (category == null || category.isEmpty() || b.getCategory().toLowerCase().contains(category.toLowerCase()))
                ).collect(Collectors.toList());
    }

    // ---------------- Borrow/Return ----------------
    @Override
    public synchronized String borrowBook(String bookId, String userId) throws RemoteException {
        for (Book b : books) {
            if (b.getId().equals(bookId)) {
                if (!b.isAvailable()) {
                    return "Sách hiện đang được mượn.";
                }
                b.setAvailable(false);
                DataStore.saveBooks(books);
                return "Mượn thành công: " + b.getTitle();
            }
        }
        return "Không tìm thấy sách với ID: " + bookId;
    }

    @Override
    public synchronized String returnBook(String bookId, String userId) throws RemoteException {
        for (Book b : books) {
            if (b.getId().equals(bookId)) {
                if (b.isAvailable()) {
                    return "Sách này đang có sẵn, không thể trả.";
                }
                b.setAvailable(true);
                DataStore.saveBooks(books);
                return "Trả thành công: " + b.getTitle();
            }
        }
        return "Không tìm thấy sách với ID: " + bookId;
    }
}
