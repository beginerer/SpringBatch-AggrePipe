package com.core.support;

import com.core.*;
import com.core.operation.Operation;
import com.core.operation.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


    private final Logger logger = LoggerFactory.getLogger(AggQueryBindingHandler.class);



    public AggQueryBindingHandler(AggQueryRegistry registry) {
        this.registry = registry;
        this.bufferMap = new ConcurrentHashMap<>();
    }


    // 1. store
    public void store(String SERIAL_NUMBER, String token, List<?> queryDtos) {
        Chunk chunk = convert(Objects.requireNonNull(SERIAL_NUMBER), Objects.requireNonNull(token), Objects.requireNonNull(queryDtos));
        Key key = new Key(SERIAL_NUMBER, token);

        Chunk result = bufferMap.putIfAbsent(key, chunk);
        logger.warn("[ERROR] key is already exist. key={}",key);
    }



    // 2. build payload
    public ChunkUpdatePayload buildPayload(String serialNumber, String token) {
        Key key = new Key(Objects.requireNonNull(serialNumber), Objects.requireNonNull(token));
        Chunk chunk = bufferMap.get(key);
        if(chunk == null)
            throw new IllegalStateException("[ERROR] key is not found in bufferMap. SERIAL_NUMBER=%s, token=%s".
                    formatted(serialNumber, token));

        return new ChunkUpdatePayload(serialNumber, List.of(chunk));
    }

    // 3. flush payload
    public void flushPayLoad(RedisWriteResultSet resultSet) {
        String serialNumber = resultSet.getScriptSerialNumber();
        List<Chunk> chunks = resultSet.getData();

        for(Chunk chunk : chunks) {
            String token = chunk.getToken();
            Key key = new Key(serialNumber, token);

            Chunk result = bufferMap.remove(key);
            if(result == null)
                logger.warn("[ERROR] key is not found in bufferMap. SERIAL_NUMBER={}, token={}",serialNumber, token);
        }
    }




    // SERIAL_NUMBER = 배치 고유 넘버, token = chunk token for Idempotency
    private Chunk convert(String serialNumber, String token, List<?> queryDtos) {
        Map<String, List<ItemUnit>> ItemMap = new HashMap<>();
        Map<String, Long> counts = new HashMap<>();

        for (Object queryDto : queryDtos) {
            String groupByKeys = registry.getAggGroupByKeys(Objects.requireNonNull(serialNumber), queryDto);

            // record count
            Long before = counts.getOrDefault(groupByKeys, 0L);
            counts.put(groupByKeys, before + 1);

            List<ItemUnit> itemUnits = registry.extractValue(queryDto);
            List<ItemUnit> preCalculatedItemUnits = preCalculate(itemUnits);

            if(ItemMap.containsKey(groupByKeys)) {
                List<ItemUnit> cur = ItemMap.get(groupByKeys);
                cur.addAll(preCalculatedItemUnits);
            }else {
                ItemMap.put(groupByKeys, preCalculatedItemUnits);
            }
        }
        return new Chunk(token, ItemMap, counts);
    }



    private List<ItemUnit> preCalculate(List<ItemUnit> itemUnits) {

        Map<String, List<ItemUnit>> collect = itemUnits.stream().collect(Collectors.groupingBy(ItemUnit::getFieldName));

        List<ItemUnit> newItemUnits = new ArrayList<>();

        for (var e : collect.values()) {

            List<ItemUnit> units = e;
            ItemUnit base = units.get(0);
            ValueType valueType = base.getValueType();


            ItemUnit.Calculation cal = null;

            if(valueType == ValueType.LONG) {
                long lv = base.getLv();
                long sum_lv = lv;
                long max_lv = lv;
                long min_lv = lv;

                for(int i=1; i<units.size(); i++) {
                    ItemUnit unit = units.get(i);
                    long unit_lv = unit.getLv();

                    sum_lv += unit_lv;
                    max_lv = Math.max(max_lv, unit_lv);
                    min_lv = Math.min(min_lv, unit_lv);
                }
                cal = new ItemUnit.Calculation(sum_lv, max_lv, min_lv, -1, -1, -1);
            }else if(valueType == ValueType.DOUBLE) {
                double dv = base.getDv();
                double sum_dv = dv;
                double max_dv = dv;
                double min_dv = dv;

                for(int i=1; i<units.size(); i++) {
                    ItemUnit unit = units.get(i);
                    double unit_dv = unit.getDv();

                    sum_dv += unit_dv;
                    max_dv = Math.max(max_dv, unit_dv);
                    min_dv = Math.min(min_dv, unit_dv);
                }
                cal = new ItemUnit.Calculation(-1, -1, -1, sum_dv, max_dv, min_dv);
            }else
                throw new IllegalStateException("[ERROR] Unsupported value type. valueType=%s".
                        formatted(valueType));


            ItemUnit combine = new ItemUnit(base.getFieldName(), base.getMask(), valueType, -1, -1, true, cal);
            newItemUnits.add(combine);
        }

        return newItemUnits;
    }

    public String createToken() {
        return UUID.randomUUID().toString();
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
