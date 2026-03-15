package com.mp.flashsale.repository.jpa;

import com.mp.flashsale.entity.OrderItem;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {
    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.item JOIN FETCH oi.order " +
            "WHERE oi.order.orderNumber = :orderNumber")
    List<OrderItem> findByOrderNumber(@Param("orderNumber") String orderNumber);

    @Query("SELECT DISTINCT oi.item.seller.id FROM OrderItem oi WHERE oi.order.orderNumber = :orderNumber")
    List<String> findSellerIdsByOrderNumber(@Param("orderNumber") String orderNumber);
}
