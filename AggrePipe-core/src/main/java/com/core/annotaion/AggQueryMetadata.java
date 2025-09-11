package com.core.annotaion;


import java.util.List;

public class AggQueryMetadata {


    private final String name;

    private final GroupByKey[] groupByKeys;

    private final boolean isRecord;

    private final List<Item> items;



    public AggQueryMetadata(String name, GroupByKey[] groupByKeys, boolean isRecord, List<Item> items) {
        this.name = name;
        this.groupByKeys = groupByKeys;
        this.isRecord = isRecord;
        this.items = items;
    }


    public String getName() {
        return name;
    }

    public GroupByKey[] getGroupByKeys() {
        return groupByKeys;
    }

    public boolean isRecord() {
        return isRecord;
    }

    public List<Item> getItems() {
        return items;
    }

}
