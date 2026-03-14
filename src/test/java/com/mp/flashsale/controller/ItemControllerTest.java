package com.mp.flashsale.controller;

import com.mp.flashsale.dto.request.item.CreateItemRequest;
import com.mp.flashsale.dto.response.item.ItemResponse;
import com.mp.flashsale.security.entity.UserDetailsImpl;
import com.mp.flashsale.service.ItemService;
import com.mp.flashsale.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class) // Quan trọng: Để Mockito tự khởi tạo Mock
class ItemControllerTest {

    @Mock // Tạo ra bản giả của Service
    private ItemService itemService;

    @InjectMocks // Tiêm cái Mock ở trên vào Controller
    private ItemController itemController;

    private ItemResponse itemResponse;

    @BeforeEach
    void init() {
        itemResponse = ItemResponse.builder()
                .id("id-123")
                .name("iPhone 15")
                .build();
    }

    @Test
    void createItem_shouldCallService() {
        // Given
        CreateItemRequest request = new CreateItemRequest();
        request.setName("iPhone 15 Pro");

        // Giả lập Service trả về response
        when(itemService.createItem(any())).thenReturn(itemResponse);

        // When
        // Gọi trực tiếp method từ instance itemController (đã được InjectMocks)
        var result = itemController.createItem(request);

        // Then
        assertNotNull(result);
        verify(itemService, times(1)).createItem(any());
    }
}
