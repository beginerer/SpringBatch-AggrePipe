package com.core;

import com.core.config.RedisClientConfig;
import com.core.config.TimeoutConfig;
import com.core.connection.RedisConnection;
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
