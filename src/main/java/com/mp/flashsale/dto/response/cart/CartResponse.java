package com.mp.flashsale.dto.response.cart;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartResponse {
    List<CartItemResponse> items;
    Double totalPrice;
}
