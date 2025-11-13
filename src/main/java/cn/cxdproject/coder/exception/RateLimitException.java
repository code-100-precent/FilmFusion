package cn.cxdproject.coder.exception;

/**
 * 限流异常类
 * 当限流器操作失败时抛出
 * 
 * @author Hibiscus
 * @since 1.0.0
 */
public class RateLimitException extends RuntimeException {
    
    public RateLimitException(String message) {
        super(message);
    }
    
    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public RateLimitException(Throwable cause) {
        super(cause);
    }
}
