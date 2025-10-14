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
import com.rentmate.service.rental.event.publisher.RentalEventPublisher;
import com.rentmate.service.rental.repository.RentalRepository;
import com.rentmate.service.rental.service.*;
import com.rentmate.service.rental.service.Validation.RentalValidator;
import com.rentmate.service.rental.shared.exception.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    private final RentalValidator rentalValidator;
    private final RentalEventPublisher rentalEventPublisher;
    private final ItemServiceClient itemServiceClient;
    private final RabbitTemplate rabbitTemplate;
    private final RentalMapper rentalMapper;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public RentalResponseDTO createRental(RentalRequestDTO rentalRequestDTO,Long renterId,  UUID idempotencyKey) {

            Rental rental = rentalMapper.toEntity(rentalRequestDTO,renterId);
            log.info("Creating rental with idempotency key: {}", idempotencyKey);
            rentalValidator.handleIdempotency(idempotencyKey);
            rentalValidator.validateRentalDates(rental,idempotencyKey);
            log.info("Checking availability for item ID: {}", rental.getItemId());
            rentalValidator. validateItemOverlapping(rental.getItemId(),rental.getStartDate(),rental.getEndDate(),idempotencyKey);
            rentalValidator. validateItemAvailability(rental.getItemId(),idempotencyKey);
            log.info("Fetching item details for item ID: {}", rental.getItemId());
            enrichRentalWithItemDetails(rental,idempotencyKey);
            log.info("Saving rental with ID: {}", rental.getId());
            rentalRepo.save(rental);

            log.info("Publishing rental.created event for rental ID: {}", rental.getId());
            // publish event to user service (owner)
            rentalEventPublisher.publishRentalCreatedEvent(rental);
            finalizeIdempotency(idempotencyKey,rental.getId());
            log.info("Rental created successfully with ID: {}", rental.getId());

            return rentalMapper.toDto(rental);

    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public RentalResponseDTO approveRental(Long ownerId,Long rentalId) {
        log.info("Approving rental ID: {} by owner ID: {}", rentalId, ownerId);
         Rental rental= rentalValidator.validateRentalForOwnerDecision(ownerId,rentalId);

        rental.setStatus(Status.Approved);
        rentalRepo.save(rental);
        log.info("Rental ID: {} approved successfully", rentalId);
        rentalEventPublisher.publishDeliveryCostRequestEvent(rental);
        return rentalMapper.toDto(rental);
    }
    @Override
    @Transactional(rollbackOn = Exception.class)
    public RentalResponseDTO rejectRental(Long ownerId,Long rentalId) {
        log.info("Rejecting rental ID: {} by owner ID: {}", rentalId, ownerId);
         Rental rental = rentalValidator.validateRentalForOwnerDecision(ownerId,rentalId);

        rental.setStatus(Status.Rejected);
         rentalRepo.save(rental);
        rabbitTemplate.convertAndSend("rental.exchange", "rental.rejected", Map.of(
                "rentalId", rental.getId(),
                "status", rental.getStatus().name()
        ));

        return rentalMapper.toDto(rental);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void cancelRentalRequest(Long rentalId,Long renterId) {

        Rental  existedRental = findByIdEntity(rentalId);
         if(!Objects.equals(existedRental.getRenterId(),renterId)){
             throw new UnauthorizedAccessException("You are not allowed to cancel this rental");
         }

         if(existedRental.getStatus()!=Status.Pending){
               throw new InvalidOperationException("Cannot cancel an "+existedRental.getStatus().name()+" rental");
         }
         existedRental.setStatus(Status.Cancelled);
         rentalRepo.save(existedRental);
         // send notification
        // in progress
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    @Scheduled(cron = "0 0 * * * *")
    public void triggerReturnsForEndedRentals() {
        LocalDateTime date = LocalDateTime.now();
      List<Rental> rentals = rentalRepo.findByStatusAndEndDateBefore(Status.Delivered,date);
      for (Rental rental:rentals) {
          rentalEventPublisher. publishDeliveryReturnRequestEvent(rental);
      }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    @Scheduled(cron = "0 0 * * * *")
    public void checkForLateReturns() {
        LocalDateTime date = LocalDateTime.now();
        List<Rental> lateRentals = rentalRepo.findByStatusAndEndDateBefore(Status.Delivered,date.minusHours(24));
        for (Rental rental:lateRentals) {
             if(rental.getStatus() == Status.Delivered){
                 rental.setStatus(Status.LateReturning);
                 rentalRepo.save(rental);
                 rentalEventPublisher. publishDeliveryLateReturnEvent(rental);
                 rentalEventPublisher. publishLateReturnPaymentEvent(rental);

             }
        }
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


    private void enrichRentalWithItemDetails(Rental rental,UUID idempotencyKey){
        ItemDetails itemDetails = extractItemDetails(rental.getItemId(),idempotencyKey);
        rental.setOwnerId(itemDetails.getOwnerId());
        BigDecimal dailyRentalPrice = itemDetails.getRentalPrice();
        long rentalDays = ChronoUnit.DAYS.between(rental.getStartDate(), rental.getEndDate())+1;
        rental.setRentalPrice(dailyRentalPrice.multiply(BigDecimal.valueOf(rentalDays)));
        rental.setDepositAmount(rental.getRentalPrice().multiply(DEPOSIT_PERCENTAGE));
        rental.setTotalPrice(rental.getRentalPrice().add(rental.getDepositAmount()));
        rental.setStatus(Status.Pending);

    }
    private void finalizeIdempotency(UUID idempotencyKey, Long rentalId){
        if (idempotencyKey!=null){
            log.info("Attaching rental ID: {} to idempotency key: {}", rentalId, idempotencyKey);
            idempotencyKeyService.attachRentalId(idempotencyKey, rentalId);
            idempotencyKeyService.markAsCompleted(idempotencyKey);
        }

    }


}
