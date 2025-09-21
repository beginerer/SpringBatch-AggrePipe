package com.core;

import java.util.List;

public class RedisWriteResultSet {



    private String scriptSerialNumber;

    private List<Chunk> data;

    private boolean success;



    public RedisWriteResultSet(ChunkUpdatePayload payload, boolean success) {
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
