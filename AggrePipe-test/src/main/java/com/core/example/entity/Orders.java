package com.core.example.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orders extends BaseEntity {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private Long storeId;

    private Long price;


    public Orders(User user, Long storeId, Long price) {
        this.user = user;
        this.storeId = storeId;
        this.price = price;
    }
}
