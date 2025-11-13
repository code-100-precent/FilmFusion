package cn.cxdproject.coder.common.encrypt;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MfaService {

    // 存储用户标识与对应的 MFA 验证码（示例中可为用户ID、手机号等）
    private final Map<String, String> mfaCodeStorage = new ConcurrentHashMap<>();
    // 存储用户标识与验证码的过期时间（毫秒）
    private final Map<String, Long> mfaCodeExpiration = new ConcurrentHashMap<>();

    // 验证码有效期：5 分钟（单位：毫秒）
    private static final long CODE_VALIDITY_MILLIS = 5 * 60 * 1000;

    // 安全随机数生成器
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 为指定用户生成6位数字形式的 MFA 验证码
     *
     * @param userIdentifier 用户标识，例如用户ID或手机号码
     * @return 生成的验证码字符串
     */
    public String generateMfaCode(String userIdentifier) {
        // 生成范围在 100000 至 999999 的6位数字
        int code = secureRandom.nextInt(900000) + 100000;
        String mfaCode = String.valueOf(code);
        // 存储验证码及其过期时间
        mfaCodeStorage.put(userIdentifier, mfaCode);
        mfaCodeExpiration.put(userIdentifier, System.currentTimeMillis() + CODE_VALIDITY_MILLIS);
        return mfaCode;
    }

    /**
     * 验证用户输入的 MFA 验证码是否正确且在有效期内
     *
     * @param userIdentifier 用户标识
     * @param code           用户输入的验证码
     * @return 如果验证码正确且未过期返回 true，否则返回 false
     */
    public boolean validateMfaCode(String userIdentifier, String code) {
        String storedCode = mfaCodeStorage.get(userIdentifier);
        Long expiration = mfaCodeExpiration.get(userIdentifier);

        // 未找到验证码或过期信息则返回 false
        if (storedCode == null || expiration == null) {
            return false;
        }

        // 检查验证码是否已经过期
        if (System.currentTimeMillis() > expiration) {
            // 验证码过期，清除相关记录
            mfaCodeStorage.remove(userIdentifier);
            mfaCodeExpiration.remove(userIdentifier);
            return false;
        }

        // 验证码匹配则返回 true，并清除记录防止重复使用
        if (storedCode.equals(code)) {
            mfaCodeStorage.remove(userIdentifier);
            mfaCodeExpiration.remove(userIdentifier);
            return true;
        }
        return false;
    }
}
