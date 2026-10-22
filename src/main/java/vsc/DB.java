package vsc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Database connection. Change USER / PASS to match your MySQL setup. */
public class DB {
    private static final String URL = "jdbc:mysql://localhost:3306/vehicle_service_center";
    private static final String USER = "root";
    private static final String PASS = "sudarshan@1823";

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void ensureVehiclePhotoColumn() throws SQLException {
        try (Connection c = get();
             ResultSet rs = c.getMetaData().getColumns(null, null, "vehicles", "photo")) {
            if (!rs.next()) {
                try (Statement st = c.createStatement()) {
                    st.execute("ALTER TABLE vehicles ADD COLUMN photo VARCHAR(255) DEFAULT NULL");
                }
            }
        }
    }

    public static void seedDemoData() throws SQLException {
            try (Connection c = get()) {
                c.setAutoCommit(false);
                try {
                    int customerId = findOrInsertCustomer(c, "Arun Kumar", "9876543210",
                        "arun.kumar@example.com", "Chennai");
                    int vehicleId = findOrInsertVehicle(c, customerId, "TN09AB1234",
                        "Toyota", "Innova", 2022);
                    insertDemoService(c, vehicleId, "Full Service",
                        "Engine oil and general inspection", "2026-10-16", "2500.00");

                    int secondCustomerId = findOrInsertCustomer(c, "Priya Sharma", "9123456780",
                        "priya.sharma@example.com", "Bengaluru");
                    int secondVehicleId = findOrInsertVehicle(c, secondCustomerId, "KA01CD5678",
                        "Honda", "City", 2021);
                    insertDemoService(c, secondVehicleId, "Brake Inspection",
                        "Brake pad and fluid inspection", "2026-10-15", "1200.00");
                    c.commit();
                } catch (SQLException ex) {
                    c.rollback();
                    throw ex;
                } finally {
                    c.setAutoCommit(true);
                }
            }
        }

    private static int findOrInsertCustomer(Connection c, String name, String phone,
                String email, String address) throws SQLException {
            String find = "SELECT id FROM customers WHERE phone=? LIMIT 1";
            try (PreparedStatement ps = c.prepareStatement(find)) {
                ps.setString(1, phone);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
            String insert = "INSERT INTO customers (name,phone,email,address) VALUES (?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, phone);
                ps.setString(3, email);
                ps.setString(4, address);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        }

    private static int findOrInsertVehicle(Connection c, int customerId, String regNo,
                String make, String model, int year) throws SQLException {
            String find = "SELECT id FROM vehicles WHERE reg_no=? LIMIT 1";
            try (PreparedStatement ps = c.prepareStatement(find)) {
                ps.setString(1, regNo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
            String insert = "INSERT INTO vehicles (customer_id,reg_no,make,model,year) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, customerId);
                ps.setString(2, regNo);
                ps.setString(3, make);
                ps.setString(4, model);
                ps.setInt(5, year);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        }

    private static void insertDemoService(Connection c, int vehicleId, String type,
                String description, String date, String cost) throws SQLException {
            String sql = "INSERT INTO services "
                + "(vehicle_id,service_type,description,service_date,cost,status) "
                + "SELECT ?,?,?,?,?,? WHERE NOT EXISTS "
                + "(SELECT 1 FROM services WHERE vehicle_id=? AND service_type=? AND service_date=?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, vehicleId);
                ps.setString(2, type);
                ps.setString(3, description);
                ps.setString(4, date);
                ps.setBigDecimal(5, new java.math.BigDecimal(cost));
                ps.setString(6, "Pending");
                ps.setInt(7, vehicleId);
                ps.setString(8, type);
                ps.setString(9, date);
                ps.executeUpdate();
        }
    }
}
