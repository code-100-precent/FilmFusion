package cn.cxdproject.coder.interceptor;

import cn.cxdproject.coder.common.PageRequest;
import cn.cxdproject.coder.common.PageResponse;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Properties;

@Intercepts(@Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
))
public class PaginationInterceptor implements Interceptor {

    private static final int MAX_PAGE_SIZE = 100;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object parameter = invocation.getArgs()[1];
        PageRequest pageRequest = extractPageRequest(parameter);

        // 无分页请求或未启用分页，直接放行
        if (pageRequest == null || Boolean.FALSE.equals(pageRequest.getPagination())) {
            return invocation.proceed();
        }

        // 限制分页大小
        if (pageRequest.getSize() > MAX_PAGE_SIZE) {
            pageRequest.setSize(MAX_PAGE_SIZE);
        }

        // 统计总数
        long total = pageRequest.getCountTotal() ? executeCount(invocation) : -1;

        // 注入分页 SQL
        injectPaginationSql(invocation, pageRequest);

        // 执行分页查询
        List<?> records = (List<?>) invocation.proceed();

        // 构造分页响应
        return PageResponse.of(
                pageRequest.getPage(),
                pageRequest.getSize(),
                total,
                records
        );
    }

    private PageRequest extractPageRequest(Object parameter) {
        if (parameter instanceof PageRequest) {
            return (PageRequest) parameter;
        } else if (parameter instanceof Map<?, ?>) {
            return ((Map<?, ?>) parameter).values().stream()
                    .filter(p -> p instanceof PageRequest)
                    .map(p -> (PageRequest) p)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private void injectPaginationSql(Invocation invocation, PageRequest pageRequest) throws Exception {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object paramObj = invocation.getArgs()[1];
        BoundSql boundSql = ms.getBoundSql(paramObj);

        String originalSql = boundSql.getSql();

        // 添加排序
        if (StringUtils.hasText(pageRequest.getSortBy())) {
            originalSql += " ORDER BY " + pageRequest.getSortBy() + " " +
                    ("asc".equalsIgnoreCase(pageRequest.getSortOrder()) ? "ASC" : "DESC");
        }

        // 添加 LIMIT 分页
        int offset = (pageRequest.getPage() - 1) * pageRequest.getSize();
        originalSql += " LIMIT " + offset + ", " + pageRequest.getSize();

        // 创建新的 BoundSql 包装原参数
        BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), originalSql,
                boundSql.getParameterMappings(), paramObj);

        // 拷贝原始 MappedStatement
        MappedStatement newMs = copyMappedStatement(ms, newBoundSql);

        // 替换 invocation 的 args[0]
        invocation.getArgs()[0] = newMs;
    }

    private long executeCount(Invocation invocation) throws Exception {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object paramObj = invocation.getArgs()[1];
        BoundSql boundSql = ms.getBoundSql(paramObj);
        String countSql = "SELECT COUNT(*) FROM (" + boundSql.getSql() + ") AS total";

        BoundSql countBoundSql = new BoundSql(ms.getConfiguration(), countSql,
                boundSql.getParameterMappings(), paramObj);

        MappedStatement countMs = copyMappedStatement(ms, countBoundSql);
        Executor executor = (Executor) invocation.getTarget();
        List<Object> resultList = executor.query(countMs, paramObj, RowBounds.DEFAULT,
                (ResultHandler<?>) invocation.getArgs()[3]);

        return (resultList != null && !resultList.isEmpty())
                ? ((Number) resultList.get(0)).longValue()
                : 0L;
    }

    private MappedStatement copyMappedStatement(MappedStatement original, BoundSql newBoundSql) {
        SqlSource newSqlSource = parameterObject -> newBoundSql;

        MappedStatement.Builder builder = new MappedStatement.Builder(
                original.getConfiguration(),
                original.getId(),
                newSqlSource,
                original.getSqlCommandType()
        );
        builder.resource(original.getResource());
        builder.parameterMap(original.getParameterMap());
        builder.resultMaps(original.getResultMaps());
        builder.statementType(original.getStatementType());
        builder.fetchSize(original.getFetchSize());
        builder.timeout(original.getTimeout());
        builder.resultSetType(original.getResultSetType());
        builder.cache(original.getCache());
        builder.flushCacheRequired(original.isFlushCacheRequired());
        builder.useCache(original.isUseCache());

        return builder.build();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可用于读取配置参数
    }
}
