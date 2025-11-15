package com.spring_boot.restaurant_service_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FullMenuResponse {
    private Long restaurantId;
    private String restaurantName;
    private List<MenuCategoryWithItems> categories;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuCategoryWithItems {
        private Long categoryId;
        private String categoryName;
        private String categoryDescription;
        private Integer displayOrder;
        private List<MenuItemResponse> items;
    }
}