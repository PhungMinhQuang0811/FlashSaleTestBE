package com.mp.flashsale.dto.request.user;


import com.mp.flashsale.validation.RequiredField;
import com.mp.flashsale.validation.UniqueEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Schema(name = "request.user.CheckUniqueEmailRequest",description = "DTO contain necessary information to check unique email")
public class CheckUniqueEmailRequest {
    @RequiredField(fieldName = "Email")
    @Email(message = "INVALID_EMAIL")
    @UniqueEmail(message = "NOT_UNIQUE_EMAIL")
    @Schema(example = "bich@example.com")
    String email;
}
