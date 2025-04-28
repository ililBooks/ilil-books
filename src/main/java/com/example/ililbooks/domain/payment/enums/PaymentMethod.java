package com.example.ililbooks.domain.payment.enums;

public enum PaymentMethod {
    CARD("card"),
    VBANK("vbank"),
    TRANS("trans"),
    PHONE("phone");

    private final String name;

    PaymentMethod(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
