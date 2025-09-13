package com.core;

import java.util.List;

public class RedisResultSet {



    private String scriptSerialNumber;

    private List<Chunk> data;

    private boolean success;



    public RedisResultSet(ChunkUpdatePayload payload, boolean success) {
        this.scriptSerialNumber = payload.getScriptSerialNumber();
        this.data = payload.getData();
        this.success = success;
    }


    public String getScriptSerialNumber() {
        return scriptSerialNumber;
    }

    public List<Chunk> getData() {
        return data;
    }

    public boolean isSuccess() {
        return success;
    }
}
