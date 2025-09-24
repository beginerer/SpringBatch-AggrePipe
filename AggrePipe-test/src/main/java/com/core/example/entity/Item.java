package com.core.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    private String name;

    private int price;


    public Item(Long itemId, String name, int price) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
    }
}
