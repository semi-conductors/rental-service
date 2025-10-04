package com.rentmate.service.rental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.rentmate.service.rental.client")
public class ServiceRentalApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceRentalApplication.class, args);
	}

}
