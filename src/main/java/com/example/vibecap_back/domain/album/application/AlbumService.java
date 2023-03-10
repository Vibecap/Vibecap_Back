package com.example.vibecap_back.domain.album.application;

import com.example.vibecap_back.domain.album.dao.AlbumRepository;
import com.example.vibecap_back.domain.album.domain.Album;
import com.example.vibecap_back.domain.album.dto.GetAlbumResponse;
import com.example.vibecap_back.domain.album.dto.GetVibeResponse;
import com.example.vibecap_back.domain.album.exception.NoAccessToVibeException;
import com.example.vibecap_back.domain.member.domain.Member;
import com.example.vibecap_back.domain.mypage.application.MyPageService;
import com.example.vibecap_back.domain.mypage.dao.MyPageRepository;
import com.example.vibecap_back.domain.post.application.PostService;
import com.example.vibecap_back.domain.post.dao.PostsRepository;
import com.example.vibecap_back.domain.post.domain.Post;
import com.example.vibecap_back.domain.post.domain.Tag.Tag;
import com.example.vibecap_back.domain.vibe.domain.Vibe;
import com.example.vibecap_back.global.common.response.BaseException;
import com.example.vibecap_back.global.config.security.JwtTokenProvider;
import com.example.vibecap_back.global.config.storage.FireBaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AlbumService {

    private final Logger LOGGER = LoggerFactory.getLogger(MyPageService.class);

    private final FireBaseService fireBaseService;
    private final PostService postService;
    private final AlbumRepository albumRepository;
    private final MyPageRepository myPageRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AlbumService(FireBaseService fireBaseService,
                        PostService postService,
                        AlbumRepository albumRepository,
                        MyPageRepository myPageRepository, JwtTokenProvider jwtTokenProvider) {
        this.fireBaseService = fireBaseService;
        this.postService = postService;
        this.albumRepository = albumRepository;
        this.myPageRepository = myPageRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    // ?????? ??????
    public GetAlbumResponse getAlbum(Long memberId) throws BaseException {
        Optional<Member> optionalMember = myPageRepository.findById(memberId);
        Member member = optionalMember.get();

        List<Vibe> myVibe = albumRepository.findByMemberId(member.getMemberId());
        List<Album> vibes = new ArrayList<>();

        for (Vibe vibe : myVibe) {
            vibes.add(new Album(vibe.getVibeId(), vibe.getVibeImage()));
        }

        return new GetAlbumResponse(member.getNickname(), member.getEmail(), member.getGmail(), vibes);
    }

    // ???????????? ?????? Vibe ??????
    public GetVibeResponse getVibe(Long vibeId) throws BaseException, NoAccessToVibeException {
        checkAccessToVibe(vibeId);

        Optional<Vibe> optionalVibe = albumRepository.findById(vibeId);
        Vibe vibe = optionalVibe.get();

        return new GetVibeResponse(vibe.getVibeId(), vibe.getMember().getMemberId(), vibe.getVibeImage(),
                vibe.getYoutubeLink(), vibe.getVibeKeywords());
    }

    // ???????????? ?????? Vibe ??????
    @Transactional
    public void deleteVibe(Long vibeId) throws BaseException, NoAccessToVibeException, IOException {
        checkAccessToVibe(vibeId);
        Optional<Vibe> optionalVibe = albumRepository.findById(vibeId);
        Vibe vibe = optionalVibe.get();
        // tag ????????? ?????? post_id ??????
        Long targetPostId = albumRepository.getPostIdByVibeId(vibeId);
        albumRepository.deleteTagByPostId(targetPostId);

        // firebase ?????? ?????? ??????
        String fileName = fireBaseService.getFileName(vibe.getVibeImage());
        fireBaseService.delete(fileName);

        albumRepository.deleteById(vibeId);
    }

    // ????????? vibe ??? ?????? ????????? ???????????? ??????
    public void checkAccessToVibe(Long vibeId) throws NoAccessToVibeException {
        // JWT ?????? email ??????
        String email = jwtTokenProvider.extractEmail();
        Optional<Member> optionalMember = myPageRepository.findByEmail(email);
        Member member = optionalMember.get();

        // memberId ???????????? ????????? ????????? vibe ?????? ??????
        List<Long> vibeIdList = albumRepository.findVibeIdByMemberId(member.getMemberId());

        if (!vibeIdList.contains(vibeId)) {
            throw new NoAccessToVibeException();
        }
    }
}