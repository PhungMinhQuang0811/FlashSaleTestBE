package com.mp.flashsale.controller;

import com.mp.flashsale.dto.request.user.AccountRegisterRequest;
import com.mp.flashsale.dto.request.user.CheckUniqueEmailRequest;
import com.mp.flashsale.dto.request.user.EditPasswordRequest;
import com.mp.flashsale.dto.response.ApiResponse;
import com.mp.flashsale.dto.response.user.UserResponse;
import com.mp.flashsale.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@Slf4j
@Tag(name = "User", description = "API for managing user")
public class UserController {

    UserService userService;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    @PostMapping("/register")
    public ApiResponse<UserResponse> registerAccount(@RequestBody @Valid AccountRegisterRequest request) {
        log.info("Registering account {}", request);
        return ApiResponse.<UserResponse>builder()
                .message("Create account successfully. Please check your email inbox to verify your email address.")
                .data(userService.addNewAccount(request))
                .build();
    }

    @PostMapping("/check-unique-email")
    public ApiResponse<String> checkUniqueEmail(@RequestBody @Valid CheckUniqueEmailRequest request) {
        return ApiResponse.<String>builder()
                .message("Email is unique.")
                .build();
    }

    @GetMapping("/resend-verify-email/{email}")
    public ApiResponse<String> resendVerifyEmail(@PathVariable("email")
                                                 @Email(message = "INVALID_EMAIL")
                                                 String email) {
        return ApiResponse.<String>builder()
                .message(userService.resendVerifyEmail(email))
                .build();
    }

    @GetMapping("/verify-email")
    public ApiResponse<String> verifyEmail(@RequestParam("t") String verifyEmailToken) {
        userService.verifyEmail(verifyEmailToken);
        return ApiResponse.<String>builder()
                .message("Verify email successfully! Now you can use your account to login.")
                .build();
    }

    /**
     * Changes the password of the current user.
     *
     * @param request the request containing current, new, and confirm passwords
     * @return a response indicating success or failure
     */
    @PutMapping("/edit-password")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'CAR_OWNER')")
    public ApiResponse<String> editPassword(@RequestBody @Valid EditPasswordRequest request) {
        log.info("Changing password for user");
        userService.editPassword(request);
        return ApiResponse.<String>builder()
                .message("Password updated successfully")
                .build();
    }

}
