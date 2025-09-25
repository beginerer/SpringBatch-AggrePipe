package com.core.batch;

import com.core.ChunkUpdatePayload;
import com.core.RedisWriteResultSet;
import com.core.connection.RedisConnection;
import com.core.operation.LuaOperation;
import com.core.support.AggQueryBindingHandler;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.support.AbstractItemStreamItemWriter;


public class QueryDslCursorItemWriter extends AbstractItemStreamItemWriter<AggQueryBindingHandler.Key> {


    private RedisConnection redisConnection;

    private AggQueryBindingHandler handler;

    private LuaOperation<String,String, ChunkUpdatePayload> luaScript;

    public QueryDslCursorItemWriter(RedisConnection redisConnection, AggQueryBindingHandler handler, LuaOperation<String, String, ChunkUpdatePayload> luaScript) {
        this.redisConnection = redisConnection;
        this.handler = handler;
        this.luaScript = luaScript;
    }

    @Override
    public void write(Chunk<? extends AggQueryBindingHandler.Key> keys) throws Exception {

        for (AggQueryBindingHandler.Key key : keys) {
            ChunkUpdatePayload payload = handler.buildPayload(key.getSERIAL_NUMBER(), key.getToken());

            RedisWriteResultSet resultSet = redisConnection.eval(luaScript, payload);
            System.out.println(resultSet);
            handler.flushPayLoad(resultSet);

            if(!resultSet.isSuccess())
                throw new IllegalStateException("ERadfs");
        }

    }
}
