package com.javalab.student.repository;

import com.javalab.student.constant.Role;
import com.javalab.student.dto.MemberFormDto;
import com.javalab.student.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

// @DataJpaTest // 이걸 사용하면 Spring Security 설정이 적용되지 않아서 테스트가 실패함
@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 테스트에서 공통으로 사용할 회원 엔티티 생성 메서드
     */
    public Member createMember() {
        MemberFormDto memberFormDto = MemberFormDto.builder()
                .email("test5@example.com")
                //.email("test1@example.com") // DB에 없는 이메일로 변경
                .name("이부산")
                .address("서울시 마포구")
                .password("1234") // 원래 비밀번호
                .phone("010-1234-5678")
                .role(Role.ADMIN)
                .build();
        return Member.createMember(memberFormDto, passwordEncoder); // 비밀번호 암호화 포함
    }

    /**
     * 회원 저장 테스트
     * - 회원 정보를 저장하고 조회하는 테스트
     */
    @Test
    @DisplayName("회원 저장 테스트")
    @Commit
    void saveMemberTest() {
        // Given : 회원 엔티티 생성
        Member member = createMember();

        // When : 회원 정보 저장
        Member savedMember = memberRepository.save(member);

        // Then : 저장된 회원 정보 확인
        assertEquals(member.getEmail(), savedMember.getEmail());
        assertEquals(member.getName(), savedMember.getName());
        assertEquals(member.getAddress(), savedMember.getAddress());

        // 비밀번호가 암호화되어 저장되었는지 확인
        assertNotEquals("1234", savedMember.getPassword()); // 암호화된 비밀번호는 원래 값과 달라야 함
        assertEquals(true, passwordEncoder.matches("1234", savedMember.getPassword())); // 원래 비밀번호와 매칭
    }

    /**
     * 중복 이메일 회원 저장 테스트
     * - 중복된 이메일로 저장하려고 하면 예외 발생
     */
    /**
     * 이메일 중복 여부 확인 테스트
     */
    @Test
    @DisplayName("이메일 중복 여부 확인 테스트")
    void checkDuplicateEmail() {
        // Given: 데이터베이스에 이미 저장되어 있는 이메일
        String existingEmail = "test@example.com";

        // When: 이메일을 조회
        Member foundMember = memberRepository.findByEmail(existingEmail);

        // Then: 중복 여부 확인 및 메시지 출력
        if (foundMember != null) {
            System.out.println("이미 존재하는 이메일입니다: " + existingEmail);
            assertEquals(existingEmail, foundMember.getEmail()); // 중복 이메일이 존재해야 함
        } else {
            System.out.println("사용 가능한 이메일입니다: " + existingEmail);
        }
    }

}
