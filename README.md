
## Perfomance Test

<img width="355" height="214" alt="스크린샷 2025-09-13 오후 8 39 07" src="https://github.com/user-attachments/assets/19b0e6f4-a884-4e75-978f-6802b8178fad" /> </br>

<img width="356" height="212" alt="스크린샷 2025-09-13 오후 8 46 04" src="https://github.com/user-attachments/assets/6926464e-e117-4173-8428-866090f63c79" />


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
```
