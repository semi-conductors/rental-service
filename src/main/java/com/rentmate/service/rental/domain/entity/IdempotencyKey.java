package com.rentmate.service.rental.domain.entity;

import com.rentmate.service.rental.domain.enumuration.KeyStatus;
import com.rentmate.service.rental.domain.enumuration.RequestType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.xml.crypto.KeySelector;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
@Entity
@Table(name = "IdempotencyKeys")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class IdempotencyKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private UUID idempotencyKey;
    private Long rentalId;
    @Enumerated(EnumType.STRING)
    private KeyStatus status;
    @Enumerated(EnumType.STRING)
    private RequestType requestType;
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;

}
