
package logic;

import java.util.List;
import model.Item;
import model.SaleRecord;

public class ReportGenerator {
    private final InventoryManager inventoryManager;

    public ReportGenerator(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    //  تقرير حالة المخزون الإجمالية
    public String generateInventorySummary() {
        List<Item> items = inventoryManager.getAllItems();
        int totalUnits = 0;
        double totalValue = 0.0;

        StringBuilder sb = new StringBuilder();
        sb.append("=== INVENTORY SUMMARY REPORT ===\n");
        sb.append(String.format("%-10s | %-20s | %-15s | %-8s | %-10s\n",
                "SKU", "Name", "Category", "Qty", "Price"));
        sb.append("-----------------------------------------------------------------------\n");

        for (Item item : items) {
            totalUnits += item.getQuantity();
            totalValue += item.getQuantity() * item.getPrice();
            sb.append(String.format("%-10s | %-20s | %-15s | %-8d | %-10.2f\n",
                    item.getItemId(),
                    item.getName(),
                    item.getCategory(),
                    item.getQuantity(),
                    item.getPrice()));
        }

        sb.append("-----------------------------------------------------------------------\n");
        sb.append("Total Distinct Items: ").append(items.size()).append("\n");
        sb.append("Total Units in Stock: ").append(totalUnits).append("\n");
        sb.append(String.format("Total Inventory Value: %.2f\n", totalValue));
        return sb.toString();
    }

    //  تقرير النواقص 
    public String generateLowStockReport() {
        List<Item> lowStockItems = inventoryManager.getLowStockItems();
        StringBuilder sb = new StringBuilder();
        sb.append("=== LOW STOCK ALERT REPORT ===\n");

        if (lowStockItems.isEmpty()) {
            sb.append("All items are above reorder levels.\n");
            return sb.toString();
        }

        sb.append(String.format("%-10s | %-20s | %-10s | %-12s\n",
                "SKU", "Name", "CurrentQty", "ReorderLevel"));
        sb.append("-------------------------------------------------------------\n");

        for (Item item : lowStockItems) {
            sb.append(String.format("%-10s | %-20s | %-10d | %-12d\n",
                    item.getItemId(),
                    item.getName(),
                    item.getQuantity(),
                    item.getReorderLevel()));
        }
        return sb.toString();
    }

    //  تقرير المبيعات والإيرادات
    public String generateSalesReport() {
        List<SaleRecord> sales = inventoryManager.getSalesHistory();
        double totalRevenue = 0.0;

        StringBuilder sb = new StringBuilder();
        sb.append("=== SALES REPORT ===\n");

        if (sales.isEmpty()) {
            sb.append("No sales recorded yet.\n");
            return sb.toString();
        }

        for (SaleRecord sale : sales) {
            totalRevenue += sale.getTotal();
            sb.append(sale.toString()).append("\n");
        }

        sb.append("-------------------------------------------------------------\n");
        sb.append("Total Completed Sales: ").append(sales.size()).append("\n");
        sb.append(String.format("Total Revenue: %.2f\n", totalRevenue));
        return sb.toString();
    }
}
