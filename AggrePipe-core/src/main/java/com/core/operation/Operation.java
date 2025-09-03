package com.core.operation;

public enum Operation {


    SUM(0, 1 << 0),

    COUNT(1, 1 << 1),

    MAX(2, 1 << 2),

    MIN(3, 1 << 3);


    private final int index;

    private final int mask;


    Operation(int index, int mask) {
        this.index = index;
        this.mask = mask;
    }


    public int getIndex() {
        return index;
    }

    public int getMask() {
        return mask;
    }
}
