package com.core;


import com.core.annotaion.AggField;
import com.core.annotaion.AggQuery;
import com.core.annotaion.GroupByKey;
import com.core.operation.Operation;
import com.core.operation.ValueType;

import java.time.LocalDateTime;

@AggQuery(groupByKeys = @GroupByKey(field = "userId"))
public class QueryDto {


    private Long userId;

    @AggField(op = Operation.COUNT, type = ValueType.LONG)
    private Long orderCount;

    @AggField(op = Operation.SUM, type = ValueType.DOUBLE)
    private Double purchaseAmount;

    private LocalDateTime orderAt;


    public QueryDto(Long userId, Long orderCount, Double purchaseAmount, LocalDateTime orderAt) {
        this.userId = userId;
        this.orderCount = orderCount;
        this.purchaseAmount = purchaseAmount;
        this.orderAt = orderAt;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public Double getPurchaseAmount() {
        return purchaseAmount;
    }

    public LocalDateTime getOrderAt() {
        return orderAt;
    }
}
