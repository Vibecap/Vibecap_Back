package com.example.vibecap_back.domain.post.domain.Tag;

import com.example.vibecap_back.domain.post.domain.Posts;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@ToString
@Table(name="tag")
public class Tags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Setter
    @Column(length = 16, nullable = false)
    private String tagName;

    public Tags(Long id, String tagName) {
        this.id = id;
        this.tagName = tagName;
    }
}
