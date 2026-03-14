package com.mp.flashsale.repository;

import com.mp.flashsale.constant.EItemStatus;
import com.mp.flashsale.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item save(Item item);
    void flush();
    Optional<Item> findById(String id);
    Page<Item> findAllActive(Pageable pageable);
    List<Item> findBySellerId(String sellerId);
    void softDelete(String id);
    Page<Item> findAllByStatus(EItemStatus status, Pageable pageable);
}
