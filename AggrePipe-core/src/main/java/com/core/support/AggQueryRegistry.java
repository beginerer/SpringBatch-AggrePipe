package com.core.support;

import com.core.*;
import com.core.annotaion.GroupByKey;
import com.core.operation.ValueType;
import org.springframework.util.ClassUtils;

import java.lang.invoke.*;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class AggQueryRegistry {


    // write query
    private final Map<Class<?>, AggQueryMetadata> aggQueryMetadataMap;

    private final Map<Class<?>, List<QueryKey>> aggQueryKeyData;

    private final Map<Class<?>, List<QueryKey>> aggQueryGroupByKeyData;

    private final Map<QueryKey, ItemSpec> keyItemMap;






    private final ConcurrentHashMap<QueryKey, Object> CACHE;




    public AggQueryRegistry(Map<Class<?>, AggQueryMetadata> aggQueryMetadataMap) {
        this.aggQueryMetadataMap = aggQueryMetadataMap;
        this.keyItemMap = new HashMap<>();
        this.aggQueryGroupByKeyData = new HashMap<>();
        this.aggQueryKeyData = buildKeyData();
        this.CACHE = buildCacheData();
    }



    private Map<Class<?>, List<QueryKey>> buildKeyData() {
        Map<Class<?>, List<QueryKey>> map = new HashMap<>();

        for (Class<?> queryClass : aggQueryMetadataMap.keySet()) {
            List<QueryKey> queryKeys = new ArrayList<>();
            List<QueryKey> groupByKeys = new ArrayList<>();
            AggQueryMetadata metadata = aggQueryMetadataMap.get(queryClass);

            for(GroupByKey key : metadata.getGroupByKeys()) {
                String field = key.field();
                ValueType valueType = key.type();

                if(valueType == ValueType.LONG) {
                    QueryKey queryKey = new QueryKey(queryClass, field, long.class);
                    groupByKeys.add(queryKey);
                }else if(valueType == ValueType.DOUBLE) {
                    QueryKey queryKey = new QueryKey(queryClass, field, double.class);
                    groupByKeys.add(queryKey);
                }else
                    throw new IllegalArgumentException("[ERROR] Unsupported value type. valueType=%s".
                            formatted(valueType));
            }
            aggQueryGroupByKeyData.put(queryClass, groupByKeys);

            for (ItemSpec item : metadata.getItems()) {
                if(item.getValueType() == ValueType.LONG) {
                    QueryKey queryKey = new QueryKey(queryClass, item.getFieldName(), long.class);
                    queryKeys.add(queryKey);
                    keyItemMap.put(queryKey, item);
                } else if(item.getValueType() == ValueType.DOUBLE) {
                    QueryKey queryKey = new QueryKey(queryClass, item.getFieldName(), double.class);
                    queryKeys.add(queryKey);
                    keyItemMap.put(queryKey, item);
                }else
                    throw new IllegalArgumentException("[ERROR] Unsupported value type. valueType=%s".
                            formatted(item.getValueType()));
            }
            map.put(queryClass, queryKeys);
        }
        return map;
    }


    private ConcurrentHashMap<QueryKey, Object> buildCacheData() {
        ConcurrentHashMap<QueryKey,Object> cacheMap = new ConcurrentHashMap<>();

        for (Class<?> queryClass : aggQueryKeyData.keySet()) {
            for (QueryKey queryKey : aggQueryKeyData.get(queryClass)) {
                Object result = cacheMap.putIfAbsent(queryKey, createLambda(queryKey.getQueryClass(), queryKey.getFieldName(), queryKey.getDataType()));

                if(result != null)
                    throw new IllegalStateException("[ERROR] duplicate key: " + queryKey);
            }
        }

        for (Class<?> queryClass : aggQueryGroupByKeyData.keySet()) {
            for (QueryKey queryKey : aggQueryGroupByKeyData.get(queryClass)) {
                Object result = cacheMap.putIfAbsent(queryKey, createLambda(queryKey.getQueryClass(), queryKey.getFieldName(), queryKey.getDataType()));

                if(result != null)
                    throw new IllegalStateException("[ERROR] duplicate key: " + queryKey);
            }
        }
        return cacheMap;
    }



    private Object createLambda(Class<?> queryClass, String fieldName, Class<?> returnType) {
        try {
            Class<?> wrappedType = ClassUtils.resolvePrimitiveIfNecessary(returnType);
            if (wrappedType != Long.class && wrappedType != Double.class) {
                throw new IllegalArgumentException("primitiveRetType must be long.class or double.class");
            }
            boolean isLong = (wrappedType == Long.class);
            Class<?> prim = isLong ? long.class : double.class;

            MethodHandles.Lookup base = MethodHandles.lookup();
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(queryClass, base);

            MethodHandle impl = findTargetHandle(lookup, queryClass, fieldName);
            MethodType erased = MethodType.methodType(prim, Object.class);
            MethodType dyn = MethodType.methodType(prim, queryClass);

            Class<?> iface = isLong ? LongAccessor.class : DoubleAccessor.class;
            CallSite cs = LambdaMetafactory.metafactory(lookup, "get", MethodType.methodType(iface), erased, impl, dyn);

            if (isLong) {
                return (LongAccessor) cs.getTarget().invokeExact();
            } else {
                return (DoubleAccessor) cs.getTarget().invokeExact();
            }
        } catch (IllegalAccessException | LambdaConversionException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    private MethodHandle findTargetHandle(MethodHandles.Lookup lookup, Class<?> queryClass, String fieldName) throws IllegalAccessException {

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


    public List<ItemUnit> extractValue(Object queryDto) {
        Class<?> queryClass = queryDto.getClass();

        List<QueryKey> queryKeys = aggQueryKeyData.get(queryClass);
        if (queryKeys == null || queryKeys.isEmpty()) {
            throw new IllegalArgumentException(
                    "[ERROR] %s is not included in MetaData".formatted(queryClass.getName()));
        }

        List<ItemUnit> itemUnits = new ArrayList<>();

        for (QueryKey queryKey : queryKeys) {
            if(queryKey.getDataType() == Long.class) {
                LongAccessor longAcc = forLong(queryKey);
                long value = longAcc.get(queryDto);

                ItemSpec itemSpec = keyItemMap.get(queryKey);
                ItemUnit itemUnit = new ItemUnit(itemSpec.getFieldName(), itemSpec.getOp(), itemSpec.getValueType(), value, null);
                itemUnits.add(itemUnit);

            }else if(queryKey.getDataType() == Double.class) {
                DoubleAccessor doubleAcc = forDouble(queryKey);
                double value = doubleAcc.get(queryDto);

                ItemSpec itemSpec = keyItemMap.get(queryKey);
                ItemUnit itemUnit = new ItemUnit(itemSpec.getFieldName(), itemSpec.getOp(), itemSpec.getValueType(), null, value);
                itemUnits.add(itemUnit);
            }else
                throw new IllegalArgumentException("[ERROR] Unsupported data type. dataType=%s".formatted(queryKey.getDataType()));
        }

        return itemUnits;
    }


    public String getGroupByKeys(String SERIAL_NUMBER, Object queryDto) {
        Class<?> queryClass = queryDto.getClass();
        AggQueryMetadata metadata = aggQueryMetadataMap.get(queryClass);
        if(metadata == null)
            throw new IllegalArgumentException("[ERROR] %s class is not included in MetaData".formatted(queryClass));

        return generateGroupByKey(SERIAL_NUMBER, queryClass, queryDto, metadata.getGroupByKeys());
    }


    private LongAccessor forLong(QueryKey queryKey) {
        return (LongAccessor) CACHE.get(queryKey);
    }


    private DoubleAccessor forDouble(QueryKey queryKey) {
        return (DoubleAccessor) CACHE.get(queryKey);
    }


    private String generateGroupByKey(String SERIAL_NUMBER, Class<?> queryClass, Object queryDto, GroupByKey[] keys) {

        List<String> groupKeys = new ArrayList<>();

        for(GroupByKey key : keys) {
            String field = key.field();
            ValueType type = key.type();

            if(type == ValueType.LONG) {
                QueryKey queryKey = new QueryKey(queryClass, field, long.class);
                LongAccessor acc = forLong(queryKey);
                groupKeys.add(String.valueOf(acc.get(queryDto)));

            }else if(type == ValueType.DOUBLE) {
                QueryKey queryKey = new QueryKey(queryClass, field, double.class);
                DoubleAccessor acc = forDouble(queryKey);
                groupKeys.add(String.valueOf(acc.get(queryDto)));

            }else
                throw new IllegalStateException("[ERROR] Unsupported value type. valueType=%s".formatted(type));
        }
        return "["+SERIAL_NUMBER +"]" + groupKeys.stream().collect(Collectors.joining(","));
    }
}
