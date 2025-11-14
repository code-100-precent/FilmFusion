package cn.cxdproject.coder.common.constants;

public interface RedisKeyConstants {

    String PREFIX = "web_scanner:";
    
    String TASK_RESULT_KEY = PREFIX + "task:result:";

    String TASK_LOCK_KEY = PREFIX + "task:lock:";

    String SCAN_RATE_LIMIT = PREFIX + "rate_limit:";

    String ARTICLE_DETAIL_PREFIX = "article:detail:";
    String CAPTCHA_PREFIX = "captcha:";
    String ARTICLE = "article";
    
    static String getTaskResultKey(Long taskId) {
        return TASK_RESULT_KEY + taskId;
    }
    
    static String getTaskLockKey(Long taskId) {
        return TASK_LOCK_KEY + taskId;
    }
    
    static String getScanRateLimitKey(String target) {
        return SCAN_RATE_LIMIT + target;
    }
} 