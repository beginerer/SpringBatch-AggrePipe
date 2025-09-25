package com.core.example.query;



public class Cursor {

    private Long itemId;

    private Long age;


    public Cursor(Long itemId, Long age) {
        this.itemId = itemId;
        this.age = age;
    }


    public Long getItemId() {
        return itemId;
    }

    public Long getAge() {
        return age;
    }
}
