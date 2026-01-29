package com.sh.querydsl.repository;

import com.sh.querydsl.dto.MemberSearchDto;
import com.sh.querydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchDto memberSearchDto);
}
