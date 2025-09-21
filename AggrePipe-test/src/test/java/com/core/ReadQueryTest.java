package com.core;


import com.core.config.RedisClientConfig;
import com.core.config.TimeoutConfig;
import com.core.operation.LuaScript;
import com.core.operation.LuaScriptFactory;
import com.core.operation.LuaScriptForReading;
import com.core.support.AggQueryBindingHandler;
import com.core.support.ReadQueryBindingHandler;
import com.redis.testcontainers.RedisContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;


@Testcontainers
@SpringBootTest(classes = com.core.Config.class)
public class ReadQueryTest {


    private RedisAsyncCommands<String, String> async;

    private RedisConnection redisConnection;


    @Autowired
    private AggQueryBindingHandler aggQueryBindingHandler;

    @Autowired
    private ReadQueryBindingHandler readQueryBindingHandler;


    @Container
    private static final RedisContainer container = new RedisContainer(
            RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));


    @Test
    public void test() {
        String serialNumber = "serialNumber";
        String idempKey = "idempKey";
        int ttl = 5000;
        int number = 1000;
        int range = 10;

        LuaScript luaScript = LuaScriptFactory.create(serialNumber, idempKey, ttl);
        LuaScriptForReading luaScriptForReading = LuaScriptFactory.create(serialNumber);


        // given
        List<QueryDto> queryDtos = List.of(
                new QueryDto(1L, 5L, 10.1, 10L, LocalDateTime.now()),
                new QueryDto(1L, 5L, 20.1, 20L, LocalDateTime.now()),
                new QueryDto(1L,5L,30.1,30L, LocalDateTime.now())
        );

        aggQueryBindingHandler.store(serialNumber, "chunk_unique_token", queryDtos);
        ChunkUpdatePayload p = aggQueryBindingHandler.buildPayload(serialNumber, "chunk_unique_token");
        redisConnection.eval(luaScript, p);

        // when
        ReadDto readDto = new ReadDto(1L, 5L);
        ChunkReadPayload payload = readQueryBindingHandler.build(serialNumber, List.of(readDto));

        RedisReadResultSet resultSet = redisConnection.read(luaScriptForReading, payload);


        ReadDto result = readQueryBindingHandler.recordValue(resultSet, readDto.getClass()).get(0);


        double uniPriceSum = 10.1 + 20.1 + 30.1;


        Assertions.assertThat(result.getMin_quanitity()).isEqualTo(10L);
        Assertions.assertThat(equalByScale(result.getMaxUnitPirce(), uniPriceSum, 15));
        Assertions.assertThat(equalByScale(result.getMaxUnitPirce(), 30.1, 15));
        Assertions.assertThat(equalByScale(result.getMinUnitPrice(), 10.1, 15));

    }


    boolean equalByScale(double a, double b, int scale) {
        BigDecimal A = BigDecimal.valueOf(a).setScale(scale, RoundingMode.HALF_UP);
        BigDecimal B = BigDecimal.valueOf(b).setScale(scale, RoundingMode.HALF_UP);
        return A.compareTo(B) == 0;
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
    void after() throws ExecutionException, InterruptedException {
        async.flushdb().get();

        if(redisConnection!=null)
            redisConnection.releaseConnection();
    }
}
