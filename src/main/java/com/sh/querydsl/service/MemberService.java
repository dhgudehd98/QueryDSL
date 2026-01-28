package com.sh.querydsl.service;

import com.sh.querydsl.dto.MemberSearchDto;
import com.sh.querydsl.dto.MemberTeamDto;
import com.sh.querydsl.entity.Member;
import com.sh.querydsl.entity.Team;
import com.sh.querydsl.repository.MemberJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final EntityManager em;
    private final MemberJpaRepository memberJpaRepository;


    @Transactional
    public void init() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        for (int i = 0; i < 100; i++) {
            Team t = i % 2 == 0 ? teamA : teamB;
            em.persist(new Member("member" + i, i, t));
        }
    }

    public List<MemberTeamDto> searchMemberV1(MemberSearchDto searchDto) {
        return memberJpaRepository.search(searchDto);
    }
}