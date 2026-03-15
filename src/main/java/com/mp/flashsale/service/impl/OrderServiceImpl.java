package com.mp.flashsale.service.impl;

import com.mp.flashsale.constant.EOrderStatus;
import com.mp.flashsale.constant.ETransactionType;
import com.mp.flashsale.dto.request.order.CreateOrderRequest;
import com.mp.flashsale.dto.response.order.OrderResponse;
import com.mp.flashsale.entity.*;
import com.mp.flashsale.exception.AppException;
import com.mp.flashsale.exception.ErrorCode;
import com.mp.flashsale.repository.*;
import com.mp.flashsale.service.OrderService;
import com.mp.flashsale.service.TransactionService;
import com.mp.flashsale.util.RedisUtil;
import com.mp.flashsale.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderServiceImpl implements OrderService {
    ItemRepository itemRepository;
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    FlashSaleRepository flashSaleRepository;
    TransactionService transactionService;
    RedisUtil redisUtil;

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        Account customerAccount = SecurityUtil.getCurrentAccount();
        String customerId = customerAccount.getId();

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND_IN_DB));

        // Mặc định là giá gốc
        Long priceToPay = item.getOriginalPrice();
        boolean isFlashSale = false;

        Optional<FlashSale> flashSaleOpt = flashSaleRepository.findActiveFlashSale(item.getId());
        if (flashSaleOpt.isPresent()) {
            FlashSale flashSale = flashSaleOpt.get();

            String limitKey = "fs_limit:" + flashSale.getId() + ":" + customerId;
            Integer alreadyBought = (Integer) redisUtil.getRedisTemplate().opsForValue().get(limitKey);
            if (alreadyBought == null) alreadyBought = 0;

            if (alreadyBought + request.getQuantity() > flashSale.getMaxPerUser()) {
                throw new AppException(ErrorCode.PURCHASE_LIMIT_EXCEEDED);
            }

            int updatedFsRows = flashSaleRepository.decreaseStock(
                    flashSale.getId(),
                    request.getQuantity(),
                    flashSale.getVersion()
            );

            if (updatedFsRows > 0) {
                priceToPay = flashSale.getSalePrice();
                isFlashSale = true;
                redisUtil.getRedisTemplate().opsForValue().set(limitKey, alreadyBought + request.getQuantity(), 24, TimeUnit.HOURS);
            } else {
                throw new AppException(ErrorCode.FLASH_SALE_CONCURRENCY_ERROR);
            }
        }

        int updatedRows = itemRepository.decreaseStock(
                item.getId(),
                request.getQuantity(),
                item.getVersion()
        );
        if (updatedRows == 0) {
            throw new AppException(ErrorCode.OUT_OF_STOCK);
        }

        Long totalPrice = priceToPay * request.getQuantity();
        transactionService.executeEscrowPayment(customerId, totalPrice);

        String orderNumber = redisUtil.generateOrderNumber();
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customer(customerAccount)
                .totalPrice(totalPrice)
                .status(EOrderStatus.PAID)
                .createdAt(LocalDateTime.now())
                .build();
        orderRepository.save(order);

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .item(item)
                .price(priceToPay)
                .quantity(request.getQuantity())
                .build();
        orderItemRepository.save(orderItem);

        redisUtil.removeItemFromCart(customerId, item.getId());

        log.info("Order created: {} | Mode: {} | Total: {}",
                orderNumber, isFlashSale ? "FLASH_SALE" : "NORMAL", totalPrice);

        return OrderResponse.builder()
                .orderNumber(orderNumber)
                .itemName(item.getName())
                .quantity(request.getQuantity())
                .totalPrice(totalPrice)
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Override
    public void confirmShipping(String orderNumber) {
        String currentSellerId = SecurityUtil.getCurrentAccountId();

        // Lấy list SellerId của đơn hàng
        List<String> sellerIds = orderItemRepository.findSellerIdsByOrderNumber(orderNumber);

        if (sellerIds.isEmpty()) throw new AppException(ErrorCode.ORDER_NOT_FOUND_IN_DB);

        if (!sellerIds.contains(currentSellerId)) {
            throw new AppException(ErrorCode.ORDER_OWNERSHIP_MISMATCH);
        }

        Order order = orderRepository.findByOrderNumber(orderNumber).orElseThrow();
        if (!order.getStatus().equals(EOrderStatus.PAID)) throw new AppException(ErrorCode.INVALID_ORDER_STATUS);

        order.setStatus(EOrderStatus.SHIPPING);
        order.setShippedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public void confirmDelivered(String orderNumber) {
        String currentBuyerId = SecurityUtil.getCurrentAccountId();

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND_IN_DB));

        if (!order.getCustomer().getId().equals(currentBuyerId)) {
            throw new AppException(ErrorCode.ORDER_OWNERSHIP_MISMATCH);
        }

        if (!order.getStatus().equals(EOrderStatus.SHIPPING)) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }

        LocalDateTime now = LocalDateTime.now();
        order.setStatus(EOrderStatus.DELIVERED);
        order.setDeliveredAt(now);
        order.setComplaintDeadline(now.plusDays(1)); // 24h complaint window
        orderRepository.save(order);
    }

    @Override
    public void manualSettlement(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND_IN_DB));

        if (!order.getStatus().equals(EOrderStatus.DELIVERED)) throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        if (LocalDateTime.now().isBefore(order.getComplaintDeadline())) throw new AppException(ErrorCode.STILL_IN_COMPLAINT_PERIOD);

        List<OrderItem> orderItems = orderItemRepository.findByOrderNumber(orderNumber);
        if (orderItems.isEmpty()) throw new AppException(ErrorCode.ORDER_NOT_FOUND_IN_DB);

        // Gom nhóm tiền theo Seller
        Map<String, Long> settlementMap = orderItems.stream()
                .collect(Collectors.groupingBy(
                        oi -> oi.getItem().getSeller().getId(),
                        Collectors.summingLong(oi -> oi.getPrice() * oi.getQuantity())
                ));

        settlementMap.forEach((sellerId, amount) -> {
            transactionService.executeReleaseEscrow(sellerId, amount);
            log.info("Released {} to Seller {} from Order {}", amount, sellerId, orderNumber);
        });

        order.setStatus(EOrderStatus.COMPLETED);
        orderRepository.save(order);
    }
}
