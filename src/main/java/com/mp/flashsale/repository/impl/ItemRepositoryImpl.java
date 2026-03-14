package com.mp.flashsale.repository.impl;

import com.mp.flashsale.entity.Item;
import com.mp.flashsale.repository.ItemRepository;
import com.mp.flashsale.repository.jpa.ItemJpaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemRepositoryImpl implements ItemRepository {

    ItemJpaRepository jpaRepository;

    @Override
    public Item save(Item item) {
        return jpaRepository.save(item);
    }

    @Override
    public Optional<Item> findById(String id) {
        return jpaRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public Page<Item> findAllActive(Pageable pageable) {
        return jpaRepository.findAllByDeletedAtIsNull(pageable);
    }

    @Override
    public List<Item> findBySellerId(String sellerId) {
        return jpaRepository.findAllBySellerIdAndDeletedAtIsNull(sellerId);
    }

    @Override
    public void softDelete(String id) {
        jpaRepository.findById(id).ifPresent(item -> {
            item.setDeletedAt(LocalDateTime.now());
            jpaRepository.save(item);
        });
    }
}
