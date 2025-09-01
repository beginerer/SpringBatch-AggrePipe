package com.core;

import com.core.config.RedisClientConfig;
import com.redis.testcontainers.RedisContainer;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Testcontainers
public class RedisConnectionTest {

    private RedisClient redisClient;

    private StatefulRedisConnection<String, String> connection;

    @Container
    private static final RedisContainer container = new RedisContainer(
            RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));




    @Test
    public void connect() {
        StatefulRedisConnection<String, String> connection = redisClient.connect();

        String ping = connection.sync().ping();

        Assertions.assertThat(ping).isEqualTo("PONG");
    }


    @Test
    public void test() throws ExecutionException, InterruptedException, TimeoutException {
        RedisAsyncCommands<String, String> async = connection.async();

        String key = "redis:key";
        String value = "redis:value";
        String newValue = "redis:newValue";

        async.set(key,value);

        async.watch(key);
        async.multi();
        RedisFuture<String> setF = async.set(key, newValue);

        TransactionResult tr = async.exec().get(500, TimeUnit.MILLISECONDS);

        String response = setF.get(500, TimeUnit.MILLISECONDS);

        String v = async.get(key).get(500, TimeUnit.MILLISECONDS);


        Assertions.assertThat(tr.wasDiscarded()).isFalse();
        Assertions.assertThat(response).isEqualTo("OK");
        Assertions.assertThat(v).isEqualTo(newValue);
    }


    @Test
    public void test2() throws ExecutionException, InterruptedException, TimeoutException {
        RedisAsyncCommands<String, String> async = connection.async();

        String key = "redis:key";
        String value = "redis:value";
        String newValue = "redis:newValue";
        String newValue2 = "redis:newValue2";

        async.set(key,value);

        async.watch(key);

        async.set(key, newValue);

        async.multi();
        RedisFuture<String> setF = async.set(key, newValue2);
        TransactionResult tr = async.exec().get(500, TimeUnit.MILLISECONDS);

        String v = async.get(key).get(500, TimeUnit.MILLISECONDS);
        String s = setF.get(500, TimeUnit.MILLISECONDS);


        Assertions.assertThat(tr.wasDiscarded()).isTrue();
        Assertions.assertThat(v).isEqualTo(newValue);
        Assertions.assertThat(s).isNull();
    }


    @Test
    public void test3() throws ExecutionException, InterruptedException, TimeoutException {
        RedisAsyncCommands<String, String> async = connection.async();
        RedisFuture<String> multiF = async.multi();
        RedisFuture<String> multiF2 = async.multi();

        String s = multiF.get(500, TimeUnit.MILLISECONDS);
        Assertions.assertThatThrownBy(() -> multiF2.get(500, TimeUnit.MILLISECONDS))
                .isInstanceOf(ExecutionException.class);

    }

    @Test
    public void test4() throws ExecutionException, InterruptedException, TimeoutException {
        connection.setAutoFlushCommands(false);
        RedisAsyncCommands<String, String> async = connection.async();

        String key = "redis:key";

        async.multi();
        RedisFuture<String> setF = async.set(key, "world");
        async.multi();
        var execF = async.exec();
        connection.flushCommands();

        String s = setF.get(500, TimeUnit.MILLISECONDS);
        Assertions.assertThat(s).isEqualTo("OK");
        Assertions.assertThat(execF.get(500,TimeUnit.MILLISECONDS).wasDiscarded()).isTrue();
        connection.setAutoFlushCommands(true);
    }

    @Test
    public void test5() throws ExecutionException, InterruptedException, TimeoutException {
        connection.setAutoFlushCommands(false);
        RedisAsyncCommands<String, String> async = connection.async();

        String key = "redis:key";

        async.set(key,"hello"); // 초기값
        async.watch(key);

        async.set(key,"hello2");

        var multiF = async.multi();
        var setF = async.set(key, "hello3");
        var execF = async.exec();


        connection.flushCommands();

        TransactionResult tr= execF.get(500, TimeUnit.MILLISECONDS);
        Assertions.assertThat(tr.wasDiscarded()).isTrue();

        Assertions.assertThat(setF.get(500,TimeUnit.MILLISECONDS)).isEqualTo("OK");

    }







    @BeforeEach
    public void before() {
        String redisURI = container.getRedisURI();
        RedisURI uri = RedisURI.create(redisURI);

        RedisClientConfig config = new RedisClientConfig(uri);
        this.redisClient = config.getRedisClient();
        this.connection = redisClient.connect();
    }

    @AfterEach
    void after() {

        if (redisClient != null) {
            redisClient.shutdown();
        }
    }

}
