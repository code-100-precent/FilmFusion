package cn.cxdproject.coder.common.encrypt;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class PasswordEncryptionService {

    // ===================== BCrypt 实现 =====================
    /**
     * 使用 BCrypt 对明文密码进行加密
     * @param plainText 明文密码
     * @return BCrypt 加密后的散列值
     */
    public String encodeWithBCrypt(String plainText) {
        // 默认工作因子 10
        return BCrypt.hashpw(plainText, BCrypt.gensalt(10));
    }

    /**
     * 使用 BCrypt 验证明文密码与加密散列是否匹配
     * @param plainText 明文密码
     * @param hash 已加密的散列值
     * @return 是否匹配
     */
    public boolean matchesBCrypt(String plainText, String hash) {
        return BCrypt.checkpw(plainText, hash);
    }

    // ===================== PBKDF2 实现 =====================
    private static final int PBKDF2_ITERATIONS = 185000; // 迭代次数
    private static final int SALT_BYTE_SIZE = 16;         // 盐值字节数
    private static final int HASH_BYTE_SIZE = 32;         // 生成散列的字节数（256位）
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"; // 算法

    /**
     * 使用 PBKDF2 对明文密码进行加密，返回格式： iterations:salt:hash
     * 其中 salt 和 hash 均为 Base64 编码
     * @param plainText 明文密码
     * @return PBKDF2 加密后的字符串
     */
    public String encodeWithPBKDF2(String plainText) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_BYTE_SIZE];
            random.nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(plainText.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();

            // 返回格式：iterations:salt:hash
            return PBKDF2_ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt)
                    + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("PBKDF2 加密错误", e);
        }
    }

    /**
     * 验证明文密码与存储的 PBKDF2 加密字符串是否匹配
     * @param plainText 明文密码
     * @param stored 存储格式为 iterations:salt:hash 的字符串
     * @return 是否匹配
     */
    public boolean matchesPBKDF2(String plainText, String stored) {
        try {
            String[] parts = stored.split(":");
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] storedHash = Base64.getDecoder().decode(parts[2]);

            PBEKeySpec spec = new PBEKeySpec(plainText.toCharArray(), salt, iterations, storedHash.length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] testHash = skf.generateSecret(spec).getEncoded();
            return Arrays.equals(storedHash, testHash);
        } catch (Exception e) {
            throw new RuntimeException("PBKDF2 校验错误", e);
        }
    }

    // ===================== 测试 main 方法 =====================
    public static void main(String[] args) {
        PasswordEncryptionService service = new PasswordEncryptionService();
        String plainPassword = "mySecretPassword";

        // BCrypt 示例
        System.out.println("=== BCrypt 加密/验证 ===");
        String bcryptHash = service.encodeWithBCrypt(plainPassword);
        System.out.println("BCrypt 加密后的散列: " + bcryptHash);
        System.out.println("BCrypt 验证: " + service.matchesBCrypt(plainPassword, bcryptHash));

        // PBKDF2 示例
        System.out.println("\n=== PBKDF2 加密/验证 ===");
        String pbkdf2Hash = service.encodeWithPBKDF2(plainPassword);
        System.out.println("PBKDF2 加密后的字符串: " + pbkdf2Hash);
        System.out.println("PBKDF2 验证: " + service.matchesPBKDF2(plainPassword, pbkdf2Hash));
    }
}