package com.mp.flashsale.controller;

import com.mp.flashsale.dto.request.auth.LoginRequest;
import com.mp.flashsale.dto.response.ApiResponse;
import com.mp.flashsale.dto.response.auth.LoginResponse;

import com.mp.flashsale.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @Mock
    AuthService authService;
    @InjectMocks
    AuthController authController;

    @Test
    void login_shouldCallAuthenticationService() {
        // Given
        LoginRequest loginRequest = new LoginRequest("abc@example.com", "password");
        ApiResponse<LoginResponse> apiResponse = mock(ApiResponse.class);
        ResponseEntity<ApiResponse<LoginResponse>> mockResponse = ResponseEntity.ok(apiResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        // When
        ResponseEntity<?> response = authController.login(loginRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(apiResponse, response.getBody());
        verify(authService, times(1)).login(any(LoginRequest.class));
    }

}
