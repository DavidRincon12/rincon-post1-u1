package com.patrones.u1;

// NoDiscount.java
public class NoDiscount implements DiscountStrategy {
    public double apply(double total) {
        return total;
    }
}
