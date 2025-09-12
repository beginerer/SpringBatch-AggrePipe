package com.core.annotaion;

@FunctionalInterface
public interface LongAccessor {

    long get(Object target);
}
