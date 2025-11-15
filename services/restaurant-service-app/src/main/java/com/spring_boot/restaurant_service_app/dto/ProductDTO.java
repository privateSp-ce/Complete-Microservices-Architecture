package com.spring_boot.restaurant_service_app.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductDTO {
    private String name;
    private double price;
}
