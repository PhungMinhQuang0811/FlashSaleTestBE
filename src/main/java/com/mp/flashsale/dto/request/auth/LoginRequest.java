package com.mp.flashsale.dto.request.auth;

import com.mp.flashsale.validation.RequiredField;
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
@Schema(name = "request.auth.LoginRequest", description = "DTO contain necessary information to login into the system")
public class LoginRequest {
    @RequiredField(fieldName = "Email")
    @Email(message = "INVALID_EMAIL")
    @Schema( format = "email", example = "bich@example.com")
    String email;

    @RequiredField(fieldName = "Password")
    //The password must have at least 1 character, 1 digit and 7 characters
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{9,}$", message = "INVALID_PASSWORD")
    @Schema(example = "a12345678")
    String password;
}
