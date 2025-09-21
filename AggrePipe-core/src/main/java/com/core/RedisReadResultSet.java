package com.core;

import java.util.List;

public class RedisReadResultSet {

    private final String scriptSerialNumber;

    private final List<Data> data;

    private final boolean success;


    public RedisReadResultSet(String scriptSerialNumber, List<Data> data, boolean success) {
        this.scriptSerialNumber = scriptSerialNumber;
        this.data = data;
        this.success = success;
    }

    public String getScriptSerialNumber() {
        return scriptSerialNumber;
    }


    public List<Data> getData() {
        return data;
    }


    public boolean isSuccess() {
        return success;
    }


    public static class Data {

        private String groupKey;

        private List<String> data;

        private Object queryDto;


        public Data(String groupKey, List<String> data, Object queryDto) {
            this.groupKey = groupKey;
            this.data = data;
            this.queryDto = queryDto;
        }

        public String getGroupKey() {
            return groupKey;
        }

        public List<String> getData() {
            return data;
        }

        public Object getQueryDto() {
            return queryDto;
        }
    }
}
