package cn.cxdproject.coder.common.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Request context tool class, which is used to save and obtain data related to the current request within the thread.
 * Based on ThreadLocal implementation, it is suitable for interceptors, log tracing, authentication context, etc.
 *
 * @author heathcetide
 */
public class RequestContext {

    /**
     * Each thread maintains an independent context Map.
     */
    private static final ThreadLocal<Map<String, Object>> context = ThreadLocal.withInitial(HashMap::new);

    /**
     * Key-value pairs placed in the context
     */
    public static void put(String key, Object value) {
        context.get().put(key, value);
    }

    /**
     * Get the value (unsafe, need to convert the type yourself)
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) context.get().get(key);
    }

    /**
     * Type gets the value safely (with default value)
     */
    public static <T> T get(String key, Class<T> type, T defaultValue) {
        Object value = context.get().get(key);
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return defaultValue;
    }

    /**
     * Remove a key value
     */
    public static void remove(String key) {
        context.get().remove(key);
    }

    /**
     * Get all context contents of the current thread (read-only)
     */
    public static Map<String, Object> getAll() {
        return Collections.unmodifiableMap(context.get());
    }

    /**
     * Clear the current thread context (it must be adjusted at the end of the request to avoid memory leakage)
     */
    public static void clear() {
        context.remove();
    }
}
