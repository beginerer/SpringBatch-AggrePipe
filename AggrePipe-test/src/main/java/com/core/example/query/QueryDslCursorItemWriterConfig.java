package com.core.example.query;

import com.core.batch.QueryDslCursorItemWriter;
import com.core.connection.RedisConnection;
import com.core.operation.LuaScript;
import com.core.operation.LuaScriptForReading;
import com.core.support.AggQueryBindingHandler;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryDslCursorItemWriterConfig {


    @Autowired
    private RedisConnection redisConnection;

    @Autowired
    private AggQueryBindingHandler handler;

    @Autowired
    private LuaScript luaScript;




    @Bean
    @StepScope
    public QueryDslCursorItemWriter itemWriter() {
        return new QueryDslCursorItemWriter(redisConnection, handler, luaScript);
    }


}
