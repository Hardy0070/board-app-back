package com.bit.boardappbackend.jwt;

import com.bit.boardappbackend.entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

// JWT를 발행하고 받아온 JWT가 유효한지 검사하는 클래스
@Component
public class JwtProvider {
    // JWT의 signature 부분이 될 서명 키 선언
    // bitcampdevops12todobootapp502reactspringboot를 Base64 인코딩한 값이다.
    private static final String SECRET_KEY = "Yml0Y2FtcGRldm9wczEydG9kb2Jvb3RhcHA1MDJyZWFjdHNwcmluZ2Jvb3Q=";

    // SECRET_KEY를 KEY 객체로 변환
    SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    // 사용자 정보를 받아서 JWT를 발행하는 메소드
    public String createJwt(Member member) {
        // 토큰 만료일 생성
        Date expireDate = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));

        // JWT 생성하여 리턴
        return Jwts.builder()
                // JWT header의 알고리즘과 JWT의 signatrue 지정
                .signWith(key, SignatureAlgorithm.HS256) // 오른쪽이 알고리즘
                // payload 부분 생성
                // sub(subject: 토큰의 주인) // 토큰 주인 설정하는 기능
                .subject(member.getUsername())
                // iss(issuer: 토큰의 발행 주체) // 설정
                .issuer("todo app backend")
                // isa(issuedAt: 토큰의 발행 일자)
                .issuedAt(new Date())
                // exp(expiration: 토근의 만료 일자)
                .expiration(expireDate)
                .compact();
    }

    // 받아온 JWT 유효성을 검사하고 유효한 JWT일 경우 토큰의 주인(subject(username))을 리턴하는 메소드
    public String validateAndGetSubject(String token) {
        Claims claims = Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(token) // 토큰과 지정한 시그니쳐가 같은지 변조가 되었는지 확인 가능
                            .getPayload(); // 검사가 유효하다면 클레임즈 부분만 꺼내서 객체로 생성할 수 있다.

        return claims.getSubject();
    }
}
