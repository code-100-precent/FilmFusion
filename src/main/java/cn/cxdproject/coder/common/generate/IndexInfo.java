package cn.cxdproject.coder.common.generate;

/**
 * 索引信息模型类
 *
 * 表示数据库中某个字段对应的索引元信息。
 * 通常由 JDBC 的 DatabaseMetaData#getIndexInfo() 获取。
 * 用于代码生成时判断字段是否具有索引、是否唯一。
 */
public class IndexInfo {

    /** 索引名称（INDEX_NAME），可用于生成注解或记录索引元信息 */
    private String indexName;

    /** 是否为唯一索引（NON_UNIQUE=false） */
    private boolean unique;

    /**
     * 获取索引名称
     *
     * @return 索引名（如：idx_user_email）
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * 设置索引名称
     *
     * @param indexName 索引名
     */
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    /**
     * 是否为唯一索引
     *
     * @return true 表示该索引是唯一的（UNIQUE）
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * 设置是否唯一
     *
     * @param unique 是否是唯一索引（true 表示唯一）
     */
    public void setUnique(boolean unique) {
        this.unique = unique;
    }
}
