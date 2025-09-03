package com.core;

import com.core.config.RedisClientConfig;
import com.core.config.TimeoutConfig;
import com.core.operation.LuaScript;
import com.core.operation.LuaScriptLongTypeFactory;
import com.core.operation.Operation;
import com.core.result.RedisLongResultSet;
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

import java.util.concurrent.ExecutionException;


@Testcontainers
public class LuaScriptWithLongTest {

    private RedisAsyncCommands<String, String> async;
    private RedisConnection redisConnection;


    @Container
    private static final RedisContainer container = new RedisContainer(
            RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));


    @Test
    @DisplayName("[SUM,COUNT,MAX,MIN] : first request test")
    public void test() throws ExecutionException, InterruptedException {
        String idemKey = "idemKey";
        String hKey = "hKey";

        String requestId = "requestId";
        Long value = 10L;

        // given
        LuaScript luaScript = LuaScriptLongTypeFactory.LongAsAll(idemKey, hKey);

        // when
        RedisLongResultSet resultSet = redisConnection.evalAsLong(luaScript, requestId, String.valueOf(value));

        // then
        Boolean requestSuccess = async.sismember(idemKey, requestId).get();
        Long setSize = async.scard(idemKey).get();

        String sum = async.hget(hKey, "0").get();
        String count = async.hget(hKey, "1").get();
        String max = async.hget(hKey, "2").get();
        String min = async.hget(hKey, "3").get();

        Assertions.assertThat(resultSet.has(Operation.SUM)).isTrue();
        Assertions.assertThat(resultSet.has(Operation.COUNT)).isTrue();
        Assertions.assertThat(resultSet.has(Operation.MAX)).isTrue();
        Assertions.assertThat(resultSet.has(Operation.MIN)).isTrue();

        Assertions.assertThat(resultSet.getSum()).isEqualTo(value);
        Assertions.assertThat(resultSet.getCount()).isEqualTo(1L);
        Assertions.assertThat(resultSet.getMax()).isEqualTo(value);
        Assertions.assertThat(resultSet.getMin()).isEqualTo(value);

        Assertions.assertThat(requestSuccess).isTrue();
        Assertions.assertThat(setSize).isEqualTo(1L);

        Assertions.assertThat(sum).isEqualTo(String.valueOf(value));
        Assertions.assertThat(count).isEqualTo("1");
        Assertions.assertThat(max).isEqualTo(String.valueOf(value));
        Assertions.assertThat(min).isEqualTo(String.valueOf(value));
    }

    @Test
    @DisplayName("[SUM,COUNT,MAX,MIN] : sequence Test")
    public void test2() throws ExecutionException, InterruptedException {

        String idemKey = "idemKey";
        String hKey = "hKey";

        String requestId = "requestId:1";
        Long value = 10L;

        String requestId2 = "requestId:2";
        Long value2 = 11L;

        String requestId3 = "requestId:3";
        Long value3 = 12L;


        // given
        LuaScript luaScript = LuaScriptLongTypeFactory.LongAsAll(idemKey, hKey);

        // when
        RedisLongResultSet resultSet1 = redisConnection.evalAsLong(luaScript, requestId, String.valueOf(value));
        RedisLongResultSet resultSet2 = redisConnection.evalAsLong(luaScript, requestId2, String.valueOf(value2));
        RedisLongResultSet resultSet3 = redisConnection.evalAsLong(luaScript, requestId3, String.valueOf(value3));

        // then
        Boolean reqSuccess1 = async.sismember(idemKey, requestId).get();
        Boolean reqSuccess2 = async.sismember(idemKey, requestId2).get();
        Boolean reqSuccess3 = async.sismember(idemKey, requestId3).get();
        Long setSize = async.scard(idemKey).get();

        String sum = async.hget(hKey, "0").get();
        String count = async.hget(hKey, "1").get();
        String max = async.hget(hKey, "2").get();
        String min = async.hget(hKey, "3").get();


        Assertions.assertThat(resultSet1.has(Operation.SUM)).isTrue();
        Assertions.assertThat(resultSet1.has(Operation.COUNT)).isTrue();
        Assertions.assertThat(resultSet1.has(Operation.MAX)).isTrue();
        Assertions.assertThat(resultSet1.has(Operation.MIN)).isTrue();

        Assertions.assertThat(resultSet2.has(Operation.SUM)).isTrue();
        Assertions.assertThat(resultSet2.has(Operation.COUNT)).isTrue();
        Assertions.assertThat(resultSet2.has(Operation.MAX)).isTrue();
        Assertions.assertThat(resultSet2.has(Operation.MIN)).isTrue();

        Assertions.assertThat(resultSet3.has(Operation.SUM)).isTrue();
        Assertions.assertThat(resultSet3.has(Operation.COUNT)).isTrue();
        Assertions.assertThat(resultSet3.has(Operation.MAX)).isTrue();
        Assertions.assertThat(resultSet3.has(Operation.MIN)).isTrue();


        Assertions.assertThat(resultSet3.getSum()).isEqualTo(value + value2 + value3);
        Assertions.assertThat(resultSet3.getCount()).isEqualTo(3L);
        Assertions.assertThat(resultSet3.getMax()).isEqualTo(value3);
        Assertions.assertThat(resultSet3.getMin()).isEqualTo(value);


        Assertions.assertThat(reqSuccess1).isTrue();
        Assertions.assertThat(reqSuccess2).isTrue();
        Assertions.assertThat(reqSuccess3).isTrue();
        Assertions.assertThat(setSize).isEqualTo(3L);


        Assertions.assertThat(sum).isEqualTo(String.valueOf(value + value2 + value3));
        Assertions.assertThat(count).isEqualTo("3");
        Assertions.assertThat(max).isEqualTo(String.valueOf(value3));
        Assertions.assertThat(min).isEqualTo(String.valueOf(value));
    }

    @Test
    @DisplayName("[SUM,COUNT,MAX,MIN] : Idempotency Test")
    public void test3() throws ExecutionException, InterruptedException {
        String idemKey = "idemKey";
        String hKey = "hKey";

        String requestId = "requestId";
        Long value = 10L;

        // given
        LuaScript luaScript = LuaScriptLongTypeFactory.LongAsAll(idemKey, hKey);

        // when
        RedisLongResultSet resultSet1 = redisConnection.evalAsLong(luaScript, requestId, String.valueOf(value));
        RedisLongResultSet resultSet2 = redisConnection.evalAsLong(luaScript, requestId, String.valueOf(value));


        // then
        Boolean reqSuccess1 = async.sismember(idemKey, requestId).get();
        Boolean reqSuccess2 = async.sismember(idemKey, requestId).get();
        Long setSize = async.scard(idemKey).get();

        String sum = async.hget(hKey, "0").get();
        String count = async.hget(hKey, "1").get();
        String max = async.hget(hKey, "2").get();
        String min = async.hget(hKey, "3").get();

        Assertions.assertThat(resultSet1.has(Operation.SUM)).isTrue();
        Assertions.assertThat(resultSet1.has(Operation.COUNT)).isTrue();
        Assertions.assertThat(resultSet1.has(Operation.MAX)).isTrue();
        Assertions.assertThat(resultSet1.has(Operation.MIN)).isTrue();

        Assertions.assertThat(resultSet2.has(Operation.SUM)).isTrue();
        Assertions.assertThat(resultSet2.has(Operation.COUNT)).isTrue();
        Assertions.assertThat(resultSet2.has(Operation.MAX)).isTrue();
        Assertions.assertThat(resultSet2.has(Operation.MIN)).isTrue();

        Assertions.assertThat(resultSet1.getSum()).isEqualTo(resultSet2.getSum());
        Assertions.assertThat(resultSet1.getCount()).isEqualTo(resultSet2.getCount());
        Assertions.assertThat(resultSet1.getMax()).isEqualTo(resultSet2.getMax());
        Assertions.assertThat(resultSet1.getMin()).isEqualTo(resultSet2.getMin());


        Assertions.assertThat(resultSet2.getSum()).isEqualTo(value);
        Assertions.assertThat(resultSet2.getCount()).isEqualTo(1L);
        Assertions.assertThat(resultSet2.getMax()).isEqualTo(value);
        Assertions.assertThat(resultSet2.getMin()).isEqualTo(value);

        Assertions.assertThat(reqSuccess1).isTrue();
        Assertions.assertThat(reqSuccess2).isTrue();
        Assertions.assertThat(setSize).isEqualTo(1L);

        Assertions.assertThat(sum).isEqualTo(String.valueOf(value));
        Assertions.assertThat(count).isEqualTo("1");
        Assertions.assertThat(max).isEqualTo(String.valueOf(value));
        Assertions.assertThat(min).isEqualTo(String.valueOf(value));
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
