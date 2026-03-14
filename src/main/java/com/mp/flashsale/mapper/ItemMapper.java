package com.mp.flashsale.mapper;

import com.mp.flashsale.dto.request.item.CreateItemRequest;
import com.mp.flashsale.dto.response.item.ItemResponse;
import com.mp.flashsale.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    // 1. Request -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "imagePublicId", ignore = true)
    @Mapping(target = "itemStatus", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "itemType", source = "itemType")
    Item toItem(CreateItemRequest request);

    // 2. Entity -> Response
    @Mapping(target = "sellerEmail", source = "seller.email")
    @Mapping(target = "imageUrl", ignore = true)
    ItemResponse toItemResponse(Item item);
}
