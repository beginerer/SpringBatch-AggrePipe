package com.core.annotaion;

import com.core.operation.Operation;
import com.core.operation.ValueType;

public class ItemUnit {


    private String fieldName;

    private Operation op;

    private ValueType valueType;

    private Long lv;

    private Double dv;


    public ItemUnit(String fieldName, Operation op, ValueType valueType, Long lv, Double dv) {
        this.fieldName = fieldName;
        this.op = op;
        this.valueType = valueType;
        this.lv = lv;
        this.dv = dv;
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

    public Long getLv() {
        return lv;
    }

    public Double getDv() {
        return dv;
    }

    @Override
    public String toString() {
        return "ItemUnit{" +
                "fieldName='" + fieldName + '\'' +
                ", op=" + op +
                ", valueType=" + valueType +
                ", lv=" + lv +
                ", dv=" + dv +
                '}';
    }



}
