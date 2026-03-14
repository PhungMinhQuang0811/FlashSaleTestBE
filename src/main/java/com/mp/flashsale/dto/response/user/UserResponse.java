package com.mp.flashsale.dto.response.user;

import com.mp.flashsale.constant.ERoleName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Schema(name = "response.user.UserResponse", description = "Data of an user")
public class UserResponse {
    @Schema(description = "Account ID")
    String id;

    @Schema(description = "User's email", example = "buyer@example.com")
    String email;

    @Schema(description = "User's role name", example = "CUSTOMER")
    String role;
}
