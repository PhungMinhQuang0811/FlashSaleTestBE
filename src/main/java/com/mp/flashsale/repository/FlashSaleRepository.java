package com.mp.flashsale.repository;

import com.mp.flashsale.entity.FlashSale;

import java.util.Optional;

public interface FlashSaleRepository {
    Optional<FlashSale> findActiveFlashSale(String itemId);
    int decreaseStock(String id, Integer qty, Integer version);
}
