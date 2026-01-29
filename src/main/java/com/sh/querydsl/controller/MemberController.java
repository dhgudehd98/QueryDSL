package com.sh.querydsl.controller;

import com.sh.querydsl.dto.MemberSearchDto;
import com.sh.querydsl.dto.MemberTeamDto;
import com.sh.querydsl.repository.MemberJpaRepository;
import com.sh.querydsl.service.MemberService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Profile("local")
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;


    @PostConstruct
    public void init() {
        memberService.init();
    }

    @GetMapping("v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchDto memberSearchDto) {
        return memberService.searchMemberV1(memberSearchDto);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(MemberSearchDto memberSearchDto, Pageable pageable) {
        return memberService.searchPageSimple(memberSearchDto, pageable);
    }
    @GetMapping("/v3/members")
    public Page<MemberTeamDto> searchMemberV3(MemberSearchDto memberSearchDto, Pageable pageable) {
        return memberService.searchPageComplex(memberSearchDto, pageable);
    }
}