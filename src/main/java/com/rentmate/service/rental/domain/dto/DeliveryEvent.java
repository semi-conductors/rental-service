package com.rentmate.service.rental.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEvent {
    private String eventType;
    private Long rentalId;
    private BigDecimal deliveryCost;
}
