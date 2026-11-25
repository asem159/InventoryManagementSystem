/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ka
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Order {
    private int orderId;
    private String customerName;
    private Date orderDate;
    private OrderStatus status;
    private List<OrderLine> lines;
    private double totalAmount;

    public Order(int orderId, String customerName) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.orderDate = new Date();
        this.status = OrderStatus.PENDING;
        this.lines = new ArrayList<>();
        this.totalAmount = 0.0;
    }

    public void addLine(OrderLine line) {
        if (line != null) {
            this.lines.add(line);
            calculateTotal();
        }
    }

    public double calculateTotal() {
        double sum = 0.0;
        for (OrderLine line : lines) {
            sum += line.getSubtotal();
        }
        this.totalAmount = sum;
        return this.totalAmount;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    // Getters & Setters لباقي الخصائص
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public OrderStatus getStatus() { return status; }

    public double getTotalAmount() { return totalAmount; }

    @Override
    public String toString() {
        return "Order #" + orderId + " | Customer: " + customerName + " | Status: " + status + " | Total: " + totalAmount;
    }
}
