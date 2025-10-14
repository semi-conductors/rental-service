package com.rentmate.service.rental.controller;


import com.rentmate.service.rental.domain.dto.RentalRequestDTO;
import com.rentmate.service.rental.domain.dto.RentalResponseDTO;
import com.rentmate.service.rental.service.RentalService;
import com.rentmate.service.rental.shared.utility.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;
    @PostMapping()
    public ResponseEntity<RentalResponseDTO> createRental(@Valid @RequestBody RentalRequestDTO rentalRequestDTO,
                                                                 @RequestHeader("Idempotency-Key") UUID idemKey,
                                                          HttpServletRequest request){
        Long renterId =JwtUtils.getExtractedId(request);
        RentalResponseDTO responseDTO  = rentalService.createRental(rentalRequestDTO,renterId,idemKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PatchMapping("/{id}/approve")
    @ResponseStatus(HttpStatus.OK)
    public RentalResponseDTO approveRental(@PathVariable Long id,HttpServletRequest request){
        Long ownerId =JwtUtils.getExtractedId(request);
        return rentalService.approveRental(ownerId,id);

    }
    @PatchMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.OK)
    public RentalResponseDTO rejectRental(@PathVariable Long id,HttpServletRequest request){
        Long ownerId =JwtUtils.getExtractedId(request);
        return rentalService.rejectRental(ownerId,id);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RentalResponseDTO getRentalById(@PathVariable Long id){
       return rentalService.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelRental(@PathVariable Long id,HttpServletRequest request){
        Long renterId =JwtUtils.getExtractedId(request);
        rentalService.cancelRentalRequest(id,renterId);
    }

}
