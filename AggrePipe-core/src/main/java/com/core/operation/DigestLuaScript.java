package com.core.operation;

import com.core.ChunkUpdatePayload;
import com.core.Jackson;
import io.lettuce.core.ScriptOutputType;



/**
 * <h3>Must match the Spring Batch job lifecycle.</h3>
 * <p>key : KEYS[1]=idempKey</p>
 * <p>argv: ARGV[1]= ttl, ARGV[2]=payLoad</p>
 * */
public class DigestLuaScript implements DigestLuaOperation<String, String, ChunkUpdatePayload> {


    private final String name;

    private final String SERIAL_NUMBER;

    private final String digestScript;

    private final String script;

    private final String[] keys;

    private final int ttl;

    private final ScriptOutputType scriptOutputType;

    public static final int DEFAULT_TTL = 86400;



    public DigestLuaScript(LuaScript spec, String digestScript) {
        if(digestScript == null || digestScript.isEmpty())
            throw new IllegalArgumentException("[ERROR] digestScript is null or empty");
        this.name = spec.getName();
        this.SERIAL_NUMBER = spec.getSerialNumber();
        this.digestScript = digestScript;
        this.script = spec.getLuaScript();
        this.keys = spec.getKeys();
        this.ttl = spec.getTtl();
        this.scriptOutputType = spec.getScriptOutputType();
    }


    @Override
    public String[] inputData(ChunkUpdatePayload payload) {
        if(!payload.getScriptSerialNumber().equals(SERIAL_NUMBER))
            throw new IllegalArgumentException("[ERROR] Unsupported payLoad. required=%s current=%s".
                    formatted(SERIAL_NUMBER, payload.getScriptSerialNumber()));

        String argv = Jackson.convetToString(payload);

        return new String[]{String.valueOf(ttl), argv};
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
    public String getDigestScript() {
        return digestScript;
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
        return ttl;
    }

}
