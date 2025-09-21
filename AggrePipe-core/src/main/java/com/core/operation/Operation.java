package com.core.operation;

public enum Operation {


    SUM("SUM:"),

    MAX("MAX:"),

    MIN("MIN:"),

    COUNT_ONLY_FOR_READ("_meta:count");


    private String prefix;

    Operation(String prefix) {
        this.prefix = prefix;
    }


    public static  String resolveFieldName(Operation op, String fieldName) {
        if(op == Operation.COUNT_ONLY_FOR_READ)
            return op.prefix;

        return op.prefix + fieldName;
    }

    public static  String resolveCountFieldName() {
        return "_meta:count";
    }

}
