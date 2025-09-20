package com.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;
import java.util.Map;

public class Jackson {

    public static final ObjectMapper DEFAULT = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();



    public static String convetToString(Object obj) {
        try {

            return DEFAULT.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, List<String>> resolveJsonString(String json) {
        try {
            return DEFAULT.readValue(json, new TypeReference<Map<String, List< String>>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
