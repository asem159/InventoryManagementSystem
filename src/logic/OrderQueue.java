/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logic;

/**
 *
 * @author ka
 */
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue; 
import model.Order;// can use order class


public class OrderQueue {
    private final Queue<Order> queue;

    public OrderQueue() {
        this.queue = new LinkedList<>();
    }
    //Encapsulation(private final Queue<Order> queue;)
    public void enqueue(Order order) {
        if (order != null) {
            queue.offer(order);
        }
    }

    public Order dequeue() {
        return queue.poll();
    }

    public Order peek() {
        return queue.peek();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }

    public List<Order> getAll() {
        return new ArrayList<>(queue);
    }
    
}
