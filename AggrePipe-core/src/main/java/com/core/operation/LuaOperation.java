package com.core.operation;

import com.core.annotaion.ChunkUpdatePayload;
import io.lettuce.core.ScriptOutputType;

public interface LuaOperation <T, V> {


    V[] inputData(ChunkUpdatePayload payload);

    String getName();

    String getSerialNumber();

    String getLuaScript();

    ScriptOutputType getScriptOutputType();

    T[] getKeys();

    int getTtl();

    boolean isStrictMode();
}
