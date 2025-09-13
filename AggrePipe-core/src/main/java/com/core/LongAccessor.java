package com.core;

@FunctionalInterface
public interface LongAccessor {

    long get(Object target);
}
