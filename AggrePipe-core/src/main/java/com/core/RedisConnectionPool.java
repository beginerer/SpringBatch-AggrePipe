package com.core;

import com.excpetion.ConnectionPoolClosedException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.AsyncConnectionPoolSupport;
import io.lettuce.core.support.BoundedAsyncPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;


public class RedisConnectionPool {


    private final RedisClient redisClient;

    private volatile BoundedAsyncPool<StatefulRedisConnection<String, String>> pool;

    private final TimeoutConfig timeoutConfig;

    private final AtomicBoolean open;

    private final Logger logger = LoggerFactory.getLogger(RedisConnectionPool.class);



    public RedisConnectionPool(RedisClientConfig config) {
        this.redisClient = config.getRedisClient();
        this.pool = AsyncConnectionPoolSupport.createBoundedObjectPool(() ->
                redisClient.connectAsync(config.getStringCodec(), config.getRedisURI()).thenApply(conn -> {
                    conn.setAutoFlushCommands(false);
                    return conn;
                }), config.getBoundedPoolConfig(),false);
        this.open = new AtomicBoolean(true);
        this.timeoutConfig = config.getTimeoutConfig();
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        connection.setAutoFlushCommands(true);
    }


    public CompletableFuture<RedisConnection> acquireConnection() {
        final var p = this.pool;
        if(p == null || !isOpen())
            return CompletableFuture.failedFuture(new ConnectionPoolClosedException("[ERROR] Connection pool is closed"));

        return p.acquire().handle((conn, ex) -> {
            if(ex != null)
                throw new CompletionException(new IllegalStateException("[ERROR] Failed to acquire a connection (pool may be closed)", ex));

            if(!isOpen()) {
                try {
                    p.release(conn);
                } catch (Exception ignore) {
                    logger.warn("[ERROR] Failed to release connection after the pool was closed", ignore);
                }
                throw new CompletionException(new ConnectionPoolClosedException("[ERROR] Connection pool is closed"));
            }
            return new RedisConnection(conn,conn.async(),timeoutConfig);
        });
    }


    public void releaseConnection(RedisConnection rc) {
        final var p = this.pool;

        rc.releaseConnection();

        if(p == null || !isOpen())
            return;

        try {
            p.release(rc.getConnection());
        } catch (Exception ignore) {
            logger.warn("[ERROR] Failed to release connection",ignore);
        }
    }


    /**
     * Closes this pool and releases the underlying resources.
     *
     * <p><strong>Caution:</strong> This method does not wait for borrowed
     * connections to be returned.</p>
     *
     * <p>After this method returns, subsequent {@link #acquireConnection()} calls will fail fast.</p>
     *
     * @throws ConnectionPoolClosedException if the pool is already closed
     */
    public void releaseConnectionPool() {
        final BoundedAsyncPool<StatefulRedisConnection<String, String>> p;

        synchronized (this) {
            if(!isOpen())
                throw new ConnectionPoolClosedException("[ERROR] Connection pool is closed");

            open.set(false);
            p = this.pool;
            this.pool = null;
        }
        if(p != null) {
            try{
                p.close();
            } catch (Exception ignore) {
                logger.warn("[ERROR] Failed to close pool",ignore);
            }
        }
    }


    public boolean isOpen() {
        return open.get();
    }

    public int getMaxTotal() {
        return pool.getMaxTotal();
    }


}
