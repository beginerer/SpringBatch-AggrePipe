package com.core.example;

import com.core.ChunkUpdatePayload;
import com.core.RedisWriteResultSet;
import com.core.config.TimeoutConfig;
import com.core.connection.RedisConnection;
import com.core.operation.LuaOperation;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.concurrent.ConcurrentLinkedQueue;

public class RedisConnectionProxy extends RedisConnection {

    private ConcurrentLinkedQueue<RedisWriteResultSet> list = new ConcurrentLinkedQueue<>();



    public RedisConnectionProxy(StatefulRedisConnection<String, String> connection, RedisAsyncCommands<String, String> async, TimeoutConfig timeoutConfig) {
        super(connection, async, timeoutConfig);
    }

    @Override
    public RedisWriteResultSet eval(LuaOperation<String, String, ChunkUpdatePayload> op, ChunkUpdatePayload payload) {
        RedisWriteResultSet resultSet = super.eval(op, payload);
        list.add(resultSet);
        return resultSet;
    }

    public ConcurrentLinkedQueue<RedisWriteResultSet> getList() {
        return list;
    }
}
