package cn.cxdproject.coder.utils;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * Invite Code
 *
 * @author heathcetide
 */
public class CodeUtil {

    /**
     * Invite Code
     */
    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String encode(long id) {
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.insert(0, BASE62.charAt((int) (id % 62)));
            id /= 62;
        }
        return sb.toString();
    }

    public static long decode(String shortCode) {
        long result = 0;
        for (char c : shortCode.toCharArray()) {
            result *= 62;
            if ('0' <= c && c <= '9') result += c - '0';
            else if ('a' <= c && c <= 'z') result += c - 'a' + 10;
            else if ('A' <= c && c <= 'Z') result += c - 'A' + 36;
        }
        return result;
    }

    public static void main(String[] args) {
        long longs = SnowflakeIdGenerator.nextId();
        System.out.println(longs);
        System.out.println(encode(longs));
        System.out.println(decode(encode(longs)));
    }


    /**
     * 构建md5加密的redis-key
     */
    public static String generateRedisKey(String email, String prefix) {
        String hashedEmail = DigestUtils.md5DigestAsHex(email.getBytes(StandardCharsets.UTF_8)); // 使用 Spring 提供的 MD5 工具
        return prefix + hashedEmail;
    }

    /**
     * 生成安全的六位验证码
     */
    public static String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        int length = 6; // 验证码长度
        StringBuilder sb = new StringBuilder(length);
        String chars = "0123456789"; // 可扩展为字母和数字组合，如 "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}