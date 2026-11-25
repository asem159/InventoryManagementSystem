/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ka
 */
import java.util.Date;

public class SaleRecord {
    
    private final int saleId;
    private final int orderId;
    private final Date saleDate;
    private final double totalAmount;

    
    public SaleRecord(int saleId, int orderId, Date saleDate, double totalAmount) {
        this.saleId = saleId;
        this.orderId = orderId;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
    }

    
    public SaleRecord(int saleId, Order order) {
        this.saleId = saleId;
        this.orderId = order.getOrderId();
        this.saleDate = new Date();
        this.totalAmount = order.getTotalAmount();
    }

    public int getSaleId() {
        return saleId;
    }

    public int getOrderId() {
        return orderId;
    }

    public Date getDate() {
        return saleDate;
    }

    public double getTotal() {
        return totalAmount;
    }

    @Override
    public String toString() {
        return "Sale #" + saleId + " | Order #" + orderId + " | Date: " + saleDate + " | Total: " + totalAmount;
    }
}
