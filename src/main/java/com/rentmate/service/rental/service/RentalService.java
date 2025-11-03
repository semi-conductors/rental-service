package com.rentmate.service.rental.service;

import com.rentmate.service.rental.domain.dto.PageResponseDTO;
import com.rentmate.service.rental.domain.dto.RentalRequestDTO;
import com.rentmate.service.rental.domain.dto.RentalResponseDTO;
import com.rentmate.service.rental.domain.entity.Rental;
import com.rentmate.service.rental.domain.enumuration.Status;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RentalService {
    RentalResponseDTO createRental(RentalRequestDTO rentalRequestDTO,Long renterId);
    RentalResponseDTO approveRental(Long ownerId,Long rentalId);
    RentalResponseDTO rejectRental(Long ownerId,Long rentalId);
    RentalResponseDTO findById(Long rentalId);
    Rental findByIdEntity(Long rentalId);
    PageResponseDTO<RentalResponseDTO> findByOwnerIdAndStatusIsPending(Long ownerId, int pageNum, int pageSize);
     PageResponseDTO<RentalResponseDTO> findByRenterId(Long renterId, int pageNum, int pageSize);
    List<Rental>  findByOwnerId(Long ownerId);
    List<Rental>  findByStatus(Status status);
    PageResponseDTO<RentalResponseDTO>findByRenterIdAndStatus(Long renterId, Status status,int pageNum, int pageSize);
    void cancelRentalRequest(Long rentalId,Long renterId );
    void triggerReturnsForEndedRentals();
    void checkForLateReturns();
}
