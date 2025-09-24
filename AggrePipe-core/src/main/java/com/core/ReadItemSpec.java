package com.core;

import com.core.operation.Operation;
import com.core.operation.ValueType;

public class ReadItemSpec {

    private String targetFieldName;

    private String fieldName;

    private Operation op;

    private ValueType valueType;


    public ReadItemSpec(String targetFieldName, String fieldName, Operation op, ValueType valueType) {
        this.targetFieldName = targetFieldName;
        this.fieldName = fieldName;
        this.op = op;
        this.valueType = valueType;
    }



    public String getTargetFieldName() {
        return targetFieldName;
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
