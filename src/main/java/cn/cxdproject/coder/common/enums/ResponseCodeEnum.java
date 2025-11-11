package cn.cxdproject.coder.common.enums;


public enum ResponseCodeEnum {

    // ========== 成功响应 ==========
    SUCCESS(200, "请求成功"),

    // ========== 通用系统错误 ==========
    SYSTEM_ERROR(500, "系统内部错误"),
    DB_ERROR(501, "数据库异常"),
    SERVICE_UNAVAILABLE(502, "服务不可用"),
    THIRD_PARTY_ERROR(503, "第三方服务调用失败"),
    RATE_LIMIT_EXCEEDED(429, "请求频率过高"),

    // ========== 参数与校验相关 ==========
    VALIDATION_ERROR(400, "参数校验失败"),
    MISSING_PARAMETER(401, "缺少必要参数"),
    ILLEGAL_ARGUMENT(402, "参数不合法"),

    // ========== 认证与权限 ==========
    UNAUTHORIZED(401, "用户未登录或Token无效"),
    FORBIDDEN(403, "没有权限访问资源"),

    // ========== 业务逻辑错误 ==========
    BUSINESS_ERROR(550, "业务处理异常"),
    INSUFFICIENT_BALANCE(551, "余额不足"),
    ITEM_OUT_OF_STOCK(552, "商品库存不足"),

    // ========== 资源相关 ==========
    ALREADY_MEMBER(409, "已是该组织成员"),
    USAGE_LIMIT_EXCEEDED(410, "已达最大使用次数"),
    NOT_FOUND(404, "资源不存在"),
    DUPLICATE_RESOURCE(409, "资源已存在"),
    EXPIRED(410, "资源已过期");

    private final int code;
    private final String message;

    ResponseCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return code + ": " + message;
    }
}
