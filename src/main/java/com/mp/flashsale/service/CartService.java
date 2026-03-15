package com.mp.flashsale.service;

import com.mp.flashsale.dto.response.cart.CartResponse;

public interface CartService {
    public void addToCart(String itemId, Integer quantity);
    public CartResponse getCart();
    public void removeFromCart(String itemId);
}
