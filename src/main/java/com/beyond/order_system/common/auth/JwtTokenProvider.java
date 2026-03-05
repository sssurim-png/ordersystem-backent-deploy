package com.beyond.order_system.common.auth;

import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {
    //    중요 정보의 경우 application.yml에 저장. Value를 통해 주입
    @Value("${jwt.secretKey}")
    private String st_secret_key;
    @Value("${jwt.expiration}")
    private int expiration;
    //    인코딩된 문자열 -> 디코딩된 문자열 -> HS512알고리즘으로 암호화
    private Key secret_key;

    @Value("${jwt.secretKeyRt}")
    private String st_secret_key_rt;
    @Value("${jwt.expirationRt}")
    private int expirationRt;
    private Key secret_key_rt;

    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;

    @Autowired
    public JwtTokenProvider(@Qualifier("rtInventory") RedisTemplate<String, String> redisTemplate, MemberRepository memberRepository) {
        this.redisTemplate = redisTemplate;
        this.memberRepository = memberRepository;
    }

    //    생성자 호출 이후에 아래 메서드를 실행하게 함으로써, @Value시점보다 늦게 실행되게하여 문제 해결
    @PostConstruct
    public void init() {
        secret_key = new SecretKeySpec(Base64.getDecoder().decode(st_secret_key), SignatureAlgorithm.HS512.getJcaName());
        secret_key_rt = new SecretKeySpec(Base64.getDecoder().decode(st_secret_key_rt), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createToken(Member member) {
//        sub : abc@naver.com형태
        Claims claims = Jwts.claims().setSubject(member.getEmail());
//        주된 키값을 제외한 나머지 정보는 put을 사용하여 key:value세팅
        claims.put("role", member.getRole().toString());
//        ex) claims.put("age",author.getAge()); 형태 가능
        Date now = new Date();
//        토큰의 구성요소 : 헤더, 페이로드, 시그니처(서명부)
        String token = Jwts.builder()
//                아래 3가지 요소는 페이로드
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration * 60 * 1000L)) // 30분을 ms형태로 변환
//                secret를 통해 서명값(signature) 생성
                .signWith(secret_key)
                .compact();
        return token;
    }

    public String createRtToken(Member member) {
//        유효기간이 긴 rt토큰 생성
        Claims claims = Jwts.claims().setSubject(member.getEmail());
        claims.put("role", member.getRole().toString());
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationRt * 60 * 1000L))
                .signWith(secret_key_rt)
                .compact();
//        rt토큰을 redis에 저장
//        opsForValue : 일반 스트링 자료구조, opsForSet(또는 Zset 또는 List등) 존재
//        redisTemplate.opsForValue().set(member.getEmail(), token);
        redisTemplate.opsForValue().set(member.getEmail(), token, expirationRt, TimeUnit.MINUTES);  // 3000분 TTL
        return token;
    }

    public Member validateRt(String refreshToken) {
        Claims claims = null;
//        rt토큰 자체 검증
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(st_secret_key_rt)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 토큰입니다. ");
        }
        String email = claims.getSubject();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("Entity is not found"));
//        redis rt와 비교 검증
        String redisRt = redisTemplate.opsForValue().get(email);
        if (!redisRt.equals(refreshToken)) {
            throw new IllegalArgumentException("잘못된 토큰입니다. ");
        }
        return member;
    }
}
