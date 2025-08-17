package com.core;

import com.redis.testcontainers.RedisContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;



@Testcontainers
public class RedisConnectionTest {

    private RedisClient redisClient;


    @Container
    private static final RedisContainer container = new RedisContainer(
            RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));


    @BeforeEach
    public void before() {
        String redisURI = container.getRedisURI();
        redisClient = RedisClient.create(redisURI);
    }

    @Test
    public void connect() {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        String ping = connection.sync().ping();

        Assertions.assertThat(ping).isEqualTo("PONG");
    }


    @AfterEach
    void after() {
        if (redisClient != null) {
            redisClient.shutdown();
        }
    }
}
