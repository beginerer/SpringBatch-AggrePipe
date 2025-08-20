package com.core;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.support.BoundedPoolConfig;



public class RedisClientConfig {

    private RedisClient redisClient;

    private final RedisURI redisURI;

    private final StringCodec stringCodec;

    private final BoundedPoolConfig boundedPoolConfig;

    public static final int DEFAULT_MAX_TOTAL = 8;

    public static final int DEFAULT_MAX_IDLE = 8;

    public static final int DEFAULT_MIN_IDLE = 0;




    public RedisClientConfig(RedisURI redisURI, StringCodec stringCodec, int maxTotal, int maxIdle, int minIdle) {
        this.redisURI = redisURI;
        this.stringCodec = stringCodec;
        this.redisClient = RedisClient.create(redisURI);
        this.boundedPoolConfig = BoundedPoolConfig.builder()
                .maxTotal(maxTotal)
                .maxIdle(maxIdle)
                .minIdle(minIdle)
                .testOnAcquire()
                .testOnCreate()
                .testOnRelease()
                .build();
    }

    public RedisClientConfig(RedisURI redisURI) {
        this(redisURI,StringCodec.UTF8,DEFAULT_MAX_TOTAL,DEFAULT_MAX_IDLE,DEFAULT_MIN_IDLE);
    }



    public RedisClient getRedisClient() {
        return redisClient;
    }

    public RedisURI getRedisURI() {
        return redisURI;
    }

    public StringCodec getStringCodec() {
        return stringCodec;
    }

    public BoundedPoolConfig getBoundedPoolConfig() {
        return boundedPoolConfig;
    }

}
