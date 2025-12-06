package cn.cxdproject.coder.common.enums;


import lombok.Getter;

/**
 * 通用操作日志类型枚举
 *
 * @author heathcetide
 */
@Getter
public enum LogType {

    // 用户相关
    USER_LOGIN("USER_LOGIN", "用户登录"),
    USER_LOGOUT("USER_LOGOUT", "用户登出"),
    USER_CREATE("USER_CREATE", "创建用户"),
    USER_UPDATE("USER_UPDATE", "更新用户信息"),
    USER_DELETE("USER_DELETE", "删除用户"),
    AVATAR_UPDATE("AVATAR_UPDATE","用户更新头像"),

    // 文章内容相关
    ARTICLE_GET("ARTICLE_GET", "根据id查找文章"),
    ARTICLE_ADMIN_GET_PAGE("ARTICLE_ADMIN_GET_PAGE", "管理员分页查询"),
    ARTICLE_USER_GET_PAGE("ARTICLE_USER_GET_PAGE", "用户分页查询"),
    ARTICLE_UPDATE("ARTICLE_UPDATE", "更新文章"),
    ARTICLE_DELETE("ARTICLE_DELETE","删除文章"),
    ARTICLE_CREATE("ARTICLE_CREATE","新建文章"),

    // 系统操作
    FILE_UPLOAD("FILE_UPLOAD", "上传文件"),

    //影视相关操作
    DRAMA_GET("DRAMA_GET", "根据ID查找影视"),
    DRAMA_ADMIN_GET_PAGE("DRAMA_ADMIN_GET_PAGE", "管理员分页查询影视"),
    DRAMA_USER_GET_PAGE("DRAMA_USER_GET_PAGE", "用户分页查询影视"),
    DRAMA_UPDATE("DRAMA_UPDATE", "更新影视"),
    DRAMA_DELETE("DRAMA_DELETE", "删除影视"),
    DRAMA_CREATE("DRAMA_CREATE", "新建影视"),

    //酒店相关操作
    HOTEL_GET("HOTEL_GET", "根据ID查找酒店"),
    HOTEL_ADMIN_GET_PAGE("HOTEL_ADMIN_GET_PAGE", "管理员分页查询酒店"),
    HOTEL_USER_GET_PAGE("HOTEL_USER_GET_PAGE", "用户分页查询酒店"),
    HOTEL_UPDATE("HOTEL_UPDATE", "更新酒店"),
    HOTEL_DELETE("HOTEL_DELETE", "删除酒店"),
    HOTEL_CREATE("HOTEL_CREATE", "新建酒店"),

    //场地相关操作
    LOCATION_GET("LOCATION_GET", "根据ID查找场地"),
    LOCATION_ADMIN_GET_PAGE("LOCATION_ADMIN_GET_PAGE", "管理员分页查询场地"),
    LOCATION_USER_GET_PAGE("LOCATION_USER_GET_PAGE", "用户分页查询场地"),
    LOCATION_UPDATE("LOCATION_UPDATE", "更新场地"),
    LOCATION_DELETE("LOCATION_DELETE", "删除场地"),
    LOCATION_CREATE("LOCATION_CREATE", "新建场地"),

    //政策相关操作
    POLICY_GET("POLICY_GET", "根据ID查找政策"),
    POLICY_ADMIN_GET_PAGE("POLICY_ADMIN_GET_PAGE", "管理员分页查询政策"),
    POLICY_USER_GET_PAGE("POLICY_USER_GET_PAGE", "用户分页查询政策"),
    POLICY_UPDATE("POLICY_UPDATE", "更新政策"),
    POLICY_DELETE("POLICY_DELETE", "删除政策"),
    POLICY_CREATE("POLICY_CREATE", "新建政策"),

    //申请相关操作
    REPORT_USER_CREATE("REPORT_USER_CREATE", "用户提交申请"),
    REPORT_USER_UPDATE("REPORT_USER_UPDATE", "用户更新申请"),
    REPORT_USER_DELETE("REPORT_USER_DELETE", "用户撤销申请"),
    REPORT_USER_GET("REPORT_USER_GET", "用户根据ID查询申请"),
    REPORT_USER_GET_PAGE("REPORT_USER_GET_PAGE", "查询自己的全部申请"),
    REPORT_ADMIN_UPDATE("REPORT_ADMIN_UPDATE", "管理员处理申请"),
    REPORT_ADMIN_GET("REPORT_ADMIN_GET", "管理员根据ID查询申请"),
    REPORT_ADMIN_GET_PAGE("REPORT_ADMIN_GET_PAGE", "管理员分页查询申请"),

    //协拍服务相关操作
    SHOOT_GET("SHOOT_GET", "根据ID查找协拍"),
    SHOOT_ADMIN_GET_PAGE("SHOOT_ADMIN_GET_PAGE", "管理员分页查询协拍"),
    SHOOT_USER_GET_PAGE("SHOOT_USER_GET_PAGE", "用户分页查询协拍"),
    SHOOT_UPDATE("SHOOT_UPDATE", "更新协拍"),
    SHOOT_DELETE("SHOOT_DELETE", "删除协拍"),
    SHOOT_CREATE("SHOOT_CREATE", "新建协拍"),

    //旅游路线相关服务
    TOUR_GET("TOUR_GET", "根据ID查找旅游路线"),
    TOUR_ADMIN_GET_PAGE("TOUR_ADMIN_GET_PAGE", "管理员分页查询旅游路线"),
    TOUR_USER_GET_PAGE("TOUR_USER_GET_PAGE", "用户分页查询旅游路线"),
    TOUR_UPDATE("TOUR_UPDATE", "更新旅游路线"),
    TOUR_DELETE("TOUR_DELETE", "删除旅游路线"),
    TOUR_CREATE("TOUR_CREATE", "新建旅游路线"),
    ;

    /**
     * 存入数据库的 code（建议用大写下划线或驼峰，保持一致性）
     */
    private final String code;

    /**
     * 默认描述（当 @Loggable.value() 未指定时使用）
     */
    private final String description;

    LogType(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
