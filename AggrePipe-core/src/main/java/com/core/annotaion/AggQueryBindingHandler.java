package com.core.annotaion;

import com.core.operation.ValueType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <P>1. Store</P>
 * <P>2. build</P>
 * <P>3. flush</P>
 * */
public class AggQueryBindingHandler {


    private final AggQueryRegistry registry;

    private final ConcurrentHashMap<Key, Chunk> bufferMap;




    public AggQueryBindingHandler(AggQueryRegistry registry) {
        this.registry = registry;
        this.bufferMap = new ConcurrentHashMap<>();
    }



    public ChunkUpdatePayload buildPayload(String serialNumber, String token) {
        Key key = new Key(Objects.requireNonNull(serialNumber), Objects.requireNonNull(token));
        Chunk chunk = bufferMap.get(key);
        if(chunk == null)
            throw new IllegalStateException("[ERROR] key is not found in bufferMap. SERIAL_NUMBER=%s, token=%s".
                    formatted(serialNumber, token));

        return new ChunkUpdatePayload(serialNumber, List.of(chunk));
    }


    public ChunkUpdatePayload buildPayload(String serialNumber, List<String> tokens) {
        Objects.requireNonNull(serialNumber);
        if(tokens == null || tokens.isEmpty())
            throw new IllegalArgumentException("[ERROR] token is empty");

        List<Chunk> chunks = new ArrayList<>();

        for (String token : tokens) {
            Key key = new Key(serialNumber, token);
            Chunk chunk = bufferMap.get(key);
            if(chunk == null)
                throw new IllegalStateException("[ERROR] key is not found in bufferMap. SERIAL_NUMBER=%s, token=%s".
                        formatted(serialNumber, token));
            chunks.add(chunk);
        }
        return new ChunkUpdatePayload(serialNumber, Collections.unmodifiableList(chunks));
    }


    public void flushPayLoad(ChunkUpdatePayload payload) {
        String serialNumber = payload.getScriptSerialNumber();
        List<Chunk> chunks = payload.getData();

        for(Chunk chunk : chunks) {
            String token = chunk.getToken();
            Key key = new Key(serialNumber, token);

            Chunk result = bufferMap.remove(key);
            if(result == null)
                throw new IllegalStateException("[ERROR] key is not found in bufferMap. SERIAL_NUMBER=%s, token=%s".
                        formatted(serialNumber, token));
        }
    }


    public void store(String SERIAL_NUMBER, String token, List<Object> queryDtos) {
        Chunk chunk = convert(Objects.requireNonNull(SERIAL_NUMBER), Objects.requireNonNull(token), Objects.requireNonNull(queryDtos));
        Key key = new Key(SERIAL_NUMBER, token);

        Chunk result = bufferMap.putIfAbsent(key, chunk);
        if(result!=null)
            throw new IllegalStateException("[ERROR] key is already exist. key=%s".formatted(key));
    }




    // SERIAL_NUMBER = 배치 고유 넘버, token = chunk token for Idempotency
    private Chunk convert(String SERIAL_NUMBER, String token, List<Object> queryDtos) {
        Map<String, List<ItemUnit>> map = new HashMap<>();

        for (Object queryDto : queryDtos) {
            String groupByKeys = registry.getGroupByKeys(Objects.requireNonNull(SERIAL_NUMBER), queryDto);
            List<ItemUnit> itemUnits = registry.extractValue(queryDto);

            if(map.containsKey(groupByKeys)) {
                List<ItemUnit> cur = map.get(groupByKeys);
                cur.addAll(itemUnits);
            }else {
                map.put(groupByKeys, itemUnits);
            }
        }
        return new Chunk(token, map);
    }


    private void preCalculate(List<ItemUnit> itemUnits) {

        Map<String, List<ItemUnit>> collect = itemUnits.stream().collect(Collectors.groupingBy(ItemUnit::getFieldName));

        for (String key : collect.keySet()) {
            List<ItemUnit> units = collect.get(key);
            ItemUnit base = units.get(0);
            ValueType valueType = base.getValueType();
            if(valueType == ValueType.LONG) {

            }else if(valueType == ValueType.DOUBLE) {

            }else
                throw new IllegalStateException("[ERROR] Unsupported ")


        }


    }


    public static class Key {

        private String SERIAL_NUMBER;

        private String token;

        public Key(String SERIAL_NUMBER, String token) {
            this.SERIAL_NUMBER = SERIAL_NUMBER;
            this.token = token;
        }

        @Override
        public boolean equals(Object object) {
            if (object == null || getClass() != object.getClass()) return false;
            Key key = (Key) object;
            return Objects.equals(SERIAL_NUMBER, key.SERIAL_NUMBER) && Objects.equals(token, key.token);
        }


        @Override
        public int hashCode() {
            return Objects.hash(SERIAL_NUMBER, token);
        }


        @Override
        public String toString() {
            return "Key{" +
                    "SERIAL_NUMBER='" + SERIAL_NUMBER + '\'' +
                    ", token='" + token + '\'' +
                    '}';
        }
    }

}
