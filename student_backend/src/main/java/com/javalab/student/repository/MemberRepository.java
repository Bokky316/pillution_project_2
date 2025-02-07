package com.javalab.student.repository;

import com.javalab.student.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmail(String email);
    List<Member> findByNameContaining(String name);
    List<Member> findByEmailContaining(String email);
    Page<Member> findByActivate(boolean activate, Pageable pageable);
    Page<Member> findByNameContaining(String name, Pageable pageable);
    Page<Member> findByEmailContaining(String email, Pageable pageable);
    Page<Member> findByNameContainingAndActivate(String name, boolean activate, Pageable pageable);
    Page<Member> findByEmailContainingAndActivate(String email, boolean activate, Pageable pageable);

}
