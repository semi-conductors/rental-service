package com.rentmate.service.rental.repository;

import com.rentmate.service.rental.domain.entity.IdempotencyKey;
import com.rentmate.service.rental.domain.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey,Long> {
    Optional<IdempotencyKey> findByIdempotencyKey(UUID key);
    boolean existsByIdempotencyKey(UUID key);



}
