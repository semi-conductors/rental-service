package com.rentmate.service.rental.repository;

import com.rentmate.service.rental.domain.entity.Rental;
import com.rentmate.service.rental.domain.enumuration.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface RentalRepository extends JpaRepository<Rental,Long> {
     List<Rental> findByRenterId(Long renterId);
    List<Rental>  findByOwnerId(Long ownerId);
    List<Rental>  findByStatus(Status status);
    List<Rental> findByRenterIdAndStatus(Long renterId, Status status);
}
