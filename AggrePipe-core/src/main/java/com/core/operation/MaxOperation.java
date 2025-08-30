package com.core.operation;

import io.lettuce.core.ScriptOutputType;

public class MaxOperation implements LuaOperation<String,String> {


    private final String[] keys;

    private final String[] values;

    public MaxOperation(String[] keys, String[] values) {
        this.keys = keys;
        this.values = values;
    }

    @Override
    public String getLuaScript() {
        return "";
    }

    @Override
    public ScriptOutputType getScriptOutputType() {
        return ScriptOutputType.VALUE;
    }

    @Override
    public String[] getKeys() {
        return keys;
    }

    @Override
    public String[] getValues() {
        return values;
    }
}
