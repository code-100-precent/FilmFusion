package cn.cxdproject.coder.common.generate;

public class NameConverter {

    // 下划线转小驼峰，例如 created_at → createdAt
    public static String toCamelCase(String name) {
        StringBuilder sb = new StringBuilder();
        boolean upperNext = false;
        for (char c : name.toCharArray()) {
            if (c == '_') {
                upperNext = true;
            } else {
                sb.append(upperNext ? Character.toUpperCase(c) : c);
                upperNext = false;
            }
        }
        return sb.toString();
    }

    // 保持原样
    public static String toSnakeCase(String name) {
        return name;
    }

    // created_at → CreatedAt
    public static String toPascalCase(String name) {
        String camel = toCamelCase(name);
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }

    // saves → Save, user_logs → UserLog
    public static String toPascalCaseSingular(String name) {
        String pascal = toPascalCase(name);
        if (pascal.endsWith("s") && pascal.length() > 1) {
            return pascal.substring(0, pascal.length() - 1);
        }
        return pascal;
    }
}
