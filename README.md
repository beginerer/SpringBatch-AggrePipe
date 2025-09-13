
## Architecture
<img width="876" height="430" alt="스크린샷 2025-09-13 오후 9 22 03" src="https://github.com/user-attachments/assets/7a7aed6a-dff8-41ad-819a-b1a3d7e70fa3" />



## Perfomance Test

<img width="355" height="214" alt="스크린샷 2025-09-13 오후 8 39 07" src="https://github.com/user-attachments/assets/19b0e6f4-a884-4e75-978f-6802b8178fad" /> </br>

<img width="357" height="216" alt="스크린샷 2025-09-13 오후 10 27 06" src="https://github.com/user-attachments/assets/e232330a-d21d-4b54-8533-e20c39160f35" />


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
