package com.mp.flashsale.service.impl;

import com.mp.flashsale.dto.response.cart.CartItemResponse;
import com.mp.flashsale.dto.response.cart.CartResponse;
import com.mp.flashsale.entity.Item;
import com.mp.flashsale.exception.AppException;
import com.mp.flashsale.exception.ErrorCode;
import com.mp.flashsale.mapper.ItemMapper;
import com.mp.flashsale.repository.ItemRepository;
import com.mp.flashsale.service.CartService;
import com.mp.flashsale.service.FileService;
import com.mp.flashsale.util.RedisUtil;
import com.mp.flashsale.util.SecurityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartServiceImpl implements CartService {
    RedisUtil redisUtil;
    ItemRepository itemRepository;
    ItemMapper itemMapper;
    FileService fileService;
    @Override
    public void addToCart(String itemId, Integer quantity) {
        String accountId = SecurityUtil.getCurrentAccountId();

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND_IN_DB));

        if (item.getQuantity() < quantity) {
            throw new AppException(ErrorCode.OUT_OF_STOCK);
        }

        redisUtil.addItemToCart(accountId, itemId, quantity);
        log.info("User {} added item {} to cart", accountId, itemId);
    }

    @Override
    public CartResponse getCart() {
        String accountId = SecurityUtil.getCurrentAccountId();

        Map<Object, Object> cartData = (Map<Object, Object>) redisUtil.getCart(accountId);

        if (cartData == null || cartData.isEmpty()) {
            return CartResponse.builder()
                    .items(List.of())
                    .totalPrice(0.0)
                    .build();
        }

        List<CartItemResponse> items = cartData.entrySet().stream()
                .map(entry -> {
                    String itemId = (String) entry.getKey();
                    Integer quantity = Integer.parseInt((String) entry.getValue());

                    return itemRepository.findById(itemId)
                            .map(item -> CartItemResponse.builder()
                                    .itemId(itemId)
                                    .name(item.getName())
                                    .price(item.getOriginalPrice())
                                    .quantity(quantity)
                                    .stock(item.getQuantity())
                                    .imageUrl(fileService.getFileUrl(item.getImagePublicId()))
                                    .build())
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .toList();

        double totalPrice = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        return CartResponse.builder()
                .items(items)
                .totalPrice(totalPrice)
                .build();
    }

    @Override
    public void removeFromCart(String itemId) {
        String accountId = SecurityUtil.getCurrentAccountId();
        String key = "cart:" + accountId;
        redisUtil.getRedisTemplate().opsForHash().delete(key, itemId);
    }
}
