package com.core;

import com.core.config.RedisClientConfig;
import com.core.config.TimeoutConfig;
import com.core.operation.LuaScript;
import com.redis.testcontainers.RedisContainer;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Testcontainers
public class RedisConnectionTest {

    private RedisAsyncCommands<String, String> async;
    private RedisConnection redisConnection;


    @Container
    private static final RedisContainer container = new RedisContainer(
            RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));



    @Test
    public void connect() {
        StatefulRedisConnection<String, String> connection = redisConnection.getConnection();

        String ping = connection.sync().ping();

        Assertions.assertThat(ping).isEqualTo("PONG");
    }


    @Test
    @DisplayName("[max] eval test")
    public void test() throws ExecutionException, InterruptedException {
        String setKey = "setKey";
        String numKey = "numKey";
        String requestId = "requestId:1";
        Long value = 10L;


        //given
        LuaScript luaScript = LuaScript.max(setKey, numKey);

        //when
        Long result = redisConnection.evalAsLong(luaScript, requestId, String.valueOf(value));

        //then
        String current = async.get(numKey).get();
        Long setSize = async.scard(setKey).get();
        Boolean requestSuccess = async.sismember(setKey, requestId).get();



        Assertions.assertThat(result).isEqualTo(10L);
        Assertions.assertThat(current).isEqualTo("10");
        Assertions.assertThat(requestSuccess).isTrue();
        Assertions.assertThat(setSize).isEqualTo(1L);
    }

    @Test
    @DisplayName("[max] Idempotency test")
    public void test2() throws ExecutionException, InterruptedException {
        String setKey = "setKey";
        String numKey = "numKey";
        String requestId = "requestId:1";
        Long value = 10L;
        Long value2 = 11L;

        //given
        LuaScript luaScript = LuaScript.max(setKey, numKey);

        //when
        Long result = redisConnection.evalAsLong(luaScript, requestId, String.valueOf(value));
        Long result2 = redisConnection.evalAsLong(luaScript, requestId, String.valueOf(value2));

        //then
        String current = async.get(numKey).get();
        Long setSize = async.scard(setKey).get();
        Boolean requestSuccess = async.sismember(setKey, requestId).get();


        Assertions.assertThat(result).isEqualTo(result2);

        Assertions.assertThat(result).isEqualTo(10L);
        Assertions.assertThat(current).isEqualTo("10");
        Assertions.assertThat(requestSuccess).isTrue();
        Assertions.assertThat(setSize).isEqualTo(1L);
    }











    @BeforeEach
    public void before() {
        String redisURI = container.getRedisURI();
        RedisURI uri = RedisURI.create(redisURI);

        RedisClientConfig config = new RedisClientConfig(uri);
        RedisClient redisClient = config.getRedisClient();

        StatefulRedisConnection<String, String> connection = redisClient.connect();
        this.async = connection.async();
        TimeoutConfig timeoutConfig = TimeoutConfig.create();

        this.redisConnection = new RedisConnection(connection, async, timeoutConfig);
    }


    @AfterEach
    void after() {

        if(redisConnection!=null)
            redisConnection.releaseConnection();
    }

}
