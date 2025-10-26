package com.rentmate.service.rental.domain.dto;

import com.rentmate.service.rental.domain.enumuration.PaymentEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private Long rentalId;
    private PaymentEventType eventType;
}
