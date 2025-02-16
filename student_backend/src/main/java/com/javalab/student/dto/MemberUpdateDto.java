package com.javalab.student.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateDto {

    @NotBlank(message = "이름을 입력해주세요.")  // 공백도 안되고 null도 안됨
    private String name;

    @NotBlank(message = "이메일을 입력해주세요.") // 공백도 안되고 null도 안됨
    @Email(regexp = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$", message = "유효한 이메일 형식으로 입력해주세요.")
    private String email;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private String confirmPassword;

    @NotBlank(message = "연락처를 입력하세요.") // 공백도 안되고 null도 안됨
    @Pattern(
            regexp = "^010-\\d{4}-\\d{4}$",
            message = "Phone number should be in the format 010-XXXX-XXXX"
    )
    private String phone;

    private LocalDate birthDate;
    private String gender;
    private boolean activate;

    // 주소 관련 필드 추가
    @NotBlank(message = "우편번호를 입력해주세요.")
    private String postalCode;

    @NotBlank(message = "도로명 주소를 입력해주세요.")
    private String roadAddress;

    @NotBlank(message = "상세 주소를 입력해주세요.")
    private String detailAddress;


}