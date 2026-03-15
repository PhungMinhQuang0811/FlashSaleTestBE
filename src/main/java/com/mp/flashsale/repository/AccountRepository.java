package com.mp.flashsale.repository;

import com.mp.flashsale.entity.Account;

import javax.swing.text.html.Option;
import java.util.Optional;



public interface AccountRepository {
    Optional<Account> findByEmail(String email);

    Account findByRoleId(int i);
    Account save(Account account);
    Optional<Account> findById(String id);
}
