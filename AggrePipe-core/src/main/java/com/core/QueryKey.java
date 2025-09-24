package com.core;

import com.core.operation.ValueType;

import java.util.Objects;

public class QueryKey {


    private final Class<?> queryClass;

    private final String fieldName;

    private final ValueType dataType;


    public QueryKey(Class<?> queryClass, String fieldName, ValueType dataType) {
        this.queryClass = queryClass;
        this.fieldName = fieldName;
        this.dataType = dataType;
    }

    public Class<?> getQueryClass() {
        return queryClass;
    }

    public String getFieldName() {
        return fieldName;
    }

    public ValueType getDataType() {
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
