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
    private String loginId;

    private String password;

    @Enumerated(value = EnumType.STRING)
    private Gender gender;

    private Long age;


    public User(String loginId, String password, Gender gender, Long age) {
        this.loginId = loginId;
        this.password = password;
        this.gender = gender;
        this.age = age;
    }
}
