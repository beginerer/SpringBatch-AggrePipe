package com.core;

import java.util.List;
import java.util.Map;


public class Chunk {


    private Map<String, List<ItemUnit>> items;

    private Map<String, Long> counts;

    // for Idempotency
    private String token;



    public Chunk(String token, Map<String, List<ItemUnit>> items, Map<String, Long> counts)  {
        this.items = items;
        this.token = token;
        this.counts = counts;
    }



    public Map<String, List<ItemUnit>> getItems() {
        return items;
    }

    public Map<String, Long> getCounts() {
        return counts;
    }

    public String getToken() {
        return token;
    }

}
