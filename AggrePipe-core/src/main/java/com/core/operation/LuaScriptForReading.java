package com.core.operation;

import com.core.ChunkReadPayload;
import io.lettuce.core.ScriptOutputType;


/**
 * <p>argv: ARGV[1]= payload</p>
 * */
public class LuaScriptForReading implements LuaOperation<String,String, ChunkReadPayload> {


    private final String name;

    private final String SERIAL_NUMBER;

    private final String script;

    private final String[] keys;

    private final ScriptOutputType scriptOutputType;



    public LuaScriptForReading(String SERIAL_NUMBER, String script, String name) {
        this.name = name;
        this.SERIAL_NUMBER = SERIAL_NUMBER;
        this.script = script;
        this.keys = new String[0];
        this.scriptOutputType = ScriptOutputType.VALUE;
    }


    @Override
    public String[] inputData(ChunkReadPayload payload) {
        if(!payload.getScriptSerialNumber().equals(SERIAL_NUMBER))
            throw new IllegalArgumentException("[ERROR] Unsupported payLoad. required=%s current=%s".
                    formatted(SERIAL_NUMBER, payload.getScriptSerialNumber()));

        return payload.getData().toArray(new String[0]);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSerialNumber() {
        return SERIAL_NUMBER;
    }

    @Override
    public String getLuaScript() {
        return script;
    }

    @Override
    public ScriptOutputType getScriptOutputType() {
        return scriptOutputType;
    }

    @Override
    public String[] getKeys() {
        return keys;
    }

    @Override
    public int getTtl() {
        return 0;
    }
}
