package com.mp.flashsale.service.impl;

import com.mp.flashsale.constant.EItemStatus;
import com.mp.flashsale.dto.request.item.CreateItemRequest;
import com.mp.flashsale.dto.request.item.UpdateItemRequest;
import com.mp.flashsale.dto.response.item.ItemResponse;
import com.mp.flashsale.entity.Item;
import com.mp.flashsale.exception.AppException;
import com.mp.flashsale.exception.ErrorCode;
import com.mp.flashsale.mapper.ItemMapper;
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

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ItemServiceImpl implements ItemService {
    ItemRepository itemRepository;
    FileService fileService;
    ItemMapper itemMapper;

    @Override
    public ItemResponse createItem(CreateItemRequest request) {
        Item item = itemMapper.toItem(request);
        //set id to save image
        item.setId(UUID.randomUUID().toString());
        //set version when create
//        item.setVersion(0);
        item.setSeller(SecurityUtil.getCurrentAccount());
        item.setItemStatus(EItemStatus.AVAILABLE);
        //up image
        processUploadImage(request, item);
        item = itemRepository.save(item);
        return toItemResponse(item);
    }

    @Override
    public ItemResponse getItemById(String id) {
        return null;
    }

    @Override
    public Page<ItemResponse> getAllItems(Pageable pageable) {
        return null;
    }

    @Override
    public List<ItemResponse> getMyItems() {
        return List.of();
    }

    @Override
    public ItemResponse updateItem(String id, UpdateItemRequest request) {
        return null;
    }

    @Override
    public void deleteItem(String id) {

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

        if (item.getImagePublicId() != null && !item.getImagePublicId().isEmpty()) {
            // Gọi sang fileService để sinh ra link secure (https)
            String fullUrl = fileService.getFileUrl(item.getImagePublicId());
            response.setImageUrl(fullUrl);
        }

        return response;
    }
}
