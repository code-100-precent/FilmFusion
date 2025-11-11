package cn.cxdproject.coder.common.enums;


/**
 * 通用操作日志类型枚举
 *
 * @author heathcetide
 */
public enum LogType {

    USER_LOGIN("用户登录"),

    USER_REGISTER("用户注册"),

    USER_UPDATE("用户信息更新"),

    USER_DELETE("用户删除"),

    USER_LOGOUT("用户退出登录"),

    AUTH("认证与授权"),

    SYSTEM_MESSAGE("系统管理"),

    USER_MANAGEMENT("用户管理"),

    DATA_MANAGEMENT("数据管理"),

    CONFIGURATION("系统配置"),

    MONITORING("系统监控"),

    SECURITY("安全审计"),

    API_CALL("接口调用"),

    ORG_MODULE("组织接口调用"),

    TASK_EXECUTION("任务执行"),

    THIRD_PARTY("第三方服务"),

    OTHER("其他");

    private final String description;

    LogType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
