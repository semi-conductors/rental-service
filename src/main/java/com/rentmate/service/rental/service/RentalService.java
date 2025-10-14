package com.rentmate.service.rental.service;

import com.rentmate.service.rental.domain.dto.RentalRequestDTO;
import com.rentmate.service.rental.domain.dto.RentalResponseDTO;
import com.rentmate.service.rental.domain.entity.Rental;
import com.rentmate.service.rental.domain.enumuration.Status;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RentalService {
    RentalResponseDTO createRental(RentalRequestDTO rentalRequestDTO,Long renterId, UUID idempotencyKey);
    RentalResponseDTO approveRental(Long ownerId,Long rentalId);
    RentalResponseDTO rejectRental(Long ownerId,Long rentalId);
    RentalResponseDTO findById(Long rentalId);
    Rental findByIdEntity(Long rentalId);
    List<Rental> findByRenterId(Long renterId);
    List<Rental>  findByOwnerId(Long ownerId);
    List<Rental>  findByStatus(Status status);
    List<Rental> findByRenterIdAndStatus(Long renterId, Status status);
    void cancelRentalRequest(Long rentalId,Long renterId);
    void triggerReturnsForEndedRentals();
    void checkForLateReturns();
}
