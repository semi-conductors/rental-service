package com.rentmate.service.rental.service.Validation;

import com.rentmate.service.rental.client.ItemServiceClient;
import com.rentmate.service.rental.domain.entity.Rental;
import com.rentmate.service.rental.domain.enumuration.Status;
import com.rentmate.service.rental.repository.RentalRepository;
import com.rentmate.service.rental.shared.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Log4j2
public class RentalValidator {
    private final RentalRepository rentalRepo;
    private final ItemServiceClient itemServiceClient;

    public void checkDuplicateRequest(Long itemId,Long renterId){
        if(rentalRepo.existsByItemIdAndRenterIdAndStatus(itemId,renterId,Status.Pending)){
            throw new DuplicateRequestException("Rental already pending for this item.");
        }
    }

    public void validateRentalDates(Rental rental){

        if(!rental.getEndDate().isAfter(rental.getStartDate())){
            log.error("Invalid dates: endDate {} is before startDate {}", rental.getEndDate(), rental.getStartDate());
            throw new InvalidRentalPeriodException("End date must be after start date");
        }
    }
    public void validateItemOverlapping(Long itemId,
                                         LocalDateTime startDate,LocalDateTime endDate,
                                          int bufferHours){

        LocalDateTime startWithBuffer = startDate.minusHours(bufferHours);
        LocalDateTime endWithBuffer = endDate.plusHours(bufferHours);

        boolean isOverlapping= rentalRepo.hasOverlappingRentals(itemId,startWithBuffer,endWithBuffer);
        boolean isLateReturn = rentalRepo.hasLateReturningRental(itemId);
        if(isOverlapping || isLateReturn){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Item is not available during the requested period");
        }
    }

    public void validateItemAvailability(Long itemId){
        if(!itemServiceClient.isItemAvailable(itemId)){
            log.error("Item ID: {} is not available", itemId);
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
