package com.rentmate.service.rental.client;

import com.rentmate.service.rental.domain.dto.CustomItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "item-service",url = "http://localhost:8585/items")
public interface ItemServiceClient {
    @GetMapping("/{id}")
    CustomItemResponse getItemById(@PathVariable Long id);

    @GetMapping("/{id}/isAvailabile")
    boolean isItemAvailable(@PathVariable() Long id);

//    @PatchMapping("")
//    void updateAvailability(@PathVariable() Long itemId , boolean availability);

}
