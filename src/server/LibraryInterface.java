package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import model.Book;
import model.User;

public interface LibraryInterface extends Remote {
    // User
    boolean register(User user) throws RemoteException;
    User login(String username, String password) throws RemoteException;

    // Books (CRUD) - admin only on server side check
    boolean addBook(Book book) throws RemoteException;
    boolean updateBook(Book book) throws RemoteException;
    boolean deleteBook(String bookId) throws RemoteException;

    // Query
    List<Book> getAllBooks() throws RemoteException;
    List<Book> searchBooks(String title, String author, String category) throws RemoteException;

    // Borrow/Return
    String borrowBook(String bookId, String userId) throws RemoteException;
    String returnBook(String bookId, String userId) throws RemoteException;
}
