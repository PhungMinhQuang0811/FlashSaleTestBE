package com.mp.flashsale.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    @Id
    @Column(name = "account_id")
    private String id;

    @Column(name = "balance", nullable = false)
    private Long balance = 0L;

    @OneToOne
    @MapsId
    @JoinColumn(name = "account_id")
    private Account account;
}
