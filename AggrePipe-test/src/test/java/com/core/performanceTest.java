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
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


@Testcontainers
@SpringBootTest
public class performanceTest {

    private static RedisAsyncCommands<String, String> async;

    private static RedisConnection redisConnection;


    @Autowired
    private AggQueryBindingHandler aggQueryBindingHandler;

    @Autowired
    private ReadQueryBindingHandler readQueryBindingHandler;


    @Container
    private static final RedisContainer container = new RedisContainer(
            RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));



    private static final Path OUT = Paths.get("target/aggre-bench.csv");

    private static final Object LOCK = new Object();

    @BeforeAll
    static void initCsv() throws IOException {
        Files.createDirectories(OUT.getParent());
        try (var w = Files.newBufferedWriter(
                OUT, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            w.write("number,range,percentage,tookMs,ok");
            w.write(System.lineSeparator());
        }
    }


    private static void appendRow(int id, int number, int range, int percentage, long tookMs, boolean ok) {
        String line = id + "," + number + "," + range + "," + percentage + "," + tookMs + "," + ok + System.lineSeparator();
        synchronized (LOCK) {
            try {
                Files.writeString(
                        OUT, line, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND
                );
            } catch (IOException e) {
                throw new RuntimeException("CSV 기록 실패: " + OUT.toAbsolutePath(), e);
            }
        }
    }



    @ParameterizedTest
    @CsvFileSource(resources = "/case1.csv", numLinesToSkip = 1)
    @DisplayName("performance test")
    public void writeBench(int id, int number, int range, int percentage) throws ExecutionException, InterruptedException, TimeoutException {
        String serialNumber = "serialNumber";
        String idempKey = "idempKey";
        int ttl = 5000;


        // given
        LuaScript luaScript = LuaScriptFactory.create(serialNumber, idempKey, ttl);
        QueryDtoFactory.statistics statistics = QueryDtoFactory.getStatistics(serialNumber, number, percentage, range);
        List<QueryDto> queryDto = statistics.getQueryDto();

        //when
        aggQueryBindingHandler.store(serialNumber, "chunk_unique_token", queryDto);
        ChunkUpdatePayload payload = aggQueryBindingHandler.buildPayload(serialNumber, "chunk_unique_token");

        long start = System.currentTimeMillis();
        RedisWriteResultSet result = redisConnection.eval(luaScript, payload);
        long end = System.currentTimeMillis();
        long gap = end - start;
        System.out.println("time: " + gap);

        boolean success = result.isSuccess();

        aggQueryBindingHandler.flushPayLoad(result);
        appendRow(id,number, range, percentage, gap, success);
    }


    @ParameterizedTest
    @CsvFileSource(resources = "/case1.csv", numLinesToSkip = 1)
    @DisplayName("performance test")
    public void readBench(int id, int number, int range, int percentage) {

        String serialNumber = "serialNumber";
        String idempKey = "idempKey";
        int ttl = 5000;


        // given
        LuaScript luaScript = LuaScriptFactory.create(serialNumber, idempKey, ttl);
        LuaScriptForReading luaScriptForReading = LuaScriptFactory.create(serialNumber);

        // statistics
        QueryDtoFactory.statistics statistics = QueryDtoFactory.getStatistics(serialNumber, number, percentage,  range);

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
        RedisWriteResultSet eval = redisConnection.eval(luaScript, writePayload);
        aggQueryBindingHandler.flushPayLoad(eval);

        //when
        ChunkReadPayload readPayload = readQueryBindingHandler.buildPayload(serialNumber, readDtos);

        long start = System.currentTimeMillis();
        RedisReadResultSet resultSet = redisConnection.read(luaScriptForReading, readPayload);
        long end = System.currentTimeMillis();
        long gap = end - start;
        System.out.println("time: " + gap);

        List<ReadDto> results = readQueryBindingHandler.recordValue(resultSet, ReadDto.class);
        appendRow(id,number, range, percentage, gap, true);
    }

    private String generateGroupByKey(String serialNumber, long userId, long orderId) {
        return "["+serialNumber +"]" + userId +  orderId;
    }



    @BeforeAll
    public static void before() {
        String redisURI = container.getRedisURI();
        RedisURI uri = RedisURI.create(redisURI);

        RedisClientConfig config = new RedisClientConfig(uri);
        RedisClient redisClient = config.getRedisClient();

        StatefulRedisConnection<String, String> connection = redisClient.connect();
        async = connection.async();
        TimeoutConfig timeoutConfig = TimeoutConfig.create();
        redisConnection = new RedisConnection(connection, async, timeoutConfig);
    }


    @AfterEach
    void after() throws ExecutionException, InterruptedException {
        async.flushdb().get();
    }

}
