package com.mp.flashsale.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
