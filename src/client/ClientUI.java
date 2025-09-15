package client;

import model.Book;
import model.User;
import server.LibraryInterface;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.rmi.Naming;
import java.util.List;

public class ClientUI extends JFrame {
    private LibraryInterface library;
    private User currentUser;

    private JTextField tfUsername, tfPassword;
    private JButton btnLogin, btnRegister;
    private JLabel lblStatus;

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField tfSearchTitle, tfSearchAuthor, tfSearchCategory;
    private JButton btnSearch, btnRefresh, btnBorrow, btnReturn;

    // Admin controls
    private JTextField tfId, tfTitle, tfAuthor, tfCategory;
    private JButton btnAdd, btnUpdate, btnDelete;

    public ClientUI() {
        setTitle("Library Client - Swing");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initRMI();
        initUI();
    }

    private void initRMI() {
        try {
            library = (LibraryInterface) Naming.lookup("rmi://localhost:1099/LibraryService");
            System.out.println("Connected to RMI server.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối server RMI:\n" + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void initUI() {
        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(320, 0));

        // Login panel
        JPanel login = new JPanel(new GridLayout(6, 1, 5, 5));
        login.setBorder(BorderFactory.createTitledBorder("Đăng nhập / Đăng ký"));
        tfUsername = new JTextField();
        tfPassword = new JPasswordField();
        btnLogin = new JButton("Đăng nhập");
        btnRegister = new JButton("Đăng ký");
        lblStatus = new JLabel("Chưa đăng nhập");

        login.add(new JLabel("Username:")); login.add(tfUsername);
        login.add(new JLabel("Password:")); login.add(tfPassword);
        JPanel pbtn = new JPanel(new FlowLayout());
        pbtn.add(btnLogin); pbtn.add(btnRegister);
        login.add(pbtn);
        login.add(lblStatus);

        left.add(login, BorderLayout.NORTH);

        // Admin book management
        JPanel adminPanel = new JPanel(new GridLayout(9,1,4,4));
        adminPanel.setBorder(BorderFactory.createTitledBorder("Quản lý sách (Admin)"));
        tfId = new JTextField();
        tfTitle = new JTextField();
        tfAuthor = new JTextField();
        tfCategory = new JTextField();
        btnAdd = new JButton("Thêm sách");
        btnUpdate = new JButton("Sửa sách");
        btnDelete = new JButton("Xóa sách");

        adminPanel.add(new JLabel("ID:")); adminPanel.add(tfId);
        adminPanel.add(new JLabel("Title:")); adminPanel.add(tfTitle);
        adminPanel.add(new JLabel("Author:")); adminPanel.add(tfAuthor);
        adminPanel.add(new JLabel("Category:")); adminPanel.add(tfCategory);
        JPanel apbtn = new JPanel(new FlowLayout());
        apbtn.add(btnAdd); apbtn.add(btnUpdate); apbtn.add(btnDelete);
        adminPanel.add(apbtn);

        left.add(adminPanel, BorderLayout.CENTER);

        // Right: book list + search + actions
        JPanel right = new JPanel(new BorderLayout());

        // Top search
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tfSearchTitle = new JTextField(12);
        tfSearchAuthor = new JTextField(12);
        tfSearchCategory = new JTextField(10);
        btnSearch = new JButton("Tìm");
        btnRefresh = new JButton("Tải lại");
        search.add(new JLabel("Title:")); search.add(tfSearchTitle);
        search.add(new JLabel("Author:")); search.add(tfSearchAuthor);
        search.add(new JLabel("Category:")); search.add(tfSearchCategory);
        search.add(btnSearch); search.add(btnRefresh);

        right.add(search, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[] {"ID", "Title", "Author", "Category", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        right.add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom actions
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnBorrow = new JButton("Mượn (dựa trên Book ID)");
        btnReturn = new JButton("Trả (dựa trên Book ID)");
        bottom.add(btnBorrow); bottom.add(btnReturn);
        right.add(bottom, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(left, BorderLayout.WEST);
        getContentPane().add(right, BorderLayout.CENTER);

        // Initial state: admin controls disabled until admin logs in
        setAdminControlsEnabled(false);
        addListeners();
        refreshTable();
    }

    private void setAdminControlsEnabled(boolean enabled) {
        tfId.setEnabled(enabled);
        tfTitle.setEnabled(enabled);
        tfAuthor.setEnabled(enabled);
        tfCategory.setEnabled(enabled);
        btnAdd.setEnabled(enabled);
        btnUpdate.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
    }

    private void addListeners() {
        btnLogin.addActionListener(e -> {
            try {
                String user = tfUsername.getText().trim();
                String pass = tfPassword.getText().trim();
                if (user.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nhập username và password.");
                    return;
                }
                User u = library.login(user, pass);
                if (u != null) {
                    currentUser = u;
                    lblStatus.setText("Đã đăng nhập: " + u.getUsername() + (u.isAdmin()? " (Admin)":""));
                    setAdminControlsEnabled(u.isAdmin());
                    JOptionPane.showMessageDialog(this, "Đăng nhập thành công.");
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Đăng nhập thất bại.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi đăng nhập: " + ex.getMessage());
            }
        });

        btnRegister.addActionListener(e -> {
            try {
                String user = tfUsername.getText().trim();
                String pass = tfPassword.getText().trim();
                if (user.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nhập username và password.");
                    return;
                }
                User u = new User();
                u.setUsername(user);
                u.setPassword(pass);
                u.setAdmin(false);
                boolean ok = library.register(u);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Đăng ký thành công, hãy đăng nhập.");
                } else {
                    JOptionPane.showMessageDialog(this, "Username đã tồn tại.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi đăng ký: " + ex.getMessage());
            }
        });

        btnRefresh.addActionListener(e -> refreshTable());

        btnSearch.addActionListener(e -> {
            try {
                String t = tfSearchTitle.getText().trim();
                String a = tfSearchAuthor.getText().trim();
                String c = tfSearchCategory.getText().trim();
                List<Book> results = library.searchBooks(t, a, c);
                loadBooksToTable(results);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm: " + ex.getMessage());
            }
        });

        btnBorrow.addActionListener(e -> {
            String bookId = JOptionPane.showInputDialog(this, "Nhập Book ID để mượn:");
            if (bookId == null || bookId.trim().isEmpty()) return;
            if (currentUser == null) {
                JOptionPane.showMessageDialog(this, "Bạn cần đăng nhập trước khi mượn.");
                return;
            }
            try {
                String res = library.borrowBook(bookId.trim(), currentUser.getId());
                JOptionPane.showMessageDialog(this, res);
                refreshTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi mượn: " + ex.getMessage());
            }
        });

        btnReturn.addActionListener(e -> {
            String bookId = JOptionPane.showInputDialog(this, "Nhập Book ID để trả:");
            if (bookId == null || bookId.trim().isEmpty()) return;
            if (currentUser == null) {
                JOptionPane.showMessageDialog(this, "Bạn cần đăng nhập trước khi trả.");
                return;
            }
            try {
                String res = library.returnBook(bookId.trim(), currentUser.getId());
                JOptionPane.showMessageDialog(this, res);
                refreshTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi trả: " + ex.getMessage());
            }
        });

        btnAdd.addActionListener(e -> {
            try {
                Book b = new Book(tfId.getText().trim(), tfTitle.getText().trim(),
                        tfAuthor.getText().trim(), tfCategory.getText().trim(), true);
                if (b.getId().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "ID không được bỏ trống.");
                    return;
                }
                boolean ok = library.addBook(b);
                JOptionPane.showMessageDialog(this, ok ? "Thêm thành công." : "ID đã tồn tại.");
                refreshTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi thêm sách: " + ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            try {
                Book b = new Book(tfId.getText().trim(), tfTitle.getText().trim(),
                        tfAuthor.getText().trim(), tfCategory.getText().trim(), true);
                boolean ok = library.updateBook(b);
                JOptionPane.showMessageDialog(this, ok ? "Cập nhật thành công." : "Không tìm thấy ID.");
                refreshTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi cập nhật sách: " + ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            try {
                String id = tfId.getText().trim();
                if (id.isEmpty()) { JOptionPane.showMessageDialog(this, "Nhập ID để xóa."); return;}
                boolean ok = library.deleteBook(id);
                JOptionPane.showMessageDialog(this, ok ? "Xóa thành công." : "Không tìm thấy ID.");
                refreshTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi xóa sách: " + ex.getMessage());
            }
        });

        // select row to populate admin fields
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int r = table.getSelectedRow();
                if (r >= 0) {
                    tfId.setText((String) tableModel.getValueAt(r,0));
                    tfTitle.setText((String) tableModel.getValueAt(r,1));
                    tfAuthor.setText((String) tableModel.getValueAt(r,2));
                    tfCategory.setText((String) tableModel.getValueAt(r,3));
                }
            }
        });
    }

    private void refreshTable() {
        try {
            List<Book> books = library.getAllBooks();
            loadBooksToTable(books);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải sách: " + ex.getMessage());
        }
    }

    private void loadBooksToTable(List<Book> books) {
        tableModel.setRowCount(0);
        for (Book b : books) {
            tableModel.addRow(new Object[] {
                b.getId(),
                b.getTitle(),
                b.getAuthor(),
                b.getCategory(),
                b.isAvailable() ? "Available" : "Borrowed"
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientUI ui = new ClientUI();
            ui.setVisible(true);
        });
    }
}
