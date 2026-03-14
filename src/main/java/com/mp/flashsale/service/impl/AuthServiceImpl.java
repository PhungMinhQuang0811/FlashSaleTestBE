package com.mp.flashsale.service.impl;

import com.mp.flashsale.dto.request.auth.LoginRequest;
import com.mp.flashsale.dto.response.ApiResponse;
import com.mp.flashsale.dto.response.auth.LoginResponse;
import com.mp.flashsale.entity.Account;
import com.mp.flashsale.exception.AppException;
import com.mp.flashsale.exception.ErrorCode;
import com.mp.flashsale.repository.AccountRepository;
import com.mp.flashsale.security.entity.UserDetailsImpl;
import com.mp.flashsale.security.service.TokenService;
import com.mp.flashsale.service.AuthService;
import com.mp.flashsale.util.JwtUtils;
import com.mp.flashsale.util.RedisUtil;
import jakarta.servlet.http.Cookie;
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
import org.springframework.web.util.WebUtils;

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
    private String refreshTokenUrl = "/api/auth/refresh-token";

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
    private String logoutUrl = "/api/auth/logout";

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
        log.info("Processing logout request");

        getTokensAndInvalidateTokens(request);

        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .data("Successfully logged out")
                .build();
        log.info("Logged out successfully");
        return sendLogoutApiResponseResponseEntity(apiResponse);
    }

    @Override
    public ResponseEntity<ApiResponse<String>> refreshToken(HttpServletRequest request) {
        log.info("Processing refresh token request");

        //get the refresh token out from cookies
        String refreshToken = getCookieValueByName(request, refreshTokenCookieName);
        if (refreshToken == null || refreshToken.trim().isEmpty()){
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        jwtUtils.validateJwtRefreshToken(refreshToken);

        if(tokenService.isRefreshTokenInvalidated(refreshToken)){
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        getTokensAndInvalidateTokens(request);

        //get user account's id from refresh token to generate new access token
        String accountId = jwtUtils.getUserAccountIdFromRefreshToken(refreshToken);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));
        //the account in the token is inactive (banned)
        if (!account.isActive()) {
            throw new AppException(ErrorCode.ACCOUNT_IS_INACTIVE);
        }
        String csrfToken = jwtUtils.generateCsrfTokenFromUserEmail(account.getEmail());

        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .data(csrfToken)
                .build();
        log.info("New refresh token is generated for account with email={}", account.getEmail());
        return sendApiResponseResponseEntity(account.getEmail(), account.getId(), apiResponse);
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
    private <T> ResponseEntity<ApiResponse<T>> sendLogoutApiResponseResponseEntity(ApiResponse<T> apiResponse) {
        //Generate token cookie
        ResponseCookie accessTokenCookie = generateCookie(accessTokenCookieName, null, contextPath, 0, true);
        ResponseCookie refreshTokenCookie = generateCookie(refreshTokenCookieName, null, refreshTokenUrl, 0, true);
        ResponseCookie refreshTokenCookieLogout = generateCookie(refreshTokenCookieName, null, logoutUrl, 0, true);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieLogout.toString())
                .body(apiResponse);
    }
    private void getTokensAndInvalidateTokens(HttpServletRequest request) {
        //get tokens out from cookies
        String accessToken = getCookieValueByName(request, accessTokenCookieName);
        String refreshToken = getCookieValueByName(request, refreshTokenCookieName);

        //refresh token exist in cookie
        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                jwtUtils.validateJwtRefreshToken(refreshToken);
                //the refresh token still not expire, invalidate it by saving to redis
                tokenService.invalidateRefreshToken(refreshToken, jwtUtils.getExpirationAtFromRefreshToken(refreshToken));
            } catch (Exception e) {
                log.info("Invalid refresh token, user can not refresh access token with this refresh token");
            }
        }

        //access token exist in cookie
        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                jwtUtils.validateJwtAccessToken(accessToken);
                //the access token still not expire, invalidate it
                tokenService.invalidateAccessToken(accessToken, jwtUtils.getExpirationAtFromAccessToken(accessToken));

            } catch (Exception e) {
                log.info("Invalid access token, user can not be authenticated with this access token");
            }
        }

        String csrfToken = request.getHeader(csrfTokenHeaderName);
        if (csrfToken != null && !csrfToken.isEmpty()) {
            try {
                jwtUtils.validateJwtCsrfToken(csrfToken);
                //the csrf token still not expire, invalidate it by saving to redis
                tokenService.invalidateCsrfToken(csrfToken, jwtUtils.getExpirationAtFromCsrfToken(csrfToken));
            } catch (Exception e) {
                log.info("Invalid csrf token, user can not access with this csrf token");
            }
        }

    }
    private String getCookieValueByName(HttpServletRequest request, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
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
