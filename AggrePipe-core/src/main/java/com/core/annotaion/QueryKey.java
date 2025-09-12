package com.core.annotaion;

import org.springframework.util.ClassUtils;

import java.util.Objects;

public class QueryKey {


    private final Class<?> queryDto;

    private final String fieldName;

    private final Class<?> dataType;


    public QueryKey(Class<?> queryDto, String fieldName, Class<?> rawType) {
        this.queryDto = Objects.requireNonNull(queryDto);
        this.fieldName = Objects.requireNonNull(fieldName);
        Objects.requireNonNull(rawType);
        this.dataType = ClassUtils.resolvePrimitiveIfNecessary(rawType);
    }


    public Class<?> getQueryDto() {
        return queryDto;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Class<?> getDataType() {
        return dataType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        QueryKey that = (QueryKey) o;
        return Objects.equals(queryDto, that.queryDto) &&
                Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(dataType, that.dataType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queryDto, fieldName, dataType);
    }
}
