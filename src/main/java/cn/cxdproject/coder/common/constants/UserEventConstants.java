package cn.cxdproject.coder.common.constants;

import java.time.LocalDateTime;

public interface UserEventConstants {

    /**
     * 用户登录事件
     */
    String USER_LOGIN_EVENT = "user.login.daily";

    /**
     * 用户注册事件
     */
    String USER_REGISTER_EVENT = "user.register";

    /**
     * 用户登录行为
     */
    String ACTION_TYPE_LOGIN = "login";

    /**
     * 用户登录前缀
     */
    String USER_LOGIN_PREFIX = "login_";

    /**
     * 用户注册行为
     */
    String ACTION_TYPE_REGISTER = "register";

    /**
     * 用户注册前缀
     */
    String USER_REGISTER_PREFIX = "register_";

    String EVENT_INTER_MEDIATE_USER = "user";

    static String USER_LOGIN_EVENT_DETAIL(String username, String ipAddress, String location, String device){
        return "用户: " +username + "在 " + LocalDateTime.now() + "于 " + ipAddress + "("+ location + ")" + "使用 " + device + "进行了登录操作";
    }

    static String USER_REGISTER_EVENT_DETAIL(String username, String ipAddress, String location, String device){
        return "用户: " +username + "在 " + LocalDateTime.now() + "于 " + ipAddress + "("+ location + ")" + "使用 " + device + "进行了注册操作";
    }

    /**
     * 提交文章前缀
     */
    String USER_PUT_ARTICLE_PREFIX = "create.article_";

    /**
     * 算法题提交前缀
     */
    String UER_SUBMIT_QUESTION_PREFIX = "algorithm.submit_";

    /**
     * 面试题提交前缀
     */
    String USER_SUBMIT_INTERVIEW_QUESTION_PREFIX = "interview.question.submit";

    /**
     * 每日刷面试题
     */
    String INTERVIEW_PRACTICE_EVENT = "practice.interview.daily";

    /**
     * 每日发帖
     */
    String POST_PUT_EVENT = "community.post.daily";

    /**
     * 每日算法题
     */
    String ALGORITHM_PRACTICE_EVENT = "practice.algorithm.daily";
}
