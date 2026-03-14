package com.mp.flashsale.controller;

import com.mp.flashsale.dto.request.auth.ChangePasswordRequest;
import com.mp.flashsale.dto.request.auth.LoginRequest;
import com.mp.flashsale.dto.response.ApiResponse;
import com.mp.flashsale.dto.response.auth.LoginResponse;
import com.mp.flashsale.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@Slf4j
public class AuthController {
    AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @GetMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        return authService.logout(request);
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<ApiResponse<String>> refreshToken(HttpServletRequest request) {
        return authService.refreshToken(request);
    }
    @GetMapping("/forgot-password/{email}")
    public ApiResponse<String> sendForgotPasswordEmail(@PathVariable("email")
                                                       @Email(message = "INVALID_EMAIL")
                                                       String email) {
        authService.sendForgotPasswordEmail(email);
        return ApiResponse.<String>builder()
                .message("An email has been send to your email address. Please click to the link in the email to change password.")
                .build();

    }
    @GetMapping("/forgot-password/verify")
    public ApiResponse<String> verifyForgotPassword(@RequestParam("t") String forgotPasswordToken) {
        return ApiResponse.<String>builder()
                .message("Verify change password request successfully!")
                .data(authService.verifyForgotPassword(forgotPasswordToken))
                .build();
    }
    @PutMapping("/forgot-password/change")
    public ApiResponse<String> changePassword(@RequestBody @Valid ChangePasswordRequest request){
        authService.changePassword(request);
        return ApiResponse.<String>builder()
                .message("Change password successfully!")
                .build();
    }
}
