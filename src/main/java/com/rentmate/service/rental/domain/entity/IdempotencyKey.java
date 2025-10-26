package com.rentmate.service.rental.domain.entity;

import com.rentmate.service.rental.domain.enumuration.KeyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "IdempotencyKeys")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class IdempotencyKey {
    @Id
    @Column(unique = true,nullable = false)
    private UUID idempotencyKey;

    private Long rentalId;

    @Enumerated(EnumType.STRING)
    private KeyStatus status;

    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;

}
