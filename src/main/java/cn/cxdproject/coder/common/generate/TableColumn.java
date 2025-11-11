package cn.cxdproject.coder.common.generate;

import lombok.Data;

import java.util.Objects;

/**
 * 表字段信息模型类
 * 表示数据库表中的一个字段及其属性，用于代码生成时作为字段元数据传递。
 */
@Data
public class TableColumn {

    /** 字段名称（代码生成使用的变量名，通常是驼峰命名） */
    private String name;

    /** 字段的 Java 类型（如：String、Integer 等） */
    private String type;

    /** 字段注释（从数据库字段注释获取，用于生成 JavaDoc 或 Swagger 注释） */
    private String comment;

    /** 是否允许为 null，对应数据库字段的 NULLABLE 属性 */
    private boolean nullable;

    /** 是否为主键，对应数据库的 PRIMARY KEY */
    private boolean primaryKey;

    /** 字段的长度（如 VARCHAR(255) 中的 255） */
    private int length;

    /** 精度（主要用于数值类型，如 DECIMAL(10,2) 中的 10） */
    private int precision;

    /** 小数位数（主要用于数值类型，如 DECIMAL(10,2) 中的 2） */
    private int scale;

    /** 是否被索引（如有普通索引或唯一索引均为 true） */
    private boolean indexed;

    /** 是否是唯一索引（唯一约束 UNIQUE） */
    private boolean unique;

    /** 索引名称（字段所属的索引名称） */
    private String indexName;

    /** Java 全限定类型（如 java.time.LocalDateTime），用于导包判断 */
    private String fullType;

    /** 用于生成代码的字段名（支持保留下划线或转为驼峰，可配置） */
    private String fieldName;

    /** 数据库中字段的原始名称（COLUMN_NAME，保持与数据库一致） */
    private String originalName;

    public TableColumn() {
    }

    public TableColumn(String name, String type, String comment) {
        this.name = name;
        this.type = type;
        this.comment = comment;
    }

    public TableColumn(String name, String type, String comment,
                       boolean nullable, boolean primaryKey,
                       int length, int precision, int scale,
                       boolean indexed, boolean unique, String indexName) {
        this.name = name;
        this.type = type;
        this.comment = comment;
        this.nullable = nullable;
        this.primaryKey = primaryKey;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
        this.indexed = indexed;
        this.unique = unique;
        this.indexName = indexName;
    }

    @Override
    public String toString() {
        return "TableColumn{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", comment='" + comment + '\'' +
                ", nullable=" + nullable +
                ", primaryKey=" + primaryKey +
                ", length=" + length +
                ", precision=" + precision +
                ", scale=" + scale +
                ", indexed=" + indexed +
                ", unique=" + unique +
                ", indexName='" + indexName + '\'' +
                ", fullType='" + fullType + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", originalName='" + originalName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TableColumn)) return false;
        TableColumn that = (TableColumn) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
