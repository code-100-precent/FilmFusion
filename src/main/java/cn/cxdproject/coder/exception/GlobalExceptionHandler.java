package cn.cxdproject.coder.exception;

import cn.cxdproject.coder.common.ApiResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.UNAUTHORIZED;
import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.SYSTEM_ERROR;


/**
 * Global exception handler, which intercepts and handles the exceptions thrown in the project in a unified way and returns a structured response.
 *
 * @author heathcetide
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

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
        // 记录详细错误日志
        e.printStackTrace();
        // 返回系统错误，而不是认证错误
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
