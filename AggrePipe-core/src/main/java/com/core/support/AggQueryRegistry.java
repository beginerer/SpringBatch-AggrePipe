package com.core.support;

import com.core.*;
import com.core.annotaion.GroupByKey;
import com.core.batch.QuerySpec;
import com.core.operation.ValueType;
import org.springframework.util.ClassUtils;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class AggQueryRegistry {


    // write query
    private final Map<Class<?>, AggQueryMetadata> aggQueryMetadataMap;

    private final Map<Class<?>, List<QueryKey>> aggQueryKeyData;

    private final Map<Class<?>, List<QueryKey>> aggQueryGroupByKeyData;

    private final Map<QueryKey, ItemSpec> aggKeyItemMap;

    // read query
    private final Map<Class<?>, ReadQueryMetadata> readQueryMetadataMap;

    private final Map<Class<?>, List<QueryKey>> readQueryKeyData;

    private final Map<Class<?>, List<QueryKey>> readQueryGroupByKeyData;

    private final Map<QueryKey, ReadItemSpec> readKeyItemMap;



    private final ConcurrentHashMap<QueryKey, Object> CACHE;




    public AggQueryRegistry(Map<Class<?>, AggQueryMetadata> aggQueryMetadataMap, Map<Class<?>, ReadQueryMetadata> readQueryMetadataMap) {
        this.aggQueryMetadataMap = aggQueryMetadataMap;
        this.readQueryMetadataMap = readQueryMetadataMap;
        this.aggKeyItemMap = new HashMap<>();
        this.aggQueryGroupByKeyData = new HashMap<>();
        this.aggQueryKeyData = buildAggKeyData();
        this.readKeyItemMap = new HashMap<>();
        this.readQueryGroupByKeyData = new HashMap<>();
        this.readQueryKeyData = buildReadKeyData();
        this.CACHE = buildAggCacheData();
        buildReadCacheData();
    }



    public List<ItemUnit> extractValue(Object queryDto) {
        Class<?> queryClass = queryDto.getClass();

        List<QueryKey> queryKeys = aggQueryKeyData.get(queryClass);
        if (queryKeys == null || queryKeys.isEmpty()) {
            throw new IllegalArgumentException(
                    "[ERROR] %s is not included in MetaData".formatted(queryClass.getName()));
        }

        List<ItemUnit> itemUnits = new ArrayList<>();

        for (QueryKey queryKey : queryKeys) {
            if(queryKey.getDataType() == ValueType.LONG) {
                LongAccessor longAcc = forLongWrite(queryKey);
                long value = longAcc.get(queryDto);

                ItemSpec itemSpec = aggKeyItemMap.get(queryKey);
                ItemUnit itemUnit = new ItemUnit(itemSpec.getFieldName(), itemSpec.getOp(), itemSpec.getValueType(), value, -1);
                itemUnits.add(itemUnit);

            }else if(queryKey.getDataType() == ValueType.DOUBLE) {
                DoubleAccessor doubleAcc = forDoubleWrite(queryKey);
                double value = doubleAcc.get(queryDto);

                ItemSpec itemSpec = aggKeyItemMap.get(queryKey);
                ItemUnit itemUnit = new ItemUnit(itemSpec.getFieldName(), itemSpec.getOp(), itemSpec.getValueType(), -1, value);
                itemUnits.add(itemUnit);
            }else
                throw new IllegalArgumentException("[ERROR] Unsupported data type. dataType=%s".formatted(queryKey.getDataType()));
        }

        return itemUnits;
    }


    public Object injectValue(Object queryDto, Map<QueryKey, String> valueMap){
        Class<?> queryClass = queryDto.getClass();

        for (var e : valueMap.entrySet()) {

            QueryKey key = e.getKey();
            String value = Objects.requireNonNull(e.getValue());

            if(queryClass != key.getQueryClass())
                throw new IllegalArgumentException("[ERROR] queryDto's class and QueryKey's class does not match. queryDto class=%s, queryKey class=%s".
                        formatted(queryClass, key.getQueryClass()));

            try {
                if(key.getDataType() == ValueType.LONG) {
                    long v = Long.parseLong(value);
                    LongSetter longSetter = forLongSet(key);
                    longSetter.set(queryDto, v);
                }else if(key.getDataType() == ValueType.DOUBLE){
                    double v = Double.parseDouble(value);
                    DoubleSetter doubleSetter = forDoubleSet(key);
                    doubleSetter.set(queryDto, v);
                }else
                    throw new IllegalArgumentException("[ERROR] Unsupported Data type. dataType=%s".
                            formatted(key.getDataType()));
            }catch (NumberFormatException ex) {
                throw new IllegalArgumentException("[ERROR] Cannot parse value %s for field %s".formatted(value, key.getFieldName()));
            }

        }
        return queryDto;
    }

    public ReadQueryMetadata getReadQueryMetadata(Object queryDto) {
        Class<?> queryClass = queryDto.getClass();
        ReadQueryMetadata metadata = readQueryMetadataMap.get(queryClass);

        if(metadata == null)
            throw new IllegalArgumentException("[ERROR] %s class is not include in Metadata".formatted(queryClass));

        return metadata;
    }


    public String getAggGroupByKeys(String SERIAL_NUMBER, Object queryDto) {
        Class<?> queryClass = queryDto.getClass();
        AggQueryMetadata metadata = aggQueryMetadataMap.get(queryClass);
        if(metadata == null)
            throw new IllegalArgumentException("[ERROR] %s class is not included in MetaData".formatted(queryClass));

        return generateGroupByKey(SERIAL_NUMBER, queryClass, queryDto, metadata.getGroupByKeys());
    }

    public String getReadGroupByKeys(String SERIAL_NUMBER, Object queryDto) {
        Class<?> queryClass = queryDto.getClass();
        ReadQueryMetadata metadata = readQueryMetadataMap.get(queryClass);
        if(metadata == null)
            throw new IllegalArgumentException("[ERROR] %s class is not included in MetaData".formatted(queryClass));

        return generateGroupByKey(SERIAL_NUMBER, queryClass, queryDto, metadata.getGroupByKeys());
    }




    private Map<Class<?>, List<QueryKey>> buildAggKeyData() {
        Map<Class<?>, List<QueryKey>> map = new HashMap<>();

        for (Class<?> queryClass : aggQueryMetadataMap.keySet()) {
            List<QueryKey> queryKeys = new ArrayList<>();
            List<QueryKey> groupByKeys = new ArrayList<>();
            AggQueryMetadata metadata = aggQueryMetadataMap.get(queryClass);

            for(GroupByKey key : metadata.getGroupByKeys()) {
                String field = key.field();
                ValueType vt = key.type();

                QueryKey queryKey = new QueryKey(queryClass, field, vt);
                groupByKeys.add(queryKey);
            }
            aggQueryGroupByKeyData.put(queryClass, groupByKeys);

            for (ItemSpec item : metadata.getItems()) {

                QueryKey queryKey = new QueryKey(queryClass, item.getFieldName(), item.getValueType());
                queryKeys.add(queryKey);
                aggKeyItemMap.put(queryKey, item);
            }
            map.put(queryClass, queryKeys);
        }
        return map;
    }

    private Map<Class<?>, List<QueryKey>> buildReadKeyData() {
        Map<Class<?>, List<QueryKey>> map = new HashMap<>();

        for(Class<?> queryClass : readQueryMetadataMap.keySet()) {
            List<QueryKey> queryKeys = new ArrayList<>();
            List<QueryKey> groupByKeys = new ArrayList<>();
            ReadQueryMetadata metadata = readQueryMetadataMap.get(queryClass);

            for(GroupByKey key : metadata.getGroupByKeys()) {
                String field = key.field();
                ValueType vt = key.type();

                QueryKey queryKey = new QueryKey(queryClass, field, vt);
                groupByKeys.add(queryKey);
            }
            readQueryGroupByKeyData.put(queryClass, groupByKeys);

            for (ReadItemSpec item : metadata.getItems()) {

                QueryKey queryKey = new QueryKey(queryClass, item.getFieldName(), item.getValueType());
                queryKeys.add(queryKey);
                readKeyItemMap.putIfAbsent(queryKey, item);

            }
            map.put(queryClass, queryKeys);
        }
        return map;
    }




    private ConcurrentHashMap<QueryKey, Object> buildAggCacheData() {
        ConcurrentHashMap<QueryKey,Object> cacheMap = new ConcurrentHashMap<>();

        for (Class<?> queryClass : aggQueryKeyData.keySet()) {
            for (QueryKey queryKey : aggQueryKeyData.get(queryClass)) {
                Object result = cacheMap.putIfAbsent(queryKey, createWriteLambda(queryKey.getQueryClass(), queryKey.getFieldName(), queryKey.getDataType()));

                if(result != null)
                    throw new IllegalStateException("[ERROR] duplicate key: " + queryKey);
            }
        }

        for (Class<?> queryClass : aggQueryGroupByKeyData.keySet()) {
            for (QueryKey queryKey : aggQueryGroupByKeyData.get(queryClass)) {
                Object result = cacheMap.putIfAbsent(queryKey, createWriteLambda(queryKey.getQueryClass(), queryKey.getFieldName(), queryKey.getDataType()));

                if(result != null)
                    throw new IllegalStateException("[ERROR] duplicate key: " + queryKey);
            }
        }
        return cacheMap;
    }

    private void buildReadCacheData() {

        for(Class<?> queryClass : readQueryKeyData.keySet()) {
            for (QueryKey queryKey : readQueryKeyData.get(queryClass)) {
                Object result = CACHE.putIfAbsent(queryKey, createReadLambda(queryKey.getQueryClass(), queryKey.getFieldName(), queryKey.getDataType()));

                if(result != null)
                    throw new IllegalStateException("[ERROR] duplicate key: " + queryKey);
            }
        }
        for (Class<?> queryClass : readQueryGroupByKeyData.keySet()) {
            for (QueryKey queryKey : readQueryGroupByKeyData.get(queryClass)) {
                Object result = CACHE.putIfAbsent(queryKey, createWriteLambda(queryKey.getQueryClass(), queryKey.getFieldName(), queryKey.getDataType()));

                if(result != null)
                    throw new IllegalStateException("[ERROR] duplicate key: " + queryKey);
            }
        }
    }



    private Object createWriteLambda(Class<?> queryClass, String fieldName, ValueType valueType) {
        try {

            if(valueType == ValueType.STRING) {
                MethodHandles.Lookup base = MethodHandles.lookup();
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(queryClass, base);

                MethodHandle impl = findTargetGetHandle(lookup, queryClass, fieldName);
                MethodType erased = MethodType.methodType(String.class, Object.class);
                MethodType dyn = MethodType.methodType(String.class, queryClass);

                CallSite cs = LambdaMetafactory.metafactory(lookup, "get", MethodType.methodType(StringAccessor.class), erased, impl, dyn);
                return (StringAccessor) cs.getTarget().invokeExact();

            }else {
                boolean isLong = (valueType == ValueType.LONG);
                Class<?> prim = isLong ? long.class : double.class;

                MethodHandles.Lookup base = MethodHandles.lookup();
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(queryClass, base);

                MethodHandle impl = findTargetGetHandle(lookup, queryClass, fieldName);
                MethodType erased = MethodType.methodType(prim, Object.class);
                MethodType dyn = MethodType.methodType(prim, queryClass);

                Class<?> iface = isLong ? LongAccessor.class : DoubleAccessor.class;
                CallSite cs = LambdaMetafactory.metafactory(lookup, "get", MethodType.methodType(iface), erased, impl, dyn);

                if (isLong) {
                    return (LongAccessor) cs.getTarget().invokeExact();
                } else {
                    return (DoubleAccessor) cs.getTarget().invokeExact();
                }
            }
        } catch (IllegalAccessException | LambdaConversionException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private Object createReadLambda(Class<?> queryClass, String fieldName, ValueType acceptedType) {
        try {

            boolean isLong = (acceptedType == ValueType.LONG);
            Class<?> prim = isLong ? long.class : double.class;

            MethodHandles.Lookup base = MethodHandles.lookup();
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(queryClass, base);

            MethodHandle impl = findTargetSetHandle(lookup, queryClass, fieldName, prim);
            MethodType erased = MethodType.methodType(void.class, Object.class, prim);
            MethodType dyn = MethodType.methodType(void.class, queryClass, prim);

            Class<?> iface = isLong ? LongSetter.class : DoubleSetter.class;

            CallSite cs = LambdaMetafactory.metafactory(lookup, "set", MethodType.methodType(iface), erased, impl, dyn);

            if(isLong) {
                return (LongSetter) cs.getTarget().invokeExact();

            }else {
                return (DoubleSetter) cs.getTarget().invokeExact();
            }

        } catch (IllegalAccessException | LambdaConversionException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    private MethodHandle findTargetGetHandle(MethodHandles.Lookup lookup, Class<?> queryClass, String fieldName) throws IllegalAccessException {

        if(queryClass.isRecord()) {
            for (RecordComponent rc : queryClass.getRecordComponents()) {
                if(rc.getName().equals(fieldName))
                    return lookup.unreflect(rc.getAccessor());
            }
        }

        String cap = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String methodName = "get" + cap;
        try {
            var m = queryClass.getMethod(methodName);
            return lookup.unreflect(m);

        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("[ERROR] %s class doesn't have %s field or getter Method".
                    formatted(queryClass.getName(), fieldName));
        }
    }


    private MethodHandle findTargetSetHandle(MethodHandles.Lookup lookup, Class<?> queryClass, String fieldName, Class<?> pramType) throws IllegalAccessException {

        if(queryClass.isRecord()) {
            for (RecordComponent rc : queryClass.getRecordComponents()) {
                if(rc.getName().equals(fieldName))
                    return lookup.unreflect(rc.getAccessor());
            }
        }

        String cap = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String methodName = "set" + cap;

        Method m = null;

        try {
            m = queryClass.getDeclaredMethod(methodName, pramType);

        } catch (NoSuchMethodException e) {

            try {
                if(pramType == long.class) {
                    m = queryClass.getDeclaredMethod(methodName, Long.class);
                    return lookup.unreflect(m);

                }else if(pramType == double.class) {
                    m = queryClass.getDeclaredMethod(methodName, Double.class);
                    return lookup.unreflect(m);
                }
            }catch (NoSuchMethodException ex) {
                throw new IllegalArgumentException("[ERROR] %s class doesn't have %s field or setter Method".
                        formatted(queryClass.getName(), fieldName));
            }
        }
        return lookup.unreflect(m);

    }






    private LongAccessor forLongWrite(QueryKey queryKey) {
        return (LongAccessor) CACHE.get(queryKey);
    }

    private DoubleAccessor forDoubleWrite(QueryKey queryKey) {
        return (DoubleAccessor) CACHE.get(queryKey);
    }

    private LongSetter forLongSet(QueryKey queryKey) {
        return (LongSetter) CACHE.get(queryKey);
    }

    private DoubleSetter forDoubleSet(QueryKey queryKey) {
        return (DoubleSetter) CACHE.get(queryKey);
    }

    private StringAccessor forStringWrite(QueryKey queryKey) {
        return (StringAccessor) CACHE.get(queryKey);
    }





    private String generateGroupByKey(String SERIAL_NUMBER, Class<?> queryClass, Object queryDto, GroupByKey[] keys) {

        List<String> groupKeys = new ArrayList<>();

        for(GroupByKey key : keys) {
            String field = key.field();
            ValueType type = key.type();

            if(type == ValueType.LONG) {
                QueryKey queryKey = new QueryKey(queryClass, field, type);
                LongAccessor acc = forLongWrite(queryKey);
                groupKeys.add(String.valueOf(acc.get(queryDto)));

            }else if(type == ValueType.DOUBLE) {
                QueryKey queryKey = new QueryKey(queryClass, field, type);
                DoubleAccessor acc = forDoubleWrite(queryKey);
                groupKeys.add(String.valueOf(acc.get(queryDto)));

            }else if(type == ValueType.STRING) {
                QueryKey queryKey = new QueryKey(queryClass, field, type);
                StringAccessor acc = forStringWrite(queryKey);
                groupKeys.add(acc.get(queryDto));

            }else
                throw new IllegalStateException("[ERROR] Unsupported value type. valueType=%s".formatted(type));
        }
        return "["+SERIAL_NUMBER +"]" + groupKeys.stream().collect(Collectors.joining(","));
    }


    public List<QueryKey> getReadQueryKeyData(Object readQuery) {

        List<QueryKey> queryKeys = readQueryKeyData.get(readQuery);
        if(queryKeys == null)
            throw new IllegalArgumentException("[ERROR] %s class is not included in Metadata".formatted(readQuery));

        return queryKeys;
    }

}
