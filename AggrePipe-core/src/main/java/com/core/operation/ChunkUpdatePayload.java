package com.core.operation;

import java.util.*;


public class ChunkUpdatePayload {


    private String name;

    private String token; // 멱등성을 위한 chunk당 value

    private Map<String, Item> dataSetAsLong;

    private int ttl;

    private boolean strictValidation;


    public ChunkUpdatePayload(String token, int ttl, boolean strictValidation) {
        this.token = token;
        this.dataSetAsLong = new HashMap<>();
        this.ttl = ttl;
        this.strictValidation = strictValidation;
    }


    public boolean add(Item item) {
        if(strictValidation) {
            Objects.requireNonNull(item,"data is must not be null");
            Objects.requireNonNull(item.getKey(),"key is must not be null");
            Objects.requireNonNull(item.getValue(), "value is must not be null");
        }
        String key = item.getKey();
        if(dataSetAsLong.containsKey(key)) {

        }else {
            dataSetAsLong.put(key, item);
        }
    }

    private void preAggregation(Item item) {
        Operation op = item.getOperation();
        Item cur = dataSetAsLong.get(item.getKey());

        if(Operation.SUM == op) {

        }

    }
}
