package com.mp.flashsale.repository.impl;

import com.mp.flashsale.entity.Wallet;
import com.mp.flashsale.repository.WalletRepository;
import com.mp.flashsale.repository.jpa.WalletJpaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletRepositoryImpl implements WalletRepository {
    WalletJpaRepository walletJpaRepository;
    @Override
    public Wallet save(Wallet wallet) {
        return walletJpaRepository.save(wallet);
    }

    @Override
    public Optional<Wallet> findById(String id) {
        return walletJpaRepository.findById(id);
    }
}
