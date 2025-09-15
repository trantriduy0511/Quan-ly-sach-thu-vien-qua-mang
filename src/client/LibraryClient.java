package client;

import model.Book;
import model.User;
import server.LibraryInterface;

import java.rmi.Naming;
import java.util.List;

public class LibraryClient {
    public static void main(String[] args) {
        try {
            LibraryInterface lib = (LibraryInterface) Naming.lookup("rmi://localhost:1099/LibraryService");
            System.out.println("Connected to LibraryService");

            // list all books
            List<Book> books = lib.getAllBooks();
            books.forEach(System.out::println);

            // try login
            User u = lib.login("admin", "admin123");
            System.out.println("Login: " + (u != null ? u : "failed"));

            // borrow
            System.out.println(lib.borrowBook("B001", u != null ? u.getId() : "unknown"));

            // return
            System.out.println(lib.returnBook("B001", u != null ? u.getId() : "unknown"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
