package com.beyond.order_system.member.service;

import com.beyond.order_system.common.auth.JwtTokenProvider;
import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.dtos.*;
import com.beyond.order_system.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MemberService {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    @Autowired
    public MemberService(PasswordEncoder passwordEncoder, MemberRepository memberRepository, JwtTokenProvider jwtTokenProvider) {
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Long save(MemberCreateDto dto) {
        Member member=null;
        if(memberRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 email입니다.");
        }
        else {
             member =memberRepository.save(dto.toEntity(passwordEncoder.encode(dto.getPassword())));
        }
        return member.getId();
    }

    public MemberTokenDto login(MemberLoginDto dto) {
        Optional<Member> opt_member = memberRepository.findByEmail(dto.getEmail());
        Member member=opt_member.orElseThrow(()->new EntityNotFoundException("잘못된 입력입니다."));
        String accessToken = jwtTokenProvider.createToken(member);
        String refreshToken = jwtTokenProvider.createRtToken(member);
        return MemberTokenDto.fromEntity(accessToken,refreshToken);
    }

    @Transactional(readOnly = true)
    public List<MemberListDto> findAll() {
        return memberRepository.findAll().stream().map(m->MemberListDto.fromEntity(m)).toList();
    }

    @Transactional(readOnly = true)
    public MemberMyInfoDto findMyInfo() {
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member=memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("다시 시도해주세요."));
        return MemberMyInfoDto.fromEntity(member);
    }

    public MemberDetailDto findById(Long id) {
        Member member=memberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("없는 회원입니다."));
        return MemberDetailDto.fromEntity(member);
    }
}
