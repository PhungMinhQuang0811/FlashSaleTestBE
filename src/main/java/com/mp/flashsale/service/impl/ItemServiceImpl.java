package com.mp.flashsale.service.impl;

import com.mp.flashsale.constant.EItemStatus;
import com.mp.flashsale.constant.EItemType;
import com.mp.flashsale.dto.request.item.CreateItemRequest;
import com.mp.flashsale.dto.request.item.UpdateItemRequest;
import com.mp.flashsale.dto.response.item.ItemResponse;
import com.mp.flashsale.entity.FlashSale;
import com.mp.flashsale.entity.Item;
import com.mp.flashsale.exception.AppException;
import com.mp.flashsale.exception.ErrorCode;
import com.mp.flashsale.mapper.ItemMapper;
import com.mp.flashsale.repository.FlashSaleRepository;
import com.mp.flashsale.repository.ItemRepository;
import com.mp.flashsale.service.FileService;
import com.mp.flashsale.service.ItemService;
import com.mp.flashsale.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ItemServiceImpl implements ItemService {
    ItemRepository itemRepository;
    FlashSaleRepository flashSaleRepository;
    FileService fileService;
    ItemMapper itemMapper;

    @Override
    public ItemResponse createItem(CreateItemRequest request) {
        Item item = itemMapper.toItem(request);
        //set id to save image
        item.setId(UUID.randomUUID().toString());

        item.setSeller(SecurityUtil.getCurrentAccount());
        item.setItemStatus(EItemStatus.AVAILABLE);
        //up image
        processUploadImage(request, item);
        item = itemRepository.save(item);
        itemRepository.flush();
        return toItemResponse(item);
    }

    @Override
    public ItemResponse getItemDetailsForSeller(String id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND_IN_DB));

        String currentAccountId = SecurityUtil.getCurrentAccountId();
        if (!item.getSeller().getId().equals(currentAccountId)) {
            throw new AppException(ErrorCode.FORBIDDEN_ITEM_ACCESS);
        }

        return toItemResponse(item);
    }
    @Override
    public ItemResponse getItemDetails(String id) {
        Item item = itemRepository.findById(id)
                .filter(i -> i.getItemStatus() == EItemStatus.AVAILABLE)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND_IN_DB));

        return toItemResponse(item);
    }

    @Override
    public Page<ItemResponse> getAllItems(Pageable pageable) {
        log.info("Fetching all items with pagination");

        return itemRepository.findAllByStatus(EItemStatus.AVAILABLE, pageable)
                .map(this::toItemResponse);
    }

    @Override
    public List<ItemResponse> getMyItems() {
        String currentAccountId = SecurityUtil.getCurrentAccountId();
        log.info("Seller {} is fetching their own items", currentAccountId);

        return itemRepository.findBySellerId(currentAccountId)
                .stream()
                .map(this::toItemResponse)
                .toList();
    }

    @Override
    public ItemResponse updateItem(String id, UpdateItemRequest request) {
        // 1. Tìm item cũ
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND_IN_DB));

        String currentAccountId = SecurityUtil.getCurrentAccountId();
        if (!item.getSeller().getId().equals(currentAccountId)) {
            throw new AppException(ErrorCode.FORBIDDEN_ITEM_ACCESS);
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            item.setName(request.getName());
        }

        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            item.setDescription(request.getDescription());
        }

        if (request.getOriginalPrice() != null) {
            item.setOriginalPrice(request.getOriginalPrice());
        }

        if (request.getQuantity() != null) {
            item.setQuantity(request.getQuantity());
        }

        if (request.getItemType() != null && !request.getItemType().trim().isEmpty()) {
            try {
                EItemType type = EItemType.valueOf(request.getItemType().toUpperCase());
                item.setItemType(type);
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_ITEM_TYPE);
            }
        }

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            processUploadImage(request, item);
        }

        // 4. Lưu và map sang Response DTO
        Item updatedItem = itemRepository.save(item);
        return toItemResponse(updatedItem);
    }

    @Override
    public void deleteItem(String id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND_IN_DB));

        String currentAccountId = SecurityUtil.getCurrentAccountId();
        if (!item.getSeller().getId().equals(currentAccountId)) {
            throw new AppException(ErrorCode.FORBIDDEN_ITEM_ACCESS);
        }

        // Soft delete
        item.setItemStatus(EItemStatus.DISCONTINUED);
        itemRepository.softDelete(id);
    }
    private void processUploadImage(Object request, Item item) {
        MultipartFile imageFile = null;

        if (request instanceof CreateItemRequest createRequest) {
            imageFile = createRequest.getImage();
        } else if (request instanceof UpdateItemRequest updateRequest) {
            imageFile = updateRequest.getImage();
        }

        if (imageFile != null && !imageFile.isEmpty()) {

            String fileName = String.format("item-%s-%d", item.getId(), System.currentTimeMillis());

            if (fileService.uploadFile(imageFile, fileName)) {
                item.setImagePublicId(fileName);

            } else {
                throw new AppException(ErrorCode.UPLOAD_FILE_FAIL);
            }
        }
    }
    private ItemResponse toItemResponse(Item item) {
        ItemResponse response = itemMapper.toItemResponse(item);

        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());

        if (item.getImagePublicId() != null && !item.getImagePublicId().isEmpty()) {
            // Gọi sang fileService để sinh ra link secure (https)
            String fullUrl = fileService.getFileUrl(item.getImagePublicId());
            response.setImageUrl(fullUrl);
        }
        flashSaleRepository.findActiveFlashSale(item.getId())
                .ifPresentOrElse(
                        fs -> response.setSalePrice(fs.getSalePrice()),
                        () -> response.setSalePrice(null)
                );

        return response;
    }
}
