package com.patrones.u1;

import java.util.List;

// OrderReporter.java — solo genera reportes
public class OrderReporter {
    public void print(List<String> orders) {
        System.out.println("=== Reporte de Órdenes ===");
        orders.forEach(o -> System.out.println("  " + o));
    }
}
