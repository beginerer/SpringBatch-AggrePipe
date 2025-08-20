package com.core;

import io.lettuce.core.RedisCommandInterruptedException;
import io.lettuce.core.RedisCommandTimeoutException;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;



/**
 * This class is not thread-safe.
 */
public class RedisConnection {

    public final static TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;
    public final static int DEFAULT_TIME_VALUE = 500;
    private final StatefulRedisConnection<String,String> connection;
    private final RedisAsyncCommands<String,String> async;
    private final Logger logger = LoggerFactory.getLogger(RedisConnection.class);
    private boolean inTx;



    public RedisConnection(StatefulRedisConnection<String, String> connection, RedisAsyncCommands<String, String> async) {
        this.connection = connection;
        this.async = async;
        this.inTx = false;
    }





    public CompletableFuture<Boolean> casWithWatch(String key, Long value, BiFunction<Long,Long,Long> f, int maxTries) {

        final var conn = connection;

        if(conn == null || !conn.isOpen())
            return CompletableFuture.failedFuture(new ConnectionClosedException("[ERROR] connection is closed"));

        if(key == null)
            return CompletableFuture.failedFuture(new IllegalArgumentException("[ERROR] key is null"));


        for (int attempts = 0; attempts < maxTries; attempts++) {
            boolean watched = false;
            boolean enteredMulti = false;
            boolean execSent = false;


            try {
                if (inTx)
                    return CompletableFuture.failedFuture(new IllegalStateException("[ERROR] connection is already in transaction"));


                inTx = true;
                var watchF = async.watch(key);
                var getF = async.get(key);
                conn.flushCommands();

                String watchReply = watchF.get(DEFAULT_TIME_VALUE, DEFAULT_TIME_UNIT);

                if(!"OK".equalsIgnoreCase(watchReply))
                    continue;
                watched = true;

                String cur = getF.get(DEFAULT_TIME_VALUE, DEFAULT_TIME_UNIT);

                if (cur == null)
                    return CompletableFuture.failedFuture(new KeyNotFoundException("[ERROR] key is not found. key=%s".formatted(key)));

                long newValue = f.apply(value,Long.parseLong(cur));


                async.multi();
                enteredMulti = true;
                var setF = async.set(key, String.valueOf(newValue));
                var execF = async.exec();
                conn.flushCommands();
                execSent = true;


                TransactionResult tr = null;

                try {
                    tr = execF.get(DEFAULT_TIME_VALUE,DEFAULT_TIME_UNIT);
                }catch (ExecutionException e) {
                    return CompletableFuture.failedFuture(e);
                }

                if(tr.wasDiscarded())
                    continue;


                String reply = setF.get(DEFAULT_TIME_VALUE, DEFAULT_TIME_UNIT);

                if ("OK".equalsIgnoreCase(reply))
                    return CompletableFuture.completedFuture(true);


            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return CompletableFuture.failedFuture(e);
            } catch (ExecutionException | CancellationException e) {
                return CompletableFuture.failedFuture(e);
            } catch (TimeoutException e){
                try {Thread.sleep(500);}
                catch (InterruptedException ie) { Thread.currentThread().interrupt();;}
            } finally {
                inTx = false;
                try {
                    if(!execSent) {
                        if(enteredMulti)
                            async.discard();
                        else if(watched)
                            async.unwatch();
                        conn.flushCommands();
                    }
                } catch (Exception ignore) {}
            }
        }
        return CompletableFuture.completedFuture(false);
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
