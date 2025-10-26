package com.rentmate.service.rental.domain.Mapper;

import com.rentmate.service.rental.domain.dto.RentalRequestDTO;
import com.rentmate.service.rental.domain.dto.RentalResponseDTO;
import com.rentmate.service.rental.domain.entity.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RentalMapper {


    @Mapping(target = "renterId", source = "renterId")
    @Mapping(target = "ownerId",ignore = true)
    @Mapping(target = "depositAmount", ignore = true)
    @Mapping(target = "rentalPrice", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "ownerAddress" , ignore = true)
    Rental toEntity(RentalRequestDTO dto,Long renterId);

    @Mapping(target = "rentalId",source = "id")
    RentalResponseDTO toDto(Rental rental);


    List<RentalResponseDTO> DTO_LIST(List<Rental> rentals);
}
