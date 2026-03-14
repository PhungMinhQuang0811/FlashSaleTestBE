package com.mp.flashsale.dto.request.item;

import com.mp.flashsale.validation.ValidImage;
import com.mp.flashsale.validation.ValidItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

/**
 * Represents the request payload for updating an existing item.
 * Fields are optional to support partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Schema(name = "request.item.UpdateItemRequest", description = "DTO for updating item information")
public class UpdateItemRequest {

    @Size(min = 5, max = 200, message = "INVALID_NAME_LENGTH")
    @Schema(example = "iPhone 15 Pro Max (Updated)", description = "New name of the item")
    String name;

    @Schema(example = "Updated description for the item", description = "New description")
    String description;

    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @Schema(example = "31000000", description = "New original price")
    Long originalPrice;

    @Min(value = 1, message = "QUANTITY_MIN_1")
    @Schema(example = "150", description = "Update stock quantity")
    Integer quantity;

    @ValidItemType(message = "INVALID_ITEM_TYPE")
    @Schema(example = "ELECTRONICS", description = "New category/type of the item")
    String itemType;

    @ValidImage(message = "INVALID_IMAGE_FILE")
    @Schema(type = "string", format = "binary", description = "New image file if the seller wants to change it")
    MultipartFile image;
}
