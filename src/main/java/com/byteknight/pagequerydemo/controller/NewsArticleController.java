package com.byteknight.pagequerydemo.controller;

import com.byteknight.pagequerydemo.vo.PageResult;
import com.byteknight.pagequerydemo.entity.NewsArticle;
import com.byteknight.pagequerydemo.service.NewsArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "*")
@Slf4j
public class NewsArticleController {

    private final NewsArticleService newsArticleService;

    public NewsArticleController(NewsArticleService newsArticleService) {
        this.newsArticleService = newsArticleService;
    }

    @GetMapping("/page")
    public PageResult<NewsArticle> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return newsArticleService.getPage(page, size);
    }

    @GetMapping("/mixpage")
    public PageResult<NewsArticle> getHybridPage(
            @RequestParam(required = false, name = "startArticle.id") Long startArticleId,
            @RequestParam(required = false, name = "startArticle.publishTime") String startArticlePublishTime,
            @RequestParam(required = false, name = "lastArticle.id") Long lastArticleId,
            @RequestParam(required = false, name = "lastArticle.publishTime") String lastArticlePublishTime,
            @RequestParam int page,
            @RequestParam(defaultValue = "10") int size) {

        NewsArticle startArticle = buildArticle(startArticleId, startArticlePublishTime);
        NewsArticle lastArticle = buildArticle(lastArticleId, lastArticlePublishTime);

        log.info("混合分页 startArticleId={} lastArticleId={} page={} size={}",
                startArticleId, lastArticleId, page, size);

        return newsArticleService.getHybridPage(startArticle, lastArticle, page, size);
    }

    private NewsArticle buildArticle(Long id, String publishTime) {
        if (id == null || publishTime == null) return null;
        NewsArticle a = new NewsArticle();
        a.setId(id);
        a.setPublishTime(LocalDateTime.parse(publishTime));
        return a;
    }
}
