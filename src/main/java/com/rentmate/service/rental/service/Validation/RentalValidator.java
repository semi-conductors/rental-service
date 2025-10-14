package com.rentmate.service.rental.service.Validation;

import com.rentmate.service.rental.client.ItemServiceClient;
import com.rentmate.service.rental.domain.entity.Rental;
import com.rentmate.service.rental.domain.enumuration.RequestType;
import com.rentmate.service.rental.domain.enumuration.Status;
import com.rentmate.service.rental.repository.RentalRepository;
import com.rentmate.service.rental.service.IdempotencyKeyService;
import com.rentmate.service.rental.shared.exception.DuplicateRequestException;
import com.rentmate.service.rental.shared.exception.InvalidStatusTransitionException;
import com.rentmate.service.rental.shared.exception.NotFoundException;
import com.rentmate.service.rental.shared.exception.UnauthorizedAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Log4j2
public class RentalValidator {
    private final RentalRepository rentalRepo;
    private final IdempotencyKeyService idempotencyKeyService;
    private final ItemServiceClient itemServiceClient;

    public void handleIdempotency(UUID idempotencyKey){
        if(idempotencyKey!=null && idempotencyKeyService.isExists(idempotencyKey)){
            log.error("Duplicate request with idempotency key: {}", idempotencyKey);
            throw new DuplicateRequestException("this request already exist");
        }
        if (idempotencyKey != null) {
            log.info("Saving idempotency key: {}", idempotencyKey);
            idempotencyKeyService.saveKey(idempotencyKey,null, RequestType.Booking);
        }

    }
    public void validateRentalDates(Rental rental, UUID idempotencyKey){
        if(rental.getEndDate().isBefore(rental.getStartDate())){
            log.error("Invalid dates: endDate {} is before startDate {}", rental.getEndDate(), rental.getStartDate());
            idempotencyKeyService.markAsFailed(idempotencyKey);
            throw new IllegalArgumentException("End date must be after start date");
        }

    }
    public void validateItemOverlapping(Long itemId,
                                         LocalDateTime startDate,
                                         LocalDateTime endDate,UUID idempotencyKey){
        boolean isOverlapping= rentalRepo.hasOverlappingRentals(itemId,startDate,endDate);
        boolean isLateReturn = rentalRepo.hasLateReturningRental(itemId);
        if(isOverlapping || isLateReturn){
            idempotencyKeyService.markAsFailed(idempotencyKey);
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Item is not available during the requested period");
        }

    }
    public void validateItemAvailability(Long itemId, UUID idempotencyKey){
        if(!itemServiceClient.isAvailable(itemId)){
            log.error("Item ID: {} is not available", itemId);
            idempotencyKeyService.markAsFailed(idempotencyKey);
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Item not available");
        }
    }
    public Rental validateRentalForOwnerDecision(Long ownerId, Long rentalId){
        var rental =  rentalRepo.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Rental Not Found With Id: "+rentalId));

        if (!Objects.equals(rental.getOwnerId(), ownerId)) {
            log.error("Unauthorized access attempt by owner ID: {} for rental ID: {}", ownerId, rentalId);
            throw new UnauthorizedAccessException("This owner is not authorized");
        }

        if (rental.getStatus() != Status.Pending) {
            log.error("Invalid status transition for rental ID: {}. Current status: {}", rentalId, rental.getStatus());
            throw new InvalidStatusTransitionException("Rental is not pending, cannot approve.");
        }
        return rental;
    }
}
