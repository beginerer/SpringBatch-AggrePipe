package com.core.annotaion;

import com.core.operation.Operation;

import java.lang.reflect.Type;

public class AggMeta {


    private String fieldName;

    private Operation op;

    private Type type;


    public AggMeta(String fieldName, Operation op, Type type) {
        this.fieldName = fieldName;
        this.op = op;
        this.type = type;
    }
}
