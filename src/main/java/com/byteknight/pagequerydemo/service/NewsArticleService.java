package com.byteknight.pagequerydemo.service;

import com.byteknight.pagequerydemo.vo.PageResult;
import com.byteknight.pagequerydemo.entity.NewsArticle;
import com.byteknight.pagequerydemo.mapper.NewsArticleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsArticleService {

    private final NewsArticleMapper newsArticleMapper;

    public NewsArticleService(NewsArticleMapper newsArticleMapper) {
        this.newsArticleMapper = newsArticleMapper;
    }

    public PageResult<NewsArticle> getPage(int page, int size) {
        long t0 = System.currentTimeMillis();

        // long total = newsArticleMapper.count();
        long total =  7000000; // 直接使用预设的总记录数，避免count查询的性能问题，700w 数据量
        int offset = (page - 1) * size;
        List<NewsArticle> records = newsArticleMapper.selectPage(offset, size);

        long elapsed = System.currentTimeMillis() - t0;
        return new PageResult<>(page, size, total, elapsed, records);
    }

    /**
     * 混合分页查询，结合传统分页和基于游标的分页，提升性能和用户体验
     * @param lastArticle 上一次查询的最后一条记录
     * @param page 对应页码
     * @param size 每页记录数
     * @return  分页结果，包含记录列表和分页信息
     */
    public PageResult<NewsArticle> getHybridPage(NewsArticle lastArticle, int page, int size) {
        long t0 = System.currentTimeMillis();

        // 计算总记录数，仍然使用预设值避免性能问题
        long total = 7000000;

        // 基于游标的分页查询，使用lastArticle的id和publish_time作为游标
        List<NewsArticle> records;
        if (lastArticle != null && lastArticle.getId() != null) {
            records = newsArticleMapper.selectPageByCursor(lastArticle, size);
        } else {
            // 如果没有提供游标，则回退到传统分页查询
            int offset = (page - 1) * size;
            records = newsArticleMapper.selectPage(offset, size);
        }

        long elapsed = System.currentTimeMillis() - t0;
        return new PageResult<>(page, size, total, elapsed, records);
    }
}
