package com.sh.querydsl;

import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sh.querydsl.dto.MemberDto;
import com.sh.querydsl.dto.QMemberDto;
import com.sh.querydsl.entity.Member;
import com.sh.querydsl.entity.QMember;
import com.sh.querydsl.entity.Team;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static com.sh.querydsl.entity.QMember.member;
import static com.sh.querydsl.entity.QTeam.team;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QueryDslMiddleTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory ;

    @BeforeEach
    public void before() {

        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamB);

        Member member3 = new Member("member3", 30, teamA);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    @DisplayName("")
    void getDtoByBean() {
        List<MemberDto> fetch = queryFactory
                // bean으로 접근하는 것은 MemberDto에 해당하는 dto - name, age에 대한 setter에 값 주입
                .select(Projections.bean(MemberDto.class,
                        member.name,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : fetch) {
//            System.out.println("username : " + memberDto.getName());

        }

    }

    @Test
    @DisplayName("")
    void getDtoByFields() {

        List<MemberDto> fetch = queryFactory
                // setter에 값 주입하는 것도 아님 기본생성자만 존재하면 자동으로 QueryDSL에서 값을 대입해줌
                .select(Projections.fields(
                        MemberDto.class,
                        member.name,
                        member.age
                ))
                .from(member)
                .fetch();

        for (MemberDto memberDto : fetch) {
//            System.out.println("member Name : " + memberDto.getName());
        }

    }

    @Test
    @DisplayName("")
    void getDtoByConstructor() {
        List<MemberDto> fetch = queryFactory.select(Projections.constructor(
                MemberDto.class,
                member.name.as("userName"),
                member.age
        )).from(member).fetch();

        for (MemberDto memberDto : fetch) {
            System.out.println("memberDto : " + memberDto.toString());

        }

    }

    void queryProjection() {
        /**
         * MemberDto 생성자에 @QueryProjection 어노테이션을 추가하면 QMemberDto에 대한 데이터가 생성되고, 해당 Dto를 select문에서 조회할 수 있음.
         */
        List<MemberDto> fetch = queryFactory.select(new QMemberDto(member.name, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : fetch) {
            System.out.println("memberDto : " + memberDto.toString());
        }
    }


    void alias() {
        QMember memberSub = new QMember("memberSub");
        List<MemberDto> fetch = queryFactory
                .select(Projections.fields(
                        MemberDto.class,
                        member.name.as("username"),
                        // 서브쿼리에 사용해서 별칭이 다른 경우 ExpressionUtils를 사용해서 별칭 설정
                        ExpressionUtils.as(
                                select(memberSub.age.max())
                                        .from(memberSub), "age")
                ))
                .from(member)
                .fetch();
    }


    @Test
    @DisplayName("")
    void dynamicQueryByBuilder() {
        /**
         * 동적쿼리 작성하는 방법
         * 1. BooleanBuilder를 사용해서 동적쿼리 작성
         * 2. where 다중 파라미터 사용(BooleanExpression 사용)
         */
        String userNameParam = "member1";
        Integer userAgeParam = 10;

        List<Member> list = searchMember(userNameParam, userAgeParam);

        assertThat(list.size()).isEqualTo(1);
        assertThat(list).extracting("name").containsExactly("member1");

        for (Member member1 : list) {
            System.out.println("memberName : " + member1.getName());

        }
    }

    private List<Member> searchMember(String userName, Integer userAgeParam) {
        BooleanBuilder builder = new BooleanBuilder();

        if (userName != null) {
            builder.and(member.name.eq(userName));
        }
        if (userAgeParam != null) {
            builder.and(member.age.eq(userAgeParam));
        }

        return queryFactory.selectFrom(member)
                .where(builder)
                .fetch();
    }


    @Test
    @DisplayName("")
    void dynamicQueryByWhere() {
        /**
         * 동적쿼리 작성하는 방법
         * 1. BooleanBuilder를 사용해서 동적쿼리 작성
         * 2. where 다중 파라미터 사용(BooleanExpression 사용)
         */
        String userNameParam = null;
        Integer userAgeParam = 10;

        List<Member> list = searchMember2(userNameParam, userAgeParam);

        assertThat(list.size()).isEqualTo(2);

        for (Member member1 : list) {
            System.out.println("memberName : " + member1.getName());
        }
    }

    private List<Member> searchMember2(String name , Integer age) {
        return queryFactory.selectFrom(member)
                .where(findName(name), findAge(age))
                .fetch();
    }

    private BooleanExpression findName(String name) {
        return name != null ? member.name.eq(name) : null;
    }

    private BooleanExpression findAge(Integer age) {
        return age != null ? member.age.eq(age) : null;
    }


    @Test
    @DisplayName("")
    void bulkUpdate() {
        long count = queryFactory
                .update(member)
                .set(member.name, "비회원")
                .where(member.age.lt(15))
                .execute();

        System.out.println("count : " + count);

        // 벌크 연산을 진행할 때 영속성 컨텍스트에 대한 값을 항상 초기화를 해줘야함. 데이터베이스에 상태값만 변경되고 영속성 컨텍스트 안에서는 값이 변경되지 않음.
        em.flush();
        em.clear();

        Member m = queryFactory.select(member).from(member).where(member.name.eq("비회원")).fetchOne();

        assertThat(m).extracting("name").isEqualTo("비회원");

    }


}