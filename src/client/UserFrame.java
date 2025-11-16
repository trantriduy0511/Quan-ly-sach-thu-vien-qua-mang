package client;

import model.Book;
import model.BorrowRecord;
import model.User;
import util.Message;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import org.bson.Document;

// Custom rounded border class
class RoundedBorderUser implements Border {
    private int radius;
    private Color color;
    
    RoundedBorderUser(int radius, Color color) {
        this.radius = radius;
        this.color = color;
    }
    
    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(this.radius + 1, this.radius + 1, this.radius + 2, this.radius + 1);
    }
    
    @Override
    public boolean isBorderOpaque() {
        return false;
    }
    
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
        g2d.dispose();
    }
}

public class UserFrame extends JFrame {
    private Client client;
    private User currentUser;
    
    // Tables
    private JTable searchBooksTable;
    private JTable borrowBooksTable;
    private JTable myBorrowsTable;
    private JTable newBooksTable;
    private JTable currentBorrowsTable;
    private JTable homeCurrentBorrowsTable; // Separate table for home panel
    
    // Table models
    private DefaultTableModel searchBooksModel;
    private DefaultTableModel borrowBooksModel;
    private DefaultTableModel myBorrowsModel;
    private DefaultTableModel newBooksModel;
    private DefaultTableModel currentBorrowsModel;
    private DefaultTableModel homeCurrentBorrowsModel; // Separate model for home panel
    
    private JTabbedPane mainTabbedPane;
    
    // Home panel components
    private JLabel homeStatsLabel;
    private JLabel welcomeLabel;
    
    // Borrow panel components
    private JLabel borrowStatsLabel;
    private JLabel lastUpdatedLabel;
    private JTabbedPane borrowSubTabs;
    
    // Footer components
    private JLabel loginLabel;
    
    // Search field
    private JTextField searchField;
    private JComboBox<String> categoryComboBox;
    private JLabel searchResultLabel;
    
    // Personal info fields
    private JTextField emailField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField phoneField;
    private JTextArea addressArea;
    private JTextField studentIdField;
    private JTextField facultyField;
    private JComboBox<String> yearOfStudyComboBox;
    private JLabel totalBorrowedLabel;
    private JLabel currentBorrowedLabel;
    private JLabel totalFinesLabel;
    private JLabel statusLabel;
    
    // Notification timer
    private javax.swing.Timer notificationTimer;
    private long lastNotificationCheckTime;
    
    // Account status check timer
    private javax.swing.Timer accountStatusTimer;
    
    // Data sync timer - auto refresh data from server
    private javax.swing.Timer dataSyncTimer;
    
    // Account locked flag
    private boolean isAccountLocked = false;
    
    // Dark royal blue color scheme - matching image
    private final Color DARK_ROYAL_BLUE = new Color(25, 25, 112); // Dark royal blue
    private final Color ROYAL_BLUE = new Color(30, 58, 138); // Slightly lighter royal blue
    private final Color PRIMARY_BLUE = new Color(30, 58, 138); // Royal blue for buttons
    private final Color PRIMARY_GREEN = new Color(52, 199, 89); // Keep green for some actions
    private final Color PRIMARY_ORANGE = new Color(255, 149, 0); // Bright orange
    private final Color PRIMARY_RED = new Color(255, 59, 48); // Bright red
    private final Color PRIMARY_PURPLE = new Color(175, 82, 222); // Bright purple
    private final Color PRIMARY_YELLOW = new Color(255, 204, 0); // Bright yellow
    private final Color PRIMARY_GRAY = new Color(142, 142, 147); // Gray
    private final Color BG_LIGHT = new Color(242, 242, 247); // Light background
    private final Color PANEL_WHITE = Color.WHITE;
    
    public UserFrame(Client client, User user) {
        this.client = client;
        this.currentUser = user;
        // Initialize lastNotificationCheckTime to current time to avoid showing old notifications on first load
        this.lastNotificationCheckTime = System.currentTimeMillis();
        initializeComponents();
        setupLayout();
        loadData();
        startNotificationTimer();
    }
    
    private void initializeComponents() {
        setTitle("Hệ thống quản lý thư viện - " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        
        // Search books table - updated columns
        String[] searchColumns = {"Tên sách", "Tác giả", "Thể loại", "Năm xuất bản", "Số trang", "Giá (VND)", "Có sẵn", "Tổng số"};
        searchBooksModel = new DefaultTableModel(searchColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        searchBooksTable = new JTable(searchBooksModel);
        styleTable(searchBooksTable);
        searchBooksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Borrow books table - will be used for available books to borrow
        String[] borrowColumns = {"ID", "Tên sách", "Tác giả", "Thể loại", "Còn lại", "Mô tả"};
        borrowBooksModel = new DefaultTableModel(borrowColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        borrowBooksTable = new JTable(borrowBooksModel);
        styleTable(borrowBooksTable);
        borrowBooksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Current borrows table for borrow management panel
        String[] currentBorrowsColumns = {"Mã sách", "Tên sách", "Ngày mượn", "Hạn trả", "Còn lại", "Trạng thái"};
        currentBorrowsModel = new DefaultTableModel(currentBorrowsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        currentBorrowsTable = new JTable(currentBorrowsModel);
        styleTable(currentBorrowsTable);
        currentBorrowsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Home current borrows table - separate table for home panel
        homeCurrentBorrowsModel = new DefaultTableModel(currentBorrowsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        homeCurrentBorrowsTable = new JTable(homeCurrentBorrowsModel);
        styleTable(homeCurrentBorrowsTable);
        homeCurrentBorrowsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // My borrows table - updated columns for home panel
        String[] borrowRecordColumns = {"Sách", "Ngày mượn", "Hạn trả", "Trạng thái", "Phạt"};
        myBorrowsModel = new DefaultTableModel(borrowRecordColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        myBorrowsTable = new JTable(myBorrowsModel);
        styleTable(myBorrowsTable);
        myBorrowsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // New books table for home panel
        String[] newBooksColumns = {"Tiêu đề", "Tác giả", "Thể loại", "Năm xuất bản", "Trạng thái"};
        newBooksModel = new DefaultTableModel(newBooksColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        newBooksTable = new JTable(newBooksModel);
        styleTable(newBooksTable);
        newBooksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        searchField = createRoundedTextField(20);
        categoryComboBox = new JComboBox<>(new String[]{"Tất cả thể loại"});
        styleComboBox(categoryComboBox);
        searchResultLabel = new JLabel("Tìm thấy 0 quyển sách");
        searchResultLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchResultLabel.setForeground(new Color(66, 133, 244));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_LIGHT);
        
        // Top panel - Header with DARK ROYAL BLUE background (matching image)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(DARK_ROYAL_BLUE); // Dark royal blue header matching image
        topPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        
        // Left side - Title and subtitle
        JPanel leftTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftTopPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Hệ thống quản lý thư viện - " + currentUser.getFullName());
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(Color.WHITE);
        leftTopPanel.add(titleLabel);
        JLabel subtitleLabel = new JLabel("Thư viện Online");
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        subtitleLabel.setForeground(Color.WHITE);
        leftTopPanel.add(subtitleLabel);
        topPanel.add(leftTopPanel, BorderLayout.WEST);
        
        // Right side - User name and Logout button
        JPanel rightTopPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightTopPanel.setOpaque(false);
        JLabel userNameLabel = new JLabel(currentUser.getFullName());
        userNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userNameLabel.setForeground(Color.WHITE);
        rightTopPanel.add(userNameLabel);
        JButton logoutButton = createStyledButton("Đăng xuất", ROYAL_BLUE, null); // Royal blue
        logoutButton.setForeground(Color.WHITE);
        logoutButton.addActionListener(e -> logout());
        rightTopPanel.add(logoutButton);
        topPanel.add(rightTopPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Navigation bar - Light gray background (matching image)
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        navBar.setBackground(new Color(240, 240, 240)); // Light gray matching image
        navBar.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        
        // Create navigation buttons (tabs)
        JButton homeNavBtn = createNavButton("Trang chủ", 0);
        JButton searchNavBtn = createNavButton("Tìm sách", 1);
        JButton borrowNavBtn = createNavButton("Mượn sách", 2);
        JButton infoNavBtn = createNavButton("Thông tin", 3);
        
        navBar.add(homeNavBtn);
        navBar.add(searchNavBtn);
        navBar.add(borrowNavBtn);
        navBar.add(infoNavBtn);
        
        add(navBar, BorderLayout.NORTH);
        
        // Center panel - Tabbed pane with styled tabs (tabs are hidden, using nav bar instead)
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mainTabbedPane.setBackground(Color.WHITE); // White background matching image
        
        // Hide tab labels since we're using navigation bar
        mainTabbedPane.setTabPlacement(JTabbedPane.TOP);
        mainTabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
                return 0; // Hide tabs
            }
        });
        
        // Tab 1: Trang chủ
        mainTabbedPane.addTab("", createHomePanel());
        
        // Tab 2: Tìm sách
        mainTabbedPane.addTab("", createSearchPanel());
        
        // Tab 3: Mượn sách
        mainTabbedPane.addTab("", createBorrowPanel());
        
        // Tab 4: Thông tin cá nhân
        mainTabbedPane.addTab("", createProfilePanel());
        
        add(mainTabbedPane, BorderLayout.CENTER);
        
        // Add change listener to refresh data and update nav buttons when switching tabs
        mainTabbedPane.addChangeListener(e -> {
            int selectedIndex = mainTabbedPane.getSelectedIndex();
            // Update nav buttons
            updateNavButtons(selectedIndex);
            
            // Refresh data based on selected tab
            if (selectedIndex == 0) {
                // Trang chủ - refresh current borrows and new books
                System.out.println("Refreshing Trang chủ tab...");
                loadHomeCurrentBorrows(); // Load for home panel
                loadNewBooks();
                refreshUserStats();
                // Force repaint
                SwingUtilities.invokeLater(() -> {
                    mainTabbedPane.revalidate();
                    mainTabbedPane.repaint();
                });
            } else if (selectedIndex == 1) {
                // Tìm sách - refresh search books
                loadAllBooksForSearch();
            } else if (selectedIndex == 2) {
                // Mượn sách - refresh current borrows
                loadCurrentBorrows();
                refreshUserStats();
                updateBorrowStats();
            }
        });
        
        // Force initial load for home tab
        SwingUtilities.invokeLater(() -> {
            loadHomeCurrentBorrows(); // Load for home panel
            loadNewBooks();
        });
        
        // Footer - Light gray background (matching image)
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(240, 240, 240)); // Light gray matching image
        statusPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        JPanel leftStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftStatusPanel.setOpaque(false);
        loginLabel = new JLabel(currentUser.getFullName() + " (" + currentUser.getEmail() + ")");
        loginLabel.setForeground(new Color(44, 62, 80));
        loginLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        leftStatusPanel.add(loginLabel);
        statusPanel.add(leftStatusPanel, BorderLayout.WEST);
        
        // Right side - Status and Logout button
        JPanel rightStatusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightStatusPanel.setOpaque(false);
        JLabel statusLabel = new JLabel("✓ Sẵn sàng");
        statusLabel.setForeground(DARK_ROYAL_BLUE); // Dark royal blue
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rightStatusPanel.add(statusLabel);
        
        // Add logout button in footer
        JButton footerLogoutBtn = createStyledButton("Đăng xuất", PRIMARY_RED, null);
        footerLogoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        footerLogoutBtn.setPreferredSize(new Dimension(90, 28));
        footerLogoutBtn.addActionListener(e -> logout());
        rightStatusPanel.add(footerLogoutBtn);
        
        statusPanel.add(rightStatusPanel, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    // Navigation buttons storage
    private JButton[] navButtons = new JButton[4];
    
    // Helper method to create navigation buttons
    private JButton createNavButton(String text, int tabIndex) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                // Draw background - darker gray if active
                if (mainTabbedPane != null && mainTabbedPane.getSelectedIndex() == tabIndex) {
                    g.setColor(new Color(220, 220, 220)); // Darker gray for active tab
                } else if (getModel().isRollover()) {
                    g.setColor(new Color(250, 250, 250));
                } else {
                    g.setColor(getBackground());
                }
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
                
                // Draw green underline if active (matching image)
                if (mainTabbedPane != null && mainTabbedPane.getSelectedIndex() == tabIndex) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(DARK_ROYAL_BLUE);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawLine(0, getHeight() - 3, getWidth(), getHeight() - 3);
                    g2.dispose();
                }
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(new Color(240, 240, 240));
        btn.setForeground(new Color(44, 62, 80));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        navButtons[tabIndex] = btn;
        
        btn.addActionListener(e -> {
            if (mainTabbedPane != null) {
                mainTabbedPane.setSelectedIndex(tabIndex);
                updateNavButtons(tabIndex);
            }
        });
        
        return btn;
    }
    
    // Helper method to update navigation button styles
    private void updateNavButtons(int activeIndex) {
        for (int i = 0; i < navButtons.length; i++) {
            if (navButtons[i] != null) {
                if (i == activeIndex) {
                    navButtons[i].setBackground(new Color(220, 220, 220)); // Darker for active
                    navButtons[i].setForeground(new Color(44, 62, 80));
                } else {
                    navButtons[i].setBackground(new Color(240, 240, 240));
                    navButtons[i].setForeground(new Color(44, 62, 80));
                }
                navButtons[i].repaint();
            }
        }
    }
    
    // Helper methods for styling
    private JTextField createRoundedTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorderUser(8, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(Color.WHITE);
        field.setOpaque(true);
        return field;
    }
    
    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorderUser(8, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        combo.setBackground(Color.WHITE);
        combo.setOpaque(true);
    }
    
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(28);
        
        // Set foreground and background for better visibility
        table.setForeground(new Color(28, 28, 30));
        table.setBackground(Color.WHITE);
        
        // Lighter selection colors for better readability
        table.setSelectionBackground(new Color(173, 216, 230)); // Light blue - easier to read
        table.setSelectionForeground(new Color(28, 28, 30)); // Dark text for better contrast
        
        // Grid colors
        table.setGridColor(new Color(230, 230, 230));
        table.setShowGrid(true);
        
        // Header styling - brighter and more visible colors
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(41, 128, 185)); // Darker blue - more visible
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(table.getTableHeader().getWidth(), 40));
        
        // Add custom header renderer for better visibility
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                javax.swing.table.DefaultTableCellRenderer renderer = (javax.swing.table.DefaultTableCellRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                renderer.setBackground(new Color(41, 128, 185)); // Darker blue
                renderer.setForeground(Color.WHITE);
                renderer.setFont(new Font("Segoe UI", Font.BOLD, 14));
                renderer.setHorizontalAlignment(JLabel.CENTER);
                return renderer;
            }
        });
        
        // Enable alternating row colors
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    // Selected row - light blue background with dark text
                    c.setBackground(new Color(173, 216, 230)); // Light blue
                    c.setForeground(new Color(28, 28, 30)); // Dark text for readability
                } else {
                    c.setForeground(new Color(44, 62, 80));
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(249, 249, 249));
                    }
                }
                return c;
            }
        });
    }
    
    private javax.swing.border.TitledBorder createStyledTitledBorder(String title) {
        javax.swing.border.TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createCompoundBorder(
                new RoundedBorderUser(12, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ),
            title
        );
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        border.setTitleColor(PRIMARY_BLUE);
        return border;
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground().darker());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(130, 38));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (hoverColor == null) {
            hoverColor = new Color(
                Math.min(255, bgColor.getRed() + 30),
                Math.min(255, bgColor.getGreen() + 30),
                Math.min(255, bgColor.getBlue() + 30)
            );
        }
        final Color finalHoverColor = hoverColor;
        final Color finalBgColor = bgColor;
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(finalHoverColor);
                button.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(finalBgColor);
                button.repaint();
            }
        });
        return button;
    }
    
    private JPanel createRoundedPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }
    
    private JPanel createHomePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(BG_LIGHT);
        
        // Welcome and stats header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        headerPanel.setOpaque(false);
        
        // Welcome text - DARK ROYAL BLUE and large (matching image)
        welcomeLabel = new JLabel("Chào mừng " + currentUser.getFullName() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(DARK_ROYAL_BLUE); // Dark royal blue matching image
        headerPanel.add(welcomeLabel, BorderLayout.NORTH);
        
        // Stats line: "Sách đang mượn: X | Tổng lượt mượn: Y | Phạt: Z VND"
        refreshUserStats();
        homeStatsLabel = new JLabel(String.format("Sách đang mượn: %d | Tổng lượt mượn: %d | Phạt: %.0f VND", 
            currentUser.getCurrentBorrowed(), currentUser.getTotalBorrowed(), currentUser.getTotalFines()));
        homeStatsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        homeStatsLabel.setForeground(new Color(127, 140, 141));
        headerPanel.add(homeStatsLabel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Two tables side by side
        JSplitPane tablesSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        tablesSplitPane.setDividerLocation(600);
        tablesSplitPane.setResizeWeight(0.5);
        
        // Left panel: New Books
        JPanel newBooksPanel = createRoundedPanel();
        newBooksPanel.setLayout(new BorderLayout());
        newBooksPanel.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Sách mới"),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        newBooksPanel.setBackground(Color.WHITE);
        newBooksPanel.add(new JScrollPane(newBooksTable), BorderLayout.CENTER);
        JButton viewAllBooksBtn = createStyledButton("Xem tất cả sách", PRIMARY_BLUE, null);
        viewAllBooksBtn.addActionListener(e -> {
            if (mainTabbedPane != null) {
                mainTabbedPane.setSelectedIndex(1); // Switch to "Tìm sách" tab
            }
        });
        JPanel newBooksButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        newBooksButtonPanel.setOpaque(false);
        newBooksButtonPanel.add(viewAllBooksBtn);
        newBooksPanel.add(newBooksButtonPanel, BorderLayout.SOUTH);
        
        // Right panel: Current Borrows - Use currentBorrowsTable like in "Mượn sách" tab
        JPanel borrowsPanel = new JPanel(new BorderLayout());
        borrowsPanel.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Sách đang mượn"),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        borrowsPanel.setBackground(Color.WHITE);
        borrowsPanel.setOpaque(true);
        
        // Create scroll pane for table - use separate table for home panel
        JScrollPane borrowsScrollPane = new JScrollPane(homeCurrentBorrowsTable);
        borrowsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        borrowsScrollPane.setPreferredSize(new Dimension(500, 300));
        borrowsPanel.add(borrowsScrollPane, BorderLayout.CENTER);
        JButton manageBorrowsBtn = createStyledButton("Quản lý mượn sách", PRIMARY_BLUE, null);
        manageBorrowsBtn.addActionListener(e -> {
            if (mainTabbedPane != null) {
                mainTabbedPane.setSelectedIndex(2); // Switch to "Mượn sách" tab
            }
        });
        JPanel borrowsButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        borrowsButtonPanel.setOpaque(false);
        borrowsButtonPanel.add(manageBorrowsBtn);
        borrowsPanel.add(borrowsButtonPanel, BorderLayout.SOUTH);
        
        tablesSplitPane.setLeftComponent(newBooksPanel);
        tablesSplitPane.setRightComponent(borrowsPanel);
        
        mainPanel.add(tablesSplitPane, BorderLayout.CENTER);
        
        // Quick Actions section
        JPanel quickActionsPanel = createRoundedPanel();
        quickActionsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        quickActionsPanel.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Thao tác nhanh"),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        quickActionsPanel.setBackground(BG_LIGHT);
        
        JButton searchBooksQuickBtn = createStyledButton("Tìm kiếm sách", PRIMARY_BLUE, null);
        searchBooksQuickBtn.addActionListener(e -> {
            if (mainTabbedPane != null) {
                mainTabbedPane.setSelectedIndex(1); // Switch to "Tìm sách" tab
            }
        });
        
        JButton borrowBooksQuickBtn = createStyledButton("Mượn sách", PRIMARY_BLUE, null);
        borrowBooksQuickBtn.addActionListener(e -> {
            if (mainTabbedPane != null) {
                mainTabbedPane.setSelectedIndex(2); // Switch to "Mượn sách" tab
            }
        });
        
        JButton profileQuickBtn = createStyledButton("Thông tin cá nhân", PRIMARY_BLUE, null);
        profileQuickBtn.addActionListener(e -> {
            if (mainTabbedPane != null) {
                mainTabbedPane.setSelectedIndex(3); // Switch to "Thông tin cá nhân" tab
            }
        });
        
        JButton historyQuickBtn = createStyledButton("Lịch sử mượn", PRIMARY_BLUE, null);
        historyQuickBtn.addActionListener(e -> {
            if (mainTabbedPane != null) {
                mainTabbedPane.setSelectedIndex(2); // Switch to "Mượn sách" tab
                // Switch to "Lịch sử mượn" subtab
                SwingUtilities.invokeLater(() -> {
                    if (borrowSubTabs != null) {
                        borrowSubTabs.setSelectedIndex(1); // Index 1 = "Lịch sử mượn"
                    }
                });
            }
        });
        
        quickActionsPanel.add(searchBooksQuickBtn);
        quickActionsPanel.add(borrowBooksQuickBtn);
        quickActionsPanel.add(profileQuickBtn);
        quickActionsPanel.add(historyQuickBtn);
        
        mainPanel.add(quickActionsPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private JLabel createStatCard(String title, String value, JPanel parent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLoweredBevelBorder());
        card.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        JLabel valueLabel = new JLabel(value, JLabel.CENTER);
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        valueLabel.setForeground(new Color(0, 102, 204));
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        parent.add(card);
        return valueLabel;
    }
    
    private JPanel createSearchPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(BG_LIGHT);
        
        // Search and filter panel - Reduced height
        JPanel searchPanel = createRoundedPanel();
        searchPanel.setLayout(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Tìm kiếm và lọc"),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Load categories for dropdown
        loadCategoriesForComboBox();
        
        JButton searchButton = createStyledButton("Tìm kiếm", PRIMARY_BLUE, null);
        searchButton.setPreferredSize(new Dimension(100, 35));
        searchButton.addActionListener(e -> {
            if (!isAccountLocked) {
                performSearch();
            } else {
                showStyledMessage("⚠ Tài khoản của bạn đã bị khóa. Bạn không thể thực hiện thao tác này.", "Tài khoản bị khóa", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        JButton refreshButton = createStyledButton("Làm mới", PRIMARY_BLUE, null);
        refreshButton.setPreferredSize(new Dimension(100, 35));
        refreshButton.addActionListener(e -> {
            if (!isAccountLocked) {
                searchField.setText("");
                categoryComboBox.setSelectedIndex(0);
                loadAllBooksForSearch();
            } else {
                showStyledMessage("⚠ Tài khoản của bạn đã bị khóa. Bạn không thể thực hiện thao tác này.", "Tài khoản bị khóa", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(new Color(44, 62, 80));
        
        JLabel categoryLabel = new JLabel("Thể loại:");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        categoryLabel.setForeground(new Color(44, 62, 80));
        
        gbc.gridx = 0; gbc.gridy = 0;
        searchPanel.add(searchResultLabel, gbc);
        gbc.gridx = 1;
        searchPanel.add(searchLabel, gbc);
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        searchPanel.add(searchField, gbc);
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        searchPanel.add(categoryLabel, gbc);
        gbc.gridx = 4;
        searchPanel.add(categoryComboBox, gbc);
        gbc.gridx = 5;
        searchPanel.add(searchButton, gbc);
        gbc.gridx = 6;
        searchPanel.add(refreshButton, gbc);
        
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Book list table - Expanded height
        JPanel resultPanel = createRoundedPanel();
        resultPanel.setLayout(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Danh sách sách"),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        resultPanel.setBackground(Color.WHITE);
        resultPanel.add(new JScrollPane(searchBooksTable), BorderLayout.CENTER);
        
        mainPanel.add(resultPanel, BorderLayout.CENTER);
        
        // Action buttons moved outside table panel, at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JButton borrowButton = createStyledButton("Mượn sách", PRIMARY_GRAY, null);
        borrowButton.addActionListener(e -> borrowBookFromSearch());
        
        JButton viewDetailButton = createStyledButton("Xem chi tiết", PRIMARY_GRAY, null);
        viewDetailButton.addActionListener(e -> viewBookDetail());
        
        buttonPanel.add(borrowButton);
        buttonPanel.add(viewDetailButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private void loadCategoriesForComboBox() {
        categoryComboBox.removeAllItems();
        categoryComboBox.addItem("Tất cả thể loại");
        categoryComboBox.addItem("Công nghệ thông tin");
        categoryComboBox.addItem("Kinh tế");
        categoryComboBox.addItem("Y học");
        categoryComboBox.addItem("Kỹ thuật");
        categoryComboBox.addItem("Văn học");
        categoryComboBox.addItem("Lịch sử");
        categoryComboBox.addItem("Tâm lý học");
    }
    
    private void performSearch() {
        String keyword = searchField.getText().trim();
        String category = (String) categoryComboBox.getSelectedItem();
        
        if (keyword.isEmpty() && "Tất cả thể loại".equals(category)) {
            loadAllBooksForSearch();
            return;
        }
        
        if (!keyword.isEmpty()) {
            searchBooks(keyword);
        } else {
            loadAllBooksForSearch();
        }
        
        // Filter by category if needed
        if (!"Tất cả thể loại".equals(category)) {
            filterBooksByCategory(category);
        }
    }
    
    private void filterBooksByCategory(String category) {
        DefaultTableModel model = (DefaultTableModel) searchBooksTable.getModel();
        int rowCount = model.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            String bookCategory = (String) model.getValueAt(i, 2);
            if (!category.equals(bookCategory)) {
                model.removeRow(i);
            }
        }
        updateSearchResultLabel();
    }
    
    private void borrowBookFromSearch() {
        if (isAccountLocked) {
            showStyledMessage("⚠ Tài khoản của bạn đã bị khóa. Bạn không thể thực hiện thao tác này.", "Tài khoản bị khóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int selectedRow = searchBooksTable.getSelectedRow();
        if (selectedRow == -1) {
            showStyledMessage("⚠ Vui lòng chọn sách cần mượn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get book title to find book ID
        String bookTitle = (String) searchBooksModel.getValueAt(selectedRow, 0);
        int available = ((Number) searchBooksModel.getValueAt(selectedRow, 6)).intValue();
        
        if (available <= 0) {
            JOptionPane.showMessageDialog(this, "Sách này đã hết, không thể mượn!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Find book by title
        Message request = new Message(Message.GET_ALL_BOOKS, null);
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<Book> books = (List<Book>) response.getData();
            String bookId = null;
            for (Book book : books) {
                if (book.getTitle().equals(bookTitle)) {
                    bookId = book.getBookId() != null ? book.getBookId() : String.valueOf(book.getId());
                    break;
                }
            }
            
            if (bookId != null) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Bạn có chắc chắn muốn mượn sách này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    Message borrowRequest = new Message(Message.BORROW_BOOK, new Object[]{currentUser.getUserId(), bookId});
                    Message borrowResponse = client.sendRequest(borrowRequest);
                    
                    if (borrowResponse.isSuccess()) {
                        JOptionPane.showMessageDialog(this, borrowResponse.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        // Refresh all data including current borrows
                        loadAllBooksForSearch();
                        loadBorrowBooks();
                        loadCurrentBorrows(); // This updates the "Mượn sách" tab
                        loadHomeCurrentBorrows(); // This updates the "Trang chủ" tab
                        loadMyBorrows();
                        loadNewBooks();
                        refreshUserStats();
                        updateBorrowStats(); // Update borrow statistics
                    } else {
                        JOptionPane.showMessageDialog(this, borrowResponse.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                showStyledMessage("✗ Không tìm thấy thông tin sách!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void viewBookDetail() {
        if (isAccountLocked) {
            showStyledMessage("⚠ Tài khoản của bạn đã bị khóa. Bạn không thể thực hiện thao tác này.", "Tài khoản bị khóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int selectedRow = searchBooksTable.getSelectedRow();
        if (selectedRow == -1) {
            showStyledMessage("Vui lòng chọn sách cần xem chi tiết!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String bookTitle = (String) searchBooksModel.getValueAt(selectedRow, 0);
        String author = (String) searchBooksModel.getValueAt(selectedRow, 1);
        String category = (String) searchBooksModel.getValueAt(selectedRow, 2);
        Integer publishYear = (Integer) searchBooksModel.getValueAt(selectedRow, 3);
        Integer pages = (Integer) searchBooksModel.getValueAt(selectedRow, 4);
        String price = (String) searchBooksModel.getValueAt(selectedRow, 5);
        Integer available = ((Number) searchBooksModel.getValueAt(selectedRow, 6)).intValue();
        Integer total = ((Number) searchBooksModel.getValueAt(selectedRow, 7)).intValue();
        
        // Get full book description
        Message request = new Message(Message.GET_ALL_BOOKS, null);
        Message response = client.sendRequest(request);
        String description = "";
        String isbn = "";
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<Book> books = (List<Book>) response.getData();
            for (Book book : books) {
                if (book.getTitle().equals(bookTitle)) {
                    description = book.getDescription() != null ? book.getDescription() : "";
                    isbn = book.getIsbn() != null ? book.getIsbn() : "";
                    break;
                }
            }
        }
        
        // Create custom dialog for book details
        JDialog dialog = new JDialog(this, "📖 Chi tiết sách", true);
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG_LIGHT);
        
        // Main panel with rounded border
        JPanel mainPanel = createRoundedPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorderUser(15, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setOpaque(true);
        
        // Title section
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        JLabel titleLabel = new JLabel("📚 " + bookTitle);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(44, 62, 80));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Content panel with scroll
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Add fields with styled labels
        addDetailField(contentPanel, gbc, "Tác giả:", author, 0);
        addDetailField(contentPanel, gbc, "Thể loại:", category, 1);
        if (!isbn.isEmpty()) {
            addDetailField(contentPanel, gbc, "ISBN:", isbn, 2);
        }
        addDetailField(contentPanel, gbc, "Năm xuất bản:", String.valueOf(publishYear), 3);
        addDetailField(contentPanel, gbc, "Số trang:", String.valueOf(pages), 4);
        addDetailField(contentPanel, gbc, "Giá:", price + " VND", 5);
        addDetailField(contentPanel, gbc, "Có sẵn:", String.valueOf(available) + " / " + String.valueOf(total), 6);
        
        // Description section
        if (!description.isEmpty()) {
            JLabel descLabel = new JLabel("<html><b>Mô tả:</b></html>");
            descLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            descLabel.setForeground(new Color(44, 62, 80));
            gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.NORTHWEST;
            contentPanel.add(descLabel, gbc);
            
            JTextArea descArea = new JTextArea(description);
            descArea.setLineWrap(true);
            descArea.setWrapStyleWord(true);
            descArea.setEditable(false);
            descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            descArea.setBackground(new Color(248, 249, 250));
            descArea.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorderUser(8, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
            ));
            descArea.setOpaque(true);
            JScrollPane descScroll = new JScrollPane(descArea);
            descScroll.setBorder(BorderFactory.createEmptyBorder());
            descScroll.setPreferredSize(new Dimension(500, 120));
            gbc.gridy = 8; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
            contentPanel.add(descScroll, gbc);
        }
        
        // Wrap content in scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        JButton closeButton = createStyledButton("Đóng", PRIMARY_BLUE, null);
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void addDetailField(JPanel panel, GridBagConstraints gbc, String label, String value, int row) {
        JLabel labelComp = new JLabel("<html><b>" + label + "</b></html>");
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        labelComp.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(labelComp, gbc);
        
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueComp.setForeground(new Color(127, 140, 141));
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(valueComp, gbc);
    }
    
    // Helper method for styled messages
    private void showStyledMessage(String message, String title, int messageType) {
        String htmlMessage = "<html><body style='font-family: Segoe UI; font-size: 13px; padding: 10px;'>" + 
            message.replace("\n", "<br>") + "</body></html>";
        JLabel messageLabel = new JLabel(htmlMessage);
        messageLabel.setVerticalAlignment(JLabel.TOP);
        JOptionPane.showMessageDialog(this, messageLabel, title, messageType, null);
    }
    
    // Helper method for styled confirm dialogs
    private int showStyledConfirmDialog(String message, String title) {
        String htmlMessage = "<html><body style='font-family: Segoe UI; font-size: 13px; padding: 10px;'>" + 
            message.replace("\n", "<br>") + "</body></html>";
        JLabel messageLabel = new JLabel(htmlMessage);
        messageLabel.setVerticalAlignment(JLabel.TOP);
        return JOptionPane.showConfirmDialog(this, messageLabel, title, 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }
    
    private void updateSearchResultLabel() {
        int count = searchBooksModel.getRowCount();
        searchResultLabel.setText("Tìm thấy " + count + " quyển sách");
    }
    
    private String formatPrice(double price) {
        return String.format("%.0f", price);
    }
    
    private JPanel createBorrowPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(BG_LIGHT);
        
        // Header section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Quản lý mượn sách");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Stats line - matching image format with update time
        refreshUserStats();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        borrowStatsLabel = new JLabel(String.format("Đang mượn: %d sách | Quá hạn: %d sách | Tối đa: 5 sách | Cập nhật: %s", 
            currentUser.getCurrentBorrowed(), getOverdueCount(), sdf.format(new java.util.Date())));
        borrowStatsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        borrowStatsLabel.setForeground(new Color(127, 140, 141));
        headerPanel.add(borrowStatsLabel, BorderLayout.CENTER);
        
        // Initialize lastUpdatedLabel for updateBorrowStats method
        lastUpdatedLabel = new JLabel();
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Sub-tabs
        borrowSubTabs = new JTabbedPane();
        borrowSubTabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        borrowSubTabs.setBackground(BG_LIGHT);
        
        // Tab 1: Sách đang mượn
        JPanel currentBorrowsPanel = createRoundedPanel();
        currentBorrowsPanel.setLayout(new BorderLayout());
        currentBorrowsPanel.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Sách đang mượn"),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        currentBorrowsPanel.setBackground(Color.WHITE);
        currentBorrowsPanel.add(new JScrollPane(currentBorrowsTable), BorderLayout.CENTER);
        
        // Action buttons
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionButtonPanel.setOpaque(false);
        JButton returnButton = createStyledButton("Trả sách", PRIMARY_BLUE, null);
        returnButton.addActionListener(e -> returnBook());
        
        JButton renewButton = createStyledButton("Gia hạn", PRIMARY_GRAY, null);
        renewButton.addActionListener(e -> renewBook());
        
        JButton lostButton = createStyledButton("Mất sách", new Color(220, 53, 69), null);
        lostButton.addActionListener(e -> reportLostBook());
        
        JButton damagedButton = createStyledButton("Hỏng", new Color(255, 193, 7), null);
        damagedButton.addActionListener(e -> reportDamagedBook());
        
        JButton refreshButton = createStyledButton("Làm mới", PRIMARY_BLUE, null);
        refreshButton.addActionListener(e -> {
            if (!isAccountLocked) {
                loadCurrentBorrows();
                refreshUserStats();
                updateBorrowStats();
            } else {
                showStyledMessage("⚠ Tài khoản của bạn đã bị khóa. Bạn không thể thực hiện thao tác này.", "Tài khoản bị khóa", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        actionButtonPanel.add(returnButton);
        actionButtonPanel.add(renewButton);
        actionButtonPanel.add(lostButton);
        actionButtonPanel.add(damagedButton);
        actionButtonPanel.add(refreshButton);
        
        currentBorrowsPanel.add(actionButtonPanel, BorderLayout.SOUTH);
        borrowSubTabs.addTab("Sách đang mượn", currentBorrowsPanel);
        
        // Tab 2: Lịch sử mượn
        JPanel historyPanel = createRoundedPanel();
        historyPanel.setLayout(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Lịch sử mượn"),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        historyPanel.setBackground(Color.WHITE);
        historyPanel.add(new JScrollPane(myBorrowsTable), BorderLayout.CENTER);
        borrowSubTabs.addTab("Lịch sử mượn", historyPanel);
        
        mainPanel.add(borrowSubTabs, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private int getOverdueCount() {
        int overdueCount = 0;
        java.util.Date now = new java.util.Date();
        Message request = new Message(Message.GET_USER_BORROW_RECORDS, currentUser.getUserId());
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<BorrowRecord> records = (List<BorrowRecord>) response.getData();
            for (BorrowRecord record : records) {
                String status = record.getStatus();
                if (status != null && (status.equals("BORROWING") || status.equals("BORROWED"))) {
                    java.util.Date dueDate = record.getDueDate();
                    if (dueDate != null && dueDate.before(now)) {
                        overdueCount++;
                    }
                }
            }
        }
        return overdueCount;
    }
    
    private void loadCurrentBorrows() {
        try {
            System.out.println("[DEBUG] loadCurrentBorrows: Loading for user: " + currentUser.getUserId());
            Message request = new Message(Message.GET_USER_BORROW_RECORDS, currentUser.getUserId());
            Message response = client.sendRequest(request);
            
            // Check for force logout
            if (checkForceLogout(response)) {
                return;
            }
            
            System.out.println("[DEBUG] loadCurrentBorrows: Response success: " + (response != null ? response.isSuccess() : "null"));
            
            currentBorrowsModel.setRowCount(0); // Clear first
            
            if (response != null && response.isSuccess() && response.getData() instanceof List) {
                @SuppressWarnings("unchecked")
                List<BorrowRecord> records = (List<BorrowRecord>) response.getData();
                
                System.out.println("[DEBUG] loadCurrentBorrows: Records count: " + (records != null ? records.size() : "null"));
                
                if (records != null && !records.isEmpty()) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                    java.util.Date now = new java.util.Date();
                    int addedCount = 0;
                    
                    // Filter only active borrows
                    for (BorrowRecord record : records) {
                        if (record == null) continue;
                        
                        String status = record.getStatus();
                        System.out.println("[DEBUG] loadCurrentBorrows: Record status: " + status + ", BookTitle: " + record.getBookTitle());
                        
                        if (status != null && (status.equals("BORROWING") || status.equals("BORROWED"))) {
                            String bookId = record.getBookId() != null ? record.getBookId() : "";
                            String borrowDate = record.getBorrowDate() != null ? sdf.format(record.getBorrowDate()) : "";
                            String dueDate = record.getDueDate() != null ? sdf.format(record.getDueDate()) : "";
                            
                            // Calculate remaining days
                            String remainingDays = "";
                            if (record.getDueDate() != null) {
                                long diff = record.getDueDate().getTime() - now.getTime();
                                long days = diff / (1000 * 60 * 60 * 24);
                                if (days < 0) {
                                    remainingDays = "Quá hạn";
                                } else {
                                    remainingDays = days + " ngày";
                                }
                            }
                            
                            String statusText = "Đang mượn";
                            String bookTitle = record.getBookTitle();
                            if (bookTitle == null || bookTitle.isEmpty()) {
                                bookTitle = "N/A";
                            }
                            
                            currentBorrowsModel.addRow(new Object[]{
                                bookId,
                                bookTitle,
                                borrowDate,
                                dueDate,
                                remainingDays,
                                statusText
                            });
                            addedCount++;
                            System.out.println("[DEBUG] loadCurrentBorrows: Added row - BookId: " + bookId + ", Title: " + bookTitle);
                        }
                    }
                    System.out.println("[DEBUG] loadCurrentBorrows: Added " + addedCount + " rows to table. Model row count: " + currentBorrowsModel.getRowCount());
                    
                    // Force table update on EDT
                    SwingUtilities.invokeLater(() -> {
                        currentBorrowsTable.revalidate();
                        currentBorrowsTable.repaint();
                        System.out.println("[DEBUG] loadCurrentBorrows: Table revalidated and repainted");
                    });
                } else {
                    System.out.println("[DEBUG] loadCurrentBorrows: No records found or empty list");
                }
            } else {
                System.err.println("[DEBUG] loadCurrentBorrows: Response failed or no data. Success: " + 
                    (response != null ? response.isSuccess() : "null") + 
                    ", Message: " + (response != null ? response.getMessage() : "null"));
            }
        } catch (Exception e) {
            System.err.println("[DEBUG] Error loading current borrows: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateBorrowStats() {
        if (borrowStatsLabel != null) {
            refreshUserStats();
            // refreshUserStats() already updates borrowStatsLabel, so we don't need to do it again
        }
    }
    
    private void returnBook() {
        if (isAccountLocked) {
            showStyledMessage("⚠ Tài khoản của bạn đã bị khóa. Bạn không thể thực hiện thao tác này.", "Tài khoản bị khóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int selectedRow = currentBorrowsTable.getSelectedRow();
        if (selectedRow == -1) {
            showStyledMessage("⚠ Vui lòng chọn sách cần trả!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String bookTitle = (String) currentBorrowsModel.getValueAt(selectedRow, 1);
        
        // Find record ID
        Message request = new Message(Message.GET_USER_BORROW_RECORDS, currentUser.getUserId());
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<BorrowRecord> records = (List<BorrowRecord>) response.getData();
            String recordId = null;
            for (BorrowRecord record : records) {
                if (record.getBookTitle().equals(bookTitle) && 
                    (record.getStatus().equals("BORROWING") || record.getStatus().equals("BORROWED"))) {
                    recordId = record.getRecordId() != null ? record.getRecordId() : String.valueOf(record.getId());
                    break;
                }
            }
            
            if (recordId != null) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Bạn có chắc chắn muốn trả sách này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    Message returnRequest = new Message(Message.RETURN_BOOK, recordId);
                    Message returnResponse = client.sendRequest(returnRequest);
                    
                    if (returnResponse.isSuccess()) {
                        JOptionPane.showMessageDialog(this, returnResponse.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                // Reload all data to sync with admin
                loadCurrentBorrows();
                loadHomeCurrentBorrows(); // Update home panel
                loadMyBorrows();
                loadBorrowBooks();
                loadAllBooksForSearch();
                loadNewBooks();
                refreshUserStats();
                updateBorrowStats();
                    } else {
                        JOptionPane.showMessageDialog(this, returnResponse.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy bản ghi mượn sách!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void renewBook() {
        if (isAccountLocked) {
            showStyledMessage("⚠ Tài khoản của bạn đã bị khóa. Bạn không thể thực hiện thao tác này.", "Tài khoản bị khóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int selectedRow = currentBorrowsTable.getSelectedRow();
        if (selectedRow == -1) {
            showStyledMessage("⚠ Vui lòng chọn sách cần gia hạn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get record ID from the selected row
        Object recordIdObj = currentBorrowsModel.getValueAt(selectedRow, 0);
        if (recordIdObj == null) {
            JOptionPane.showMessageDialog(this, "Không thể lấy thông tin phiếu mượn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get status
        String status = (String) currentBorrowsModel.getValueAt(selectedRow, 5);
        if (!"BORROWING".equals(status) && !"Đang mượn".equals(status)) {
            JOptionPane.showMessageDialog(this, "Chỉ có thể gia hạn sách đang mượn!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get renewal days from settings
        int renewalDays = 7; // Default value
        try {
            Message settingsRequest = new Message(Message.GET_SETTINGS, null);
            Message settingsResponse = client.sendRequest(settingsRequest);
            if (settingsResponse != null && settingsResponse.isSuccess() && settingsResponse.getData() != null) {
                org.bson.Document settings = (org.bson.Document) settingsResponse.getData();
                Object renewalDaysObj = settings.get("renewalDays");
                if (renewalDaysObj != null) {
                    renewalDays = ((Number) renewalDaysObj).intValue();
                }
            }
        } catch (Exception e) {
            // Use default value if can't get settings
            System.err.println("Error getting settings for renewal: " + e.getMessage());
        }
        
        // Confirm renewal with correct days from settings
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn gia hạn sách này thêm " + renewalDays + " ngày?",
            "Xác nhận gia hạn",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Find record ID - need to get it from borrow records
        String recordId = null;
        try {
            Message request = new Message(Message.GET_USER_BORROW_RECORDS, currentUser.getUserId());
            Message response = client.sendRequest(request);
            if (response.isSuccess() && response.getData() instanceof List) {
                @SuppressWarnings("unchecked")
                List<BorrowRecord> records = (List<BorrowRecord>) response.getData();
                if (records != null && !records.isEmpty()) {
                    // Find the record by matching book title
                    String bookTitle = (String) currentBorrowsModel.getValueAt(selectedRow, 1);
                    for (BorrowRecord record : records) {
                        if (record.getBookTitle() != null && record.getBookTitle().equals(bookTitle) 
                            && ("BORROWING".equals(record.getStatus()) || "Đang mượn".equals(record.getStatus()))) {
                            recordId = record.getRecordId();
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (recordId == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy phiếu mượn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Send renew request
        try {
            Message request = new Message(Message.RENEW_BOOK, recordId);
            Message response = client.sendRequest(request);
            if (response != null && response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                // Reload all data to sync with admin
                loadCurrentBorrows();
                loadHomeCurrentBorrows(); // Update home panel
                loadMyBorrows();
                refreshUserStats();
                updateBorrowStats();
            } else {
                JOptionPane.showMessageDialog(this, 
                    response != null ? response.getMessage() : "Gia hạn sách thất bại!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi gia hạn sách: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void reportLostBook() {
        if (isAccountLocked) {
            showStyledMessage("⚠ Tài khoản của bạn đã bị khóa. Bạn không thể thực hiện thao tác này.", "Tài khoản bị khóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int selectedRow = currentBorrowsTable.getSelectedRow();
        if (selectedRow == -1) {
            showStyledMessage("⚠ Vui lòng chọn sách cần báo mất!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String bookTitle = (String) currentBorrowsModel.getValueAt(selectedRow, 1);
        
        // Find record ID
        Message request = new Message(Message.GET_USER_BORROW_RECORDS, currentUser.getUserId());
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<BorrowRecord> records = (List<BorrowRecord>) response.getData();
            String recordId = null;
            for (BorrowRecord record : records) {
                if (record.getBookTitle().equals(bookTitle) && 
                    (record.getStatus().equals("BORROWING") || record.getStatus().equals("BORROWED"))) {
                    recordId = record.getRecordId() != null ? record.getRecordId() : null;
                    break;
                }
            }
            
            if (recordId == null) {
                showStyledMessage("⚠ Không tìm thấy phiếu mượn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn báo cáo sách '" + bookTitle + "' là mất?\n\nLưu ý: Bạn sẽ phải chịu phí mất sách theo quy định của thư viện.",
                "Xác nhận báo mất sách",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                Message lostRequest = new Message(Message.MARK_LOST, recordId);
                Message lostResponse = client.sendRequest(lostRequest);
                
                if (lostResponse != null && lostResponse.isSuccess()) {
                    showStyledMessage(lostResponse.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadCurrentBorrows();
                    loadHomeCurrentBorrows();
                    loadMyBorrows();
                    refreshUserStats();
                    updateBorrowStats();
                } else {
                    showStyledMessage(
                        lostResponse != null ? lostResponse.getMessage() : "Báo mất sách thất bại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }
    
    private void reportDamagedBook() {
        if (isAccountLocked) {
            showStyledMessage("⚠ Tài khoản của bạn đã bị khóa. Bạn không thể thực hiện thao tác này.", "Tài khoản bị khóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int selectedRow = currentBorrowsTable.getSelectedRow();
        if (selectedRow == -1) {
            showStyledMessage("⚠ Vui lòng chọn sách cần báo hỏng!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String bookTitle = (String) currentBorrowsModel.getValueAt(selectedRow, 1);
        
        // Find record ID
        Message request = new Message(Message.GET_USER_BORROW_RECORDS, currentUser.getUserId());
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<BorrowRecord> records = (List<BorrowRecord>) response.getData();
            String recordId = null;
            for (BorrowRecord record : records) {
                if (record.getBookTitle().equals(bookTitle) && 
                    (record.getStatus().equals("BORROWING") || record.getStatus().equals("BORROWED"))) {
                    recordId = record.getRecordId() != null ? record.getRecordId() : null;
                    break;
                }
            }
            
            if (recordId == null) {
                showStyledMessage("⚠ Không tìm thấy phiếu mượn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn báo cáo sách '" + bookTitle + "' là hỏng?\n\nLưu ý: Bạn sẽ phải chịu phí hư hỏng sách theo quy định của thư viện.",
                "Xác nhận báo hỏng sách",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                Message damagedRequest = new Message(Message.MARK_DAMAGED, recordId);
                Message damagedResponse = client.sendRequest(damagedRequest);
                
                if (damagedResponse != null && damagedResponse.isSuccess()) {
                    showStyledMessage(damagedResponse.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadCurrentBorrows();
                    loadHomeCurrentBorrows();
                    loadMyBorrows();
                    refreshUserStats();
                    updateBorrowStats();
                } else {
                    showStyledMessage(
                        damagedResponse != null ? damagedResponse.getMessage() : "Báo hỏng sách thất bại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }
    
    private JPanel createProfilePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(BG_LIGHT);
        
        // Title
        JLabel titleLabel = new JLabel("Thông tin cá nhân");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Form panel - wrapped in rounded panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorderUser(15, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        formPanel.setBackground(Color.WHITE);
        formPanel.setOpaque(true);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Initialize fields with rounded styling
        firstNameField = createRoundedTextField(25);
        lastNameField = createRoundedTextField(25);
        emailField = createRoundedTextField(25);
        phoneField = createRoundedTextField(25);
        studentIdField = createRoundedTextField(25);
        facultyField = createRoundedTextField(25);
        yearOfStudyComboBox = new JComboBox<>(new String[]{"2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030"});
        styleComboBox(yearOfStudyComboBox);
        addressArea = new JTextArea(3, 50);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        addressArea.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorderUser(8, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        addressArea.setBackground(Color.WHITE);
        addressArea.setOpaque(true);
        
        // Populate fields
        refreshProfileFields();
        
        emailField.setEditable(false);
        emailField.setBackground(new Color(240, 240, 240));
        studentIdField.setEditable(false);
        studentIdField.setBackground(new Color(240, 240, 240));
        
        // Form layout with 2 columns of fields to reduce height
        // Row 0: Tên and Họ side by side
        int y = 0;
        JLabel label1 = new JLabel("Tên *:");
        label1.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label1.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; gbc.weightx = 0;
        formPanel.add(label1, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(firstNameField, gbc);
        
        JLabel label2 = new JLabel("Họ *:");
        label2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label2.setForeground(new Color(44, 62, 80));
        gbc.gridx = 2; gbc.weightx = 0;
        formPanel.add(label2, gbc);
        gbc.gridx = 3; gbc.weightx = 1.0;
        formPanel.add(lastNameField, gbc);
        y++;
        
        // Row 1: Email and Số điện thoại
        JLabel label3 = new JLabel("Email:");
        label3.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label3.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        formPanel.add(label3, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(emailField, gbc);
        
        JLabel label4 = new JLabel("Số điện thoại *:");
        label4.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label4.setForeground(new Color(44, 62, 80));
        gbc.gridx = 2; gbc.weightx = 0;
        formPanel.add(label4, gbc);
        gbc.gridx = 3; gbc.weightx = 1.0;
        formPanel.add(phoneField, gbc);
        y++;
        
        // Row 2: Mã sinh viên and Khoa
        JLabel label5 = new JLabel("Mã sinh viên:");
        label5.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label5.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        formPanel.add(label5, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(studentIdField, gbc);
        
        JLabel label6 = new JLabel("Khoa:");
        label6.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label6.setForeground(new Color(44, 62, 80));
        gbc.gridx = 2; gbc.weightx = 0;
        formPanel.add(label6, gbc);
        gbc.gridx = 3; gbc.weightx = 1.0;
        formPanel.add(facultyField, gbc);
        y++;
        
        // Row 3: Năm học
        JLabel label7 = new JLabel("Năm học:");
        label7.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label7.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        formPanel.add(label7, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(yearOfStudyComboBox, gbc);
        y++;
        
        // Row 4: Địa chỉ (full width)
        JLabel label8 = new JLabel("Địa chỉ:");
        label8.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label8.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(label8, gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.3;
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setBorder(BorderFactory.createEmptyBorder());
        formPanel.add(addressScroll, gbc);
        gbc.gridwidth = 1; gbc.weighty = 0; gbc.anchor = GridBagConstraints.WEST;
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        JButton saveButton = createStyledButton("Lưu thay đổi", PRIMARY_BLUE, null);
        saveButton.addActionListener(e -> saveProfile());
        
        JButton cancelButton = createStyledButton("Hủy", PRIMARY_GRAY, null);
        cancelButton.addActionListener(e -> refreshProfileFields());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        // Wrap form panel in a container for better centering
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setOpaque(false);
        formContainer.setBorder(BorderFactory.createEmptyBorder(0, 100, 0, 100));
        formContainer.add(formPanel, BorderLayout.CENTER);
        
        // Center panel with form
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(formContainer, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Stats bar at bottom - Modern design
        JPanel statsBar = new JPanel(new BorderLayout());
        statsBar.setBackground(new Color(52, 73, 94)); // Dark blue-gray
        statsBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JPanel leftStatsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftStatsPanel.setOpaque(false);
        JLabel statsTitle = new JLabel("Thống kê cá nhân:");
        statsTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statsTitle.setForeground(new Color(236, 240, 241));
        leftStatsPanel.add(statsTitle);
        totalBorrowedLabel = new JLabel(String.valueOf(currentUser.getTotalBorrowed()));
        totalBorrowedLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalBorrowedLabel.setForeground(new Color(255, 255, 255));
        leftStatsPanel.add(totalBorrowedLabel);
        leftStatsPanel.add(new JLabel(" | "));
        currentBorrowedLabel = new JLabel(String.valueOf(currentUser.getCurrentBorrowed()));
        currentBorrowedLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        currentBorrowedLabel.setForeground(new Color(255, 255, 255));
        leftStatsPanel.add(currentBorrowedLabel);
        leftStatsPanel.add(new JLabel(" | "));
        totalFinesLabel = new JLabel(String.format("%.0f VND", currentUser.getTotalFines()));
        totalFinesLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalFinesLabel.setForeground(new Color(255, 255, 255));
        leftStatsPanel.add(totalFinesLabel);
        
        JPanel rightStatsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightStatsPanel.setOpaque(false);
        String userStatus = currentUser.getStatus() != null ? currentUser.getStatus() : "ACTIVE";
        statusLabel = new JLabel("ACTIVE".equals(userStatus) ? "✓ Hoạt động" : "✗ Bị khóa");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground("ACTIVE".equals(userStatus) ? new Color(52, 199, 89) : new Color(255, 59, 48));
        rightStatsPanel.add(statusLabel);
        
        statsBar.add(leftStatsPanel, BorderLayout.WEST);
        statsBar.add(rightStatsPanel, BorderLayout.EAST);
        
        mainPanel.add(statsBar, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private void refreshProfileFields() {
        if (emailField != null) {
            emailField.setText(currentUser.getEmail());
        }
        if (firstNameField != null) {
            firstNameField.setText(currentUser.getFirstName());
        }
        if (lastNameField != null) {
            lastNameField.setText(currentUser.getLastName());
        }
        if (phoneField != null) {
            phoneField.setText(currentUser.getPhone());
        }
        if (addressArea != null) {
            addressArea.setText(currentUser.getAddress());
        }
        if (studentIdField != null) {
            studentIdField.setText(currentUser.getStudentId());
        }
        if (facultyField != null) {
            facultyField.setText(currentUser.getFaculty());
        }
        if (yearOfStudyComboBox != null) {
            String yearOfStudy = currentUser.getYearOfStudy();
            if (yearOfStudy != null && !yearOfStudy.isEmpty()) {
                yearOfStudyComboBox.setSelectedItem(yearOfStudy);
            } else {
                yearOfStudyComboBox.setSelectedItem("2025");
            }
        }
    }
    
    private GridBagConstraints gbc(int x, int y) {
        return gbc(x, y, 1, 1);
    }
    
    private GridBagConstraints gbc(int x, int y, int width, int height) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        if (x == 1) gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }
    
    // Helper method to check for force logout
    private boolean checkForceLogout(Message response) {
        if (response != null && Message.FORCE_LOGOUT.equals(response.getType())) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    response.getMessage() != null ? response.getMessage() : "Tài khoản của bạn đã bị khóa. Bạn sẽ bị đăng xuất.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                logout();
            });
            return true;
        }
        return false;
    }
    
    private void loadData() {
        loadMyBorrows();
        loadAllBooksForSearch();
        loadBorrowBooks();
        loadNewBooks();
        loadCurrentBorrows(); // For "Mượn sách" tab
        loadHomeCurrentBorrows(); // For "Trang chủ" tab
        refreshUserStats();
        updateBorrowStats();
        checkNotifications();
    }
    
    // Load current borrows for home panel (separate table)
    private void loadHomeCurrentBorrows() {
        try {
            System.out.println("[DEBUG] loadHomeCurrentBorrows: Loading for user: " + currentUser.getUserId());
            Message request = new Message(Message.GET_USER_BORROW_RECORDS, currentUser.getUserId());
            Message response = client.sendRequest(request);
            
            System.out.println("[DEBUG] loadHomeCurrentBorrows: Response success: " + (response != null ? response.isSuccess() : "null"));
            
            homeCurrentBorrowsModel.setRowCount(0); // Clear first
            
            if (response != null && response.isSuccess() && response.getData() instanceof List) {
                @SuppressWarnings("unchecked")
                List<BorrowRecord> records = (List<BorrowRecord>) response.getData();
                
                System.out.println("[DEBUG] loadHomeCurrentBorrows: Records count: " + (records != null ? records.size() : "null"));
                
                if (records != null && !records.isEmpty()) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                    java.util.Date now = new java.util.Date();
                    int addedCount = 0;
                    
                    // Filter only active borrows
                    for (BorrowRecord record : records) {
                        if (record == null) continue;
                        
                        String status = record.getStatus();
                        System.out.println("[DEBUG] loadHomeCurrentBorrows: Record status: " + status + ", BookTitle: " + record.getBookTitle());
                        
                        if (status != null && (status.equals("BORROWING") || status.equals("BORROWED"))) {
                            String bookId = record.getBookId() != null ? record.getBookId() : "";
                            String borrowDate = record.getBorrowDate() != null ? sdf.format(record.getBorrowDate()) : "";
                            String dueDate = record.getDueDate() != null ? sdf.format(record.getDueDate()) : "";
                            
                            // Calculate remaining days
                            String remainingDays = "";
                            if (record.getDueDate() != null) {
                                long diff = record.getDueDate().getTime() - now.getTime();
                                long days = diff / (1000 * 60 * 60 * 24);
                                if (days < 0) {
                                    remainingDays = "Quá hạn";
                                } else {
                                    remainingDays = days + " ngày";
                                }
                            }
                            
                            String statusText = "Đang mượn";
                            String bookTitle = record.getBookTitle();
                            if (bookTitle == null || bookTitle.isEmpty()) {
                                bookTitle = "N/A";
                            }
                            
                            homeCurrentBorrowsModel.addRow(new Object[]{
                                bookId,
                                bookTitle,
                                borrowDate,
                                dueDate,
                                remainingDays,
                                statusText
                            });
                            addedCount++;
                            System.out.println("[DEBUG] loadHomeCurrentBorrows: Added row - BookId: " + bookId + ", Title: " + bookTitle);
                        }
                    }
                    System.out.println("[DEBUG] loadHomeCurrentBorrows: Added " + addedCount + " rows to table. Model row count: " + homeCurrentBorrowsModel.getRowCount());
                    
                    // Force table update on EDT
                    SwingUtilities.invokeLater(() -> {
                        homeCurrentBorrowsTable.revalidate();
                        homeCurrentBorrowsTable.repaint();
                        System.out.println("[DEBUG] loadHomeCurrentBorrows: Table revalidated and repainted");
                    });
                } else {
                    System.out.println("[DEBUG] loadHomeCurrentBorrows: No records found or empty list");
                }
            } else {
                System.err.println("[DEBUG] loadHomeCurrentBorrows: Response failed or no data. Success: " + 
                    (response != null ? response.isSuccess() : "null") + 
                    ", Message: " + (response != null ? response.getMessage() : "null"));
            }
        } catch (Exception e) {
            System.err.println("[DEBUG] Error loading home current borrows: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void startNotificationTimer() {
        // Check notifications every 3 seconds while user is logged in
        notificationTimer = new javax.swing.Timer(3000, e -> {
            checkNotifications();
        });
        notificationTimer.setRepeats(true);
        notificationTimer.start();
        
        // Check account status every 5 seconds while user is logged in
        accountStatusTimer = new javax.swing.Timer(5000, e -> {
            checkAccountStatus();
        });
        accountStatusTimer.setRepeats(true);
        accountStatusTimer.start();
        
        // Sync data every 10 seconds to keep in sync with admin changes
        dataSyncTimer = new javax.swing.Timer(10000, e -> {
            if (!isAccountLocked) {
                syncData();
            }
        });
        dataSyncTimer.setRepeats(true);
        dataSyncTimer.start();
        
        // Stop timers when window is closed
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (notificationTimer != null) {
                    notificationTimer.stop();
                }
                if (accountStatusTimer != null) {
                    accountStatusTimer.stop();
                }
                if (dataSyncTimer != null) {
                    dataSyncTimer.stop();
                }
            }
        });
    }
    
    private void syncData() {
        // Sync data based on current tab to avoid unnecessary updates
        int currentTab = mainTabbedPane != null ? mainTabbedPane.getSelectedIndex() : 0;
        
        try {
            // Always refresh user stats
            refreshUserStats();
            
            // Refresh data based on current tab
            if (currentTab == 0) {
                // Trang chủ - refresh current borrows and new books
                loadHomeCurrentBorrows();
                loadNewBooks();
            } else if (currentTab == 1) {
                // Tìm sách - refresh search books
                loadAllBooksForSearch();
            } else if (currentTab == 2) {
                // Mượn sách - refresh current borrows
                loadCurrentBorrows();
                loadBorrowBooks();
                loadMyBorrows();
                updateBorrowStats();
            }
        } catch (Exception e) {
            // Silent fail - don't show error to user
            System.err.println("Error syncing data: " + e.getMessage());
        }
    }
    
    private void checkAccountStatus() {
        try {
            Message request = new Message(Message.CHECK_USER_STATUS, null);
            Message response = client.sendRequest(request);
            
            if (response != null && Message.FORCE_LOGOUT.equals(response.getType())) {
                // Account is locked
                if (!isAccountLocked) {
                    // First time detecting lock - disable all functions
                    isAccountLocked = true;
                    SwingUtilities.invokeLater(() -> {
                        disableAllFunctionsExceptLogout();
                        JOptionPane.showMessageDialog(this, 
                            response.getMessage() != null ? response.getMessage() : "Tài khoản của bạn đã bị khóa. Bạn không thể thao tác bất kỳ chức năng nào. Chỉ có thể đăng xuất.",
                            "Tài khoản bị khóa", JOptionPane.WARNING_MESSAGE);
                    });
                } else {
                    // Already locked - show reminder every 5 seconds
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, 
                            "Tài khoản của bạn đã bị khóa. Bạn không thể thao tác bất kỳ chức năng nào. Chỉ có thể đăng xuất.",
                            "Tài khoản bị khóa", JOptionPane.WARNING_MESSAGE);
                    });
                }
            } else if (isAccountLocked && response != null && response.isSuccess()) {
                // Account was unlocked - re-enable functions
                isAccountLocked = false;
                SwingUtilities.invokeLater(() -> {
                    enableAllFunctions();
                });
            }
        } catch (Exception e) {
            // Silent fail - don't show error to user
            System.err.println("Error checking account status: " + e.getMessage());
        }
    }
    
    private void disableAllFunctionsExceptLogout() {
        // Disable main tabbed pane
        if (mainTabbedPane != null) {
            mainTabbedPane.setEnabled(false);
        }
        
        // Disable borrow sub tabs
        if (borrowSubTabs != null) {
            borrowSubTabs.setEnabled(false);
        }
        
        // Disable all tables
        if (searchBooksTable != null) {
            searchBooksTable.setEnabled(false);
        }
        if (borrowBooksTable != null) {
            borrowBooksTable.setEnabled(false);
        }
        if (currentBorrowsTable != null) {
            currentBorrowsTable.setEnabled(false);
        }
        if (myBorrowsTable != null) {
            myBorrowsTable.setEnabled(false);
        }
        if (homeCurrentBorrowsTable != null) {
            homeCurrentBorrowsTable.setEnabled(false);
        }
        
        // Disable search field and category combo
        if (searchField != null) {
            searchField.setEnabled(false);
        }
        if (categoryComboBox != null) {
            categoryComboBox.setEnabled(false);
        }
        
        // Disable profile fields
        if (emailField != null) emailField.setEnabled(false);
        if (firstNameField != null) firstNameField.setEnabled(false);
        if (lastNameField != null) lastNameField.setEnabled(false);
        if (phoneField != null) phoneField.setEnabled(false);
        if (addressArea != null) addressArea.setEnabled(false);
        if (studentIdField != null) studentIdField.setEnabled(false);
        if (facultyField != null) facultyField.setEnabled(false);
        if (yearOfStudyComboBox != null) yearOfStudyComboBox.setEnabled(false);
        
        // Note: Logout buttons are NOT disabled - they remain enabled
    }
    
    private void enableAllFunctions() {
        // Re-enable main tabbed pane
        if (mainTabbedPane != null) {
            mainTabbedPane.setEnabled(true);
        }
        
        // Re-enable borrow sub tabs
        if (borrowSubTabs != null) {
            borrowSubTabs.setEnabled(true);
        }
        
        // Re-enable all tables
        if (searchBooksTable != null) {
            searchBooksTable.setEnabled(true);
        }
        if (borrowBooksTable != null) {
            borrowBooksTable.setEnabled(true);
        }
        if (currentBorrowsTable != null) {
            currentBorrowsTable.setEnabled(true);
        }
        if (myBorrowsTable != null) {
            myBorrowsTable.setEnabled(true);
        }
        if (homeCurrentBorrowsTable != null) {
            homeCurrentBorrowsTable.setEnabled(true);
        }
        
        // Re-enable search field and category combo
        if (searchField != null) {
            searchField.setEnabled(true);
        }
        if (categoryComboBox != null) {
            categoryComboBox.setEnabled(true);
        }
        
        // Re-enable profile fields (read-only fields remain disabled)
        if (emailField != null) emailField.setEnabled(true);
        if (firstNameField != null) firstNameField.setEnabled(true);
        if (lastNameField != null) lastNameField.setEnabled(true);
        if (phoneField != null) phoneField.setEnabled(true);
        if (addressArea != null) addressArea.setEnabled(true);
        if (studentIdField != null) studentIdField.setEnabled(true);
        if (facultyField != null) facultyField.setEnabled(true);
        if (yearOfStudyComboBox != null) yearOfStudyComboBox.setEnabled(true);
    }
    
    private void checkNotifications() {
        try {
            Message request = new Message(Message.GET_USER_NOTIFICATIONS, null);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess() && response.getData() instanceof List) {
                @SuppressWarnings("unchecked")
                List<Document> notifications = (List<Document>) response.getData();
                
                if (notifications != null && !notifications.isEmpty()) {
                    // Check if there are new notifications (not yet shown)
                    boolean hasNewNotifications = false;
                    for (Document notif : notifications) {
                        String notificationId = notif.getString("notificationId");
                        if (notificationId != null) {
                            // Check creation time - only show if created after last check
                            java.util.Date createdAt = notif.getDate("createdAt");
                            if (createdAt != null && createdAt.getTime() > lastNotificationCheckTime) {
                                hasNewNotifications = true;
                                break;
                            }
                        }
                    }
                    
                    if (hasNewNotifications) {
                        StringBuilder message = new StringBuilder();
                        message.append("<html><body style='font-family: Segoe UI; font-size: 13px;'>");
                        message.append("<h3 style='color: #FF3B30; margin-top: 0;'>📢 Thông báo quan trọng</h3>");
                        message.append("<ul style='margin: 10px 0; padding-left: 20px;'>");
                        
                        // Only show new notifications
                        for (Document notif : notifications) {
                            String msg = notif.getString("message");
                            java.util.Date createdAt = notif.getDate("createdAt");
                            if (msg != null && createdAt != null && createdAt.getTime() > lastNotificationCheckTime) {
                                message.append("<li style='margin: 8px 0;'>").append(msg).append("</li>");
                            }
                        }
                        
                        message.append("</ul>");
                        message.append("<p style='color: #666; font-size: 11px; margin-top: 10px;'>Vui lòng đến thư viện để xử lý.</p>");
                        message.append("</body></html>");
                        
                        JLabel messageLabel = new JLabel(message.toString());
                        messageLabel.setVerticalAlignment(JLabel.TOP);
                        
                        // Show notification dialog on EDT
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                this,
                                messageLabel,
                                "Thông báo từ thư viện",
                                JOptionPane.WARNING_MESSAGE,
                                null
                            );
                        });
                        
                        // Mark all notifications as read
                        for (Document notif : notifications) {
                            String notificationId = notif.getString("notificationId");
                            if (notificationId != null) {
                                Message markRequest = new Message(Message.MARK_NOTIFICATION_READ, notificationId);
                                client.sendRequest(markRequest);
                            }
                        }
                    }
                    
                    // Update last check time
                    lastNotificationCheckTime = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking notifications: " + e.getMessage());
            // Don't show error to user, just log it
        }
    }
    
    private void loadNewBooks() {
        Message request = new Message(Message.GET_ALL_BOOKS, null);
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<Book> books = (List<Book>) response.getData();
            newBooksModel.setRowCount(0);
            
            // Get only recent 10 books, sorted by some criteria (could be by ID or creation date)
            // For now, just take first 10 books
            int count = 0;
            for (Book book : books) {
                if (count >= 10) break;
                String status = book.getAvailableCopies() > 0 ? "Có sẵn" : "Hết sách";
                newBooksModel.addRow(new Object[]{
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategory(),
                    book.getPublishYear(),
                    status
                });
                count++;
            }
        }
    }
    
    private void refreshUserStats() {
        // Reload user data to get latest stats
        Message request = new Message(Message.GET_ALL_USERS, null);
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<User> users = (List<User>) response.getData();
            for (User u : users) {
                if (u.getUserId() != null && u.getUserId().equals(currentUser.getUserId())) {
                    currentUser = u;
                    break;
                }
            }
        }
        
        // Calculate current borrowed count directly from borrow records to ensure accuracy
        int currentBorrowedCount = getCurrentBorrowedCount();
        
        // Update stats labels
        if (totalBorrowedLabel != null) {
            totalBorrowedLabel.setText(String.valueOf(currentUser.getTotalBorrowed()));
        }
        if (currentBorrowedLabel != null) {
            currentBorrowedLabel.setText(String.valueOf(currentBorrowedCount));
        }
        if (totalFinesLabel != null) {
            totalFinesLabel.setText(String.format("%.0f VNĐ", currentUser.getTotalFines()));
        }
        // Update home stats label
        if (homeStatsLabel != null) {
            homeStatsLabel.setText(String.format("Sách đang mượn: %d | Tổng lượt mượn: %d | Phạt: %.0f VND", 
                currentBorrowedCount, currentUser.getTotalBorrowed(), currentUser.getTotalFines()));
        }
        // Update borrow stats label
        if (borrowStatsLabel != null) {
            int overdueCount = getOverdueCount();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            borrowStatsLabel.setText(String.format("Đang mượn: %d sách | Quá hạn: %d sách | Tối đa: 5 sách | Cập nhật: %s",
                currentBorrowedCount, overdueCount, sdf.format(new java.util.Date())));
        }
        
        // Update welcome label
        if (welcomeLabel != null) {
            welcomeLabel.setText("Chào mừng " + currentUser.getFullName() + "!");
        }
        
        // Update login label in footer
        if (loginLabel != null) {
            loginLabel.setText(currentUser.getFullName() + " (" + currentUser.getEmail() + ")");
        }
    }
    
    private int getCurrentBorrowedCount() {
        int count = 0;
        Message request = new Message(Message.GET_USER_BORROW_RECORDS, currentUser.getUserId());
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<BorrowRecord> records = (List<BorrowRecord>) response.getData();
            for (BorrowRecord record : records) {
                String status = record.getStatus();
                if (status != null && (status.equals("BORROWING") || status.equals("BORROWED"))) {
                    count++;
                }
            }
        }
        return count;
    }
    
    private void loadAllBooksForSearch() {
        Message request = new Message(Message.GET_ALL_BOOKS, null);
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<Book> books = (List<Book>) response.getData();
            searchBooksModel.setRowCount(0);
            for (Book book : books) {
                searchBooksModel.addRow(new Object[]{
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategory(),
                    book.getPublishYear(),
                    book.getPages(),
                    formatPrice(book.getPrice()),
                    book.getAvailableCopies(),
                    book.getTotalCopies()
                });
            }
            updateSearchResultLabel();
        }
    }
    
    private void loadBorrowBooks() {
        Message request = new Message(Message.GET_ALL_BOOKS, null);
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<Book> books = (List<Book>) response.getData();
            borrowBooksModel.setRowCount(0);
            for (Book book : books) {
                if (book.getAvailableCopies() > 0) {
                    borrowBooksModel.addRow(new Object[]{
                        book.getBookId() != null ? book.getBookId() : String.valueOf(book.getId()),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getCategory(),
                        book.getAvailableCopies(),
                        book.getDescription()
                    });
                }
            }
        }
    }
    
    private void loadMyBorrows() {
        Message request = new Message(Message.GET_USER_BORROW_RECORDS, currentUser.getUserId());
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<BorrowRecord> records = (List<BorrowRecord>) response.getData();
            myBorrowsModel.setRowCount(0);
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            
            // Filter only active borrows (BORROWING status) for home panel
            for (BorrowRecord record : records) {
                String status = record.getStatus();
                if (status != null && (status.equals("BORROWING") || status.equals("BORROWED"))) {
                    String borrowDate = record.getBorrowDate() != null ? sdf.format(record.getBorrowDate()) : "";
                    String dueDate = record.getDueDate() != null ? sdf.format(record.getDueDate()) : "";
                    String statusText = "Đang mượn";
                    String fineText = String.format("%.0f VND", record.getFine());
                    
                    myBorrowsModel.addRow(new Object[]{
                        record.getBookTitle(),
                        borrowDate,
                        dueDate,
                        statusText,
                        fineText
                    });
                }
            }
        }
    }
    
    private void searchBooks(String keyword) {
        if (isAccountLocked) {
            showStyledMessage("⚠ Tài khoản của bạn đã bị khóa. Bạn không thể thực hiện thao tác này.", "Tài khoản bị khóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (keyword == null || keyword.trim().isEmpty()) {
            loadAllBooksForSearch();
            return;
        }
        
        Message request = new Message(Message.SEARCH_BOOKS, keyword.trim());
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<Book> books = (List<Book>) response.getData();
            searchBooksModel.setRowCount(0);
            for (Book book : books) {
                searchBooksModel.addRow(new Object[]{
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategory(),
                    book.getPublishYear(),
                    book.getPages(),
                    formatPrice(book.getPrice()),
                    book.getAvailableCopies(),
                    book.getTotalCopies()
                });
            }
            updateSearchResultLabel();
        }
    }
    
    private void borrowBook() {
        if (isAccountLocked) {
            showStyledMessage("⚠ Tài khoản của bạn đã bị khóa. Bạn không thể thực hiện thao tác này.", "Tài khoản bị khóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int selectedRow = borrowBooksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sách cần mượn!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Object bookIdObj = borrowBooksModel.getValueAt(selectedRow, 0);
        String bookId = bookIdObj != null ? bookIdObj.toString() : null;
        int available = ((Number) borrowBooksModel.getValueAt(selectedRow, 4)).intValue();
        
        if (available <= 0) {
            JOptionPane.showMessageDialog(this, "Sách này đã hết, không thể mượn!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn mượn sách này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Message request = new Message(Message.BORROW_BOOK, new Object[]{currentUser.getUserId(), bookId});
            Message response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                // Reload all data including current borrows
                loadBorrowBooks();
                loadAllBooksForSearch();
                        loadCurrentBorrows(); // This updates the "Mượn sách" tab
                        loadHomeCurrentBorrows(); // This updates the "Trang chủ" tab
                        loadMyBorrows();
                        loadNewBooks();
                        refreshUserStats();
                        updateBorrowStats(); // Update borrow statistics
                if (totalBorrowedLabel != null && currentBorrowedLabel != null && totalFinesLabel != null) {
                    // Update profile stats if on profile tab
                    refreshProfile();
                }
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveProfile() {
        String firstName = firstNameField != null ? firstNameField.getText().trim() : "";
        String lastName = lastNameField != null ? lastNameField.getText().trim() : "";
        String phone = phoneField != null ? phoneField.getText().trim() : "";
        String address = addressArea != null ? addressArea.getText().trim() : "";
        String studentId = studentIdField != null ? studentIdField.getText().trim() : "";
        String faculty = facultyField != null ? facultyField.getText().trim() : "";
        String yearOfStudy = yearOfStudyComboBox != null ? (String) yearOfStudyComboBox.getSelectedItem() : "";
        
        if (firstName.isEmpty() || lastName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ và tên không được để trống!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        User updatedUser = new User();
        updatedUser.setUserId(currentUser.getUserId());
        updatedUser.setEmail(currentUser.getEmail());
        updatedUser.setPassword("unchanged");
        updatedUser.setFirstName(firstName);
        updatedUser.setLastName(lastName);
        updatedUser.setPhone(phone);
        updatedUser.setAddress(address);
        updatedUser.setStudentId(studentId);
        updatedUser.setFaculty(faculty);
        updatedUser.setYearOfStudy(yearOfStudy);
        updatedUser.setRole(currentUser.getRole());
        updatedUser.setStatus(currentUser.getStatus());
        
        Message request = new Message(Message.UPDATE_USER, updatedUser);
        Message response = client.sendRequest(request);
        
        if (response.isSuccess()) {
            // Update current user
            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);
            currentUser.setPhone(phone);
            currentUser.setAddress(address);
            currentUser.setStudentId(studentId);
            currentUser.setFaculty(faculty);
            currentUser.setYearOfStudy(yearOfStudy);
            
            // Update UI labels
            setTitle("Hệ thống quản lý thư viện - " + currentUser.getFullName());
            if (welcomeLabel != null) {
                welcomeLabel.setText("Chào mừng " + currentUser.getFullName() + "!");
            }
            if (loginLabel != null) {
                loginLabel.setText(currentUser.getFullName() + " (" + currentUser.getEmail() + ")");
            }
            
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            refreshProfileFields();
        } else {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refreshProfile() {
        if (isAccountLocked) {
            showStyledMessage("⚠ Tài khoản của bạn đã bị khóa. Bạn không thể thực hiện thao tác này.", "Tài khoản bị khóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Reload user data
        Message request = new Message(Message.GET_ALL_USERS, null);
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<User> users = (List<User>) response.getData();
            for (User u : users) {
                if (u.getUserId() != null && u.getUserId().equals(currentUser.getUserId())) {
                    currentUser = u;
                    break;
                }
            }
        }
        
        // Update fields
        refreshProfileFields();
        
        // Update stats labels
        if (totalBorrowedLabel != null) {
            totalBorrowedLabel.setText(String.valueOf(currentUser.getTotalBorrowed()));
        }
        if (currentBorrowedLabel != null) {
            currentBorrowedLabel.setText(String.valueOf(currentUser.getCurrentBorrowed()));
        }
        if (totalFinesLabel != null) {
            totalFinesLabel.setText(String.format("%.0f VND", currentUser.getTotalFines()));
        }
        if (statusLabel != null) {
            String userStatus = currentUser.getStatus() != null ? currentUser.getStatus() : "ACTIVE";
            statusLabel.setText("ACTIVE".equals(userStatus) ? "Hoạt động" : "Bị khóa");
        }
        
        // Update UI labels
        setTitle("Hệ thống quản lý thư viện - " + currentUser.getFullName());
        if (welcomeLabel != null) {
            welcomeLabel.setText("Chào mừng " + currentUser.getFullName() + "!");
        }
        if (loginLabel != null) {
            loginLabel.setText(currentUser.getFullName() + " (" + currentUser.getEmail() + ")");
        }
        
        JOptionPane.showMessageDialog(this, "Đã làm mới thông tin!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void logout() {
        int confirm = showStyledConfirmDialog(
            "Bạn có chắc chắn muốn đăng xuất?",
            "Xác nhận đăng xuất"
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Message request = new Message(Message.LOGOUT, null);
                client.sendRequest(request);
                client.disconnect();
                dispose();
                new LoginFrame().setVisible(true);
            } catch (Exception e) {
                System.err.println("Error during logout: " + e.getMessage());
                // Still close the window even if logout request fails
                dispose();
                new LoginFrame().setVisible(true);
            }
        }
    }
    
    // Show borrow history dialog with all borrow records
    private void showBorrowHistoryDialog() {
        JDialog dialog = new JDialog(this, "📚 Lịch sử mượn sách", true);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG_LIGHT);
        
        // Main panel with rounded border
        JPanel mainPanel = createRoundedPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorderUser(15, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setOpaque(true);
        
        // Title section
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        JLabel titleLabel = new JLabel("📚 Lịch sử mượn sách");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Refresh button
        JButton refreshBtn = createStyledButton("🔄 Làm mới", PRIMARY_BLUE, null);
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setPreferredSize(new Dimension(100, 30));
        titlePanel.add(refreshBtn, BorderLayout.EAST);
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Table for history
        String[] historyColumns = {"Mã sách", "Tên sách", "Ngày mượn", "Hạn trả", "Ngày trả", "Trạng thái", "Phạt (VND)"};
        DefaultTableModel historyModel = new DefaultTableModel(historyColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable historyTable = new JTable(historyModel);
        styleTable(historyTable);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Load history data
        Runnable loadHistory = () -> {
            try {
                historyModel.setRowCount(0);
                Message request = new Message(Message.GET_USER_BORROW_RECORDS, currentUser.getUserId());
                Message response = client.sendRequest(request);
                
                if (response.isSuccess() && response.getData() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<BorrowRecord> records = (List<BorrowRecord>) response.getData();
                    
                    if (records != null && !records.isEmpty()) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                        
                        // Sort by borrow date (newest first)
                        records.sort((r1, r2) -> {
                            if (r1.getBorrowDate() == null && r2.getBorrowDate() == null) return 0;
                            if (r1.getBorrowDate() == null) return 1;
                            if (r2.getBorrowDate() == null) return -1;
                            return r2.getBorrowDate().compareTo(r1.getBorrowDate());
                        });
                        
                        for (BorrowRecord record : records) {
                            if (record == null) continue;
                            
                            String bookId = record.getBookId() != null ? record.getBookId() : "";
                            String bookTitle = record.getBookTitle();
                            if (bookTitle == null || bookTitle.isEmpty()) {
                                bookTitle = "N/A";
                            }
                            
                            String borrowDate = record.getBorrowDate() != null ? sdf.format(record.getBorrowDate()) : "";
                            String dueDate = record.getDueDate() != null ? sdf.format(record.getDueDate()) : "";
                            String returnDate = record.getReturnDate() != null ? sdf.format(record.getReturnDate()) : "Chưa trả";
                            
                            String status = record.getStatus();
                            String statusText = "";
                            if (status == null) {
                                statusText = "N/A";
                            } else if (status.equals("BORROWING") || status.equals("BORROWED")) {
                                statusText = "Đang mượn";
                            } else if (status.equals("RETURNED")) {
                                statusText = "Đã trả";
                            } else if (status.equals("LOST")) {
                                statusText = "Mất";
                            } else if (status.equals("DAMAGED")) {
                                statusText = "Hư hỏng";
                            } else {
                                statusText = status;
                            }
                            
                            double fine = record.getFine() > 0 ? record.getFine() : 0.0;
                            String fineText = String.format("%.0f", fine);
                            
                            historyModel.addRow(new Object[]{
                                bookId,
                                bookTitle,
                                borrowDate,
                                dueDate,
                                returnDate,
                                statusText,
                                fineText
                            });
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading borrow history: " + e.getMessage());
                e.printStackTrace();
                showStyledMessage("✗ Lỗi khi tải lịch sử mượn: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        };
        
        refreshBtn.addActionListener(e -> {
            if (!isAccountLocked) {
                loadHistory.run();
            } else {
                showStyledMessage("⚠ Tài khoản của bạn đã bị khóa. Bạn không thể thực hiện thao tác này.", "Tài khoản bị khóa", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        // Initial load
        loadHistory.run();
        
        // Wrap table in scroll pane
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        JButton closeButton = createStyledButton("Đóng", PRIMARY_BLUE, null);
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
}
