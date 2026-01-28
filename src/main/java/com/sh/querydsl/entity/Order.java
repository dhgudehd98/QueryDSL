package com.sh.querydsl.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity@Getter
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    private String orderName;

    public Order() {
    }

    public Order(Long id, String orderName) {
        this.id = id;
        this.orderName = orderName;
    }
}