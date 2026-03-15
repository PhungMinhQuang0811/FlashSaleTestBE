package com.mp.flashsale.service.impl;

import com.mp.flashsale.constant.EOrderStatus;
import com.mp.flashsale.dto.request.order.CreateOrderRequest;
import com.mp.flashsale.dto.response.order.OrderResponse;
import com.mp.flashsale.entity.*;
import com.mp.flashsale.exception.AppException;
import com.mp.flashsale.exception.ErrorCode;
import com.mp.flashsale.repository.FlashSaleRepository;
import com.mp.flashsale.repository.ItemRepository;
import com.mp.flashsale.repository.OrderItemRepository;
import com.mp.flashsale.repository.OrderRepository;
import com.mp.flashsale.service.TransactionService;
import com.mp.flashsale.util.RedisUtil;
import com.mp.flashsale.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {
    @Mock
    ItemRepository itemRepository;
    @Mock
    FlashSaleRepository flashSaleRepository;
    @Mock
    OrderRepository orderRepository;
    @Mock
    OrderItemRepository orderItemRepository;
    @Mock
    TransactionService transactionService;
    @Mock
    RedisUtil redisUtil;
    @Mock
    RedisTemplate<String, Object> redisTemplate;
    @Mock
    ValueOperations<String, Object> valueOperations;

    @InjectMocks
    OrderServiceImpl orderService;

    CreateOrderRequest request;
    Account customer;
    Item item;

    @BeforeEach
    void setUp() {
        request = new CreateOrderRequest("item-123", 1);
        customer = Account.builder().id("user-456").build();
        item = Item.builder()
                .id("item-123")
                .name("Laptop")
                .originalPrice(1000L)
                .quantity(10)
                .version(1)
                .build();
    }
    @Test
    void createOrder_NormalSuccess() {
        // Mock Security & Repositories
        try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentAccount).thenReturn(customer);

            when(itemRepository.findById(anyString())).thenReturn(Optional.of(item));
            when(flashSaleRepository.findActiveFlashSale(anyString())).thenReturn(Optional.empty());
            when(itemRepository.decreaseStock(anyString(), anyInt(), anyInt())).thenReturn(1);
            when(redisUtil.generateOrderNumber()).thenReturn("20260315-001");

            // Execute
            OrderResponse response = orderService.createOrder(request);

            // Verify
            assertEquals(1000L, response.getTotalPrice()); // Phải là giá gốc
            verify(transactionService).executeEscrowPayment(customer.getId(), 1000L);
            verify(redisUtil).removeItemFromCart(customer.getId(), item.getId());
        }
    }
    @Test
    void createOrder_FlashSaleSuccess() {
        FlashSale fs = new FlashSale();
        fs.setId("fs-999");
        fs.setSalePrice(500L);
        fs.setMaxPerUser(2);
        fs.setVersion(1);

        try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentAccount).thenReturn(customer);

            when(itemRepository.findById(anyString())).thenReturn(Optional.of(item));
            when(flashSaleRepository.findActiveFlashSale(anyString())).thenReturn(Optional.of(fs));

            // Mock Redis check limit
            when(redisUtil.getRedisTemplate()).thenReturn(redisTemplate);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(0); // Chưa mua cái nào

            when(flashSaleRepository.decreaseStock(anyString(), anyInt(), anyInt())).thenReturn(1);
            when(itemRepository.decreaseStock(anyString(), anyInt(), anyInt())).thenReturn(1);
            when(redisUtil.generateOrderNumber()).thenReturn("20260315-SALE");

            // Execute
            OrderResponse response = orderService.createOrder(request);

            // Verify
            assertEquals(500L, response.getTotalPrice()); // Phải là giá Sale
            verify(transactionService).executeEscrowPayment(customer.getId(), 500L);
        }
    }
    @Test
    void createOrder_FlashSaleConcurrencyError() {
        FlashSale fs = new FlashSale();
        fs.setId("fs-999");
        fs.setMaxPerUser(2);
        fs.setVersion(1);

        try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentAccount).thenReturn(customer);
            when(itemRepository.findById(anyString())).thenReturn(Optional.of(item));
            when(flashSaleRepository.findActiveFlashSale(anyString())).thenReturn(Optional.of(fs));

            // Mock Redis limit OK
            when(redisUtil.getRedisTemplate()).thenReturn(redisTemplate);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(0);

            // Trả về 0 để giả lập có người khác đã update version trước
            when(flashSaleRepository.decreaseStock(anyString(), anyInt(), anyInt())).thenReturn(0);

            // Verify Exception
            AppException exception = assertThrows(AppException.class, () -> orderService.createOrder(request));
            assertEquals(ErrorCode.FLASH_SALE_CONCURRENCY_ERROR, exception.getErrorCode());
        }
    }
    @Test
    void createOrder_ExceedLimit() {
        FlashSale fs = new FlashSale();
        fs.setId("fs-999");
        fs.setMaxPerUser(2);

        try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentAccount).thenReturn(customer);
            when(itemRepository.findById(anyString())).thenReturn(Optional.of(item));
            when(flashSaleRepository.findActiveFlashSale(anyString())).thenReturn(Optional.of(fs));

            // Mock Redis: đã mua 2 món
            when(redisUtil.getRedisTemplate()).thenReturn(redisTemplate);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(2);

            // Verify Exception
            AppException exception = assertThrows(AppException.class, () -> orderService.createOrder(request));
            assertEquals(ErrorCode.PURCHASE_LIMIT_EXCEEDED, exception.getErrorCode());
        }
    }

    @Test
    void confirmShipping_Success() {
        String orderNumber = "20260315-001";
        String sellerId = "seller-123";

        // Mock data
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setStatus(EOrderStatus.PAID);

        try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentAccountId).thenReturn(sellerId);

            when(orderItemRepository.findSellerIdsByOrderNumber(orderNumber))
                    .thenReturn(List.of(sellerId));
            when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(order));

            // Execute
            orderService.confirmShipping(orderNumber);

            // Verify
            assertEquals(EOrderStatus.SHIPPING, order.getStatus());
            verify(orderRepository).save(order);
        }
    }

    @Test
    void confirmShipping_OwnershipMismatch() {
        String orderNumber = "20260315-001";
        String hackerId = "hacker-456";
        String realSellerId = "seller-123";

        try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentAccountId).thenReturn(hackerId);

            when(orderItemRepository.findSellerIdsByOrderNumber(orderNumber))
                    .thenReturn(List.of(realSellerId));

            // Verify Exception
            AppException exception = assertThrows(AppException.class,
                    () -> orderService.confirmShipping(orderNumber));
            assertEquals(ErrorCode.ORDER_OWNERSHIP_MISMATCH, exception.getErrorCode());
        }
    }

    @Test
    void confirmDelivered_Success() {
        String orderNumber = "20260315-001";
        String buyerId = "user-456";

        Order order = new Order();
        order.setCustomer(Account.builder().id(buyerId).build());
        order.setStatus(EOrderStatus.SHIPPING);

        try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentAccountId).thenReturn(buyerId);
            when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(order));

            // Execute
            orderService.confirmDelivered(orderNumber);

            // Verify
            assertEquals(EOrderStatus.DELIVERED, order.getStatus());
            assertNotNull(order.getComplaintDeadline());
            verify(orderRepository).save(order);
        }
    }

    @Test
    void manualSettlement_Success() {
        String orderNumber = "20260315-001";
        String sellerId = "seller-123";

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setStatus(EOrderStatus.DELIVERED);
        // Set deadline là quá khứ để được quyết toán
        order.setComplaintDeadline(LocalDateTime.now().minusHours(1));
        order.setTotalPrice(1000L);

        Item item = new Item();
        item.setSeller(Account.builder().id(sellerId).build());

        OrderItem oi = new OrderItem();
        oi.setItem(item);
        oi.setPrice(1000L);
        oi.setQuantity(1);

        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderNumber(orderNumber)).thenReturn(List.of(oi));

        // Execute
        orderService.manualSettlement(orderNumber);

        // Verify
        verify(transactionService).executeReleaseEscrow(sellerId, 1000L);
        assertEquals(EOrderStatus.COMPLETED, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void manualSettlement_StillInComplaintPeriod() {
        String orderNumber = "20260315-001";
        Order order = new Order();
        order.setStatus(EOrderStatus.DELIVERED);
        // Deadline ở tương lai
        order.setComplaintDeadline(LocalDateTime.now().plusHours(24));

        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(order));

        // Verify Exception
        AppException exception = assertThrows(AppException.class,
                () -> orderService.manualSettlement(orderNumber));
        assertEquals(ErrorCode.STILL_IN_COMPLAINT_PERIOD, exception.getErrorCode());
    }
}
