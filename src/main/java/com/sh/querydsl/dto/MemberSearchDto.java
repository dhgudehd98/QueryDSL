package com.sh.querydsl.dto;


import lombok.Data;

@Data
public class MemberSearchDto {

    private String name;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;
}