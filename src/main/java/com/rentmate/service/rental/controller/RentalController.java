package com.rentmate.service.rental.controller;


import com.rentmate.service.rental.client.ItemServiceClient;
import com.rentmate.service.rental.domain.dto.CustomItemResponse;
import com.rentmate.service.rental.domain.dto.PageResponseDTO;
import com.rentmate.service.rental.domain.dto.RentalRequestDTO;
import com.rentmate.service.rental.domain.dto.RentalResponseDTO;
import com.rentmate.service.rental.service.RentalService;
import com.rentmate.service.rental.shared.utility.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;
    private final ItemServiceClient itemServiceClient;
    private final JwtUtils jwtUtils;
    @PostMapping()
    public ResponseEntity<RentalResponseDTO> createRental(@Valid @RequestBody RentalRequestDTO rentalRequestDTO,
                                                          HttpServletRequest request){
//        Long renterId =jwtUtils.getExtractedId(request);
        Long renterId=14232L;
       // Long renterId=142L;
        RentalResponseDTO responseDTO  = rentalService.createRental(rentalRequestDTO,renterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PatchMapping("/{id}/approve")
    @ResponseStatus(HttpStatus.OK)
    public RentalResponseDTO approveRental(@PathVariable Long id,HttpServletRequest request){
//        Long ownerId =jwtUtils.getExtractedId(request);
        Long ownerId = 14250L;
        return rentalService.approveRental(ownerId,id);

    }
    @PatchMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.OK)
    public RentalResponseDTO rejectRental(@PathVariable Long id,HttpServletRequest request){
       // Long ownerId =jwtUtils.getExtractedId(request);
        Long ownerId = 14250L;
        return rentalService.rejectRental(ownerId,id);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RentalResponseDTO getRentalById(@PathVariable Long id){
       return rentalService.findById(id);
    }

    @GetMapping("/owner")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDTO<RentalResponseDTO> getRentalByOwnerId(
                                                                 @RequestParam(defaultValue = "0") int pageNum,
                                                                 @RequestParam(defaultValue = "10")  int pageSize){
        // Long ownerId =jwtUtils.getExtractedId(request);
        Long ownerId = 14250L;
        return rentalService.findByOwnerIdAndStatusIsPending(ownerId,pageNum,pageSize);
    }
    @GetMapping("/items/{id}")
    public ResponseEntity<CustomItemResponse> getItemById(@PathVariable Long id) {
        CustomItemResponse item = itemServiceClient.getItemById(id);
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelRental(@PathVariable Long id,HttpServletRequest request){
        //Long renterId =jwtUtils.getExtractedId(request);
        Long renterId=2L;
        rentalService.cancelRentalRequest(id,renterId);
    }

}
