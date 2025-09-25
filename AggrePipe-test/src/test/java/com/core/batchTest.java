package com.core;

import com.core.config.RedisClientConfig;
import com.core.config.TimeoutConfig;
import com.core.connection.RedisConnection;
import com.core.example.RedisConnectionProxy;
import com.core.support.AggQueryBindingHandler;
import com.core.support.ReadQueryBindingHandler;
import com.redis.testcontainers.RedisContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@Testcontainers
@SpringBootTest(properties = "spring.batch.job.enabled=false")
@SpringBatchTest
@ActiveProfiles("test")
public class batchTest {

    private RedisAsyncCommands<String, String> async;

    private RedisConnectionProxy redisConnectionProxy;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils; // spring-batch-test 제공

    @Autowired
    private Job itemSaleJob;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;





    @Autowired
    private AggQueryBindingHandler aggQueryBindingHandler;

    @Autowired
    private ReadQueryBindingHandler readQueryBindingHandler;

    @Container
    private static final RedisContainer container = new RedisContainer(
            RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));


    @Test
    void runItemSaleJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("serialNumber", "SN-001")
                .addString("readerName", "itemSaleReader")
                .addLong("chunkSize", 1000L)               // 정수는 Long로 넣어두면 안전
                .addString("idempKey", "test-run")
                .addString("from", "2024-01-01T00:00:00")   // @Value LocalDateTime이면 ISO 문자열로 전달
                .addString("to",   "2026-01-31T23:59:59")
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
        StepExecution step = execution.getStepExecutions().iterator().next();
        assertThat(step.getReadCount()).isGreaterThan(0);
    }

    @Test
    void test() throws Exception {
        String serialNumebr = String.valueOf(System.currentTimeMillis());
        JobParameters params = new JobParametersBuilder()
                .addString("serialNumber", serialNumebr)
                .addString("readerName", "itemSaleReader")
                .addLong("chunkSize", 1000L)               // 정수는 Long로 넣어두면 안전
                .addString("idempKey", "test-run")
                .addString("from", "2024-01-01T00:00:00")   // @Value LocalDateTime이면 ISO 문자열로 전달
                .addString("to",   "2026-01-31T23:59:59")
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        ConcurrentLinkedQueue<RedisWriteResultSet> list = redisConnectionProxy.getList();

        for (RedisWriteResultSet resultSet : list) {
            List<Chunk> data = resultSet.getData();

            for (Chunk datum : data) {
                for (String key : datum.getItems().keySet()) {

                    Map<String, String> stringStringMap = async.hgetall(key).get(redisConnectionProxy.timeAmount, redisConnectionProxy.timeUnit);
                    System.out.println(stringStringMap.size());
                }


            }


        }


    }





    @BeforeEach
    void setup() {
        jobLauncherTestUtils.setJob(itemSaleJob); // 여러 잡 있을 때 어떤 잡을 실행할지 지정
        jobRepositoryTestUtils.removeJobExecutions(); // 이전 실행 흔적 제거
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

        this.redisConnectionProxy = new RedisConnectionProxy(connection, async, timeoutConfig);
    }


    @AfterEach
    void after() throws ExecutionException, InterruptedException {
        async.flushdb().get();

        if(redisConnectionProxy !=null)
            redisConnectionProxy.releaseConnection();
    }
}
