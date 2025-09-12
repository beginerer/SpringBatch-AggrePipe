package com.core.operation;

import com.core.annotaion.ChunkUpdatePayload;
import com.core.annotaion.Jackson;
import io.lettuce.core.ScriptOutputType;

import java.util.Arrays;
import java.util.Objects;


/**
 * <h3>Must match the Spring Batch job lifecycle.</h3>
 * <p>key : KEYS[1]=idempKey</p>
 * <p>argv: ARGV[1]= ttl, ARGV[2]=payLoad</p>
 * */
public class LuaScript implements LuaOperation<String, String> {


    private final String name;

    private final String SERIAL_NUMBER;

    private final String script;

    private final String[] keys;

    private final int ttl;

    private final ScriptOutputType scriptOutputType;

    private final boolean strictMode;

    public static final int DEFAULT_TTL = 86400;





    public LuaScript(String name, String SERIAL_NUMBER, String script, String idemKey, int ttl, boolean strictMode) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.SERIAL_NUMBER = Objects.requireNonNull(SERIAL_NUMBER);
        this.script = Objects.requireNonNull(script, "script must not be null");
        Objects.requireNonNull(idemKey,"setKey must not be null");
        this.scriptOutputType = ScriptOutputType.STATUS;
        this.keys = new String[]{idemKey};
        this.strictMode = strictMode;
        this.ttl = ttl;
    }





    @Override
    public String[] inputData(ChunkUpdatePayload payload) {
        if(!payload.getScriptSerialNumber().equals(SERIAL_NUMBER))
            throw new IllegalArgumentException("[ERROR] Unsupported payLoad. required=%s current=%s".
                    formatted(SERIAL_NUMBER, payload.getScriptSerialNumber()));


        String argv = Jackson.convetToString(payload);

        return new String[]{String.valueOf(ttl), argv};
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSerialNumber() {
        return SERIAL_NUMBER;
    }

    @Override
    public String getLuaScript() {
        return script;
    }


    @Override
    public ScriptOutputType getScriptOutputType() {
        return scriptOutputType;
    }

    @Override
    public String[] getKeys() {
        return keys;
    }

    @Override
    public int getTtl() {
        return ttl;
    }

    @Override
    public boolean isStrictMode() {
        return strictMode;
    }







    @Override
    public String toString() {
        return "LuaScript{" +
                "name='" + name + '\'' +
                ", script='" + script + '\'' +
                ", AggregateOutputTYpe=" + aggregateOutputType +
                ", scriptOutputType=" + aggregateOutputType.getScriptOutputType() +
                ", keys=" + Arrays.toString(keys) +
                '}';
    }

}
