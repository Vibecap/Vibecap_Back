package com.example.vibecap_back.domain.member.dao;

import com.example.vibecap_back.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    public Optional<Member> findByEmail(String email);
    
    public Optional<Member> findByNickname(String nickname);

    public Optional<Member> findByMemberId(Long Id);
}
