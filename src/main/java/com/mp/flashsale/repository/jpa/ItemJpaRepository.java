package com.mp.flashsale.repository.jpa;

import com.mp.flashsale.constant.EItemStatus;
import com.mp.flashsale.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ItemJpaRepository extends JpaRepository<Item, String> {
    Optional<Item> findByIdAndDeletedAtIsNull(String id);

    Page<Item> findAllByDeletedAtIsNull(Pageable pageable);

    void flush();

    @Query("SELECT i FROM Item i WHERE i.id = :id AND i.deletedAt IS NULL")
    Optional<Item> findActiveById(String id);

    @Query("SELECT i FROM Item i WHERE i.itemStatus = :status " +
            "AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<Item> findAvailableItems(EItemStatus status, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.seller.id = :sellerId " +
            "AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    List<Item> findAllBySeller(String sellerId);

    @Modifying
    @Query("UPDATE Item i SET i.deletedAt = :now, i.itemStatus = :status WHERE i.id = :id")
    void softDelete(String id, LocalDateTime now, EItemStatus status);

    @Modifying
    @Query("UPDATE Item i SET i.quantity = i.quantity - :qty, i.version = i.version + 1 " +
            "WHERE i.id = :id AND i.quantity >= :qty AND i.version = :currentVersion")
    int decreaseStock(String id, Integer qty, Integer currentVersion);

}
