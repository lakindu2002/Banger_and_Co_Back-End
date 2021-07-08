package com.lakindu.bangerandcobackend.util.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD}) //apply annotation on method and field
@Retention(RetentionPolicy.RUNTIME) //keep compiled version of the annotation during runtime.
@Constraint(validatedBy = DrivingLicenseNumberValidator.class) //the class containing validation logic.
public @interface DrivingLicenseNumberChecker {
    //used to group validation constraints
    Class<?>[] groups() default {};

    //used to give additional information about validation error.
    Class<? extends Payload>[] payload() default {};

    public String message() default "Driving License Should Be Of Format - X0000000"; //error message thrown when validation fails.
}
