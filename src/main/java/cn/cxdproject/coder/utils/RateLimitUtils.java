package cn.cxdproject.coder.utils;

import java.util.concurrent.TimeUnit;

/**
 * 限流工具类
 * 提供常用的工具方法
 * 
 * @author Hibiscus
 * @since 1.0.0
 */
public class RateLimitUtils {
    
    /**
     * 私有构造函数，防止实例化
     */
    private RateLimitUtils() {}
    
    /**
     * 将时间转换为纳秒
     */
    public static long toNanos(long time, TimeUnit unit) {
        return unit.toNanos(time);
    }
    
    /**
     * 将纳秒转换为指定时间单位
     */
    public static long fromNanos(long nanos, TimeUnit unit) {
        return unit.convert(nanos, TimeUnit.NANOSECONDS);
    }
    
    /**
     * 计算QPS（每秒查询数）
     */
    public static double calculateQps(long count, long timeMillis) {
        if (timeMillis <= 0) {
            return 0.0;
        }
        return (double) count * 1000 / timeMillis;
    }
    
    /**
     * 计算成功率
     */
    public static double calculateSuccessRate(long success, long total) {
        if (total <= 0) {
            return 0.0;
        }
        return (double) success / total;
    }
    
    /**
     * 计算限流率
     */
    public static double calculateLimitRate(long limited, long total) {
        if (total <= 0) {
            return 0.0;
        }
        return (double) limited / total;
    }
    
    /**
     * 格式化百分比
     */
    public static String formatPercentage(double rate) {
        return String.format("%.2f%%", rate * 100);
    }
    
    /**
     * 格式化时间（毫秒）
     */
    public static String formatTime(long timeMillis) {
        if (timeMillis < 1000) {
            return timeMillis + "ms";
        } else if (timeMillis < 60000) {
            return String.format("%.2fs", timeMillis / 1000.0);
        } else {
            long minutes = timeMillis / 60000;
            long seconds = (timeMillis % 60000) / 1000;
            return minutes + "m " + seconds + "s";
        }
    }
    
    /**
     * 检查参数是否有效
     */
    public static void validateRange(long value, long min, long max, String paramName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                String.format("%s must be between %d and %d, got: %d", paramName, min, max, value)
            );
        }
    }
    
    /**
     * 检查参数是否为正数
     */
    public static void validatePositive(long value, String paramName) {
        if (value <= 0) {
            throw new IllegalArgumentException(
                String.format("%s must be positive, got: %d", paramName, value)
            );
        }
    }
    
    /**
     * 检查参数是否为非负数
     */
    public static void validateNonNegative(long value, String paramName) {
        if (value < 0) {
            throw new IllegalArgumentException(
                String.format("%s must be non-negative, got: %d", paramName, value)
            );
        }
    }
    
    /**
     * 检查参数是否为非负数
     */
    public static void validateNonNegative(double value, String paramName) {
        if (value < 0) {
            throw new IllegalArgumentException(
                String.format("%s must be non-negative, got: %f", paramName, value)
            );
        }
    }
    
    /**
     * 生成限流键
     */
    public static String generateKey(String prefix, String granularity, String identifier) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return granularity + ":" + identifier;
        }
        return prefix + ":" + granularity + ":" + identifier;
    }
    
    /**
     * 计算令牌桶的填充时间
     */
    public static long calculateRefillTime(long tokens, double refillRate) {
        if (refillRate <= 0) {
            return Long.MAX_VALUE;
        }
        return (long) (tokens * 1000 / refillRate);
    }
}
