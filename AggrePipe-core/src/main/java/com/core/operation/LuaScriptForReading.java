package com.core.operation;

import com.core.Chunk;
import com.core.ChunkUpdatePayload;
import io.lettuce.core.ScriptOutputType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * <p>argv: ARGV[1]= Payload</p>
 * */
public class LuaScriptForReading implements LuaOperation<String,String, ChunkUpdatePayload> {


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
    public String[] inputData(ChunkUpdatePayload payload) {
        String scriptSerialNumber = payload.getScriptSerialNumber();
        if(!SERIAL_NUMBER.equals(scriptSerialNumber))
            throw new IllegalArgumentException("[ERROR] serialNumber is different. expected=%s current=%s".
                    formatted(SERIAL_NUMBER, scriptSerialNumber));

        Set<String> groupByKeys = new HashSet<>();

        for (Chunk chunk : payload.getData()) {
            Set<String> keys = chunk.getItems().keySet();
            groupByKeys.addAll(keys);
        }
        return groupByKeys.toArray(new String[0]);
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
