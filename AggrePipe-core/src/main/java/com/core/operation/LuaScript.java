package com.core.operation;

import io.lettuce.core.ScriptOutputType;

import java.util.Arrays;
import java.util.Objects;


/**
 * <p>key : KEYS[1]=idemtKey, KEYS[2]=hKey</p>
 * <p>argv: ARGV[1]=requestId, ARGV[2]=value, ARGV[3]=ttl</p>
 * */
public class LuaScript implements LuaOperation<String, String> {


    private final String name;

    private final String script;

    private final AggregateOutputType aggregateOutputType;

    private final String[] keys;

    private final int ttl;

    private final boolean safetyMode;

    public static final int DEFAULT_TTL = 86400;

    private int[] opIndex;



    public LuaScript(String name, String script, int[] opIndex, String idemKey, String hKey, int ttl, boolean safetyMode, AggregateOutputType outputType) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.script = Objects.requireNonNull(script, "script must not be null");
        this.aggregateOutputType = outputType;
        Objects.requireNonNull(idemKey,"setKey must not be null");
        Objects.requireNonNull(hKey, "numKey must not be null");
        this.keys = new String[]{idemKey, hKey};
        this.safetyMode = safetyMode;
        this.ttl = ttl;
        this.opIndex = Arrays.copyOf(opIndex, opIndex.length);
    }



    @Override
    public String[] inputData(String... data) {
        if (data == null || data.length != 2) {
            throw new IllegalArgumentException(
                    "[ERROR] ARGV expects exactly 2 elements: [requestId, value], but got " + Arrays.toString(data)
            );
        }
        String requestId = Objects.requireNonNull(data[0], "requestId must not be null");
        String value = Objects.requireNonNull(data[1], "value must not be null");
        return new String[]{requestId, value, String.valueOf(ttl)};
    }


    @Override
    public int[] opIndex() {
        return opIndex;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLuaScript() {
        return script;
    }

    @Override
    public AggregateOutputType getAggregateOutputType() {
        return aggregateOutputType;
    }

    @Override
    public ScriptOutputType getScriptOutputType() {
        return aggregateOutputType.getScriptOutputType();
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
    public boolean isSafetyMode() {
        return safetyMode;
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
