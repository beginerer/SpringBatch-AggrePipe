package com.core.operation;

import io.lettuce.core.ScriptOutputType;

import java.util.Arrays;


public class DigestDigestLuaScript<T,V> implements DigestLuaOperation<T,V> {


    private final String name;

    private final String digestScript;

    private final String script;

    private final AggregateOutputType aggregateOutputType;

    private final T[] keys;

    private final V[] args;

    private final boolean safetyMode;



    public DigestDigestLuaScript(LuaScript<T,V> spec, String digestScript) {
        if(digestScript == null || digestScript.isEmpty())
            throw new IllegalArgumentException("[ERROR] digestScript is null or empty");
        this.name = spec.getName();
        this.digestScript = digestScript;
        this.script = spec.getLuaScript();
        this.aggregateOutputType = spec.getAggregateOutputType();
        this.keys = spec.getKeys();
        this.args = spec.getArgs();
        this.safetyMode = spec.isSafetyMode();
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
    public T[] getKeys() {
        return keys;
    }

    @Override
    public V[] getArgs() {
        return args;
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
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
