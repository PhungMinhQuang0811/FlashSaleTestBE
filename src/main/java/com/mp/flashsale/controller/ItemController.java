package com.mp.flashsale.controller;

import com.mp.flashsale.dto.request.item.CreateItemRequest;
import com.mp.flashsale.dto.request.item.UpdateItemRequest;
import com.mp.flashsale.dto.response.ApiResponse;
import com.mp.flashsale.dto.response.item.ItemResponse;
import com.mp.flashsale.service.ItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PutMapping(value = "/seller/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ItemResponse> updateItem(
            @PathVariable String id,
            @ModelAttribute @Valid UpdateItemRequest request) {
        return ApiResponse.<ItemResponse>builder()
                .data(itemService.updateItem(id, request))
                .build();
    }

    @DeleteMapping("/seller/delete/{id}")
    public ApiResponse<Void> deleteItem(@PathVariable String id) {
        itemService.deleteItem(id);
        return ApiResponse.<Void>builder()
                .message("Item deleted successfully")
                .build();
    }
    @GetMapping("/{id}")
    public ApiResponse<ItemResponse> getDetails(@PathVariable String id) {
        return ApiResponse.<ItemResponse>builder()
                .data(itemService.getItemDetails(id))
                .build();
    }

    @GetMapping("/seller/my-items/{id}")
    public ApiResponse<ItemResponse> getMyItemDetails(@PathVariable String id) {
        return ApiResponse.<ItemResponse>builder()
                .data(itemService.getItemDetailsForSeller(id))
                .build();
    }
    @GetMapping
    public ApiResponse<Page<ItemResponse>> getAllItems(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<Page<ItemResponse>>builder()
                .data(itemService.getAllItems(pageable))
                .build();
    }

    @GetMapping("seller/my-items")
    public ApiResponse<List<ItemResponse>> getMyItems() {
        return ApiResponse.<List<ItemResponse>>builder()
                .data(itemService.getMyItems())
                .build();
    }
}
