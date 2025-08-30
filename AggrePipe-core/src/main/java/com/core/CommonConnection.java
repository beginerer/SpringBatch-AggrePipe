package com.core;

import com.excpetion.ConnectionClosedException;
import com.excpetion.ConnectionPoolClosedException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * auto flush is true
 * */
public class CommonConnection {

    private StatefulRedisConnection<String,String> connection;

    public final static TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

    public final static int DEFAULT_TIME_VALUE = 500;


    public CommonConnection(RedisClientConfig config) {

        RedisClient redisClient = config.getRedisClient();
        this.connection = redisClient.connect();
    }



    public RedisFuture<Long> incrBy(String key, long number) {

        final var conn = connection;

        if(key == null || key.isEmpty())
            throw new IllegalArgumentException("[ERROR] key is null");

        if(number < 0 )
            throw new IllegalArgumentException("[ERROR] number is smaller than zero");

        if(conn == null)
            throw new ConnectionClosedException("[ERROR] connection is closed");


        return conn.async().incrby(key,number);
    }


    public Long incr(String key) {

        final var conn = connection;

        if(key == null || key.isEmpty())
            throw new IllegalArgumentException("[ERROR] key is null");

        if(conn == null)
            throw new ConnectionClosedException("[ERROR] connection is closed");


        return conn.sync().incr(key);
    }



    public RedisFuture<String> readByKey(String key) {
        if(key == null || key.isEmpty())
            throw  new IllegalArgumentException("[ERROR] key is null");

        final var conn = this.connection;

        if(conn == null || !conn.isOpen())
            throw new ConnectionPoolClosedException("Connection pool/common connection is closed");

        RedisAsyncCommands<String, String> async = conn.async();
        return async.get(key);
    }



}
