package com.core.batch;

public interface KeyExtractor<Key, K> {

    K extract(Key t);
}
