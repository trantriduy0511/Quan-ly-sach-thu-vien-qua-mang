// Interface RMI

package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import model.Book;

public interface LibraryInterface extends Remote {
    List<Book> getAllBooks() throws RemoteException;
    String borrowBook(String bookId) throws RemoteException;
    String returnBook(String bookId) throws RemoteException;
}

