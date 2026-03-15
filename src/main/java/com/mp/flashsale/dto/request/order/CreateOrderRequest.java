package com.mp.flashsale.dto.request.order;

import com.mp.flashsale.validation.RequiredField;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CreateOrderRequest {
    @RequiredField(fieldName = "item id")
    String itemId;
    @RequiredField(fieldName = "quantity")
    @Min(value = 1, message = "INVALID_VALUE_MIN")
    Integer quantity;
}
