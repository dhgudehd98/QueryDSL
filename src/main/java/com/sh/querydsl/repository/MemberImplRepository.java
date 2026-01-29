package com.sh.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sh.querydsl.dto.MemberSearchDto;
import com.sh.querydsl.dto.MemberTeamDto;
import com.sh.querydsl.dto.QMemberTeamDto;
import jakarta.persistence.EntityManager;

import java.util.List;

import static com.sh.querydsl.entity.QMember.member;
import static com.sh.querydsl.entity.QTeam.team;

public class MemberImplRepository implements MemberRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public MemberImplRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchDto searchDto) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.name,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        nameEq(searchDto.getName()),
                        teamNameEq(searchDto.getTeamName()),
                        ageGoe(searchDto.getAgeGoe()),
                        ageLoe(searchDto.getAgeLoe())
                )
                .fetch();
    }

    private BooleanExpression nameEq(String name) {
        return name != null ? member.name.eq(name) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return teamName != null ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.goe(ageLoe) : null;
    }
}