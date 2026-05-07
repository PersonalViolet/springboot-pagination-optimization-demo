package com.byteknight.pagequerydemo.mapper;

import com.byteknight.pagequerydemo.entity.NewsArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NewsArticleMapper {

    List<NewsArticle> selectPage(@Param("offset") int offset, @Param("size") int size);

    long count();

    List<NewsArticle> selectPageByCursor(@Param("lastArticle") NewsArticle lastArticle, @Param("size") int size);
}
