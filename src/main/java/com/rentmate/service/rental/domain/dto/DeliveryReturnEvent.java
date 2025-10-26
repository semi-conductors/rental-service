package com.rentmate.service.rental.domain.dto;

import com.rentmate.service.rental.domain.enumuration.DeliveryEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryReturnEvent {
    private DeliveryEventType eventType;
    private Long rentalId;
}
