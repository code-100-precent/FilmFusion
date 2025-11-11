package cn.cxdproject.coder.common.constants;



import cn.cxdproject.coder.model.entity.Admin;

import java.security.SecureRandom;
import java.util.UUID;

import static cn.cxdproject.coder.common.constants.RoleConstants.USER;


/**
 * 管理员常量
 *
 * @author heathcetide
 */
public interface UserConstants {

    /**
     * 当前管理员
     */
    String CURRENT_USERNAME = "current_username";

    /**
     * 匿名管理员名
     */
    String DEFAULT_USERNAME = "anonymous";

    /**
     * 发送验证码
     */
    String SEND_EMAIL_CODE = "send_email_code";

    /**
     * 发送验证码发送时间
     */
    String SEND_EMAIL_CODE_SEND_TIME = "sendEmailCodeSendTime";

    /**
     * 发送间隔（分钟）
     */
    long SEND_INTERVAL = 1;

    /**
     * 验证码有效期（分钟）
     */
    Long SEND_EMAIL_CODE_TIME = 10L;

    /**
     * 登录token缓存key
     */
    String TOKEN_CACHE_KEY = "token_key";

    /**
     * 登录token缓存时间（分钟）
     */
    Long TOKEN_CACHE_TIME = 3L;

    /**
     * 管理员缓存key
     */
    String USER_CACHE_KEY = "user_key";

    /**
     * 管理员缓存时间（分钟）
     */
    Long USER_CACHE_TIME = 3L;

    String USER_STATUS_ACTIVE = "ACTIVE";

    String NEW_USER_PASSWORD = "123456";

    String EMPTY_PASSWORD_HASH = generateComplexPassword();

    String DEFAULT_USER = "https://cetide-1325039295.cos.ap-chengdu.myqcloud.com/codeforge/course/Docker/default-avatar.png";

    String USER_LANGUAGE_DEFAULT = "zh-CN";

    Boolean USER_NOTIFICATIONS_DEFAULT = true;

    Boolean USER_THEME_DARK_DEFAULT = false;

    String USER_BIO_DEFAULT = "此用户很懒, 什么也没有留下.....";

    Integer NEW_USER_GENDER = 0;

    String EMPTY_PERMISSIONS = "{}";

    Integer DEFAULT_USER_DELETED = 0;

    Long DEFAULT_USER_POINT = 0L;

    Long DEFAULT_USER_ARTICLE_COUNT = 0L;

    String EMPTY_ADDRESS = "成都";
    String NEW_USER_NICKNAME = "Coder-" + UUID.randomUUID().toString().replace("-", "").substring(22);

    Long DEFAULT_USERID = 0L;
    /**
     * 构建新管理员
     */
    static Admin BuildNewAdmin(String email) {
        return Admin.builder()
                .username(NEW_USER_NICKNAME)
                .email(email)
                .password(NEW_USER_PASSWORD)
                .avatar(DEFAULT_USER)
                .role(USER)
                .enabled(true)
                .build();
    }

    /**
     * 构建新管理员
     */
    static Admin BuildNewAdmin(String username, String email, String avatarUrl) {
        return Admin.builder()
                .username(username != null ? username : email)
                .email(email)
                .password(NEW_USER_PASSWORD + EMPTY_PASSWORD_HASH)
                .avatar(avatarUrl)
                .role(USER)
                .enabled(true)
                .build();
    }

    static String generateComplexPassword() {
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()-_=+[]{}|;:,.<>?";
        String allChars = upperCaseLetters + lowerCaseLetters + numbers + specialChars;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // 确保至少包含每种类型的字符各一个
        password.append(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length())));
        password.append(lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // 填充剩余长度至16位
        for (int i = 4; i < 16; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // 打乱字符顺序
        return shuffleString(password.toString(), random);
    }

    static String shuffleString(String string, SecureRandom random) {
        char[] chars = string.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}
