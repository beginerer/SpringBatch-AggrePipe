package com.core;


import java.util.Map;

public class ChunkReadPayload {

    private final String scriptSerialNumber;

    private final Map<String, Object> data;


    public ChunkReadPayload(String scriptSerialNumber, Map<String, Object> data) {
        this.scriptSerialNumber = scriptSerialNumber;
        this.data = data;
    }

    public String getScriptSerialNumber() {
        return scriptSerialNumber;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
