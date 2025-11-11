package cn.cxdproject.coder.common.encrypt;

import org.springframework.stereotype.Service;

@Service
public class PasswordStrengthMeter {

    public enum PasswordStrength {
        WEAK,
        MEDIUM,
        STRONG,
        VERY_STRONG
    }

    /**
     * 计算密码强度
     *
     * @param password 待测密码
     * @return 密码强度等级（WEAK、MEDIUM、STRONG、VERY_STRONG）
     */
    public PasswordStrength measurePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordStrength.WEAK;
        }

        int score = 0;

        // 长度检查：8 位及以上得分，12 位及以上额外得分
        if (password.length() >= 8) {
            score++;
        }
        if (password.length() >= 12) {
            score++;
        }

        // 检查是否包含大写字母
        if (password.matches(".*[A-Z].*")) {
            score++;
        }
        // 检查是否包含小写字母
        if (password.matches(".*[a-z].*")) {
            score++;
        }
        // 检查是否包含数字
        if (password.matches(".*\\d.*")) {
            score++;
        }
        // 检查是否包含特殊字符（部分常见特殊字符）
        if (password.matches(".*[!@#$%^&*()\\-+].*")) {
            score++;
        }

        // 根据得分返回对应的密码强度等级
        if (score <= 2) {
            return PasswordStrength.WEAK;
        } else if (score <= 4) {
            return PasswordStrength.MEDIUM;
        } else if (score == 5) {
            return PasswordStrength.STRONG;
        } else {
            return PasswordStrength.VERY_STRONG;
        }
    }
}
