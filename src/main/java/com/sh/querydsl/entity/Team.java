package com.sh.querydsl.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Team {

    @Id @GeneratedValue
    private Long id;
    private String name;

    @OneToMany(mappedBy = "team")
    List<Member> memberList = new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }

    public Team() {
    }
}