package com.mp.flashsale.service.impl;

import com.mp.flashsale.constant.ERoleName;
import com.mp.flashsale.dto.request.auth.ChangePasswordRequest;
import com.mp.flashsale.dto.request.auth.LoginRequest;
import com.mp.flashsale.dto.response.ApiResponse;
import com.mp.flashsale.dto.response.auth.LoginResponse;
import com.mp.flashsale.entity.Account;
import com.mp.flashsale.exception.AppException;
import com.mp.flashsale.exception.ErrorCode;
import com.mp.flashsale.repository.AccountRepository;
import com.mp.flashsale.security.entity.UserDetailsImpl;
import com.mp.flashsale.security.service.TokenService;
import com.mp.flashsale.util.JwtUtils;
import com.mp.flashsale.util.RedisUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtUtils jwtUtils;
    @Mock
    RedisUtil redisUtil;
    @Mock
    TokenService tokenService;
    @Mock
    AccountRepository accountRepository;
    @InjectMocks
    AuthServiceImpl authService;

    private LoginRequest loginRequest;


    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("dev@fpt.edu.vn", "password123");
        ReflectionTestUtils.setField(authService, "accessTokenCookieName", "flashsale-jwt");
        ReflectionTestUtils.setField(authService, "refreshTokenCookieName", "flashsale-refresh");
        // Nếu có domain, path, expiration thì cũng gán luôn ở đây
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", 3600000L);
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", 86400000L);
    }

    @Nested
    @DisplayName("Tests cho phương thức Login")
    class LoginTests {
        @Test
        @DisplayName("Login thành công - Trả về 200 OK và Set-Cookie")
        void login_Success() {

            UserDetailsImpl mockUserDetails = mock(UserDetailsImpl.class);
            when(mockUserDetails.getEmail()).thenReturn("dev@fpt.edu.vn");
            when(mockUserDetails.getAccoutnId()).thenReturn("acc-123");

            var mockRole = mock(com.mp.flashsale.entity.Role.class);
            when(mockRole.getRoleName()).thenReturn(ERoleName.CUSTOMER);
            when(mockUserDetails.getRole()).thenReturn(mockRole);

            // Giả lập lệnh bài Authentication
            Authentication mockAuth = mock(Authentication.class);
            when(mockAuth.getPrincipal()).thenReturn(mockUserDetails);

            // QUAN TRỌNG: Dặn Manager khi gọi authenticate thì TRẢ VỀ mockAuth (không được trả về null)
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuth);

            // Giả lập JwtUtils để không bị lỗi Null ở các dòng tiếp theo
            when(jwtUtils.generateCsrfTokenFromUserEmail(anyString())).thenReturn("mock-csrf");
            when(jwtUtils.generateAccessTokenFromUserEmail(anyString())).thenReturn("mock-at");
            when(jwtUtils.generateRefreshTokenFromAccountId(anyString())).thenReturn("mock-rt");

            // 2. THỰC HIỆN (WHEN)
            ResponseEntity<ApiResponse<LoginResponse>> response = authService.login(loginRequest);

            // 3. KIỂM TRA (THEN)
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody().getData().getCsrfToken()).isEqualTo("mock-csrf");
        }

        @Test
        @DisplayName("Login thất bại - Tài khoản bị khóa (Inactive)")
        void login_Fail_AccountInactive() {
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new InternalAuthenticationServiceException("Inactive"));

            AppException ex = assertThrows(AppException.class, () -> authService.login(loginRequest));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_IS_INACTIVE);
        }

        @Test
        @DisplayName("Login thất bại - Sai mật khẩu")
        void login_Fail_InvalidCredentials() {
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            AppException ex = assertThrows(AppException.class, () -> authService.login(loginRequest));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_LOGIN_INFORMATION);
        }
    }

    @Nested
    @DisplayName("Tests cho phương thức Logout")
    class LogoutTests {
        @Test
        @DisplayName("Logout thành công - Vô hiệu hóa token và xóa cookie")
        void logout_Success() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            // Giả lập có cookie gửi lên
            Cookie atCookie = new Cookie("flashsale-jwt", "valid-at");
            when(request.getCookies()).thenReturn(new Cookie[]{atCookie});
            when(jwtUtils.getExpirationAtFromAccessToken(anyString())).thenReturn(Instant.now());

            ResponseEntity<ApiResponse<String>> response = authService.logout(request);

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            verify(tokenService, atLeastOnce()).invalidateAccessToken(anyString(), any());
        }
    }

    @Nested
    @DisplayName("Tests cho phương thức Refresh Token")
    class RefreshTokenTests {
        @Test
        @DisplayName("Refresh thành công - Cấp bộ token mới và hủy token cũ")
        void refreshToken_Success() {
            // 1. GIVEN
            HttpServletRequest request = mock(HttpServletRequest.class);
            String oldRt = "valid-old-refresh-token";
            String email = "user@example.com";
            String accountId = "ACC_123";

            // SỬA TÊN COOKIE Ở ĐÂY: Phải khớp với "flashsale-refresh" trong setUp()
            Cookie rtCookie = new Cookie("flashsale-refresh", oldRt);
            when(request.getCookies()).thenReturn(new Cookie[]{rtCookie});

            // Mock Account
            Account mockAccount = new Account();
            mockAccount.setId(accountId);
            mockAccount.setEmail(email);
            mockAccount.setActive(true);

            when(tokenService.isRefreshTokenInvalidated(oldRt)).thenReturn(false);
            when(jwtUtils.getUserAccountIdFromRefreshToken(oldRt)).thenReturn(accountId);
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

            String newCsrf = "new-csrf-token";
            when(jwtUtils.generateCsrfTokenFromUserEmail(email)).thenReturn(newCsrf);

            // 2. WHEN
            ResponseEntity<ApiResponse<String>> response = authService.refreshToken(request);

            // 3. THEN
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getData()).isEqualTo(newCsrf);
        }

        @Test
        @DisplayName("Refresh thất bại - Token đã nằm trong blacklist")
        void refreshToken_Fail_Blacklisted() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            Cookie rtCookie = new Cookie("flashsale-refresh", "blacklisted-rt");
            when(request.getCookies()).thenReturn(new Cookie[]{rtCookie});
            when(tokenService.isRefreshTokenInvalidated(anyString())).thenReturn(true);

            assertThrows(AppException.class, () -> authService.refreshToken(request));
        }
    }

    @Nested
    @DisplayName("Tests cho tính năng Forgot Password (Service Logic)")
    class ForgotPasswordExtendedTests {

        @Test
        @DisplayName("Gửi email quên mật khẩu thất bại - Email không tồn tại")
        void sendForgotPasswordEmail_EmailNotFound_Fail() {
            // Given
            String email = "notfound@example.com";
            when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());

            // When & Then
            AppException ex = assertThrows(AppException.class, () -> authService.sendForgotPasswordEmail(email));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.EMAIL_NOT_USED_BY_ANY_ACCOUNT);
        }

        @Test
        @DisplayName("Xác thực Forgot Token thất bại - Tài khoản đã bị khóa (sau khi cấp token)")
        void verifyForgotPassword_AccountLocked_Fail() {
            // Given
            String token = "valid-token-but-locked-user";
            Account lockedAccount = new Account();
            lockedAccount.setId("acc-123");
            lockedAccount.setActive(false); // User bị khóa bởi admin sau khi nhận mail

            when(redisUtil.getValueOfForgotPasswordToken(token)).thenReturn("acc-123");
            when(accountRepository.findById("acc-123")).thenReturn(Optional.of(lockedAccount));

            // When & Then
            AppException ex = assertThrows(AppException.class, () -> authService.verifyForgotPassword(token));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_IS_INACTIVE);
        }

        @Test
        @DisplayName("Xác thực Forgot Token thất bại - Token rỗng hoặc null")
        void verifyForgotPassword_TokenEmpty_Fail() {
            // When & Then (accountId trả về rỗng từ Redis)
            when(redisUtil.getValueOfForgotPasswordToken("empty-token")).thenReturn("");

            assertThrows(AppException.class, () -> authService.verifyForgotPassword("empty-token"));
        }

        @Test
        @DisplayName("Đổi mật khẩu thất bại - Token hết hạn ngay lúc nhấn Submit")
        void changePassword_TokenExpiredAtSubmit_Fail() {
            // Given
            ChangePasswordRequest request = new ChangePasswordRequest("expired-at-submit", "newPass");
            when(redisUtil.getValueOfForgotPasswordToken(anyString())).thenReturn(null);

            // When & Then
            AppException ex = assertThrows(AppException.class, () -> authService.changePassword(request));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_FORGOT_PASSWORD_TOKEN);
        }

        @Test
        @DisplayName("Đổi mật khẩu thất bại - Account bỗng dưng biến mất khỏi DB")
        void changePassword_AccountDisappeared_Fail() {
            // Given
            ChangePasswordRequest request = new ChangePasswordRequest("token-exists", "newPass");
            when(redisUtil.getValueOfForgotPasswordToken("token-exists")).thenReturn("ghost-id");
            when(accountRepository.findById("ghost-id")).thenReturn(Optional.empty());

            // When & Then
            AppException ex = assertThrows(AppException.class, () -> authService.changePassword(request));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB);
        }
    }
}