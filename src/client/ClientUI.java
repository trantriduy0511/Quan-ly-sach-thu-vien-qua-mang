// Giao diện Java Swing cho client

package client;

import server.LibraryInterface;
import model.Book;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.Naming;
import java.util.List;

public class ClientUI extends JFrame {
    private LibraryInterface library;
    private JTextArea textArea;

    public ClientUI() {
        setTitle("Library Client");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        textArea = new JTextArea();
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel panel = new JPanel();
        JTextField bookIdField = new JTextField(10);
        JButton borrowBtn = new JButton("Mượn");
        JButton returnBtn = new JButton("Trả");

        panel.add(new JLabel("Book ID:"));
        panel.add(bookIdField);
        panel.add(borrowBtn);
        panel.add(returnBtn);

        add(panel, BorderLayout.SOUTH);

        try {
            library = (LibraryInterface) Naming.lookup("rmi://localhost:1099/LibraryService");
            loadBooks();
        } catch (Exception e) {
            textArea.setText("Không thể kết nối server");
            e.printStackTrace();
        }

        borrowBtn.addActionListener(e -> {
            try {
                String result = library.borrowBook(bookIdField.getText());
                JOptionPane.showMessageDialog(this, result);
                loadBooks();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        returnBtn.addActionListener(e -> {
            try {
                String result = library.returnBook(bookIdField.getText());
                JOptionPane.showMessageDialog(this, result);
                loadBooks();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void loadBooks() {
        try {
            List<Book> books = library.getAllBooks();
            textArea.setText("");
            for (Book b : books) {
                textArea.append(b.toString() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientUI().setVisible(true));
    }
}

