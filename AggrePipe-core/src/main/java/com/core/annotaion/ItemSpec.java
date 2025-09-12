package com.core.annotaion;

import com.core.operation.Operation;
import com.core.operation.ValueType;

public class ItemSpec {


    private String fieldName;

    private Operation op;

    private ValueType valueType;



    public ItemSpec(String fieldName, Operation op, ValueType valueType) {
        this.fieldName = fieldName;
        this.op = op;
        this.valueType = valueType;
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
