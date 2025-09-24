package com.core.operation;

public enum Operation {


    SUM("SUM:", 1 << 0),

    MAX("MAX:", 1 << 1),

    MIN("MIN:", 1 << 2),

    COUNT_ONLY_FOR_READ("_meta:count", 1 << 3);


    private String prefix;

    private int mask;


    Operation(String prefix, int mask) {
        this.prefix = prefix;
        this.mask = mask;
    }



    public static  String resolveFieldName(Operation op, String fieldName) {
        if(op == Operation.COUNT_ONLY_FOR_READ)
            return op.prefix;

        return op.prefix + fieldName;
    }

    public static String resolveCountFieldName() {
        return Operation.COUNT_ONLY_FOR_READ.getPrefix();
    }


    public static int resolveMasking(Operation[] operations) {
        int mask = 0;

        for (Operation op : operations) {
            mask |= op.getMask();
        }
        return mask;
    }

    public static boolean hasOperation(int mask, Operation operation) {
        return (mask & operation.getMask()) != 0;
    }


    public String getPrefix() {
        return prefix;
    }

    public int getMask() {
        return mask;
    }
}
