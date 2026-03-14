package com.mp.flashsale.validation;

import com.mp.flashsale.validation.validator.UniqueEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {

    /**
     * The default error message when validation fails.
     *
     * @return the error message
     */
    String message() default "{The email must be unique}";

    /**
     * Allows the specification of validation groups, to which this constraint belongs.
     *
     * @return an array of groups the constraint belongs to
     */
    Class<?>[] groups() default {};

    /**
     * Can be used by clients of the Bean Validation API to assign custom payload objects to a constraint.
     *
     * @return an array of payload classes associated with the constraint
     */
    Class<? extends Payload>[] payload() default {};

}
