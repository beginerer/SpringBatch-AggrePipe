package com.core.annotaion;


import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;




@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(AggregationQuerySupport.class)
public @interface EnableAggQuery {

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

}
