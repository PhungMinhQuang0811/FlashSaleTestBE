package com.mp.flashsale.repository.jpa;

import com.mp.flashsale.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface OrderJpaRepository extends JpaRepository<Order, String> {
    @Modifying
    @Query("UPDATE Item i SET i.quantity = i.quantity - :qty, i.version = i.version + 1 " +
            "WHERE i.id = :id AND i.quantity >= :qty AND i.version = :currentVersion")
    int decreaseStock(String id, Integer qty, Long currentVersion);
}
