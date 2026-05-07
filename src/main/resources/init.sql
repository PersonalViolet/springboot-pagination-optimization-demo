CREATE TABLE `news_article` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `title` varchar(200) NOT NULL COMMENT '新闻标题',
                                `summary` varchar(500) DEFAULT NULL COMMENT '新闻摘要',
                                `source` varchar(100) DEFAULT NULL COMMENT '来源',
                                `author` varchar(50) DEFAULT NULL COMMENT '作者',
                                `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                PRIMARY KEY (`id`),
                                KEY `idx_publish_time` (`publish_time`)
) ENGINE=InnoDB AUTO_INCREMENT=7000001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='新闻预览表'