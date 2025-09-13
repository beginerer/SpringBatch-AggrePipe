package com.core;


import java.util.List;

public class ChunkUpdatePayload {


    private String scriptSerialNumber;

    private List<Chunk> data;



    public ChunkUpdatePayload(String scriptSerialNumber, List<Chunk> data) {
        this.scriptSerialNumber = scriptSerialNumber;
        this.data = data;
    }




    public String getScriptSerialNumber() {
        return scriptSerialNumber;
    }


    public List<Chunk> getData() {
        return data;
    }
}
