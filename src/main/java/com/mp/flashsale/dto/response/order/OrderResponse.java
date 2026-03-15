package com.mp.flashsale.dto.response.order;

import com.mp.flashsale.constant.EOrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    String orderNumber;
    Long totalPrice;
    EOrderStatus status;
    LocalDateTime createdAt;

    String itemName;
    Integer quantity;
}
