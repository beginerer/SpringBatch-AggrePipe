package com.core.result;


import com.core.operation.Operation;

public final class RedisLongResultSet implements RedisResultSet {


    private final int mask;

    private final Long sum;

    private final Long count;

    private final Long max;

    private final Long min;



    public RedisLongResultSet(Long sum, Long count, Long max, Long min) {
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


    public boolean has(Operation op) {
        return (mask & op.getMask()) != 0;
    }

    public Long getSum() {
        if(!has(Operation.SUM))
            throw new IllegalStateException("[ERROR] operation not present: " + Operation.SUM);
        return sum;
    }

    public Long getCount() {
        if(!has(Operation.COUNT))
            throw new IllegalStateException("[ERROR] operation not present: " + Operation.COUNT);
        return count;
    }

    public Long getMax() {
        if(!has(Operation.MAX))
            throw new IllegalStateException("[ERROR] operation not present: " + Operation.MAX);
        return max;
    }

    public Long getMin() {
        if(!has(Operation.MIN))
            throw new IllegalStateException("[ERROR] operation not present: " + Operation.MIN);
        return min;
    }


    private int resolveMaskValue(Long sum, Long count, Long max, Long min) {
        int mask = 0;

        if(sum != null) mask |= Operation.SUM.getMask();
        if(count != null) mask |= Operation.COUNT.getMask();
        if(max != null) mask |= Operation.MAX.getMask();
        if(min != null) mask |= Operation.MIN.getMask();

        return mask;
    }


}
