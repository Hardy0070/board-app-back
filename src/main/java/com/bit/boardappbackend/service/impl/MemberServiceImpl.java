package com.bit.boardappbackend.service.impl;

import com.bit.boardappbackend.dto.MemberDto;
import com.bit.boardappbackend.entity.Member;
import com.bit.boardappbackend.jwt.JwtProvider;
import com.bit.boardappbackend.repository.MemberRepository;
import com.bit.boardappbackend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider; // 로그인을 위해 token을 발행할 수 있는 의존성 추가

    @Override
    public Map<String, String> usernameCheck(String username) {
        Map<String, String> userCheckMsgMap = new HashMap<>();

        long usernameCheck = memberRepository.countByUsername(username);

        if(usernameCheck == 0) // <= DB에 username이 없다면
            userCheckMsgMap.put("usernameCheckMsg", "available username");
        else // <= DB에 username이 있다면
            userCheckMsgMap.put("usernameCheckMsg", "invalid username");

        return userCheckMsgMap;
    }

    @Override
    public Map<String, String> nicknameCheck(String nickname) {
        Map<String, String> nicknameCheckMsgMap = new HashMap<>();

        long nicknameCheck = memberRepository.countByNickname(nickname);

        if(nicknameCheck == 0)
            nicknameCheckMsgMap.put("nicknameCheckMsg", "available nickname");
        else
            nicknameCheckMsgMap.put("nicknameCheckMsg", "invalid nickname");

        return nicknameCheckMsgMap;
    }

    @Override
    public MemberDto join(MemberDto memberDto) {
        memberDto.setRole("ROLE_USER");
        memberDto.setPassword(passwordEncoder.encode(memberDto.getPassword())); // 인코딩된 비번을 보내준다.

        MemberDto joinmemberDto = memberRepository.save(memberDto.toEntity()).toDto(); // save 메소드 매개변수는 Entity이다. 타입 먼저 ()로 맞춰주고 .toDto로 리턴타입 맞춰준다.

        joinmemberDto.setPassword(""); // 비지니스 로직은 서비스에 다 담아준다.

        // 예외처리 // 값이 안 담겼다. 필수값인 이름이 없다. 비밀번호가 없다. 일 때 처리 //
        if (memberDto == null || memberDto.getUsername() == null || memberDto.getPassword() == null) {
            throw new RuntimeException("please check again."); // 에러 메세지로 처리 ->
        }

        return joinmemberDto;
    }

    @Override
    public MemberDto login(MemberDto memberDto) {

        Member member = memberRepository.findByUsername(memberDto.getUsername()).orElseThrow(
                () -> new RuntimeException("username not exist")
        );

        if(passwordEncoder.matches(memberDto.getPassword(), member.getPassword())) {
            throw new RuntimeException("wrong password");
        }

        MemberDto loginMemberDto = member.toDto();

        loginMemberDto.setPassword("");
        loginMemberDto.setToken(jwtProvider.createJwt(member)); // token을 발행해서 보내준다! // member에 token 할당 법

        return loginMemberDto;
    }


}
