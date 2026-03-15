package com.mp.flashsale.service;

import com.mp.flashsale.dto.request.order.CreateOrderRequest;
import com.mp.flashsale.dto.response.order.OrderResponse;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    void confirmShipping(String orderNumber);
    void confirmDelivered(String orderNumber);
    void manualSettlement(String orderNumber);
}
