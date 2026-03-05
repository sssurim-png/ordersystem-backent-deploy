package com.beyond.order_system.member.controller;

import com.beyond.order_system.common.auth.JwtTokenProvider;
import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.dtos.*;
import com.beyond.order_system.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "회원가입", description = "이메일, 비밀번호를 통한 회원가입 "
    )
    public Long create(@RequestBody @Valid MemberCreateDto dto) {
        return memberService.save(dto);
    }

    @PostMapping("/doLogin")
    public MemberTokenDto login(@RequestBody MemberLoginDto dto) {
        return memberService.login(dto);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MemberListDto> findAll() {
        return memberService.findAll();
    }

    @GetMapping("/myinfo")
    public MemberMyInfoDto findMyInfo() {
        return memberService.findMyInfo();
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MemberDetailDto detail(@PathVariable Long id) {
        return memberService.findById(id);
    }

    @PostMapping("/refresh-at")
    public MemberTokenDto refreshAt(@RequestBody RefreshTokenDto dto) {
//        rt 검증(1. 토큰 자체 검증 2. redis 조회 검증)
        Member member=jwtTokenProvider.validateRt(dto.getRefreshToken());

//        at 신규 생성
        String accessToken = jwtTokenProvider.createToken(member);
        return MemberTokenDto.builder().accessToken(accessToken).refreshToken(null).build();
    }
}
