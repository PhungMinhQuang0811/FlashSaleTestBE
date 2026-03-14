package com.mp.flashsale.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mp.flashsale.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CloudinaryFileServiceImpl implements FileService {
    Cloudinary cloudinary;

    @NonFinal
    @Value("${cloud.cloudinary.folder-name}")
    String folderName;

    @Override
    public boolean uploadFile(MultipartFile file, String key) {
        if (file == null || file.isEmpty() || key == null || key.isEmpty()) {
            return false;
        }

        // Cloudinary tự thêm extension, nên nếu key có .jpg thì cắt đi để tránh trùng
        if (key.contains(".")) {
            key = key.substring(0, key.lastIndexOf('.'));
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", key,
                            "folder", folderName,
                            "overwrite", true,
                            "resource_type", "auto"
                    )
            );

            log.info("Upload file {} success: {}", key, uploadResult.get("secure_url"));
            return true;
        } catch (IOException e) {
            log.error("Upload file failed: {}", key, e);
            return false;
        }
    }

    @Override
    public String getFileUrl(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            return null;
        }

        return cloudinary.url()
                .secure(true)
                .resourceType("image")
                .generate(folderName + "/" + publicId);
    }

    @Override
    public String getFileExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        }
        return "";
    }
}
