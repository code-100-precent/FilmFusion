package cn.cxdproject.coder.common.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 启动时预热：在应用 ready 之后，异步顺序执行一遍所有降级缓存任务，
 * 让 fallback 用的 Redis 缓存立刻就绪，避免"应用刚启动 → 还没到当晚 2 点 →
 * 一旦熔断 / 超时触发 fallback 就只能拿到空数据 / 抛 502"的窗口期。
 *
 * <p>设计要点：
 * <ul>
 *   <li>监听 {@link ApplicationReadyEvent}，不会阻塞 Spring 容器启动；</li>
 *   <li>所有任务在<strong>单独一个守护线程</strong>中串行跑，避免 7 个模块同时打 DB；</li>
 *   <li>每个任务独立 try/catch，单个模块失败不影响其它模块；</li>
 *   <li>对外仅暴露日志，不抛出，不影响应用对外提供服务。</li>
 * </ul>
 */
@Slf4j
@Component
public class CacheWarmupRunner {

    private final DailyLatesTourCacheTask tourTask;
    private final DailyLatesDramaCacheTask dramaTask;
    private final DailyLatestArticleCacheTask articleTask;
    private final DailyLatesShootCacheTask shootTask;
    private final DailyLatesPolicyCacheTask policyTask;
    private final DailyLatesLocationCacheTask locationTask;
    private final DailyLatesHotelCacheTask hotelTask;

    public CacheWarmupRunner(DailyLatesTourCacheTask tourTask,
                             DailyLatesDramaCacheTask dramaTask,
                             DailyLatestArticleCacheTask articleTask,
                             DailyLatesShootCacheTask shootTask,
                             DailyLatesPolicyCacheTask policyTask,
                             DailyLatesLocationCacheTask locationTask,
                             DailyLatesHotelCacheTask hotelTask) {
        this.tourTask = tourTask;
        this.dramaTask = dramaTask;
        this.articleTask = articleTask;
        this.shootTask = shootTask;
        this.policyTask = policyTask;
        this.locationTask = locationTask;
        this.hotelTask = hotelTask;
    }

    /**
     * 应用 ready 后触发；@Order 大数表示尽量晚执行，让其他 ApplicationReadyEvent 监听器先跑完。
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(1000)
    public void warmup() {
        Thread t = new Thread(this::runAll, "cache-warmup");
        t.setDaemon(true);
        t.start();
    }

    private void runAll() {
        long startedAt = System.currentTimeMillis();
        log.info("[cache-warmup] start");
        AtomicInteger ok = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        // 顺序：先页缓存（少量数据，定位 fallback 列表用），再单条缓存（大量数据，定位 fallback 详情用）。
        run("tourPage",     tourTask::cacheLatestTourPage,         ok, fail);
        run("dramaPage",    dramaTask::cacheLatestDramaPage,       ok, fail);
        run("articlePage",  articleTask::cacheLatestArticlePage,   ok, fail);
        run("shootPage",    shootTask::cacheLatestShootPage,       ok, fail);
        run("policyPage",   policyTask::cacheLatest10Locations,    ok, fail);
        run("locationPage", locationTask::cacheLatestLocationPage, ok, fail);
        run("hotelPage",    hotelTask::cacheLatestHotelPage,       ok, fail);

        run("tourIds",     tourTask::cacheLatestTourId,        ok, fail);
        run("dramaIds",    dramaTask::cacheLatestDrama,        ok, fail);
        run("articleIds",  articleTask::cacheLatestArticleId,  ok, fail);
        run("shootIds",    shootTask::cacheLatestShootId,      ok, fail);
        run("policyIds",   policyTask::cacheLatestPolicy,      ok, fail);
        run("locationIds", locationTask::cacheLatestLocation,  ok, fail);
        run("hotelIds",    hotelTask::cacheLatestHotel,        ok, fail);

        long elapsed = System.currentTimeMillis() - startedAt;
        log.info("[cache-warmup] done in {}ms (ok={}, fail={})", elapsed, ok.get(), fail.get());
    }

    private void run(String name, Runnable task, AtomicInteger ok, AtomicInteger fail) {
        long t0 = System.currentTimeMillis();
        try {
            task.run();
            ok.incrementAndGet();
            log.info("[cache-warmup] {} ok in {}ms", name, System.currentTimeMillis() - t0);
        } catch (Throwable e) {
            fail.incrementAndGet();
            log.warn("[cache-warmup] {} failed in {}ms: {}", name, System.currentTimeMillis() - t0, e.getMessage());
        }
    }
}
