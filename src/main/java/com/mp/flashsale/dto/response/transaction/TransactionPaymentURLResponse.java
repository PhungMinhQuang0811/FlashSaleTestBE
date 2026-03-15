package com.mp.flashsale.dto.response.transaction;

import com.mp.flashsale.payment.dto.response.InitPaymentResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TransactionPaymentURLResponse {
    @Schema(description = "transaction response")
    TransactionResponse transactionResponse;
    @Schema(description = "payment url response")
    InitPaymentResponse payment;
}
