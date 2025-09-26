package com.core.example.config;


import com.core.example.query.Cursor;
import com.core.example.dto.ItemSaleForUserQueryDto;
import com.core.example.query.QuerydslCursorItemReaderConfig;
import com.core.operation.LuaScript;
import com.core.operation.LuaScriptFactory;
import com.core.support.AggQueryBindingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;


    private final QuerydslCursorItemReaderConfig config;


    private final ItemWriteListener itemWriteListener;




    @Bean
    @JobScope
    public LuaScript luaScript(
            @Value("#{jobParameters['serialNumber']}") String serialNumber,
            @Value("#{jobParameters['idempKey']}") String idempKey,
            @Value("#{jobParameters['ttl'] ?: 600}") Integer ttl
    ) {
        return LuaScriptFactory.create(serialNumber, idempKey, ttl);
    }




    @Bean
    @StepScope
    public QuerydslCursorItemReader<ItemSaleForUserQueryDto, Cursor> itemSaleReader(
            @Value("#{jobParameters['serialNumber']}") String serialNumber,
            @Value("#{jobParameters['readerName']}") String readerName,
            @Value("#{jobParameters['chunkSize']}") Integer chunkSize,
            @Value("#{jobParameters['from']}") LocalDateTime from,
            @Value("#{jobParameters['to']}") LocalDateTime to
    ) {
       return config.buildReader(serialNumber, readerName, chunkSize, from, to);
    }


    @Bean
    public Step itemSaleStep(
            QuerydslCursorItemReader<ItemSaleForUserQueryDto, Cursor> itemSaleReader,
            QueryDslCursorItemWriter itemWriter
    ) {
        return new StepBuilder("itemSaleStep", jobRepository)
                .<AggQueryBindingHandler.Key, AggQueryBindingHandler.Key>chunk(1000, transactionManager)
                .reader(itemSaleReader)
                .writer(itemWriter)
                .listener(itemWriteListener)
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .skipLimit(10)
                .skip(Exception.class)
                .build();
    }


    @Bean
    public Job itemSaleJob(Step itemSaleStep) {
        return new JobBuilder("itemSaleJob", jobRepository)
                .start(itemSaleStep)
                .incrementer(new UniqueRunIdIncrementer())
                .build();
    }
}
