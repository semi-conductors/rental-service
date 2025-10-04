package com.rentmate.service.rental.domain.dto;

import com.rentmate.service.rental.domain.enumuration.DeliveryMethode;
import com.rentmate.service.rental.domain.enumuration.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RentalResponseDTO {
    private Long rentalId;
    private Long itemId;
    private BigDecimal rentalPrice;
    private BigDecimal depositAmount;
    private BigDecimal totalPrice;
    private DeliveryMethode deliveryMethode;
    private Status status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Date createdDate;
    private Date lastModifiedDate;
}
