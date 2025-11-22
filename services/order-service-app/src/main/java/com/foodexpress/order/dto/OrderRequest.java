package com.foodexpress.order.dto;

import com.foodexpress.order.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private String userId; // Gateway nunchi header lo vastundi, but just in case
    private PaymentMethod paymentMethod;
    private String deliveryAddress;
}