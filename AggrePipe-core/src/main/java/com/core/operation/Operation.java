package com.core.operation;

public enum Operation {


    SUM("SUM:"),

    MAX("MAX:"),

    MIN("MIN:");


    private String prefix;

    Operation(String prefix) {
        this.prefix = prefix;
    }


    public static  String resolveFieldName(Operation op, String fieldName) {
        return op.prefix + fieldName;
    }

    public static  String resolveCountFieldName() {
        return "_meta:count";
    }

}
