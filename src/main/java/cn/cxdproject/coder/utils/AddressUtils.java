package cn.cxdproject.coder.utils;

import cn.cxdproject.coder.common.constants.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 地址工具类
 * 用于根据IP地址获取地理位置信息
 *
 * @author heathcetide
 */
@Slf4j
public class AddressUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // IP地址查询
    public static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";

    // 未知地址
    public static final String UNKNOWN = "XX XX";

    public static String getRealAddressByIP(String ip) {
        // 内网不查询
        if (IpUtils.internalIp(ip)) {
            return "内网IP";
        }
        try {
            String rspStr = HttpUtils.sendGet(IP_URL, "ip=" + ip + "&json=true", Constants.GBK);
            if (StringUtils.isEmpty(rspStr)) {
                log.error("获取地理位置异常 {}", ip);
                return UNKNOWN;
            }

            JsonNode root = objectMapper.readTree(rspStr);
            String region = root.path("pro").asText("");
            String city = root.path("city").asText("");

            return String.format("%s %s", region, city);
        } catch (Exception e) {
            log.error("获取地理位置异常 {}", ip, e);
        }
        return UNKNOWN;
    }

    /**
     * Main 方法用于测试地址解析功能
     */
    public static void main(String[] args) {
        // 测试内网 IP
        String internalIp = "192.168.1.100";
        String internalAddress = getRealAddressByIP(internalIp);
        System.out.println("Internal IP (" + internalIp + "): " + internalAddress);

        // 测试外网 IP（例如 8.8.8.8）
        String externalIp = "180.84.30.195";
        String externalAddress = getRealAddressByIP(externalIp);
        System.out.println("External IP (" + externalIp + "): " + externalAddress);
    }
}
