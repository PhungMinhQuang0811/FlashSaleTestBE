package com.mp.flashsale.dto.response.item;

import com.mp.flashsale.constant.EItemStatus;
import com.mp.flashsale.constant.EItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Schema(name = "response.item.ItemResponse", description = "DTO containing item details for display")
public class ItemResponse {

    @Schema(example = "uuid-123-456", description = "Unique identifier of the item")
    String id;

    @Schema(example = "iPhone 15 Pro Max", description = "Name of the item")
    String name;

    @Schema(example = "Titanium finish, 256GB...", description = "Detailed description")
    String description;

    @Schema(example = "32000000", description = "Original price in VND")
    Long originalPrice;

    @Schema(example = "https://res.cloudinary.com/.../iphone15.jpg", description = "Full URL to the product image")
    String imageUrl;

    @Schema(example = "100", description = "Available stock quantity")
    Integer quantity;

    @Schema(example = "ELECTRONICS", description = "Category of the item")
    EItemType itemType;

    @Schema(example = "ACTIVE", description = "Current status of the item")
    EItemStatus itemStatus;

    @Schema(example = "seller@fpt.edu.vn", description = "Email of the seller who owns this item")
    String sellerEmail;

    @Schema(description = "Timestamp when the item was created")
    LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update")
    LocalDateTime updatedAt;
}