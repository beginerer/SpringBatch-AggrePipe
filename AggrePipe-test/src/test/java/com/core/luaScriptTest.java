package com.core;

import com.core.config.RedisClientConfig;
import com.core.config.TimeoutConfig;
import com.core.operation.*;
import com.core.support.AggQueryBindingHandler;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Testcontainers
@SpringBootTest(classes = com.core.Config.class)
public class luaScriptTest {

    private RedisAsyncCommands<String, String> async;

    private RedisConnection redisConnection;


    @Autowired
    private AggQueryBindingHandler handler;


    @Container
    private static final RedisContainer container = new RedisContainer(
            RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));



    @Test
    @DisplayName("eval test")
    public void test() throws ExecutionException, InterruptedException, TimeoutException {
        String serialNumber = "serialNumber";
        String idempKey = "idempKey";
        int ttl = 5000;
        int number = 1000;
        int range = 10;

        LuaScript luaScript = LuaScriptFactory.create(serialNumber, idempKey, ttl);
        QueryDtoFactory.statistics statistics = QueryDtoFactory.getStatistics(serialNumber, number, range);

        // given
        List<QueryDto> queryDto = statistics.getQueryDto();
        Map<String, Long> counts = statistics.getCounts();
        Map<String, QueryDtoFactory.staticCal> data = statistics.getData();

        //when
        handler.store(serialNumber, "chunk_unique_token", queryDto);
        ChunkUpdatePayload payload = handler.buildPayload(serialNumber, "chunk_unique_token");

        long start = System.currentTimeMillis();
        RedisResultSet result = redisConnection.eval(luaScript, payload);
        long end = System.currentTimeMillis();
        long gap = end - start;
        System.out.println("time: " + gap);

        String scriptSerialNumber = result.getScriptSerialNumber();
        Chunk  chunk = result.getData().get(0);
        boolean success = result.isSuccess();


        // then
        Assertions.assertThat(success).isTrue();

        for (var e : counts.entrySet()) {
            String key = e.getKey();
            String fieldName = Operation.resolveCountFieldName();
            String value = async.hget(key, fieldName).get(100, TimeUnit.MILLISECONDS);

            Assertions.assertThat(value).isEqualTo(String.valueOf(e.getValue()));
        }

        for (var e  : data.entrySet()) {
            String key = e.getKey();
            QueryDtoFactory.staticCal value = e.getValue();

            double sumUnitPrice = value.sum_unitPrice;
            double maxUnitPrice = value.max_unitPrice;
            double minUnitPrice = value.min_unitPrice;

            long maxQuantity = value.max_quantity;
            long minQuantity = value.min_quantity;

            String sum_unitPrice_name = Operation.resolveFieldName(Operation.SUM, "unitPrice");
            String max_unitPrice_name = Operation.resolveFieldName(Operation.MAX, "unitPrice");
            String min_unitPrice_name = Operation.resolveFieldName(Operation.MIN, "unitPrice");

            String max_quantity_name = Operation.resolveFieldName(Operation.MAX, "quantity");
            String min_quantity_name = Operation.resolveFieldName(Operation.MIN, "quantity");

            String sum_unitPrice = async.hget(key, sum_unitPrice_name).get(100, TimeUnit.MILLISECONDS);
            String max_unitPrice = async.hget(key, max_unitPrice_name).get(100, TimeUnit.MILLISECONDS);
            String min_unitPrice = async.hget(key, min_unitPrice_name).get(100, TimeUnit.MILLISECONDS);

            String max_quantity = async.hget(key, max_quantity_name).get(100, TimeUnit.MILLISECONDS);
            String min_quantity = async.hget(key, min_quantity_name).get(100, TimeUnit.MILLISECONDS);

            Assertions.assertThat(equalByScale(sumUnitPrice, Double.valueOf(sum_unitPrice), 15));
            Assertions.assertThat(equalByScale(maxUnitPrice, Double.valueOf(max_unitPrice), 15));
            Assertions.assertThat(equalByScale(minUnitPrice, Double.valueOf(min_unitPrice), 15));

            Assertions.assertThat(max_quantity).isEqualTo(String.valueOf(maxQuantity));
            Assertions.assertThat(min_quantity).isEqualTo(String.valueOf(minQuantity));
        }
    }

    @Test
    @DisplayName("evalsha test")
    public void test2() throws ExecutionException, InterruptedException, TimeoutException {
        String serialNumber = "serialNumber";
        String idempKey = "idempKey";
        int ttl = 5000;
        int number = 10000;
        int range = 100;

        LuaScript luaScript = LuaScriptFactory.create(serialNumber, idempKey, ttl);
        QueryDtoFactory.statistics statistics = QueryDtoFactory.getStatistics(serialNumber, number, range);

        DigestLuaScript digestLuaScript = redisConnection.loadScript(luaScript);

        // given
        List<QueryDto> queryDto = statistics.getQueryDto();
        Map<String, Long> counts = statistics.getCounts();
        Map<String, QueryDtoFactory.staticCal> data = statistics.getData();

        //when
        handler.store(serialNumber, "chunk_unique_token", queryDto);
        ChunkUpdatePayload payload = handler.buildPayload(serialNumber, "chunk_unique_token");

        long start = System.currentTimeMillis();
        RedisResultSet result = redisConnection.evalsha(digestLuaScript, payload);
        long end = System.currentTimeMillis();
        long gap = end - start;
        System.out.println("time: " + gap);

        String scriptSerialNumber = result.getScriptSerialNumber();
        Chunk  chunk = result.getData().get(0);
        boolean success = result.isSuccess();


        // then
        Assertions.assertThat(success).isTrue();

        for (var e : counts.entrySet()) {
            String key = e.getKey();
            String fieldName = Operation.resolveCountFieldName();
            String value = async.hget(key, fieldName).get(100, TimeUnit.MILLISECONDS);

            Assertions.assertThat(value).isEqualTo(String.valueOf(e.getValue()));
        }

        for (var e  : data.entrySet()) {
            String key = e.getKey();
            QueryDtoFactory.staticCal value = e.getValue();

            double sumUnitPrice = value.sum_unitPrice;
            double maxUnitPrice = value.max_unitPrice;
            double minUnitPrice = value.min_unitPrice;

            long maxQuantity = value.max_quantity;
            long minQuantity = value.min_quantity;

            String sum_unitPrice_name = Operation.resolveFieldName(Operation.SUM, "unitPrice");
            String max_unitPrice_name = Operation.resolveFieldName(Operation.MAX, "unitPrice");
            String min_unitPrice_name = Operation.resolveFieldName(Operation.MIN, "unitPrice");

            String max_quantity_name = Operation.resolveFieldName(Operation.MAX, "quantity");
            String min_quantity_name = Operation.resolveFieldName(Operation.MIN, "quantity");

            String sum_unitPrice = async.hget(key, sum_unitPrice_name).get(100, TimeUnit.MILLISECONDS);
            String max_unitPrice = async.hget(key, max_unitPrice_name).get(100, TimeUnit.MILLISECONDS);
            String min_unitPrice = async.hget(key, min_unitPrice_name).get(100, TimeUnit.MILLISECONDS);

            String max_quantity = async.hget(key, max_quantity_name).get(100, TimeUnit.MILLISECONDS);
            String min_quantity = async.hget(key, min_quantity_name).get(100, TimeUnit.MILLISECONDS);

            Assertions.assertThat(equalByScale(sumUnitPrice, Double.valueOf(sum_unitPrice), 15));
            Assertions.assertThat(equalByScale(maxUnitPrice, Double.valueOf(max_unitPrice), 15));
            Assertions.assertThat(equalByScale(minUnitPrice, Double.valueOf(min_unitPrice), 15));

            Assertions.assertThat(max_quantity).isEqualTo(String.valueOf(maxQuantity));
            Assertions.assertThat(min_quantity).isEqualTo(String.valueOf(minQuantity));
        }
    }


    @Test
    @DisplayName("Reading Test")
    public void test3() {
        String serialNumber = "serialNumber";
        String idempKey = "idempKey";
        int ttl = 5000;
        int number = 1000;
        int range = 10;

        LuaScript luaScript = LuaScriptFactory.create(serialNumber, idempKey, ttl);
        LuaScriptForReading luaScriptForReading = LuaScriptFactory.create(serialNumber);
        QueryDtoFactory.statistics statistics = QueryDtoFactory.getStatistics(serialNumber, number, range);

        // given
        List<QueryDto> queryDto = statistics.getQueryDto();
        handler.store(serialNumber, "chunk_unique_token", queryDto);
        ChunkUpdatePayload payload = handler.buildPayload(serialNumber, "chunk_unique_token");
        redisConnection.eval(luaScript, payload);

        // when
        ChunkReadPayload result = redisConnection.read(luaScriptForReading, payload);


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
