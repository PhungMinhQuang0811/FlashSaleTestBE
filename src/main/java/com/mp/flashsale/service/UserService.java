package com.mp.flashsale.service;

import com.mp.flashsale.dto.request.user.AccountRegisterRequest;
import com.mp.flashsale.dto.request.user.EditPasswordRequest;
import com.mp.flashsale.dto.response.user.UserResponse;

public interface UserService {
    UserResponse addNewAccount(AccountRegisterRequest request);
    String resendVerifyEmail(String email);
    void verifyEmail(String verifyEmailToken);
    void editPassword(EditPasswordRequest request);
}
