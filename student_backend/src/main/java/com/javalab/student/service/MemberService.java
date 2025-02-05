package com.javalab.student.service;

import com.javalab.student.dto.LoginFormDto;
import com.javalab.student.dto.MemberFormDto;
import com.javalab.student.entity.Member;
import com.javalab.student.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 처리
     * @param memberFormDto - 클라이언트에서 전달받은 회원가입 데이터
     */
    @Transactional
    public void registerMember(MemberFormDto memberFormDto) {
        // 이메일 중복 체크
        if (memberRepository.findByEmail(memberFormDto.getEmail()) != null) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        // MemberFormDto를 Member 엔티티로 변환
        Member member = Member.createMember(memberFormDto, passwordEncoder);

        // 데이터 저장
        memberRepository.save(member);
    }

    /**
     * 사용자 정보를 ID로 조회
     * @param id - 사용자 ID
     * @return Member 엔티티
     * @throws IllegalArgumentException - 해당 ID의 사용자가 없는 경우 예외 발생
     */
    @Transactional(readOnly = true)
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자를 찾을 수 없습니다."));
    }


    // 사용자 정보 수정 메서드
    public void updateMember(Long id, MemberFormDto memberFormDto) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        member.setName(memberFormDto.getName());
        member.setPhone(memberFormDto.getPhone());
        member.setAddress(memberFormDto.getAddress());

        memberRepository.save(member); // 변경 사항 저장
    }


    /**
     * 이메일 중복 체크
     * @param email - 클라이언트에서 입력받은 이메일
     * @return true(중복) or false(사용 가능)
     */
    public boolean isEmailDuplicate(String email) {
        Member foundMember = memberRepository.findByEmail(email);
        return foundMember != null;
    }

    /**
     * 로그인 처리
     * @param loginForm 로그인 폼 데이터 (이메일, 비밀번호)
     * @return 로그인 성공 여부 (true: 성공, false: 실패)
     */
    public boolean login(LoginFormDto loginForm) {
        // 이메일로 회원 검색
        Member member = memberRepository.findByEmail(loginForm.getEmail());

        // 회원이 존재하지 않거나 비밀번호가 일치하지 않으면 실패
        return member != null && passwordEncoder.matches(loginForm.getPassword(), member.getPassword());

        // 로그인 성공
    }

    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }


    // 로그인 성공 시 마지막 로그인 날짜 업데이트
    @Transactional
    public void updateLastLogin(String email) {
        Member member = memberRepository.findByEmail(email);
        if (member != null) {
            member.updateLastLogin(); // 마지막 로그인 날짜 업데이트
            memberRepository.save(member);
            System.out.println("🔹 [updateLastLogin] 저장 완료: " + member.getLastLoginAt());
        } else {
            System.out.println("⚠ [updateLastLogin] 회원을 찾을 수 없음: " + email);
        }
    }

    // 회원 비활성화 처리
    public void deactivateMember(String email) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("존재하지 않는 이메일입니다.");
        }
        member.deactivateMember(); //  비활성화 및 탈퇴일 저장
        memberRepository.save(member);
    }
    /**
     * 페이징 처리된 회원 목록 반환
     * @param page - 페이지 번호
     * @param size - 한 페이지당 항목 수
     * @return 회원 목록
     */
    public List<Member> getMemberList(int page, int size) {
        Page<Member> memberPage = memberRepository.findAll(PageRequest.of(page, size));
        return memberPage.getContent(); // 현재 페이지에 해당하는 회원 목록 반환
    }

    /**
     * 총 회원 수 반환
     * @return 총 회원 수
     */
    public long getTotalMemberCount() {
        return memberRepository.count(); // 총 회원 수 반환
    }

    @Transactional(readOnly = true)
    public List<Member> findByNameContaining(String name) {
        return memberRepository.findByNameContaining(name);
    }

    @Transactional(readOnly = true)
    public List<Member> findByEmailContaining(String email) {
        return memberRepository.findByEmailContaining(email);
    }

}
