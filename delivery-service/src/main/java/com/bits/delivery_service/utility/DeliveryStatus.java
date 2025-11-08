package com.bits.delivery_service.utility;

public enum DeliveryStatus {
    CREATED("CREATED"),
    ASSIGNED("ASSIGNED"),
    IN_TRANSIT("IN_TRANSIT"),
    DELIVERED("DELIVERED");

    DeliveryStatus(String created) {
    }
}
