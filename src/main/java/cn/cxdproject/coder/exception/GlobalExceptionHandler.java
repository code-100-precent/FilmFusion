package cn.cxdproject.coder.exception;

import cn.cxdproject.coder.common.ApiResponse;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.RATE_LIMIT_EXCEEDED;
import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.SERVICE_UNAVAILABLE;
import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.UNAUTHORIZED;
import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.SYSTEM_ERROR;


/**
 * Global exception handler, which intercepts and handles the exceptions thrown in the project in a unified way and returns a structured response.
 *
 * @author heathcetide
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 限流：返回 429。注意不要打印堆栈，避免日志爆炸。
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ApiResponse<?> handleRateLimit(RequestNotPermitted ex) {
        log.warn("rate-limit triggered: {}", ex.getMessage());
        return ApiResponse.error(RATE_LIMIT_EXCEEDED.code(), "请求过于频繁，请稍后再试");
    }

    /**
     * Bulkhead 满载：当前模块并发已达上限，等同 503。
     */
    @ExceptionHandler(BulkheadFullException.class)
    public ApiResponse<?> handleBulkheadFull(BulkheadFullException ex) {
        log.warn("bulkhead full: {}", ex.getMessage());
        return ApiResponse.error(SERVICE_UNAVAILABLE.code(), "服务繁忙，请稍后重试");
    }

    /**
     * 熔断器处于 OPEN 状态时拒绝调用。
     * 一般情况下 Service 层的 fallbackMethod 会先兜底；只有当 fallback 本身也抛异常
     * 或者方法上没配 fallback 时才会冒到这里。
     */
    @ExceptionHandler(CallNotPermittedException.class)
    public ApiResponse<?> handleCircuitOpen(CallNotPermittedException ex) {
        log.warn("circuit breaker open: {}", ex.getMessage());
        return ApiResponse.error(SERVICE_UNAVAILABLE.code(), "服务暂时不可用，请稍后重试");
    }

    /**
     * Handle business anomalies (such as insufficient balance, state conflict, etc.)
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> handleBusinessException(BusinessException ex) {
        return ApiResponse.error(ex.getErrorCode(), ex.getMessage());
    }

    /**
     * Handle system-level exceptions (such as database connection failure, unavailability of third-party services, etc.)
     */
    @ExceptionHandler(SystemException.class)
    public ApiResponse<?> handleSystemException(SystemException ex) {
        return ApiResponse.error(ex.getErrorCode(), ex.getMessage());
    }

    /**
     * Exception in handling authentication or authorization failure (e.g. no login, no access rights, etc.)
     */
    @ExceptionHandler(AuthException.class)
    public ApiResponse<?> handleAuthException(AuthException ex) {
        return ApiResponse.error(ex.getErrorCode(), ex.getMessage());
    }

    /**
     * No exception was found when processing resources (such as visiting nonexistent users, commodities, etc.)
     */
    @ExceptionHandler(NotFoundException.class)
    public ApiResponse<?> handleNotFoundException(NotFoundException ex) {
        return ApiResponse.error(ex.getErrorCode(), ex.getMessage());
    }

    /**
     * No exception was found when processing resources (such as visiting nonexistent users, commodities, etc.)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ApiResponse.badRequest(errors.toString());
    }

    /**
     * Exception in processing permission authentication.
     */
    @ExceptionHandler(AuthorizationException.class)
    public ApiResponse<?> handleAuthorizationException(AuthorizationException e) {
        return ApiResponse.error(UNAUTHORIZED.code(),  e.getMessage());
    }

    /**
     * Handling abnormal operation
     * 注意：这里不应该返回 401，因为 RuntimeExceptions 通常是系统错误，不是认证错误
     * 但为了安全，我们只记录日志，不暴露详细错误信息
     */
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> handleRuntimeException(RuntimeException e) {
        // 之前用 e.printStackTrace() 写到 stderr，会绕过日志框架且 System.err 内部带锁，
        // 在高并发异常场景下会成为瓶颈。改为 log.error 走 SLF4J / Logback。
        log.error("unhandled runtime exception", e);
        return ApiResponse.error(SYSTEM_ERROR.code(), e.getMessage() != null ? e.getMessage() : "系统内部错误");
    }

    /**
     * handling unauthorized=
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ApiResponse<?> handleUnauthorizedException(UnauthorizedException e) {
        return ApiResponse.error(UNAUTHORIZED.code(),  e.getMessage());
    }

    /**
     * Catch all other exceptions that are not explicitly handled to avoid direct exposure of 500 errors.
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleOther(Exception ex) {
        return ApiResponse.error(ex.getMessage());
    }

}
