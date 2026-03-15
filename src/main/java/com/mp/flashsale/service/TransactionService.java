package com.mp.flashsale.service;

import com.mp.flashsale.dto.response.transaction.TransactionPaymentURLResponse;
import com.mp.flashsale.dto.response.transaction.TransactionResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface TransactionService {
    void executeEscrowPayment(String customerId, Long amount);
    void executeReleaseEscrow(String sellerId, Long amount);
    void executeDeposit(String accountId, Long amount);
    void executeWithdraw(String accountId, Long amount);
    TransactionPaymentURLResponse createDepositUrl(Long amount, HttpServletRequest request);
    TransactionResponse processVnpayCallback(Map<String, String> params);
}
