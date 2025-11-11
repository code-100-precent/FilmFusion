package cn.cxdproject.coder.common.context;


import cn.cxdproject.coder.model.entity.Admin;

/**
 * Thread-local auth context for admin and token only in template.
 */
public class AuthContext {

    private static final ThreadLocal<Admin> currentAdmin = new ThreadLocal<>();
    private static final ThreadLocal<String> currentToken = new ThreadLocal<>();

    public static void setCurrentAdmin(Admin admin) {
        currentAdmin.set(admin);
    }

    public static Admin getCurrentAdmin() {
        return currentAdmin.get();
    }

    @Deprecated
    public static Admin getCurrentUser() {
        return getCurrentAdmin();
    }

    @Deprecated
    public static void setCurrentUser(Admin admin) {
        setCurrentAdmin(admin);
    }

    public static void setCurrentToken(String token) {
        currentToken.set(token);
    }

    public static String getCurrentToken() {
        return currentToken.get();
    }

    public static void clear() {
        currentAdmin.remove();
        currentToken.remove();
    }
}
