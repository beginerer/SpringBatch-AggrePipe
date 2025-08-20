package com.core;

import com.redis.testcontainers.RedisContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@Testcontainers
public class CasWatchTest {

    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;
    private RedisConnectionPool pool;

    @Container
    private static final RedisContainer container = new RedisContainer(
            RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));



    @Test
    @DisplayName("max test")
    public void test() throws ExecutionException, InterruptedException, TimeoutException {


        RedisConnection redisConnection = pool.acquireConnection().get(500, TimeUnit.MILLISECONDS);
        RedisAsyncCommands<String, String> async = connection.async();

        // given
        String key ="redis:key";
        String value = "10";
        async.set(key,value);

        // when
        CompletableFuture<Boolean> future = redisConnection.casWithWatch(key, 11L, Operation.max, 3);

        // then
        Boolean result = future.get(500,TimeUnit.MILLISECONDS);
        String now = async.get(key).get(500, TimeUnit.MILLISECONDS);

        Assertions.assertThat(result).isTrue();
        Assertions.assertThat(now).isEqualTo("11");
    }

    @Test
    @DisplayName("min test")
    public void test2() throws ExecutionException, InterruptedException, TimeoutException {
        RedisConnection redisConnection = pool.acquireConnection().get(500, TimeUnit.MILLISECONDS);
        RedisAsyncCommands<String, String> async = connection.async();

        // given
        String key = "redis:key";
        String value = "10";
        async.set(key,value);

        // when
        CompletableFuture<Boolean> future = redisConnection.casWithWatch(key, 9L, Operation.min, 3);

        //then
        Boolean result = future.get(500, TimeUnit.MILLISECONDS);
        String now = async.get(key).get(500, TimeUnit.MILLISECONDS);

        Assertions.assertThat(result).isTrue();
        Assertions.assertThat(now).isEqualTo("9");
    }

    @Test
    @DisplayName("동시성 테스트")
    public void test3() throws ExecutionException, InterruptedException, TimeoutException {
        RedisConnection conn = pool.acquireConnection().get(500, TimeUnit.MILLISECONDS);
        RedisAsyncCommands<String, String> async = connection.async();


        // given
        String key = "redis:concurrent";
        String value = "10";
        async.set(key,value);

        ExecutorService es = Executors.newFixedThreadPool(2);
        CyclicBarrier start = new CyclicBarrier(2);


        Future<Boolean> f1 = es.submit(() -> {
            try {
                start.await();
                return conn.casWithWatch(key, 11L, Operation.max, 3).get(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        });

        Assertions.assertThat(f1.get(3, TimeUnit.SECONDS)).isTrue();




    }





    @BeforeEach
    public void before() {
        String redisURI = container.getRedisURI();
        RedisURI uri = RedisURI.create(redisURI);

        RedisClientConfig config = new RedisClientConfig(uri);
        this.redisClient = config.getRedisClient();
        this.connection = redisClient.connect();
        this.pool = new RedisConnectionPool(config);
    }

    @AfterEach
    public void after() {
        if(redisClient!=null)
            redisClient.close();
        if(pool!=null)
            pool.releaseConnectionPool();
    }
}
