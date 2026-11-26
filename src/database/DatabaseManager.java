
package database;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import model.*;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/inventory_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "11442006As#"; // اكتب باسورد MySQL هنا

    private Connection connection;

    public boolean connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
            return true;
        } catch (SQLException e) {
            System.err.println("DB Connection Error: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("DB Disconnect Error: " + e.getMessage());
        }
    }

    public Map<String, Item> loadItems() {
        Map<String, Item> itemsMap = new HashMap<>();
        String sql = "SELECT i.sku, i.name, c.name AS category_name, i.price, i.quantity, i.reorder_level "
                   + "FROM ITEM i JOIN CATEGORY c ON i.category_id = c.category_id";

        if (!connect()) return itemsMap;

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String sku = rs.getString("sku");
                String name = rs.getString("name");
                String category = rs.getString("category_name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                int reorderLevel = rs.getInt("reorder_level");

                itemsMap.put(sku, new Item(sku, name, category, price, quantity, reorderLevel));
            }
        } catch (SQLException e) {
            System.err.println("Error loading items: " + e.getMessage());
        }
        return itemsMap;
    }

    private int getOrCreateCategoryId(String categoryName) throws SQLException {
        String selectSql = "SELECT category_id FROM CATEGORY WHERE name = ?";
        try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
            selectStmt.setString(1, categoryName);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("category_id");
                }
            }
        }

        String insertSql = "INSERT INTO CATEGORY (name) VALUES (?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, categoryName);
            insertStmt.executeUpdate();
            try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to get or create category: " + categoryName);
    }

    private int getInternalItemId(String sku) throws SQLException {
        String sql = "SELECT item_id FROM ITEM WHERE sku = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("item_id");
                }
            }
        }
        throw new SQLException("Item SKU not found in DB: " + sku);
    }

    public void saveItem(Item item) {
        if (!connect()) return;

        String sql = "INSERT INTO ITEM (sku, name, category_id, price, quantity, reorder_level) "
                   + "VALUES (?, ?, ?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE "
                   + "name = VALUES(name), category_id = VALUES(category_id), "
                   + "price = VALUES(price), quantity = VALUES(quantity), reorder_level = VALUES(reorder_level)";

        try {
            int categoryId = getOrCreateCategoryId(item.getCategory());
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, item.getItemId());
                stmt.setString(2, item.getName());
                stmt.setInt(3, categoryId);
                stmt.setDouble(4, item.getPrice());
                stmt.setInt(5, item.getQuantity());
                stmt.setInt(6, item.getReorderLevel());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error saving item: " + e.getMessage());
        }
    }

    public void deleteItem(String sku) {
        if (!connect()) return;

        String sql = "DELETE FROM ITEM WHERE sku = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sku);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting item: " + e.getMessage());
        }
    }

    public void logStockMovement(String sku, String movementType, int changeQty, String note) {
        if (!connect()) return;

        String sql = "INSERT INTO STOCK_MOVEMENT (item_id, change_qty, movement_type, note) VALUES (?, ?, ?, ?)";
        try {
            int internalItemId = getInternalItemId(sku);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, internalItemId);
                stmt.setInt(2, changeQty);
                stmt.setString(3, movementType);
                stmt.setString(4, note);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error logging stock movement: " + e.getMessage());
        }
    }

    public void saveOrder(Order order) {
        if (!connect()) return;

        String orderSql = "INSERT INTO Orders (customer_name, order_date, status, total_amount) VALUES (?, ?, ?, ?)";
        String lineSql = "INSERT INTO Order_Item (quantity, unit_price, order_id, item_id) VALUES (?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement orderStmt = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setString(1, order.getCustomerName());
                orderStmt.setTimestamp(2, new Timestamp(order.getOrderDate().getTime()));
                orderStmt.setString(3, order.getStatus().name());
                orderStmt.setDouble(4, order.getTotalAmount());
                orderStmt.executeUpdate();

                try (ResultSet keys = orderStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        order.setOrderId(keys.getInt(1));
                    }
                }
            }

            try (PreparedStatement lineStmt = connection.prepareStatement(lineSql)) {
                for (OrderLine line : order.getLines()) {
                    int internalItemId = getInternalItemId(line.getItemId());
                    lineStmt.setInt(1, line.getQuantity());
                    lineStmt.setDouble(2, line.getUnitPrice());
                    lineStmt.setInt(3, order.getOrderId());
                    lineStmt.setInt(4, internalItemId);
                    lineStmt.addBatch();
                }
                lineStmt.executeBatch();
            }

            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            System.err.println("Error saving order: " + e.getMessage());
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    public void updateOrderStatus(int orderId, OrderStatus status) {
        if (!connect()) return;

        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
        }
    }

   public int saveSale(SaleRecord sale) {
        if (!connect()) return -1;

        String sql = "INSERT INTO Sales (sale_date, total_amount, order_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, new Timestamp(sale.getDate().getTime()));
            stmt.setDouble(2, sale.getTotal());
            stmt.setInt(3, sale.getOrderId());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1); // إرجاع رقم الفاتورة الحقيقي من الداتابيز
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving sale: " + e.getMessage());
        }
        return -1;
    }
}
