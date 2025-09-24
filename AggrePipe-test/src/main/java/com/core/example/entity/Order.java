package com.core.example.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {


    @Id @GeneratedValue
    private Long orderId;


    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private Long storeId;

    private int price;


    public Order(User user, Long storeId, int price) {
        this.user = user;
        this.storeId = storeId;
        this.price = price;
    }
}
