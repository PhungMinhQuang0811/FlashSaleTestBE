package com.mp.flashsale.dto.request.transaction;

import com.mp.flashsale.constant.ETransactionType;
import com.mp.flashsale.validation.ValidTransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TransactionRequest {
    @ValidTransactionType(message = "INVALID_TRANSACTION_TYPE")
    @NotNull

    ETransactionType type;

    String orderNumber;
    @Schema(
            description = "Car Name(brand + model)",
            example = "Toyota Camry"
    )
     String itemName;
    @Schema(
            description = "Amount of the transaction",
            example = "1000000"
    )
    long amount;

    String message;
    @Schema(
            description = "Amount of the transaction",
            example = "127.2.0.34"
    )
    private String ipAddress;
}
