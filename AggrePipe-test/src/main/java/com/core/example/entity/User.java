package com.core.example.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true)
    private Long loginId;

    private String password;

    private Gender gender;

    private int age;


    public User(Long loginId, String password, Gender gender, int age) {
        this.loginId = loginId;
        this.password = password;
        this.gender = gender;
        this.age = age;
    }

}
