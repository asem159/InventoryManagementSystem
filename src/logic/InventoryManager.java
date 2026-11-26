/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logic;

import database.DatabaseManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.*;
import java.util.Date;

public class InventoryManager {
   private final Map<String, Item> items;
    private final OrderQueue orderQueue;
    private final List<SaleRecord> salesHistory; //توليد التقارير
    private final DatabaseManager dbManager;
   // private int nextSaleId; // test before connect DB

    public InventoryManager(DatabaseManager dbManager) {
        this.items = new HashMap<>();
        this.orderQueue = new OrderQueue();
        this.salesHistory = new ArrayList<>();
        this.dbManager = dbManager;
       // this.nextSaleId = 1;
    }

    //   download DB in initi
    public void loadFromDB() {
        if (dbManager != null && dbManager.connect()) {
            Map<String, Item> loaded = dbManager.loadItems();
            if (loaded != null) {
                items.clear();
                items.putAll(loaded);
            }
        }
    }

    //   إدارة المنتجات  CRUD
    public void addItem(Item item) {
        if (item != null && !items.containsKey(item.getItemId())) {
            items.put(item.getItemId(), item);
            if (dbManager != null) {
                dbManager.saveItem(item);
                dbManager.logStockMovement(item.getItemId(), "IN", item.getQuantity(), "Initial stock");
            }
        }
    }

    public void updateItem(Item item) {
        if (item != null && items.containsKey(item.getItemId())) {
            items.put(item.getItemId(), item);
            if (dbManager != null) {
                dbManager.saveItem(item);
            }
        }
    }

    public boolean deleteItem(String itemId) {
        if (items.remove(itemId) != null) {
            if (dbManager != null) {
                dbManager.deleteItem(itemId);
            }
            return true;
        }
        return false;
    }

    public Item findItemById(String itemId) {
        return items.get(itemId);
    }

    public List<Item> getAllItems() {
        return new ArrayList<>(items.values());
    }

    public List<Item> getLowStockItems() {
        List<Item> lowStock = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.isLowStock()) {
                lowStock.add(item);
            }
        }
        return lowStock;
    }

    //  إدارة الطلبات والطابور 
    public void submitOrder(Order order) {
        if (order != null && !order.getLines().isEmpty()) {
            order.setStatus(OrderStatus.PENDING);
            orderQueue.enqueue(order);
            if (dbManager != null) {
                dbManager.saveOrder(order);
            }
        }
    }

    public Order processNextOrder() {
        if (orderQueue.isEmpty()) {
            return null;
        }

        Order currentOrder = orderQueue.dequeue();
        currentOrder.setStatus(OrderStatus.PROCESSING);

        // المرحلة الأولى: التحقق من توافر كل الأصناف قبل خصم أي قطعة
        for (OrderLine line : currentOrder.getLines()) {
            Item item = items.get(line.getItemId());
            if (item == null || item.getQuantity() < line.getQuantity()) {
                currentOrder.setStatus(OrderStatus.CANCELLED);
                if (dbManager != null) {
                    dbManager.updateOrderStatus(currentOrder.getOrderId(), OrderStatus.CANCELLED);
                }
                return currentOrder;
            }
        }

        // المرحلة الثانية: تنفيذ الخصم وتحديث الداتابيز وتسجيل حركة المخزون
        for (OrderLine line : currentOrder.getLines()) {
            Item item = items.get(line.getItemId());
            item.decreaseStock(line.getQuantity());
            if (dbManager != null) {
                dbManager.saveItem(item);
                dbManager.logStockMovement(
                    item.getItemId(),
                    "OUT",
                    line.getQuantity(),
                    "Order #" + currentOrder.getOrderId()
                );
            }
        }

        // المرحلة الثالثة: إتمام الطلب وإصدار فاتورة بيع
       // المرحلة الثالثة: إتمام الطلب وإصدار فاتورة بيع
        currentOrder.setStatus(OrderStatus.COMPLETED);
        Date saleDate = new Date();
        int generatedSaleId = salesHistory.size() + 1;

        if (dbManager != null) {
            dbManager.updateOrderStatus(currentOrder.getOrderId(), OrderStatus.COMPLETED);
            SaleRecord tempSale = new SaleRecord(0, currentOrder.getOrderId(), saleDate, currentOrder.getTotalAmount());
            int dbSaleId = dbManager.saveSale(tempSale);
            if (dbSaleId != -1) {
                generatedSaleId = dbSaleId;
            }
        }

        SaleRecord sale = new SaleRecord(
            generatedSaleId,
            currentOrder.getOrderId(),
            saleDate,
            currentOrder.getTotalAmount()
        );
        salesHistory.add(sale);

        return currentOrder;

        
    }

    //  الوصول للطابور وسجل المبيعات 
    public OrderQueue getOrderQueue() {
        return orderQueue;
    }

    public List<SaleRecord> getSalesHistory() {
        return new ArrayList<>(salesHistory);
    }
}
