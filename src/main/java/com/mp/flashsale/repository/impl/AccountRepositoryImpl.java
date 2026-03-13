package com.mp.flashsale.repository.impl;

import com.mp.flashsale.entity.Account;
import com.mp.flashsale.repository.AccountRepository;
import com.mp.flashsale.repository.jpa.AccountJpaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountRepositoryImpl implements AccountRepository {
    AccountJpaRepository accountJpaRepository;
    @Override
    public Optional<Account> findByEmail(String email) {
        return accountJpaRepository.findByEmail(email);
    }

    @Override
    public Account findByRoleId(int i) {
        return accountJpaRepository.findByRoleId(i);
    }

    @Override
    public Account save(Account account) {
        return accountJpaRepository.save(account);
    }
}
