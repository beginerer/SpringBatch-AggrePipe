package com.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String userName;

    private String password;

    @Enumerated(value = EnumType.STRING)
    private Gender gender;


    public User(String userName, String password, Gender gender) {
        this.userName = userName;
        this.password = password;
        this.gender = gender;
    }
}
