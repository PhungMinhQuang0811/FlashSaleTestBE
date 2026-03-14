package com.mp.flashsale.validation;

import com.mp.flashsale.validation.validator.ItemTypeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Constraint(validatedBy = ItemTypeValidator.class) // Specifies the validator class responsible for validation logic
@Target({ElementType.FIELD}) // Applicable only to fields
@Retention(RetentionPolicy.RUNTIME) // Available at runtime for reflection-based validation
public @interface ValidItemType {


    String message() default "{Your item type were not predefined}";

    /**
     * Defines validation groups for grouping different validation constraints.
     *
     * @return The groups for validation.
     */
    Class<?>[] groups() default {};

    /**
     * Additional metadata for validation payload.
     *
     * @return The payload classes.
     */
    Class<? extends Payload>[] payload() default {};
}
