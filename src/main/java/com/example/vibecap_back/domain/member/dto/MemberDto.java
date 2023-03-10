package com.example.vibecap_back.domain.member.dto;

import com.example.vibecap_back.domain.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Member 엔티티에 이미지 파일이 포함되어있을 경우 Member 엔티티를 전달하게 되면 메모리를 많이 사용하게 된다.
 * 이를 막기 위해 MemberDto 사용.
 */
@Getter
@Builder
@AllArgsConstructor
public class MemberDto {
    private Long memberId;
    private String email;
    private String password;
    private String gmail;
    private String role;
    private String nickname;
    private String status;

    public MemberDto(Member member) {
        this.memberId = member.getMemberId();
        this.password = member.getPassword();
        this.gmail = member.getGmail();
        this.role = String.valueOf(member.getRole());
        this.nickname = member.getNickname();
        this.status = String.valueOf(member.getStatus());
    }
}
