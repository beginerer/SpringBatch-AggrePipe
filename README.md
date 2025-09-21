
## Architecture
<img width="876" height="430" alt="스크린샷 2025-09-13 오후 9 22 03" src="https://github.com/user-attachments/assets/7a7aed6a-dff8-41ad-819a-b1a3d7e70fa3" />



## Write Perfomance Test

<img width="347" height="210" alt="write" src="https://github.com/user-attachments/assets/10ba043f-8d79-4515-8474-642dce9117f8" />

## Read Performance Test

<img width="354" height="214" alt="스크린샷 2025-09-21 오후 11 53 11" src="https://github.com/user-attachments/assets/68695cbe-1132-449b-8ba7-df53834cf482" />




### Test Dto
``` java
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


}
```
``` java
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

}
```
