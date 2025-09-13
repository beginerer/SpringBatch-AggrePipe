package com.core.annotaion;

import com.core.operation.ValueType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({})
public @interface GroupByKey {


    String field();

    ValueType type();

}
