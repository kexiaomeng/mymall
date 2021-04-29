package com.tracy.mymall.common.valid;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 自定义校验注解
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(MyListStrict.List.class)
@Documented
//校验注解是使用哪个校验器校验的，这里可以指定校验器，这里不指定时候需要在初始化的时候指定
@Constraint(validatedBy = {MyConstraintValidator.class }) // 校验类
public @interface MyListStrict {
    //校验出错后，错误信息去哪取，默认这个属性，取ValidationMessages.properties取
    String message() default "{com.tracy.mymall.myliststrict.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    int []value() default {};
    /**
     * Defines several {@code @MyListStrict} constraints on the same element.
     *
     * @see MyListStrict
     */
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        MyListStrict[] value();
    }
}
