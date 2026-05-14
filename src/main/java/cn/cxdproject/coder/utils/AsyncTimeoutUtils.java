package cn.cxdproject.coder.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 带超时的异步执行工具。
 *
 * <p>原代码中各 Service 使用 {@code CompletableFuture.supplyAsync(...)} 默认走
 * {@code ForkJoinPool.commonPool()}，并在 {@code future.get(timeout)} 超时后不取消任务，
 * 容易在高并发场景下造成 ForkJoin 公共池被 IO 任务占满、且后台任务继续堆积。
 *
 * <p>本工具：
 * <ul>
 *   <li>使用专用的有界 IO 线程池，与 ForkJoinPool 隔离；</li>
 *   <li>超时后调用 {@code future.cancel(true)} 中断底层任务，避免任务堆积。</li>
 * </ul>
 */
@Slf4j
public final class AsyncTimeoutUtils {

    /**
     * 专用 IO 线程池：
     * <ul>
     *   <li>核心 16、最大 64：适配数据库 + 缓存查询为主的中等并发；</li>
     *   <li>队列 500：吸收瞬时突发；</li>
     *   <li>CallerRunsPolicy：满载时退化为调用线程执行，避免直接丢任务。</li>
     * </ul>
     */
    private static final ExecutorService IO_EXECUTOR = new ThreadPoolExecutor(
            16,
            64,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(500),
            new ThreadFactory() {
                private final AtomicInteger idx = new AtomicInteger();

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "io-timeout-" + idx.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    private AsyncTimeoutUtils() {
    }

    /**
     * 在专用 IO 线程池中执行任务，并在指定超时后取消任务。
     *
     * <p>使用 {@link Callable} 而非 {@link java.util.function.Supplier}，
     * 以支持业务层抛出 {@code InterruptedException} 等检查异常的方法签名。
     *
     * @param task    要执行的任务
     * @param timeout 超时时间
     * @param unit    时间单位
     * @param <T>     返回值类型
     * @return 任务返回值
     * @throws TimeoutException 任务在超时时间内未完成
     */
    public static <T> T runWithTimeout(Callable<T> task, long timeout, TimeUnit unit) throws TimeoutException {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                // 将检查异常包装为非检查异常，使其能透过 CompletableFuture 传递
                throw new CompletionException(e);
            }
        }, IO_EXECUTOR);
        try {
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            // 关键修复：超时后中断底层任务，避免任务堆积导致雪崩
            future.cancel(true);
            throw e;
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            // 解包 CompletionException，使原始业务异常能被上层捕获
            if (cause instanceof CompletionException && cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause != null ? cause : e);
        }
    }
}
