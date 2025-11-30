package com.foodexpress.user.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserProfileDto {
    private String username;
    private String email;
    private String avatarUrl;
    private List<String> preferences; // e.g. "VEGAN", "SPICY"
}
