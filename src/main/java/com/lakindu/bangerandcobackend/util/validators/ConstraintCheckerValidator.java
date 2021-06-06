package com.lakindu.bangerandcobackend.util.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ConstraintCheckerValidator implements ConstraintValidator<ConstraintChecker, String> {

    public String[] allowedConstraints; //the allowed validation values

    @Override
    public void initialize(ConstraintChecker constraintAnnotation) {
        allowedConstraints = constraintAnnotation.allowedConstants().split(","); //when declaring in Annotation Definition
        //separate each constraint by a ,
    }

    @Override
    public boolean isValid(String passedConstraint, ConstraintValidatorContext constraintValidatorContext) {
        if (passedConstraint != null) {
            //if user passes null, return false
            for (String eachConstraint : allowedConstraints) {
                if (eachConstraint.equalsIgnoreCase(passedConstraint)) {
                    //if the string is acceptable return true.
                    return true;
                }
            }
        }
        return false;
    }
}
