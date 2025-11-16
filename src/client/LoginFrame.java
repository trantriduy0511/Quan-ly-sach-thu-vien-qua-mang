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

// Custom rounded border class for login
class RoundedBorderLogin implements Border {
    private int radius;
    private Color color;
    
    RoundedBorderLogin(int radius, Color color) {
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

public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckBox;
    private JButton loginButton;
    private JButton registerButton;
    private Client client;
    
    // Orange color scheme - matching image
    private final Color ORANGE_PRIMARY = new Color(255, 127, 0); // Bright orange #FF7F00
    private final Color ORANGE_HOVER = new Color(255, 140, 20); // Hover orange
    private final Color BLUE_BUTTON = new Color(66, 133, 244); // Bright blue
    private final Color BLUE_HOVER = new Color(92, 153, 255); // Hover blue
    private final Color BG_COLOR = new Color(242, 242, 247); // Light gray background
    private final Color BORDER_COLOR = new Color(220, 220, 220);
    private final Color TEXT_COLOR = new Color(28, 28, 30);
    
    public LoginFrame() {
        client = new Client();
        if (!client.connect()) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến server. Vui lòng đảm bảo server đang chạy.", 
                "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
        }
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setTitle("Đăng nhập User");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Customize text fields
        emailField = createStyledTextField();
        passwordField = createStyledPasswordField();
        
        showPasswordCheckBox = new JCheckBox("Hiển thị mật khẩu");
        showPasswordCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showPasswordCheckBox.setForeground(new Color(150, 150, 150));
        showPasswordCheckBox.setBackground(BG_COLOR);
        showPasswordCheckBox.setOpaque(false);
        
        loginButton = createStyledButton("Đăng nhập", ORANGE_PRIMARY, ORANGE_HOVER);
        registerButton = createStyledButton("Đăng ký tài khoản mới", BLUE_BUTTON, BLUE_HOVER);
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorderLogin(10, BORDER_COLOR),
            new EmptyBorder(10, 16, 10, 16)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_COLOR);
        field.setOpaque(true);
        field.setPreferredSize(new Dimension(450, 42));
        return field;
    }
    
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorderLogin(10, BORDER_COLOR),
            new EmptyBorder(10, 16, 10, 16)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_COLOR);
        field.setOpaque(true);
        field.setPreferredSize(new Dimension(450, 42));
        return field;
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
                // No border for rounded button
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(450, 45));
        
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
    
    private JButton createBackButton() {
        JButton button = new JButton("Quay lại") {
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
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(142, 142, 147));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(380, 42));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(250, 250, 250));
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
                button.repaint();
            }
        });
        
        return button;
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);
        
        // Main container with two panels side by side
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BG_COLOR);
        
        // Left Panel - Dark royal blue background with welcome message
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                // Dark royal blue background
                g2d.setColor(new Color(25, 25, 112)); // Dark royal blue
                g2d.fillRect(0, 0, w, h);
                g2d.dispose();
            }
        };
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(450, getHeight()));
        leftPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints leftGbc = new GridBagConstraints();
        leftGbc.insets = new Insets(20, 20, 20, 20);
        leftGbc.anchor = GridBagConstraints.CENTER;
        
        // Logo - Orange starburst design
        JPanel logoBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                int width = getWidth();
                int height = getHeight();
                int centerX = width / 2;
                int centerY = height / 2;
                
                // Calculate sizes
                int outerRadius = Math.min(width, height) / 2 - 20;
                int innerRadius = outerRadius / 3;
                int rayLength = outerRadius - innerRadius;
                int dotRadius = 6;
                int dotDistance = outerRadius + 15;
                
                // Draw 16 rays
                int numRays = 16;
                double angleStep = 2 * Math.PI / numRays;
                
                for (int i = 0; i < numRays; i++) {
                    double angle = i * angleStep;
                    double startX = centerX + innerRadius * Math.cos(angle);
                    double startY = centerY + innerRadius * Math.sin(angle);
                    double endX = centerX + outerRadius * Math.cos(angle);
                    double endY = centerY + outerRadius * Math.sin(angle);
                    
                    // Create triangle shape for ray
                    double angle1 = angle - angleStep / 2;
                    double angle2 = angle + angleStep / 2;
                    
                    int[] xPoints = new int[3];
                    int[] yPoints = new int[3];
                    
                    xPoints[0] = (int) (centerX + innerRadius * Math.cos(angle1));
                    yPoints[0] = (int) (centerY + innerRadius * Math.sin(angle1));
                    xPoints[1] = (int) (centerX + outerRadius * Math.cos(angle));
                    yPoints[1] = (int) (centerY + outerRadius * Math.sin(angle));
                    xPoints[2] = (int) (centerX + innerRadius * Math.cos(angle2));
                    yPoints[2] = (int) (centerY + innerRadius * Math.sin(angle2));
                    
                    g2d.setColor(Color.WHITE);
                    g2d.fillPolygon(xPoints, yPoints, 3);
                }
                
                // Draw central circle
                g2d.setColor(Color.WHITE);
                g2d.fillOval(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2);
                
                // Draw 16 outer dots
                for (int i = 0; i < numRays; i++) {
                    double angle = i * angleStep;
                    int dotX = (int) (centerX + dotDistance * Math.cos(angle));
                    int dotY = (int) (centerY + dotDistance * Math.sin(angle));
                    
                    g2d.setColor(Color.WHITE);
                    g2d.fillOval(dotX - dotRadius, dotY - dotRadius, dotRadius * 2, dotRadius * 2);
                }
                
                g2d.dispose();
            }
        };
        logoBox.setOpaque(false);
        logoBox.setBackground(Color.WHITE);
        logoBox.setPreferredSize(new Dimension(180, 180));
        leftGbc.gridx = 0;
        leftGbc.gridy = 0;
        leftPanel.add(logoBox, leftGbc);
        
        // "Chào mừng bạn!" text
        JLabel welcomeLabel = new JLabel("Chào mừng bạn!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        welcomeLabel.setForeground(Color.WHITE);
        leftGbc.gridy = 1;
        leftGbc.insets = new Insets(30, 20, 10, 20);
        leftPanel.add(welcomeLabel, leftGbc);
        
        // "Thư viện Online" text
        JLabel libraryLabel = new JLabel("Thư viện Online");
        libraryLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        libraryLabel.setForeground(Color.WHITE);
        leftGbc.gridy = 2;
        leftGbc.insets = new Insets(10, 20, 30, 20);
        leftPanel.add(libraryLabel, leftGbc);
        
        // Subtitle text
        JLabel subtitle1 = new JLabel("Tìm kiếm và mượn sách");
        subtitle1.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitle1.setForeground(Color.WHITE);
        leftGbc.gridy = 3;
        leftGbc.insets = new Insets(10, 20, 5, 20);
        leftPanel.add(subtitle1, leftGbc);
        
        JLabel subtitle2 = new JLabel("một cách dễ dàng và tiện lợi");
        subtitle2.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitle2.setForeground(Color.WHITE);
        leftGbc.gridy = 4;
        leftGbc.insets = new Insets(5, 20, 20, 20);
        leftPanel.add(subtitle2, leftGbc);
        
        // Right Panel - White background with login form
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(BG_COLOR);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(70, 70, 70, 70));
        
        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.insets = new Insets(18, 10, 18, 10);
        rightGbc.anchor = GridBagConstraints.WEST;
        
        // "Đăng nhập" title
        JLabel loginTitle = new JLabel("Đăng nhập");
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        loginTitle.setForeground(ORANGE_PRIMARY);
        rightGbc.gridx = 0;
        rightGbc.gridy = 0;
        rightGbc.gridwidth = 2;
        rightGbc.fill = GridBagConstraints.HORIZONTAL;
        rightGbc.insets = new Insets(0, 0, 40, 0);
        rightPanel.add(loginTitle, rightGbc);
        
        // Email label and field
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        emailLabel.setForeground(TEXT_COLOR);
        rightGbc.gridx = 0;
        rightGbc.gridy = 1;
        rightGbc.gridwidth = 2;
        rightGbc.fill = GridBagConstraints.HORIZONTAL;
        rightGbc.insets = new Insets(12, 0, 8, 0);
        rightPanel.add(emailLabel, rightGbc);
        
        rightGbc.gridy = 2;
        rightGbc.insets = new Insets(8, 0, 18, 0);
        rightPanel.add(emailField, rightGbc);
        
        // Password label and field
        JLabel passwordLabel = new JLabel("Mật khẩu");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passwordLabel.setForeground(TEXT_COLOR);
        rightGbc.gridy = 3;
        rightGbc.insets = new Insets(12, 0, 8, 0);
        rightPanel.add(passwordLabel, rightGbc);
        
        rightGbc.gridy = 4;
        rightGbc.insets = new Insets(8, 0, 12, 0);
        rightPanel.add(passwordField, rightGbc);
        
        // Show password checkbox
        showPasswordCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showPasswordCheckBox.setForeground(new Color(142, 142, 147));
        showPasswordCheckBox.setBackground(BG_COLOR);
        showPasswordCheckBox.setOpaque(false);
        rightGbc.gridy = 5;
        rightGbc.insets = new Insets(5, 0, 25, 0);
        rightPanel.add(showPasswordCheckBox, rightGbc);
        
        // Login button
        rightGbc.gridy = 6;
        rightGbc.insets = new Insets(12, 0, 12, 0);
        rightPanel.add(loginButton, rightGbc);
        
        // Register button
        rightGbc.gridy = 7;
        rightPanel.add(registerButton, rightGbc);
        
        // Back button
        JButton backButton = createBackButton();
        backButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, 
                "Bạn có muốn đóng cửa sổ đăng nhập không?", 
                "Xác nhận", 
                JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        rightGbc.gridy = 8;
        rightGbc.insets = new Insets(12, 0, 0, 0);
        rightPanel.add(backButton, rightGbc);
        
        // Add panels to main container
        mainContainer.add(leftPanel, BorderLayout.WEST);
        mainContainer.add(rightPanel, BorderLayout.CENTER);
        
        add(mainContainer, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });
        
        loginButton.addActionListener(e -> performLogin());
        
        registerButton.addActionListener(e -> {
            dispose();
            new RegistrationFrame(client).setVisible(true);
        });
        
        passwordField.addActionListener(e -> performLogin());
    }
    
    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", 
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!client.isConnected()) {
            if (!client.connect()) {
                JOptionPane.showMessageDialog(this, "Không thể kết nối đến server!", 
                    "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        Message request = new Message(Message.LOGIN, new Object[]{email, password});
        Message response = client.sendRequest(request);
        
        if (response.isSuccess() && response.getData() instanceof User) {
            User user = (User) response.getData();
            dispose();
            
            if (user.isAdmin()) {
                new AdminFrame(client, user).setVisible(true);
            } else {
                new UserFrame(client, user).setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                response.getMessage() != null ? response.getMessage() : "Đăng nhập thất bại!", 
                "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Open 2 login windows: one for admin and one for user
            LoginFrame adminLoginFrame = new LoginFrame();
            adminLoginFrame.setTitle("Đăng nhập - Admin");
            adminLoginFrame.setLocation(100, 100);
            adminLoginFrame.setVisible(true);
            
            LoginFrame userLoginFrame = new LoginFrame();
            userLoginFrame.setTitle("Đăng nhập - User");
            userLoginFrame.setLocation(adminLoginFrame.getX() + adminLoginFrame.getWidth() + 20, 100);
            userLoginFrame.setVisible(true);
        });
    }
}
