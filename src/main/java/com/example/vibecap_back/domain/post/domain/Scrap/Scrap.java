package com.example.vibecap_back.domain.post.domain.Scrap;

import com.example.vibecap_back.domain.member.domain.Member;
import com.example.vibecap_back.domain.post.domain.BaseTimeEntity;
import com.example.vibecap_back.domain.post.domain.Post;
import lombok.*;

import javax.persistence.*;
import java.util.Optional;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="post_scrap")
public class Scrap extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scrap_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "FK_PostScrap_Member"))
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(name = "FK_PostScrap_Post"))
    private Post post;

    public static boolean isVotedPost(Optional<Scrap> optionalPostScrap) {
        return optionalPostScrap.isPresent();
    }

    public void mappingMember(Member member) {
        this.member = member;
        member.mappingPostScrap(this);
    }

    public void mappingPost(Post post) {
        this.post = post;
        post.mappingPostScrap(this);
    }
}
