package vsc;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Shared UI theme constants and factory methods for the Vehicle Service Center app.
 */
public final class UITheme {

    // ── Palette ──────────────────────────────────────────────────────────────
    public static final Color BG_DEEP       = new Color(0x0A0F1E);
    public static final Color BG_SURFACE    = new Color(0x0D1526);
    public static final Color BG_CARD       = new Color(0x111C30);
    public static final Color BG_INPUT      = new Color(0x162035);
    public static final Color BG_SIDEBAR    = new Color(0x080D18);

    public static final Color ACCENT_CYAN   = new Color(0x00D4FF);
    public static final Color ACCENT_ORANGE = new Color(0xFF6B35);
    public static final Color ACCENT_GREEN  = new Color(0x00E5A0);
    public static final Color ACCENT_PURPLE = new Color(0xA855F7);
    public static final Color ACCENT_YELLOW = new Color(0xFFB800);

    public static final Color TEXT_PRIMARY  = new Color(0xECF0F8);
    public static final Color TEXT_SECONDARY= new Color(0x8898B0);
    public static final Color TEXT_MUTED    = new Color(0x4A5C78);

    public static final Color BORDER_SUBTLE = new Color(0x1E2D45);
    public static final Color BORDER_BRIGHT = new Color(0x00D4FF, false);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    public static Font font(int style, int size) {
        return new Font("SansSerif", style, size);
    }
    public static Font bold(int size)  { return font(Font.BOLD,  size); }
    public static Font plain(int size) { return font(Font.PLAIN, size); }

    // ── Rounded Border ────────────────────────────────────────────────────────
    public static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        private final int thickness;

        public RoundedBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x + thickness / 2f, y + thickness / 2f,
                    w - thickness, h - thickness, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            int p = thickness + radius / 4;
            return new Insets(p, p, p, p);
        }
    }

    // ── Gradient Panel ────────────────────────────────────────────────────────
    public static class GradientPanel extends JPanel {
        private final Color from;
        private final Color to;
        private final boolean horizontal;

        public GradientPanel(Color from, Color to, boolean horizontal) {
            this.from = from;
            this.to = to;
            this.horizontal = horizontal;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth(), h = getHeight();
            GradientPaint gp = horizontal
                    ? new GradientPaint(0, 0, from, w, 0, to)
                    : new GradientPaint(0, 0, from, 0, h, to);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Rounded Panel ─────────────────────────────────────────────────────────
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;

        public RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
        }
    }

    // ── Animated Button ───────────────────────────────────────────────────────
    public static class AnimatedButton extends JButton {
        private final Color normalBg;
        private final Color hoverBg;
        private Color currentBg;

        public AnimatedButton(String text, Color normalBg, Color hoverBg) {
            super(text);
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            this.currentBg = normalBg;
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setFont(bold(13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    animateTo(hoverBg);
                }
                @Override public void mouseExited(MouseEvent e) {
                    animateTo(normalBg);
                }
            });
        }

        private void animateTo(Color target) {
            Timer t = new Timer(8, null);
            t.addActionListener(e -> {
                int dr = target.getRed()   - currentBg.getRed();
                int dg = target.getGreen() - currentBg.getGreen();
                int db = target.getBlue()  - currentBg.getBlue();
                if (Math.abs(dr) < 3 && Math.abs(dg) < 3 && Math.abs(db) < 3) {
                    currentBg = target;
                    ((Timer) e.getSource()).stop();
                } else {
                    currentBg = new Color(
                        currentBg.getRed()   + dr / 5,
                        currentBg.getGreen() + dg / 5,
                        currentBg.getBlue()  + db / 5
                    );
                }
                repaint();
            });
            t.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Glow Input Field ─────────────────────────────────────────────────────
    public static JTextField glowField(int cols) {
        JTextField f = new JTextField(cols) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setOpaque(false);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT_CYAN);
        f.setFont(plain(14));
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_SUBTLE, 10, 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(ACCENT_CYAN, 10, 2),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(BORDER_SUBTLE, 10, 1),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));
            }
        });
        return f;
    }

    public static JPasswordField glowPassword(int cols) {
        JPasswordField f = new JPasswordField(cols) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setOpaque(false);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT_CYAN);
        f.setFont(plain(14));
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_SUBTLE, 10, 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(ACCENT_CYAN, 10, 2),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(BORDER_SUBTLE, 10, 1),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));
            }
        });
        return f;
    }

    // ── Stat Card Factory ─────────────────────────────────────────────────────
    public static JPanel createStatCard(String title, JLabel valueLabel, Color accent, String icon) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // Left accent bar
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setFont(bold(10));

        valueLabel.setFont(bold(34));
        valueLabel.setForeground(TEXT_PRIMARY);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(titleLabel, BorderLayout.WEST);
        top.add(iconLabel, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private UITheme() {}
}
