package com.javalab.student.dto;

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

    private String name;

    private String email;

    private String address;

    private String phone;

    private LocalDate birthDate;
    private String gender;
    private boolean activate;
}