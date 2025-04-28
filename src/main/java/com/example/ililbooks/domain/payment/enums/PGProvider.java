package com.example.ililbooks.domain.payment.enums;

public enum PGProvider {
    KAKAOPAY("kakaopay"),
    TOSS("tosspay"),
    NAVERPAY("naverpay"),
    NICE("nice"),
    KCP("kcp"),
    DANAL("danal"),
    UPLUS("uplus"),
    PAYCO("payco"),
    KG("html5_inicis");

    private final String name;

    PGProvider(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}