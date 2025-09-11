package com.core.annotaion;

import org.springframework.context.annotation.Import;
import java.lang.annotation.*;


@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(AggregationQuerySupport.class)
public @interface EnableAggQuery {

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

}
