package com.mp.flashsale.controller;

import com.mp.flashsale.dto.request.item.CreateItemRequest;
import com.mp.flashsale.dto.response.ApiResponse;
import com.mp.flashsale.dto.response.item.ItemResponse;
import com.mp.flashsale.service.ItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
@Tag(name = "Item", description = "API for managing item")
public class ItemController {
    ItemService itemService;

    @PostMapping(value = "/seller/create-item", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ItemResponse> createItem(@ModelAttribute @Valid CreateItemRequest request) {
        log.info("add new item {}", request);
        return ApiResponse.<ItemResponse>builder()
                .data(itemService.createItem(request))
                .build();
    }
}
