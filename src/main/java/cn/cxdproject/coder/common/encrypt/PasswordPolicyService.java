package cn.cxdproject.coder.common.encrypt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PasswordPolicyService {

    private final PasswordBreachService passwordBreachService;

    // 密码策略常量
    private static final int MIN_LENGTH = 8;

    @Autowired
    public PasswordPolicyService(PasswordBreachService passwordBreachService) {
        this.passwordBreachService = passwordBreachService;
    }

    /**
     * 校验密码是否符合安全策略要求：
     * 1. 长度不小于 MIN_LENGTH
     * 2. 包含至少一个大写字母、一个小写字母、一个数字和一个特殊字符
     * 3. 不存在于已泄露的密码库中
     *
     * @param password 需要校验的密码
     * @return 如果密码符合策略返回 true，否则返回 false
     */
    public boolean validatePassword(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return false;
        }

        // 检查是否包含大写字母
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        // 检查是否包含小写字母
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        // 检查是否包含数字
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        // 检查是否包含特殊字符（这里只匹配部分常用特殊字符）
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()\\-+].*");

        if (!hasUppercase || !hasLowercase || !hasDigit || !hasSpecialChar) {
            return false;
        }

        // 检查密码是否在泄露数据库中出现
        int breachCount = passwordBreachService.checkPasswordBreach(password);
        if (breachCount > 0) {
            return false;
        }

        return true;
    }

    /**
     * 返回密码策略的描述信息
     *
     * @return 密码策略描述
     */
    public String getPolicyDescription() {
        return "密码至少 " + MIN_LENGTH + " 位，必须包含大写字母、小写字母、数字和特殊字符，且不能出现在已知的密码泄露库中。";
    }
}
