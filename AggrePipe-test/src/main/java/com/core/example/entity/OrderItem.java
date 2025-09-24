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
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    private Item item;

    private int quantity;


    public OrderItem(Order order, Item item, int quantity) {
        this.order = order;
        this.item = item;
        this.quantity = quantity;
    }
}
