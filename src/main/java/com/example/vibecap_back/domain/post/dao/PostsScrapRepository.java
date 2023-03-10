package com.example.vibecap_back.domain.post.dao;

import com.example.vibecap_back.domain.member.domain.Member;
import com.example.vibecap_back.domain.post.domain.Post;
import com.example.vibecap_back.domain.post.domain.Scrap.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostsScrapRepository extends JpaRepository<Scrap, Long> {
    Optional<Scrap> findByPostAndMember(Post post, Member member);
}
