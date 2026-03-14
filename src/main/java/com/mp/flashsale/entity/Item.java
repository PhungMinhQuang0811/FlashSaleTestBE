package com.mp.flashsale.entity;

import com.mp.flashsale.constant.EItemStatus;
import com.mp.flashsale.constant.EItemType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "original_price", nullable = false)
    private Long originalPrice;

    @Column(name = "image_public_id", nullable = false)
    private String imagePublicId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "item_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EItemStatus itemStatus;

    @Column(name = "item_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EItemType itemType;

    @Version // chống bán quá số lượng (Race Condition)
    @Column(name = "version_id", nullable = false)
    private Integer version;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Account seller;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
