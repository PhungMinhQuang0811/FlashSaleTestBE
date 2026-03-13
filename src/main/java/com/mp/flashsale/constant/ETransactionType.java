package com.mp.flashsale.constant;

public enum ETransactionType {
    DEPOSIT,          // Nạp tiền
    WITHDRAW,         // Rút tiền
    PAYMENT,          // Trả tiền mua hàng
    RELEASE_ESCROW,   // Admin giải ngân cho Seller
    REFUND            // Hoàn tiền cho Customer
}
