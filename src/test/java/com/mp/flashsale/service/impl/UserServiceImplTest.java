package com.mp.flashsale.service.impl;

import com.mp.flashsale.constant.ERoleName;
import com.mp.flashsale.dto.request.user.AccountRegisterRequest;
import com.mp.flashsale.dto.response.user.UserResponse;
import com.mp.flashsale.entity.Account;
import com.mp.flashsale.entity.Role;
import com.mp.flashsale.exception.AppException;
import com.mp.flashsale.exception.ErrorCode;
import com.mp.flashsale.mapper.UserMapper;
import com.mp.flashsale.repository.AccountRepository;
import com.mp.flashsale.repository.RoleRepository;
import com.mp.flashsale.repository.WalletRepository;
import com.mp.flashsale.service.EmailService;
import com.mp.flashsale.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock AccountRepository accountRepository;
    @Mock RoleRepository roleRepository;
    @Mock WalletRepository walletRepository;
    @Mock UserMapper userMapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailService emailService;
    @Mock RedisUtil redisUtil;

    @InjectMocks
    UserServiceImpl userService;

    AccountRegisterRequest registerRequest;
    Account account;
    Role role;

    @BeforeEach
    void setUp() {
        // Cấu hình giá trị cho @Value thông qua ReflectionTestUtils
        ReflectionTestUtils.setField(userService, "frontEndBaseUrl", "http://localhost:3000");

        registerRequest = AccountRegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .isCustomer("true")
                .build();

        role = new Role();
        role.setRoleName(ERoleName.CUSTOMER);

        account = new Account();
        account.setId("uuid-123");
        account.setEmail("test@example.com");
    }

    // --- TEST: addNewAccount ---

    @Test
    void addNewAccount_Success() {
        // 1. Chuẩn bị dữ liệu: Account phải có password để encode không bị null
        account.setPassword("rawPassword");

        // 2. Stubbing: Sử dụng anyString() cho an toàn hoặc khớp chính xác giá trị
        when(userMapper.toAccount(any())).thenReturn(account);
        when(roleRepository.findByName(ERoleName.CUSTOMER)).thenReturn(Optional.of(role));

        // Thay vì dùng "" hoặc giá trị cứng, hãy dùng anyString()
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        when(accountRepository.save(any())).thenReturn(account);
        when(redisUtil.generateVerifyEmailToken(anyString())).thenReturn("token-abc");
        when(userMapper.toUserResponse(any())).thenReturn(UserResponse.builder().email("test@example.com").build());

        // When
        UserResponse response = userService.addNewAccount(registerRequest);

        // Then
        assertNotNull(response);
        verify(passwordEncoder).encode("rawPassword"); // Kiểm tra xem có đúng là encode cái password đó không
    }

    @Test
    void addNewAccount_RoleNotFound_Fail() {
        // Given
        when(userMapper.toAccount(any())).thenReturn(account);
        when(roleRepository.findByName(any())).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> userService.addNewAccount(registerRequest));
        assertEquals(ErrorCode.ROLE_NOT_FOUND_IN_DB, exception.getErrorCode());
    }

    // --- TEST: verifyEmail ---

    @Test
    void verifyEmail_Success() {
        // Given
        String token = "valid-token";
        when(redisUtil.getValueOfVerifyEmailToken(token)).thenReturn("uuid-123");
        when(accountRepository.findById("uuid-123")).thenReturn(Optional.of(account));

        // When
        userService.verifyEmail(token);

        // Then
        assertTrue(account.isEmailVerified());
        assertTrue(account.isActive());
        verify(accountRepository).save(account);
    }

    @Test
    void verifyEmail_InvalidToken_Fail() {
        // Given
        when(redisUtil.getValueOfVerifyEmailToken(anyString())).thenReturn(null);

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> userService.verifyEmail("invalid-token"));
        assertEquals(ErrorCode.INVALID_ONETIME_TOKEN, exception.getErrorCode());
    }

    @Test
    void resendVerifyEmail_AccountAlreadyVerified() {
        // Given
        account.setEmailVerified(true);
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(account));

        // When
        String result = userService.resendVerifyEmail("test@example.com");

        // Then
        assertEquals("The email is already verified", result);
        verify(emailService, never()).sendRegisterEmail(anyString(), anyString());
    }
}