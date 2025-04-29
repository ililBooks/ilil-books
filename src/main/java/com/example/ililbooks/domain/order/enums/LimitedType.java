package com.example.ililbooks.domain.order.enums;

public enum LimitedType {
    LIMITED,
    REGULAR;

    public boolean canCancel() {
        return this == REGULAR;
    }
}
