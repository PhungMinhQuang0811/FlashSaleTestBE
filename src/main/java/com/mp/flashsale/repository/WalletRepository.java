package com.mp.flashsale.repository;

import com.mp.flashsale.entity.Wallet;

import java.util.Optional;

public interface WalletRepository {
    Wallet save(Wallet wallet);
    Optional<Wallet> findById(String id);
}
