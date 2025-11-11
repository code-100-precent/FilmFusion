package cn.cxdproject.coder.common.csrf;

import cn.cxdproject.coder.utils.RedisUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * CSRF Token仓库
 * 用于生成和验证CSRF Token
 */
@Component
public class CsrfTokenRepository {

    private final RedisUtils redisUtils;

    private static final String CSRF_KEY_PREFIX = "csrf:";
    private static final long TOKEN_VALID_SECONDS = 7200; // 2小时

    public CsrfTokenRepository(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    /**
     * 生成CSRF Token
     * @param sessionId 会话ID
     * @return CSRF Token
     */
    public String generateToken(String sessionId) {
        String token = UUID.randomUUID().toString();
        String key = CSRF_KEY_PREFIX + sessionId;
        redisUtils.set(key, token, TOKEN_VALID_SECONDS);
        return token;
    }

    /**
     * 获取已有的Token（不生成）
     * @param sessionId 会话ID
     * @return 现有Token（或null）
     */
    public String getToken(String sessionId) {
        String key = CSRF_KEY_PREFIX + sessionId;
        Object token = redisUtils.get(key);
        return token != null ? token.toString() : null;
    }

    /**
     * 验证CSRF Token
     * @param sessionId 会话ID
     * @param token CSRF Token
     * @return 是否有效
     */
    public boolean validateToken(String sessionId, String token) {
        String key = CSRF_KEY_PREFIX + sessionId;
        Object storedToken = redisUtils.get(key);
        return token != null && token.equals(storedToken);
    }

    /**
     * 移除CSRF Token（可用于登出）
     * @param sessionId 会话ID
     */
    public void removeToken(String sessionId) {
        String key = CSRF_KEY_PREFIX + sessionId;
        redisUtils.delete(key);
    }
}
