package com.rentmate.service.rental.repository;

import com.rentmate.service.rental.domain.entity.IdempotencyKey;
import com.rentmate.service.rental.domain.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey,UUID> {
    Optional<IdempotencyKey> findByIdempotencyKey(UUID key);
    boolean existsByIdempotencyKey(UUID key);
    @Modifying
    @Query("DELETE FROM IdempotencyKey k WHERE k.expiredAt < :dateTime")
    int deleteByExpiredAtBefore(@Param("dateTime") LocalDateTime dateTime);

}
