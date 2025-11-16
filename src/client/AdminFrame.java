package client;

import model.Book;
import model.BookCopy;
import model.BorrowRecord;
import model.User;
import util.Message;
import org.bson.Document;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

// Custom rounded border class
class RoundedBorder implements Border {
    private int radius;
    private Color color;
    
    RoundedBorder(int radius, Color color) {
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

public class AdminFrame extends JFrame {
    private Client client;
    private User currentUser;
    
    // Tables
    private JTable booksTable;
    private JTable usersTable;
    private JTable borrowTable;
    private JTable bookCopiesTable;
    
    // Table models
    private DefaultTableModel booksModel;
    private DefaultTableModel usersModel;
    private DefaultTableModel borrowModel;
    private DefaultTableModel bookCopiesModel;
    
    // Dashboard labels
    private JLabel[] statLabels = new JLabel[8];
    
    // Dashboard tables
    private JTable recentBorrowsTable;
    private JTable newUsersTable;
    private DefaultTableModel recentBorrowsModel;
    private DefaultTableModel newUsersModel;
    
    private JTabbedPane mainTabbedPane;
    
    // Book management components
    private JTextField searchKeywordField;
    private JComboBox<String> categoryComboBox;
    
    // User management components
    private JTextField searchUserField;
    private JComboBox<String> statusFilterComboBox;
    private JComboBox<String> roleFilterComboBox;
    private JLabel totalUsersLabel;
    
    // Borrow management components
    private JTextField searchBorrowField;
    private JComboBox<String> statusBorrowFilterComboBox;
    private JLabel recordsCountLabel;
    
    // Report management components
    private JComboBox<String> timeRangeComboBox;
    private JTabbedPane reportSubTabs;
    private JTable bookReportTable;
    private JTable userReportTable;
    private JTable borrowReportTable;
    private JTable penaltyReportTable;
    private DefaultTableModel bookReportModel;
    private DefaultTableModel userReportModel;
    private DefaultTableModel borrowReportModel;
    private DefaultTableModel penaltyReportModel;
    private JLabel reportStatusLabel;
    
    public AdminFrame(Client client, User user) {
        this.client = client;
        this.currentUser = user;
        initializeComponents();
        setupLayout();
        loadData();
    }
    
    private void initializeComponents() {
        setTitle("Hệ thống quản lý thư viện - Admin Panel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);
        
        // Books table - updated columns
        String[] bookColumns = {"ID", "Tiêu đề", "Tác giả", "ISBN", "Thể loại", "Năm XB", "Giá", "Tổng số", "Có sẵn", "Trạng thái"};
        booksModel = new DefaultTableModel(bookColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        booksTable = new JTable(booksModel);
        styleTable(booksTable);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadBookCopies();
            }
        });
        
        // Book copies table
        String[] copyColumns = {"ID", "Sách", "Trạng thái", "Vị trí", "Ghi chú"};
        bookCopiesModel = new DefaultTableModel(copyColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookCopiesTable = new JTable(bookCopiesModel);
        styleTable(bookCopiesTable);
        bookCopiesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Users table - updated columns
        String[] userColumns = {"ID", "Email", "Họ tên", "Mã SV", "Khoa", "Năm học", "Vai trò", "Trạng thái", "Số sách mượn", "Tổng phạt", "Ngày đăng ký"};
        usersModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        usersTable = new JTable(usersModel);
        styleTable(usersTable);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Allow last column to auto-resize to fill remaining space
        usersTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Borrow records table - updated columns
        String[] borrowColumns = {"Mã mượn", "Người dùng", "Sách", "Ngày mượn", "Hạn trả", "Ngày trả", "Trạng thái", "Phạt (VND)"};
        borrowModel = new DefaultTableModel(borrowColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        borrowTable = new JTable(borrowModel);
        styleTable(borrowTable);
        borrowTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Dashboard tables
        String[] recentBorrowsColumns = {"ID", "Người mượn", "Sách", "Ngày mượn", "Hạn trả", "Trạng thái"};
        recentBorrowsModel = new DefaultTableModel(recentBorrowsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recentBorrowsTable = new JTable(recentBorrowsModel);
        styleTable(recentBorrowsTable);
        recentBorrowsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentBorrowsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        String[] newUsersColumns = {"ID", "Họ tên", "Email", "Mã SV", "Khoa", "Trạng thái"};
        newUsersModel = new DefaultTableModel(newUsersColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        newUsersTable = new JTable(newUsersModel);
        styleTable(newUsersTable);
        newUsersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        newUsersTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Borrow management components
        searchBorrowField = new JTextField(20);
        statusBorrowFilterComboBox = new JComboBox<>(new String[]{"Tất cả trạng thái", "Đang mượn", "Đã trả", "Mất", "Hỏng"});
        recordsCountLabel = new JLabel("Đã tải 0 bản ghi");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel - Header with ORANGE background (matching login)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 127, 0)); // Orange #FF7F00 - matching login
        topPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        
        // Left side - Title and welcome message
        JPanel leftTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftTopPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Hệ thống quản lý thư viện - Admin Panel");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        leftTopPanel.add(titleLabel);
        topPanel.add(leftTopPanel, BorderLayout.WEST);
        
        // Right side - University name and Logout button
        JPanel rightTopPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightTopPanel.setOpaque(false);
        JLabel universityLabel = new JLabel("Đại Nam Univers");
        universityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        universityLabel.setForeground(Color.WHITE);
        rightTopPanel.add(universityLabel);
        JButton logoutButton = createStyledButton("Đăng xuất", new Color(255, 127, 0), null);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.addActionListener(e -> logout());
        rightTopPanel.add(logoutButton);
        topPanel.add(rightTopPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel - Tabbed pane with modern styling
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mainTabbedPane.setBackground(Color.WHITE); // White background for tabs
        mainTabbedPane.setForeground(new Color(44, 62, 80)); // Dark text
        
        // Custom tab renderer to add red underline for active tab
        mainTabbedPane.addChangeListener(e -> {
            for (int i = 0; i < mainTabbedPane.getTabCount(); i++) {
                mainTabbedPane.setBackgroundAt(i, Color.WHITE);
            }
        });
        
        // Tab 1: Tổng quát (Dashboard)
        mainTabbedPane.addTab("📊 Tổng quát", createDashboardPanel());
        
        // Tab 2: Quản lý sách
        mainTabbedPane.addTab("📚 Quản lý sách", createBooksPanel());
        
        // Tab 3: Quản lý người dùng
        mainTabbedPane.addTab("👥 Quản lý người dùng", createUsersPanel());
        
        // Tab 4: Quản lý mượn trả
        mainTabbedPane.addTab("🔄 Quản lý mượn trả", createBorrowPanel());
        
        // Tab 5: Báo cáo
        mainTabbedPane.addTab("📈 Báo cáo", createReportsPanel());
        
        add(mainTabbedPane, BorderLayout.CENTER);
        
        // Status bar - Modern design
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(52, 73, 94)); // Dark blue-gray
        statusPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        JPanel leftStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftStatusPanel.setOpaque(false);
        if (recordsCountLabel != null) {
            recordsCountLabel.setForeground(new Color(236, 240, 241));
            recordsCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            leftStatusPanel.add(recordsCountLabel);
        }
        JLabel loginLabel = new JLabel("👤 " + currentUser.getFullName() + " (" + currentUser.getEmail() + ")");
        loginLabel.setForeground(new Color(236, 240, 241));
        loginLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        leftStatusPanel.add(loginLabel);
        statusPanel.add(leftStatusPanel, BorderLayout.WEST);
        JLabel statusLabel = new JLabel("✓ Sẵn sàng");
        statusLabel.setForeground(new Color(46, 204, 113)); // Green
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusPanel.add(statusLabel, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createDashboardPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        mainPanel.setBackground(BG_LIGHT);
        
        // Summary cards panel - 8 cards: 4 in top row, 4 in bottom row (smaller and closer to top)
        JPanel cardsPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        cardsPanel.setBackground(BG_LIGHT);
        
        // Top row: 4 cards
        statLabels[0] = createStatCard("Tổng số sách", "0", cardsPanel);
        statLabels[1] = createStatCard("Tổng số người dùng", "0", cardsPanel);
        statLabels[2] = createStatCard("Tổng số lượt mượn", "0", cardsPanel);
        statLabels[3] = createStatCard("Sách có sẵn", "0", cardsPanel);
        
        // Bottom row: 4 cards
        statLabels[4] = createStatCard("Sách đang mượn", "0", cardsPanel);
        statLabels[5] = createStatCard("Tổng số phạt", "0 VND", cardsPanel);
        statLabels[6] = createStatCard("Sách quá hạn", "0", cardsPanel);
        statLabels[7] = createStatCard("Người dùng chờ duyệt", "0", cardsPanel);
        
        mainPanel.add(cardsPanel, BorderLayout.NORTH);
        
        // Two tables side by side - Make them larger
        JSplitPane tablesSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        tablesSplitPane.setDividerLocation(750);
        tablesSplitPane.setResizeWeight(0.5);
        tablesSplitPane.setDividerSize(8);
        
        // Left panel: Recent Borrows - wrapped in rounded panel, larger
        JPanel recentBorrowsPanelRounded = createRoundedPanel();
        recentBorrowsPanelRounded.setLayout(new BorderLayout());
        recentBorrowsPanelRounded.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Lượt mượn gần đây"),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        recentBorrowsPanelRounded.setBackground(Color.WHITE);
        recentBorrowsPanelRounded.setOpaque(true);
        recentBorrowsPanelRounded.add(new JScrollPane(recentBorrowsTable), BorderLayout.CENTER);
        JButton refreshRecentBorrowsBtn = createStyledButton("Làm mới", PRIMARY_BLUE, null);
        refreshRecentBorrowsBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshRecentBorrowsBtn.setPreferredSize(new Dimension(100, 32));
        refreshRecentBorrowsBtn.addActionListener(e -> loadRecentBorrows());
        JPanel recentBorrowsButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        recentBorrowsButtonPanel.setOpaque(false);
        recentBorrowsButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        recentBorrowsButtonPanel.add(refreshRecentBorrowsBtn);
        recentBorrowsPanelRounded.add(recentBorrowsButtonPanel, BorderLayout.SOUTH);
        
        // Right panel: New Users - wrapped in rounded panel, larger
        JPanel newUsersPanelRounded = createRoundedPanel();
        newUsersPanelRounded.setLayout(new BorderLayout());
        newUsersPanelRounded.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Người dùng mới"),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        newUsersPanelRounded.setBackground(Color.WHITE);
        newUsersPanelRounded.setOpaque(true);
        newUsersPanelRounded.add(new JScrollPane(newUsersTable), BorderLayout.CENTER);
        JButton refreshNewUsersBtn = createStyledButton("Làm mới", PRIMARY_BLUE, null);
        refreshNewUsersBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshNewUsersBtn.setPreferredSize(new Dimension(100, 32));
        refreshNewUsersBtn.addActionListener(e -> loadNewUsers());
        JPanel newUsersButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        newUsersButtonPanel.setOpaque(false);
        newUsersButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        newUsersButtonPanel.add(refreshNewUsersBtn);
        newUsersPanelRounded.add(newUsersButtonPanel, BorderLayout.SOUTH);
        
        tablesSplitPane.setLeftComponent(recentBorrowsPanelRounded);
        tablesSplitPane.setRightComponent(newUsersPanelRounded);
        
        mainPanel.add(tablesSplitPane, BorderLayout.CENTER);
        
        // Quick Actions section - Smaller and more compact
        JPanel quickActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JPanel quickActionsPanelRounded = createRoundedPanel();
        quickActionsPanelRounded.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Thao tác nhanh"),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        quickActionsPanelRounded.setBackground(BG_LIGHT);
        quickActionsPanel = quickActionsPanelRounded;
        
        // Quick action buttons - RED color matching image, more compact buttons
        Color QUICK_ACTION_ORANGE = new Color(255, 127, 0);
        JButton addBookQuickBtn = createStyledButton("Thêm sách mới", QUICK_ACTION_ORANGE, null);
        addBookQuickBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        addBookQuickBtn.setPreferredSize(new Dimension(120, 32));
        addBookQuickBtn.addActionListener(e -> {
            // Switch to books tab and show add dialog
            if (mainTabbedPane != null) {
                mainTabbedPane.setSelectedIndex(1); // Books tab index
            }
            showAddBookDialog();
        });
        
        JButton addUserQuickBtn = createStyledButton("Thêm người dùng", QUICK_ACTION_ORANGE, null);
        addUserQuickBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        addUserQuickBtn.setPreferredSize(new Dimension(120, 32));
        addUserQuickBtn.addActionListener(e -> {
            // Switch to users tab and show add dialog
            if (mainTabbedPane != null) {
                mainTabbedPane.setSelectedIndex(2); // Users tab index
            }
            showAddUserDialog();
        });
        
        JButton viewReportsQuickBtn = createStyledButton("Xem báo cáo", QUICK_ACTION_ORANGE, null);
        viewReportsQuickBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        viewReportsQuickBtn.setPreferredSize(new Dimension(120, 32));
        viewReportsQuickBtn.addActionListener(e -> {
            // Switch to reports tab
            if (mainTabbedPane != null) {
                mainTabbedPane.setSelectedIndex(4); // Reports tab index
            }
        });
        
        JButton systemSettingsBtn = createStyledButton("Cài đặt", QUICK_ACTION_ORANGE, null);
        systemSettingsBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        systemSettingsBtn.setPreferredSize(new Dimension(120, 32));
        systemSettingsBtn.addActionListener(e -> showSettingsDialog());
        
        quickActionsPanel.add(addBookQuickBtn);
        quickActionsPanel.add(addUserQuickBtn);
        quickActionsPanel.add(viewReportsQuickBtn);
        quickActionsPanel.add(systemSettingsBtn);
        
        mainPanel.add(quickActionsPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private JLabel createStatCard(String title, String value, JPanel parent) {
        JPanel card = createRoundedPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        card.setBackground(Color.WHITE);
        card.setOpaque(true);
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLabel.setForeground(new Color(127, 140, 141)); // Gray
        
        JLabel valueLabel = new JLabel(value, JLabel.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(new Color(255, 127, 0)); // ORANGE - matching login
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        // Add subtle hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(245, 245, 245));
                card.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(Color.WHITE);
                card.repaint();
            }
        });
        
        parent.add(card);
        return valueLabel;
    }
    
    // Brighter color scheme
    private final Color PRIMARY_BLUE = new Color(66, 133, 244); // Bright blue
    private final Color PRIMARY_GREEN = new Color(52, 199, 89); // Bright green
    private final Color PRIMARY_ORANGE = new Color(255, 149, 0); // Bright orange
    private final Color PRIMARY_RED = new Color(255, 59, 48); // Bright red
    private final Color PRIMARY_PURPLE = new Color(175, 82, 222); // Bright purple
    private final Color PRIMARY_YELLOW = new Color(255, 204, 0); // Bright yellow
    private final Color PRIMARY_GRAY = new Color(142, 142, 147); // Gray
    private final Color BG_LIGHT = new Color(242, 242, 247); // Light background
    private final Color PANEL_WHITE = Color.WHITE;
    
    // Helper method to create rounded text field
    private JTextField createRoundedTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(Color.WHITE);
        field.setOpaque(true);
        return field;
    }
    
    // Helper method to create rounded combo box
    private JComboBox<String> createRoundedComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        combo.setBackground(Color.WHITE);
        combo.setOpaque(true);
        return combo;
    }
    
    // Helper method to style tables with rounded corners
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(28);
        
        // Set foreground and background for better visibility
        table.setForeground(new Color(28, 28, 30)); // Dark text color
        table.setBackground(Color.WHITE);
        
        // Brighter selection colors
        table.setSelectionBackground(new Color(66, 133, 244)); // Bright blue
        table.setSelectionForeground(Color.WHITE);
        
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
        
        // Enable alternating row colors for better readability
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Set text color
                c.setForeground(new Color(44, 62, 80));
                
                // Alternating row colors
                if (!isSelected) {
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
    
    // Helper method to create styled TitledBorder with visible text and rounded corners
    private javax.swing.border.TitledBorder createStyledTitledBorder(String title) {
        javax.swing.border.TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createCompoundBorder(
                new RoundedBorder(12, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ),
            title
        );
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        border.setTitleColor(new Color(66, 133, 244)); // Bright blue
        return border;
    }
    
    // Helper method to create border with red left border (matching image)
    private Border createRedLeftBorder(String title) {
        Border redLeftBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(255, 127, 0)), // Orange left border
            BorderFactory.createEmptyBorder(8, 12, 8, 8)
        );
        if (title != null && !title.isEmpty()) {
            javax.swing.border.TitledBorder titled = BorderFactory.createTitledBorder(redLeftBorder, title);
            titled.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
            titled.setTitleColor(new Color(44, 62, 80));
            return titled;
        }
        return redLeftBorder;
    }
    
    // Helper method to create styled buttons with rounded corners
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
            // Create brighter hover color
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
    
    // Helper method to create rounded panel
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
    
    private JPanel createUsersPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(BG_LIGHT);
        
        // Search and filter panel - with red left border matching image
        JPanel searchPanelRounded = createRoundedPanel();
        searchPanelRounded.setLayout(new GridBagLayout());
        searchPanelRounded.setBorder(createRedLeftBorder("Tìm kiếm và bộ lọc"));
        searchPanelRounded.setBackground(Color.WHITE);
        searchPanelRounded.setOpaque(true);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        searchUserField = createRoundedTextField(20);
        statusFilterComboBox = createRoundedComboBox(new String[]{"Tất cả", "ACTIVE", "LOCKED"});
        roleFilterComboBox = createRoundedComboBox(new String[]{"Tất cả", "ADMIN", "USER"});
        totalUsersLabel = new JLabel("Tổng: 0 người dùng");
        
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.addActionListener(e -> performUserSearch());
        
        JButton refreshButton = createStyledButton("Làm mới", PRIMARY_BLUE, null);
        refreshButton.addActionListener(e -> {
            searchUserField.setText("");
            statusFilterComboBox.setSelectedIndex(0);
            roleFilterComboBox.setSelectedIndex(0);
            loadUsers();
        });
        
        gbc.gridx = 0; gbc.gridy = 0;
        searchPanelRounded.add(totalUsersLabel, gbc);
        gbc.gridx = 1;
        searchPanelRounded.add(new JLabel("Tìm kiếm:"), gbc);
        gbc.gridx = 2;
        searchPanelRounded.add(searchUserField, gbc);
        gbc.gridx = 3;
        searchPanelRounded.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 4;
        searchPanelRounded.add(statusFilterComboBox, gbc);
        gbc.gridx = 5;
        searchPanelRounded.add(new JLabel("Vai trò:"), gbc);
        gbc.gridx = 6;
        searchPanelRounded.add(roleFilterComboBox, gbc);
        gbc.gridx = 7;
        searchPanelRounded.add(searchButton, gbc);
        gbc.gridx = 8;
        searchPanelRounded.add(refreshButton, gbc);
        
        mainPanel.add(searchPanelRounded, BorderLayout.NORTH);
        
        // User list table - wrapped in rounded panel with proper padding
        JPanel tablePanelRounded = createRoundedPanel();
        tablePanelRounded.setLayout(new BorderLayout());
        tablePanelRounded.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Danh sách người dùng"),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        tablePanelRounded.setBackground(Color.WHITE);
        tablePanelRounded.setOpaque(true);
        
        // Create scroll pane and ensure it fills the available space
        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tablePanelRounded.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(tablePanelRounded, BorderLayout.CENTER);
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton addButton = createStyledButton("Thêm người dùng", PRIMARY_GREEN, null);
        addButton.addActionListener(e -> showAddUserDialog());
        
        JButton editButton = createStyledButton("Sửa thông tin", PRIMARY_YELLOW, null);
        editButton.setForeground(new Color(28, 28, 30));
        editButton.addActionListener(e -> showEditUserDialog());
        
        JButton deleteButton = createStyledButton("Xóa người dùng", PRIMARY_RED, null);
        deleteButton.addActionListener(e -> deleteUser());
        
        JButton lockButton = createStyledButton("Khóa tài khoản", PRIMARY_ORANGE, null);
        lockButton.addActionListener(e -> lockUser());
        
        JButton unlockButton = createStyledButton("Mở khóa tài khoản", PRIMARY_GREEN, null);
        unlockButton.setForeground(new Color(28, 28, 30));
        unlockButton.addActionListener(e -> unlockUser());
        
        JButton resetPasswordButton = createStyledButton("Đặt lại mật khẩu", PRIMARY_PURPLE, null);
        resetPasswordButton.addActionListener(e -> resetPassword());
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(lockButton);
        buttonPanel.add(unlockButton);
        buttonPanel.add(resetPasswordButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private void performUserSearch() {
        loadUsers();
        
        String keyword = searchUserField.getText().trim().toLowerCase();
        String statusFilter = (String) statusFilterComboBox.getSelectedItem();
        String roleFilter = (String) roleFilterComboBox.getSelectedItem();
        
        if (keyword.isEmpty() && "Tất cả".equals(statusFilter) && "Tất cả".equals(roleFilter)) {
            return; // No filter needed
        }
        
        DefaultTableModel model = (DefaultTableModel) usersTable.getModel();
        int rowCount = model.getRowCount();
        
        for (int i = rowCount - 1; i >= 0; i--) {
            boolean shouldRemove = false;
            
            // Filter by keyword
            if (!keyword.isEmpty()) {
                String email = ((String) model.getValueAt(i, 1)).toLowerCase();
                String name = ((String) model.getValueAt(i, 2)).toLowerCase();
                String studentId = model.getValueAt(i, 3) != null ? model.getValueAt(i, 3).toString().toLowerCase() : "";
                if (!email.contains(keyword) && !name.contains(keyword) && !studentId.contains(keyword)) {
                    shouldRemove = true;
                }
            }
            
            // Filter by status
            if (!"Tất cả".equals(statusFilter)) {
                String status = (String) model.getValueAt(i, 7);
                if (!statusFilter.equals(status)) {
                    shouldRemove = true;
                }
            }
            
            // Filter by role
            if (!"Tất cả".equals(roleFilter)) {
                String role = (String) model.getValueAt(i, 6);
                if (!roleFilter.equals(role)) {
                    shouldRemove = true;
                }
            }
            
            if (shouldRemove) {
                model.removeRow(i);
            }
        }
    }
    
    private JPanel createBorrowPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(BG_LIGHT);
        
        // Search and filter panel - with red left border matching image
        JPanel searchPanel = new JPanel(new GridBagLayout());
        JPanel searchPanelRounded = createRoundedPanel();
        searchPanelRounded.setBorder(createRedLeftBorder("Tìm kiếm và bộ lọc"));
        searchPanelRounded.setBackground(Color.WHITE);
        searchPanel = searchPanelRounded;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        searchBorrowField = createRoundedTextField(20);
        statusBorrowFilterComboBox = createRoundedComboBox(new String[]{"Tất cả trạng thái", "Đang mượn", "Đã trả", "Mất", "Hỏng"});
        recordsCountLabel = new JLabel("Đã tải 0 bản ghi");
        
        JButton searchButton = createStyledButton("Tìm kiếm", PRIMARY_BLUE, null);
        searchButton.addActionListener(e -> performBorrowSearch());
        
        JButton refreshButton = createStyledButton("Làm mới", PRIMARY_GRAY, null);
        refreshButton.addActionListener(e -> {
            searchBorrowField.setText("");
            statusBorrowFilterComboBox.setSelectedIndex(0);
            loadBorrowRecords();
        });
        
        gbc.gridx = 0; gbc.gridy = 0;
        searchPanel.add(new JLabel("Từ khóa:"), gbc);
        gbc.gridx = 1;
        searchPanel.add(searchBorrowField, gbc);
        gbc.gridx = 2;
        searchPanel.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 3;
        searchPanel.add(statusBorrowFilterComboBox, gbc);
        gbc.gridx = 4;
        searchPanel.add(searchButton, gbc);
        gbc.gridx = 5;
        searchPanel.add(refreshButton, gbc);
        
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Borrow/Return list table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Danh sách mượn/trả"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.add(new JScrollPane(borrowTable), BorderLayout.CENTER);
        
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton returnButton = createStyledButton("Trả sách", PRIMARY_GREEN, null);
        returnButton.addActionListener(e -> adminReturnBook());
        
        JButton markLostButton = createStyledButton("Đánh dấu mất", PRIMARY_RED, null);
        markLostButton.addActionListener(e -> markAsLost());
        
        JButton markDamagedButton = createStyledButton("Đánh dấu hỏng", PRIMARY_ORANGE, null);
        markDamagedButton.addActionListener(e -> markAsDamaged());
        
        JButton forceReturnButton = createStyledButton("Bắt buộc trả", PRIMARY_PURPLE, null);
        forceReturnButton.addActionListener(e -> forceReturn());
        
        buttonPanel.add(returnButton);
        buttonPanel.add(markLostButton);
        buttonPanel.add(markDamagedButton);
        buttonPanel.add(forceReturnButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private void performBorrowSearch() {
        loadBorrowRecords();
        
        String keyword = searchBorrowField.getText().trim().toLowerCase();
        String statusFilter = (String) statusBorrowFilterComboBox.getSelectedItem();
        
        if (keyword.isEmpty() && "Tất cả trạng thái".equals(statusFilter)) {
            return; // No filter needed
        }
        
        DefaultTableModel model = (DefaultTableModel) borrowTable.getModel();
        int rowCount = model.getRowCount();
        
        for (int i = rowCount - 1; i >= 0; i--) {
            boolean shouldRemove = false;
            
            // Filter by keyword
            if (!keyword.isEmpty()) {
                String recordId = model.getValueAt(i, 0).toString().toLowerCase();
                String username = model.getValueAt(i, 1).toString().toLowerCase();
                String bookTitle = model.getValueAt(i, 2).toString().toLowerCase();
                if (!recordId.contains(keyword) && !username.contains(keyword) && !bookTitle.contains(keyword)) {
                    shouldRemove = true;
                }
            }
            
            // Filter by status
            if (!"Tất cả trạng thái".equals(statusFilter)) {
                String statusText = (String) model.getValueAt(i, 6);
                if (!statusFilter.equals(statusText)) {
                    shouldRemove = true;
                }
            }
            
            if (shouldRemove) {
                model.removeRow(i);
            }
        }
    }
    
    private String getStatusText(String status) {
        if ("BORROWING".equals(status) || "BORROWED".equals(status)) {
            return "Đang mượn";
        } else if ("RETURNED".equals(status)) {
            return "Đã trả";
        } else if ("LOST".equals(status)) {
            return "Mất";
        } else if ("DAMAGED".equals(status)) {
            return "Hỏng";
        }
        return status;
    }
    
    private void adminReturnBook() {
        int selectedRow = borrowTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi mượn sách cần trả!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Object recordIdObj = borrowModel.getValueAt(selectedRow, 0);
        String recordId = recordIdObj != null ? recordIdObj.toString() : null;
        
        String status = (String) borrowModel.getValueAt(selectedRow, 6);
        if ("Đã trả".equals(status)) {
            JOptionPane.showMessageDialog(this, "Sách này đã được trả rồi!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn trả sách này?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Message request = new Message(Message.RETURN_BOOK, recordId);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                // Reload all data to sync with user
                loadBorrowRecords();
                loadBooks();
                loadBookCopies();
                loadDashboardStats();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void markAsLost() {
        int selectedRow = borrowTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi cần đánh dấu mất!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Object recordIdObj = borrowModel.getValueAt(selectedRow, 0);
        String recordId = recordIdObj != null ? recordIdObj.toString() : null;
        
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đánh dấu sách này là mất?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Message request = new Message(Message.MARK_LOST, recordId);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                // Reload all data to sync with user
                loadBorrowRecords();
                loadBooks();
                loadBookCopies();
                loadDashboardStats();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void markAsDamaged() {
        int selectedRow = borrowTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi cần đánh dấu hỏng!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Object recordIdObj = borrowModel.getValueAt(selectedRow, 0);
        String recordId = recordIdObj != null ? recordIdObj.toString() : null;
        
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đánh dấu sách này là hỏng?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Message request = new Message(Message.MARK_DAMAGED, recordId);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                // Reload all data to sync with user
                loadBorrowRecords();
                loadBooks();
                loadBookCopies();
                loadDashboardStats();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void forceReturn() {
        int selectedRow = borrowTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi cần bắt buộc trả!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Object recordIdObj = borrowModel.getValueAt(selectedRow, 0);
        String recordId = recordIdObj != null ? recordIdObj.toString() : null;
        
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn bắt buộc trả sách này?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Message request = new Message(Message.FORCE_RETURN, recordId);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                // Reload all data to sync with user
                loadBorrowRecords();
                loadBooks();
                loadBookCopies();
                loadDashboardStats();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private JPanel createBooksPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(BG_LIGHT);
        
        // Search and filter panel - with red left border matching image
        JPanel searchPanelRounded = createRoundedPanel();
        searchPanelRounded.setLayout(new GridBagLayout());
        searchPanelRounded.setBorder(createRedLeftBorder("Tìm kiếm và bộ lọc"));
        searchPanelRounded.setBackground(Color.WHITE);
        searchPanelRounded.setOpaque(true);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        searchKeywordField = createRoundedTextField(20);
        categoryComboBox = createRoundedComboBox(new String[]{"Tất cả thể loại"});
        categoryComboBox.addItem("Công nghệ thông tin");
        categoryComboBox.addItem("Kinh tế");
        categoryComboBox.addItem("Y học");
        categoryComboBox.addItem("Kỹ thuật");
        categoryComboBox.addItem("Văn học");
        categoryComboBox.addItem("Lịch sử");
        categoryComboBox.addItem("Tâm lý học");
        
        JButton searchButton = createStyledButton("Tìm kiếm", PRIMARY_GREEN, null);
        searchButton.addActionListener(e -> performSearch());
        
        JButton refreshButton = createStyledButton("Làm mới", PRIMARY_GRAY, null);
        refreshButton.addActionListener(e -> {
            searchKeywordField.setText("");
            categoryComboBox.setSelectedIndex(0);
            loadBooks();
        });
        
        gbc.gridx = 0; gbc.gridy = 0;
        searchPanelRounded.add(new JLabel("Từ khóa:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.3;
        searchPanelRounded.add(searchKeywordField, gbc);
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        searchPanelRounded.add(new JLabel("Thể loại:"), gbc);
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.3;
        searchPanelRounded.add(categoryComboBox, gbc);
        gbc.gridx = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        searchPanelRounded.add(searchButton, gbc);
        gbc.gridx = 5;
        searchPanelRounded.add(refreshButton, gbc);
        
        mainPanel.add(searchPanelRounded, BorderLayout.NORTH);
        
        // Two panels side by side
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(700);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(8);
        
        // Left panel: Book list - wrapped in rounded panel for symmetry
        JPanel leftPanelRounded = createRoundedPanel();
        leftPanelRounded.setLayout(new BorderLayout());
        leftPanelRounded.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Danh sách sách"),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        leftPanelRounded.setBackground(Color.WHITE);
        leftPanelRounded.setOpaque(true);
        
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftButtonPanel.setOpaque(false);
        JButton addBookButton = createStyledButton("Thêm sách", PRIMARY_GREEN, null);
        addBookButton.addActionListener(e -> showAddBookDialog());
        
        JButton editBookButton = createStyledButton("Sửa sách", PRIMARY_YELLOW, null);
        editBookButton.setForeground(new Color(28, 28, 30));
        editBookButton.addActionListener(e -> showEditBookDialog());
        
        JButton deleteBookButton = createStyledButton("Xóa sách", PRIMARY_RED, null);
        deleteBookButton.addActionListener(e -> deleteBook());
        
        leftButtonPanel.add(addBookButton);
        leftButtonPanel.add(editBookButton);
        leftButtonPanel.add(deleteBookButton);
        
        leftPanelRounded.add(leftButtonPanel, BorderLayout.NORTH);
        leftPanelRounded.add(new JScrollPane(booksTable), BorderLayout.CENTER);
        
        // Right panel: Book copies - wrapped in rounded panel for symmetry
        JPanel rightPanelRounded = createRoundedPanel();
        rightPanelRounded.setLayout(new BorderLayout());
        rightPanelRounded.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Bản sao sách"),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        rightPanelRounded.setBackground(Color.WHITE);
        rightPanelRounded.setOpaque(true);
        
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rightButtonPanel.setOpaque(false);
        JButton addCopyButton = createStyledButton("Thêm bản sao", PRIMARY_BLUE, null);
        addCopyButton.addActionListener(e -> showAddCopyDialog());
        
        JButton deleteCopyButton = createStyledButton("Xóa bản sao", PRIMARY_PURPLE, null);
        deleteCopyButton.addActionListener(e -> deleteBookCopy());
        
        rightButtonPanel.add(addCopyButton);
        rightButtonPanel.add(deleteCopyButton);
        
        rightPanelRounded.add(rightButtonPanel, BorderLayout.NORTH);
        rightPanelRounded.add(new JScrollPane(bookCopiesTable), BorderLayout.CENTER);
        
        splitPane.setLeftComponent(leftPanelRounded);
        splitPane.setRightComponent(rightPanelRounded);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private void performSearch() {
        String keyword = searchKeywordField.getText().trim();
        String category = (String) categoryComboBox.getSelectedItem();
        
        if (keyword.isEmpty() && "Tất cả thể loại".equals(category)) {
            loadBooks();
            return;
        }
        
        if (!keyword.isEmpty()) {
            searchBooks(keyword);
        } else {
            loadBooks();
        }
        
        // Filter by category if needed
        if (!"Tất cả thể loại".equals(category)) {
            filterBooksByCategory(category);
        }
    }
    
    private void filterBooksByCategory(String category) {
        DefaultTableModel model = (DefaultTableModel) booksTable.getModel();
        int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String bookCategory = (String) model.getValueAt(i, 4);
            if (!category.equals(bookCategory)) {
                model.removeRow(i);
                i--;
                rowCount--;
            }
        }
    }
    
    private JPanel createReportsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(BG_LIGHT);
        
        // Report options panel - with red left border matching image
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBorder(createRedLeftBorder("Tùy chọn báo cáo"));
        optionsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        timeRangeComboBox = new JComboBox<>(new String[]{"Hôm nay", "Tuần này", "Tháng này", "Năm này", "Tất cả"});
        reportStatusLabel = new JLabel("");
        reportStatusLabel.setForeground(new Color(0, 150, 0));
        
        JButton generateButton = createStyledButton("Tạo báo cáo", new Color(52, 152, 219), null);
        generateButton.addActionListener(e -> generateReport());
        
        JButton exportButton = createStyledButton("Xuất Excel", new Color(46, 204, 113), null);
        exportButton.addActionListener(e -> exportToExcel());
        
        gbc.gridx = 0; gbc.gridy = 0;
        optionsPanel.add(new JLabel("Khoảng thời gian:"), gbc);
        gbc.gridx = 1;
        optionsPanel.add(timeRangeComboBox, gbc);
        gbc.gridx = 2;
        optionsPanel.add(generateButton, gbc);
        gbc.gridx = 3;
        optionsPanel.add(exportButton, gbc);
        
        mainPanel.add(optionsPanel, BorderLayout.NORTH);
        
        // Report sub-tabs
        reportSubTabs = new JTabbedPane();
        
        // Book report tab
        String[] bookReportColumns = {"Thể loại", "Tổng sách", "Có sẵn", "Đang mượn", "Bị mất", "Bị hỏng", "Tỷ lệ mượn (%)"};
        bookReportModel = new DefaultTableModel(bookReportColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookReportTable = new JTable(bookReportModel);
        styleTable(bookReportTable);
        JPanel bookReportPanel = new JPanel(new BorderLayout());
        bookReportPanel.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Báo cáo sách"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        bookReportPanel.setBackground(Color.WHITE);
        bookReportPanel.add(new JScrollPane(bookReportTable), BorderLayout.CENTER);
        reportSubTabs.addTab("Báo cáo sách", bookReportPanel);
        
        // User report tab
        String[] userReportColumns = {"Khoa", "Tổng người dùng", "Hoạt động", "Bị khóa", "Tổng mượn", "Tổng phạt"};
        userReportModel = new DefaultTableModel(userReportColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userReportTable = new JTable(userReportModel);
        styleTable(userReportTable);
        JPanel userReportPanel = new JPanel(new BorderLayout());
        userReportPanel.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Báo cáo người dùng"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        userReportPanel.setBackground(Color.WHITE);
        userReportPanel.add(new JScrollPane(userReportTable), BorderLayout.CENTER);
        reportSubTabs.addTab("Báo cáo người dùng", userReportPanel);
        
        // Borrow report tab
        String[] borrowReportColumns = {"Trạng thái", "Số lượng", "Tỷ lệ (%)"};
        borrowReportModel = new DefaultTableModel(borrowReportColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        borrowReportTable = new JTable(borrowReportModel);
        styleTable(borrowReportTable);
        JPanel borrowReportPanel = new JPanel(new BorderLayout());
        borrowReportPanel.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Báo cáo mượn/trả"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        borrowReportPanel.setBackground(Color.WHITE);
        borrowReportPanel.add(new JScrollPane(borrowReportTable), BorderLayout.CENTER);
        reportSubTabs.addTab("Báo cáo mượn/trả", borrowReportPanel);
        
        // Penalty report tab
        String[] penaltyReportColumns = {"Người dùng", "Sách", "Lý do", "Số tiền (VNĐ)", "Trạng thái", "Ngày tạo"};
        penaltyReportModel = new DefaultTableModel(penaltyReportColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        penaltyReportTable = new JTable(penaltyReportModel);
        styleTable(penaltyReportTable);
        JPanel penaltyReportPanel = new JPanel(new BorderLayout());
        penaltyReportPanel.setBorder(BorderFactory.createCompoundBorder(
            createStyledTitledBorder("Báo cáo phạt"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        penaltyReportPanel.setBackground(Color.WHITE);
        penaltyReportPanel.add(new JScrollPane(penaltyReportTable), BorderLayout.CENTER);
        reportSubTabs.addTab("Báo cáo phạt", penaltyReportPanel);
        
        mainPanel.add(reportSubTabs, BorderLayout.CENTER);
        
        // Status panel
        JPanel statusReportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusReportPanel.add(reportStatusLabel);
        mainPanel.add(statusReportPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private void generateReport() {
        String timeRange = (String) timeRangeComboBox.getSelectedItem();
        int selectedTab = reportSubTabs.getSelectedIndex();
        
        boolean success = false;
        switch (selectedTab) {
            case 0: // Book report
                success = generateBookReport();
                break;
            case 1: // User report
                success = generateUserReport();
                break;
            case 2: // Borrow report
                success = generateBorrowReport();
                break;
            case 3: // Penalty report
                success = generatePenaltyReport();
                break;
        }
        
        if (reportStatusLabel != null) {
            if (success) {
                reportStatusLabel.setText("Báo cáo đã được tạo thành công");
                reportStatusLabel.setForeground(new Color(0, 150, 0));
            } else {
                reportStatusLabel.setText("Lỗi khi tạo báo cáo. Vui lòng thử lại.");
                reportStatusLabel.setForeground(new Color(200, 0, 0));
            }
        }
    }
    
    private boolean generateBookReport() {
        try {
            Message request = new Message(Message.GET_BOOK_REPORT, null);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess() && response.getData() instanceof List) {
                @SuppressWarnings("unchecked")
                List<Document> reportData = (List<Document>) response.getData();
                bookReportModel.setRowCount(0);
                
                if (reportData.isEmpty()) {
                    if (reportStatusLabel != null) {
                        reportStatusLabel.setText("Không có dữ liệu báo cáo sách");
                        reportStatusLabel.setForeground(new Color(200, 0, 0));
                    }
                    return false;
                }
                
                for (Document doc : reportData) {
                    String category = doc.getString("category");
                    if (category == null) category = "N/A";
                    long totalBooks = getLongValue(doc, "totalBooks");
                    long available = getLongValue(doc, "available");
                    long borrowed = getLongValue(doc, "borrowed");
                    long lost = getLongValue(doc, "lost");
                    long damaged = getLongValue(doc, "damaged");
                    // Total copies should include all: available + borrowed + lost + damaged
                    long totalCopies = available + borrowed + lost + damaged;
                    double borrowRate = totalCopies > 0 ? (borrowed * 100.0 / totalCopies) : 0.0;
                    
                    bookReportModel.addRow(new Object[]{
                        category,
                        totalBooks,
                        available,
                        borrowed,
                        lost,
                        damaged,
                        String.format("%.1f", borrowRate)
                    });
                }
                return true;
            } else {
                if (reportStatusLabel != null) {
                    reportStatusLabel.setText("Lỗi: " + (response.getMessage() != null ? response.getMessage() : "Không thể tải dữ liệu báo cáo sách"));
                    reportStatusLabel.setForeground(new Color(200, 0, 0));
                }
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error generating book report: " + e.getMessage());
            e.printStackTrace();
            if (reportStatusLabel != null) {
                reportStatusLabel.setText("Lỗi khi tạo báo cáo sách: " + e.getMessage());
                reportStatusLabel.setForeground(new Color(200, 0, 0));
            }
            return false;
        }
    }
    
    private boolean generateUserReport() {
        try {
            Message request = new Message(Message.GET_USER_REPORT, null);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess() && response.getData() instanceof List) {
                @SuppressWarnings("unchecked")
                List<Document> reportData = (List<Document>) response.getData();
                userReportModel.setRowCount(0);
                
                if (reportData.isEmpty()) {
                    if (reportStatusLabel != null) {
                        reportStatusLabel.setText("Không có dữ liệu báo cáo người dùng");
                        reportStatusLabel.setForeground(new Color(200, 0, 0));
                    }
                    return false;
                }
                
                for (Document doc : reportData) {
                    String faculty = doc.getString("faculty");
                    long totalUsers = getLongValue(doc, "totalUsers");
                    long active = getLongValue(doc, "active");
                    long locked = getLongValue(doc, "locked");
                    long totalBorrows = getLongValue(doc, "totalBorrows");
                    double totalFines = getDoubleValue(doc, "totalFines");
                    
                    userReportModel.addRow(new Object[]{
                        faculty != null ? faculty : "N/A",
                        totalUsers,
                        active,
                        locked,
                        totalBorrows,
                        String.format("%.0f", totalFines)
                    });
                }
                return true;
            } else {
                if (reportStatusLabel != null) {
                    reportStatusLabel.setText("Lỗi: " + (response.getMessage() != null ? response.getMessage() : "Không thể tải dữ liệu báo cáo người dùng"));
                    reportStatusLabel.setForeground(new Color(200, 0, 0));
                }
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error generating user report: " + e.getMessage());
            e.printStackTrace();
            if (reportStatusLabel != null) {
                reportStatusLabel.setText("Lỗi khi tạo báo cáo người dùng: " + e.getMessage());
                reportStatusLabel.setForeground(new Color(200, 0, 0));
            }
            return false;
        }
    }
    
    private boolean generateBorrowReport() {
        try {
            Message request = new Message(Message.GET_BORROW_REPORT, null);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess() && response.getData() instanceof Document) {
                Document reportData = (Document) response.getData();
                borrowReportModel.setRowCount(0);
                
                long totalBorrows = getLongValue(reportData, "totalBorrows");
                long activeBorrows = getLongValue(reportData, "activeBorrows");
                long returnedBorrows = getLongValue(reportData, "returnedBorrows");
                long overdueBorrows = getLongValue(reportData, "overdueBorrows");
                long lostBorrows = getLongValue(reportData, "lostBorrows");
                long damagedBorrows = getLongValue(reportData, "damagedBorrows");
                
                if (totalBorrows > 0) {
                    borrowReportModel.addRow(new Object[]{
                        "Đang mượn",
                        activeBorrows,
                        String.format("%.1f", (activeBorrows * 100.0 / totalBorrows))
                    });
                    borrowReportModel.addRow(new Object[]{
                        "Đã trả",
                        returnedBorrows,
                        String.format("%.1f", (returnedBorrows * 100.0 / totalBorrows))
                    });
                    borrowReportModel.addRow(new Object[]{
                        "Quá hạn",
                        overdueBorrows,
                        String.format("%.1f", (overdueBorrows * 100.0 / totalBorrows))
                    });
                    borrowReportModel.addRow(new Object[]{
                        "Mất",
                        lostBorrows,
                        String.format("%.1f", (lostBorrows * 100.0 / totalBorrows))
                    });
                    borrowReportModel.addRow(new Object[]{
                        "Hỏng",
                        damagedBorrows,
                        String.format("%.1f", (damagedBorrows * 100.0 / totalBorrows))
                    });
                    return true;
                } else {
                    if (reportStatusLabel != null) {
                        reportStatusLabel.setText("Không có dữ liệu mượn trả");
                        reportStatusLabel.setForeground(new Color(200, 0, 0));
                    }
                    return false;
                }
            } else {
                if (reportStatusLabel != null) {
                    reportStatusLabel.setText("Lỗi: " + (response.getMessage() != null ? response.getMessage() : "Không thể tải dữ liệu báo cáo mượn trả"));
                    reportStatusLabel.setForeground(new Color(200, 0, 0));
                }
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error generating borrow report: " + e.getMessage());
            e.printStackTrace();
            if (reportStatusLabel != null) {
                reportStatusLabel.setText("Lỗi khi tạo báo cáo mượn trả: " + e.getMessage());
                reportStatusLabel.setForeground(new Color(200, 0, 0));
            }
            return false;
        }
    }
    
    private boolean generatePenaltyReport() {
        try {
            Message request = new Message(Message.GET_PENALTY_REPORT, null);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess() && response.getData() instanceof List) {
                @SuppressWarnings("unchecked")
                List<Document> reportData = (List<Document>) response.getData();
                penaltyReportModel.setRowCount(0);
                
                if (reportData.isEmpty()) {
                    if (reportStatusLabel != null) {
                        reportStatusLabel.setText("Không có dữ liệu báo cáo phạt");
                        reportStatusLabel.setForeground(new Color(200, 0, 0));
                    }
                    return false;
                }
                
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                
                for (Document doc : reportData) {
                    String username = doc.getString("username");
                    if (username == null) username = "N/A";
                    String bookTitle = doc.getString("bookTitle");
                    if (bookTitle == null) bookTitle = "N/A";
                    String reason = doc.getString("reason");
                    if (reason == null) reason = "N/A";
                    double amount = getDoubleValue(doc, "amount");
                    String status = doc.getString("status");
                    if (status == null) status = "N/A";
                    java.util.Date createdDate = doc.getDate("createdDate");
                    String dateStr = createdDate != null ? sdf.format(createdDate) : "N/A";
                    
                    penaltyReportModel.addRow(new Object[]{
                        username,
                        bookTitle,
                        reason,
                        String.format("%.0f", amount),
                        status,
                        dateStr
                    });
                }
                return true;
            } else {
                if (reportStatusLabel != null) {
                    reportStatusLabel.setText("Lỗi: " + (response.getMessage() != null ? response.getMessage() : "Không thể tải dữ liệu báo cáo phạt"));
                    reportStatusLabel.setForeground(new Color(200, 0, 0));
                }
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error generating penalty report: " + e.getMessage());
            e.printStackTrace();
            if (reportStatusLabel != null) {
                reportStatusLabel.setText("Lỗi khi tạo báo cáo phạt: " + e.getMessage());
                reportStatusLabel.setForeground(new Color(200, 0, 0));
            }
            return false;
        }
    }
    
    private double getDoubleValue(Document doc, String key) {
        Object value = doc.get(key);
        if (value == null) return 0.0;
        if (value instanceof Double) return ((Double) value).doubleValue();
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }
    
    private void exportToExcel() {
        try {
            // Get the currently selected report tab
            int selectedTab = reportSubTabs.getSelectedIndex();
            if (selectedTab == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một báo cáo để xuất!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Get table model based on selected tab
            DefaultTableModel model = null;
            String reportName = "";
            
            switch (selectedTab) {
                case 0: // Book report
                    model = bookReportModel;
                    reportName = "Báo cáo sách";
                    break;
                case 1: // User report
                    model = userReportModel;
                    reportName = "Báo cáo người dùng";
                    break;
                case 2: // Borrow report
                    model = borrowReportModel;
                    reportName = "Báo cáo mượn trả";
                    break;
                case 3: // Penalty report
                    model = penaltyReportModel;
                    reportName = "Báo cáo phạt";
                    break;
            }
            
            if (model == null || model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất! Vui lòng tạo báo cáo trước.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Show file chooser
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("Lưu file Excel");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
            
            // Set default filename
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");
            String defaultFileName = reportName + "_" + sdf.format(new java.util.Date()) + ".xlsx";
            fileChooser.setSelectedFile(new java.io.File(defaultFileName));
            
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                String filePath = file.getAbsolutePath();
                
                // Ensure .xlsx extension
                if (!filePath.toLowerCase().endsWith(".xlsx")) {
                    filePath += ".xlsx";
                }
                
                // Check if file exists and confirm overwrite
                java.io.File targetFile = new java.io.File(filePath);
                if (targetFile.exists()) {
                    int overwrite = JOptionPane.showConfirmDialog(this,
                        "File đã tồn tại. Bạn có muốn ghi đè không?",
                        "Xác nhận",
                        JOptionPane.YES_NO_OPTION);
                    if (overwrite != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                
                try {
                    // Try to export to Excel format using Apache POI
                    boolean excelSuccess = false;
                    try {
                        exportToExcelFile(model, filePath, reportName);
                        
                        // Verify file was created
                        java.io.File createdFile = new java.io.File(filePath);
                        if (createdFile.exists() && createdFile.length() > 0) {
                            excelSuccess = true;
                            JOptionPane.showMessageDialog(this, 
                                "Đã xuất báo cáo thành công!\nFile: " + filePath + "\nKích thước: " + createdFile.length() + " bytes", 
                                "Thành công", 
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            throw new Exception("File không được tạo hoặc file rỗng");
                        }
                    } catch (NoClassDefFoundError | ClassNotFoundException | NoSuchMethodError | UnsatisfiedLinkError e) {
                        // Apache POI not available or incompatible, fallback to CSV
                        System.err.println("Excel export failed (POI issue), using CSV fallback: " + e.getMessage());
                        e.printStackTrace();
                        
                        String csvPath = filePath.replace(".xlsx", ".csv");
                        exportToCSV(model, csvPath, reportName);
                        
                        java.io.File csvFile = new java.io.File(csvPath);
                        if (csvFile.exists() && csvFile.length() > 0) {
                            JOptionPane.showMessageDialog(this, 
                                "Đã xuất báo cáo thành công (CSV format)!\nFile: " + csvPath + "\nKích thước: " + csvFile.length() + " bytes\n\nLưu ý: File CSV có thể mở bằng Excel.", 
                                "Thành công", 
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            throw new Exception("CSV file không được tạo");
                        }
                    } catch (Exception e) {
                        // Other errors (IO, etc.) - try CSV fallback
                        System.err.println("Excel export failed, trying CSV fallback: " + e.getMessage());
                        e.printStackTrace();
                        
                        String csvPath = filePath.replace(".xlsx", ".csv");
                        exportToCSV(model, csvPath, reportName);
                        
                        java.io.File csvFile = new java.io.File(csvPath);
                        if (csvFile.exists() && csvFile.length() > 0) {
                            JOptionPane.showMessageDialog(this, 
                                "Đã xuất báo cáo thành công (CSV format)!\nFile: " + csvPath + "\nKích thước: " + csvFile.length() + " bytes\n\nLưu ý: File CSV có thể mở bằng Excel.", 
                                "Thành công", 
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            throw new Exception("CSV file không được tạo");
                        }
                    }
                } catch (Exception ex) {
                    String errorDetails = ex.getMessage();
                    if (ex.getCause() != null) {
                        errorDetails += "\nNguyên nhân: " + ex.getCause().getMessage();
                    }
                    System.err.println("Error exporting file: " + errorDetails);
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, 
                        "Lỗi khi xuất file:\n" + errorDetails + "\n\nLoại lỗi: " + ex.getClass().getSimpleName(), 
                        "Lỗi", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            String errorMsg = "Lỗi khi xuất file: " + e.getMessage();
            if (e.getCause() != null) {
                errorMsg += "\nNguyên nhân: " + e.getCause().getMessage();
            }
            System.err.println("Error in exportToExcel: " + errorMsg);
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                errorMsg, 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportToCSV(DefaultTableModel model, String filePath, String reportName) throws Exception {
        try (java.io.FileWriter writer = new java.io.FileWriter(filePath, java.nio.charset.StandardCharsets.UTF_8)) {
            // Write BOM for Excel UTF-8 support
            writer.write('\uFEFF');
            
            // Write report title
            writer.write(reportName + "\n");
            writer.write("Ngày xuất: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()) + "\n\n");
            
            // Write column headers
            int columnCount = model.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                writer.write(model.getColumnName(i));
                if (i < columnCount - 1) {
                    writer.write(",");
                }
            }
            writer.write("\n");
            
            // Write data rows
            int rowCount = model.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j < columnCount; j++) {
                    Object value = model.getValueAt(i, j);
                    String cellValue = "";
                    if (value != null) {
                        cellValue = value.toString();
                        // Escape commas and quotes in CSV
                        if (cellValue.contains(",") || cellValue.contains("\"") || cellValue.contains("\n")) {
                            cellValue = "\"" + cellValue.replace("\"", "\"\"") + "\"";
                        }
                    }
                    writer.write(cellValue);
                    if (j < columnCount - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");
            }
            
            writer.flush();
            System.out.println("CSV file exported successfully to: " + filePath);
        } catch (java.io.IOException e) {
            throw new Exception("Lỗi khi ghi file CSV: " + e.getMessage(), e);
        }
    }
    
    private void exportToExcelFile(DefaultTableModel model, String filePath, String reportName) throws Exception {
        // Check if Apache POI is available
        try {
            Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook");
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError("Apache POI library not found. Please ensure poi-ooxml-5.2.4.jar is in lib folder.");
        }
        
        org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = null;
        java.io.FileOutputStream fileOut = null;
        
        try {
            // Create workbook and sheet
            workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            
            // Limit sheet name to 31 characters (Excel limit)
            String sheetName = reportName.length() > 31 ? reportName.substring(0, 31) : reportName;
            // Remove invalid characters for sheet name
            sheetName = sheetName.replaceAll("[\\\\/:*?\"<>|]", "_");
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet(sheetName);
            
            // Create styles
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            
            org.apache.poi.ss.usermodel.CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);
            
            org.apache.poi.ss.usermodel.CellStyle dateStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font dateFont = workbook.createFont();
            dateFont.setFontHeightInPoints((short) 10);
            dateStyle.setFont(dateFont);
            
            org.apache.poi.ss.usermodel.CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            dataStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            dataStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            dataStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            dataStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            
            int rowNum = 0;
            
            // Write title
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(reportName);
            titleCell.setCellStyle(titleStyle);
            
            // Write export date
            org.apache.poi.ss.usermodel.Row dateRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Ngày xuất: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()));
            dateCell.setCellStyle(dateStyle);
            
            // Empty row
            rowNum++;
            
            // Write header row
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            int columnCount = model.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(model.getColumnName(i));
                cell.setCellStyle(headerStyle);
            }
            
            // Write data rows
            int rowCount = model.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                for (int j = 0; j < columnCount; j++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(j);
                    Object value = model.getValueAt(i, j);
                    
                    if (value != null) {
                        if (value instanceof Number) {
                            if (value instanceof Double || value instanceof Float) {
                                cell.setCellValue(((Number) value).doubleValue());
                            } else {
                                cell.setCellValue(((Number) value).longValue());
                            }
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    } else {
                        cell.setCellValue("");
                    }
                    cell.setCellStyle(dataStyle);
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < columnCount; i++) {
                sheet.autoSizeColumn(i);
                // Add some padding
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }
            
            // Create parent directory if it doesn't exist
            java.io.File targetFile = new java.io.File(filePath);
            java.io.File parentDir = targetFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
                System.out.println("Created directory: " + parentDir.getAbsolutePath());
            }
            
            // Create file output stream
            System.out.println("Creating file output stream for: " + filePath);
            fileOut = new java.io.FileOutputStream(filePath, false); // overwrite if exists
            
            // Write workbook to file
            System.out.println("Writing workbook to file...");
            workbook.write(fileOut);
            
            // Flush to ensure data is written
            fileOut.flush();
            System.out.println("File flushed successfully");
            
            // Close file output stream first
            fileOut.close();
            fileOut = null;
            System.out.println("File output stream closed");
            
            // Then close workbook
            workbook.close();
            workbook = null;
            System.out.println("Workbook closed");
            
            // Verify file exists and has content
            java.io.File verifyFile = new java.io.File(filePath);
            if (verifyFile.exists()) {
                long fileSize = verifyFile.length();
                System.out.println("Excel file exported successfully to: " + filePath);
                System.out.println("File size: " + fileSize + " bytes");
                if (fileSize == 0) {
                    throw new Exception("File was created but is empty (0 bytes)");
                }
            } else {
                throw new Exception("File was not created at: " + filePath);
            }
            
        } catch (Exception e) {
            // Close resources in case of error
            if (fileOut != null) {
                try {
                    fileOut.close();
                } catch (Exception ex) {
                    // Ignore
                }
            }
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception ex) {
                    // Ignore
                }
            }
            
            System.err.println("Error exporting to Excel: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Lỗi khi xuất file Excel: " + e.getMessage(), e);
        }
    }
    
    private void loadData() {
        loadDashboardStats();
        loadUsers();
        loadBooks();
        loadBorrowRecords();
        loadCategories();
        loadRecentBorrows();
        loadNewUsers();
    }
    
    private void loadRecentBorrows() {
        Message request = new Message(Message.GET_ALL_BORROW_RECORDS, null);
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<BorrowRecord> records = (List<BorrowRecord>) response.getData();
            recentBorrowsModel.setRowCount(0);
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            
            // Get only recent 10 records, sorted by borrow date (most recent first)
            records.sort((r1, r2) -> {
                if (r1.getBorrowDate() == null && r2.getBorrowDate() == null) return 0;
                if (r1.getBorrowDate() == null) return 1;
                if (r2.getBorrowDate() == null) return -1;
                return r2.getBorrowDate().compareTo(r1.getBorrowDate());
            });
            
            int count = 0;
            for (BorrowRecord record : records) {
                if (count >= 10) break;
                String borrowDate = record.getBorrowDate() != null ? sdf.format(record.getBorrowDate()) : "";
                String dueDate = record.getDueDate() != null ? sdf.format(record.getDueDate()) : "";
                String statusText = getStatusText(record.getStatus());
                
                recentBorrowsModel.addRow(new Object[]{
                    record.getRecordId() != null ? record.getRecordId() : String.valueOf(record.getId()),
                    record.getUsername(),
                    record.getBookTitle(),
                    borrowDate,
                    dueDate,
                    statusText
                });
                count++;
            }
            
            // Set column widths for better display
            setRecentBorrowsTableColumnWidths();
        }
    }
    
    private void setRecentBorrowsTableColumnWidths() {
        if (recentBorrowsTable == null) return;
        javax.swing.table.TableColumnModel columnModel = recentBorrowsTable.getColumnModel();
        
        // Calculate equal width for all columns (approximately 700px available width, 6 columns)
        int columnCount = columnModel.getColumnCount();
        if (columnCount > 0) {
            int equalWidth = 700 / columnCount; // Divide available width equally
            for (int i = 0; i < columnCount; i++) {
                columnModel.getColumn(i).setPreferredWidth(equalWidth);
            }
        }
        
        // Use AUTO_RESIZE_OFF to maintain equal column widths
        recentBorrowsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }
    
    private void loadNewUsers() {
        Message request = new Message(Message.GET_ALL_USERS, null);
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<User> users = (List<User>) response.getData();
            newUsersModel.setRowCount(0);
            
            // Get only recent 10 users, sorted by registration date (most recent first)
            users.sort((u1, u2) -> {
                if (u1.getRegistrationDate() == null && u2.getRegistrationDate() == null) return 0;
                if (u1.getRegistrationDate() == null) return 1;
                if (u2.getRegistrationDate() == null) return -1;
                return u2.getRegistrationDate().compareTo(u1.getRegistrationDate());
            });
            
            int count = 0;
            for (User user : users) {
                if (count >= 10) break;
                String status = user.getStatus() != null ? user.getStatus() : "ACTIVE";
                String statusText = "ACTIVE".equals(status) ? "Hoạt động" : "Bị khóa";
                
                newUsersModel.addRow(new Object[]{
                    user.getUserId() != null ? user.getUserId() : "",
                    user.getFullName(),
                    user.getEmail() != null ? user.getEmail() : "",
                    user.getStudentId() != null ? user.getStudentId() : "N/A",
                    user.getFaculty() != null ? user.getFaculty() : "",
                    statusText
                });
                count++;
            }
            
            // Set column widths for better display
            setNewUsersTableColumnWidths();
        }
    }
    
    private void setNewUsersTableColumnWidths() {
        if (newUsersTable == null) return;
        javax.swing.table.TableColumnModel columnModel = newUsersTable.getColumnModel();
        
        // Calculate equal width for all columns (approximately 700px available width, 6 columns)
        int columnCount = columnModel.getColumnCount();
        if (columnCount > 0) {
            int equalWidth = 700 / columnCount; // Divide available width equally
            for (int i = 0; i < columnCount; i++) {
                columnModel.getColumn(i).setPreferredWidth(equalWidth);
            }
        }
        
        // Use AUTO_RESIZE_OFF to maintain equal column widths
        newUsersTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }
    
    private void loadCategories() {
        // Load categories for dropdown - can be enhanced with actual category loading
    }
    
    private void loadDashboardStats() {
        Message request = new Message(Message.GET_DASHBOARD_STATS, null);
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof Document) {
            Document stats = (Document) response.getData();
            // Update stat labels according to new order: Tổng số sách, Tổng số người dùng, Tổng số lượt mượn, 
            // Sách có sẵn, Sách đang mượn, Tổng số phạt, Sách quá hạn, Người dùng chờ duyệt
            if (statLabels[0] != null) statLabels[0].setText(String.valueOf(getLongValue(stats, "totalBooks")));
            if (statLabels[1] != null) statLabels[1].setText(String.valueOf(getLongValue(stats, "totalUsers")));
            if (statLabels[2] != null) statLabels[2].setText(String.valueOf(getLongValue(stats, "totalBorrows")));
            if (statLabels[3] != null) {
                // Sách có sẵn - use availableCopies from stats
                statLabels[3].setText(String.valueOf(getLongValue(stats, "availableCopies")));
            }
            if (statLabels[4] != null) statLabels[4].setText(String.valueOf(getLongValue(stats, "activeBorrows")));
            if (statLabels[5] != null) {
                Object finesObj = stats.get("totalFinesAmount");
                double totalFines = 0.0;
                if (finesObj instanceof Number) {
                    totalFines = ((Number) finesObj).doubleValue();
                }
                statLabels[5].setText(String.format("%.0f VND", totalFines));
            }
            if (statLabels[6] != null) statLabels[6].setText(String.valueOf(getLongValue(stats, "overdueBorrows")));
            if (statLabels[7] != null) {
                // Người dùng chờ duyệt - count users with status "LOCKED" (pending approval)
                statLabels[7].setText(String.valueOf(getLongValue(stats, "pendingUsers")));
            }
            
            // Refresh dashboard tables
            loadRecentBorrows();
            loadNewUsers();
        }
    }
    
    private long getLongValue(Document doc, String key) {
        Object value = doc.get(key);
        if (value == null) return 0;
        if (value instanceof Long) return ((Long) value).longValue();
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return 0;
    }
    
    private void loadUsers() {
        Message request = new Message(Message.GET_ALL_USERS, null);
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<User> users = (List<User>) response.getData();
            usersModel.setRowCount(0);
            
            for (User user : users) {
                String registrationDate = "N/A";
                if (user.getRegistrationDate() != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                    registrationDate = sdf.format(user.getRegistrationDate());
                }
                
                usersModel.addRow(new Object[]{
                    user.getUserId() != null ? user.getUserId() : "",
                    user.getEmail() != null ? user.getEmail() : "",
                    user.getFullName(),
                    user.getStudentId() != null ? user.getStudentId() : "N/A",
                    user.getFaculty() != null ? user.getFaculty() : "",
                    user.getYearOfStudy() != null ? user.getYearOfStudy() : "",
                    user.getRole() != null ? user.getRole() : "USER",
                    user.getStatus() != null ? user.getStatus() : "ACTIVE",
                    user.getCurrentBorrowed(),
                    String.format("%.0f VNĐ", user.getTotalFines()),
                    registrationDate
                });
            }
            
            // Update total users label
            if (totalUsersLabel != null) {
                totalUsersLabel.setText("Tổng: " + users.size() + " người dùng");
            }
            
            // Set column widths for better display
            setUsersTableColumnWidths();
        }
    }
    
    private void setUsersTableColumnWidths() {
        if (usersTable == null) return;
        
        javax.swing.table.TableColumnModel columnModel = usersTable.getColumnModel();
        int columnCount = columnModel.getColumnCount();
        
        if (columnCount == 0) return;
        
        // Get the table's parent width to calculate equal column widths
        java.awt.Container parent = usersTable.getParent();
        int availableWidth = 0;
        
        if (parent != null) {
            availableWidth = parent.getWidth();
            // Account for scrollbar and padding
            availableWidth = availableWidth - 30; // Approximate scrollbar width
        }
        
        // If we can't get parent width, use a default calculation
        if (availableWidth <= 0) {
            // Use frame width as fallback (approximately 1400px - margins)
            availableWidth = 1300;
        }
        
        // Calculate equal width for each column
        int equalWidth = availableWidth / columnCount;
        
        // Ensure minimum width per column
        if (equalWidth < 100) {
            equalWidth = 100;
        }
        
        // Set equal width for all columns
        for (int i = 0; i < columnCount; i++) {
            columnModel.getColumn(i).setPreferredWidth(equalWidth);
        }
        
        // Use AUTO_RESIZE_ALL_COLUMNS to evenly distribute space
        usersTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }
    
    private void lockUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần khóa!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Object userIdObj = usersModel.getValueAt(selectedRow, 0);
        String userId = userIdObj != null ? userIdObj.toString() : null;
        if (userId == null || userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy ID người dùng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String currentUserId = currentUser.getUserId();
        if (currentUserId != null && currentUserId.equals(userId)) {
            JOptionPane.showMessageDialog(this, "Bạn không thể khóa chính mình!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn khóa tài khoản này?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Message request = new Message(Message.LOCK_USER, userId);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void unlockUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần mở khóa!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Object userIdObj = usersModel.getValueAt(selectedRow, 0);
        String userId = userIdObj != null ? userIdObj.toString() : null;
        if (userId == null || userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy ID người dùng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn mở khóa tài khoản này?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Message request = new Message(Message.UNLOCK_USER, userId);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void resetPassword() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần đặt lại mật khẩu!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Object userIdObj = usersModel.getValueAt(selectedRow, 0);
        String userId = userIdObj != null ? userIdObj.toString() : null;
        if (userId == null || userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy ID người dùng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String newPassword = JOptionPane.showInputDialog(this, "Nhập mật khẩu mới:", "Đặt lại mật khẩu", JOptionPane.QUESTION_MESSAGE);
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            Message request = new Message(Message.RESET_PASSWORD, new Object[]{userId, newPassword.trim()});
            Message response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void loadBooks() {
        Message request = new Message(Message.GET_ALL_BOOKS, null);
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<Book> books = (List<Book>) response.getData();
            booksModel.setRowCount(0);
            for (Book book : books) {
                String status = book.getAvailableCopies() > 0 ? "Có sẵn" : "Hết sách";
                booksModel.addRow(new Object[]{
                    book.getBookId() != null ? book.getBookId() : String.valueOf(book.getId()),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getIsbn() != null ? book.getIsbn() : "",
                    book.getCategory(),
                    book.getPublishYear(),
                    String.format("%.0f VNĐ", book.getPrice()),
                    book.getTotalCopies(),
                    book.getAvailableCopies(),
                    status
                });
            }
        }
    }
    
    private void loadBookCopies() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            bookCopiesModel.setRowCount(0);
            return;
        }
        
        Object bookIdObj = booksModel.getValueAt(selectedRow, 0);
        String bookId = bookIdObj != null ? bookIdObj.toString() : null;
        if (bookId == null) {
            bookCopiesModel.setRowCount(0);
            return;
        }
        
        Message request = new Message(Message.GET_BOOK_COPIES, bookId);
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<BookCopy> copies = (List<BookCopy>) response.getData();
            bookCopiesModel.setRowCount(0);
            for (BookCopy copy : copies) {
                String statusText = "Có sẵn";
                if ("BORROWED".equals(copy.getStatus())) statusText = "Đang mượn";
                else if ("LOST".equals(copy.getStatus())) statusText = "Mất";
                else if ("DAMAGED".equals(copy.getStatus())) statusText = "Hỏng";
                
                bookCopiesModel.addRow(new Object[]{
                    copy.getCopyId(),
                    copy.getBookId(),
                    statusText,
                    copy.getLocation() != null ? copy.getLocation() : "",
                    copy.getNotes() != null ? copy.getNotes() : ""
                });
            }
        }
    }
    
    private void loadBorrowRecords() {
        Message request = new Message(Message.GET_ALL_BORROW_RECORDS, null);
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<BorrowRecord> records = (List<BorrowRecord>) response.getData();
            borrowModel.setRowCount(0);
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            
            for (BorrowRecord record : records) {
                String borrowDate = record.getBorrowDate() != null ? sdf.format(record.getBorrowDate()) : "";
                String dueDate = record.getDueDate() != null ? sdf.format(record.getDueDate()) : "";
                String returnDate = record.getReturnDate() != null ? sdf.format(record.getReturnDate()) : "";
                
                String statusText = getStatusText(record.getStatus());
                
                borrowModel.addRow(new Object[]{
                    record.getRecordId() != null ? record.getRecordId() : String.valueOf(record.getId()),
                    record.getUsername(),
                    record.getBookTitle(),
                    borrowDate,
                    dueDate,
                    returnDate,
                    statusText,
                    String.format("%.0f", record.getFine())
                });
            }
            
            // Update records count label
            if (recordsCountLabel != null) {
                recordsCountLabel.setText("Đã tải " + records.size() + " bản ghi");
            }
        }
    }
    
    private void searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadBooks();
            return;
        }
        
        Message request = new Message(Message.SEARCH_BOOKS, keyword.trim());
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<Book> books = (List<Book>) response.getData();
            booksModel.setRowCount(0);
            for (Book book : books) {
                String status = book.getAvailableCopies() > 0 ? "Có sẵn" : "Hết sách";
                booksModel.addRow(new Object[]{
                    book.getBookId() != null ? book.getBookId() : String.valueOf(book.getId()),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getIsbn() != null ? book.getIsbn() : "",
                    book.getCategory(),
                    book.getPublishYear(),
                    String.format("%.0f VNĐ", book.getPrice()),
                    book.getTotalCopies(),
                    book.getAvailableCopies(),
                    status
                });
            }
        }
    }
    
    // Book dialog methods
    private void showAddBookDialog() {
        showBookDialog(null);
    }
    
    private void showEditBookDialog() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sách cần sửa!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Object bookIdObj = booksModel.getValueAt(selectedRow, 0);
        String bookId = bookIdObj != null ? bookIdObj.toString() : null;
        
        // Load full book data
        Message request = new Message(Message.GET_BOOK_BY_ID, bookId);
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof Book) {
            Book book = (Book) response.getData();
            showBookDialog(book);
        } else {
            JOptionPane.showMessageDialog(this, "Không thể tải thông tin sách!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showBookDialog(Book book) {
        JDialog dialog = new JDialog(this, book == null ? "Thêm sách mới" : "Sửa thông tin sách", true);
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG_LIGHT);
        
        // Main panel with rounded border and card style
        JPanel mainPanel = createRoundedPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        mainPanel.setBackground(Color.WHITE);
        
        // Title header
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel(book == null ? "➕ Thêm sách mới" : "✏️ Sửa thông tin sách");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Form panel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Create styled components
        JTextField titleField = createRoundedTextField(28);
        JTextField authorField = createRoundedTextField(28);
        JTextField isbnField = createRoundedTextField(28);
        JComboBox<String> categoryCombo = createRoundedComboBox(new String[]{
            "Công nghệ thông tin", "Kinh tế", "Y học", "Kỹ thuật", 
            "Văn học", "Lịch sử", "Tâm lý học"
        });
        
        // Style spinners
        JSpinner publishYearSpinner = new JSpinner(new SpinnerNumberModel(2024, 1900, 2100, 1));
        styleSpinner(publishYearSpinner);
        JSpinner priceSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10000000.0, 1000.0));
        styleSpinner(priceSpinner);
        JSpinner pagesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
        styleSpinner(pagesSpinner);
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        styleSpinner(quantitySpinner);
        
        // Style text area
        JTextArea descriptionArea = new JTextArea(5, 28);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        descriptionArea.setBackground(Color.WHITE);
        descriptionArea.setOpaque(true);
        
        if (book != null) {
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            isbnField.setText(book.getIsbn() != null ? book.getIsbn() : "");
            categoryCombo.setSelectedItem(book.getCategory());
            publishYearSpinner.setValue(book.getPublishYear());
            priceSpinner.setValue(book.getPrice());
            pagesSpinner.setValue(book.getPages());
            quantitySpinner.setValue(book.getTotalCopies());
            descriptionArea.setText(book.getDescription() != null ? book.getDescription() : "");
        }
        
        // Add form fields with styled labels
        int y = 0;
        addFormField(panel, "Tên sách *:", titleField, gbc, y++);
        addFormField(panel, "Tác giả *:", authorField, gbc, y++);
        addFormField(panel, "ISBN:", isbnField, gbc, y++);
        addFormField(panel, "Thể loại *:", categoryCombo, gbc, y++);
        addFormField(panel, "Năm xuất bản:", publishYearSpinner, gbc, y++);
        addFormField(panel, "Giá (VNĐ):", priceSpinner, gbc, y++);
        addFormField(panel, "Số trang:", pagesSpinner, gbc, y++);
        addFormField(panel, "Tổng số bản sao:", quantitySpinner, gbc, y++);
        
        // Description field
        JLabel descLabel = new JLabel("Mô tả:");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        descLabel.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0; gbc.gridy = y;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(descLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(descScroll, gbc);
        gbc.weighty = 0;
        y++;
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton saveButton = createStyledButton("Lưu", PRIMARY_GREEN, null);
        saveButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String isbn = isbnField.getText().trim();
            String category = (String) categoryCombo.getSelectedItem();
            
            if (title.isEmpty() || author.isEmpty() || category == null) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ thông tin bắt buộc (*)!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Book newBook = new Book();
            if (book != null) {
                newBook.setBookId(book.getBookId());
                // When updating, calculate availableCopies based on current borrowed copies
                int totalCopies = (Integer) quantitySpinner.getValue();
                int currentBorrowed = book.getTotalCopies() - book.getAvailableCopies();
                int newAvailable = Math.max(0, totalCopies - currentBorrowed);
                newBook.setTotalCopies(totalCopies);
                newBook.setAvailableCopies(newAvailable);
            } else {
                // When adding new book, all copies are available
                int totalCopies = (Integer) quantitySpinner.getValue();
                newBook.setTotalCopies(totalCopies);
                newBook.setAvailableCopies(totalCopies);
            }
            newBook.setTitle(title);
            newBook.setAuthor(author);
            newBook.setIsbn(isbn);
            newBook.setCategory(category);
            newBook.setPublishYear((Integer) publishYearSpinner.getValue());
            newBook.setPrice(((Number) priceSpinner.getValue()).doubleValue());
            newBook.setPages((Integer) pagesSpinner.getValue());
            newBook.setDescription(descriptionArea.getText().trim());
            
            Message request = new Message(book == null ? Message.ADD_BOOK : Message.UPDATE_BOOK, newBook);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(dialog, response.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadBooks();
                loadBookCopies();
                loadDashboardStats();
            } else {
                JOptionPane.showMessageDialog(dialog, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton cancelButton = createStyledButton("Hủy", PRIMARY_GRAY, null);
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        panel.add(buttonPanel, gbc(0, y, 2, 1));
        
        mainPanel.add(panel, BorderLayout.CENTER);
        
        // Wrap main panel in scroll pane if needed
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }
    
    // Helper method to style JSpinner
    private void styleSpinner(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            textField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            textField.setBackground(Color.WHITE);
            textField.setOpaque(true);
        }
    }
    
    // Helper method to add form field with styled label
    private void addFormField(JPanel panel, String labelText, JComponent component, GridBagConstraints gbc, int y) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(label, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(component, gbc);
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
    
    private void deleteBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sách cần xóa!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Object bookIdObj = booksModel.getValueAt(selectedRow, 0);
        String bookId = bookIdObj != null ? bookIdObj.toString() : null;
        
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa sách này?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Message request = new Message(Message.DELETE_BOOK, bookId);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadBooks();
                loadBookCopies();
                loadDashboardStats();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showAddCopyDialog() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sách để thêm bản sao!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Object bookIdObj = booksModel.getValueAt(selectedRow, 0);
        String bookId = bookIdObj != null ? bookIdObj.toString() : null;
        
        JDialog dialog = new JDialog(this, "Thêm bản sao", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG_LIGHT);
        
        // Main panel with rounded border
        JPanel mainPanel = createRoundedPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        mainPanel.setBackground(Color.WHITE);
        
        // Title header
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("➕ Thêm bản sao sách");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Form panel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        
        JTextField locationField = createRoundedTextField(25);
        
        // Style text area
        JTextArea notesArea = new JTextArea(5, 25);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        notesArea.setBackground(Color.WHITE);
        notesArea.setOpaque(true);
        
        int y = 0;
        addFormField(panel, "Vị trí:", locationField, gbc, y++);
        
        JLabel notesLabel = new JLabel("Ghi chú:");
        notesLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        notesLabel.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0; gbc.gridy = y;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(notesLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(notesScroll, gbc);
        y++;
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton saveButton = createStyledButton("Thêm", PRIMARY_GREEN, null);
        saveButton.addActionListener(e -> {
            BookCopy copy = new BookCopy();
            copy.setBookId(bookId);
            copy.setStatus("AVAILABLE");
            copy.setLocation(locationField.getText().trim());
            copy.setNotes(notesArea.getText().trim());
            
            Message request = new Message(Message.ADD_BOOK_COPY, copy);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(dialog, response.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadBookCopies();
                loadBooks();
                loadDashboardStats();
            } else {
                JOptionPane.showMessageDialog(dialog, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton cancelButton = createStyledButton("Hủy", PRIMARY_GRAY, null);
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        panel.add(buttonPanel, gbc(0, y, 2, 1));
        
        mainPanel.add(panel, BorderLayout.CENTER);
        
        // Wrap main panel in scroll pane if needed
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }
    
    private void deleteBookCopy() {
        int selectedRow = bookCopiesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản sao cần xóa!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Object copyIdObj = bookCopiesModel.getValueAt(selectedRow, 0);
        String copyId = copyIdObj != null ? copyIdObj.toString() : null;
        
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa bản sao này?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Message request = new Message(Message.DELETE_BOOK_COPY, copyId);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadBookCopies();
                loadBooks();
                loadDashboardStats();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // User dialog methods
    private void showAddUserDialog() {
        showUserDialog(null);
    }
    
    private void showEditUserDialog() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần sửa!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Object userIdObj = usersModel.getValueAt(selectedRow, 0);
        String userId = userIdObj != null ? userIdObj.toString() : null;
        if (userId == null || userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy ID người dùng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Load full user data
        Message request = new Message(Message.GET_USER_BY_ID, userId);
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof User) {
            User user = (User) response.getData();
            showUserDialog(user);
        } else {
            JOptionPane.showMessageDialog(this, "Không thể tải thông tin người dùng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showUserDialog(User user) {
        JDialog dialog = new JDialog(this, user == null ? "Thêm người dùng mới" : "Sửa thông tin người dùng", true);
        dialog.setSize(550, 700);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG_LIGHT);
        
        // Main panel with rounded border
        JPanel mainPanel = createRoundedPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        mainPanel.setBackground(Color.WHITE);
        
        // Title header
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel(user == null ? "👤 Thêm người dùng mới" : "✏️ Sửa thông tin người dùng");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Form panel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Create styled components
        JTextField emailField = createRoundedTextField(28);
        JPasswordField passwordField = new JPasswordField(28);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordField.setBackground(Color.WHITE);
        passwordField.setOpaque(true);
        JTextField firstNameField = createRoundedTextField(28);
        JTextField lastNameField = createRoundedTextField(28);
        JTextField phoneField = createRoundedTextField(28);
        JTextField addressField = createRoundedTextField(28);
        JTextField studentIdField = createRoundedTextField(28);
        JTextField facultyField = createRoundedTextField(28);
        JTextField yearOfStudyField = createRoundedTextField(28);
        JComboBox<String> roleComboBox = createRoundedComboBox(new String[]{"USER", "ADMIN"});
        JComboBox<String> statusComboBox = createRoundedComboBox(new String[]{"ACTIVE", "LOCKED"});
        
        if (user != null) {
            emailField.setText(user.getEmail());
            emailField.setEditable(false);
            emailField.setBackground(new Color(240, 240, 240));
            firstNameField.setText(user.getFirstName());
            lastNameField.setText(user.getLastName());
            phoneField.setText(user.getPhone());
            addressField.setText(user.getAddress() != null ? user.getAddress() : "");
            studentIdField.setText(user.getStudentId() != null ? user.getStudentId() : "");
            facultyField.setText(user.getFaculty() != null ? user.getFaculty() : "");
            yearOfStudyField.setText(user.getYearOfStudy() != null ? user.getYearOfStudy() : "");
            roleComboBox.setSelectedItem(user.getRole());
            statusComboBox.setSelectedItem(user.getStatus());
        }
        
        // Add form fields with styled labels
        int y = 0;
        addFormField(panel, "Email *:", emailField, gbc, y++);
        if (user == null) {
            JLabel passLabel = new JLabel("Mật khẩu *:");
            passLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            passLabel.setForeground(new Color(44, 62, 80));
            gbc.gridx = 0; gbc.gridy = y;
            panel.add(passLabel, gbc);
            gbc.gridx = 1;
            panel.add(passwordField, gbc);
            y++;
        }
        addFormField(panel, "Họ *:", lastNameField, gbc, y++);
        addFormField(panel, "Tên *:", firstNameField, gbc, y++);
        addFormField(panel, "Điện thoại:", phoneField, gbc, y++);
        addFormField(panel, "Địa chỉ:", addressField, gbc, y++);
        addFormField(panel, "Mã sinh viên:", studentIdField, gbc, y++);
        addFormField(panel, "Khoa:", facultyField, gbc, y++);
        addFormField(panel, "Năm học:", yearOfStudyField, gbc, y++);
        addFormField(panel, "Vai trò:", roleComboBox, gbc, y++);
        addFormField(panel, "Trạng thái:", statusComboBox, gbc, y++);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton saveButton = createStyledButton("Lưu", PRIMARY_GREEN, null);
        saveButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            
            if (email.isEmpty() || (user == null && password.isEmpty()) || firstName.isEmpty() || lastName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ thông tin!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            User newUser = new User();
            if (user != null) {
                newUser.setUserId(user.getUserId());
                newUser.setPassword("unchanged");
            } else {
                newUser.setPassword(password);
            }
            newUser.setEmail(email);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setPhone(phoneField.getText().trim());
            newUser.setAddress(addressField.getText().trim());
            newUser.setStudentId(studentIdField.getText().trim());
            newUser.setFaculty(facultyField.getText().trim());
            newUser.setYearOfStudy(yearOfStudyField.getText().trim());
            newUser.setRole((String) roleComboBox.getSelectedItem());
            newUser.setStatus((String) statusComboBox.getSelectedItem());
            
            Message request = new Message(user == null ? Message.ADD_USER : Message.UPDATE_USER, newUser);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(dialog, response.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadUsers();
                loadDashboardStats();
            } else {
                JOptionPane.showMessageDialog(dialog, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton cancelButton = createStyledButton("Hủy", PRIMARY_GRAY, null);
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        panel.add(buttonPanel, gbc(0, y, 2, 1));
        
        mainPanel.add(panel, BorderLayout.CENTER);
        
        // Wrap main panel in scroll pane if needed
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }
    
    private void deleteUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần xóa!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Object userIdObj = usersModel.getValueAt(selectedRow, 0);
        String userId = userIdObj != null ? userIdObj.toString() : null;
        if (userId == null || userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy ID người dùng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String currentUserId = currentUser.getUserId();
        if (currentUserId != null && currentUserId.equals(userId)) {
            JOptionPane.showMessageDialog(this, "Bạn không thể xóa chính mình!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa người dùng này?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Message request = new Message(Message.DELETE_USER, userId);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
                loadDashboardStats();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có muốn đăng xuất?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Message request = new Message(Message.LOGOUT, null);
            client.sendRequest(request);
            client.disconnect();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
    
    private void showSettingsDialog() {
        // Load current settings
        Message request = new Message(Message.GET_SETTINGS, null);
        Message response = client.sendRequest(request);
        
        if (!response.isSuccess() || response.getData() == null) {
            JOptionPane.showMessageDialog(this, "Không thể tải cài đặt hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        org.bson.Document settings = (org.bson.Document) response.getData();
        
        // Create settings dialog
        JDialog dialog = new JDialog(this, "⚙️ Cài đặt hệ thống", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Tabbed pane for different setting categories
        JTabbedPane settingsTabs = new JTabbedPane();
        settingsTabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Tab 1: Quy tắc mượn sách
        JPanel borrowRulesPanel = createBorrowRulesPanel(settings);
        settingsTabs.addTab("📚 Quy tắc mượn sách", borrowRulesPanel);
        
        // Tab 2: Cài đặt phạt
        JPanel finesPanel = createFinesPanel(settings);
        settingsTabs.addTab("💰 Cài đặt phạt", finesPanel);
        
        // Tab 3: Cài đặt hệ thống
        JPanel systemPanel = createSystemPanel(settings);
        settingsTabs.addTab("⚙️ Cài đặt hệ thống", systemPanel);
        
        dialog.add(settingsTabs, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton saveButton = createStyledButton("Lưu", PRIMARY_GREEN, null);
        saveButton.addActionListener(e -> {
            saveSettings(settings, dialog, settingsTabs);
        });
        
        JButton cancelButton = createStyledButton("Hủy", PRIMARY_GRAY, null);
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private JPanel createBorrowRulesPanel(org.bson.Document settings) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Max borrow days
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel label1 = new JLabel("Số ngày mượn tối đa:");
        label1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(label1, gbc);
        
        gbc.gridx = 1;
        JSpinner maxBorrowDaysSpinner = new JSpinner(new SpinnerNumberModel(
            settings.getInteger("maxBorrowDays", 14), 1, 90, 1));
        maxBorrowDaysSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        maxBorrowDaysSpinner.setPreferredSize(new Dimension(150, 30));
        panel.add(maxBorrowDaysSpinner, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("ngày"), gbc);
        
        // Max borrow books
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel label2 = new JLabel("Số sách mượn tối đa:");
        label2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(label2, gbc);
        
        gbc.gridx = 1;
        JSpinner maxBorrowBooksSpinner = new JSpinner(new SpinnerNumberModel(
            settings.getInteger("maxBorrowBooks", 5), 1, 20, 1));
        maxBorrowBooksSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        maxBorrowBooksSpinner.setPreferredSize(new Dimension(150, 30));
        panel.add(maxBorrowBooksSpinner, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("quyển"), gbc);
        
        // Renewal days
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel label3 = new JLabel("Số ngày gia hạn:");
        label3.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(label3, gbc);
        
        gbc.gridx = 1;
        JSpinner renewalDaysSpinner = new JSpinner(new SpinnerNumberModel(
            settings.getInteger("renewalDays", 7), 1, 30, 1));
        renewalDaysSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        renewalDaysSpinner.setPreferredSize(new Dimension(150, 30));
        panel.add(renewalDaysSpinner, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("ngày"), gbc);
        
        // Store references for saving
        panel.putClientProperty("maxBorrowDays", maxBorrowDaysSpinner);
        panel.putClientProperty("maxBorrowBooks", maxBorrowBooksSpinner);
        panel.putClientProperty("renewalDays", renewalDaysSpinner);
        
        return panel;
    }
    
    private JPanel createFinesPanel(org.bson.Document settings) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Overdue fine per day
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel label1 = new JLabel("Phí phạt mỗi ngày quá hạn:");
        label1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(label1, gbc);
        
        gbc.gridx = 1;
        Object overdueFineObj = settings.get("overdueFinePerDay");
        double overdueFine = overdueFineObj != null ? ((Number) overdueFineObj).doubleValue() : 5000.0;
        JSpinner overdueFineSpinner = new JSpinner(new SpinnerNumberModel(
            overdueFine, 0, 100000, 1000));
        overdueFineSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        overdueFineSpinner.setPreferredSize(new Dimension(150, 30));
        panel.add(overdueFineSpinner, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("VNĐ"), gbc);
        
        // Lost book fine
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel label2 = new JLabel("Phí mất sách:");
        label2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(label2, gbc);
        
        gbc.gridx = 1;
        Object lostBookFineObj = settings.get("lostBookFine");
        double lostBookFine = lostBookFineObj != null ? ((Number) lostBookFineObj).doubleValue() : 100000.0;
        JSpinner lostBookFineSpinner = new JSpinner(new SpinnerNumberModel(
            lostBookFine, 0, 1000000, 10000));
        lostBookFineSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lostBookFineSpinner.setPreferredSize(new Dimension(150, 30));
        panel.add(lostBookFineSpinner, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("VNĐ"), gbc);
        
        // Damaged book fine
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel label3 = new JLabel("Phí hư hỏng sách:");
        label3.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(label3, gbc);
        
        gbc.gridx = 1;
        Object damagedBookFineObj = settings.get("damagedBookFine");
        double damagedBookFine = damagedBookFineObj != null ? ((Number) damagedBookFineObj).doubleValue() : 50000.0;
        JSpinner damagedBookFineSpinner = new JSpinner(new SpinnerNumberModel(
            damagedBookFine, 0, 500000, 5000));
        damagedBookFineSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        damagedBookFineSpinner.setPreferredSize(new Dimension(150, 30));
        panel.add(damagedBookFineSpinner, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("VNĐ"), gbc);
        
        // Store references for saving
        panel.putClientProperty("overdueFinePerDay", overdueFineSpinner);
        panel.putClientProperty("lostBookFine", lostBookFineSpinner);
        panel.putClientProperty("damagedBookFine", damagedBookFineSpinner);
        
        return panel;
    }
    
    private JPanel createSystemPanel(org.bson.Document settings) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Auto check overdue
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel label1 = new JLabel("Tự động kiểm tra sách quá hạn:");
        label1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(label1, gbc);
        
        gbc.gridx = 1;
        JCheckBox autoCheckBox = new JCheckBox();
        autoCheckBox.setSelected(settings.getBoolean("autoCheckOverdue", true));
        autoCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(autoCheckBox, gbc);
        
        // Reminder days before
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel label2 = new JLabel("Số ngày nhắc nhở trước hạn:");
        label2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(label2, gbc);
        
        gbc.gridx = 1;
        JSpinner reminderDaysSpinner = new JSpinner(new SpinnerNumberModel(
            settings.getInteger("reminderDaysBefore", 2), 0, 7, 1));
        reminderDaysSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reminderDaysSpinner.setPreferredSize(new Dimension(150, 30));
        panel.add(reminderDaysSpinner, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("ngày"), gbc);
        
        // Store references for saving
        panel.putClientProperty("autoCheckOverdue", autoCheckBox);
        panel.putClientProperty("reminderDaysBefore", reminderDaysSpinner);
        
        return panel;
    }
    
    private void saveSettings(org.bson.Document currentSettings, JDialog dialog, JTabbedPane settingsTabs) {
        try {
            // Get borrow rules panel
            JPanel borrowRulesPanel = (JPanel) settingsTabs.getComponentAt(0);
            JSpinner maxBorrowDaysSpinner = (JSpinner) borrowRulesPanel.getClientProperty("maxBorrowDays");
            JSpinner maxBorrowBooksSpinner = (JSpinner) borrowRulesPanel.getClientProperty("maxBorrowBooks");
            JSpinner renewalDaysSpinner = (JSpinner) borrowRulesPanel.getClientProperty("renewalDays");
            
            // Get fines panel
            JPanel finesPanel = (JPanel) settingsTabs.getComponentAt(1);
            JSpinner overdueFineSpinner = (JSpinner) finesPanel.getClientProperty("overdueFinePerDay");
            JSpinner lostBookFineSpinner = (JSpinner) finesPanel.getClientProperty("lostBookFine");
            JSpinner damagedBookFineSpinner = (JSpinner) finesPanel.getClientProperty("damagedBookFine");
            
            // Get system panel
            JPanel systemPanel = (JPanel) settingsTabs.getComponentAt(2);
            JCheckBox autoCheckBox = (JCheckBox) systemPanel.getClientProperty("autoCheckOverdue");
            JSpinner reminderDaysSpinner = (JSpinner) systemPanel.getClientProperty("reminderDaysBefore");
            
            // Create updated settings document
            org.bson.Document updatedSettings = new org.bson.Document()
                .append("maxBorrowDays", ((Number) maxBorrowDaysSpinner.getValue()).intValue())
                .append("maxBorrowBooks", ((Number) maxBorrowBooksSpinner.getValue()).intValue())
                .append("renewalDays", ((Number) renewalDaysSpinner.getValue()).intValue())
                .append("overdueFinePerDay", ((Number) overdueFineSpinner.getValue()).doubleValue())
                .append("lostBookFine", ((Number) lostBookFineSpinner.getValue()).doubleValue())
                .append("damagedBookFine", ((Number) damagedBookFineSpinner.getValue()).doubleValue())
                .append("autoCheckOverdue", autoCheckBox.isSelected())
                .append("reminderDaysBefore", ((Number) reminderDaysSpinner.getValue()).intValue());
            
            // Send update request
            Message request = new Message(Message.UPDATE_SETTINGS, updatedSettings);
            Message response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(dialog, "Cập nhật cài đặt thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "Lỗi khi lưu cài đặt: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
