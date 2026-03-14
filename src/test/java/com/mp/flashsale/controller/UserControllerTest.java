package com.mp.flashsale.controller;

import com.mp.flashsale.constant.ERoleName;
import com.mp.flashsale.dto.request.user.AccountRegisterRequest;
import com.mp.flashsale.dto.request.user.CheckUniqueEmailRequest;
import com.mp.flashsale.dto.request.user.EditPasswordRequest;
import com.mp.flashsale.dto.response.ApiResponse;
import com.mp.flashsale.dto.response.user.UserResponse;
import com.mp.flashsale.exception.AppException;
import com.mp.flashsale.exception.ErrorCode;
import com.mp.flashsale.repository.AccountRepository;
import com.mp.flashsale.service.UserService;
import com.mp.flashsale.validation.validator.UniqueEmailValidator;
import jakarta.validation.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserService userService;
    @InjectMocks
    private UserController userController;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private UniqueEmailValidator uniqueEmailValidator;


    @Test
    void registerAccount() {
        // Given
        AccountRegisterRequest request = new AccountRegisterRequest();
        request.setIsCustomer("true");

        UserResponse expectedUserResponse = new UserResponse();
        expectedUserResponse.setRole(ERoleName.CUSTOMER.name());

        // Configure the service mock to return the expected response
        when(userService.addNewAccount(request)).thenReturn(expectedUserResponse);

        // When
        ApiResponse<UserResponse> result = userController.registerAccount(request);

        // Then
        assertNotNull(result, "ApiResponse should not be null");
        assertEquals(expectedUserResponse, result.getData(), "Returned user response does not match expected");

        // Verify that the service's addNewAccount method was invoked exactly once with the given request
        verify(userService, times(1)).addNewAccount(request);
    }

    /**
     * Test edit password successfully
     */
    @Test
    void editPassword_Success() {
        // Given
        EditPasswordRequest request = new EditPasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newSecurePassword");

        doNothing().when(userService).editPassword(request);

        // When
        ApiResponse<String> result = userController.editPassword(request);

        // Then
        assertNotNull(result);
        verify(userService, times(1)).editPassword(request);
    }

    @Test
    void editPassword_Fail_IncorrectPassword() {
        EditPasswordRequest request = new EditPasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newSecurePassword");

        doThrow(new AppException(ErrorCode.INCORRECT_PASSWORD)).when(userService).editPassword(request);

        AppException exception = assertThrows(AppException.class, () -> userController.editPassword(request));
        assertEquals(ErrorCode.INCORRECT_PASSWORD, exception.getErrorCode());
        verify(userService, times(1)).editPassword(request);
    }

    @Test
    void editPassword_Fail_UserNotFound() {
        EditPasswordRequest request = new EditPasswordRequest();
        doThrow(new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB)).when(userService).editPassword(request);

        AppException exception = assertThrows(AppException.class, () -> userController.editPassword(request));
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB, exception.getErrorCode());
        verify(userService, times(1)).editPassword(request);
    }

    @Test
    void shouldPassValidationWhenEmailIsUnique() {
        // Given: Email not existed
        CheckUniqueEmailRequest request = new CheckUniqueEmailRequest("unique@email.com");

        // Mock repo
        when(accountRepository.findByEmail("unique@email.com")).thenReturn(Optional.empty());

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean isValid = uniqueEmailValidator.isValid("unique@email.com", context);
        ApiResponse<String> result = userController.checkUniqueEmail(request);

        assertTrue(isValid);
        assertNotNull(result);
    }

    @Test
    void testResendVerifyEmail_Success() {
        // Arrange
        String email = "test@example.com";
        String expectedMessage = "The verify email is sent successfully. Please check your inbox again and follow instructions to verify your email.";
        when(userService.resendVerifyEmail(email)).thenReturn(expectedMessage);

        // Act
        ApiResponse<String> response = userController.resendVerifyEmail(email);

        // Assert
        assertNotNull(response);
        assertEquals(expectedMessage, response.getMessage());
        verify(userService).resendVerifyEmail(email);
    }

    @Test
    void testVerifyEmail_Success() {
        // Arrange
        String token = "valid-token";
        doNothing().when(userService).verifyEmail(token);

        // Act
        ApiResponse<String> response = userController.verifyEmail(token);

        // Assert
        assertNotNull(response);
        assertEquals("Verify email successfully! Now you can use your account to login.", response.getMessage());
        verify(userService).verifyEmail(token); // Kiểm tra userService.verifyEmail() đã được gọi đúng cách
    }

    @Test
    void testVerifyEmail_InvalidToken() {
        // Arrange
        String invalidToken = "invalid-token";
        doThrow(new AppException(ErrorCode.INVALID_ONETIME_TOKEN))
                .when(userService).verifyEmail(invalidToken);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            userController.verifyEmail(invalidToken);
        });

        assertEquals(ErrorCode.INVALID_ONETIME_TOKEN, exception.getErrorCode());
        verify(userService).verifyEmail(invalidToken); // Kiểm tra userService.verifyEmail() đã được gọi đúng cách
    }
}