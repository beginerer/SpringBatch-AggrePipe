package com.core;

import org.springframework.util.ClassUtils;

import java.util.Objects;

public class QueryKey {


    private final Class<?> queryClass;

    private final String fieldName;

    private final Class<?> dataType;


    public QueryKey(Class<?> queryClass, String fieldName, Class<?> dataType) {
        this.queryClass = Objects.requireNonNull(queryClass);
        this.fieldName = Objects.requireNonNull(fieldName);
        Objects.requireNonNull(dataType);
        this.dataType = ClassUtils.resolvePrimitiveIfNecessary(dataType);
    }


    public Class<?> getQueryClass() {
        return queryClass;
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
        return Objects.equals(queryClass, that.queryClass) &&
                Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(dataType, that.dataType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queryClass, fieldName, dataType);
    }
}
