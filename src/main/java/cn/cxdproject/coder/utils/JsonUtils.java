package cn.cxdproject.coder.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * JSON 工具类
 *
 * @author heathcetide
 */
public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 配置 ObjectMapper
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    /**
     * 将对象转换为 JSON 字符串
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * 将 JSON 字符串转换为对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }

    /**
     * 将 JSON 字符串转换为 ObjectNode
     */
    public static ObjectNode fromJsonToObjectNode(String json) {
        try {
            return objectMapper.readValue(json, ObjectNode.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON to ObjectNode", e);
        }
    }

    /**
     * 创建一个新的 ObjectNode
     */
    public static ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    /**
     * 将对象转换为 ObjectNode
     */
    public static ObjectNode toObjectNode(Object obj) {
        try {
            return objectMapper.convertValue(obj, ObjectNode.class);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to convert object to ObjectNode", e);
        }
    }
}
