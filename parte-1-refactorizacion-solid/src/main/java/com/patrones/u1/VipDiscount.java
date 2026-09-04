package com.patrones.u1;

// VipDiscount.java
public class VipDiscount implements DiscountStrategy {
    public double apply(double total) {
        return total * 0.85;
    }
}
