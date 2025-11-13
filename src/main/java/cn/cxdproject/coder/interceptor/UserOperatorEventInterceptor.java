package cn.cxdproject.coder.interceptor;

import cn.cxdproject.coder.common.context.AuthContext;
import com.hibiscus.signal.core.SignalContext;
import com.hibiscus.signal.core.SignalInterceptor;
import com.hibiscus.signal.spring.anno.SignalInterceptorBind;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static cn.cxdproject.coder.common.constants.CommonEventConstants.EVENT_INTER_MEDIATE_REQUEST;
import static cn.cxdproject.coder.common.constants.UserConstants.DEFAULT_USERID;
import static cn.cxdproject.coder.common.constants.UserConstants.DEFAULT_USERNAME;
import static cn.cxdproject.coder.common.constants.UserEventConstants.*;


@Component
@SignalInterceptorBind(value = {USER_LOGIN_EVENT, USER_REGISTER_EVENT})
public class UserOperatorEventInterceptor implements SignalInterceptor {

    @Override
    public boolean beforeHandle(String event, Object sender, Object... params) {
        SignalContext context = (SignalContext) params[0];
        Map<String, Object> values = context.getIntermediateValues();
        HttpServletRequest request = (HttpServletRequest) values.get(EVENT_INTER_MEDIATE_REQUEST);
//        Admin admin = AuthContext.getCurrentAdmin();
//        if (admin == null) {
//            admin = (Admin) values.getOrDefault(EVENT_INTER_MEDIATE_USER, getAnonymousAdmin());
//        }
        // 添加请求为空的检查
        if (request == null) {
            return SignalInterceptor.super.beforeHandle(event, sender, params);
        }
        return SignalInterceptor.super.beforeHandle(event, sender, params);
    }

    @Override
    public void afterHandle(String event, Object sender, Object[] params, Throwable error) {
        SignalInterceptor.super.afterHandle(event, sender, params, error);
    }

    @Override
    public int getOrder() {
        return SignalInterceptor.super.getOrder();
    }
}
