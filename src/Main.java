

import database.DatabaseManager;
import logic.InventoryManager;
import model.*;
public class Main {
    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();
        InventoryManager manager = new InventoryManager(db);

        // 1. تحميل البيانات الموجودة في الداتابيز
        manager.loadFromDB();
        System.out.println("Items loaded from DB: " + manager.getAllItems().size());

        // 2. إضافة منتج جديد (هينشئ التصنيف ويسجل المنتج وحركة IN)
        Item laptop = new Item("SKU-101", "Dell Laptop", "Electronics", 25000.0, 10, 3);
        manager.addItem(laptop);

        // 3. إنشاء طلب وتقديمه للطابور
        Order order = new Order(0, "Ahmed Ali");
        order.addLine(new OrderLine("SKU-101", 2, 25000.0));
        manager.submitOrder(order);
        System.out.println("Submitted Order ID from DB: " + order.getOrderId());

        // 4. معالجة الطلب (هيخصم الكمية ويسجل حركة OUT ويصدر فاتورة في Sales)
        Order processed = manager.processNextOrder();
        System.out.println("Processed Order: " + processed);
        System.out.println("Sales History: " + manager.getSalesHistory());

        db.disconnect();
    }
    
}
