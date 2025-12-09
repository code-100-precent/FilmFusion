package cn.cxdproject.coder.common.aspect;

import cn.cxdproject.coder.common.anno.Loggable;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.common.enums.LogType;
import cn.cxdproject.coder.model.entity.OperationLog;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.service.OperationLogService;
import cn.cxdproject.coder.utils.IpUtils;
import cn.cxdproject.coder.utils.JsonUtils;
import cn.cxdproject.coder.utils.StringUtils;
import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CompletableFuture;


@Aspect
@Component
@Slf4j
public class ServiceLogAspect {

    private OperationLogService operationLogService;
    private final ExpressionParser spelParser = new SpelExpressionParser();

    public ServiceLogAspect(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;

    }

    @Pointcut("@annotation(cn.cxdproject.coder.common.anno.Loggable)")
    public void loggablePointcut() {

    }

    @Around("loggablePointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(joinPoint.getTarget());
        method = targetClass.getMethod(method.getName(), method.getParameterTypes());
        Loggable loggable = method.getAnnotation(Loggable.class);

        OperationLog logEntity = new OperationLog();
        logEntity.setTimestamp(LocalDateTime.now());
        logEntity.setDeleted(false);

        //  1. 操作类型
        LogType logType = loggable.type();
        logEntity.setType(logType.getCode());

        //  2. 操作描述
        String desc = resolveSpel(loggable.value(), method, joinPoint.getArgs());
        logEntity.setDescription(StringUtils.hasText(desc) ? desc : logType.getDescription());

        //  3. 操作人信息
        User currentUser = AuthContext.getCurrentUser();

        if (currentUser == null){
            log.warn("未登录");
            logEntity.setUserId(0L);
            logEntity.setUserName("未登录");
        }else {
            logEntity.setUserId(currentUser.getId());
            logEntity.setUserName(currentUser.getUsername());
        }
        String clientIp = IpUtils.getIpAddr();
        logEntity.setOperator(clientIp);



        // 4. 请求参数快照
        try {
            logEntity.setParams(JsonUtils.toJson(joinPoint.getArgs()));
        } catch (Exception e) {
            log.warn("序列化操作日志参数失败", e);
            logEntity.setParams("[PARAM_SERIALIZE_ERROR]");
        }

        // 5. 执行目标方法并记录结果
        Object result;
        try {
            result = joinPoint.proceed();
            logEntity.setSuccess(true);

            // 避免记录大数据量结果
            if (result == null) {
                logEntity.setResult("null");
            } else if (isLargeResult(result)) {
                // 是大结果
                logEntity.setResult("数据量过大，暂不记录");
            } else {
                // 小结果
                logEntity.setResult(safeToJson(result));
            }
        } catch (Exception ex) {
            logEntity.setSuccess(false);
            logEntity.setResult("{\"error\": \"" + escapeJson(ex.getMessage()) + "\"}");
            throw ex;
        }

        //  6. 异步保存日志
        saveLogAsync(logEntity);

        return result;
    }

    private String resolveSpel(String expression, Method method, Object[] args) {
        if (!StringUtils.hasText(expression) || !expression.contains("#{")) {
            return expression;
        }

        try {
            // 使用 Spring 的 MethodBasedEvaluationContext
            EvaluationContext context = new MethodBasedEvaluationContext(null, method, args,
                    new LocalVariableTableParameterNameDiscoverer());

            ExpressionParser parser = new SpelExpressionParser();
            return parser.parseExpression(expression, new TemplateParserContext())
                    .getValue(context, String.class);
        } catch (Exception e) {
            log.warn("SpEL 解析失败: {}", expression, e);
            return expression; // 降级
        }
    }

    /**
     * 获取方法参数名（需编译时保留参数名：-parameters）
     */
    private String[] getParameterNames(Method method) {
        return Arrays.stream(method.getParameters())
                .map(Parameter::getName)
                .toArray(String[]::new);
    }

    /**
     * 判断结果是否过大（避免记录分页、列表等）
     */
    private boolean isLargeResult(Object result) {
        if (result instanceof Iterable<?>) {
            return true;
        }
        if (result.getClass().isArray()) {
            return true;
        }
        // 可根据需要扩展，例如 Map size > 100
        return false;
    }

    /**
     * 安全地将对象转为 JSON 字符串
     */
    private String safeToJson(Object obj) {
        try {
            return JsonUtils.toJson(obj);
        } catch (Exception e) {
            log.warn("结果序列化失败", e);
            return "[RESULT_SERIALIZE_ERROR]";
        }
    }

    /**
     * 转义双引号，用于嵌入 JSON 字符串
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"");
    }


    protected void saveLogAsync(OperationLog operationLog) {
        CompletableFuture.runAsync(() -> {
            try {
                operationLogService.save(operationLog);
            } catch (Exception e) {
                log.error("异步保存操作日志失败", e);
            }
        });
    }
}
