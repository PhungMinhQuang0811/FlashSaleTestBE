package com.mp.flashsale.service;

public interface EmailService {
    void sendRegisterEmail(String to, String confirmUrl);
    void sendForgotPasswordEmail(String to, String forgotPasswordUrl);
    void sendOrderSuccessEmail(String toCustomer, String itemName, String orderNumber, long price);
    void sendNewOrderNotificationToSeller(String toSeller, String itemName, String orderNumber);
    void sendWalletUpdateEmail(String to, String walletUrl);
}
