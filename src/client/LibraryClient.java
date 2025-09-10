// Kết nối server

package client;

import server.LibraryInterface;
import java.rmi.Naming;
import java.util.List;
import model.Book;

public class LibraryClient {
    public static void main(String[] args) {
        try {
            LibraryInterface library = 
                (LibraryInterface) Naming.lookup("rmi://localhost:1099/LibraryService");
            
            List<Book> books = library.getAllBooks();
            System.out.println("Danh sách sách trong thư viện:");
            for (Book b : books) {
                System.out.println(b);
            }

            System.out.println(library.borrowBook("B001"));
            System.out.println(library.returnBook("B001"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
