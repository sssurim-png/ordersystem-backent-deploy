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
public class MemberTokenDto {
    private String accessToken;
    private String refreshToken;

    public static MemberTokenDto fromEntity(String accessToken, String refreshToken) {
        return MemberTokenDto.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }
}
