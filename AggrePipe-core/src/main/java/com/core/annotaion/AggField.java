package com.core.annotaion;


import com.core.operation.Operation;
import com.core.operation.ValueType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AggField {


    String fieldName();

    Operation op();

    ValueType type();
}
