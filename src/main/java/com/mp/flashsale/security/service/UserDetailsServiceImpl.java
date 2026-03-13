package com.mp.flashsale.security.service;

import com.mp.flashsale.entity.Account;
import com.mp.flashsale.exception.ErrorCode;
import com.mp.flashsale.repository.AccountRepository;
import com.mp.flashsale.security.entity.UserDetailsImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of UserDetailsService class
 *
 * @author DieuTTH4
 * @version 1.0
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    AccountRepository accountRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = null;
        //get the account from the repository
        account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB.getMessage()));

        if (!account.isActive() || !account.isEmailVerified()) {
            //the account of user has been banned
            throw new InternalAuthenticationServiceException(ErrorCode.ACCOUNT_IS_INACTIVE.getMessage());
        }
        //build UserDetails object
        return UserDetailsImpl.build(account);
    }
}
