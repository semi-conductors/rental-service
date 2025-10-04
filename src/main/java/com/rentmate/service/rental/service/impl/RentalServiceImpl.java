package com.rentmate.service.rental.service.impl;

import com.rentmate.service.rental.client.ItemServiceClient;
import com.rentmate.service.rental.domain.Mapper.RentalMapper;
import com.rentmate.service.rental.domain.dto.CustomItemResponse;
import com.rentmate.service.rental.domain.dto.ItemDetails;
import com.rentmate.service.rental.domain.dto.RentalRequestDTO;
import com.rentmate.service.rental.domain.dto.RentalResponseDTO;
import com.rentmate.service.rental.domain.entity.Rental;
import com.rentmate.service.rental.domain.enumuration.RequestType;
import com.rentmate.service.rental.domain.enumuration.Status;
import com.rentmate.service.rental.repository.RentalRepository;
import com.rentmate.service.rental.service.*;
import com.rentmate.service.rental.shared.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Log4j2
public class RentalServiceImpl implements RentalService {
    private static final BigDecimal DEPOSIT_PERCENTAGE = new BigDecimal("0.15");
    private final RentalRepository rentalRepo;
    private final IdempotencyKeyService idempotencyKeyService;
    private final ItemServiceClient itemServiceClient;
    private final RabbitTemplate rabbitTemplate;
    private final RentalMapper rentalMapper;

    @Override
    @Transactional
    public RentalResponseDTO createRental(RentalRequestDTO rentalRequestDTO,Long renterId,  UUID idempotencyKey) {

        Rental rental = rentalMapper.toEntity(rentalRequestDTO,renterId);
        log.info("Creating rental with idempotency key: {}", idempotencyKey);
             handleIdempotency(idempotencyKey);
             validateRentalDates(rental,idempotencyKey);
            log.info("Checking availability for item ID: {}", rental.getItemId());
             validateItemAvailability(rental,idempotencyKey);
            log.info("Fetching item details for item ID: {}", rental.getItemId());
             enrichRentalWithItemDetails(rental,idempotencyKey);
            log.info("Saving rental with ID: {}", rental.getId());
            rentalRepo.save(rental);

            log.info("Publishing rental.created event for rental ID: {}", rental.getId());
            // publish event to user service (owner)
             publishRentalCreatedEvent(rental);
             finalizeIdempotency(idempotencyKey,rental.getId());
            log.info("Rental created successfully with ID: {}", rental.getId());

            return rentalMapper.toDto(rental);

    }

    @Override
    @Transactional
    public RentalResponseDTO approveRental(Long ownerId,Long rentalId) {
        log.info("Approving rental ID: {} by owner ID: {}", rentalId, ownerId);
         Rental rental=validateRentalForOwnerDecision(ownerId,rentalId);

        rental.setStatus(Status.Approved);
        rentalRepo.save(rental);
        log.info("Rental ID: {} approved successfully", rentalId);
        //  Publish event for PaymentService to handle payment
        Map<String,Object> paymentEventPayLoad = Map.of(
                "rentalId", rental.getId(),
                "totalPrice", rental.getTotalPrice(),
                "depositAmount",rental.getDepositAmount()
        );
        rabbitTemplate.convertAndSend("rental.exchange","rental.approved",paymentEventPayLoad);

            return rentalMapper.toDto(rental);
    }
    @Override
    @Transactional
    public RentalResponseDTO rejectRental(Long ownerId,Long rentalId) {
        log.info("Rejecting rental ID: {} by owner ID: {}", rentalId, ownerId);
         Rental rental = validateRentalForOwnerDecision(ownerId,rentalId);

        rental.setStatus(Status.Rejected);
         rentalRepo.save(rental);
        rabbitTemplate.convertAndSend("rental.exchange", "rental.rejected", Map.of(
                "rentalId", rental.getId(),
                "status", rental.getStatus().name()
        ));

        return rentalMapper.toDto(rental);
    }

    @Override
    @Transactional
    public void cancelRentalRequest(Long rentalId,Long renterId) {

        Rental  existedRental = findByIdEntity(rentalId);
         if(!Objects.equals(existedRental.getRenterId(),renterId)){
             throw new UnauthorizedAccessException("You are not allowed to cancel this rental");
         }
        if(existedRental.getStatus()==Status.Cancelled){
            throw new InvalidOperationException("Rental already cancelled");
        }

         if(existedRental.getStatus()==Status.Approved){
               throw new InvalidOperationException("Cannot cancel an approved rental");
         }
         existedRental.setStatus(Status.Cancelled);
         rentalRepo.save(existedRental);
         // send notification
        // in progress
    }


    @Override
    public Rental findByIdEntity(Long rentalId) {
        return  rentalRepo.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Rental Not Found With Id: "+rentalId));
    }
    @Override
    public RentalResponseDTO findById(Long rentalId) {
        return rentalMapper.toDto(findByIdEntity(rentalId));
    }

    @Override
    public List<Rental> findByRenterId(Long renterId) {
        return rentalRepo.findByRenterId(renterId);
    }

    @Override
    public List<Rental> findByOwnerId(Long ownerId) {
        return rentalRepo.findByOwnerId(ownerId);
    }

    @Override
    public List<Rental> findByStatus(Status status) {
        return rentalRepo.findByStatus(status);
    }

    @Override
    public List<Rental> findByRenterIdAndStatus(Long renterId, Status status) {
        return  rentalRepo.findByRenterIdAndStatus(renterId,status);
    }

    private ItemDetails extractItemDetails(Long itemId,UUID idempotencyKey) {
            CustomItemResponse itemResponse = itemServiceClient.getItemById(itemId);
            if (itemResponse == null) {
                idempotencyKeyService.markAsFailed(idempotencyKey);
                throw new NotFoundException("Item not found with ID: " + itemId);
            }
                ItemDetails itemDetails = new ItemDetails();
                itemDetails.setOwnerId(itemResponse.getOwnerId());
                itemDetails.setRentalPrice(itemResponse.getRentalPrice());
                return itemDetails;
    }
    private void handleIdempotency(UUID idempotencyKey){
        if(idempotencyKey!=null && idempotencyKeyService.isExists(idempotencyKey)){
            log.error("Duplicate request with idempotency key: {}", idempotencyKey);
            throw new DuplicateRequestException("this request already exist");
        }
        if (idempotencyKey != null) {
            log.info("Saving idempotency key: {}", idempotencyKey);
            idempotencyKeyService.saveKey(idempotencyKey,null, RequestType.Booking);
        }

    }
    private void validateRentalDates(Rental rental, UUID idempotencyKey){
        if(rental.getEndDate().isBefore(rental.getStartDate())){
            log.error("Invalid dates: endDate {} is before startDate {}", rental.getEndDate(), rental.getStartDate());
            idempotencyKeyService.markAsFailed(idempotencyKey);
            throw new IllegalArgumentException("End date must be after start date");
        }

    }
    private void validateItemAvailability(Rental rental, UUID idempotencyKey){
        if(!itemServiceClient.isAvailable(rental.getItemId(),rental.getStartDate(),rental.getEndDate())){
            log.error("Item ID: {} is not available", rental.getItemId());
            idempotencyKeyService.markAsFailed(idempotencyKey);
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Item not available");
        }
    }
    private void enrichRentalWithItemDetails(Rental rental,UUID idempotencyKey){
        ItemDetails itemDetails = extractItemDetails(rental.getItemId(),idempotencyKey);
        rental.setOwnerId(itemDetails.getOwnerId());
        rental.setRentalPrice(itemDetails.getRentalPrice());
        rental.setDepositAmount(rental.getRentalPrice().multiply(DEPOSIT_PERCENTAGE));
        rental.setTotalPrice(rental.getRentalPrice().add(rental.getDepositAmount()));
        rental.setStatus(Status.Pending);

    }
    private void publishRentalCreatedEvent(Rental rental){
        Map<String, Object> eventPayload = Map.of(
                "rentalId", rental.getId(),
                "ownerId", rental.getOwnerId(),
                "renterId", rental.getRenterId(),
                "itemId", rental.getItemId(),
                "status", rental.getStatus().name()
        );
        rabbitTemplate.convertAndSend("rental.exchange", "rental.created", eventPayload);

    }
    private void finalizeIdempotency(UUID idempotencyKey, Long rentalId){
        if (idempotencyKey!=null){
            log.info("Attaching rental ID: {} to idempotency key: {}", rentalId, idempotencyKey);
            idempotencyKeyService.attachRentalId(idempotencyKey, rentalId);
            idempotencyKeyService.markAsCompleted(idempotencyKey);
        }

    }
    private Rental validateRentalForOwnerDecision(Long ownerId, Long rentalId){
        var rental = findByIdEntity(rentalId);

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
