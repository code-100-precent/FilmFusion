package cn.cxdproject.coder.utils;

import cn.hutool.crypto.digest.BCrypt;

public class PasswordEncoder {

    public static String encode(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean matches(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    /**
     * 判断一个字符串是否是 Bcrypt 加密格式（以 $2a$、$2b$、$2y$ 开头）
     */
    public static boolean isEncrypted(String password) {
        return password != null && password.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }
}
