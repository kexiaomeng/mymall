package com.tracy.mymall.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * 校验类
 */
public class MyConstraintValidator implements ConstraintValidator<MyListStrict, Integer> {
    private Set<Integer> sets = new HashSet<>();

    @Override
    public void initialize(MyListStrict constraintAnnotation) {
        int[] value = constraintAnnotation.value();
        for (int i : value) {
            sets.add(i);
        }
    }
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
       return sets.contains(value);
    }

}
