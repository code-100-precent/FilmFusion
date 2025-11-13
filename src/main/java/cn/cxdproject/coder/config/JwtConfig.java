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

    @Value("${app.jwt.secret:change-me}")
    private String secret = "6Vvq8$fG3jKlP2mNpQs5tRwUyXzA7B+C9D-E)";

    @Value("${app.jwt.expiration:86400000}")
    private Long expiration = 86400000L;

    @Value("${app.jwt.header:Authorization}")
    private String tokenHeader = "Authorization";

    @Value("${app.jwt.token-start:Bearer}")
    private String tokenPrefix = "Bearer";

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

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return !isTokenBlacklisted(token);
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
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
            log.error("Invalid JWT token: {}", token, e);
            throw new JwtException("Invalid token provided.");
        }
    }

}