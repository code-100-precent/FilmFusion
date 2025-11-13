package cn.cxdproject.coder.common.filter;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class TraceIdFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws ServletException, IOException {
        MDC.put("traceId", UUID.randomUUID().toString());
        ((HttpServletResponse) response).addHeader("X-Trace-Id", MDC.get("traceId"));
        chain.doFilter(request, response);
        MDC.remove("traceId");
    }
} 