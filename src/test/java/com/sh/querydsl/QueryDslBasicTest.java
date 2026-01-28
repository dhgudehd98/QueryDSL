package com.sh.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sh.querydsl.entity.Member;
import com.sh.querydsl.entity.QMember;
import com.sh.querydsl.entity.QTeam;
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

import static com.querydsl.jpa.JPAExpressions.*;
import static com.sh.querydsl.entity.QMember.*;
import static com.sh.querydsl.entity.QTeam.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QueryDslBasicTest {

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
    public void startQueryDSL() {

        Member member1 = queryFactory.
                selectFrom(member)
                .where(
                        member.name.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        List<Member> memberList = queryFactory
                .selectFrom(member)
                .fetch();

        for (Member findMember : memberList) {
            System.out.println("member Name : "  +findMember.getName());
        }

        assertThat(member1.getName()).isEqualTo("member1");
    }

    @Test
    @DisplayName("QueryDSL sort")
    public void sort() {
        /**
         * 회원 정렬 순서
         * 1. 회원 나이 내림차순
         * 2. 회원 이름 올림차순
         * 회원 이름이 없으면 마지막에 출력(nullLast())
         */

        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> memberList = queryFactory.select(member)
                .from(member)
                .orderBy(member.age.desc(), member.name.asc().nullsLast()) // age인 값들이 먼저 정렬 되고 , 그 안에서 name에 대한 값이 정렬
                .fetch();


        for (Member m : memberList) {
            System.out.println("member Name : " + m.getName());
            System.out.println("member age : " + m.getAge());
        }

    }

    @Test
    @DisplayName("Query DSL Paging By fetchResults")
    public void paging() {

        /**
         * fetchResults()에 대한 부분은 QueryDSL 4.x Version Deprecated 됨.
         * - 조회에 대한 부분에서 조인을 통해 조회를 하는 경우 count()를 할 때도 조인 후 count()를 하기 때문에 성능상 문제가 생길 수 있음.
         * - 따라서 fetchResult()에 대한 부분을 사용 자제하거나 , 다른 QueryDSL 생성해서 사용
         */
        QueryResults<Member> memberQueryResults = queryFactory
                .selectFrom(member)
                .offset(0)
                .limit(3)
                .fetchResults();

        List<Member> memberList = queryFactory
                .selectFrom(member)
                .offset(0)
                .limit(3)
                .fetch();

        // count에 대한 부분이 필요한 경우에만 조회하도록 설정
        Long l = queryFactory.select(member.count()).from(member).fetchOne();

        assertThat(memberQueryResults.getResults().size()).isEqualTo(3);
        assertThat(l).isEqualTo(4);
        assertThat(memberQueryResults.getOffset()).isEqualTo(0);
        assertThat(memberQueryResults.getLimit()).isEqualTo(3);
    }

    @Test
    @DisplayName("집계 함수 사용 ")
    void aggregate() {

        // 집계함수에 대한 값이 여러개 있으면 List 안에 값이 Tuple에 대한 값으로 받고 , 여러개의 집계함수가 select에 있으면 해당 변수형으로
        List<Tuple> tuples = queryFactory.select(member.age.sum(), member.age.avg())
                .from(member)
                .fetch();

        Tuple tuple = tuples.get(0);

        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);

    }


    @Test
    @DisplayName("Query DSL Groupt by ")
    void groupBy () {
        List<Tuple> teamNameList = queryFactory.select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = teamNameList.get(0);
        Tuple teamB = teamNameList.get(1);

        assertThat(teamA.get(member.age.avg())).isEqualTo(20);
        assertThat(teamB.get(member.age.avg())).isEqualTo(30);

    }

    @Test
    @DisplayName("연관관계가 없는 필드로 조인 ")
    void thetaJoin() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> fetch = queryFactory.select(member)
                .from(member, team)
                .where(member.name.eq(team.name))
                .fetch();

        assertThat(fetch).extracting("name").containsExactly("teamA", "teamB");
    }


    @Test
    @DisplayName("ON 절을 활용한 조인 ")
    void joinOnFiltering() {

        List<Tuple> fetch = queryFactory.select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        System.out.println("List Size : " + fetch.size());

    }

    @Test
    @DisplayName("Fetch Join")
    void fetchJoin() {

        System.out.println("== No fetch Join ==");
        Member notFetchmember = queryFactory.selectFrom(member)
                .where(member.name.eq("member2"))
                .fetchOne();
        assertThat(notFetchmember.getName()).isEqualTo("member2");
        System.out.println("== No fetch Join ==");
        System.out.println("== fetch Join ==");
        Member fetchMember = queryFactory.selectFrom(QMember.member)
                .join(QMember.member.team, team).fetchJoin()
                .where(QMember.member.name.eq("member1"))
                .fetchOne();
        System.out.println("== fetch Join ==");

        assertThat(fetchMember.getName()).isEqualTo("member1");
    }

    @Test
    @DisplayName("SubQuery DSL")
    void subQueryMax() {
        /**
         * 나이가 가장 많은 나이 조회
         */
        QMember memberSub = new QMember("memberSub");

        List<Member> memberList = queryFactory.selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();
        Member m = memberList.get(0);
        assertThat(m.getAge()).isEqualTo(40);
        assertThat(memberList).extracting("age").containsExactly(40);
    }

    @Test
    @DisplayName("SubQuery Goe")
    void subQueryGoe() {
        QMember memberSub = new QMember("memberSub");

        List<Member> memberList = queryFactory.selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(memberList.size()).isEqualTo(2);
        assertThat(memberList).extracting("age").containsExactly(30, 40);
    }

    @Test
    void subQueryIn() {
        QMember memberSubQuery = new QMember("memberSubQuery");
        List<Member> memberList = queryFactory.selectFrom(member)
                .where(member.age.in(
                        select(memberSubQuery.age)
                                .from(memberSubQuery)
                                .where(memberSubQuery.age.gt(10))
                ))
                .fetch();

        assertThat(memberList).extracting("age").containsExactly(20, 30, 40);

    }

    @Test
    void simpleCase() {
        List<String> fetch = queryFactory.select(
                        member.age
                                .when(10).then("열살")
                                .when(20).then("스무살")
                                .otherwise("Undefined")
                )
                .from(member)
                .fetch();


        for (String string : fetch) {
            System.out.println("나이 : " + string);
        }

    }

    @Test
    @DisplayName("")
    void flexCase() {
        List<String> fetch = queryFactory.select(
                        new CaseBuilder()
                                .when(member.age.lt(10)).then("어린이")
                                .when(member.age.lt(20)).then("청소년")
                                .when(member.age.lt(30)).then("성인")
                                .otherwise("어른")
                ).from(member)
                .fetch();

        for (String string : fetch) {
            System.out.println("나이 : " + string);
        }
    }

    @Test
    @DisplayName("")
    void concatQuery() {
        List<String> fetch = queryFactory.select(member.name.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();

        for (String s : fetch) {
            System.out.println("name : " + s);
        }

    }


}