package cn.cxdproject.coder.common.task;

import cn.cxdproject.coder.common.constants.TaskConstants;
import cn.cxdproject.coder.mapper.ArticleMapper;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.Tour;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.TourVO;
import cn.cxdproject.coder.utils.JsonUtils;
import cn.cxdproject.coder.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
//Article的定时任务（用于分页降级时查询的数据）
@Component
@Slf4j
public class DailyLatestArticleCacheTask {

    private final ArticleMapper articleMapper;
    private final RedisUtils redisUtils;

    public DailyLatestArticleCacheTask(ArticleMapper articleMapper,
                                       RedisUtils redisUtils) {
        this.articleMapper = articleMapper;
        this.redisUtils = redisUtils;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cacheLatestArticlePage() {
        try {
            // 1. 从数据库查询最新10条
            List<Article> latestArticles = articleMapper.selectLatest10();

            if (latestArticles == null || latestArticles.isEmpty()) {
                log.warn("未查到文章数据，跳过缓存");
                return;
            }

            // 2. 转为VO
            List<ArticleVO> voList = latestArticles.stream()
                    .map(this::toArticleVO)
                    .collect(Collectors.toList());


            // 3. 序列化
            String json = JsonUtils.toJson(voList);

            // 4. 写入 Redis，有效期25小时
            redisUtils.set(
                    TaskConstants.ARTICLE_PAGE,
                    json,
                    Duration.ofHours(25)
            );
            log.info("成功缓存 {} 条文章到 Redis", voList.size());
        } catch (Exception e) {
            log.error("缓存失败", e);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cacheLatestArticleId() {
        try {
            // 1. 查询所有未删除的文章数据
            List<Article> allArticles = articleMapper.selectAll();

            if (allArticles == null || allArticles.isEmpty()) {
                log.warn("未查到任何文章数据，跳过缓存");
                return;
            }

            // 2. 遍历每一条，单独存入 Redis
            for (Article article : allArticles) {
                ArticleVO vo = toArticleVO(article);
                String key = TaskConstants.ARTICLE + article.getId();

                redisUtils.set(key, vo, Duration.ofHours(25));
            }

            log.info("成功将 {} 条文章信息逐条缓存到 Redis", allArticles.size());

        } catch (Exception e) {
            log.error("全量缓存文章到 Redis 失败", e);
        }
    }

    public ArticleVO toArticleVO(Article article) {
        if (article == null) {
            return null;
        }
        return ArticleVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .issueUnit(article.getIssueUnit())
                .issueTime(article.getIssueTime())
                .content(article.getContent())
                .userId(article.getUserId())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .image(article.getImage())
                .thumbImage(article.getThumbImage())
                .build();
    }
}
