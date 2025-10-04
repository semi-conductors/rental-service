package com.rentmate.service.rental.domain.entity;

import com.rentmate.service.rental.domain.enumuration.DeliveryMethode;
import com.rentmate.service.rental.domain.enumuration.Status;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
@Entity
@Table(name = "rentals")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long itemId;
    private  Long ownerId;
    private Long renterId;
    private BigDecimal rentalPrice;
    private BigDecimal depositAmount;
    private BigDecimal totalPrice;
    @Enumerated(EnumType.STRING)
    private DeliveryMethode deliveryMethode;
    @Enumerated(EnumType.STRING)
    private Status status;
    private LocalDateTime startDate;
    private LocalDateTime  endDate;
    @CreatedDate
    private Date createdDate;
    @LastModifiedDate
    private Date lastModifiedDate;
}
