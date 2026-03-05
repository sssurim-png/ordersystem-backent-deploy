package com.beyond.order_system.common.init;

import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.domain.Role;
import com.beyond.order_system.member.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class InitialDataLoad implements CommandLineRunner {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialDataLoad(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if(memberRepository.findByEmail("admin@naver.com").isPresent()) return;
        memberRepository.save(Member.builder()
                .name("admin")
                .email("admin@naver.com")
                .role(Role.ADMIN)
                .password(passwordEncoder.encode("12341234")).build());
    }
}
