package com.mp.flashsale.controller;

import com.mp.flashsale.dto.request.auth.ChangePasswordRequest;
import com.mp.flashsale.dto.request.auth.LoginRequest;
import com.mp.flashsale.dto.response.ApiResponse;
import com.mp.flashsale.dto.response.auth.LoginResponse;

import com.mp.flashsale.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    @Test
    void logout_shouldCallAuthenticationService() {
        // Given
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        ApiResponse<String> apiResponse = mock(ApiResponse.class);
        ResponseEntity<ApiResponse<String>> mockResponse = ResponseEntity.ok(apiResponse);

        when(authService.logout(any(HttpServletRequest.class))).thenReturn(mockResponse);

        // When
        ResponseEntity<?> response = authController.logout(mockRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(apiResponse, response.getBody());
        verify(authService, times(1)).logout(any(HttpServletRequest.class));
    }
    @Test
    void refreshToken_shouldCallAuthenticationService() {
        // Given
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        ApiResponse<String> apiResponse = mock(ApiResponse.class);
        ResponseEntity<ApiResponse<String>> mockResponse = ResponseEntity.ok(apiResponse);

        when(authService.refreshToken(any(HttpServletRequest.class))).thenReturn(mockResponse);

        // When
        ResponseEntity<?> response = authController.refreshToken(mockRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(apiResponse, response.getBody());
        verify(authService, times(1)).refreshToken(any(HttpServletRequest.class));
    }
    @Test
    void testSendForgotPasswordEmail_ValidEmail() {
        // Arrange
        String email = "test@example.com";

        // Act
        ApiResponse<String> response = authController.sendForgotPasswordEmail(email);

        // Assert
        assertEquals("An email has been send to your email address. Please click to the link in the email to change password.",
                response.getMessage());
        verify(authService, times(1)).sendForgotPasswordEmail(email);
    }

    @Test
    void testVerifyForgotPassword_ValidToken() {
        // Arrange
        String token = "valid-token";
        String expectedResponse = "AccountId123";
        when(authService.verifyForgotPassword(token)).thenReturn(expectedResponse);

        // Act
        ApiResponse<String> response = authController.verifyForgotPassword(token);

        // Assert
        assertEquals("Verify change password request successfully!", response.getMessage());
        assertEquals(expectedResponse, response.getData());
        verify(authService, times(1)).verifyForgotPassword(token);
    }

    @Test
    void testVerifyForgotPassword_InvalidToken() {
        // Arrange
        String token = "invalid-token";
        when(authService.verifyForgotPassword(token)).thenThrow(new RuntimeException("Invalid token"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authController.verifyForgotPassword(token)
        );
        assertEquals("Invalid token", exception.getMessage());
        verify(authService, times(1)).verifyForgotPassword(token);
    }

    @Test
    void testChangePassword_ValidRequest() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setForgotPasswordToken("valid-token");
        request.setNewPassword("NewP@ssword123");

        doNothing().when(authService).changePassword(request);

        // Act
        ApiResponse<String> response = authController.changePassword(request);

        // Assert
        assertEquals("Change password successfully!", response.getMessage());
        verify(authService, times(1)).changePassword(request);
    }

}
