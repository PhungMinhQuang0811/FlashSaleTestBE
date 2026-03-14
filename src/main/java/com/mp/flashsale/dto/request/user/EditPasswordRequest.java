package com.mp.flashsale.dto.request.user;

import com.mp.flashsale.validation.RequiredField;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EditPasswordRequest {
    @RequiredField(fieldName = "Current password")
    String currentPassword;

    @RequiredField(fieldName = "New password")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{9,}$", message = "INVALID_PASSWORD")
    String newPassword;

}
