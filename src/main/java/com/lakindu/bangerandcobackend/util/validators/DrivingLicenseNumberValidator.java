package com.lakindu.bangerandcobackend.util.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DrivingLicenseNumberValidator implements ConstraintValidator<DrivingLicenseNumberChecker, String> {
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            //number not passed, admin is updating.
            return true;
        } else {
            if (s.matches("^[A-Z]{1}[0-9]{7}$")) {
                //license number in valid format
                return true;
            } else {
                return false;
            }
        }
    }
}
