package com.mp.flashsale.service;

import com.mp.flashsale.dto.request.auth.LoginRequest;
import com.mp.flashsale.dto.response.ApiResponse;
import com.mp.flashsale.dto.response.auth.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    public ResponseEntity<ApiResponse<LoginResponse>> login(LoginRequest request);
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request);
    public ResponseEntity<ApiResponse<String>> refreshToken(HttpServletRequest request);
}
