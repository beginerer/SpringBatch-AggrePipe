package com.core.operation;

import io.lettuce.core.ScriptOutputType;

import java.util.Arrays;
import java.util.Objects;


public class LuaScript<T,V> implements LuaOperation<T,V> {


    private final String name;

    private final String script;

    private final AggregateOutputType aggregateOutputType;

    private final T[] keys;

    private final V[] args;

    private final boolean safetyMode;




    public LuaScript(String name, String script, AggregateOutputType outputType, T[] keys, V[] args, boolean safetyMode) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.script = Objects.requireNonNull(script, "script must not be null");
        this.aggregateOutputType = Objects.requireNonNull(outputType,"aggregateOutputType must not be null");
        Objects.requireNonNull(keys, "keys must not be null");
        if(keys.length == 0)
            throw new IllegalArgumentException("keys must be included");
        this.keys = Arrays.copyOf(keys,keys.length);
        this.args = args != null ? Arrays.copyOf(args, args.length) : (V[]) new Object[0];
        this.safetyMode = safetyMode;
    }


    public static LuaScript<String, String> max() {

        String name = "MaxOperation";
        String script = "script";
        AggregateOutputType type = AggregateOutputType.LONG;
        String[] keys = new String[3];
        String[] args = new String[3];
        boolean safetyMode = false;

        return new LuaScript<>(name,script,type,keys,args,safetyMode);
    }

    public static LuaScript<String, String> max(AggregateOutputType type, boolean safetyMode) {
        if(type == null)
            throw new IllegalArgumentException("[ERROR] AggregateOutputType is null");
        String name = "MaxOperation";
        String script = "script";
        String[] keys = new String[3];
        String[] args = new String[3];

        return new LuaScript<>(name,script,type,keys,args,safetyMode);
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
        return "LuaScript{" +
                "name='" + name + '\'' +
                ", script='" + script + '\'' +
                ", AggregateOutputTYpe=" + aggregateOutputType +
                ", scriptOutputType=" + aggregateOutputType.getScriptOutputType() +
                ", keys=" + Arrays.toString(keys) +
                ", args=" + Arrays.toString(args) +
                '}';
    }

}
