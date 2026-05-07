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
        long total = 7000000;
        int offset = (page - 1) * size;
        List<NewsArticle> records = newsArticleMapper.selectPage(offset, size);
        long elapsed = System.currentTimeMillis() - t0;
        return new PageResult<>(page, size, total, elapsed, records, 0);
    }


    /**
     * 混合分页
     * 混合分页，三者选一：lastArticle → 下一页 / startArticle → 上一页 / page → 跳页。
     * 若三者都无效，兜底为 page=1。
     * 三者只能一个有效，若同时存在，则抛出异常。
     * @param startArticle 上一次查询的第一条记录
     * @param lastArticle 上一次查询的最后一条记录
     * @param page 对应页码
     * @param size 每页记录数
     * @return  分页结果，包含记录列表和分页信息
     */
    public PageResult<NewsArticle> getHybridPage(NewsArticle startArticle, NewsArticle lastArticle, int page, int size) {
        long t0 = System.currentTimeMillis();
        long total = 7000000;

        boolean hasStart = startArticle != null && startArticle.getId() != null;
        boolean hasLast = lastArticle != null && lastArticle.getId() != null;
        boolean hasPage = page > 0;

        // 兜底：三者都无效 → 默认第 1 页
        if (!hasStart && !hasLast && !hasPage) {
            page = 1;
            hasPage = true;
        }

        // 三者只能有一个有效
        int count = (hasStart ? 1 : 0) + (hasLast ? 1 : 0) + (hasPage ? 1 : 0);
        if (count != 1) {
            throw new IllegalArgumentException(
                "参数冲突：startArticle, lastArticle, page 三者只能有一个有效，当前 startArticle="
                + (hasStart ? "有" : "无") + ", lastArticle=" + (hasLast ? "有" : "无")
                + ", page=" + page);
        }

        int pageDelta = 0;
        List<NewsArticle> records;

        if (hasLast) {
            records = newsArticleMapper.selectPageByCursor(lastArticle, size);
            if (records.isEmpty()) {
                // 已经是最后一页了，没有下一页了，保持不变
                long elapsed = System.currentTimeMillis() - t0;
                return new PageResult<>(page, size, total, elapsed, records, pageDelta, startArticle, lastArticle);
            }
            startArticle = records.get(0);
            lastArticle = records.get(records.size() - 1);
            pageDelta = 1;
        } else if (hasStart) {
            records = newsArticleMapper.selectPreviousPageByCursor(startArticle, size);
            if (records.isEmpty()) {
                // 已经是最后一页了，没有下一页了，保持不变
                long elapsed = System.currentTimeMillis() - t0;
                return new PageResult<>(page, size, total, elapsed, records, pageDelta, startArticle, lastArticle);
            }
            startArticle = records.get(0);
            lastArticle = records.get(records.size() - 1);
            pageDelta = -1;
        } else {
            int offset = (page - 1) * size;
            records = newsArticleMapper.selectPageSmart(offset, size);
            if (records.isEmpty()) {
                // 已经是最后一页了，没有下一页了，保持不变
                long elapsed = System.currentTimeMillis() - t0;
                return new PageResult<>(page, size, total, elapsed, records, pageDelta, startArticle, lastArticle);
            }
            startArticle = records.get(0);
            lastArticle = records.get(records.size() - 1);
        }

        long elapsed = System.currentTimeMillis() - t0;
        return new PageResult<>(page, size, total, elapsed, records, pageDelta, startArticle, lastArticle);
    }
}
