package com.mp.flashsale.repository.jpa;

import com.mp.flashsale.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<Account, String> {
    Optional<Account> findByEmail(String email);
    Account findByRoleId(int i);
}
