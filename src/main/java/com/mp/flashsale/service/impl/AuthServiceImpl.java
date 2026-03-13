package com.mp.flashsale.service.impl;

import com.mp.flashsale.dto.request.auth.LoginRequest;
import com.mp.flashsale.dto.response.ApiResponse;
import com.mp.flashsale.dto.response.auth.LoginResponse;
import com.mp.flashsale.exception.AppException;
import com.mp.flashsale.exception.ErrorCode;
import com.mp.flashsale.repository.AccountRepository;
import com.mp.flashsale.security.entity.UserDetailsImpl;
import com.mp.flashsale.security.service.TokenService;
import com.mp.flashsale.service.AuthService;
import com.mp.flashsale.util.JwtUtils;
import com.mp.flashsale.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Value("${front-end.base-url}")
    @NonFinal
    private String frontEndBaseUrl;

    //=======================================
    @Value("${server.servlet.context-path}")
    @NonFinal
    private String contextPath;

    @Value("${application.security.jwt.access-token-cookie-name}")
    @NonFinal
    private String accessTokenCookieName;

    @Value("${application.security.jwt.access-token-expiration}")
    @NonFinal
    private long accessTokenExpiration;
    //=======================================
    @NonFinal
    private String refreshTokenUrl = "/karental/auth/refresh-token";

    @Value("${application.security.jwt.refresh-token-cookie-name}")
    @NonFinal
    private String refreshTokenCookieName;

    @Value("${application.security.jwt.refresh-token-expiration}")
    @NonFinal
    private long refreshTokenExpiration;
    //=======================================
    @Value("${application.security.jwt.csrf-token-cookie-name}")
    @NonFinal
    private String csrfTokenCookieName;

    @Value("${application.security.jwt.csrf-token-header-name}")
    @NonFinal
    private String csrfTokenHeaderName;

    @NonFinal
    private String logoutUrl = "/karental/auth/logout";

    @Value("${application.domain-name}")
    @NonFinal
    private String domain;

    AuthenticationManager authenticationManager;

    JwtUtils jwtUtils;
    RedisUtil redisUtil;
    PasswordEncoder passwordEncoder;

    TokenService tokenService;
//    EmailService emailService;

    AccountRepository accountRepository;

    @Override
    public ResponseEntity<ApiResponse<LoginResponse>> login(LoginRequest request) {
        log.info("Processing login request, email={}", request.getEmail());
        //authenticate user's login information
        Authentication authentication = null;

        try {
            authentication = authenticationManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
                    );
        } catch (InternalAuthenticationServiceException e) {
            log.info("Login fail, account is inactive - email={}", request.getEmail());
            throw new AppException(ErrorCode.ACCOUNT_IS_INACTIVE);
        } catch (BadCredentialsException e) {
            log.info("Login fail, invalid login information - email={}", request.getEmail());
            throw new AppException(ErrorCode.INVALID_LOGIN_INFORMATION);
        }

        //set Authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        //put user's role and fullname in response
        String role = userDetails.getRole().getRoleName().toString();
        String csrfToken = jwtUtils.generateCsrfTokenFromUserEmail(userDetails.getEmail());
        ApiResponse<LoginResponse> apiResponse = ApiResponse.<LoginResponse>builder()
                .data(new LoginResponse(role, csrfToken))
                .build();
        log.info("Account with email={} logged in successfully", request.getEmail());
        return sendApiResponseResponseEntity(userDetails.getEmail(), userDetails.getAccoutnId(), apiResponse);
    }

    @Override
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<String>> refreshToken(HttpServletRequest request) {
        return null;
    }
    private <T> ResponseEntity<ApiResponse<T>> sendApiResponseResponseEntity(String email, String accountId, ApiResponse<T> apiResponse) {
        //generate tokens
        String accessToken = jwtUtils.generateAccessTokenFromUserEmail(email);
        String refreshToken = jwtUtils.generateRefreshTokenFromAccountId(accountId);

        //Generate token cookie
        ResponseCookie accessTokenCookie = generateCookie(accessTokenCookieName, accessToken, contextPath, accessTokenExpiration, true);
        ResponseCookie refreshTokenCookie = generateCookie(refreshTokenCookieName, refreshToken, refreshTokenUrl, refreshTokenExpiration, true);
        ResponseCookie refreshTokenCookieLogout = generateCookie(refreshTokenCookieName, refreshToken, logoutUrl, refreshTokenExpiration, true);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE,refreshTokenCookieLogout.toString())
                .body(apiResponse);
    }
    private ResponseCookie generateCookie(String cookieName, String cookieValue, String path, long maxAgeMiliseconds, boolean isHttpOnly) {
        return ResponseCookie
                .from(cookieName, cookieValue)
//                .path(path)
                .path("/")
//                .domain(domain)
                .maxAge(maxAgeMiliseconds / 1000) // seconds ~ 1days
                .httpOnly(isHttpOnly)
//                .secure(true)
                .secure(false)
//                .sameSite("None")
                .sameSite("Lax")
                .build();
    }
}
