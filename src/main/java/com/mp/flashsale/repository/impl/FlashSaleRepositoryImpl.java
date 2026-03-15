package com.mp.flashsale.repository.impl;

import com.mp.flashsale.entity.FlashSale;
import com.mp.flashsale.repository.FlashSaleRepository;
import com.mp.flashsale.repository.jpa.FlashSaleJpaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlashSaleRepositoryImpl implements FlashSaleRepository {
    FlashSaleJpaRepository jpaRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String FS_CACHE_PREFIX = "flashsale:active:";

    @Override
    public Optional<FlashSale> findActiveFlashSale(String itemId) {
        String cacheKey = FS_CACHE_PREFIX + itemId;

        // 1. Thử lấy từ Redis
        FlashSale cachedFs = (FlashSale) redisTemplate.opsForValue().get(cacheKey);
        if (cachedFs != null) {
            // Kiểm tra nhanh xem còn trong thời gian sale không (đề phòng cache chưa kịp expire)
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(cachedFs.getStartTime()) && now.isBefore(cachedFs.getEndTime())) {
                return Optional.of(cachedFs);
            }
            // Nếu hết hạn thì xóa luôn
            redisTemplate.delete(cacheKey);
        }

        // 2. Cache miss -> Query DB
        Optional<FlashSale> activeFs = jpaRepository.findActive(itemId, LocalDateTime.now());

        // 3. Nếu có đợt Sale đang diễn ra, lưu vào Redis
        activeFs.ifPresent(fs -> {
            // Chỉ cache đến khi đợt sale kết thúc để đảm bảo dữ liệu luôn chính xác
            long ttl = java.time.Duration.between(LocalDateTime.now(), fs.getEndTime()).toSeconds();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(cacheKey, fs, ttl, java.util.concurrent.TimeUnit.SECONDS);
            }
        });

        return activeFs;
    }

    @Override
    public int decreaseStock(String id, Integer qty, Integer version) {
        return jpaRepository.decreaseStock(id, qty, version);
    }
}
