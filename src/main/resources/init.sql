-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS pagequery_demo
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE pagequery_demo;

-- 新闻预览表
DROP TABLE IF EXISTS news_article;
CREATE TABLE news_article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '新闻标题',
    summary VARCHAR(500) COMMENT '新闻摘要',
    source VARCHAR(100) COMMENT '来源',
    author VARCHAR(50) COMMENT '作者',
    publish_time DATETIME COMMENT '发布时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻预览表';
