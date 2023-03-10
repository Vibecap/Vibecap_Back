package com.example.vibecap_back.domain.notice;

import com.example.vibecap_back.BoilerPlate;
import com.example.vibecap_back.domain.comment.dao.CommentRepository;
import com.example.vibecap_back.domain.comment.domain.Comments;
import com.example.vibecap_back.domain.member.dao.MemberRepository;
import com.example.vibecap_back.domain.member.domain.Member;
import com.example.vibecap_back.domain.notice.application.NoticeManager;
import com.example.vibecap_back.domain.notice.dao.NoticeCommentRepository;
import com.example.vibecap_back.domain.notice.dao.NoticeLikeRepository;
import com.example.vibecap_back.domain.notice.dao.NoticeSubCommentRepository;
import com.example.vibecap_back.domain.notice.domain.NoticeComment;
import com.example.vibecap_back.domain.notice.domain.NoticeLike;
import com.example.vibecap_back.domain.post.dao.PostsRepository;
import com.example.vibecap_back.domain.post.domain.Like.Likes;
import com.example.vibecap_back.domain.post.domain.Post;
import com.example.vibecap_back.domain.vibe.dao.VibeRepository;
import com.example.vibecap_back.domain.vibe.domain.Vibe;
import com.example.vibecap_back.factory.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Repository layer test
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Commit
public class NoticeManagerTest {

    @Autowired
    private NoticeCommentRepository commentNoticeRepository;
    @Autowired
    private NoticeSubCommentRepository subCommentNoticeRepository;
    @Autowired
    NoticeLikeRepository likeNoticeRepository;
    @Autowired
    private PostsRepository postsRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private VibeRepository vibeRepository;
    @Autowired
    private CommentRepository commentRepository;
    private NoticeManager noticeManager;

    // test members (id : 1~TEST_MEMBER_NUM)
    static final int TEST_MEMBER_NUM = 10;
    // test vibes
    static final int TEST_VIBE_PER_MEMBER = 5;

    BoilerPlate boilerPlate;
    List<Member> testMembers;
    Map<Long, List<Vibe>> testVibes;
    // test posts
    Map<Long, List<Post>> testPosts;

    @PostConstruct
    void initNoticeManager() {
        noticeManager = new NoticeManager(commentNoticeRepository,
                subCommentNoticeRepository, likeNoticeRepository);
    }
    @BeforeEach
    void initPosts() throws Exception {
        boilerPlate = new BoilerPlate(TEST_MEMBER_NUM, TEST_VIBE_PER_MEMBER);
        testMembers = boilerPlate.getMembers();
        testVibes = boilerPlate.getVibes();
        testPosts = boilerPlate.getPosts();
        boilerPlate.persist(memberRepository, vibeRepository, postsRepository, TEST_VIBE_PER_MEMBER, true);
    }

    @Test
    void ?????????_??????_??????_??????_?????????() {
        // given
        List<Likes> testLikes;
        Likes like;
        int index;          // ???????????? ?????? member??? index (0~TEST_MEMBER_NUM-1)
        int targetIdx;      // ???????????? ????????? ????????? ????????? index (1~TEST_MEMBER_NUM-1, 0)
        List<Post> targetPosts;     // targetId??? ?????? ?????????
        // when : testMembers[index]??? testMembers[targetIndex]??? ?????? ???????????? ???????????? ?????????.
        for (int i=0; i<TEST_MEMBER_NUM; i++) {
            targetIdx = ((i+1) % TEST_MEMBER_NUM);
            targetPosts = testPosts.get(testMembers.get(targetIdx).getMemberId());
            for (Post post : targetPosts) {
                like = LikeFactory.getLike(testMembers.get(i), post);
                noticeManager.sendNotice(like);
            }
        }
        // then : ???????????? ?????? ????????? ????????? sender ????????? ??????
        List<NoticeLike> actualNotices;
        Member receiver, expectedSender;
        for (int i=0; i<TEST_MEMBER_NUM; i++) {
            receiver = testMembers.get((i+1)%TEST_MEMBER_NUM);
            expectedSender = testMembers.get(i);
            // receiver??? ?????? ?????? ????????? ????????? ??????
            actualNotices = likeNoticeRepository.selectAllLikeNotices(receiver.getMemberId());
            for (NoticeLike actualNotice : actualNotices) {
                Assertions.assertThat(actualNotice.getSenderNickname()).isEqualTo(expectedSender.getNickname());
            }
        }
    }

    @Test
    void ??????_??????_??????_??????_?????????() {
        // given
        List<Comments> testComments;
        Comments comment;
        int index, targetIndex;         // ????????? ????????? ??????, ????????? ????????? ????????? ?????????
        List<Post> targetPosts;
        // when : testMember[index]??? testMember[targetIndex]??? ???????????? ????????? ????????????.
        for (int i=0; i<TEST_MEMBER_NUM; i++) {
            targetIndex = ((i+1)%TEST_MEMBER_NUM);
            targetPosts = testPosts.get(testMembers.get(targetIndex).getMemberId());
            for (Post post : targetPosts) {
                comment = CommentFactory.getComment(testMembers.get(i), post, i);
                commentRepository.save(comment);
                noticeManager.sendNotice(comment);
            }
        }
        // then
        List<NoticeComment> actualNotices;
        Member receiver, expectedSender;
        for (int i=0; i<TEST_MEMBER_NUM; i++) {
            receiver = testMembers.get((i+1)%TEST_MEMBER_NUM);
            expectedSender = testMembers.get(i);
            // receiver??? ?????? ????????? ????????? ??????
            actualNotices = commentNoticeRepository.selectAllCommentNotices(receiver.getMemberId());
            for (NoticeComment noticeComment : actualNotices) {
                Assertions.assertThat(noticeComment.getSenderNickname()).isEqualTo(expectedSender.getNickname());
            }
        }
    }

}
