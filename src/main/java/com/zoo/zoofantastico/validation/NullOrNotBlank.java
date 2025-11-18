package com.zoo.zoofantastico.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NullOrNotBlankValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface NullOrNotBlank {
    String message() default "must be null or not blank";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
