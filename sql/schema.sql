CREATE DATABASE if not exists campus_guide_db;

USE campus_guide_db;

# admin 管理员表
CREATE TABLE `admin`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `username`   VARCHAR(255) NOT NULL COMMENT '用户名',
    `password`   VARCHAR(255) NOT NULL COMMENT '密码',
    `email`      VARCHAR(255) DEFAULT NULL COMMENT '邮箱',
    `avatar`     VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `role`       VARCHAR(255) DEFAULT NULL COMMENT '角色',
    `enabled`    BOOLEAN      DEFAULT TRUE COMMENT '是否启用',
    `deleted`    BOOLEAN      DEFAULT FALSE COMMENT '是否删除',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='管理员表';

# Operation Log 操作日志表
CREATE TABLE `co_operation_log`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `type`        VARCHAR(100) NOT NULL COMMENT '操作类型（如：LOGIN、DOC_EDIT）',
    `description` TEXT COMMENT '操作描述',
    `user_id`     BIGINT COMMENT '执行用户ID',
    `operator`    VARCHAR(100) NOT NULL COMMENT '操作人标识（用户名或IP）',
    `success`     BOOLEAN      NOT NULL COMMENT '是否成功',
    `params`      TEXT COMMENT '输入参数快照',
    `result`      TEXT COMMENT '输出结果快照',
    `timestamp`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否逻辑删除',
    `created_at`  DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='操作日志表';

# co_feedback 反馈表
CREATE TABLE `co_feedback`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL COMMENT '主键ID',
    `user_id`    BIGINT                            NOT NULL COMMENT '用户ID，关联到用户表',
    `content`    TEXT                              NOT NULL COMMENT '反馈内容',
    `status`     VARCHAR(32)                       NOT NULL DEFAULT '未处理' COMMENT '反馈状态（未处理、处理中、已解决等）',
    `type`       VARCHAR(32) COMMENT '反馈类型（建议、投诉等）',
    `deleted`    TINYINT                           NOT NULL DEFAULT 0 COMMENT '逻辑删除（0：正常，1：删除）',
    `created_at` DATETIME                                   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME                                   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户反馈表';

-- ----------------------------
-- 1. 游客表（visitor）
-- ----------------------------
DROP TABLE IF EXISTS `visitor`;
CREATE TABLE `visitor` (
                           `visitor_id` VARCHAR(64) NOT NULL COMMENT '游客唯一ID（主键）',
                           `name` VARCHAR(32) NOT NULL COMMENT '游客姓名',
                           `phone` VARCHAR(11) NOT NULL COMMENT '手机号（唯一）',
                           `password` VARCHAR(64) NOT NULL COMMENT '加密后的密码（MD5/BCrypt）',
                           `identity` VARCHAR(16) NOT NULL COMMENT '身份类型：新生/家长/其他',
                           `register_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                           `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
                           PRIMARY KEY (`visitor_id`),
                           UNIQUE KEY `uk_phone` (`phone`),
                           KEY `idx_identity` (`identity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游客信息表';

-- ----------------------------
-- 2. 校友表（alumni）
-- ----------------------------
DROP TABLE IF EXISTS `alumni`;
CREATE TABLE `alumni` (
                          `alumni_id` VARCHAR(64) NOT NULL COMMENT '校友唯一ID（主键）',
                          `name` VARCHAR(32) NOT NULL COMMENT '校友姓名',
                          `grad_year` VARCHAR(8) NOT NULL COMMENT '毕业届数（如：2010届）',
                          `major` VARCHAR(64) NOT NULL COMMENT '所学专业',
                          `career` VARCHAR(64) DEFAULT NULL COMMENT '现职业',
                          `contact` VARCHAR(32) DEFAULT NULL COMMENT '联系方式（手机号/微信）',
                          `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像图片URL',
                          PRIMARY KEY (`alumni_id`),
                          KEY `idx_grad_year` (`grad_year`),
                          KEY `idx_major` (`major`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校友信息表';

-- ----------------------------
-- 3. POI表（兴趣点，point_of_interest）
-- ----------------------------
DROP TABLE IF EXISTS `point_of_interest`;
CREATE TABLE `point_of_interest` (
                                     `poi_id` VARCHAR(64) NOT NULL COMMENT 'POI唯一ID（主键）',
                                     `name` VARCHAR(64) NOT NULL COMMENT 'POI名称（如：图书馆）',
                                     `x` DECIMAL(10,6) NOT NULL COMMENT '坐标X（经度）',
                                     `y` DECIMAL(10,6) NOT NULL COMMENT '坐标Y（纬度）',
                                     `type` VARCHAR(16) NOT NULL COMMENT 'POI类型：建筑/景观/场馆',
                                     `intro` TEXT DEFAULT NULL COMMENT 'POI简介',
                                     `image_url` VARCHAR(255) DEFAULT NULL COMMENT 'POI图片URL',
                                     `open_time` VARCHAR(64) DEFAULT NULL COMMENT '开放时间（如：08:00-22:00）',
                                     PRIMARY KEY (`poi_id`),
                                     KEY `idx_type` (`type`),
                                     KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园兴趣点（POI）表';

-- ----------------------------
-- 4. 路线表（route）
-- ----------------------------
DROP TABLE IF EXISTS `route`;
CREATE TABLE `route` (
                         `route_id` VARCHAR(64) NOT NULL COMMENT '路线唯一ID（主键）',
                         `route_name` VARCHAR(128) NOT NULL COMMENT '路线名称（如：新生校园初体验路线）',
                         `route_desc` TEXT DEFAULT NULL COMMENT '路线描述',
                         `duration` INT NOT NULL COMMENT '预计时长（分钟）',
                         `suitable_identity` VARCHAR(16) NOT NULL COMMENT '适合身份：新生/家长/其他',
                         `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         PRIMARY KEY (`route_id`),
                         KEY `idx_suitable_identity` (`suitable_identity`),
                         KEY `idx_duration` (`duration`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园导览路线表';

-- ----------------------------
-- 5. 校友故事表（alumni_story）
-- ----------------------------
DROP TABLE IF EXISTS `alumni_story`;
CREATE TABLE `alumni_story` (
                                `story_id` VARCHAR(64) NOT NULL COMMENT '故事唯一ID（主键）',
                                `title` VARCHAR(128) NOT NULL COMMENT '故事标题',
                                `content` LONGTEXT NOT NULL COMMENT '故事内容',
                                `publish_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
                                `status` VARCHAR(16) NOT NULL DEFAULT '待审核' COMMENT '状态：待审核/已发布/已驳回',
                                `image_url` VARCHAR(255) DEFAULT NULL COMMENT '故事配图URL',
                                `alumni_id` VARCHAR(64) NOT NULL COMMENT '关联校友ID（业务关联）',
                                PRIMARY KEY (`story_id`),
                                KEY `idx_status` (`status`),
                                KEY `idx_publish_time` (`publish_time`),
                                KEY `idx_alumni_id` (`alumni_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校友故事表';

-- ----------------------------
-- 6. 情感标签表（emotion_tag）
-- ----------------------------
DROP TABLE IF EXISTS `emotion_tag`;
CREATE TABLE `emotion_tag` (
                               `tag_id` VARCHAR(64) NOT NULL COMMENT '标签唯一ID（主键）',
                               `tag_name` VARCHAR(32) NOT NULL COMMENT '标签名称（如：励志/青春）',
                               `tag_desc` VARCHAR(255) DEFAULT NULL COMMENT '标签描述',
                               PRIMARY KEY (`tag_id`),
                               UNIQUE KEY `uk_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='情感标签表';

-- ----------------------------
-- 7. 路线-POI关联表（route_poi_relation）（多对多）
-- ----------------------------
DROP TABLE IF EXISTS `route_poi_relation`;
CREATE TABLE `route_poi_relation` (
                                      `id` VARCHAR(64) NOT NULL COMMENT '关联记录唯一ID（主键）',
                                      `route_id` VARCHAR(64) NOT NULL COMMENT '关联路线ID（业务关联）',
                                      `poi_id` VARCHAR(64) NOT NULL COMMENT '关联POIID（业务关联）',
                                      `sort_order` INT NOT NULL COMMENT 'POI在路线中的顺序（1,2,3...）',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_route_poi` (`route_id`, `poi_id`) COMMENT '避免路线与POI重复关联',
                                      KEY `idx_route_id` (`route_id`),
                                      KEY `idx_poi_id` (`poi_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线与POI的多对多关联表';

-- ----------------------------
-- 8. POI-故事关联表（poi_story_relation）（多对多）
-- ----------------------------，
DROP TABLE IF EXISTS `poi_story_relation`;
CREATE TABLE `poi_story_relation` (
                                      `id` VARCHAR(64) NOT NULL COMMENT '关联记录唯一ID（主键）',
                                      `poi_id` VARCHAR(64) NOT NULL COMMENT '关联POIID（业务关联）',
                                      `story_id` VARCHAR(64) NOT NULL COMMENT '关联故事ID（业务关联）',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_poi_story` (`poi_id`, `story_id`) COMMENT '避免POI与故事重复关联',
                                      KEY `idx_poi_story_poi` (`poi_id`),
                                      KEY `idx_poi_story_story` (`story_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='POI与校友故事的多对多关联表';

-- ----------------------------
-- 9. 故事-标签关联表（story_tag_relation）（多对多）
-- ----------------------------
DROP TABLE IF EXISTS `story_tag_relation`;
CREATE TABLE `story_tag_relation` (
                                      `id` VARCHAR(64) NOT NULL COMMENT '关联记录唯一ID（主键）',
                                      `story_id` VARCHAR(64) NOT NULL COMMENT '关联故事ID（业务关联）',
                                      `tag_id` VARCHAR(64) NOT NULL COMMENT '关联标签ID（业务关联）',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_story_tag` (`story_id`, `tag_id`) COMMENT '避免故事与标签重复关联',
                                      KEY `idx_story_tag_story` (`story_id`),
                                      KEY `idx_story_tag_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校友故事与情感标签的多对多关联表';

-- ----------------------------
-- 10. 游客收藏表（visitor_collection）（多对多）
-- ----------------------------
DROP TABLE IF EXISTS `visitor_collection`;
CREATE TABLE `visitor_collection` (
                                      `id` VARCHAR(64) NOT NULL COMMENT '收藏记录唯一ID（主键）',
                                      `visitor_id` VARCHAR(64) NOT NULL COMMENT '关联游客ID（业务关联）',
                                      `poi_id` VARCHAR(64) NOT NULL COMMENT '关联POIID（业务关联）',
                                      `collect_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_visitor_poi` (`visitor_id`, `poi_id`) COMMENT '避免游客重复收藏同一POI',
                                      KEY `idx_collection_visitor` (`visitor_id`),
                                      KEY `idx_collection_poi` (`poi_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游客收藏POI表';

-- ----------------------------
-- 11. 游客浏览记录表（visitor_browse_record）
-- ----------------------------
DROP TABLE IF EXISTS `visitor_browse_record`;
CREATE TABLE `visitor_browse_record` (
                                         `record_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录唯一ID（主键）',
                                         `visitor_id` VARCHAR(64) NOT NULL COMMENT '关联游客ID（业务关联）',
                                         `poi_id` VARCHAR(64) NOT NULL COMMENT '关联POIID（业务关联）',
                                         `browse_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
                                         PRIMARY KEY (`record_id`),
                                         KEY `idx_visitor_poi` (`visitor_id`, `poi_id`),
                                         KEY `idx_browse_time` (`browse_time`),
                                         KEY `idx_browse_visitor` (`visitor_id`),
                                         KEY `idx_browse_poi` (`poi_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游客浏览POI记录表';