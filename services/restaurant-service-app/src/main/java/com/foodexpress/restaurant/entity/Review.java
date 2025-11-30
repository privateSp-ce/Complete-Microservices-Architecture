package com.foodexpress.restaurant.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private String userId;
    private String userName;
    private Double rating;
    private String comment;
    private LocalDateTime createdAt = LocalDateTime.now();
}
