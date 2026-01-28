-- 数据库迁移脚本：为 fi_shoots 表添加 module_id 字段
-- 执行日期：2026-01-28
-- 目的：支持协拍服务按模块分类

USE film_fusion;

-- 1. 检查字段是否已存在（避免重复添加）
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'film_fusion'
      AND TABLE_NAME = 'fi_shoots'
      AND COLUMN_NAME = 'module_id'
);

-- 2. 如果字段不存在，则添加
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE fi_shoots ADD COLUMN module_id BIGINT DEFAULT NULL COMMENT ''模块ID，关联到模块表'' AFTER thumb_image',
    'SELECT ''Column module_id already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 添加索引（如果不存在）
SET @index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = 'film_fusion'
      AND TABLE_NAME = 'fi_shoots'
      AND INDEX_NAME = 'idx_module_id'
);

SET @sql = IF(@index_exists = 0,
    'ALTER TABLE fi_shoots ADD INDEX idx_module_id (module_id)',
    'SELECT ''Index idx_module_id already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 验证字段已添加
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'film_fusion'
  AND TABLE_NAME = 'fi_shoots'
  AND COLUMN_NAME = 'module_id';

-- 5. 显示当前表结构
DESC fi_shoots;

-- 6. 可选：为现有数据分配模块（示例）
-- 取消注释以下语句来为现有数据分配模块

/*
-- 假设你已经创建了一些模块
-- 示例：将服务随机分配到不同模块

-- 首先确保 fi_module 表有数据
SELECT * FROM fi_module WHERE deleted = 0;

-- 然后根据实际情况更新 shoots 表
-- 方式1：按服务名称关键词分配
UPDATE fi_shoots SET module_id = 1 WHERE name LIKE '%摄影%' AND deleted = 0;
UPDATE fi_shoots SET module_id = 2 WHERE name LIKE '%后期%' AND deleted = 0;
UPDATE fi_shoots SET module_id = 3 WHERE name LIKE '%设备%' AND deleted = 0;
UPDATE fi_shoots SET module_id = 4 WHERE name LIKE '%场地%' AND deleted = 0;

-- 方式2：按ID范围分配（用于测试）
UPDATE fi_shoots SET module_id = 1 WHERE id % 4 = 1 AND deleted = 0;
UPDATE fi_shoots SET module_id = 2 WHERE id % 4 = 2 AND deleted = 0;
UPDATE fi_shoots SET module_id = 3 WHERE id % 4 = 3 AND deleted = 0;
UPDATE fi_shoots SET module_id = 4 WHERE id % 4 = 0 AND deleted = 0;
*/

-- 7. 查看更新后的数据分布
SELECT 
    COALESCE(m.name, '未分类') AS module_name,
    COUNT(s.id) AS shoot_count
FROM fi_shoots s
LEFT JOIN fi_module m ON s.module_id = m.id AND m.deleted = 0
WHERE s.deleted = 0
GROUP BY s.module_id, m.name
ORDER BY shoot_count DESC;

SELECT '迁移脚本执行完成！' AS status;
