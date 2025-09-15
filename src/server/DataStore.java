package server;

import model.Book;
import model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple file-based storage. Data saved in files:
 * - books.db
 * - users.db
 *
 * This is simple serialization. For production use DB (MySQL/SQLite + JDBC).
 */
public class DataStore {
    private static final String BOOK_FILE = "books.db";
    private static final String USER_FILE = "users.db";

    @SuppressWarnings("unchecked")
    public static List<Book> loadBooks() {
        File f = new File(BOOK_FILE);
        if (!f.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (List<Book>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveBooks(List<Book> books) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BOOK_FILE))) {
            oos.writeObject(books);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static List<User> loadUsers() {
        File f = new File(USER_FILE);
        if (!f.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (List<User>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveUsers(List<User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            oos.writeObject(users);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
