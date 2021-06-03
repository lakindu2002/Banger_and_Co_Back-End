package com.lakindu.bangerandcobackend.util.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;

public class SizeCheckValidator implements ConstraintValidator<SizeCheck, String> {

    private ArrayList<String> allowedSizes;

    @Override
    public void initialize(SizeCheck constraintAnnotation) {
        allowedSizes = new ArrayList<>();
        allowedSizes.add("small");
        allowedSizes.add("medium");
        allowedSizes.add("large");
    }

    @Override
    public boolean isValid(String userProvidedInput, ConstraintValidatorContext constraintValidatorContext) {
        if (userProvidedInput == null) {
            return false;
        }
        for (String allowedMatcher : allowedSizes) {
            if (userProvidedInput.equalsIgnoreCase(allowedMatcher)) {
                //if type is allowed
                return true;
            }
        }
        return false;
    }
}
