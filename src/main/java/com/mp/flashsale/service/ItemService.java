package com.mp.flashsale.service;

import com.mp.flashsale.dto.request.item.CreateItemRequest;
import com.mp.flashsale.dto.request.item.UpdateItemRequest;
import com.mp.flashsale.dto.response.item.ItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemService {
    // Create
    ItemResponse createItem(CreateItemRequest request);

    // Read
    ItemResponse getItemById(String id);

    // Read - list
    Page<ItemResponse> getAllItems(Pageable pageable);

    // Read - my list
    List<ItemResponse> getMyItems();

    // Update
    ItemResponse updateItem(String id, UpdateItemRequest request);

    // Delete (Soft Delete)
    void deleteItem(String id);
}
