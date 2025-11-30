package com.foodexpress.restaurant.dto.response;

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
    private String restaurantId;
    private String restaurantName;
    private List<MenuCategoryWithItems> categories;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuCategoryWithItems {
        private String categoryId;
        private String categoryName;
        private String categoryDescription;
        private Integer displayOrder;
        private List<MenuItemResponse> items;
    }
}