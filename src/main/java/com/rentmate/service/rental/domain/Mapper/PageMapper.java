package com.rentmate.service.rental.domain.Mapper;

import com.rentmate.service.rental.domain.dto.PageResponseDTO;
import org.hibernate.validator.constraints.br.CPF;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class PageMapper {
    public <E,D>PageResponseDTO<D> toPageResponseDTO(Page<E> page, Function<E,D> mapper){
        Page<D> dtoPage = page.map(mapper);
        return new PageResponseDTO<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages(),
                dtoPage.isLast()
        );


    }

}
