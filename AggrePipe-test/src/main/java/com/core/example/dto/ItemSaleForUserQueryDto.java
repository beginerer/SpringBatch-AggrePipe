package com.core.example.dto;


import com.core.annotaion.AggField;
import com.core.annotaion.AggQuery;
import com.core.annotaion.GroupByKey;
import com.core.operation.Operation;
import com.core.operation.ValueType;
import com.querydsl.core.annotations.QueryProjection;


@AggQuery(groupByKeys = {
        @GroupByKey(field = "itemId", type = ValueType.LONG),
        @GroupByKey(field = "age", type = ValueType.LONG)
})
public class ItemSaleForUserQueryDto {


    private Long orderItemId;

    private Long orderId;

    private Long itemId;

    private String itemName;

    private Long age;

    @AggField(op = {Operation.SUM}, type = ValueType.LONG)
    private Long quantity;

    @AggField(op = {Operation.MAX, Operation.MIN}, type = ValueType.LONG)
    private Long price;



    @QueryProjection
    public ItemSaleForUserQueryDto(Long orderItemId, Long orderId, Long itemId, String itemName, Long age, Long quantity, Long price) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.age = age;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public Long getAge() {
        return age;
    }

    public Long getQuantity() {
        return quantity;
    }

    public Long getPrice() {
        return price;
    }
}
