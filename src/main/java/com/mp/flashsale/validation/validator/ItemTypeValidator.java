package com.mp.flashsale.validation.validator;

import com.mp.flashsale.constant.EItemType;
import com.mp.flashsale.validation.ValidItemType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class ItemTypeValidator implements ConstraintValidator<ValidItemType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        return Arrays.stream(EItemType.values())
                .anyMatch(enumValue -> enumValue.name().equals(value.toUpperCase()));
    }
}