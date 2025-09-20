package com.core;

import com.core.annotaion.GroupByKey;

import java.util.List;

public class ReadQueryMetadata {

    private final String name;

    private final GroupByKey[] groupByKeys;

    private final boolean isRecord;

    private final List<ReadItemSpec> itemSpecs;



    public ReadQueryMetadata(String name, GroupByKey[] groupByKeys, boolean isRecord, List<ReadItemSpec> itemSpecs) {
        this.name = name;
        this.groupByKeys = groupByKeys;
        this.isRecord = isRecord;
        this.itemSpecs = itemSpecs;
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

    public List<ReadItemSpec> getItems() {
        return itemSpecs;
    }
}
