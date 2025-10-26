package com.rentmate.service.rental.domain.enumuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeliveryEventType {
    DELIVERY_COST("delivery.deliveryCost"),
    DELIVERED("delivery.delivered"),
    DELIVERY_RETURNED("delivery.returned"),
    DELIVERY_IN_RETURNING("delivery.inReturning");

    @JsonValue
    private final String value;

    @JsonCreator
    public static DeliveryEventType fromValue(String value){
        for(DeliveryEventType type:values()){
            if(type.getValue().equalsIgnoreCase(value)){
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown delivery event type: " + value);
    }
}
