package com.core.annotaion;

import com.core.operation.Operation;
import com.core.operation.ValueType;

public class Item {


    private String name;

    private Operation op;

    private ValueType valueType;



    public Item(String name, Operation op, ValueType valueType) {
        this.name = name;
        this.op = op;
        this.valueType = valueType;
    }

}
