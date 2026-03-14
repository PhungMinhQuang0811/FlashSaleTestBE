package com.mp.flashsale.dto.request.user;

import com.mp.flashsale.validation.RequiredField;
import com.mp.flashsale.validation.UniqueEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Schema(name = "request.user.AccountRegisterRequest", description = "DTO contain necessary information to create new user's account")
public class AccountRegisterRequest {

    @RequiredField(fieldName = "Email")
    @Email(message = "INVALID_EMAIL")
    @UniqueEmail(message = "NOT_UNIQUE_EMAIL")
    @Schema( format = "email", example = "bich@example.com")
    String email;

    @RequiredField(fieldName = "Password")
    //The password must have at least 1 character, 1 digit and 7 characters
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{9,}$", message = "INVALID_PASSWORD")
    @Schema(example = "a12345678")
    String password;

    @RequiredField(fieldName = "The role of the account")
    @Schema(example = "true")
    String isCustomer;
}
