package com.mp.flashsale.controller;

import com.mp.flashsale.dto.response.ApiResponse;
import com.mp.flashsale.dto.response.cart.CartResponse;
import com.mp.flashsale.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Cart", description = "API for shopping cart management")
public class CartController {

    CartService cartService;

    @PostMapping("/add")
    public ApiResponse<Void> addToCart(
            @RequestParam String itemId,
            @RequestParam Integer quantity) {
        cartService.addToCart(itemId, quantity);
        return ApiResponse.<Void>builder()
                .message("Item added to cart successfully")
                .build();
    }

    @GetMapping
    public ApiResponse<CartResponse> getCart() {
        return ApiResponse.<CartResponse>builder()
                .data(cartService.getCart())
                .build();
    }

    @DeleteMapping("/remove/{itemId}")
    public ApiResponse<Void> removeFromCart(@PathVariable String itemId) {
        cartService.removeFromCart(itemId);
        return ApiResponse.<Void>builder()
                .message("Item removed from cart")
                .build();
    }
}