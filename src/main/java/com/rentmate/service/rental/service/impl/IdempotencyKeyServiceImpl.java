package com.rentmate.service.rental.service.impl;

import com.rentmate.service.rental.domain.entity.IdempotencyKey;
import com.rentmate.service.rental.domain.enumuration.KeyStatus;
import com.rentmate.service.rental.domain.enumuration.RequestType;
import com.rentmate.service.rental.repository.IdempotencyKeyRepository;
import com.rentmate.service.rental.service.IdempotencyKeyService;
import com.rentmate.service.rental.shared.exception.ExpiredKeyException;
import com.rentmate.service.rental.shared.exception.InvalidStatusTransitionException;
import com.rentmate.service.rental.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class IdempotencyKeyServiceImpl implements IdempotencyKeyService {
    private final IdempotencyKeyRepository idempotencyKeyRepo;
    @Override
    public boolean isExists(UUID key) {
        return  idempotencyKeyRepo.existsByIdempotencyKey(key);
    }
    @Override
    public IdempotencyKey saveKey(UUID key,Long rentalId, RequestType type) {
        return idempotencyKeyRepo.findByIdempotencyKey(key)
                .orElseGet(()->{
                    IdempotencyKey newKey = new IdempotencyKey();
                    newKey.setIdempotencyKey(key);
                    newKey.setRentalId(rentalId);
                    newKey.setStatus(KeyStatus.pending);
                    newKey.setRequestType(type);
                    newKey.setExpiredAt(LocalDateTime.now().plusHours(1));

                   return idempotencyKeyRepo.save(newKey);
                });
    }
    @Override
    public IdempotencyKey findByIdempotencyKey(UUID key){
       IdempotencyKey keyEntity= idempotencyKeyRepo.findByIdempotencyKey(key)
               .orElseThrow(()->new NotFoundException("idempotencyKeyNotFound"));
       if(keyEntity.getExpiredAt().isBefore(LocalDateTime.now())){
           throw new ExpiredKeyException("Idempotency key has expired");
       }
       return keyEntity;
    }

    @Override
    public void markAsCompleted(UUID key) {
        IdempotencyKey existedKey =  findByIdempotencyKey(key);
        if(existedKey.getStatus() != KeyStatus.pending) {
            throw new InvalidStatusTransitionException("Only pending keys can be marked as completed");
        }
        existedKey.setStatus(KeyStatus.completed);
        idempotencyKeyRepo.save(existedKey);

    }

    @Override
    public void markAsFailed(UUID key) {
        IdempotencyKey existedKey =  findByIdempotencyKey(key);
        if(existedKey.getStatus() != KeyStatus.pending) {
            throw new InvalidStatusTransitionException("Only pending keys can be marked as failed");
        }
        existedKey.setStatus(KeyStatus.failed);
        idempotencyKeyRepo.save(existedKey);

    }

    @Override
    public void attachRentalId(UUID key, Long rentalId) {
        IdempotencyKey existedKey = findByIdempotencyKey(key);
        existedKey.setRentalId(rentalId);
        idempotencyKeyRepo.save(existedKey);
    }
}
