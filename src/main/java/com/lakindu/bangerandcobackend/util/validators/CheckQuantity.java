package com.lakindu.bangerandcobackend.util.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation defined to validate quantites passed.
 *
 * @author Lakindu Hewawasam
 */
@Target({ElementType.FIELD, ElementType.METHOD}) //apply annotation on field and method
@Retention(RetentionPolicy.RUNTIME) //retain compiled annotation for runtime
@Constraint(validatedBy = CheckQuantityValidator.class)
public @interface CheckQuantity {
    public int minimumQuantity() default 1; //set the minimum quantity required as 1

    //assign default message
    public String message() default "Please keep quantity greater than 0";

    //used to group validation constraints
    Class<?>[] groups() default {};

    //additional information about validation error
    Class<? extends Payload>[] payload() default {};
}
