package com.spring_boot.restaurant_service_app.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RestaurantDTO {
    private String name;
    private double price;
}
