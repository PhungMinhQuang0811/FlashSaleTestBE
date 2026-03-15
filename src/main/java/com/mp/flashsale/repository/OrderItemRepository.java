package com.mp.flashsale.repository;

import com.mp.flashsale.entity.Order;
import com.mp.flashsale.entity.OrderItem;

import java.util.List;

public interface OrderItemRepository {
    OrderItem save(OrderItem orderItem);
    List<OrderItem> findByOrderNumber(String orderNumber);
    List<String> findSellerIdsByOrderNumber(String orderNumber);
}
