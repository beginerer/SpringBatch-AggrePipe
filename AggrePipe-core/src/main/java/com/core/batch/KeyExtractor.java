package com.core.batch;

public interface KeyExtractor<T, K> {

    K extract(T t);
}
