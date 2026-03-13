package com.mp.flashsale.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RedisUtil {
    RedisTemplate<String, Object> redisTemplate;

    // Keys
    static String ORDER_SEQUENCE_KEY = "order-sequence";
    static String CART_PREFIX = "cart:";
    static String VERIFY_EMAIL_TOKEN_PREFIX = "verify-email-tk:";
    static String FORGOT_PASSWORD_TOKEN_PREFIX = "forgot-password-tk:";
    static String LOCK_ITEM_PREFIX = "lock:item:"; // khóa sản phẩm khi thanh toán

    /**
     * Sinh Order Number theo dạng: yyyyMMdd-00000001
     */
    public String generateOrderNumber() {
        Long sequence = redisTemplate.opsForValue().increment(ORDER_SEQUENCE_KEY, 1);

        if (sequence != null && sequence == 1L) {
            // Reset sequence vào cuối ngày (23:59:59)
            redisTemplate.expire(ORDER_SEQUENCE_KEY, 24, TimeUnit.HOURS);
        }

        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return date + "-" + String.format("%08d", sequence);
    }

    // --- Quản lý Cart (Dùng Redis Hash) ---

    public void addItemToCart(String accountId, String itemId, Integer quantity) {
        String key = CART_PREFIX + accountId;
        redisTemplate.opsForHash().put(key, itemId, String.valueOf(quantity));
        redisTemplate.expire(key, 7, TimeUnit.DAYS); // Giỏ hàng lưu 7 ngày
    }

    public Object getCart(String accountId) {
        return redisTemplate.opsForHash().entries(CART_PREFIX + accountId);
    }

    public void deleteCart(String accountId) {
        redisTemplate.delete(CART_PREFIX + accountId);
    }

    // --- Email & Token

    public String generateVerifyToken(String accountId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(VERIFY_EMAIL_TOKEN_PREFIX + token, accountId, 30, TimeUnit.MINUTES);
        return token;
    }

    public String getAccountIdByToken(String token) {
        return (String) redisTemplate.opsForValue().getAndDelete(VERIFY_EMAIL_TOKEN_PREFIX + token);
    }
}
