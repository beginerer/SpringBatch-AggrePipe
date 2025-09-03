package com.core.operation;

import io.lettuce.core.ScriptOutputType;

import java.util.Arrays;
import java.util.Objects;


public class DigestDigestLuaScript implements DigestLuaOperation<String, String> {


    private final String name;

    private final int[] opIndex;

    private final String digestScript;

    private final String script;

    private final AggregateOutputType aggregateOutputType;

    private final String[] keys;

    private final int ttl;

    private final boolean safetyMode;



    public DigestDigestLuaScript(LuaScript spec, String digestScript) {
        if(digestScript == null || digestScript.isEmpty())
            throw new IllegalArgumentException("[ERROR] digestScript is null or empty");
        this.name = spec.getName();
        this.opIndex = spec.opIndex();
        this.digestScript = digestScript;
        this.script = spec.getLuaScript();
        this.aggregateOutputType = spec.getAggregateOutputType();
        this.keys = spec.getKeys();
        this.ttl = spec.getTtl();
        this.safetyMode = spec.isSafetyMode();
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
    public String getDigestScript() {
        return digestScript;
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
        return "DigestLuaScript{" +
                "name='" + name + '\'' +
                ", script='" + script + '\'' +
                ", AggregateOutputTYpe=" + aggregateOutputType +
                ", scriptOutputType=" + aggregateOutputType.getScriptOutputType() +
                ", keys=" + Arrays.toString(keys) +
                '}';
    }
}
