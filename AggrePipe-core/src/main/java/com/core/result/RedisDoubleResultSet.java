package com.core.result;

import com.core.operation.Operation;

public final class RedisDoubleResultSet implements RedisResultSet {


    private final int mask;

    private final Double sum;

    private final Long count;

    private final Double max;

    private final Double min;



    public RedisDoubleResultSet(Double sum, Long count, Double max, Double min) {
        this.mask = resolveMaskValue(sum, count, max, min);
        this.sum = sum;
        this.count = count;
        this.max = max;
        this.min = min;
    }


    @Override
    public int mask() {
        return mask;
    }

    @Override
    public boolean has(Operation op) {
        return (mask & op.getMask()) != 0;
    }

    public Double getSum() {
        if(has(Operation.SUM))
            throw new IllegalStateException("[ERROR] operation not present : " + Operation.SUM);
        return sum;
    }

    public Long getCount() {
        if(has(Operation.COUNT))
            throw new IllegalStateException("[ERROR] operation not present : " + Operation.COUNT);
        return count;
    }

    public Double getMax() {
        if(has(Operation.MAX))
            throw new IllegalStateException("[ERROR] operation not present : " + Operation.MAX);
        return max;
    }

    public Double getMin() {
        if(has(Operation.MIN))
            throw new IllegalStateException("[ERROR] operation not present : " + Operation.MIN);
        return min;
    }

    private int resolveMaskValue(Double sum, Long count, Double max, Double min) {
        int mask = 0;

        if(sum != null) mask |= Operation.SUM.getMask();
        if(count != null) mask |= Operation.COUNT.getMask();
        if(max != null) mask |= Operation.MAX.getMask();
        if(min != null) mask |= Operation.MIN.getMask();

        return mask;
    }
}
