package com.mp.flashsale.validation.validator;

import com.mp.flashsale.constant.EImage;
import com.mp.flashsale.validation.ValidImage;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class ImageValidator implements ConstraintValidator<ValidImage, MultipartFile> {

    /**
     * Validates whether the uploaded file has a valid image extension.
     *
     * @param file    The uploaded file to validate.
     * @param context The validation context.
     * @return true if the file is valid or empty (assuming required validation is handled separately), false otherwise.
     */
    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true;  // Skip validation if the file is null or empty (handled separately with @NotNull)
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            return false; // Invalid if the file has no name
        }

        // Extract the file extension
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        // Validate the file extension using the isValidExtension method
        return isValidExtension(fileExtension);
    }


    private boolean isValidExtension(String extension) {
        for (EImage fileType : EImage.values()) {
            if (fileType.getExtension().equalsIgnoreCase(extension)) {
                return true;  // Extension is valid
            }
        }
        return false;  // Extension is not valid
    }
}

