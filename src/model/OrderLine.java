/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ka
 */
public class OrderLine {
    
    private String itemId;
    private int quantity;
    private double unitPrice;

    public OrderLine(String itemId, int quantity, double unitPrice) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public double getSubtotal() {
        return this.quantity * this.unitPrice;
    }

    public String getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    @Override
    public String toString() {
        return "Item: " + itemId + " | Qty: " + quantity + " | Unit Price: " + unitPrice + " | Subtotal: " + getSubtotal();
    }
    
    
}
