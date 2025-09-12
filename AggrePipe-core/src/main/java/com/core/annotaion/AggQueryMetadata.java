package com.core.annotaion;


import java.util.List;

public class AggQueryMetadata {


    private final String name;

    private final GroupByKey[] groupByKeys;

    private final boolean isRecord;

    private final List<ItemSpec> itemSpecs;



    public AggQueryMetadata(String name, GroupByKey[] groupByKeys, boolean isRecord, List<ItemSpec> itemSpecs) {
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

    public List<ItemSpec> getItems() {
        return itemSpecs;
    }

}
