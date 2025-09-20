package com.core;


import com.core.annotaion.*;
import com.core.operation.Operation;
import com.core.operation.ValueType;


@ReadQuery(aggQueryClassName = "QueryDto",
        groupByKeys = {
                @GroupByKey(field = "userId", type = ValueType.LONG),
                @GroupByKey(field = "orderId", type = ValueType.LONG)
        })
public class ReadDto {

    private Long userId;

    private Long orderId;

    @ReadAggField(originalFieldName = "unitPrice", op = Operation.SUM, type = ValueType.DOUBLE)
    private Double Summ_unitPrice;

    @ReadAggField(originalFieldName = "unitPrice", op = Operation.MAX, type = ValueType.DOUBLE)
    private Double maxUnitPirce;

    @ReadAggField(originalFieldName = "unitPrice", op = Operation.MIN, type = ValueType.DOUBLE)
    private Double minUnitPrice;

    @ReadAggField(originalFieldName = "quntity", op = Operation.MIN, type = ValueType.LONG)
    private Long quantity;
}
