package com.javalab.student.repository;

import com.javalab.student.entity.MemberResponseOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MemberResponseOptionRepository extends JpaRepository<MemberResponseOption, Long> {
    List<MemberResponseOption> findByMember_Id(Long memberId);

    @Query("SELECT mro FROM MemberResponseOption mro WHERE mro.member.id = :memberId AND mro.regTime = (SELECT MAX(m.regTime) FROM MemberResponseOption m WHERE m.member.id = :memberId)")
    List<MemberResponseOption> findLatestResponsesByMemberId(@Param("memberId") Long memberId);
}