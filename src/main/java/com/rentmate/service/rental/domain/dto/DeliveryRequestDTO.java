package com.rentmate.service.rental.domain.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class DeliveryRequestDTO {
    Long rentalId;
    Long itemId;
    Long ownerId;
    Long renterId;
//    String pickupAddress;
//    String deliveryAddress;
//    String contactInfo;
}
