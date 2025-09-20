package com.core.operation;

import com.core.ChunkUpdatePayload;
import io.lettuce.core.ScriptOutputType;

public interface LuaOperation <T, V, U> {


    V[] inputData(U payload);

    String getName();

    String getSerialNumber();

    String getLuaScript();

    ScriptOutputType getScriptOutputType();

    T[] getKeys();

    int getTtl();

}
