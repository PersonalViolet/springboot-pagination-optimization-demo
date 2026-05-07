package com.byteknight.pagequerydemo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final int TOTAL = 7_000_000;
    private static final int THREADS = 8;
    private static final int BATCH_SIZE = 5000;

    private static final String[] PREFIXES = {
        "最新研究显示", "突破性进展", "重磅发布", "权威解读",
        "深度分析", "独家报道", "多部门联合", "专家呼吁",
        "数据揭示", "年报出炉", "正式宣布", "全球首发",
        "持续发力", "全面升级", "加速推进", "重磅官宣",
        "首度公开", "最高法明确", "多省同步", "国家发改委"
    };

    private static final String[] KEYWORDS = {
        "人工智能", "新能源", "数字经济", "量子计算",
        "半导体", "生物医药", "航天工程", "乡村振兴",
        "碳中和", "国产替代", "6G通信", "新能源汽车",
        "光伏发电", "氢能产业", "脑机接口", "智能制造",
        "高铁建设", "跨境电商", "智慧城市", "区块链",
        "元宇宙", "储能技术", "自动驾驶", "人形机器人",
        "低空经济", "大语言模型", "基因编辑", "固态电池",
        "芯片制造", "虚拟现实"
    };

    private static final String[] SUFFIXES = {
        "取得重大突破", "进入新阶段", "市场规模超预期", "引发广泛关注",
        "政策红利持续释放", "成全球焦点", "投资热度不减", "体系日益完善",
        "迎来发展黄金期", "实现弯道超车", "国际竞争力提升", "创新成果涌现",
        "标准体系发布", "产业生态加速构建", "人才缺口扩大", "应用场景不断拓展",
        "国际合作深入推进", "年度报告正式公布", "利好政策密集出台", "核心技术实现自主可控"
    };

    private static final String[] SUMMARIES = {
        "近日，%s领域取得重要进展，业内专家表示这将深刻影响未来产业格局。",
        "据最新统计，%s相关产业规模已达%d亿元，同比增长%d%%。",
        "政策持续加码，预计到2030年%s相关市场规模将突破万亿。",
        "在政策和市场需求双重驱动下，%s行业迎来新一轮快速发展期。",
        "多家研究机构联合发布报告，认为%s将成为未来五年最具投资价值的赛道之一。",
        "%s技术快速迭代正在重塑传统行业，引发资本市场高度关注。",
        "国家部委相关负责人表示，将加大%s领域投入力度，推动产业高质量发展。",
        "国际比较研究显示，我国在%s领域已进入全球第一梯队。",
        "随着%s持续发展，产业链上下游企业迎来新一轮发展机遇。",
        "分析人士指出，%s正成为新质生产力的重要代表。",
        "记者从新闻发布会上获悉，%s将迎来一系列重大政策利好。",
        "产学研各界代表齐聚一堂，共同探讨%s发展路径与未来方向。",
        "最新发布的行业白皮书显示，%s发展前景广阔，应用潜力巨大。",
        "资本市场对%s赛道热情高涨，年内已有多家企业获得融资。",
        "多位院士联名建议，加快%s基础研究布局，抢占科技制高点。",
        "全球%s竞争日趋激烈，我国凭借市场规模和人才优势持续领跑。",
        "地方政府纷纷出台%s专项扶持政策，营造良好发展环境。",
        "此举被业界视为推动%s高质量发展的重要举措。",
        "经过多年技术积累，我国%s已达到国际领先水平。",
        "最新数据显示，%s领域从业人员持续增长，人才需求旺盛。",
        "在%s细分赛道上，国内企业市场份额稳步提升。",
        "社会各界对%s关注度持续升温，多方力量积极参与。",
        "行业龙头企业在%s领域持续加码布局，引领产业创新方向。",
        "科研团队攻克了%s关键核心技术，打破国外垄断。",
        "国际权威期刊发表我国科学家在%s领域最新研究成果。",
        "随着%s示范项目落地推广，商业化进程加速推进。",
        "面对复杂多变的国际环境，我国%s产业展现出强大韧性。",
        "多部委联合印发%s发展规划，明确阶段目标和重点任务。",
        "%s行业协会发布年度报告，多项指标创历史新高。",
        "%s标准化技术委员会成立，行业规范发展迈出关键一步。"
    };

    private static final String[] SOURCES = {
        "新华社", "人民日报", "科技日报", "经济日报",
        "光明日报", "中国科学报", "21世纪经济报道", "第一财经",
        "环球时报", "证券时报", "上海证券报", "中国经营报",
        "每日经济新闻", "界面新闻", "澎湃新闻", "36氪",
        "央视新闻", "参考消息"
    };

    private static final String[] SURNAMES = {
        "张", "李", "王", "赵", "陈", "刘", "周", "孙",
        "杨", "吴", "黄", "马", "郑", "林", "何", "罗",
        "郭", "梁", "宋", "唐", "韩", "冯", "邓", "曹",
        "彭", "肖", "田", "董", "潘", "袁"
    };

    private static final String[] GIVENS = {
        "伟", "明", "强", "丽", "洋", "芳", "华", "磊",
        "涛", "敏", "宇", "雪", "峰", "杰", "文", "辉",
        "宁", "鹏", "浩", "静", "勇", "睿", "博", "婷",
        "毅", "超", "鑫", "然", "恒", "一"
    };

    private final DataSource dataSource;

    public DataInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM news_article");
            rs.next();
            long count = rs.getLong(1);
            if (count >= TOTAL) {
                log.info("数据已存在 ({} 条)，跳过初始化", count);
                return;
            }
            if (count > 0) {
                log.info("清空现有 {} 条数据，重新生成", count);
                stmt.execute("TRUNCATE TABLE news_article");
            }
        }

        log.info("开始生成 {} 条新闻数据 ({} 线程)...", TOTAL, THREADS);
        long start = System.currentTimeMillis();

        int perThread = TOTAL / THREADS;
        var executor = Executors.newFixedThreadPool(THREADS);
        var latch = new CountDownLatch(THREADS);
        var progressTotal = new AtomicLong(0);

        for (int t = 0; t < THREADS; t++) {
            final int threadIdx = t;
            final int startRow = t * perThread + 1;
            final int endRow = (t == THREADS - 1) ? TOTAL : startRow + perThread - 1;
            executor.submit(() -> {
                try {
                    insertRange(startRow, endRow, threadIdx, progressTotal);
                } catch (Exception e) {
                    log.error("线程 {} 异常", threadIdx, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 每 5 秒打印一次总进度
        var scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            long done = progressTotal.get();
            if (done > 0) {
                log.info("总进度: {}/{} ({}%)", done, TOTAL, done * 100 / TOTAL);
            }
        }, 5, 5, java.util.concurrent.TimeUnit.SECONDS);

        latch.await();
        scheduler.shutdown();
        executor.shutdown();

        long elapsed = System.currentTimeMillis() - start;
        log.info("数据生成完成！共 {} 条，耗时 {} 秒 ({:.1f} 万条/秒)",
                TOTAL, elapsed / 1000, TOTAL * 1000.0 / elapsed / 10000);
    }

    private void insertRange(int startRow, int endRow, int threadIdx, AtomicLong progressTotal) {
        String sql = "INSERT INTO news_article (title, summary, source, author, publish_time) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                Random rnd = ThreadLocalRandom.current();
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                int total = endRow - startRow + 1;
                int count = 0;

                for (int i = startRow; i <= endRow; i++) {
                    String keyword = pick(rnd, KEYWORDS);
                    String title = pick(rnd, PREFIXES) + "：" + keyword + pick(rnd, SUFFIXES);
                    String summary = String.format(pick(rnd, SUMMARIES), keyword,
                            1000 + rnd.nextInt(9000), 10 + rnd.nextInt(40));
                    String source = pick(rnd, SOURCES);
                    String author = pick(rnd, SURNAMES) + pick(rnd, GIVENS);
                    LocalDateTime publishTime = LocalDateTime.now()
                            .minusMinutes(rnd.nextInt(730 * 24 * 60));

                    ps.setString(1, title);
                    ps.setString(2, summary);
                    ps.setString(3, source);
                    ps.setString(4, author);
                    ps.setString(5, publishTime.format(fmt));
                    ps.addBatch();

                    count++;
                    if (count % BATCH_SIZE == 0) {
                        ps.executeBatch();
                        conn.commit();
                        progressTotal.addAndGet(BATCH_SIZE);
                    }
                }
                // 剩余批次
                int remainder = count % BATCH_SIZE;
                if (remainder > 0) {
                    ps.executeBatch();
                    conn.commit();
                    progressTotal.addAndGet(remainder);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("线程 " + threadIdx + " 插入失败", e);
        }
    }

    private static <T> T pick(Random rnd, T[] arr) {
        return arr[rnd.nextInt(arr.length)];
    }
}
