// Cài đặt interface

package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import model.Book;

public class LibraryImpl extends UnicastRemoteObject implements LibraryInterface {
    private static final long serialVersionUID = 1L;
    private List<Book> books;

    protected LibraryImpl() throws RemoteException {
        super();
        books = new ArrayList<>();
        books.add(new Book("B001", "Lập trình Java", "Nguyễn Văn A", true));
        books.add(new Book("B002", "Mạng máy tính", "Trần Văn B", true));
        books.add(new Book("B003", "Cơ sở dữ liệu", "Lê Văn C", true));
    }

    @Override
    public List<Book> getAllBooks() {
        return books;
    }

    @Override
    public String borrowBook(String bookId) {
        for (Book b : books) {
            if (b.getId().equals(bookId) && b.isAvailable()) {
                b.setAvailable(false);
                return "Mượn thành công: " + b.getTitle();
            }
        }
        return "Không thể mượn sách.";
    }

    @Override
    public String returnBook(String bookId) {
        for (Book b : books) {
            if (b.getId().equals(bookId) && !b.isAvailable()) {
                b.setAvailable(true);
                return "Trả thành công: " + b.getTitle();
            }
        }
        return "Không thể trả sách.";
    }
}

