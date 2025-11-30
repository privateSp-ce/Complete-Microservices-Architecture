package com.foodexpress.user.config;

import com.foodexpress.user.entity.Address;
import com.foodexpress.user.entity.User;
import com.foodexpress.user.repository.AddressRepository;
import com.foodexpress.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DataSeeder {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Bean
    public CommandLineRunner seedUserData() {
        return args -> {
            if (userRepository.count() > 0) {
                log.info("Users already seeded, skipping.");
                return;
            }

            log.info("Seeding User Data...");

            // 1. Create Demo User
            User user = User.builder()
                    .email("demo@foodexpress.com")
                    .firstName("Demo")
                    .lastName("User")
                    .phone("9999999999")
                    .password("password123") // In real app, this should be encoded
                    .role("CUSTOMER")
                    .isActive(true)
                    .build();
            user = userRepository.save(user);

            // 2. Add Address
            Address address = Address.builder()
                    .user(user)
                    .addressLine1("Flat 101, Tech Park")
                    .city("Hyderabad")
                    .state("Telangana")
                    .pincode("500081")
                    .country("India")
                    .isDefault(true)
                    .type("HOME")
                    .build();
            addressRepository.save(address);

            log.info("Seeding User Completed. User ID: {}", user.getId());
        };
    }
}
