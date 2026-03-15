package com.mp.flashsale.repository.impl;

import com.mp.flashsale.entity.OrderItem;
import com.mp.flashsale.repository.OrderItemRepository;
import com.mp.flashsale.repository.jpa.OrderItemJpaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderItemRepositoryImpl implements OrderItemRepository {
    OrderItemJpaRepository orderItemJpaRepository;

    @Override
    public OrderItem save(OrderItem orderItem) {
        return orderItemJpaRepository.save(orderItem);
    }

    @Override
    public List<OrderItem> findByOrderNumber(String orderNumber) {
        return orderItemJpaRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public List<String> findSellerIdsByOrderNumber(String orderNumber) {
        return orderItemJpaRepository.findSellerIdsByOrderNumber(orderNumber);
    }
}
