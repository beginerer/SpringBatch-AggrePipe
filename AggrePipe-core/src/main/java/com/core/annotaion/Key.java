package com.core.annotaion;

import org.springframework.util.ClassUtils;

import java.util.Objects;

public class Key {


    private final Class<?> queryDto;

    private final String name;

    private final Class<?> dataType;


    public Key(Class<?> queryDto, String logicalName, Class<?> rawType) {
        this.queryDto = Objects.requireNonNull(queryDto);
        this.name = Objects.requireNonNull(logicalName);
        Objects.requireNonNull(rawType);
        this.dataType = ClassUtils.resolvePrimitiveIfNecessary(rawType);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Key that = (Key) o;
        return Objects.equals(queryDto, that.queryDto) &&
                Objects.equals(name, that.name) &&
                Objects.equals(dataType, that.dataType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queryDto, name, dataType);
    }
}
