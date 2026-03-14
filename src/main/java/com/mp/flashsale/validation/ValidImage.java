package com.mp.flashsale.validation;

import com.mp.flashsale.validation.validator.ImageValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ImageValidator.class) // Specifies the validator class
@Target({ElementType.FIELD}) // Can be applied to fields only
@Retention(RetentionPolicy.RUNTIME) // Retained at runtime for reflection-based validation
public @interface ValidImage {

    /**
     * Default error message when the uploaded file type is invalid.
     *
     * @return The error message string.
     */
    String message() default "Invalid file type. Accepted formats are .jpg, .jpeg, .png, .gif";

    /**
     * Defines validation groups for applying different validation constraints.
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
