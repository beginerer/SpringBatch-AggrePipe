package com.core;

import com.core.operation.Operation;
import com.core.operation.ValueType;

public class ReadItemSpec {

    private String originalFieldName;

    private String fieldName;

    private Operation op;

    private ValueType valueType;



    public ReadItemSpec(String originalFieldName, String fieldName, Operation op, ValueType valueType) {
        this.originalFieldName = originalFieldName;
        this.fieldName = fieldName;
        this.op = op;
        this.valueType = valueType;
    }


    public String getOriginalFieldName() {
        return originalFieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Operation getOp() {
        return op;
    }

    public ValueType getValueType() {
        return valueType;
    }
}
