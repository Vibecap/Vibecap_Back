package com.example.vibecap_back.domain.vibe.application;

import com.example.vibecap_back.domain.member.dao.MemberRepository;
import com.example.vibecap_back.domain.member.domain.Member;
import com.example.vibecap_back.domain.model.ExtraInfo;
import com.example.vibecap_back.domain.vibe.api.VibeCapture;
import com.example.vibecap_back.domain.vibe.dao.VibeRepository;
import com.example.vibecap_back.domain.vibe.domain.Vibe;
import com.example.vibecap_back.domain.vibe.dto.CaptureResult;
import com.example.vibecap_back.domain.vibe.exception.ExternalApiException;
import com.example.vibecap_back.domain.vibe.exception.NoProperVideoException;
import com.example.vibecap_back.global.config.storage.FileSaveErrorException;
import com.example.vibecap_back.global.config.storage.FireBaseService;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class VibeService {

    private final Logger LOGGER = LoggerFactory.getLogger(VibeCapture.class);
    private final VibeRepository vibeRepository;
    private ImageAnalyzer imageAnalyzer;
    private PlaylistSearchEngine playlistSearchEngine;
    private TextTranslator textTranslator;
    private final VideoQuery videoQuery;
    private final FireBaseService fireBaseService;
    private final MemberRepository memberRepository;

    @Autowired
    public VibeService(ImageAnalyzer imageAnalyzer, PlaylistSearchEngine playlistSearchEngine,
                       VideoQuery videoQuery, VibeRepository vibeRepository,
                       TextTranslator textTranslator, FireBaseService fireBaseService,
                       MemberRepository memberRepository) {
        this.imageAnalyzer = imageAnalyzer;
        this.playlistSearchEngine = playlistSearchEngine;
        this.videoQuery = videoQuery;
        this.vibeRepository = vibeRepository;
        this.textTranslator = textTranslator;
        this.fireBaseService = fireBaseService;
        this.memberRepository = memberRepository;
    }

    /**
     * ????????? ?????? ????????? ???????????? vibe??? ??????, ???????????? ?????? ??????
     * @param memberId
     * @param imageFile
     * @param extraInfo
     * @return
     * @throws ExternalApiException
     * @throws IOException
     */
    @Transactional
    public CaptureResult capture(Long memberId, MultipartFile imageFile, ExtraInfo extraInfo)
            throws ExternalApiException, IOException, NoProperVideoException, FileSaveErrorException {

        byte[] data = imageFile.getBytes();
        String label;
        String query;
        String videoId;
        String videoLink;
        String keywords;
        Long vibeId;

        // ???????????? ???????????? ?????? ??????
        label = imageAnalyzer.detectLabelsByWebReference(data);
        LOGGER.warn("[GOOGLE_VISION] ?????????????????? ????????? label: " + label);
        // label????????? youtube query ??????
        query = videoQuery.assemble(extraInfo, label);
        // query ?????? ??????
        videoId = selectTheFirstVideo(playlistSearchEngine.searchVideos(query));
        videoLink = getFullUrl(videoId);
        // ????????? ????????? firebase storage??? ??????
        String imgUrl = fireBaseService.uploadFiles(imageFile);
        // ????????? vibe??? DB??? ??????
        keywords = label + extraInfo.toString();
        vibeId = saveVibe(memberId, imgUrl, videoLink, keywords);

        CaptureResult result = CaptureResult.builder()
                .keywords(keywords.split(" "))
                .youtubeLink(videoLink)
                .videoId(videoId)
                .vibeId(vibeId)
                .build();

        return result;
    }

    /**
     * ????????? ???????????? vibe ??????, ???????????? ?????? ??????
     * @param memberId
     * @param imageFile
     * @return
     * @throws ExternalApiException
     * @throws IOException
     */
    @Transactional
    public CaptureResult capture(Long memberId, MultipartFile imageFile)
            throws ExternalApiException, IOException, NullPointerException, NoProperVideoException, FileSaveErrorException {

        if (memberId == null || imageFile == null)
            throw new NullPointerException("empty request");

        byte[] data = imageFile.getBytes();
        String label;
        String query;
        String videoId;
        String videoLink;
        String[] keywords = new String[1];
        Long vibeId;

        // ???????????? ???????????? ?????? ??????
        label = imageAnalyzer.detectLabelsByWebReference(data);
        keywords[0] = label;
        // query ??????
        query = videoQuery.assemble(label);
        videoId = selectTheFirstVideo(playlistSearchEngine.searchVideos(query));
        videoLink = getFullUrl(videoId);
        // ????????? ????????? firebase storage??? ??????
        String imgUrl = fireBaseService.uploadFiles(imageFile);
        // vibe??? DB??? ??????
        vibeId = saveVibe(memberId, imgUrl, videoLink, label);

        CaptureResult result = CaptureResult.builder()
                .keywords(keywords)
                .youtubeLink(videoLink)
                .videoId(videoId)
                .vibeId(vibeId)
                .build();

        return result;
    }

    /**
     * ??????????????? ???????????? ?????? ??????.
     * vibe??? ???????????? ?????????.
     * @param extraInfo
     * @return
     */
    @Transactional
    public CaptureResult capture(ExtraInfo extraInfo) throws ExternalApiException, NoProperVideoException {
        String query;
        String videoId;
        String videoLink;
        CaptureResult result;
        String[] keywords = new String[1];

        query = videoQuery.assemble(extraInfo);
        keywords[0] = query;
        videoId = selectTheFirstVideo(playlistSearchEngine.searchVideos(query));
        videoLink = getFullUrl(videoId);

        result = CaptureResult.builder()
                .keywords(keywords)
                .youtubeLink(videoLink)
                .videoId(videoId)
                .build();


        return result;
    }

    /**
     * ????????? ???????????? ????????? vibe??? ?????? ??????.
     * @param memberId
     * @param image
     * @param link
     * @param keywords
     * @return
     * ????????? vibe??? id???
     */
    private Long saveVibe(Long memberId, String image, String link, String keywords) {
        Optional<Member> author;
        author = this.memberRepository.findById(memberId);
        Vibe vibe = Vibe.builder()
                .member(author.get())
                .vibeImage(image)
                .youtubeLink(link)
                .vibeKeywords(keywords)
                .build();
        return vibeRepository.save(vibe).getVibeId();
    }

    /**
     * ?????? ???????????? ???????????? ?????? ??????
     * @param videoId
     * @return
     */
    private String getFullUrl(String videoId) {
        return  String.format("https://www.youtube.com/watch?v=%s", videoId);
    }

    /**
     * NUMBER_OF_VIDEOS_RETURNED ?????? ????????? ??? ????????? ????????? ????????? ???????????????.
     * @param searchResultList
     * @return
     * key: "link", "videoId"
     */
    private String selectRandomVideo(List<SearchResult> searchResultList) {
        // ?????? ????????? ?????? ????????? ???????????? ?????? ????????? ?????????
        Random random = new Random(new Date().getTime());
        int randomIdx = random.nextInt(searchResultList.size());

        // ????????? ????????? 1??? ??????
        SearchResult singleVideo = searchResultList.get(randomIdx);
        ResourceId rId = singleVideo.getId();

        return rId.getVideoId();
    }

    /**
     * ????????? ?????? ??? ??? ?????? ????????? ????????? ??????.
     * @param searchResultList
     * @return
     */
    private String selectTheFirstVideo(List<SearchResult> searchResultList) {
        return searchResultList.get(0).getId().getVideoId();
    }

    /**
     * tester client??? ???????????? ?????????
     */
    // ???????????? + ??????
    public List<String> getEveryVideos(ExtraInfo extraInfo, MultipartFile image)
            throws ExternalApiException, IOException, NoProperVideoException {
        String query;
        String label;
        List<SearchResult> videos;
        List<String> results = new ArrayList<>();
        // ????????? ??????
        label = imageAnalyzer.detectLabelsByWebReference(image.getBytes());
        query = videoQuery.assemble(extraInfo, label);
        // ??????
        videos = playlistSearchEngine.searchVideos(query);
        for (SearchResult videoInfo : videos) {
            String link = getFullUrl(videoInfo.getId().getVideoId());
            results.add(link);
        }

        return results;
    }

    // ????????????
    public List<String> getEveryVideos(ExtraInfo extraInfo)
            throws ExternalApiException, IOException, NoProperVideoException {
        String query;
        List<SearchResult> videos;
        List<String> results = new ArrayList<>();
        // ????????? ??????
        query = videoQuery.assemble(extraInfo);
        // ??????
        videos = playlistSearchEngine.searchVideos(query);
        for (SearchResult videoInfo : videos) {
            String link = getFullUrl(videoInfo.getId().getVideoId());
            results.add(link);
        }

        return results;
    }

    // ??????
    public List<String> getEveryVideos(MultipartFile image)
            throws ExternalApiException, IOException, NoProperVideoException {
        String query;
        String label;
        List<SearchResult> videos;
        List<String> results = new ArrayList<>();
        // ????????? ??????
        label = imageAnalyzer.detectLabelsByWebReference(image.getBytes());
        query = videoQuery.assemble(label);
        // ??????
        videos = playlistSearchEngine.searchVideos(query);
        for (SearchResult videoInfo : videos) {
            String link = getFullUrl(videoInfo.getId().getVideoId());
            results.add(link);
        }

        return results;
    }
}
