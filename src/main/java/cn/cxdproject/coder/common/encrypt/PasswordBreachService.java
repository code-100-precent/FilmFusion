package cn.cxdproject.coder.common.encrypt;

import org.springframework.stereotype.Service;

/**
 * 密码泄露检查服务
 * 在实际项目中，这里应该调用外部API（如Have I Been Pwned API）来检查密码是否在泄露数据库中
 * 这里简化实现，仅做模拟
 */
@Service
public class PasswordBreachService {

    /**
     * 检查密码是否在泄露数据库中
     * 实际项目中应该调用外部API进行真实检查
     * 
     * @param password 需要检查的密码
     * @return 泄露次数，0表示未泄露
     */
    public int checkPasswordBreach(String password) {
        // 在实际项目中，这里应该调用Have I Been Pwned API
        // 简化实现：仅检查一些常见弱密码
        String[] commonPasswords = {
            "123456", "password", "12345678", "qwerty", "123456789",
            "12345", "1234", "111111", "1234567", "dragon",
            "123123", "baseball", "abc123", "football", "monkey",
            "letmein", "696969", "shadow", "master", "666666"
        };
        
        for (String commonPassword : commonPasswords) {
            if (password.equals(commonPassword)) {
                return 1; // 密码在常见弱密码列表中
            }
        }
        
        return 0; // 未泄露
    }
}