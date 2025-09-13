package com.core;


import com.core.annotaion.AggField;
import com.core.annotaion.AggQuery;
import com.core.annotaion.GroupByKey;
import com.core.operation.Operation;
import com.core.operation.ValueType;

import java.time.LocalDateTime;



@AggQuery(groupByKeys = {
        @GroupByKey(field = "userId", type = ValueType.LONG),
        @GroupByKey(field = "orderId", type = ValueType.LONG)}
)
public class QueryDto {


    private Long userId;

    private Long orderId;

    @AggField(op = {Operation.SUM, Operation.MAX, Operation.MIN}, type = ValueType.DOUBLE)
    private Double unitPrice;

    @AggField(op = {Operation.MAX, Operation.MIN}, type = ValueType.LONG)
    private Long quantity;

    private LocalDateTime orderAt;


    public QueryDto(Long userId, Long orderId, Double unitPrice, Long quantity, LocalDateTime orderAt) {
        this.userId = userId;
        this.orderId = orderId;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.orderAt = orderAt;
    }


    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public Long getQuantity() {
        return quantity;
    }

    public LocalDateTime getOrderAt() {
        return orderAt;
    }


}
