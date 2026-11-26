/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ka
 */
public class Item {
    private String itemId;
    private String name;
    private String category;
    private double price;
    private int quantity;
    private int reorderLevel;

    public Item(String itemId, String name, String category, double price, int quantity, int reorderLevel) {
        this.itemId = itemId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
    }

    public boolean isLowStock() {
        return this.quantity <= this.reorderLevel;
    }

    public void increaseStock(int qty) {
        if (qty > 0) {
            this.quantity += qty;
        }
    }

    public boolean decreaseStock(int qty) {
        if (qty > 0 && this.quantity >= qty) {
            this.quantity -= qty;
            return true;
        }
        return false;
    }

    public double getTotalValue() {
        return this.price * this.quantity;
    }

    // Getters & Setters
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    @Override
    public String toString() {
        return itemId + " - " + name + " (" + category + ") | Price: " + price + " | Qty: " + quantity;
    }
}

 

