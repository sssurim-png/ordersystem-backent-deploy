package com.beyond.order_system.member.dtos;

import com.beyond.order_system.member.domain.Member;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberCreateDto {
    private String name;
    @NotBlank(message = "이메일은 필수 입력 사항입니다")
    private String email;
    @NotBlank(message = "비밀번호는 필수 입력 사항입니다")
    private String password;

    public Member toEntity(String encodedPassword) {
        return Member.builder()
                .name(this.getName())
                .email(this.getEmail())
                .password(encodedPassword)
                .build();
    }
}
