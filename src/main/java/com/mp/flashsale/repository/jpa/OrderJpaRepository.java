package com.mp.flashsale.repository.jpa;

import com.mp.flashsale.entity.Item;
import com.mp.flashsale.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<Order, String> {
    Optional<Order> findByOrderNumber(String orderNumber);
}
