package cn.cxdproject.coder.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 性能监控切面
 * 监控所有Controller方法的执行时间，对慢接口进行警告
 *
 * 那么如何进行定位慢查询？
 * 慢查询一般是那种sql执行时间比较长的查询语句，称之为慢查询，如何去定位到MySQL中的慢查询呢？
 * 这里可以分为三个层面去处理
 * 1. 数据库层面的话，MySQL是可以开启慢日志的配置，从而能够获取到慢查询，不过这种方案会影响本身业务，所以不建议生产环境使用
 * 2. 业务层面使用，可以使用比如此处的AOP层面对方法进行拦截，查看查询时间从而得到是否是慢查询，或者自定义MyBatis的这个自定义拦截器插件的方式实现
 * 3. 运维层面，使用普罗米修斯这类运维工具进行监听从而得到慢查询的相关方法并且获得慢查询相关的原因
 *
 * @author heathcetide
 */
@Slf4j
@Aspect
@Component
public class PerformanceAspect {

    /**
     * 性能日志记录器（使用特殊的Logger名称用于日志分类）
     */
    private static final org.slf4j.Logger perfLogger = LoggerFactory.getLogger("PERF");

    private static final long WARN_THRESHOLD = 500;

    @Around("execution(* cn.cxdproject.coder.controller..*(..))")
    public Object monitor(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return pjp.proceed();
        } finally {
            long cost = System.currentTimeMillis() - start;
            String method = pjp.getSignature().toShortString();
            perfLogger.info("{} - {}ms", method, cost);
            if (cost > WARN_THRESHOLD) {
                perfLogger.warn("慢接口警告: {}", method);
            }
        }
    }
}