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
     * 为管理员生成Token
     */
//    public String generateToken(Admin admin) {
//        if (admin == null) {
//            return null;
//        }
//        HashMap<String, Object> objectObjectHashMap = new HashMap<>();
//        objectObjectHashMap.put("id", admin.getId());
//        objectObjectHashMap.put("username", admin.getUsername());
//        objectObjectHashMap.put("email", admin.getEmail());
//        objectObjectHashMap.put("avatar", admin.getAvatar());
//        objectObjectHashMap.put("role", admin.getRole());
//        objectObjectHashMap.put("type", "admin");
//        return Jwts.builder()
//            .setSubject(String.valueOf(admin.getId()))
//            .setClaims(objectObjectHashMap)
//            .setIssuedAt(new Date())
//            .setExpiration(new Date(System.currentTimeMillis() + expiration))
//            .signWith(SignatureAlgorithm.HS512, secret)
//            .compact();
//    }
//
//    public boolean validateToken(String token) {
//        try {
//            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
//            return !isTokenBlacklisted(token);
//        } catch (ExpiredJwtException e) {
//            log.debug("JWT token expired: {}", e.getMessage());
//            return false;
//        } catch (Exception e) {
//            log.warn("Invalid JWT token: {}", e.getMessage());
//            return false;
//        }
//    }

//    private boolean isTokenBlacklisted(String token) {
//        try {
//                Admin admin = getAdminFromToken(token);
//                return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("jwt:blacklist:" + admin.getEmail(), token));
//        } catch (Exception e) {
//            log.debug("Cannot check token blacklist due to invalid token: {}", e.getMessage());
//            return false;
//        }
//    }

    // token type is always admin in template
    public String getTokenType(String token) { return "admin"; }

    /**
     * 从Token中获取Claims（供内部使用）
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

//    public String refreshToken(String oldToken) {
//        if (validateToken(oldToken)) {
//            Date now = new Date();
//            Date expiryDate = getExpirationDateFromToken(oldToken);
//            if (expiryDate.getTime() - now.getTime() < 300000) {
//                    return generateToken(getAdminFromToken(oldToken));
//            }
//        }
//        return null;
//    }
    
//    public void invalidateToken(String token) {
//            Admin admin = getAdminFromToken(token);
//            redisTemplate.opsForSet().add("jwt:blacklist:" + admin.getEmail(), token);
//    }

    // template omits scheduled cleanup; rely on TTL policies if needed

    private Date getExpirationDateFromToken(String token) {
        return Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody()
            .getExpiration();
    }
//
//    public Admin getAdminFromToken(String token) {
//        try {
//            Claims claims = Jwts.parser()
//                    .setSigningKey(secret)
//                    .parseClaimsJws(token)
//                    .getBody();
//
//            Long adminId = claims.get("id", Long.class);
//            String username = claims.get("username", String.class);
//            if (username == null || username.isEmpty()) {
//                throw new IllegalArgumentException("Username is missing or empty in the token");
//            }
//
//            String email = claims.get("email", String.class);
//            String avatarUrl = claims.get("avatar", String.class);
//            String role = claims.get("role", String.class);
//
//            return Admin.builder()
//                    .id(adminId)
//                    .username(username)
//                    .email(email)
//                    .avatar(avatarUrl)
//                    .role(role)
//                    .build();
//        } catch (JwtException e) {
//            log.error("Invalid JWT token: {}", token, e);
//            throw new JwtException("Invalid token provided.");
//        }
//    }
//
//    // visitor token operations removed in template
//
//    /**
//     * @deprecated Use getAdminFromToken instead
//     */
//    @Deprecated
//    public Admin getUserFromToken(String token) {
//        return getAdminFromToken(token);
//    }

}