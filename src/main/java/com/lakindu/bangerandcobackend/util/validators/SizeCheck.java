package com.lakindu.bangerandcobackend.util.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD}) //apply annotation on field and method
@Retention(RetentionPolicy.RUNTIME) //keep the compiled annotation for runtime
@Constraint(validatedBy = SizeCheckValidator.class) //validator class for annotation
public @interface SizeCheck {

    public String message() default "Vehicle Type does not match criteria set";

    //used to group validation constraints
    Class<?>[] groups() default {};

    //used to give additional information about validation error.
    Class<? extends Payload>[] payload() default {};
}
