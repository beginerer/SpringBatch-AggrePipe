package com.core;


import com.redis.testcontainers.RedisContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Testcontainers
public class ConnectionPoolTest {

    private RedisClientConfig config;

    private RedisConnectionPool pool;

    @Container
    private static final RedisContainer container = new RedisContainer(
            RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));

    @BeforeEach
    public void before() {
        String redisURI = container.getRedisURI();
        RedisURI uri = RedisURI.create(redisURI);

        config = new RedisClientConfig(uri);
        pool = new RedisConnectionPool(config);
    }

    @Test
    public void test() {
        int maxTotal = pool.getMaxTotal();

        List<CompletableFuture<RedisConnection>> futures =
                IntStream.range(0, maxTotal)
                        .mapToObj(i -> pool.acquireConnection().orTimeout(2, TimeUnit.SECONDS))
                        .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<RedisConnection> connections = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        Assertions.assertThat(pool.getMaxTotal()).isEqualTo(maxTotal);

        connections.forEach(rc -> {
            StatefulRedisConnection<String, String> conn = rc.getConnection();
            Assertions.assertThat(conn.isOpen()).isTrue();
        });
    }

    @Test
    @DisplayName("가용 커넥션이 없다면 예외가 발생한다.")
    public void test1() {
        int maxTotal = pool.getMaxTotal();

        List<CompletableFuture<RedisConnection>> futures =
                IntStream.range(0, maxTotal)
                        .mapToObj(i -> pool.acquireConnection().orTimeout(500, TimeUnit.MILLISECONDS))
                        .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<RedisConnection> connections = futures.stream().map(CompletableFuture::join).toList();

        Assertions.assertThat(pool.getMaxTotal()).isEqualTo(maxTotal);

        connections.forEach(rc -> {
            StatefulRedisConnection<String, String> conn = rc.getConnection();
            Assertions.assertThat(conn.isOpen()).isTrue();
        });

        var extra = pool.acquireConnection().orTimeout(500, TimeUnit.MILLISECONDS);

        Assertions.assertThatThrownBy(() -> extra.join())
                .isInstanceOf(CompletionException.class)
                .hasMessageContaining("[ERROR] Failed to acquire a connection (pool may be closed)");

    }

    @Test
    public void test2() {
        int maxTotal = pool.getMaxTotal();

        List<CompletableFuture<RedisConnection>> futures =
                IntStream.range(0, maxTotal)
                        .mapToObj(i -> pool.acquireConnection().orTimeout(500, TimeUnit.MILLISECONDS))
                        .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<RedisConnection> connections = futures.stream().map(CompletableFuture::join).toList();

        RedisConnection connection = connections.get(0);
        pool.releaseConnection(connection);

        var result = pool.acquireConnection().orTimeout(500,TimeUnit.MILLISECONDS);
        RedisConnection newConnection = result.join();

        Assertions.assertThat(newConnection.getConnection().isOpen()).isTrue();
    }



    @AfterEach
    public void after() {
        pool.releaseConnectionPool();
        final RedisClient redisClient = config.getRedisClient();

        if(redisClient !=null) {
            redisClient.shutdown();
        }
    }

}
