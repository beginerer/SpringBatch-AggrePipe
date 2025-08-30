package com.core.operation;

import io.lettuce.core.ScriptOutputType;

public interface LuaOperation <T, V> {


    String getLuaScript();

    ScriptOutputType getScriptOutputType();

    T[] getKeys();

    V[] getValues();
}
