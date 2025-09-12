package com.core.annotaion;


import com.core.operation.Operation;
import com.core.operation.ValueType;
import java.lang.annotation.*;



@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface AggField {


    Operation op();

    ValueType type();

}
