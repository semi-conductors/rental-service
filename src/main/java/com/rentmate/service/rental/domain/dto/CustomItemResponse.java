package com.rentmate.service.rental.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomItemResponse {
   private String title;
   private Long ownerId;
   private Double rentalPrice;
   private String ownerAddress;
}
