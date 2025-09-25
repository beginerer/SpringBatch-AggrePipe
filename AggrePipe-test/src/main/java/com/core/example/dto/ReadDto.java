package com.core.example.dto;


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

    @ReadAggField(originalFieldName = "quantity", op = Operation.MIN, type = ValueType.LONG)
    private Long min_quanitity;



    public ReadDto(Long userId, Long orderId) {
        this.userId = userId;
        this.orderId = orderId;
    }


    public Long getUserId() {
        return userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Double getSumm_unitPrice() {
        return Summ_unitPrice;
    }

    public Double getMaxUnitPirce() {
        return maxUnitPirce;
    }

    public Double getMinUnitPrice() {
        return minUnitPrice;
    }

    public Long getMin_quanitity() {
        return min_quanitity;
    }


    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setSumm_unitPrice(Double summ_unitPrice) {
        Summ_unitPrice = summ_unitPrice;
    }

    public void setMaxUnitPirce(Double maxUnitPirce) {
        this.maxUnitPirce = maxUnitPirce;
    }

    public void setMinUnitPrice(Double minUnitPrice) {
        this.minUnitPrice = minUnitPrice;
    }

    public void setMin_quanitity(Long min_quanitity) {
        this.min_quanitity = min_quanitity;
    }

    @Override
    public String toString() {
        return "ReadDto{" +
                "  userId=" + userId +
                ", orderId=" + orderId +
                ", Summ_unitPrice=" + Summ_unitPrice +
                ", maxUnitPirce=" + maxUnitPirce +
                ", minUnitPrice=" + minUnitPrice +
                ", quantity=" + min_quanitity +
                '}';
    }
}
