package com.mp.flashsale.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "flash_sales")
@Getter
@Setter
public class FlashSale {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private Long salePrice;
    @Column(nullable = false)
    private Integer discountPercent;

    @Column(nullable = false)
    private Integer saleQuantity;

    @Column(nullable = false)
    private Integer remainingQuantity;

    @Column(nullable = false)
    private Integer maxPerUser = 2;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Version
    private Integer version;
}
