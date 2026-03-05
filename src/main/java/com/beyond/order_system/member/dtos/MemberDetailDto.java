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
public class MemberDetailDto {
    private Long id;
    private String name;
    private String email;

    public static MemberDetailDto fromEntity(Member member) {
        return MemberDetailDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }
}
