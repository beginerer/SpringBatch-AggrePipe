package com.core.annotaion;

import java.util.List;
import java.util.Map;


public class Chunk {


    private Map<String, List<ItemUnit>> items;

    // for Idempotency
    private String token;



    public Chunk(String token, Map<String, List<ItemUnit>> items) {
        this.items = items;
        this.token = token;
    }



    public Map<String, List<ItemUnit>> getItems() {
        return items;
    }


    public String getToken() {
        return token;
    }

}
