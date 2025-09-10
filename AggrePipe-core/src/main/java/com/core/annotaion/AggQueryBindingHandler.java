package com.core.annotaion;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AggQueryBindingHandler{


    private final List<String> bases;
    private final Map<String, List<AggMeta>> map;



    public AggQueryBindingHandler(List<String> bases) {
        this.bases = bases;
        map = new HashMap<>();
    }


}
