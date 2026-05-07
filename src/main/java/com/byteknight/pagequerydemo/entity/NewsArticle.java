package com.byteknight.pagequerydemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NewsArticle {
    private Long id;
    private String title;
    private String summary;
    private String source;
    private String author;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
}
