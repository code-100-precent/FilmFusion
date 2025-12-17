package cn.cxdproject.coder.common.constants;

/**
 * 通用常量信息
 *
 * @author heathcetide
 */
public interface Constants {
    /**
     * UTF-8 字符集
     */
    String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    String GBK = "GBK";

    /**
     * http请求
     */
    String HTTP = "http://";

    /**
     * https请求
     */
    String HTTPS = "https://";

    /**
     * 资源映射路径 前缀
     */
    String RESOURCE_PREFIX = "/img";

    /**
     * 栏目基础祖先
     */
    String BASE_COLUMN_ID = "0";

    /**
     * 通用成功标识
     */
    String SUCCESS = "0";

    /**
     * 通用失败标识
     */
    String FAIL = "1";

    /**
     * 通用未删除标识
     */
    String NOT_DELETED = "0";

    /**
     * 通用删除标识
     */
    String DELETED = "2";

    /**
     * 登录成功
     */
    String LOGIN_SUCCESS = "Success";

    /**
     * 注销
     */
    String LOGOUT = "Logout";

    /**
     * 注册
     */
    String REGISTER = "Register";

    /**
     * 重新注册
     */
    String REGISTER_AGAIN = "RegisterAgain";

    /**
     * 登录失败
     */
    String LOGIN_FAIL = "Error";

    /**
     * 用户未登录
     */
    String NOT_LOGGED_IN = "NotLogIn";

    /**
     * 已经点过赞
     */
    String ALREADY_LIKED = "AlreadyLiked";

    /**
     * 未点赞
     */
    String UNLIKED = "UnLiked";

    /**
     * 管理员角色权限标识
     */
    String SUPER_ADMIN = "admin";

    /**
     * 角色权限分隔符
     */
    String ROLE_DELIMETER = ",";

    /**
     * 令牌
     */
    String TOKEN = "token";

    /**
     * 登录提示信息
     */
    String LOGIN_MSG = "login_msg";

    /**
     * 登录提示信息
     */
    String LOGIN_EXPIRED_MSG = "login_expired_msg";

    /**
     * 令牌前缀
     */
    String TOKEN_PREFIX = "Bearer ";

    /**
     * 令牌前缀
     */
    String LOGIN_USER_KEY = "login_user_key";

    /**
     * 令牌UUID前缀
     */
    String LOGIN_USER_UUID_KEY = "login_user_code";

    /**
     * 自动识别json对象白名单配置（仅允许解析的包名，范围越小越安全）
     */
    String[] JSON_WHITELIST_STR = {"org.springframework", "com.bonss"};

    /**
     * 未注册标识
     */
    String UNREGISTER_CODE = "unregisterCode";

    /**
     * 已回复标识
     */
    String IS_REPLY_STATUS = "1";

    /**
     * 未回复标识
     */
    String NOT_REPLY_STATUS = "0";


    /**
     * 显示状态 1 表示隐藏
     */
    String STATE_HIDE = "1";

    /**
     * 消息通知未读
     */
    String NO_READ_MESSAGE = "0";

    /**
     * 消息通知已读
     */
    String IS_READ_MESSAGE = "1";


    /**
     * 本地存储类型
     */
    String STORAGE_TYPE_LOCAL = "local";

    Long TIME = 2L;
}
