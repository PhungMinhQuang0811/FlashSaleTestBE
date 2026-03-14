package com.mp.flashsale.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    boolean uploadFile(MultipartFile file, String key);

    String getFileUrl(String publicId);

    String getFileExtension(MultipartFile file);
}
