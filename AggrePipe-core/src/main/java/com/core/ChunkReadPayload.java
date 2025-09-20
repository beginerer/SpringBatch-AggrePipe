package com.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChunkReadPayload {

    private String scriptSerialNumber;

    private Map<String, List<String>> result;

    private List<String> tokens;



    public ChunkReadPayload(Map<String,List<String>> result, ChunkUpdatePayload payload) {
        this.result = result;
        this.scriptSerialNumber = payload.getScriptSerialNumber();
        this.tokens = extractToken(payload.getData());
    }


    private List<String> extractToken(List<Chunk> chunks) {
        List<String> tokens = new ArrayList<>();

        for (Chunk chunk : chunks) {
            String token = chunk.getToken();
            tokens.add(token);
        }
        return tokens;
    }


    public String getScriptSerialNumber() {
        return scriptSerialNumber;
    }

    public Map<String, List<String>> getResult() {
        return result;
    }

    public List<String> getTokens() {
        return tokens;
    }
}
