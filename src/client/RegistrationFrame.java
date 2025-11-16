package client;

import model.User;
import util.Message;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Date;

// Custom rounded border class
class RoundedBorderReg implements Border {
    private int radius;
    private Color color;
    
    RoundedBorderReg(int radius, Color color) {
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

public class RegistrationFrame extends JFrame {
    private Client client;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField phoneField;
    private JTextField studentIdField;
    private JComboBox<String> facultyCombo;
    private JComboBox<String> yearCombo;
    private JTextArea addressArea;
    private JButton registerButton;
    private JButton cancelButton;
    
    // Color scheme based on image
    private final Color PRIMARY_PURPLE = new Color(138, 43, 226); // Purple
    private final Color PURPLE_HOVER = new Color(153, 50, 204);
    private final Color PRIMARY_BLUE = new Color(66, 133, 244); // Bright blue for left border
    private final Color BG_COLOR = Color.WHITE;
    private final Color CARD_BG = Color.WHITE;
    private final Color BORDER_COLOR = new Color(220, 220, 220);
    private final Color TEXT_COLOR = new Color(28, 28, 30);
    
    public RegistrationFrame(Client client) {
        this.client = client;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setTitle("Đăng ký tài khoản");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 650);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Create styled text fields
        emailField = createStyledTextField();
        passwordField = createStyledPasswordField();
        confirmPasswordField = createStyledPasswordField();
        firstNameField = createStyledTextField();
        lastNameField = createStyledTextField();
        phoneField = createStyledTextField();
        studentIdField = createStyledTextField();
        
        // Create styled combo boxes
        facultyCombo = new JComboBox<>(new String[]{
            "Công nghệ thông tin", "Kinh tế", "Y học", "Kỹ thuật", 
            "Văn học", "Lịch sử", "Tâm lý học"
        });
        facultyCombo.setSelectedItem("Công nghệ thông tin");
        styleComboBox(facultyCombo);
        
        yearCombo = new JComboBox<>(new String[]{"2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030"});
        yearCombo.setSelectedItem("2021");
        styleComboBox(yearCombo);
        
        // Create styled text area with rounded border
        addressArea = new JTextArea(4, 30);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addressArea.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorderReg(10, BORDER_COLOR),
            new EmptyBorder(10, 15, 10, 15)
        ));
        addressArea.setBackground(Color.WHITE);
        addressArea.setForeground(new Color(28, 28, 30));
        addressArea.setOpaque(true);
        
        registerButton = createStyledButton("Đăng ký", PRIMARY_PURPLE, PURPLE_HOVER);
        cancelButton = createStyledButton("Hủy", PRIMARY_PURPLE, PURPLE_HOVER);
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(30);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorderReg(10, BORDER_COLOR),
            new EmptyBorder(10, 15, 10, 15)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(new Color(28, 28, 30));
        field.setOpaque(true);
        return field;
    }
    
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(30);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorderReg(10, BORDER_COLOR),
            new EmptyBorder(10, 15, 10, 15)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(new Color(28, 28, 30));
        field.setOpaque(true);
        return field;
    }
    
    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorderReg(10, BORDER_COLOR),
            new EmptyBorder(10, 15, 10, 15)
        ));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(28, 28, 30));
        combo.setOpaque(true);
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g2);
                g2.dispose();
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                // No border needed for rounded button
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 45));
        
        final Color finalBgColor = bgColor;
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(finalBgColor);
                button.repaint();
            }
        });
        
        return button;
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);
        
        // Main container with left blue border
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BG_COLOR);
        
        // Left blue border panel
        JPanel leftBorderPanel = new JPanel();
        leftBorderPanel.setPreferredSize(new Dimension(8, getHeight()));
        leftBorderPanel.setBackground(PRIMARY_BLUE);
        mainContainer.add(leftBorderPanel, BorderLayout.WEST);
        
        // Title panel at top
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_COLOR);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("Đăng ký tài khoản");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        mainContainer.add(titlePanel, BorderLayout.NORTH);
        
        // Main form panel - Card style with rounded corners
        JPanel cardPanel = new JPanel(new GridBagLayout()) {
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
        cardPanel.setOpaque(false);
        cardPanel.setBackground(CARD_BG);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorderReg(15, BORDER_COLOR),
            BorderFactory.createEmptyBorder(30, 35, 30, 35)
        ));
        
        // Add "Thông tin đăng ký" title inside card
        JLabel formTitleLabel = new JLabel("Thông tin đăng ký");
        formTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitleLabel.setForeground(TEXT_COLOR);
        formTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(10, 10, 10, 10);
        formGbc.anchor = GridBagConstraints.WEST;
        
        // Add form title
        formGbc.gridx = 0;
        formGbc.gridy = 0;
        formGbc.gridwidth = 2;
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.insets = new Insets(0, 0, 20, 0);
        cardPanel.add(formTitleLabel, formGbc);
        formGbc.gridwidth = 1;
        formGbc.insets = new Insets(10, 10, 10, 10);
        
        int y = 1;
        
        // Email
        addField(cardPanel, formGbc, y++, "Email *", emailField);
        
        // Password
        addField(cardPanel, formGbc, y++, "Mật khẩu *", passwordField);
        
        // Confirm Password
        addField(cardPanel, formGbc, y++, "Xác nhận mật khẩu *", confirmPasswordField);
        
        // First Name
        addField(cardPanel, formGbc, y++, "Tên *", firstNameField);
        
        // Last Name
        addField(cardPanel, formGbc, y++, "Họ *", lastNameField);
        
        // Phone
        addField(cardPanel, formGbc, y++, "Số điện thoại *", phoneField);
        
        // Student ID
        addField(cardPanel, formGbc, y++, "Mã sinh viên *", studentIdField);
        JLabel hintLabel = new JLabel("(1671020000 - 1671029999)");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLabel.setForeground(new Color(150, 150, 150));
        formGbc.gridx = 1;
        formGbc.gridy = y++;
        formGbc.insets = new Insets(0, 10, 10, 10);
        formGbc.fill = GridBagConstraints.NONE;
        cardPanel.add(hintLabel, formGbc);
        formGbc.insets = new Insets(10, 10, 10, 10);
        
        // Faculty
        JLabel facultyLabel = new JLabel("Khoa");
        facultyLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        facultyLabel.setForeground(TEXT_COLOR);
        formGbc.gridx = 0;
        formGbc.gridy = y;
        formGbc.fill = GridBagConstraints.NONE;
        formGbc.weightx = 0;
        cardPanel.add(facultyLabel, formGbc);
        formGbc.gridx = 1;
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.weightx = 1.0;
        cardPanel.add(facultyCombo, formGbc);
        y++;
        
        // Year of Study
        JLabel yearLabel = new JLabel("Năm học");
        yearLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        yearLabel.setForeground(TEXT_COLOR);
        formGbc.gridx = 0;
        formGbc.gridy = y;
        formGbc.fill = GridBagConstraints.NONE;
        formGbc.weightx = 0;
        cardPanel.add(yearLabel, formGbc);
        formGbc.gridx = 1;
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.weightx = 1.0;
        cardPanel.add(yearCombo, formGbc);
        y++;
        
        // Address
        JLabel addressLabel = new JLabel("Địa chỉ");
        addressLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addressLabel.setForeground(TEXT_COLOR);
        formGbc.gridx = 0;
        formGbc.gridy = y;
        formGbc.anchor = GridBagConstraints.NORTHWEST;
        formGbc.fill = GridBagConstraints.NONE;
        formGbc.weightx = 0;
        cardPanel.add(addressLabel, formGbc);
        formGbc.gridx = 1;
        formGbc.fill = GridBagConstraints.BOTH;
        formGbc.weightx = 1.0;
        formGbc.weighty = 0.3;
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorderReg(10, BORDER_COLOR),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        addressScroll.setOpaque(false);
        addressScroll.getViewport().setOpaque(false);
        cardPanel.add(addressScroll, formGbc);
        formGbc.weighty = 0;
        y++;
        
        // Add filler to push buttons down
        formGbc.gridx = 0;
        formGbc.gridy = y;
        formGbc.gridwidth = 2;
        formGbc.fill = GridBagConstraints.BOTH;
        formGbc.weightx = 1.0;
        formGbc.weighty = 1.0;
        cardPanel.add(new JPanel(), formGbc);
        formGbc.weighty = 0;
        
        // Wrap card panel in scroll pane
        JScrollPane scrollPane = new JScrollPane(cardPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBackground(BG_COLOR);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel - Always visible at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 20, 0));
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        
        // Add back button
        JButton backButton = createBackButton();
        backButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        buttonPanel.add(backButton);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BG_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        
        mainContainer.add(mainPanel, BorderLayout.CENTER);
        mainContainer.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainContainer, BorderLayout.CENTER);
    }
    
    private JButton createBackButton() {
        JButton button = new JButton("Quay về") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g2);
                g2.dispose();
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                // No border for rounded button
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(142, 142, 147)); // Gray
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 45));
        
        final Color bgColor = new Color(142, 142, 147);
        final Color hoverColor = new Color(162, 162, 167);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
                button.repaint();
            }
        });
        
        return button;
    }
    
    private void addField(JPanel panel, GridBagConstraints gbc, int y, String label, JComponent field) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 13));
        labelComponent.setForeground(TEXT_COLOR);
        
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(labelComponent, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
    }
    
    private void setupEventHandlers() {
        registerButton.addActionListener(e -> performRegistration());
        
        cancelButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc muốn hủy đăng ký không?", 
                "Xác nhận", 
                JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
    }
    
    private void performRegistration() {
        // Validate fields
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String studentId = studentIdField.getText().trim();
        
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || 
            firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ các trường bắt buộc!", 
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", 
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ!", 
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validate student ID
        try {
            long studentIdNum = Long.parseLong(studentId);
            if (studentIdNum < 1671020000 || studentIdNum > 1671029999) {
                JOptionPane.showMessageDialog(this, "Mã sinh viên phải trong khoảng 1671020000 - 1671029999!", 
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Mã sinh viên phải là số!", 
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validate phone number format
        if (!phone.matches("^[0-9]{10,11}$")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ! Vui lòng nhập 10-11 chữ số.", 
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validate password length
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Mật khẩu phải có ít nhất 6 ký tự!", 
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create user object
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setStudentId(studentId);
        
        // Handle optional fields - can be null if not selected
        String faculty = (String) facultyCombo.getSelectedItem();
        user.setFaculty(faculty != null ? faculty : "");
        
        String yearOfStudy = (String) yearCombo.getSelectedItem();
        user.setYearOfStudy(yearOfStudy != null ? yearOfStudy : "");
        
        String address = addressArea.getText().trim();
        user.setAddress(address != null && !address.isEmpty() ? address : "");
        
        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setRegistrationDate(new Date());
        user.setTotalBorrowed(0);
        user.setCurrentBorrowed(0);
        user.setTotalFines(0.0);
        
        if (!client.isConnected()) {
            if (!client.connect()) {
                JOptionPane.showMessageDialog(this, "Không thể kết nối đến server!", 
                    "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        Message request = new Message(Message.REGISTER, user);
        Message response = client.sendRequest(request);
        
        if (response.isSuccess()) {
            JOptionPane.showMessageDialog(this, "Đăng ký thành công! Vui lòng đăng nhập.", 
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginFrame().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, 
                response.getMessage() != null ? response.getMessage() : "Đăng ký thất bại!", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}



