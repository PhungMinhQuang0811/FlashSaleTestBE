package com.mp.flashsale.controller;

import com.mp.flashsale.dto.request.order.CreateOrderRequest;
import com.mp.flashsale.dto.response.ApiResponse;
import com.mp.flashsale.dto.response.order.OrderResponse;
import com.mp.flashsale.service.OrderService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flash-sale/order")
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    OrderService orderService;
    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@RequestBody @Valid CreateOrderRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .data(orderService.createOrder(request))
                .build();
    }
    @PatchMapping("/seller/{orderNumber}/shipping")
    public ApiResponse<Void> confirmShipping(@PathVariable String orderNumber) {
        orderService.confirmShipping(orderNumber);
        return ApiResponse.<Void>builder()
                .message("Order is now being shipped.")
                .build();
    }
    @PatchMapping("/{orderNumber}/delivered")
    public ApiResponse<Void> confirmDelivered(@PathVariable String orderNumber) {
        orderService.confirmDelivered(orderNumber);
        return ApiResponse.<Void>builder()
                .message("Order received successfully.")
                .build();
    }
    @PostMapping("/admin/{orderNumber}/settle")
    public ApiResponse<Void> manualSettlement(@PathVariable String orderNumber) {
        orderService.manualSettlement(orderNumber);
        return ApiResponse.<Void>builder()
                .message("Settlement completed for order: " + orderNumber)
                .build();
    }
}
