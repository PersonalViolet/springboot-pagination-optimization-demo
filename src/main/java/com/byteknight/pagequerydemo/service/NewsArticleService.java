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
        return new PageResult<>(page, size, total, elapsed, records, 0);
    }

    /**
     * 混合分页查询，结合传统分页和基于游标的分页，提升性能和用户体验。
     * 若用户查询的是当前页码的下一页或上一页，则page参数应为0表明page参数无效
     * startArticle, lastArticle、page三者只能存在一个非空且有效，否则抛出异常，避免查询逻辑混乱
     * @param startArticle  上一次查询的第一条记录
     * @param lastArticle 上一次查询的最后一条记录
     * @param page 对应页码
     * @param size 每页记录数
     * @return  分页结果，包含记录列表和分页信息
     */
    public PageResult<NewsArticle> getHybridPage(NewsArticle startArticle, NewsArticle lastArticle, int page, int size) {
        long t0 = System.currentTimeMillis();
        // 计算总记录数，仍然使用预设值避免性能问题
        long total = 7000000;
        // 检查参数合法性，确保三个参数只有一个非空且有效
        Boolean check = check(startArticle, lastArticle, page);
        if (!check) {
            throw new IllegalArgumentException("参数不合法，startArticle, lastArticle, page三者只能存在一个非空且有效");
        }
        int pageDelta = 0;
        // 基于游标的分页查询，使用lastArticle的id和publish_time作为游标
        List<NewsArticle> records;
        if (lastArticle != null && lastArticle.getId() != null) {
            records = newsArticleMapper.selectPageByCursor(lastArticle, size);
            startArticle = records.get(0);
            lastArticle = records.get(records.size() - 1);
            pageDelta = 1; // 基于lastArticle查询下一页，page参数无效，pageDelta用于前端展示页码加1
        } else if (startArticle != null && startArticle.getId() != null) {
            // 如果提供了startArticle，则查询上一页数据，基于startArticle的id和publish_time作为游标
            records = newsArticleMapper.selectPreviousPageByCursor(startArticle, size);
            startArticle = records.get(0);
            lastArticle = records.get(records.size() - 1);
            pageDelta = -1;
        } else {
            // 如果没有提供游标，则回退到传统分页查询
            int offset = (page - 1) * size;
            records = newsArticleMapper.selectPage(offset, size);
            startArticle = records.get(0);
            lastArticle = records.get(records.size() - 1);
        }
        long elapsed = System.currentTimeMillis() - t0;
        return new PageResult<>(page, size, total, elapsed, records, pageDelta, startArticle, lastArticle);
    }

    /**
     * 检查参数的合法性，确保三个参数只有一个非空且有效，避免查询逻辑混乱和性能问题
     * @param startArticle  上一次查询的第一条记录
     * @param lastArticle   上一次查询的最后一条记录
     * @param page  要查询的对应页码
     * @return  如果参数合法返回true，否则返回false
     */
    private Boolean check(NewsArticle startArticle, NewsArticle lastArticle, int page) {
        int count = 0;
        if (startArticle != null && startArticle.getId() != null) {
            count++;
        }
        if (lastArticle != null && lastArticle.getId() != null) {
            count++;
        }
        if (page > 0) {
            count++;
        }
        return count == 1; // 确保三个参数只有一个非空且有效
    }
}
