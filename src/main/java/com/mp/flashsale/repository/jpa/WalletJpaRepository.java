package com.mp.flashsale.repository.jpa;

import com.mp.flashsale.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletJpaRepository extends JpaRepository<Wallet, String> {

}
