package com.rentmate.service.rental.service;

import com.rentmate.service.rental.domain.entity.IdempotencyKey;
import com.rentmate.service.rental.domain.enumuration.RequestType;

import java.util.UUID;

public interface IdempotencyKeyService {
    boolean isExists(UUID key);
    IdempotencyKey saveKey(UUID key,Long rentalId, RequestType type);
    IdempotencyKey findByIdempotencyKey(UUID key);
    void markAsCompleted(UUID key);
    void markAsFailed(UUID key);
    void attachRentalId(UUID key, Long rentalId);

}
