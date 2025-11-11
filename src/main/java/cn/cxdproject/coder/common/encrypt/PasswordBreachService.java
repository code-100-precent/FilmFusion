package cn.cxdproject.coder.common.encrypt;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class PasswordBreachService {

 // HIBP API 地址，传入密码 SHA-1 前5位进行查询
 private static final String HIBP_API_URL = "https://api.pwnedpasswords.com/range/";

 private final RestTemplate restTemplate;

 public PasswordBreachService() {
  this.restTemplate = new RestTemplate();
 }

 /**
  * 检查密码是否存在泄露风险。
  * 方法将返回该密码在泄露库中出现的次数，
  * 若返回 0 则表示未检测到泄露记录。
  *
  * @param password 需要检查的密码
  * @return 泄露次数
  */
 public int checkPasswordBreach(String password) {
  try {
   // 计算密码的 SHA-1 哈希值（大写）
   MessageDigest digest = MessageDigest.getInstance("SHA-1");
   byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
   String hash = bytesToHex(hashBytes).toUpperCase();

   // 根据 HIBP API 要求，取哈希值前 5 位作为前缀，其余作为后缀
   String prefix = hash.substring(0, 5);
   String suffix = hash.substring(5);

   // 构造 API URL，发起 GET 请求
   String url = HIBP_API_URL + prefix;
   String response = restTemplate.getForObject(url, String.class);

   if (response == null) {
    return 0;
   }

   // 解析返回结果，格式为：HASH_SUFFIX:COUNT
   String[] lines = response.split("\\r?\\n");
   for (String line : lines) {
    String[] parts = line.split(":");
    if (parts.length == 2) {
     String returnedSuffix = parts[0];
     int count = Integer.parseInt(parts[1].trim());
     if (returnedSuffix.equalsIgnoreCase(suffix)) {
      return count;
     }
    }
   }
  } catch (NoSuchAlgorithmException e) {
   throw new RuntimeException("SHA-1 算法不可用", e);
  } catch (Exception e) {
   throw new RuntimeException("调用 HIBP API 检查密码泄露失败", e);
  }
  return 0;
 }

 /**
  * 将字节数组转换为十六进制字符串
  *
  * @param bytes 字节数组
  * @return 十六进制字符串
  */
 private String bytesToHex(byte[] bytes) {
  StringBuilder sb = new StringBuilder();
  for (byte b : bytes) {
   sb.append(String.format("%02x", b));
  }
  return sb.toString();
 }
}
