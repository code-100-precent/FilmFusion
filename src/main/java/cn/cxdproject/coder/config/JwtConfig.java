package cn.cxdproject.coder.config;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;

/**
 * JWT令牌工具类
 *
 * @author heathcetide
 */
@Slf4j
@Component
public class JwtConfig {

    private final RedisTemplate<String, String> redisTemplate;

    // 注意：配置前缀必须与 application.yml 中的 code100.jwt.* 保持一致
    @Value("${code100.jwt.secret:change-me}")
    private String secret = "change-me";

    @Value("${code100.jwt.expiration:86400000}")
    private Long expiration = 86400000L;

    @Value("${code100.jwt.header:Authorization}")
    private String tokenHeader = "Authorization";

    @Value("${code100.jwt.token-start:Bearer}")
    private String tokenPrefix = "Bearer";

    /**
     * 用户级 token 失效时间戳的 Redis key 前缀。
     * 任何在该时间之前签发的 token 都视为无效。
     */
    private static final String USER_INVALID_BEFORE_KEY = "jwt:user-invalid-before:";

    public JwtConfig(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getTokenFromHeader(HttpServletRequest request) {
        String header = request.getHeader(tokenHeader);
        if (header != null && header.startsWith(tokenPrefix + " ")) {
            return header.substring(tokenPrefix.length() + 1);
        }
        return null;
    }

    /**
     * 为用户生成Token
     */
    public String generateToken(cn.cxdproject.coder.model.entity.User user) {
        if (user == null) {
            return null;
        }
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        claims.put("phoneNumber", user.getPhoneNumber());
        claims.put("avatar", user.getAvatar());
        claims.put("role", user.getRole());
        claims.put("enabled", user.getEnabled());
        claims.put("type", "user");
        return Jwts.builder()
            .setSubject(String.valueOf(user.getId()))
            .setClaims(claims)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }

    /**
     * 解析并校验 token，一次性完成签名验证 + 黑名单 + 用户级失效检查。
     *
     * <p>注意：本方法替代了原来"先 validateToken → 再 getTokenType → 再 getUserFromToken"
     * 的 3~4 次重复解析路径，把 JWT 签名验证从每次请求 4 次降到 1 次，
     * 显著降低受保护接口的固定开销。
     *
     * @return 合法时返回 {@link Claims}，非法 / 过期 / 已撤销时返回 {@code null}
     */
    public Claims parseAndValidate(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();

            String username = claims.get("username", String.class);
            if (username != null) {
                // 黑名单：单个 token 被显式吊销
                if (Boolean.TRUE.equals(redisTemplate.opsForSet()
                        .isMember("jwt:blacklist:" + username, token))) {
                    return null;
                }
                // 用户级失效：该用户在某时刻之前签发的所有 token 一律视为无效
                String ts = redisTemplate.opsForValue().get(USER_INVALID_BEFORE_KEY + username);
                if (ts != null) {
                    long invalidBefore = Long.parseLong(ts);
                    Date issuedAt = claims.getIssuedAt();
                    if (issuedAt != null && issuedAt.getTime() < invalidBefore) {
                        return null;
                    }
                }
            }
            return claims;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired");
            return null;
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        return parseAndValidate(token) != null;
    }

    private boolean isTokenBlacklisted(String token) {
        try {
            cn.cxdproject.coder.model.entity.User user = getUserFromToken(token);
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("jwt:blacklist:" + user.getUsername(), token));
        } catch (Exception e) {
            log.debug("Cannot check token blacklist due to invalid token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从已解析的 Claims 中读取 token 类型。
     */
    public String getTokenType(Claims claims) {
        return claims == null ? null : claims.get("type", String.class);
    }

    /**
     * 获取Token类型
     */
    public String getTokenType(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("type", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从已解析的 Claims 直接构造 User，避免再次签名验签。
     */
    public cn.cxdproject.coder.model.entity.User getUserFromClaims(Claims claims) {
        if (claims == null) {
            throw new JwtException("Claims is null");
        }
        Long userId = claims.get("id", Long.class);
        String username = claims.get("username", String.class);
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username is missing or empty in the token");
        }
        String phoneNumber = claims.get("phoneNumber", String.class);
        String avatarUrl = claims.get("avatar", String.class);
        String role = claims.get("role", String.class);
        Boolean enabled = claims.get("enabled", Boolean.class);

        return cn.cxdproject.coder.model.entity.User.builder()
                .id(userId)
                .username(username)
                .phoneNumber(phoneNumber)
                .avatar(avatarUrl)
                .role(role)
                .enabled(enabled)
                .build();
    }

    /**
     * 从Token中获取Claims（供内部使用）
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    public String refreshToken(String oldToken) {
        if (validateToken(oldToken)) {
            Date now = new Date();
            Date expiryDate = getExpirationDateFromToken(oldToken);
            if (expiryDate.getTime() - now.getTime() < 300000) {
                    return generateToken(getUserFromToken(oldToken));
            }
        }
        return null;
    }
    
    public void invalidateToken(String token) {
            cn.cxdproject.coder.model.entity.User user = getUserFromToken(token);
            redisTemplate.opsForSet().add("jwt:blacklist:" + user.getUsername(), token);
    }

    /**
     * 让指定用户在此刻之前签发的所有 token 立即失效。
     * 用于删除/禁用用户、强制下线等场景。
     */
    public void invalidateAllUserTokens(String username) {
        if (username == null || username.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        redisTemplate.opsForValue().set(USER_INVALID_BEFORE_KEY + username, String.valueOf(now));
    }

    // template omits scheduled cleanup; rely on TTL policies if needed

    private Date getExpirationDateFromToken(String token) {
        return Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody()
            .getExpiration();
    }
//
    public cn.cxdproject.coder.model.entity.User getUserFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();

            Long userId = claims.get("id", Long.class);
            String username = claims.get("username", String.class);
            if (username == null || username.isEmpty()) {
                throw new IllegalArgumentException("Username is missing or empty in the token");
            }

            String phoneNumber = claims.get("phoneNumber", String.class);
            String avatarUrl = claims.get("avatar", String.class);
            String role = claims.get("role", String.class);
            Boolean enabled = claims.get("enabled", Boolean.class);

            return cn.cxdproject.coder.model.entity.User.builder()
                    .id(userId)
                    .username(username)
                    .phoneNumber(phoneNumber)
                    .avatar(avatarUrl)
                    .role(role)
                    .enabled(enabled)
                    .build();
        } catch (JwtException e) {
            // 不要打印完整 token，避免敏感信息泄露
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new JwtException("Invalid token provided.");
        }
    }

}