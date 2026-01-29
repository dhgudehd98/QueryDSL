package com.sh.querydsl.repository;

import com.sh.querydsl.dto.MemberSearchDto;
import com.sh.querydsl.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchDto memberSearchDto);

    Page<MemberTeamDto> searchPageSimple(MemberSearchDto searchDto, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex(MemberSearchDto searchDto, Pageable pageable);
}
