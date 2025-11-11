package cn.cxdproject.coder.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

/**
 * SQL执行时间拦截器
 * 用于监控和记录SQL执行时间，对慢SQL进行警告
 *
 * @author heathcetide
 */
@Slf4j
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", 
              args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class SqlExecuteTimeInterceptor implements Interceptor {
    private static final long SLOW_SQL_THRESHOLD = 1000;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        String sqlId = mappedStatement.getId();
        long start = System.currentTimeMillis();
        
        try {
            return invocation.proceed();
        } finally {
            long cost = System.currentTimeMillis() - start;
            if (cost > SLOW_SQL_THRESHOLD) {
                log.warn("[慢SQL警告] SQL ID: {} - 执行耗时: {}ms", sqlId, cost);
            } else {
                log.debug("[SQL执行] SQL ID: {} - 执行耗时: {}ms", sqlId, cost);
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}