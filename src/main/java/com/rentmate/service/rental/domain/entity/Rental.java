package com.rentmate.service.rental.domain.entity;

import com.rentmate.service.rental.domain.enumuration.Status;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
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
    @Column(nullable = false)
    private Long itemId;
    @Column(nullable = false)
    private  Long ownerId;
    @Column(nullable = false)
    private Long renterId;
    private BigDecimal rentalPrice;
    private BigDecimal depositAmount;
    private BigDecimal totalPrice;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(nullable = false)
    private LocalDateTime startDate;
    @Column(nullable = false)
    private LocalDateTime  endDate;
    @CreatedDate
    private Date createdDate;
    @LastModifiedDate
    private Date lastModifiedDate;
    private String ownerAddress;
    private String renterAddress;
}
