package com.mp.flashsale.repository.impl;

import com.mp.flashsale.entity.Order;
import com.mp.flashsale.repository.OrderRepository;
import com.mp.flashsale.repository.jpa.OrderJpaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderRepositoryImpl implements OrderRepository {
    OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderJpaRepository.findByOrderNumber(orderNumber);
    }
}
