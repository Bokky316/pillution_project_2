package com.javalab.student.controller;

import com.javalab.student.config.jwt.TokenProvider;
import com.javalab.student.dto.LoginFormDto;
import com.javalab.student.dto.MemberFormDto;
import com.javalab.student.dto.MemberUpdateDto;
import com.javalab.student.entity.Member;
import com.javalab.student.repository.VerificationCodeRepository;
import com.javalab.student.service.EmailVerificationService;
import com.javalab.student.service.MemberService;
import com.javalab.student.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final VerificationCodeRepository verificationCodeRepository; // 이메일 인증 확인을 위한 리포지토리 추가
    private final EmailVerificationService emailVerificationService; // 이메일 인증을 관리하는 클래스

    /**
     * 회원가입 처리
     * - 사용자가 입력한 이메일이 인증되었는지 확인 후, 회원가입을 진행한다.
     * - 회원가입이 성공하면 인증 정보를 삭제하여 재사용을 방지한다.
     * @param memberFormDto - 클라이언트에서 전송한 회원가입 데이터
     * @return 회원가입 성공 또는 실패 메시지
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerMember(@Valid @RequestBody MemberFormDto memberFormDto) {
        //  이메일 인증 여부 확인 (인증되지 않은 경우 회원가입 불가)
        if (!emailVerificationService.isVerified(memberFormDto.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이메일 인증이 필요합니다.");
        }

        try {
            // 회원가입 진행
            memberService.registerMember(memberFormDto);

            //  회원가입 완료 후 인증 정보 삭제 (재사용 방지)
            emailVerificationService.removeVerified(memberFormDto.getEmail());

            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * ID로 사용자 정보 조회
     * @param id - 사용자 ID
     * @return 사용자 정보 또는 에러 메시지를 포함한 JSON 응답
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMemberById(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Member member = memberService.findById(id); // 사용자 정보 조회
            if (member == null) {
                response.put("status", "error");
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.put("status", "success");
            response.put("data", member); // 사용자 정보를 data 키로 반환
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    /*
        위 메소드의 반환결과가 다음과 같다.
        {
          "status": "success",
          "data": {
            "id": 102,
            "name": "김길동",
            "email": "test2@example.com",
            "phone": "010-2222-3333",
            "address": "서울시 강남구"
            // 기타 사용자 정보
          }
        }
     */

    /**
     * 사용자 정보 수정
     * @param id - 사용자 ID
     * @param memberFormDto - 수정할 사용자 정보
     * @return 성공 또는 실패 메시지를 포함한 JSON 응답
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMember(@PathVariable("id") Long id,
                                                            @Valid @RequestBody MemberFormDto memberFormDto,
                                                            HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            // 1. 사용자 정보 DB 업데이트
            memberService.updateMember(id, memberFormDto);

            // 2. 업데이트된 사용자 정보 조회
            Member updatedMember = memberService.findById(id);
            if (updatedMember == null) {
                responseBody.put("status", "error");
                responseBody.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
            }

            // 3. 새로운 액세스 토큰 생성
            String newAccessToken = tokenProvider.generateToken(
                    updatedMember.getEmail(),
                    //updatedMember.getAuthorities(),
                    //updatedMember.getName(),
                    Duration.ofMinutes(1) // 액세스 토큰 유효 기간
            );

            // 4. 새로운 리프레시 토큰 생성
            String newRefreshToken = tokenProvider.generateRefreshToken(
                    updatedMember.getEmail(),
                    Duration.ofDays(7) // 리프레시 토큰 유효 기간
            );

            // 5. 리프레시 토큰 DB 저장 또는 업데이트
            refreshTokenService.saveOrUpdateRefreshToken(updatedMember.getEmail(), newRefreshToken, updatedMember.getId());

            // 6. 액세스 토큰을 HttpOnly 쿠키로 설정
            Cookie accessTokenCookie = new Cookie("accToken", newAccessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false); // HTTPS 환경에서 true로 설정
            accessTokenCookie.setPath("/");
            response.addCookie(accessTokenCookie);

            // 7. 리프레시 토큰을 HttpOnly 쿠키로 설정
            Cookie refreshTokenCookie = new Cookie("refToken", newRefreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false); // HTTPS 환경에서 true로 설정
            refreshTokenCookie.setPath("/refresh");
            response.addCookie(refreshTokenCookie);

            // 8. 사용자 정보와 상태 반환
            responseBody.put("status", "success");
            responseBody.put("message", "사용자 정보가 수정되고 JWT가 갱신되었습니다.");
            responseBody.put("id", updatedMember.getId());
            responseBody.put("email", updatedMember.getEmail());
            responseBody.put("name", updatedMember.getName());
            responseBody.put("roles", updatedMember.getAuthorities());
            return ResponseEntity.ok(responseBody);
        } catch (IllegalArgumentException e) {
            responseBody.put("status", "error");
            responseBody.put("message", "잘못된 요청: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        } catch (Exception e) {
            responseBody.put("status", "error");
            responseBody.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        }
    }

    /**
     * 사용자 정보 수정 (비밀번호 제외)
     * @param id - 사용자 ID
     * @param memberUpdateDto - 수정할 사용자 정보 (비밀번호 제외)
     * @return 성공 또는 실패 메시지를 포함한 JSON 응답
     */
    @PutMapping("/{id}/update") // 새 엔드포인트 추가
    public ResponseEntity<Map<String, Object>> updateMemberWithoutPassword(
            @PathVariable("id") Long id,
            @Valid @RequestBody MemberUpdateDto memberUpdateDto,
            HttpServletResponse response) {

        Map<String, Object> responseBody = new HashMap<>();
        try {
            // 1. 사용자 정보 DB 업데이트 (비밀번호 제외)
            memberService.updateMemberWithoutPassword(id, memberUpdateDto);

            // 2. 업데이트된 사용자 정보 조회
            Member updatedMember = memberService.findById(id);
            if (updatedMember == null) {
                responseBody.put("status", "error");
                responseBody.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
            }

            // 3. 새로운 액세스 토큰 생성
            String newAccessToken = tokenProvider.generateToken(
                    updatedMember.getEmail(),
                    Duration.ofMinutes(1) // 액세스 토큰 유효 기간
            );

            // 4. 새로운 리프레시 토큰 생성
            String newRefreshToken = tokenProvider.generateRefreshToken(
                    updatedMember.getEmail(),
                    Duration.ofDays(7) // 리프레시 토큰 유효 기간
            );

            // 5. 리프레시 토큰 DB 저장 또는 업데이트
            refreshTokenService.saveOrUpdateRefreshToken(updatedMember.getEmail(), newRefreshToken, updatedMember.getId());

            // 6. 액세스 토큰을 HttpOnly 쿠키로 설정
            Cookie accessTokenCookie = new Cookie("accToken", newAccessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false); // HTTPS 환경에서 true로 설정
            accessTokenCookie.setPath("/");
            response.addCookie(accessTokenCookie);

            // 7. 리프레시 토큰을 HttpOnly 쿠키로 설정
            Cookie refreshTokenCookie = new Cookie("refToken", newRefreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false); // HTTPS 환경에서 true로 설정
            refreshTokenCookie.setPath("/refresh");
            response.addCookie(refreshTokenCookie);

            // 8. 사용자 정보와 상태 반환
            responseBody.put("status", "success");
            responseBody.put("message", "사용자 정보가 수정되고 JWT가 갱신되었습니다.");
            responseBody.put("id", updatedMember.getId());
            responseBody.put("email", updatedMember.getEmail());
            responseBody.put("name", updatedMember.getName());
            responseBody.put("roles", updatedMember.getAuthorities());
            return ResponseEntity.ok(responseBody);
        } catch (IllegalArgumentException e) {
            responseBody.put("status", "error");
            responseBody.put("message", "잘못된 요청: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        } catch (Exception e) {
            responseBody.put("status", "error");
            responseBody.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        }
    }



    /**
     * 이메일 중복 체크
     * @param email - 클라이언트에서 입력받은 이메일
     * @return 중복 여부 (200 OK로 항상 응답, 상태 값으로 판단)
     */
    @GetMapping("/checkEmail")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam("email") String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isDuplicate = memberService.isEmailDuplicate(email);

            if (isDuplicate) {
                response.put("status", "duplicate");
                response.put("message", "이미 존재하는 이메일입니다.");
            } else {
                response.put("status", "available");
                response.put("message", "사용 가능한 이메일입니다.");
            }

            return ResponseEntity.ok(response); // 항상 200 OK 반환
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "이메일 중복 체크 중 오류 발생: " + e.getMessage());
            return ResponseEntity.ok(response); // 오류도 200 OK로 응답
        }
    }



    /**
     * 로그인 처리[미사용-일반 시큐리티 로그인]
     * @param loginForm - 클라이언트에서 전송한 로그인 데이터
     * @return 성공 메시지 또는 에러 메시지
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginMember(@RequestBody LoginFormDto loginForm) {
        Map<String, String> response = new HashMap<>();

        // 로그인 성공 여부 확인
        boolean isLoginSuccessful = memberService.login(loginForm);

        if (isLoginSuccessful) {
            response.put("message", "로그인 성공");
            response.put("status", "success");
            return ResponseEntity.ok(response); // HTTP 200 OK
        }

        // 로그인 실패 처리
        response.put("message", "로그인 실패");
        response.put("status", "failed");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // HTTP 401 Unauthorized
    }

    /**
     * 회원 탈퇴 API 추가
     */
    @PostMapping("/deactivate")
    public ResponseEntity<String> deactivateMember(@RequestParam String email) {
        memberService.deactivateMember(email);
        return ResponseEntity.ok("회원 탈퇴 완료");
    }

    /**
     * 파비콘 요청 무시
     */
    @GetMapping("/favicon.ico")
    @ResponseBody
    void disableFavicon() {
        // 아무 작업도 하지 않음
    }
    /**
     * 회원 목록 조회 (페이징 처리)
     * @param page - 페이지 번호
     * @param size - 한 페이지에 보여줄 항목 수
     * @return 회원 목록 및 총 회원 수를 포함한 JSON 응답
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMemberList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "status", required = false) String status) {

        Map<String, Object> response = new HashMap<>();
        try {
            Page<Member> memberPage;

            // 상태별로 회원 조회
            if ("ACTIVE".equalsIgnoreCase(status)) {
                memberPage = memberService.getMembersByActivate(true, page, size); // activate=1
            } else if ("DELETED".equalsIgnoreCase(status)) {
                memberPage = memberService.getMembersByActivate(false, page, size); // activate=0
            } else {
                memberPage = memberService.getMemberList(page, size); // 전체 조회
            }

            response.put("status", "success");
            response.put("data", memberPage.getContent());
            response.put("total", memberPage.getTotalElements());
            response.put("totalPages", memberPage.getTotalPages());
            response.put("currentPage", memberPage.getNumber());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 회원 검색 (상태별 검색 포함)
     * @param type - 검색 유형 (name, email)
     * @param value - 검색 값
     * @param status - 회원 상태 (ACTIVE, DELETED)
     * @return 검색 결과
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchMembers(
            @RequestParam(name = "type") String type,
            @RequestParam(name = "value") String value,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Map<String, Object> response = new HashMap<>();
        try {
            Page<Member> memberPage;

            if ("name".equalsIgnoreCase(type)) {
                if ("ACTIVE".equalsIgnoreCase(status)) {
                    memberPage = memberService.findByNameAndActivate(value, true, page, size);
                } else if ("DELETED".equalsIgnoreCase(status)) {
                    memberPage = memberService.findByNameAndActivate(value, false, page, size);
                } else {
                    memberPage = memberService.findByNameContaining(value, page, size);
                }
            } else if ("email".equalsIgnoreCase(type)) {
                if ("ACTIVE".equalsIgnoreCase(status)) {
                    memberPage = memberService.findByEmailAndActivate(value, true, page, size);
                } else if ("DELETED".equalsIgnoreCase(status)) {
                    memberPage = memberService.findByEmailAndActivate(value, false, page, size);
                } else {
                    memberPage = memberService.findByEmailContaining(value, page, size);
                }
            } else {
                response.put("status", "error");
                response.put("message", "잘못된 검색 유형입니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            response.put("status", "success");
            response.put("data", memberPage.getContent());
            response.put("total", memberPage.getTotalElements());
            response.put("totalPages", memberPage.getTotalPages());
            response.put("currentPage", memberPage.getNumber());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }







}
