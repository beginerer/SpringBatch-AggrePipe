package com.core.operation;

import io.lettuce.core.ScriptOutputType;

public interface LuaOperation <T, V> {


    V[] inputData(V... data);

    String getName();

    String getLuaScript();

    AggregateOutputType getAggregateOutputType();

    ScriptOutputType getScriptOutputType();

    T[] getKeys();

    int getTtl();

    boolean isSafetyMode();

}
