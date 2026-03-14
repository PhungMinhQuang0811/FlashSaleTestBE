package com.mp.flashsale.dto.request.item;

import com.mp.flashsale.validation.RequiredField;
import com.mp.flashsale.validation.ValidImage;
import com.mp.flashsale.validation.ValidItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;


@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Schema(name = "request.item.CreateItemRequest", description = "DTO containing necessary information to add a new item")
public class CreateItemRequest {
    @RequiredField(fieldName = "name")
    @Size(min = 5, max = 200, message = "INVALID_NAME_LENGTH")
    @Schema(example = "iPhone 15 Pro Max 256GB", description = "The name of the item")
    String name;

    @RequiredField(fieldName = "description")
    @Schema(example = "Brand new, VN/A version with titanium frame", description = "Detailed description of the item")
    String description;

    @RequiredField(fieldName = "original price")
    @Min(value = 0, message = "INVALID_VALUE_MIN")
    @Schema(example = "32000000", description = "The base price of the item before any discount")
    Long originalPrice;

    @RequiredField(fieldName = "quantity")
    @Min(value = 1, message = "INVALID_VALUE_MIN")
    @Schema(example = "100", description = "Total stock quantity in warehouse")
    Integer quantity;

    @RequiredField(fieldName = "item type")
    @ValidItemType(message = "INVALID_MODEL")
    @Schema(example = "ELECTRONICS", description = "Type/Category of the item")
    String itemType;

    @RequiredField(fieldName = "image")
    @ValidImage(message = "INVALID_IMAGE_FILE")
    @Schema(type = "string", format = "binary", description = "Main image of the product (.jpg, .png)")
    MultipartFile image;
}