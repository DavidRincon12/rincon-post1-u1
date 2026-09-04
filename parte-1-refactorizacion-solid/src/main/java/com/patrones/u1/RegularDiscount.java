package com.patrones.u1;

// RegularDiscount.java
public class RegularDiscount implements DiscountStrategy {
    public double apply(double total) {
        return total * 0.95;
    }
}
