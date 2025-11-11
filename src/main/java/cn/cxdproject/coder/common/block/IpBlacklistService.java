package cn.cxdproject.coder.common.block;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IpBlacklistService {

    private static final int MAX_ATTEMPTS = 100;
    private static final long ATTEMPT_WINDOW_SECONDS = 3600; // 1小时
    private static final long BLOCK_DURATION_SECONDS = 86400; // 24小时

    private final Map<String, BlacklistEntry> blacklist = new ConcurrentHashMap<>();
    private final Map<String, IpAttempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlacklisted(String ip) {
        BlacklistEntry entry = blacklist.get(ip);
        return entry != null && entry.getExpiresAt() > Instant.now().getEpochSecond();
    }

    public void addToBlacklist(String ip, String reason) {
        long expiresAt = Instant.now().getEpochSecond() + BLOCK_DURATION_SECONDS;
        blacklist.put(ip, new BlacklistEntry(reason, expiresAt));
    }

    public void removeFromBlacklist(String ip) {
        blacklist.remove(ip);
    }

    public void recordIpAttempt(String ip) {
        IpAttempt attempt = attempts.computeIfAbsent(ip, k -> new IpAttempt());
        long now = Instant.now().getEpochSecond();

        synchronized (attempt) {
            if (now - attempt.firstAttemptAt > ATTEMPT_WINDOW_SECONDS) {
                attempt.count = 1;
                attempt.firstAttemptAt = now;
            } else {
                attempt.count++;
            }

            if (attempt.count > MAX_ATTEMPTS) {
                addToBlacklist(ip, "请求频率过高");
            }
        }
    }

    public Set<String> getAllBlacklistedIps() {
        return blacklist.keySet();
    }

    // 每分钟清理一次过期黑名单（可调频率）
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredBlacklist() {
        long now = Instant.now().getEpochSecond();
        blacklist.entrySet().removeIf(entry -> entry.getValue().getExpiresAt() <= now);
    }

    // 内部类：黑名单信息
    private static class BlacklistEntry {
        private final String reason;
        private final long expiresAt;

        public BlacklistEntry(String reason, long expiresAt) {
            this.reason = reason;
            this.expiresAt = expiresAt;
        }

        public long getExpiresAt() {
            return expiresAt;
        }

        public String getReason() {
            return reason;
        }
    }

    // 内部类：IP 尝试记录
    private static class IpAttempt {
        private int count = 0;
        private long firstAttemptAt = Instant.now().getEpochSecond();
    }
}
