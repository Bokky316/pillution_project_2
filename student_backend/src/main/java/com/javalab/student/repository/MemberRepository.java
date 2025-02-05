package com.javalab.student.repository;

import com.javalab.student.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmail(String email);
    List<Member> findByNameContaining(String name);
    List<Member> findByEmailContaining(String email);

}
