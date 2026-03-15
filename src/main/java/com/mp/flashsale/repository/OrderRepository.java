package com.mp.flashsale.repository;

public interface OrderRepository {
    int decreaseStock(String id, Integer quantity, Long currentVersion);
}
