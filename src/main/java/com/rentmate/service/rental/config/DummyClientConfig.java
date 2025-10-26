package com.rentmate.service.rental.config;

import com.rentmate.service.rental.client.ItemServiceClient;
import com.rentmate.service.rental.domain.dto.CustomItemResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

//@Configuration
//public class DummyClientConfig {
//    @Bean
//    public ItemServiceClient itemServiceClient() {
//        return new ItemServiceClient() {
//            @Override
//            public CustomItemResponse getItemById(Long itemId) {
//                return new CustomItemResponse("item1", 14250L, new BigDecimal(234),"Maadi City Centre");
//            }
//
//            @Override
//            public boolean isAvailable(Long itemId) {
//                return true;
//            }
//
//            @Override
//            public void updateAvailability(Long itemId, boolean availability) {
//                System.out.println("Dummy updateAvailability called");
//            }
//        };
//    }
//}
