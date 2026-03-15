package com.mp.flashsale.repository;

import com.mp.flashsale.entity.Order;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findByOrderNumber(String orderNumber);
}
