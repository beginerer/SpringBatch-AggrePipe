package com.core.example.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Orders orders;

    @ManyToOne(fetch = FetchType.LAZY)
    private Item item;

    private Long price;

    private Long quantity;


    public OrderItem(Orders orders, Item item, Long price, Long quantity) {
        this.orders = orders;
        this.item = item;
        this.price = price;
        this.quantity = quantity;
    }
}
