package com.core.operation;

import io.lettuce.core.ScriptOutputType;

public interface LuaOperation <T, V> {


    String getName();

    String getLuaScript();

    AggregateOutputType getAggregateOutputType();

    ScriptOutputType getScriptOutputType();

    T[] getKeys();

    V[] getArgs();

    boolean isSafetyMode();
}
