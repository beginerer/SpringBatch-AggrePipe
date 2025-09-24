package com.core.support;

import com.core.*;
import com.core.operation.Operation;

import java.util.*;


/**
 * <P>1.Build</P>
 * <P>2.Record</P>*/
public class ReadQueryBindingHandler {


    private final AggQueryRegistry registry;


    public ReadQueryBindingHandler(AggQueryRegistry registry) {
        this.registry = registry;
    }


    public ChunkReadPayload buildPayload(String serialNumber, List<? extends Object> readQueries) {

        if(serialNumber==null || serialNumber.isBlank())
            throw new IllegalArgumentException("[ERROR] serialNumber is null");

        Map<String, Object> map = new HashMap<>();
        Set<String> data = new HashSet<>();


        for (Object readQuery : readQueries) {
            String groupByKey = registry.getReadGroupByKeys(serialNumber, readQuery);
            data.add(groupByKey);
            map.putIfAbsent(groupByKey, readQuery);
        }

        return new ChunkReadPayload(serialNumber, map,List.copyOf(data));
    }




    public <T> List<T> recordValue(RedisReadResultSet resultSet, Class<T> type) {
        List<T> out = new ArrayList<>();
        List<RedisReadResultSet.Data> data = resultSet.getData();

        RedisReadResultSet.Data base = data.get(0);
        Class<?> queryClass = base.getQueryDto().getClass();

        ReadQueryMetadata metadata = registry.getReadQueryMetadata(base.getQueryDto());

        try {
            for (var datum : data) {
                Map<QueryKey, String> valueMap = new HashMap<>();
                Object queryDto = datum.getQueryDto();

                Map<String, String> redisResult = resolveHGetAll(datum.getData());

                for (ReadItemSpec item : metadata.getItems()) {
                    String fieldName = item.getFieldName();
                    String redisFieldName = Operation.resolveFieldName(item.getOp(), item.getTargetFieldName());
                    String value = redisResult.get(redisFieldName);

                    if(value == null)
                        throw new IllegalStateException("[ERROR] %s filed is not included at Redis".formatted(redisFieldName));


                    QueryKey queryKey = new QueryKey(queryClass, fieldName, item.getValueType());
                    valueMap.putIfAbsent(queryKey, value);
                }

                T obj = (T) registry.injectValue(queryDto, valueMap);
                out.add(obj);
            }
        }catch (ClassCastException e) {
            throw new IllegalStateException("[ERROR] queryDto is not instance of %s class".
                    formatted(type));
        }
        return out;
    }


    private Map<String, String> resolveHGetAll(List<String> data) {
        Map<String, String> map = new HashMap<>();

        int size = data.size();

        if(size%2 != 0)
            throw new IllegalStateException("[ERROR] data size is odd");

        for(int i=0; i<data.size(); i+=2) {
            String key = data.get(i);
            String value = data.get(i + 1);
            map.putIfAbsent(key,value);
        }

        return map;
    }








}
