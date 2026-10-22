package vsc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFrame extends JFrame {

    private final JLabel customerCount = statValue();
    private final JLabel vehicleCount  = statValue();
    private final JLabel serviceCount  = statValue();
    private boolean overviewRefreshInProgress;

    // Sidebar state
    private final JPanel contentArea = new JPanel(new CardLayout());
    private JButton activeBtn = null;

    public MainFrame() {
        super("Vehicle Service Center Management");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DEEP);

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMainArea(), BorderLayout.CENTER);

        setContentPane(root);
        setMinimumSize(new Dimension(1100, 680));
        setSize(1280, 780);
        setLocationRelativeTo(null);

        refreshOverview();
    }

    // ── Sidebar ────────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER_SUBTLE));

        // Logo area
        JPanel logoArea = new JPanel();
        logoArea.setOpaque(false);
        logoArea.setLayout(new BoxLayout(logoArea, BoxLayout.Y_AXIS));
        logoArea.setBorder(BorderFactory.createEmptyBorder(28, 22, 22, 22));
        logoArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoArea.setMaximumSize(new Dimension(220, 120));

        JLabel badge = new JLabel("VSC") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UITheme.ACCENT_CYAN, getWidth(), getHeight(), new Color(0x0080FF));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setForeground(Color.WHITE);
        badge.setFont(UITheme.bold(16));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        badge.setPreferredSize(new Dimension(60, 36));
        badge.setMaximumSize(new Dimension(60, 36));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel appName = new JLabel("Service Center");
        appName.setForeground(UITheme.TEXT_PRIMARY);
        appName.setFont(UITheme.bold(14));
        appName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel appSub = new JLabel("Operations Dashboard");
        appSub.setForeground(UITheme.TEXT_SECONDARY);
        appSub.setFont(UITheme.plain(11));
        appSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoArea.add(badge);
        logoArea.add(Box.createVerticalStrut(10));
        logoArea.add(appName);
        logoArea.add(Box.createVerticalStrut(2));
        logoArea.add(appSub);

        // Separator
        JPanel sep = new JPanel();
        sep.setBackground(UITheme.BORDER_SUBTLE);
        sep.setMaximumSize(new Dimension(220, 1));
        sep.setPreferredSize(new Dimension(220, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Nav label
        JLabel navLabel = new JLabel("NAVIGATION");
        navLabel.setForeground(UITheme.TEXT_MUTED);
        navLabel.setFont(UITheme.bold(10));
        navLabel.setBorder(BorderFactory.createEmptyBorder(18, 22, 8, 0));
        navLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Nav items
        JButton customersBtn = sidebarBtn("👥  Customers", "customers");
        JButton vehiclesBtn  = sidebarBtn("🚗  Vehicles",  "vehicles");
        JButton servicesBtn  = sidebarBtn("🔧  Services",  "services");

        // Cards for content
        contentArea.setBackground(UITheme.BG_DEEP);
        contentArea.add(buildCrudCard("customers",
            new String[]{"name","phone","email","address"},
            new String[]{"Name","Phone","Email","Address"}, null), "customers");
        contentArea.add(buildCrudCard("vehicles",
            new String[]{"customer_id","reg_no","make","model","year","photo"},
            new String[]{"Customer ID","Reg. No","Make","Model","Year","Photo"}, null), "vehicles");
        contentArea.add(buildCrudCard("services",
            new String[]{"vehicle_id","service_type","description","service_date","cost","status"},
            new String[]{"Vehicle ID","Service Type","Description","Date (YYYY-MM-DD)","Cost","Status"},
            new String[][]{null,null,null,null,null,{"Pending","In Progress","Completed","Delivered"}}), "services");

        // Default tab
        activateBtn(customersBtn, "customers");

        customersBtn.addActionListener(e -> activateBtn(customersBtn, "customers"));
        vehiclesBtn.addActionListener(e  -> activateBtn(vehiclesBtn,  "vehicles"));
        servicesBtn.addActionListener(e  -> activateBtn(servicesBtn,  "services"));

        // Bottom: logout
        JPanel bottomArea = new JPanel();
        bottomArea.setOpaque(false);
        bottomArea.setLayout(new BoxLayout(bottomArea, BoxLayout.Y_AXIS));
        bottomArea.setBorder(BorderFactory.createEmptyBorder(0, 14, 20, 14));
        bottomArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton logoutBtn = new UITheme.AnimatedButton("⬅  Logout",
            new Color(0x1A0A05), UITheme.ACCENT_ORANGE.darker());
        logoutBtn.setForeground(UITheme.ACCENT_ORANGE);
        logoutBtn.setFont(UITheme.bold(12));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(192, 38));
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        bottomArea.add(logoutBtn);

        sidebar.add(logoArea);
        sidebar.add(sep);
        sidebar.add(navLabel);
        sidebar.add(customersBtn);
        sidebar.add(vehiclesBtn);
        sidebar.add(servicesBtn);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(bottomArea);

        return sidebar;
    }

    private JButton sidebarBtn(String text, String cardName) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getClientProperty("active") != null) {
                    g2.setColor(new Color(0x00D4FF, false).brighter());
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                    g2.setColor(UITheme.ACCENT_CYAN);
                    g2.fillRoundRect(0, (getHeight() - 28) / 2, 3, 28, 3, 3);
                } else if (getModel().isRollover()) {
                    g2.setColor(UITheme.BORDER_SUBTLE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(UITheme.TEXT_SECONDARY);
        btn.setFont(UITheme.plain(13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(220, 44));
        return btn;
    }

    private void activateBtn(JButton btn, String card) {
        if (activeBtn != null) {
            activeBtn.putClientProperty("active", null);
            activeBtn.setForeground(UITheme.TEXT_SECONDARY);
            activeBtn.repaint();
        }
        activeBtn = btn;
        btn.putClientProperty("active", true);
        btn.setForeground(UITheme.ACCENT_CYAN);
        btn.repaint();
        ((CardLayout) contentArea.getLayout()).show(contentArea, card);
    }

    // ── Main Area ─────────────────────────────────────────────────────────────
    private JPanel buildMainArea() {
        JPanel area = new JPanel(new BorderLayout(0, 0));
        area.setBackground(UITheme.BG_DEEP);

        area.add(buildHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setBackground(UITheme.BG_DEEP);
        center.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        center.add(buildStatBar(), BorderLayout.NORTH);
        center.add(contentArea, BorderLayout.CENTER);

        area.add(center, BorderLayout.CENTER);
        return area;
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        UITheme.GradientPanel header = new UITheme.GradientPanel(
            new Color(0x0D1A2E), new Color(0x0A0F1E), true);
        header.setLayout(new BorderLayout(20, 0));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(16, 24, 16, 24)
        ));

        JLabel title = new JLabel("Operations Dashboard");
        title.setFont(UITheme.bold(22));
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Manage customers, vehicles and services");
        sub.setFont(UITheme.plain(12));
        sub.setForeground(UITheme.TEXT_SECONDARY);

        JPanel titleGroup = new JPanel();
        titleGroup.setOpaque(false);
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.add(title);
        titleGroup.add(Box.createVerticalStrut(2));
        titleGroup.add(sub);

        // Live clock
        JLabel clock = new JLabel();
        clock.setForeground(UITheme.ACCENT_CYAN);
        clock.setFont(UITheme.bold(13));
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM  HH:mm:ss");
        Timer clockTimer = new Timer(1000, e -> clock.setText(sdf.format(new Date())));
        clockTimer.setInitialDelay(0);
        clockTimer.start();

        header.add(titleGroup, BorderLayout.WEST);
        header.add(clock, BorderLayout.EAST);
        return header;
    }

    // ── Stat Bar ──────────────────────────────────────────────────────────────
    private JPanel buildStatBar() {
        JPanel bar = new JPanel(new GridLayout(1, 3, 14, 0));
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        bar.add(UITheme.createStatCard("Customers", customerCount, UITheme.ACCENT_CYAN,   "👥"));
        bar.add(UITheme.createStatCard("Vehicles",  vehicleCount,  UITheme.ACCENT_GREEN,  "🚗"));
        bar.add(UITheme.createStatCard("Services",  serviceCount,  UITheme.ACCENT_ORANGE, "🔧"));
        return bar;
    }

    private JPanel buildCrudCard(String table, String[] cols, String[] labels, String[][] opts) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(new CrudPanel(table, cols, labels, opts));
        return wrapper;
    }

    // ── Stat refresh ──────────────────────────────────────────────────────────
    private static JLabel statValue() {
        JLabel l = new JLabel("—");
        l.setFont(UITheme.bold(32));
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    void refreshOverview() {
        if (overviewRefreshInProgress) return;
        overviewRefreshInProgress = true;
        SwingWorker<Integer[], Void> w = new SwingWorker<>() {
            @Override protected Integer[] doInBackground() {
                return new Integer[]{
                    count("SELECT COUNT(*) FROM customers"),
                    count("SELECT COUNT(*) FROM vehicles"),
                    count("SELECT COUNT(*) FROM services")
                };
            }
            @Override protected void done() {
                try {
                    Integer[] c = get();
                    animateCount(customerCount, c[0]);
                    animateCount(vehicleCount,  c[1]);
                    animateCount(serviceCount,  c[2]);
                } catch (Exception ignored) {}
                finally { overviewRefreshInProgress = false; }
            }
        };
        w.execute();
    }

    private void animateCount(JLabel label, int target) {
        Timer t = new Timer(30, null);
        final int[] cur = {0};
        t.addActionListener(e -> {
            cur[0] += Math.max(1, (target - cur[0]) / 6);
            if (cur[0] >= target) { cur[0] = target; t.stop(); }
            label.setText(String.valueOf(cur[0]));
        });
        t.start();
    }

    private int count(String sql) {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) { return 0; }
    }
}
