package com.core.example.batch;

public interface KeyExtractor<T, K> {

    K extract(T t);
}
