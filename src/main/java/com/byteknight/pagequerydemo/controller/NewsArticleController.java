package com.byteknight.pagequerydemo.controller;

import com.byteknight.pagequerydemo.vo.PageResult;
import com.byteknight.pagequerydemo.entity.NewsArticle;
import com.byteknight.pagequerydemo.service.NewsArticleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "*")
public class NewsArticleController {

    private final NewsArticleService newsArticleService;

    public NewsArticleController(NewsArticleService newsArticleService) {
        this.newsArticleService = newsArticleService;
    }


    /**
     * 传统分页接口，基于MySQL的LIMIT和OFFSET实现，适用于小数据量或不频繁访问的场景
     * @param page  页码，从1开始
     * @param size  每页记录数
     * @return  分页结果，包含记录列表和分页信息
     */
    @GetMapping("/page")
    public PageResult<NewsArticle> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return newsArticleService.getPage(page, size);
    }


    /**
     * 混合分页接口，结合MySQL传统分页和基于游标的分页，提升性能和用户体验
     * @param lastArticle   上一次查询的最后一条记录，包含id和publish_time作为游标，如果跳页则需为null，否则会基于游标查询下一页数据
     * @param page        对应页码，主要用于前端展示和计算总页数，如果提供了lastArticle则page参数仅用于展示，不影响查询逻辑
     * @param size      每页记录数，影响查询结果的数量
     * @return  分页结果，包含记录列表和分页信息
     */
    @GetMapping("/mixpage")
    public PageResult<NewsArticle> getHybridPage(
            NewsArticle lastArticle,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return newsArticleService.getHybridPage(lastArticle, page, size);
    }
}
