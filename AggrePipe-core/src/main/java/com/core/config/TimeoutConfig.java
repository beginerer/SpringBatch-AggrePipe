package com.core.config;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class TimeoutConfig {

    private final long amount;

    private final TimeUnit timeUnit;

    private static final int DEFAULT_TIME_VALUE = 500;

    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;


    public TimeoutConfig(long amount, TimeUnit timeUnit) {
        if(amount <= 0)
            throw new IllegalArgumentException("[ERROR] amount must be > 0");
        this.amount = amount;
        this.timeUnit = Objects.requireNonNull(timeUnit);
    }


    public static TimeoutConfig create() {
        return new TimeoutConfig(DEFAULT_TIME_VALUE, DEFAULT_TIME_UNIT);
    }


    public long getAmount() {
        return amount;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
