package com.lakindu.bangerandcobackend.util.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for checking quantities
 *
 * @author Lakindu Hewawasam
 */
public class CheckQuantityValidator implements ConstraintValidator<CheckQuantity, Integer> {

    private int constraintConditionPassed;

    /**
     * Initialize the validation data
     *
     * @param constraintAnnotation The Validation object that contains information of data passed.
     */
    @Override
    public void initialize(CheckQuantity constraintAnnotation) {
        //initialize the value to check for minimum when annotation is declared
        this.constraintConditionPassed = constraintAnnotation.minimumQuantity();
    }

    /**
     * Validation logic for the annotation
     *
     * @param theValue                   The integer value to be checked for.
     * @param constraintValidatorContext The validator context
     * @return The boolean to denote pass/fail validation
     */
    @Override
    public boolean isValid(Integer theValue, ConstraintValidatorContext constraintValidatorContext) {
        if (theValue >= constraintConditionPassed) {
            //if passed value is greater than or equal to minimum constraint, pass the validation
            return true;
        } else {
            //fail the validation.
            return false;
        }
    }
}
