package com.lakindu.bangerandcobackend.util.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation will validate the user input for a list of valid strings.
 * This list will be held in string name "allowedConstants"
 * Each unique constraint will be separated by a "," eg: manual,petrol
 */
@Target({ElementType.FIELD, ElementType.METHOD}) //apply annotation on method and field
@Retention(RetentionPolicy.RUNTIME) //keep compiled version of the annotation during runtime.
@Constraint(validatedBy = ConstraintCheckerValidator.class) //the class containing validation logic.
public @interface ConstraintChecker {
    //used to group validation constraints
    Class<?>[] groups() default {};

    //used to give additional information about validation error.
    Class<? extends Payload>[] payload() default {};

    public String allowedConstants();

    public String message() default "Invalid Format"; //error message thrown when validation fails.
}
