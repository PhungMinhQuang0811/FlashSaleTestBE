package com.mp.flashsale.repository.impl;

import com.mp.flashsale.constant.EItemStatus;
import com.mp.flashsale.dto.response.item.ItemResponse;
import com.mp.flashsale.entity.Item;
import com.mp.flashsale.repository.ItemRepository;
import com.mp.flashsale.repository.jpa.ItemJpaRepository;
import com.mp.flashsale.util.RedisUtil;
import com.mp.flashsale.util.SecurityUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ItemRepositoryImpl implements ItemRepository {

    ItemJpaRepository jpaRepository;
    RedisUtil redisUtil;
    RedisTemplate<String, Object> redisTemplate;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Item save(Item item) {
        Item savedItem = jpaRepository.save(item);

        String cacheKey = "item:" + savedItem.getId();
        redisTemplate.delete(cacheKey);
        log.info("Đã xóa cache cho Item ID: {} sau khi save", savedItem.getId());
        return savedItem;
    }

    @Override
    public Optional<Item> findById(String id) {
        String cacheKey = "item:" + id;
        String stockKey = "stock:" + id;

        // 1. Thử lấy thông tin Item từ Cache
        Object cached = redisUtil.getCachedItem(id);
        if ("EMPTY".equals(cached)) return Optional.empty();

        if (cached != null) {
            ItemResponse resp = (ItemResponse) cached;
            // Trả về proxy để tránh lỗi Transient khi lưu OrderItem
            return Optional.of(entityManager.getReference(Item.class, resp.getId()));
        }

        // 2. Cache Miss hoặc cần nạp lại dữ liệu
        if (redisUtil.tryLockItemSearch(id)) {
            try {
                Optional<Item> dbItem = jpaRepository.findByIdAndDeletedAtIsNull(id);

                if (dbItem.isPresent()) {
                    Item item = dbItem.get();

                    redisTemplate.execute((org.springframework.data.redis.connection.RedisConnection connection) -> {
                        connection.stringCommands().set(
                                stockKey.getBytes(),
                                String.valueOf(item.getQuantity()).getBytes()
                        );
                        return null;
                    });

                    // Cache thông tin ItemResponse để lần sau không vào DB nữa
                    ItemResponse rawCache = ItemResponse.builder()
                            .id(item.getId())
                            .name(item.getName())
                            .originalPrice(item.getOriginalPrice())
                            .quantity(item.getQuantity())
                            .imageUrl(item.getImagePublicId())
                            .build();
                    redisUtil.cacheItem(id, rawCache);

                    return Optional.of(item);
                } else {
                    redisUtil.cacheItem(id, null); // Chống Penetration
                    return Optional.empty();
                }
            } finally {
                redisUtil.unlockItemSearch(id);
            }
        } else {
            // Đợi một chút rồi thử lại để lấy dữ liệu từ luồng đang nạp cache
            try {
                TimeUnit.MILLISECONDS.sleep(100);
                return findById(id);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        }
    }

    @Override
    public Page<Item> findAllActive(Pageable pageable) {
        return jpaRepository.findAllByDeletedAtIsNull(pageable);
    }
    @Override
    public void softDelete(String id) {
        jpaRepository.softDelete(id, LocalDateTime.now(), EItemStatus.DISCONTINUED);

        String cacheKey = "item:" + id;
        redisTemplate.delete(cacheKey);
        log.info("Đã xóa cache cho Item ID: {} sau khi softDelete", id);
    }
    @Override
    public void flush() {
        jpaRepository.flush();
    }
    @Override
    public Page<Item> findAllByStatus(EItemStatus status, Pageable pageable) {
        return jpaRepository.findAvailableItems(status, pageable);
    }
    @Override
    public List<Item> findBySellerId(String sellerId) {
        return jpaRepository.findAllBySeller(sellerId);
    }
    @Override
    public int decreaseStock(String id, Integer quantity, Integer currentVersion) {
        String userId = SecurityUtil.getCurrentAccountId();

        // 1. Trừ trên Redis (Dùng Lua Script với bộ đếm Stock riêng)
        Long result = redisUtil.deductStockWithLua(id, userId, quantity, 2);

        if (result == 1) {
            log.info("Redis trừ kho OK. Bắt đầu trừ DB cho ID: {}", id);
            int rows = jpaRepository.decreaseStock(id, quantity, currentVersion);
            log.info("Số dòng DB bị ảnh hưởng (Rows affected): {}", rows);

            if (rows > 0) {
                log.info("DB đã trừ kho thành công!");
            } else {
                log.warn("DB KHÔNG trừ được kho (có thể ID sai hoặc quantity trong DB < quantity mua)");
            }

            redisTemplate.delete("item:" + id);
            return rows;
        }
        else{
            log.info(">>>> REDIS DEDUCT RESULT: {}", result);
        }
        return result.intValue();
    }
}
