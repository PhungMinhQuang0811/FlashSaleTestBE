package com.mp.flashsale.repository.jpa;

import com.mp.flashsale.entity.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface FlashSaleJpaRepository extends JpaRepository<FlashSale, String> {
    @Query("SELECT fs FROM FlashSale fs WHERE fs.item.id = :itemId " +
            "AND :now BETWEEN fs.startTime AND fs.endTime")
    Optional<FlashSale> findActive(String itemId, LocalDateTime now);

    @Modifying
    @Query("UPDATE FlashSale fs SET fs.remainingQuantity = fs.remainingQuantity - :qty, fs.version = fs.version + 1 " +
            "WHERE fs.id = :id AND fs.remainingQuantity >= :qty AND fs.version = :version")
    int decreaseStock(String id, Integer qty, Integer version);
}
