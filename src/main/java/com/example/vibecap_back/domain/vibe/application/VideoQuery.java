package com.example.vibecap_back.domain.vibe.application;

import com.example.vibecap_back.domain.model.ExtraInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * query는 세 가지 종류 존재한다.
 * 1. 추가정보 + label => query
 * 2. 사진 => query
 * 3. 추가정보 => query
 * 추가정보는 "{season} + {time} + {feeling}" 형태의 문자열
 *
 * TODO String을 생성하는 utility class가 아닌 query를 필드로 가지고 다양한 기능을 제공하는 클래스로 수정
 */
@Component
public class VideoQuery {

    private String[] timeList = {
            "아침", "낮", "저녁", "밤", "새벽"
    };
    // TODO 감정 단순화
    private String[] feelingList = {
            "신나는", "포근한", "신선한", "낭만적인", "잔잔한", "우울한",
            "공허한", "분노한", "심심한"
    };

    // TODO enum type으로 리팩토링
    private static String PLAYLIST_KR = "플레이리스트";
    private static String SONG_KR = "노래";
    private static String MUSIC_KR = "음악";
    private static String PLAYLIST_EN = "playlist";

    /**
     * 사진과 추가 정보 모두 사용해서 query 생성
     * @param extraInfo
     * season, time, feeling
     * @param label
     * 이미지에서 추출한 label
     * @return
     */
    public String assemble(ExtraInfo extraInfo, String label) {
        String query;
        String season = extraInfo.getSeason();
        String time = extraInfo.getTime();
        String feeling = extraInfo.getFeeling();

        query = String.format("%s %s %s %s %s",
                season, time, feeling, label, PLAYLIST_KR);

        System.out.println(query);

        return query;
    }

    /**
     * 사진만으로 검색어 생성
     * @param label
     * @return
     */
    public String assemble(String label) {
        String query;

        query = String.format("%s %s",
                label, PLAYLIST_KR);

        return query;
    }

    /**
     * 추가 정보만 사용해서 query 생성
     * @param extraInfo
     * season, time, feeling
     * @return
     */
    public String assemble(ExtraInfo extraInfo) {
        String query;
        String season = extraInfo.getSeason();
        String time = extraInfo.getTime();
        String feeling = extraInfo.getFeeling();

        query = String.format("%s %s %s %s",
                season, time, feeling, PLAYLIST_KR);

        System.out.println(query);

        return query;
    }
}