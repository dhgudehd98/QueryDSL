package com.sh.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sh.querydsl.dto.MemberSearchDto;
import com.sh.querydsl.dto.MemberTeamDto;
import com.sh.querydsl.dto.QMemberTeamDto;
import com.sh.querydsl.entity.Member;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.sh.querydsl.entity.QMember.member;
import static com.sh.querydsl.entity.QTeam.team;

public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * memberTeamDtoQueryResults.getTotal()을 통해서 쿼리 결과 전체 count() 값을 산출
     * - count() 할 때 조인을 포함하면서 쿼리를 날리기 때문에 성능 저하 발생
     * - 위 방식을 해결하기 위해서는 fetchResults로 하지말고, 메인 쿼리의 반환 값을 .fetch()로 설정하고 별도의 count쿼리 작성
     */
    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchDto searchDto, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory.select(new QMemberTeamDto(
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
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

//        long total = queryFactory
//                .select(member)
//                .from(member)
//                .leftJoin(member.team , team)
//                .where(
//                        nameEq(searchDto.getName()),
//                        teamNameEq(searchDto.getTeamName()),
//                        ageGoe(searchDto.getAgeGoe()),
//                        ageLoe(searchDto.getAgeLoe())
//                )
//                .fetchCount();

        JPAQuery<Member> query = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        nameEq(searchDto.getName()),
                        teamNameEq(searchDto.getTeamName()),
                        ageGoe(searchDto.getAgeGoe()),
                        ageLoe(searchDto.getAgeLoe())
                );
        System.out.println("======= countQuery =======");
        /**
         * countQuery 생략 가능한 경우 생략해서 처리
         * - 첫 페이지이면서 결과 개수가 페이지 사이즈보다 작을 때
         * - 마지막 페이지 일 때
         * http 요청이 > http://localhost:8080/v3/members?size=110&page=0 이렇게 이루어진 경우 현재 데이터가 100개이고 , 첫번째 페이지이면서 size에 대한 값이 DB에 존재하는 데이터의 수 보다 큰 경우 count() 쿼리가 날라가지 않음.
         */
        return PageableExecutionUtils.getPage(content, pageable, query::fetchCount);

    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchDto searchDto, Pageable pageable) {
        QueryResults<MemberTeamDto> memberTeamDtoQueryResults = queryFactory.select(new QMemberTeamDto(
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
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDto> content = memberTeamDtoQueryResults.getResults();

        System.out.println("====count Query ====");
        long total = memberTeamDtoQueryResults.getTotal();
        System.out.println("====count Query ====");

        return new PageImpl<>(content, pageable, total);
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