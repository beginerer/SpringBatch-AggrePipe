package com.core.annotaion;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AggQueryRegistry {


    private final Map<Class<?>, AggQueryMetadata> registry;

    private final ConcurrentHashMap<Key, Object> CACHE = new ConcurrentHashMap<>();




    public AggQueryRegistry(Map<Class<?>, AggQueryMetadata> registry) {
        this.registry = registry;
        init();
    }

    private void init() throws IllegalAccessException {
        MethodHandles.Lookup base = MethodHandles.lookup();

        for (Class<?> queryDto : registry.keySet()) {
            AggQueryMetadata metadata = registry.get(queryDto);

            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(queryDto, base);
            lookup.unreflect()
            if(metadata.isRecord()) {




            }else {

            }

        }

    }

    private MethodHandle findMethodHandler()



}
