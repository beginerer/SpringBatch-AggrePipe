package com.core.example.config;

import com.core.config.RedisClientConfig;
import com.core.connection.RedisConnection;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Value("${redis.url}")
    private String url;


    @Bean
    public RedisClientConfig redisClientConfig() {
        return new RedisClientConfig(RedisURI.create(url));
    }

    @Bean
    public RedisConnection redisConnection(RedisClientConfig redisClientConfig) {
        RedisClient redisClient = redisClientConfig.getRedisClient();
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisAsyncCommands<String, String> async = connection.async();

        return new RedisConnection(connection, async, redisClientConfig.getTimeoutConfig());
    }
}
