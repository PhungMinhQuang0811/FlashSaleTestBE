package com.mp.flashsale.controller;

import com.mp.flashsale.dto.response.ApiResponse;
import com.mp.flashsale.dto.response.transaction.TransactionPaymentURLResponse;
import com.mp.flashsale.dto.response.transaction.TransactionResponse;
import com.mp.flashsale.service.TransactionService;
import com.mp.flashsale.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/flash-sale/transaction")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionController {
    TransactionService transactionService;

    @PostMapping("/withdraw")
    public ApiResponse<Void> withdraw(@RequestBody Long amount) {
        String currentAccountId = SecurityUtil.getCurrentAccountId();
        transactionService.executeWithdraw(currentAccountId, amount);
        return ApiResponse.<Void>builder()
                .message("Withdrawal successful.")
                .build();
    }
    @PostMapping("/deposit")
    public ApiResponse<TransactionPaymentURLResponse> deposit(
            @RequestBody Long amount,
            HttpServletRequest request) {
        return ApiResponse.<TransactionPaymentURLResponse>builder()
                .data(transactionService.createDepositUrl(amount, request))
                .build();
    }

    @GetMapping("/vnpay-callback")
    public ApiResponse<TransactionResponse> callback(@RequestParam Map<String, String> params) {
        return ApiResponse.<TransactionResponse>builder()
                .data(transactionService.processVnpayCallback(params))
                .build();
    }
}
