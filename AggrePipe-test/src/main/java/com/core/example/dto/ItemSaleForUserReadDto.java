package com.core.example.dto;


import com.core.annotaion.GroupByKey;
import com.core.annotaion.ReadAggField;
import com.core.annotaion.ReadQuery;
import com.core.operation.Operation;
import com.core.operation.ValueType;

@ReadQuery(
        aggQueryClassName = "ItemSaleForUserQueryDto",
        groupByKeys = {
            @GroupByKey(field = "itemId", type = ValueType.LONG),
            @GroupByKey(field = "age", type = ValueType.LONG)}
)
public class ItemSaleForUserReadDto {


    private Long itemId;

    private Long age;

    @ReadAggField(originalFieldName = "quantity", op = Operation.SUM, type = ValueType.LONG)
    private Long sum_quantity;


    @ReadAggField(originalFieldName = "price", op = Operation.MAX, type = ValueType.LONG)
    private Long max_price;

    @ReadAggField(originalFieldName = "price", op = Operation.MIN, type = ValueType.LONG)
    private Long min_price;




    public ItemSaleForUserReadDto(Long itemId, Long age) {
        this.itemId = itemId;
        this.age = age;
    }

    public Long getItemId() {
        return itemId;
    }

    public Long getAge() {
        return age;
    }

    public Long getSum_quantity() {
        return sum_quantity;
    }

    public Long getMax_price() {
        return max_price;
    }

    public Long getMin_price() {
        return min_price;
    }


    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public void setAge(Long age) {
        this.age = age;
    }

    public void setSum_quantity(Long sum_quantity) {
        this.sum_quantity = sum_quantity;
    }

    public void setMax_price(Long max_price) {
        this.max_price = max_price;
    }

    public void setMin_price(Long min_price) {
        this.min_price = min_price;
    }

}
