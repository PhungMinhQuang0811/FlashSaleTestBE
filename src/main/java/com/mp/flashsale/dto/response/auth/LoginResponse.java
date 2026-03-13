package com.mp.flashsale.dto.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Schema(name = "response.auth.LoginResponse", description = "Data return after login success fully")
public class LoginResponse {
    @Schema(example = "CUSTOMER")
    String userRole;
    @Schema(example = "hoadsfhiawuehdsgvf")
    String csrfToken;

}
