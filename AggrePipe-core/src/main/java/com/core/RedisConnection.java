package com.core;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * This class is not thread-safe.
 */
public class RedisConnection {

    private final StatefulRedisConnection<String,String> connection;

    private final StatefulRedisConnection<String,String> commonConnectionBus;

    private final RedisAsyncCommands<String,String> async;

    private boolean inTx;

    public final static TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

    public final static int DEFAULT_TIME_VALUE = 500;

    private final Logger logger = LoggerFactory.getLogger(RedisConnection.class);



    public RedisConnection(StatefulRedisConnection<String, String> connection, RedisAsyncCommands<String, String> async,
                           StatefulRedisConnection<String, String> commonConnectionBus) {
        this.connection = connection;
        this.commonConnectionBus = commonConnectionBus;
        this.async = async;
        this.inTx = false;
    }

    /**
     * Executes a MULTI/EXEC transaction in pipelined mode.
     *
     * <p><Strong>Warnings:</Strong></p>
     * <p>- Not thread-safe: use this instance from a single thread only.</p>
     * <p>- Assumes autoFlushCommands=false; do not change it inside {@code commands}.</p>
     * <p>- Do NOT call MULTI/EXEC/DISCARD/WATCH inside {@code commands}; queue only regular ops.</p>
     * <p>- On failure (timeout/network/server error), this connection is closed and must not be reused.</p>
     */
    public CompletableFuture<TransactionResult> transactionPipeline(Consumer<RedisAsyncCommands<String,String>> commands) {

        final var conn = connection;

        if(commands == null)
            throw new IllegalArgumentException("[ERROR] commands is null");
        if(conn == null || !conn.isOpen())
            throw new ConnectionClosedException("[ERROR] connection is closed");
        if(inTx)
            throw new IllegalStateException("[ERROR] connection is already in transaction");

        try {
            inTx = true;
            async.multi();
            commands.accept(async);
            RedisFuture<TransactionResult> exec = async.exec();
            conn.flushCommands();

            return exec.toCompletableFuture()
                    .orTimeout(DEFAULT_TIME_VALUE,DEFAULT_TIME_UNIT)
                    .whenComplete((r, ex) -> {
                        if(ex != null) {
                            try{
                                conn.close();
                            }
                            catch (Exception ignore) {}
                        }
                        inTx = false;
                    });
        }catch (RuntimeException e) {
            try {
                async.discard();
                conn.flushCommands();
            }catch (Exception ignore) {
                logger.warn("[ERROR] Failed to dispatch transaction commands",ignore);
            }finally {
                inTx = false;
            }
            throw new TransactionDispatchException("[ERROR] Failed to dispatch transaction commands",e);
        }
    }

    public CompletableFuture<String> readByKey(String key) {
        if(key == null || key.isEmpty())
            throw  new IllegalArgumentException("[ERROR] key is null");

        final var conn = this.commonConnectionBus;

        if(conn == null || !conn.isOpen())
            throw new ConnectionPoolClosedException("Connection pool/common connection is closed");

        RedisAsyncCommands<String, String> async = conn.async();
        return async.get(key).toCompletableFuture().orTimeout(DEFAULT_TIME_VALUE, DEFAULT_TIME_UNIT);
    }


    public void releaseConnection() {
        boolean mustClose = false;
        try {
            if(inTx) {
                var discard = async.discard();
                connection.flushCommands();
                String response = discard.toCompletableFuture().get(DEFAULT_TIME_VALUE,DEFAULT_TIME_UNIT);
                if(!"OK".equalsIgnoreCase(response))
                    mustClose = true;
            }
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            mustClose = true;
        } catch (ExecutionException e) {
            mustClose = true;
        }catch (TimeoutException e) {
            mustClose = true;
        }finally {
            if(connection.isOpen() && mustClose) {
                connection.close();
            }
            inTx = false;
        }
    }

    public StatefulRedisConnection<String, String> getConnection() {
        if(!connection.isOpen())
            throw new ConnectionClosedException("[ERROR] Connection is closed");
        return connection;
    }
}
