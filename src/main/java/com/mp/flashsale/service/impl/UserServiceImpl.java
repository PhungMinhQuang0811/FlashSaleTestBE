package com.mp.flashsale.service.impl;

import com.mp.flashsale.constant.ERoleName;
import com.mp.flashsale.dto.request.user.AccountRegisterRequest;
import com.mp.flashsale.dto.request.user.EditPasswordRequest;
import com.mp.flashsale.dto.response.user.UserResponse;
import com.mp.flashsale.entity.Account;
import com.mp.flashsale.entity.Role;
import com.mp.flashsale.entity.Wallet;
import com.mp.flashsale.exception.AppException;
import com.mp.flashsale.exception.ErrorCode;
import com.mp.flashsale.mapper.UserMapper;
import com.mp.flashsale.repository.AccountRepository;
import com.mp.flashsale.repository.RoleRepository;
import com.mp.flashsale.repository.WalletRepository;
import com.mp.flashsale.service.EmailService;
import com.mp.flashsale.service.UserService;
import com.mp.flashsale.util.RedisUtil;
import com.mp.flashsale.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImpl implements UserService {
    @Value("${front-end.base-url}")
    @NonFinal
    private String frontEndBaseUrl;

    AccountRepository accountRepository;
    RoleRepository roleRepository;
    WalletRepository walletRepository;

    UserMapper userMapper;

    PasswordEncoder passwordEncoder;
    EmailService emailService;
    RedisUtil redisUtil;

    @Override
    public UserResponse addNewAccount(AccountRegisterRequest request){
        log.info("Create new account");
        Account account = userMapper.toAccount(request);
        //set role for the account
        Optional<Role> role = roleRepository
                .findByName(request.getIsCustomer().equals("true") ? ERoleName.CUSTOMER : ERoleName.SELLER);
        if (role.isPresent()){
            account.setRole(role.get());
        } else {
            log.info("Create new account failed");
            throw new AppException(ErrorCode.ROLE_NOT_FOUND_IN_DB);
        }
        account.setActive(false); //set status of the account, this will change after the email is verified
        //encode password
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account = accountRepository.save(account);

        //create user's wallet
        Wallet wallet = Wallet.builder()
                .account(account)
                .balance(0L)
                .build();
        walletRepository.save(wallet);

        sendVerifyEmail(account);
        log.info("Account created successfully, email={}, accountId={}", account.getEmail(), account.getId());
        return userMapper.toUserResponse(account);
    }

    @Override
    public String resendVerifyEmail(String email){
        log.info("User request sending verify email for {}", email);
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_USED_BY_ANY_ACCOUNT));
        //account already verify
        if (account.isEmailVerified()){
            //not send email any more
            return "The email is already verified";
        }
        sendVerifyEmail(account);
        return "The verify email is sent successfully. Please check your inbox again and follow instructions to verify your email.";
    }

    @Override
    public void verifyEmail(String verifyEmailToken){
        log.info("Verify email");
        String accountId = redisUtil.getValueOfVerifyEmailToken(verifyEmailToken);
        //check if the token valid
        if (accountId != null && !accountId.isEmpty()) {
            //token valid: exist in redis and still not expired
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_USED_BY_ANY_ACCOUNT));
            //Email is not already verify
            if (!account.isEmailVerified()){
                //set the email verified status to true
                account.setEmailVerified(true);
                //activate the account, so that user could use this account to login
                account.setActive(true);
                accountRepository.save(account);
                log.info("Email={} verified successfully", account.getEmail());
            }
        } else {
            //The verify email token is not valid or
            //has expired or
            //has been used
            log.info("Verify token invalid, can not verify email");
            throw new AppException(ErrorCode.INVALID_ONETIME_TOKEN);
        }
    }

    @Override
    public void editPassword(EditPasswordRequest request) {
        // Get information of current password
        String accountID = SecurityUtil.getCurrentAccountId();
        Account account = SecurityUtil.getCurrentAccount();

        // Confirm current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPassword())) {
            throw new AppException(ErrorCode.INCORRECT_PASSWORD);
        }

        // Check new password not null
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        // Encode and update new password
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }
    private void sendVerifyEmail(Account account) {
        log.info("Send verify email to user.");
        //send email to verified user email
        String verifyEmailToken = redisUtil.generateVerifyEmailToken(account.getId());
        String confirmUrl = frontEndBaseUrl + "/#/user/verify-email?t=" + verifyEmailToken;
        log.info("Verify email url: {}", confirmUrl);
        //sending email
        emailService.sendRegisterEmail(account.getEmail(), confirmUrl);
    }
}
