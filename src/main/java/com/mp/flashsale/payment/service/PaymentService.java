package com.mp.flashsale.payment.service;


import com.mp.flashsale.payment.dto.request.InitPaymentRequest;
import com.mp.flashsale.payment.dto.response.InitPaymentResponse;

public interface PaymentService {
    InitPaymentResponse initPayment(InitPaymentRequest request);
}
