package vsc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;

public class LoginFrame extends JFrame {

    private final JTextField user      = UITheme.glowField(20);
    private final JPasswordField pass  = UITheme.glowPassword(20);
    private JButton loginBtn;

    public LoginFrame() {
        super("Vehicle Service Center — Login");
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 860, 540, 24, 24));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DEEP);
        root.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_SUBTLE, 1));

        root.add(buildBrandPanel(), BorderLayout.WEST);
        root.add(buildFormPanel(), BorderLayout.CENTER);

        setContentPane(root);
        setSize(860, 540);
        setLocationRelativeTo(null);

        // Drag to move (since undecorated)
        addWindowDragSupport(root);
    }

    // ── Left Brand Panel ─────────────────────────────────────────────────────
    private JPanel buildBrandPanel() {
        JPanel panel = new UITheme.GradientPanel(UITheme.BG_DEEP, new Color(0x0C1830), false) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Decorative circles
                g2.setColor(new Color(0x00D4FF, false).brighter());
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.04f));
                g2.fillOval(-60, -60, 260, 260);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.03f));
                g2.fillOval(60, 320, 300, 300);

                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(360, 540));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 36, 50, 36));

        // Logo badge
        JLabel badge = new JLabel("VSC") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UITheme.ACCENT_CYAN, getWidth(), getHeight(), new Color(0x0080FF));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        badge.setForeground(Color.WHITE);
        badge.setFont(UITheme.bold(28));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setPreferredSize(new Dimension(88, 56));
        badge.setMaximumSize(new Dimension(88, 56));
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Vehicle icon
        JLabel carIcon = new JLabel("🚗");
        carIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        carIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        carIcon.setHorizontalAlignment(SwingConstants.CENTER);

        // Title
        JLabel title = new JLabel("<html><center>Vehicle Service<br>Center</center></html>");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setFont(UITheme.bold(22));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        // Subtitle
        JLabel subtitle = new JLabel("Drive in. Service smart.");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(UITheme.TEXT_SECONDARY);
        subtitle.setFont(UITheme.plain(13));

        // Cyan divider
        JPanel divider = new JPanel();
        divider.setMaximumSize(new Dimension(100, 3));
        divider.setPreferredSize(new Dimension(100, 3));
        divider.setBackground(UITheme.ACCENT_CYAN);
        divider.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Feature bullets
        JPanel bullets = new JPanel();
        bullets.setOpaque(false);
        bullets.setLayout(new BoxLayout(bullets, BoxLayout.Y_AXIS));
        bullets.setAlignmentX(Component.CENTER_ALIGNMENT);
        for (String feat : new String[]{"⚡  Fast service tracking", "🔧  Repair management", "👥  Customer records"}) {
            JLabel l = new JLabel(feat);
            l.setForeground(UITheme.TEXT_SECONDARY);
            l.setFont(UITheme.plain(12));
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
            l.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
            bullets.add(l);
        }

        panel.add(Box.createVerticalGlue());
        panel.add(badge);
        panel.add(Box.createVerticalStrut(20));
        panel.add(carIcon);
        panel.add(Box.createVerticalStrut(18));
        panel.add(title);
        panel.add(Box.createVerticalStrut(8));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(18));
        panel.add(divider);
        panel.add(Box.createVerticalStrut(18));
        panel.add(bullets);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // ── Right Form Panel ──────────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UITheme.BG_SURFACE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        // Close button
        JButton closeBtn = new JButton("✕");
        closeBtn.setFocusPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setForeground(UITheme.TEXT_SECONDARY);
        closeBtn.setFont(UITheme.bold(16));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> System.exit(0));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { closeBtn.setForeground(UITheme.ACCENT_ORANGE); }
            public void mouseExited(MouseEvent e) { closeBtn.setForeground(UITheme.TEXT_SECONDARY); }
        });
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topBar.setOpaque(false);
        topBar.add(closeBtn);
        topBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Heading
        JLabel heading = new JLabel("Welcome back");
        heading.setFont(UITheme.bold(28));
        heading.setForeground(UITheme.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subheading = new JLabel("Sign in to your operations dashboard");
        subheading.setFont(UITheme.plain(13));
        subheading.setForeground(UITheme.TEXT_SECONDARY);
        subheading.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Username row
        JLabel userLabel = fieldLabel("Username");
        user.setAlignmentX(Component.LEFT_ALIGNMENT);
        user.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // Password row
        JLabel passLabel = fieldLabel("Password");
        pass.setAlignmentX(Component.LEFT_ALIGNMENT);
        pass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // Show/hide toggle
        JCheckBox showPass = new JCheckBox("Show password");
        showPass.setOpaque(false);
        showPass.setForeground(UITheme.TEXT_SECONDARY);
        showPass.setFont(UITheme.plain(11));
        showPass.setFocusPainted(false);
        showPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPass.addActionListener(e ->
            pass.setEchoChar(showPass.isSelected() ? '\0' : '•')
        );

        // Login button
        loginBtn = new UITheme.AnimatedButton("Sign In →",
            UITheme.ACCENT_CYAN.darker(),
            UITheme.ACCENT_CYAN);
        loginBtn.setFont(UITheme.bold(15));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        loginBtn.addActionListener(e -> doLogin());

        // Footer
        JLabel footer = new JLabel("Vehicle Service Center • v2.0");
        footer.setForeground(UITheme.TEXT_MUTED);
        footer.setFont(UITheme.plain(11));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(topBar);
        form.add(Box.createVerticalStrut(12));
        form.add(heading);
        form.add(Box.createVerticalStrut(6));
        form.add(subheading);
        form.add(Box.createVerticalStrut(28));
        form.add(userLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(user);
        form.add(Box.createVerticalStrut(16));
        form.add(passLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(pass);
        form.add(Box.createVerticalStrut(8));
        form.add(showPass);
        form.add(Box.createVerticalStrut(24));
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(20));
        form.add(footer);

        outer.add(form, new GridBagConstraints());
        getRootPane().setDefaultButton(loginBtn);
        return outer;
    }

    private static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.bold(12));
        l.setForeground(UITheme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ── Drag to move ──────────────────────────────────────────────────────────
    private void addWindowDragSupport(JPanel root) {
        final int[] dragOffset = {0, 0};
        root.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dragOffset[0] = e.getX();
                dragOffset[1] = e.getY();
            }
        });
        root.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                setLocation(e.getXOnScreen() - dragOffset[0], e.getYOnScreen() - dragOffset[1]);
            }
        });
    }

    // ── Shake Animation ───────────────────────────────────────────────────────
    private void shake() {
        Point origin = getLocation();
        Timer t = new Timer(18, null);
        final int[] step = {0};
        int[] offsets = {-10, 10, -8, 8, -5, 5, -2, 2, 0};
        t.addActionListener(e -> {
            if (step[0] < offsets.length) {
                setLocation(origin.x + offsets[step[0]], origin.y);
                step[0]++;
            } else {
                setLocation(origin);
                t.stop();
            }
        });
        t.start();
    }

    // ── Login Logic ───────────────────────────────────────────────────────────
    private void doLogin() {
        loginBtn.setEnabled(false);
        loginBtn.setText("Signing in…");
        String sql = "SELECT 1 FROM users WHERE username=? AND password_hash=SHA2(?,256)";
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() throws Exception {
                try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, user.getText().trim());
                    ps.setString(2, new String(pass.getPassword()));
                    return ps.executeQuery().next();
                }
            }
            @Override protected void done() {
                loginBtn.setEnabled(true);
                loginBtn.setText("Sign In →");
                try {
                    if (get()) {
                        new MainFrame().setVisible(true);
                        dispose();
                    } else {
                        shake();
                        JOptionPane.showMessageDialog(LoginFrame.this,
                            "Invalid username or password", "Authentication Failed",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                        "Database error: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                DB.ensureVehiclePhotoColumn();
                DB.seedDemoData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null,
                    "Database error: " + ex.getMessage(), "Database setup",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            new LoginFrame().setVisible(true);
        });
    }
}
