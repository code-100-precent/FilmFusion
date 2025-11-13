package cn.cxdproject.coder.common.generate;

import java.sql.Types;

/**
 * Java 类型名称常量定义，避免魔法值
 */
public interface SqlTypeConstants {

    String TYPE_STRING = "String";
    String TYPE_INTEGER = "Integer";
    String TYPE_LONG = "Long";
    String TYPE_SHORT = "Short";
    String TYPE_FLOAT = "Float";
    String TYPE_DOUBLE = "Double";
    String TYPE_BYTE = "Byte";
    String TYPE_BOOLEAN = "Boolean";

    String TYPE_BIGDECIMAL = "java.math.BigDecimal";

    String TYPE_LOCALDATE = "java.time.LocalDate";
    String TYPE_LOCALTIME = "java.time.LocalTime";
    String TYPE_LOCALDATETIME = "java.time.LocalDateTime";
    String TYPE_OFFSETTIME = "java.time.OffsetTime";
    String TYPE_OFFSETDATETIME = "java.time.OffsetDateTime";

    String TYPE_BYTES = "byte[]";

    String TYPE_BLOB = "java.sql.Blob";
    String TYPE_CLOB = "java.sql.Clob";
    String TYPE_NCLOB = "java.sql.NClob";

    String TYPE_STRUCT = "java.sql.Struct";
    String TYPE_ARRAY = "java.sql.Array";
    String TYPE_REF = "java.sql.Ref";
    String TYPE_ROWID = "java.sql.RowId";
    String TYPE_SQLXML = "java.sql.SQLXML";
    String TYPE_URL = "java.net.URL";
    String TYPE_OBJECT = "Object";

    /**
     * 映射 java.sql.Types 到 Java 类型
     */
    static String mapSqlType(int type) {
        return switch (type) {
            // 字符串类型
            case Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR, Types.NCHAR, Types.NVARCHAR, Types.LONGNVARCHAR ->
                    SqlTypeConstants.TYPE_STRING;

            // 数值类型
            case Types.INTEGER -> SqlTypeConstants.TYPE_INTEGER;
            case Types.TINYINT -> SqlTypeConstants.TYPE_BYTE;
            case Types.SMALLINT -> SqlTypeConstants.TYPE_SHORT;
            case Types.BIGINT -> SqlTypeConstants.TYPE_LONG;
            case Types.FLOAT, Types.REAL -> SqlTypeConstants.TYPE_FLOAT;
            case Types.DOUBLE -> SqlTypeConstants.TYPE_DOUBLE;
            case Types.NUMERIC, Types.DECIMAL -> SqlTypeConstants.TYPE_BIGDECIMAL;

            // 布尔类型
            case Types.BIT, Types.BOOLEAN -> SqlTypeConstants.TYPE_BOOLEAN;

            // 日期时间类型
            case Types.DATE -> SqlTypeConstants.TYPE_LOCALDATE;
            case Types.TIME -> SqlTypeConstants.TYPE_LOCALTIME;
            case Types.TIME_WITH_TIMEZONE -> SqlTypeConstants.TYPE_OFFSETTIME;
            case Types.TIMESTAMP -> SqlTypeConstants.TYPE_LOCALDATETIME;
            case Types.TIMESTAMP_WITH_TIMEZONE -> SqlTypeConstants.TYPE_OFFSETDATETIME;

            // 二进制类型
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> SqlTypeConstants.TYPE_BYTES;

            // LOB 类型
            case Types.BLOB -> SqlTypeConstants.TYPE_BLOB;
            case Types.CLOB -> SqlTypeConstants.TYPE_CLOB;
            case Types.NCLOB -> SqlTypeConstants.TYPE_NCLOB;

            // 结构化类型
            case Types.STRUCT -> SqlTypeConstants.TYPE_STRUCT;
            case Types.ARRAY -> SqlTypeConstants.TYPE_ARRAY;
            case Types.REF -> SqlTypeConstants.TYPE_REF;
            case Types.ROWID -> SqlTypeConstants.TYPE_ROWID;
            case Types.SQLXML -> SqlTypeConstants.TYPE_SQLXML;

            // 其他类型
            case Types.JAVA_OBJECT -> SqlTypeConstants.TYPE_OBJECT;
            case Types.DATALINK -> SqlTypeConstants.TYPE_URL;
            default -> SqlTypeConstants.TYPE_OBJECT;
        };
    }
}