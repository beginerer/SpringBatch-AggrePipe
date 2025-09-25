package com.core;

import com.core.config.RedisClientConfig;
import com.core.config.TimeoutConfig;
import com.core.connection.RedisConnection;
import com.core.example.dto.QueryDto;
import com.core.example.dto.QueryDtoFactory;
import com.core.example.dto.ReadDto;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;



@Testcontainers
@SpringBootTest

public class IntegrationTest {


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
        int percentage = 3;

        int number = 1000;
        int range = 1000;


        // given
        LuaScript luaScript = LuaScriptFactory.create(serialNumber, idempKey, ttl);
        LuaScriptForReading luaScriptForReading = LuaScriptFactory.create(serialNumber);

        // statistics
        QueryDtoFactory.statistics statistics = QueryDtoFactory.getStatistics(serialNumber, number, percentage, range);

        Map<String, QueryDtoFactory.staticCal> data = statistics.getData();

        List<QueryDto> writeDtos = statistics.getQueryDto();
        List<ReadDto> readDtos = writeDtos.stream().map(dto -> {
            Long userId = dto.getUserId();
            Long orderId = dto.getOrderId();
            return new ReadDto(userId, orderId);
        }).toList();

        aggQueryBindingHandler.store(serialNumber, "chunk_unique_token", writeDtos);
        ChunkUpdatePayload writePayload = aggQueryBindingHandler.buildPayload(serialNumber, "chunk_unique_token");

        // write redis request
        redisConnection.eval(luaScript, writePayload);

        //when
        ChunkReadPayload readPayload = readQueryBindingHandler.buildPayload(serialNumber, readDtos);

        RedisReadResultSet resultSet = redisConnection.read(luaScriptForReading, readPayload);
        List<ReadDto> results = readQueryBindingHandler.recordValue(resultSet, ReadDto.class);


        for (ReadDto result : results) {
            String groupKey = generateGroupByKey(serialNumber, result.getUserId(), result.getOrderId());
            QueryDtoFactory.staticCal cal = data.get(groupKey);

            if(cal == null)
                throw new IllegalStateException("[ERROR] group key is not included in redis.");

            Double summUnitPrice = result.getSumm_unitPrice();
            Double maxUnitPirce = result.getMaxUnitPirce();
            Double minUnitPrice = result.getMinUnitPrice();
            Long minQuanitity = result.getMin_quanitity();

            Assertions.assertThat(equalByScale(summUnitPrice, cal.sum_unitPrice, 15));
            Assertions.assertThat(equalByScale(maxUnitPirce, cal.max_unitPrice, 15));
            Assertions.assertThat(equalByScale(minUnitPrice, cal.min_unitPrice, 15));
            Assertions.assertThat(minQuanitity).isEqualTo(cal.min_quantity);
        }
    }





    boolean equalByScale(double a, double b, int scale) {
        BigDecimal A = BigDecimal.valueOf(a).setScale(scale, RoundingMode.HALF_UP);
        BigDecimal B = BigDecimal.valueOf(b).setScale(scale, RoundingMode.HALF_UP);
        return A.compareTo(B) == 0;
    }




    private String generateGroupByKey(String serialNumber, long userId, long orderId) {
        return "["+serialNumber +"]" + userId + "," +  orderId;
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
