package com.core;


import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class testMain {

    public static void main(String[] args) {
        Node node = new Node();
        System.out.println(node.getClass().getName());

    }


    static class Node {
        int x;
        int y;

    }
}
