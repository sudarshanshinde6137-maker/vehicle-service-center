package vsc;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;

/**
 * Reusable Add / Update / Delete / Search panel for any table with an "id" primary key.
 * Premium dark-themed redesign.
 */
public class CrudPanel extends JPanel {
    private final String table;
    private final String[] cols;
    private final JComponent[] inputs;
    private final DefaultTableModel model;
    private final JTable grid;
    private final JTextField search = buildSearchField();

    public CrudPanel(String table, String[] cols, String[] labels, String[][] options) {
        super(new BorderLayout(14, 14));
        this.table = table;
        this.cols  = cols;
        setBackground(UITheme.BG_DEEP);
        setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // ── Form (left) ───────────────────────────────────────────────────────
        JPanel formCard = new UITheme.RoundedPanel(14, UITheme.BG_CARD);
        formCard.setLayout(new BorderLayout(0, 12));
        formCard.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel fieldGrid = new JPanel(new GridBagLayout());
        fieldGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 4, 6, 4);
        gbc.fill    = GridBagConstraints.HORIZONTAL;

        inputs = new JComponent[cols.length];
        for (int i = 0; i < cols.length; i++) {
            if (isPhotoField(cols[i])) {
                inputs[i] = new PhotoField();
            } else if (isReferenceField(cols[i])) {
                JComboBox<String> cb = new JComboBox<>();
                styleCombo(cb);
                inputs[i] = cb;
                loadReferenceOptions(cb, cols[i]);
            } else if (options != null && options[i] != null) {
                JComboBox<String> cb = new JComboBox<>(options[i]);
                styleCombo(cb);
                inputs[i] = cb;
            } else {
                inputs[i] = UITheme.glowField(18);
            }

            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(UITheme.bold(11));
            lbl.setForeground(UITheme.TEXT_SECONDARY);

            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.3;
            fieldGrid.add(lbl, gbc);
            gbc.gridx = 1;             gbc.weightx = 0.7;
            fieldGrid.add(inputs[i], gbc);
        }

        // ── Action buttons ────────────────────────────────────────────────────
        JButton add = iconBtn("✚  Add",    UITheme.ACCENT_CYAN.darker(),   UITheme.ACCENT_CYAN);
        JButton upd = iconBtn("✎  Update", new Color(0x064E5A),            new Color(0x06B6D4));
        JButton del = iconBtn("✖  Delete", new Color(0x4A0A0A),            new Color(0xEF4444));
        JButton clr = iconBtn("↺  Clear",  UITheme.BG_SURFACE,             UITheme.TEXT_SECONDARY);

        JPanel btnRow = new JPanel(new GridLayout(2, 2, 8, 8));
        btnRow.setOpaque(false);
        btnRow.add(add); btnRow.add(upd); btnRow.add(del); btnRow.add(clr);

        JLabel formTitle = new JLabel("Record Details");
        formTitle.setFont(UITheme.bold(14));
        formTitle.setForeground(UITheme.TEXT_PRIMARY);

        formCard.add(formTitle,  BorderLayout.NORTH);
        formCard.add(fieldGrid,  BorderLayout.CENTER);
        formCard.add(btnRow,     BorderLayout.SOUTH);

        JPanel west = new JPanel(new BorderLayout());
        west.setOpaque(false);
        west.setPreferredSize(new Dimension(360, 0));
        west.add(formCard, BorderLayout.CENTER);
        add(west, BorderLayout.WEST);

        // ── Table (center) ────────────────────────────────────────────────────
        String[] heads = new String[labels.length + 1];
        heads[0] = "ID";
        System.arraycopy(labels, 0, heads, 1, labels.length);

        model = new DefaultTableModel(heads, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        grid = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? UITheme.BG_SURFACE : UITheme.BG_CARD);
                }
                return c;
            }
        };
        grid.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        grid.setRowHeight(34);
        grid.setFillsViewportHeight(true);
        grid.setShowGrid(false);
        grid.setIntercellSpacing(new Dimension(0, 2));
        grid.setBackground(UITheme.BG_SURFACE);
        grid.setForeground(UITheme.TEXT_PRIMARY);
        grid.setSelectionBackground(new Color(0x00D4FF, false));
        grid.setSelectionForeground(UITheme.BG_DEEP);
        grid.setFont(UITheme.plain(13));

        // Custom selection color with alpha
        grid.setSelectionBackground(new Color(0x00, 0xD4, 0xFF, 60));
        grid.setSelectionForeground(UITheme.TEXT_PRIMARY);

        // Header
        JTableHeader th = grid.getTableHeader();
        th.setReorderingAllowed(false);
        th.setBackground(UITheme.BG_SIDEBAR);
        th.setForeground(UITheme.TEXT_SECONDARY);
        th.setFont(UITheme.bold(11));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_SUBTLE));
        th.setPreferredSize(new Dimension(th.getWidth(), 38));

        // Status badge renderer for the last column if it has status options
        if (options != null) {
            for (int i = 0; i < options.length; i++) {
                if (options[i] != null) {
                    grid.getColumnModel().getColumn(i + 1).setCellRenderer(new StatusBadgeRenderer());
                }
            }
        }

        grid.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillForm();
        });

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBackground(UITheme.BG_SURFACE);
        scroll.getViewport().setBackground(UITheme.BG_SURFACE);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        UITheme.RoundedPanel tableCard = new UITheme.RoundedPanel(14, UITheme.BG_CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Search bar at the top of table card
        JPanel tableTop = new JPanel(new BorderLayout(10, 0));
        tableTop.setOpaque(false);
        tableTop.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));

        JLabel tableTitle = new JLabel("Records");
        tableTitle.setFont(UITheme.bold(14));
        tableTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        search.setMaximumSize(new Dimension(220, 36));
        JButton go = iconBtn("🔍  Search", new Color(0x0A2A1A), UITheme.ACCENT_GREEN);
        searchRow.add(search, BorderLayout.CENTER);
        searchRow.add(go, BorderLayout.EAST);

        tableTop.add(tableTitle, BorderLayout.WEST);
        tableTop.add(searchRow, BorderLayout.EAST);

        tableCard.add(tableTop,  BorderLayout.NORTH);
        tableCard.add(scroll,    BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);

        // ── Wire up actions ───────────────────────────────────────────────────
        go.addActionListener(e -> load());
        search.addActionListener(e -> load());
        add.addActionListener(e -> insert());
        upd.addActionListener(e -> update());
        del.addActionListener(e -> delete());
        clr.addActionListener(e -> clearForm());
        load();
    }

    // ── Status Badge Renderer ─────────────────────────────────────────────────
    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int col) {
            JLabel lbl = new JLabel(value == null ? "" : value.toString()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lbl.setOpaque(false);
            lbl.setFont(UITheme.bold(11));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));

            String v = value == null ? "" : value.toString();
            switch (v) {
                case "Pending"     -> { lbl.setBackground(new Color(0x78350F)); lbl.setForeground(UITheme.ACCENT_YELLOW); }
                case "In Progress" -> { lbl.setBackground(new Color(0x0C3A5E)); lbl.setForeground(UITheme.ACCENT_CYAN); }
                case "Completed"   -> { lbl.setBackground(new Color(0x064E3B)); lbl.setForeground(UITheme.ACCENT_GREEN); }
                case "Delivered"   -> { lbl.setBackground(new Color(0x1E1B4B)); lbl.setForeground(UITheme.ACCENT_PURPLE); }
                default            -> { lbl.setBackground(UITheme.BG_CARD);     lbl.setForeground(UITheme.TEXT_SECONDARY); }
            }

            // Row background
            JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
            cell.setBackground(selected
                ? new Color(0x00, 0xD4, 0xFF, 40)
                : (row % 2 == 0 ? UITheme.BG_SURFACE : UITheme.BG_CARD));
            cell.add(lbl);
            return cell;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static boolean isPhotoField(String col) {
        return col != null && (col.equalsIgnoreCase("photo")
            || col.equalsIgnoreCase("photo_path")
            || col.equalsIgnoreCase("image_path"));
    }

    private boolean isReferenceField(String col) {
        return ("vehicles".equals(table) && "customer_id".equalsIgnoreCase(col))
            || ("services".equals(table) && "vehicle_id".equalsIgnoreCase(col));
    }

    private void loadReferenceOptions(JComboBox<String> combo, String column) {
        String parent = "customer_id".equalsIgnoreCase(column) ? "customers" : "vehicles";
        SwingWorker<String[], Void> worker = new SwingWorker<>() {
            @Override protected String[] doInBackground() throws Exception {
                java.util.List<String> ids = new java.util.ArrayList<>();
                try (Connection c = DB.get();
                     PreparedStatement ps = c.prepareStatement("SELECT id FROM " + parent + " ORDER BY id");
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) ids.add(String.valueOf(rs.getInt(1)));
                }
                return ids.toArray(new String[0]);
            }

            @Override protected void done() {
                try {
                    combo.removeAllItems();
                    for (String id : get()) combo.addItem(id);
                } catch (Exception ex) {
                    error(new SQLException("Failed to load " + parent + " IDs", ex));
                }
            }
        };
        worker.execute();
    }

    private static JTextField buildSearchField() {
        JTextField f = UITheme.glowField(16);
        f.setPreferredSize(new Dimension(200, 36));
        return f;
    }

    private static JButton iconBtn(String text, Color normal, Color hover) {
        UITheme.AnimatedButton b = new UITheme.AnimatedButton(text, normal, hover);
        b.setFont(UITheme.bold(12));
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return b;
    }

    private static void styleCombo(JComboBox<String> cb) {
        cb.setBackground(UITheme.BG_INPUT);
        cb.setForeground(UITheme.TEXT_PRIMARY);
        cb.setFont(UITheme.plain(13));
        cb.setBorder(BorderFactory.createCompoundBorder(
            new UITheme.RoundedBorder(UITheme.BORDER_SUBTLE, 10, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v,
                    int i, boolean sel, boolean foc) {
                super.getListCellRendererComponent(l, v, i, sel, foc);
                setBackground(sel ? UITheme.ACCENT_CYAN.darker() : UITheme.BG_INPUT);
                setForeground(UITheme.TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
    }

    // ── CRUD Logic (unchanged) ────────────────────────────────────────────────
    private Object value(int i) {
        if (inputs[i] instanceof JComboBox<?> cb) {
            Object selected = cb.getSelectedItem();
            if (selected == null) return null;
            String v = selected.toString().trim();
            return v.isEmpty() ? null : v;
        }
        if (inputs[i] instanceof PhotoField pf) {
            String v = pf.getText().trim();
            return v.isEmpty() ? null : v;
        }
        String v = ((JTextField) inputs[i]).getText().trim();
        return v.isEmpty() ? null : v;
    }

    private void load() {
        String query = "SELECT id," + String.join(",", cols) + " FROM " + table
            + " WHERE CONCAT_WS(' ',id," + String.join(",", cols) + ") LIKE ? ORDER BY id DESC";
        String keyword = "%" + search.getText().trim() + "%";
        SwingWorker<Object[][], Void> w = new SwingWorker<>() {
            @Override protected Object[][] doInBackground() {
                try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(query)) {
                    ps.setString(1, keyword);
                    ResultSet rs = ps.executeQuery();
                    java.util.List<Object[]> rows = new java.util.ArrayList<>();
                    while (rs.next()) {
                        Object[] row = new Object[cols.length + 1];
                        for (int i = 0; i < row.length; i++) row[i] = rs.getObject(i + 1);
                        rows.add(row);
                    }
                    return rows.toArray(new Object[0][0]);
                } catch (SQLException ex) {
                    SwingUtilities.invokeLater(() -> error(ex));
                    return new Object[0][0];
                }
            }
            @Override protected void done() {
                try {
                    Object[][] rows = get();
                    model.setRowCount(0);
                    for (Object[] row : rows) model.addRow(row);
                } catch (Exception ex) {
                    error(new SQLException("Failed to load records", ex));
                }
            }
        };
        w.execute();
    }

    private void insert() {
        if (!validateReference()) return;
        String sql = "INSERT INTO " + table + " (" + String.join(",", cols) + ") VALUES ("
            + "?,".repeat(cols.length - 1) + "?)";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < cols.length; i++) ps.setObject(i + 1, value(i));
            ps.executeUpdate();
            clearForm(); load();
        } catch (SQLException ex) { error(ex); }
    }

    private void update() {
        int row = grid.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
        if (!validateReference()) return;
        StringBuilder set = new StringBuilder();
        for (int i = 0; i < cols.length; i++) set.append(i > 0 ? "," : "").append(cols[i]).append("=?");
        String sql = "UPDATE " + table + " SET " + set + " WHERE id=?";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < cols.length; i++) ps.setObject(i + 1, value(i));
            ps.setObject(cols.length + 1, model.getValueAt(row, 0));
            ps.executeUpdate(); load();
        } catch (SQLException ex) { error(ex); }
    }

    /**
     * Checks the references used by the two dependent tables before sending
     * the record to MySQL, so users get an actionable message for an unknown
     * customer or vehicle ID.
     */
    private boolean validateReference() {
        String parentTable;
        String foreignKey;
        String parentName;

        if ("vehicles".equals(table)) {
            parentTable = "customers";
            foreignKey = "customer_id";
            parentName = "customer";
        } else if ("services".equals(table)) {
            parentTable = "vehicles";
            foreignKey = "vehicle_id";
            parentName = "vehicle";
        } else {
            return true;
        }

        int columnIndex = -1;
        for (int i = 0; i < cols.length; i++) {
            if (foreignKey.equals(cols[i])) {
                columnIndex = i;
                break;
            }
        }
        if (columnIndex < 0) return true;

        Object entered = value(columnIndex);
        if (entered == null) {
            JOptionPane.showMessageDialog(this,
                "Enter a " + parentName + " ID first.",
                "Missing reference", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        final int id;
        try {
            id = Integer.parseInt(entered.toString());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "The " + parentName + " ID must be a whole number.",
                "Invalid reference", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String sql = "SELECT 1 FROM " + parentTable + " WHERE id=? LIMIT 1";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return true;
            }
        } catch (SQLException ex) {
            error(ex);
            return false;
        }

        JOptionPane.showMessageDialog(this,
            "No " + parentName + " with ID " + id + " exists.\n"
                + "Add the " + parentName + " first, then use its generated ID.",
            "Invalid reference", JOptionPane.WARNING_MESSAGE);
        return false;
    }

    private void delete() {
        int row = grid.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete this record?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM " + table + " WHERE id=?")) {
            ps.setObject(1, model.getValueAt(row, 0));
            ps.executeUpdate(); clearForm(); load();
        } catch (SQLException ex) { error(ex); }
    }

    private void fillForm() {
        int row = grid.getSelectedRow();
        if (row < 0) return;
        for (int i = 0; i < cols.length; i++) {
            Object v = model.getValueAt(row, i + 1);
            String s = v == null ? "" : v.toString();
            if (inputs[i] instanceof JComboBox<?> cb) cb.setSelectedItem(s);
            else if (inputs[i] instanceof PhotoField pf) pf.setText(s);
            else ((JTextField) inputs[i]).setText(s);
        }
    }

    private void clearForm() {
        grid.clearSelection();
        for (JComponent in : inputs) {
            if (in instanceof JComboBox<?> cb) cb.setSelectedIndex(0);
            else if (in instanceof PhotoField pf) pf.setText("");
            else ((JTextField) in).setText("");
        }
    }

    private void error(SQLException ex) {
        String message = ex.getMessage();
        String lowerMessage = message == null ? "" : message.toLowerCase();
        boolean foreignKeyViolation = "23000".equals(ex.getSQLState())
            && (lowerMessage.contains("foreign key constraint")
                || lowerMessage.contains("cannot add or update a child row"));

        if ("services".equals(table) && foreignKeyViolation
                && lowerMessage.contains("vehicle_id")) {
            message = "The selected vehicle ID does not exist.\n"
                + "Add the vehicle first, then use its generated ID.";
        } else if ("vehicles".equals(table) && foreignKeyViolation
                && lowerMessage.contains("customer_id")) {
            message = "The selected customer ID does not exist.\n"
                + "Add the customer first, then use its generated ID.";
        }
        JOptionPane.showMessageDialog(this, message, "Database error", JOptionPane.ERROR_MESSAGE);
    }

    // ── Photo Field ───────────────────────────────────────────────────────────
    private static class PhotoField extends JPanel {
        private final JTextField textField = UITheme.glowField(14);

        PhotoField() {
            super(new BorderLayout(6, 0));
            setOpaque(false);

            JButton browse = iconBtn("📂", UITheme.BG_INPUT, UITheme.ACCENT_CYAN.darker());
            browse.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            browse.setPreferredSize(new Dimension(42, 36));
            browse.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Images", "jpg","jpeg","png","gif","bmp","webp"));
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();
                    if (f != null) textField.setText(f.getAbsolutePath());
                }
            });
            add(textField, BorderLayout.CENTER);
            add(browse,    BorderLayout.EAST);
        }

        String getText()         { return textField.getText(); }
        void   setText(String v) { textField.setText(v == null ? "" : v); }
    }
}
