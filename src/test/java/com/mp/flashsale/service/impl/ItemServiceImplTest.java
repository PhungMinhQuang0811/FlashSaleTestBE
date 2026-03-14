package com.mp.flashsale.service.impl;

import com.mp.flashsale.dto.request.item.CreateItemRequest;
import com.mp.flashsale.dto.request.item.UpdateItemRequest;
import com.mp.flashsale.dto.response.item.ItemResponse;
import com.mp.flashsale.entity.Account;
import com.mp.flashsale.entity.Item;
import com.mp.flashsale.mapper.ItemMapper;
import com.mp.flashsale.repository.ItemRepository;
import com.mp.flashsale.security.entity.UserDetailsImpl;
import com.mp.flashsale.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {
    @Mock
    ItemRepository itemRepository;
    @Mock
    FileService fileService;
    @Mock
    ItemMapper itemMapper;

    @InjectMocks
    ItemServiceImpl itemService;

    Item item;
    CreateItemRequest createRequest;
    ItemResponse itemResponse;

    @BeforeEach
    void initData() {
        item = new Item();
        item.setId("id-123");
        item.setName("iPhone 15");
        item.setOriginalPrice(30000000L);
        item.setImagePublicId("img-id");

        createRequest = CreateItemRequest.builder()
                .name("iPhone 15")
                .originalPrice(30000000L)
                .quantity(10)
                .itemType("ELECTRONICS")
                .image(new MockMultipartFile("image", "test.jpg", "image/jpeg", "content".getBytes()))
                .build();

        itemResponse = ItemResponse.builder()
                .id("id-123")
                .name("iPhone 15")
                .imageUrl("http://cloudinary.com/img-id")
                .build();
    }
    @Test
    @DisplayName("Create Item - Success")
    void createItem_validRequest_success() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        UserDetailsImpl mockUserDetails = mock(UserDetailsImpl.class);

        Account mockAccount = new Account();
        mockAccount.setId("seller-id-123");
        mockAccount.setEmail("seller@fpt.edu.vn");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUserDetails);

        SecurityContextHolder.setContext(securityContext);

        when(itemMapper.toItem(any())).thenReturn(item);
        when(fileService.uploadFile(any(), anyString())).thenReturn(true);
        when(itemRepository.save(any())).thenReturn(item);
        when(itemMapper.toItemResponse(any())).thenReturn(itemResponse);

        var response = itemService.createItem(createRequest);

        assertNotNull(response);
        verify(itemRepository).save(any());

        SecurityContextHolder.clearContext();
    }
    @Test
    @DisplayName("Update Item - Should not update when fields are blank")
    void updateItem_blankFields_shouldKeepOldValues() {
        // GIVEN
        String itemId = "id-123";
        UpdateItemRequest updateRequest = new UpdateItemRequest();
        updateRequest.setName(""); // Gửi chuỗi rỗng

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(item);
        when(itemMapper.toItemResponse(any())).thenReturn(itemResponse);

        // WHEN
        itemService.updateItem(itemId, updateRequest);

        // THEN
        // Kiểm tra xem name có bị đổi thành rỗng không (Mong đợi vẫn là "iPhone 15")
        assertEquals("iPhone 15", item.getName());
        verify(itemRepository).save(argThat(savedItem -> savedItem.getName().equals("iPhone 15")));
    }
    @Test
    @DisplayName("Delete Item - Success (Soft Delete)")
    void deleteItem_success() {
        // GIVEN
        String itemId = "id-123";
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // WHEN
        itemService.deleteItem(itemId);

        // THEN
        assertNotNull(item.getDeletedAt()); // Kiểm tra xem đã set ngày xóa chưa
        verify(itemRepository).save(item);
    }
}
