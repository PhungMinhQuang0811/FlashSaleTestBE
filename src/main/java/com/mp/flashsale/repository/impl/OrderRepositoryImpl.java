package com.mp.flashsale.repository.impl;

import com.mp.flashsale.repository.OrderRepository;
import com.mp.flashsale.repository.jpa.OrderJpaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderRepositoryImpl implements OrderRepository {
    OrderJpaRepository orderJpaRepository;

    @Override
    public int decreaseStock(String id, Integer quantity, Long currentVersion) {
        return orderJpaRepository.decreaseStock(id, quantity, currentVersion);
    }
}
