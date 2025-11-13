package cn.cxdproject.coder.common.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Arrays;

class XssRequestWrapper extends HttpServletRequestWrapper {
    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        return cleanXSS(super.getParameter(name));
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        return Arrays.stream(values).map(this::cleanXSS).toArray(String[]::new);
    }

    private String cleanXSS(String value) {
        if (value == null) return null;
        return value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }
}
