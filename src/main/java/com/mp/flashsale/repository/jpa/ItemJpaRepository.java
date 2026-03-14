package com.mp.flashsale.repository.jpa;

import com.mp.flashsale.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ItemJpaRepository extends JpaRepository<Item, String> {
    Optional<Item> findByIdAndDeletedAtIsNull(String id);

    Page<Item> findAllByDeletedAtIsNull(Pageable pageable);

    List<Item> findAllBySellerIdAndDeletedAtIsNull(String sellerId);
    void flush();

    Page<Item> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String name, Pageable pageable);

    @Query("SELECT i FROM Item i JOIN FlashSale fs ON i.id = fs.item.id " +
            "WHERE fs.startTime <= CURRENT_TIMESTAMP " +
            "AND fs.endTime >= CURRENT_TIMESTAMP " +
            "AND i.deletedAt IS NULL")
    List<Item> findItemsInActiveFlashSale();
}
