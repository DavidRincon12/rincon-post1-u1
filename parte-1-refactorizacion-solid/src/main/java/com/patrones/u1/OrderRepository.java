package com.patrones.u1;

import java.util.ArrayList;
import java.util.List;

// OrderRepository.java — solo persiste
public class OrderRepository {
    private final List<String> orders = new ArrayList<>();

    public void save(String orderId, double total) {
        orders.add(orderId + ":" + total);
        System.out.println("[DB] Orden guardada: " + orderId);
    }

    public List<String> findAll() {
        return List.copyOf(orders);
    }
}
