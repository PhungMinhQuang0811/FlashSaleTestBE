package com.mp.flashsale.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    String itemId;
    String name;
    String imageUrl;
    Long price;
    Integer quantity;
    Integer stock;
}
