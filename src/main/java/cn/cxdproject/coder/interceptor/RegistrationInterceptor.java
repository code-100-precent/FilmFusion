package cn.cxdproject.coder.interceptor;

import cn.cxdproject.coder.common.context.AuthContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

@Component
public class RegistrationInterceptor implements HandlerInterceptor {

    private static final String REGISTER_SUCCESS_PATH = "/api/admin/register/email";
    // 2. 配置注册成功的响应状态码（通常是 200 OK 或 201 Created）
    private static final int REGISTER_SUCCESS_STATUS = 200;
    // 3. （可选）若响应是JSON，可配置成功标识（如 {"code":200,"msg":"success"} 中的 code=200）
    private static final String SUCCESS_CODE_MARKER = "\"code\":200";

    /**
     * 接口执行后、响应返回前触发（可获取接口执行结果）
     */
    @Override
    public void postHandle(@NotNull HttpServletRequest request,
                           @NotNull HttpServletResponse response,
                           @NotNull Object handler,
                           org.springframework.web.servlet.ModelAndView modelAndView) throws Exception {
        // 第一步：仅拦截注册接口的请求（排除其他所有请求）
        String requestUri = request.getRequestURI();
        // 匹配注册接口路径
        if (!REGISTER_SUCCESS_PATH.equals(requestUri)) {
            return; // 不是注册接口，直接返回，不触发记录
        }

        // 第二步：判断注册是否成功（通过响应状态码 + 响应内容双重验证）
        // 2.1 先判断响应状态码（快速过滤失败场景）
        if (response.getStatus() != REGISTER_SUCCESS_STATUS) {
            return; // 状态码不是成功，不记录
        }

        // 2.2 再判断响应内容（避免状态码正确但实际业务失败的情况，如 {"code":500,"msg":"用户名已存在"}）
        // 注意：需用 ContentCachingResponseWrapper 包装响应，才能读取响应内容（否则响应流只能读一次）
        if (!(response instanceof ContentCachingResponseWrapper)) {
            return; // 未包装响应，无法读取内容，安全起见不记录
        }
        ContentCachingResponseWrapper responseWrapper = (ContentCachingResponseWrapper) response;
        String responseBody = new String(responseWrapper.getContentAsByteArray(), response.getCharacterEncoding());
        // 验证响应内容是否包含成功标识（根据你的实际响应格式修改，如判断 msg 是否为 "注册成功"）
        if (!responseBody.contains(SUCCESS_CODE_MARKER)) {
            return; // 业务失败，不记录
        }

        // 第三步：注册成功，获取管理员ID并记录
        // 从 AuthContext 获取（若注册成功后会自动将管理员信息存入 AuthContext）
//        Admin registeredAdmin = AuthContext.getCurrentAdmin();
//        if (registeredAdmin != null && registeredAdmin.getId() != null) {
//            String adminId = registeredAdmin.getId().toString();
//        }

        // 关键：将包装后的响应内容写回，避免前端无法获取响应（因为 ContentCachingResponseWrapper 读过后需重置）
        responseWrapper.copyBodyToResponse();
    }

    // 废弃原 preHandle 逻辑（仅保留静态资源过滤，或直接删除，因为已在 postHandle 精准拦截）
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request,
                             @NotNull HttpServletResponse response,
                             Object handler) throws Exception {
        // （可选）若需保留静态资源过滤，可保留此逻辑；否则直接 return true
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith("/static/") ||
                requestUri.contains(".css") ||
                requestUri.contains(".js") ||
                requestUri.contains(".png")) {
            return true;
        }
        return true; // 所有请求都放行，拦截逻辑在 postHandle 中处理
    }
}
