package com.mp.flashsale.constant;

public enum EOrderStatus {
    PENDING,           // Chờ thanh toán
    PAID,              // Đã thanh toán (Tiền ở ví Admin)
    SHIPPING,          // Đang giao hàng
    DELIVERED,         // Đã giao (Chờ hết hạn khiếu nại)
    COMPLETED,         // Thành công (Tiền đã về ví Seller)
    CANCELLED,         // Đã hủy
    DISPUTING,         // Đang khiếu nại/tranh chấp
    REFUNDED           // Đã hoàn tiền
}
