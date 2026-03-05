package com.beyond.order_system.member.dtos;

import com.beyond.order_system.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberMyInfoDto {
    private Long id;
    private String name;
    private String email;

    public static MemberMyInfoDto fromEntity(Member member) {
        return MemberMyInfoDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }
}
