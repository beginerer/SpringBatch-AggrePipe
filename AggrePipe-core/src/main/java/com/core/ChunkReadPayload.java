package com.core;


import java.util.List;
import java.util.Map;

public class ChunkReadPayload {

    private final String scriptSerialNumber;

    private final Map<String, Object> groupKeyQueryDtoMapping;

    private final List<String> data;


    public ChunkReadPayload(String scriptSerialNumber, Map<String, Object> groupKeyQueryDtoMapping, List<String> data) {
        this.scriptSerialNumber = scriptSerialNumber;
        this.groupKeyQueryDtoMapping = groupKeyQueryDtoMapping;
        this.data = data;
    }


    public List<String> getData() {
        return data;
    }

    public String getScriptSerialNumber() {
        return scriptSerialNumber;
    }

    public Map<String, Object> getGroupKeyQueryDtoMapping() {
        return groupKeyQueryDtoMapping;
    }
}
