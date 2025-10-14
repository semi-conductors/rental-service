package com.rentmate.service.rental.repository;

import com.rentmate.service.rental.domain.entity.Rental;
import com.rentmate.service.rental.domain.enumuration.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface RentalRepository extends JpaRepository<Rental,Long> {
    List<Rental> findByRenterId(Long renterId);
    List<Rental>  findByOwnerId(Long ownerId);
    List<Rental>  findByStatus(Status status);
    List<Rental> findByRenterIdAndStatus(Long renterId, Status status);
    List<Rental> findByStatusAndEndDateBefore(Status status,LocalDateTime endDate);

    @Query("""
    SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
    FROM Rental r
    WHERE r.itemId = :itemId
      AND r.startDate <= :requestedEndDate
      AND r.endDate >= :requestedStartDate
      AND r.status NOT IN ('Rejected', 'Cancelled','PaymentFailed')
    """)
    boolean hasOverlappingRentals(Long itemId,
                                  LocalDateTime requestedStartDate,
                                  LocalDateTime requestedEndDate);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Rental r WHERE r.itemId = :itemId AND r.status = 'LateReturning'")
    boolean hasLateReturningRental(Long itemId);
}
