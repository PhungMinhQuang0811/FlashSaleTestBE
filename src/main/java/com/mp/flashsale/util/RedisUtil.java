package com.mp.flashsale.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
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
    static final String ITEM_CACHE_PREFIX = "item:";
    static final String ITEM_LOCK_PREFIX = "lock:item_find:"; // khóa sản phẩm khi thanh toán
    static final String NULL_VALUE = "EMPTY";
    static final String PROCESSING_TRANSACTION_PREFIX = "trans:";
    static final String STOCK_PREFIX = "stock:";
    static final String USER_LIMIT_PREFIX = "user_limit:";
    private static final DefaultRedisScript<Long> STOCK_SCRIPT;

    static {
        String script =
                "local stock = redis.call('get', KEYS[1]) " +
                        "if not stock then return -3 end " + // Case: Key không tồn tại (chưa warm-up)

                        "stock = tonumber(stock) " +
                        "local buyQty = tonumber(ARGV[1]) " +
                        "local limit = tonumber(ARGV[3]) " +
                        "local userId = ARGV[2] " +

                        "if (stock < buyQty) then return -1 end " + // Case: Hết hàng

                        "local bought = tonumber(redis.call('hget', KEYS[2], userId) or '0') " +
                        "if (bought + buyQty > limit) then return -2 end " + // Case: Vượt giới hạn

                        "redis.call('decrby', KEYS[1], buyQty) " +
                        "redis.call('hset', KEYS[2], userId, bought + buyQty) " +
                        "return 1";
        STOCK_SCRIPT = new DefaultRedisScript<>(script, Long.class);
    }

    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }
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
        redisTemplate.expire(key, 2, TimeUnit.DAYS); // Giỏ hàng lưu 7 ngày
    }

    public Object getCart(String accountId) {
        return redisTemplate.opsForHash().entries(CART_PREFIX + accountId);
    }

    public void removeItemFromCart(String accountId, String itemId) {
        String key = CART_PREFIX + accountId;
        redisTemplate.opsForHash().delete(key, itemId);
    }

    public void deleteCart(String accountId) {
        String key = CART_PREFIX + accountId;
        redisTemplate.delete(key);
    }

    // --- Email & Token

    public String generateVerifyEmailToken(String accountId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(VERIFY_EMAIL_TOKEN_PREFIX + token, accountId, 30, TimeUnit.MINUTES);
        return token;
    }
    public String getValueOfVerifyEmailToken(String token){
        String key = VERIFY_EMAIL_TOKEN_PREFIX + token;
        Object accountId = redisTemplate.opsForValue().getAndDelete(key);

        if (accountId == null) return null;
        return String.valueOf(accountId);
    }
    public String generateForgotPasswordToken(String accountId){
        String token = UUID.randomUUID().toString();
        String key = FORGOT_PASSWORD_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, accountId, 24, TimeUnit.HOURS);
        //TODO: test
//        redisTemplate.opsForValue().set(key, accountId, 20, TimeUnit.SECONDS);
        return token;
    }
    public String getValueOfForgotPasswordToken(String token){
        String key = FORGOT_PASSWORD_TOKEN_PREFIX + token;
        Object accountId = redisTemplate.opsForValue().getAndDelete(key);

        if (accountId == null) return null;
        return String.valueOf(accountId);
    }
    public void deleteForgotPasswordToken(String token){
        String key = FORGOT_PASSWORD_TOKEN_PREFIX + token;
        redisTemplate.delete(key);
    }

    public String getAccountIdByToken(String token) {
        return (String) redisTemplate.opsForValue().getAndDelete(VERIFY_EMAIL_TOKEN_PREFIX + token);
    }
    public void cacheProcessingTransaction(String transactionId){
        String key = PROCESSING_TRANSACTION_PREFIX + transactionId;
        redisTemplate.opsForValue().set(key, transactionId, 15, TimeUnit.MINUTES);
    }

    public void removeCacheProcessingTransaction(String transactionId){
        String key = PROCESSING_TRANSACTION_PREFIX + transactionId;
        redisTemplate.delete(key);
    }
    // 1. Lưu Item vào Redis (với thời gian hết hạn, ví dụ 10 phút)
    public void cacheItem(String itemId, Object itemData) {
        String key = ITEM_CACHE_PREFIX + itemId;
        if (itemData == null) {
            // Nếu null, lưu giá trị "EMPTY" với thời gian hết hạn ngắn (ví dụ 2-3 phút)
            redisTemplate.opsForValue().set(key, NULL_VALUE, 3, TimeUnit.MINUTES);
        } else {
            redisTemplate.opsForValue().set(key, itemData, 10, TimeUnit.MINUTES);
        }
    }

    // 2. Lấy Item từ Redis
    public Object getCachedItem(String itemId) {
        return redisTemplate.opsForValue().get(ITEM_CACHE_PREFIX + itemId);
    }

    // 3. Thử lấy Lock để truy cập DB (Chống Cache Breakdown)
    public boolean tryLockItemSearch(String itemId) {
        String key = ITEM_LOCK_PREFIX + itemId;
        // Set nếu chưa có, hết hạn sau 5 giây để an toàn
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "LOCKED", 5, TimeUnit.SECONDS));
    }

    // 4. Giải phóng Lock
    public void unlockItemSearch(String itemId) {
        redisTemplate.delete(ITEM_LOCK_PREFIX + itemId);
    }
    public Long deductStockWithLua(String itemId, String userId, int quantity, int limitPerUser) {
        String stockKey = STOCK_PREFIX + itemId;
        String userLimitKey = USER_LIMIT_PREFIX + itemId;

        try {
            return redisTemplate.execute(
                    STOCK_SCRIPT,
                    new StringRedisSerializer(), // Serializer cho Keys & ARGV
                    new org.springframework.data.redis.serializer.GenericToStringSerializer<>(Long.class), // Serializer cho Result
                    java.util.List.of(stockKey, userLimitKey),
                    String.valueOf(quantity),
                    userId,
                    String.valueOf(limitPerUser)
            );
        } catch (Exception e) {
            log.error("Lỗi thực thi Lua Script chi tiết: {}", e.getMessage());
            return -4L;
        }
    }
}
