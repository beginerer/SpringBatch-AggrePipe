package com.core;


import com.core.config.TimeoutConfig;
import com.core.exception.LuaScriptInterruptedException;
import com.core.exception.LuaScriptNonRetryableException;
import com.core.exception.LuaScriptTimeoutException;
import com.core.operation.*;
import com.core.exception.ConnectionClosedException;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;




/**
 * <p>This class is not thread-safe.</p>
 * <p>auto flush is true.</p>
 */
public class RedisConnection  {


    private final StatefulRedisConnection<String, String> connection;

    private final RedisAsyncCommands<String, String> async;

    public final TimeUnit timeUnit;

    public final long timeAmount;

    private final Logger logger = LoggerFactory.getLogger(RedisConnection.class);






    public RedisConnection(StatefulRedisConnection<String, String> connection, RedisAsyncCommands<String, String> async, TimeoutConfig timeoutConfig) {
        this.connection = connection;
        this.async = async;
        this.timeUnit = timeoutConfig.getTimeUnit();
        this.timeAmount = timeoutConfig.getAmount();
    }





    public RedisResultSet eval(LuaOperation<String, String> op, ChunkUpdatePayload payload) {

        final var conn = connection;

        if(conn == null || !conn.isOpen())
            throw new ConnectionClosedException("[ERROR] connection is closed");

        if(op == null)
            throw new IllegalArgumentException("[ERROR] LuaOperation is null");

        try {
            String[] argv = op.inputData(payload);
            var response = async.eval(op.getLuaScript(), op.getScriptOutputType(), op.getKeys(), argv).get(timeAmount, timeUnit);

            if(response == null)
                throw new LuaScriptNonRetryableException("[ERROR] result is null");

            if (response.equals("OK"))
                return new RedisResultSet(payload,true);
            else
                return new RedisResultSet(payload,false);


        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LuaScriptInterruptedException("[ERROR] SCRIPT EVAL interrupted (name=%s)".formatted(op.getName()), e);
        } catch (ExecutionException e) {
            throw new LuaScriptNonRetryableException("[ERROR] SCRIPT EVAL failed (name=%s)".formatted(op.getName()), e);
        } catch (TimeoutException e) {
            throw new LuaScriptTimeoutException("[ERROR] SCRIPT EVAL timed out (name =%s, timeout=%d %s)".
                    formatted(op.getName(), timeAmount, timeUnit), e);
        }
    }


    public RedisResultSet evalsha(DigestLuaOperation<String, String> op, ChunkUpdatePayload payload) {

        final var conn = connection;

        if(conn == null || !conn.isOpen())
            throw new ConnectionClosedException("[ERROR] connection is closed");

        if(op == null)
            throw new IllegalArgumentException("[ERROR] LuaOperation is null");

        try {
            String[] argv = op.inputData(payload);
            var response = async.evalsha(op.getDigestScript(), op.getScriptOutputType(), op.getKeys(), argv).get(timeAmount, timeUnit);

            if(response == null)
                throw new LuaScriptNonRetryableException("[ERROR] result is null");

            if (response.equals("OK"))
                return new RedisResultSet(payload,true);
            else
                return new RedisResultSet(payload,false);


        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LuaScriptInterruptedException("[ERROR] SCRIPT EVAL interrupted (name=%s)".formatted(op.getName()), e);
        } catch (ExecutionException e) {
            throw new LuaScriptNonRetryableException("[ERROR] SCRIPT EVAL failed (name=%s)".formatted(op.getName()), e);
        } catch (TimeoutException e) {
            throw new LuaScriptTimeoutException("[ERROR] SCRIPT EVAL timed out (name =%s, timeout=%d %s)".
                    formatted(op.getName(), timeAmount, timeUnit), e);
        }
    }






    /**
     * Load the specified Lua script into the script cache.
     * */
    public DigestLuaScript loadScript(LuaScript spec) {

        final var conn = connection;

        if(conn ==null || !conn.isOpen())
            throw new ConnectionClosedException("[ERROR] connection is closed");

        if(spec == null)
            throw new IllegalArgumentException("[ERROR] luaOperation must not be null");

        try {
            String sha = async.scriptLoad(spec.getLuaScript()).get(timeAmount, timeUnit);

            return new DigestLuaScript(spec, sha);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LuaScriptInterruptedException("[ERROR] SCRIPT load interrupted (name=%s)".
                    formatted(spec.getName()), e);
        } catch (ExecutionException e) {
            throw new LuaScriptNonRetryableException("[ERROR] SCRIPT load failed (name=%s)".
                    formatted(spec.getName()));
        } catch (TimeoutException e) {
            throw new LuaScriptTimeoutException("[ERROR] SCRIPT load timed out (name=%s, timed=%d %s)".
                    formatted(spec.getName(), timeAmount, timeUnit), e);
        }
    }



    /**
     * Create a SHA1 digest from a Lua script
     * */
    public DigestLuaScript digest(LuaScript spec) {

        if(spec == null)
            throw new IllegalArgumentException("[ERROR] spec must not be null");

        String sha = async.digest(spec.getLuaScript());

        return new DigestLuaScript(spec,sha);
    }



    public void releaseConnection() {
        final var conn = connection;
        boolean mustClose = false;

        if(conn == null || !conn.isOpen())
            return;

        try {
            if(conn.isMulti()) {
                String reply = async.discard().get(timeAmount, timeUnit);
                if(!"OK".equalsIgnoreCase(reply))
                    mustClose =true;
            }
        } catch (ExecutionException | TimeoutException e) {
            mustClose = true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            mustClose = true;
        } finally {
            if(mustClose) {
                try {
                    conn.close();
                }catch (Exception ignore) {
                    logger.warn("[ERROR] connection close failed", ignore);
                };
            }
        }
    }


    public void flushDb() {
        final var conn = connection;

        if(conn == null || !conn.isOpen())
            throw new ConnectionClosedException("[ERROR] connection is closed");

        try {
            async.flushdb().get(timeAmount, timeUnit);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }



    public StatefulRedisConnection<String, String> getConnection() {
        if(!connection.isOpen())
            throw new ConnectionClosedException("[ERROR] Connection is closed");
        return connection;
    }

}
