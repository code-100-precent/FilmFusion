CREATE DATABASE if not exists film_fusion;


USE film_fusion;

# user 用户表
CREATE TABLE `fi_users`
(
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `username`       VARCHAR(255)  NOT NULL COMMENT '用户名',
    `password`       VARCHAR(255)  NOT NULL COMMENT '密码',
    `phoneNumber`    VARCHAR(15)   DEFAULT NULL COMMENT '电话号码',
    `avatar`         VARCHAR(255)  DEFAULT NULL COMMENT '头像',
    `role`           VARCHAR(255)  DEFAULT NULL COMMENT '角色',
    `enabled`        BOOLEAN       DEFAULT TRUE COMMENT '是否启用',
    `deleted`        TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0：正常，1：删除）',
    `created_at`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `thumb_avatar`   varchar(2550) DEFAULT NULL COMMENT '压缩后头像',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

# Operation Log 操作日志表
CREATE TABLE `fi_operation_logs`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `type`        VARCHAR(100) NOT NULL COMMENT '操作类型（如：LOGIN、DOC_EDIT）',
    `description` TEXT COMMENT '操作描述',
    `user_id`     BIGINT COMMENT '执行用户ID',
    `user_name`   varchar(50)  NOT NULL COMMENT '执行用户名',
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
CREATE TABLE `fi_feedbacks`
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

# report 影视剧备案表
CREATE TABLE `fi_reports`
(
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`          VARCHAR(50)   NOT NULL COMMENT '影视剧名称',
    `type`          VARCHAR(50)   NOT NULL COMMENT '类型',
    `genre`         VARCHAR(50)   NOT NULL COMMENT '题材',
    `episodes`      INT           NOT NULL COMMENT '集数',
    `invest_amount` DECIMAL(15,2) NOT NULL COMMENT '投资金额（万元）',
    `main_creators` VARCHAR(255)  NOT NULL COMMENT '主创人员',
    `lead_producer` VARCHAR(100)  NOT NULL COMMENT '第一出品单位',
    `producer_unit` VARCHAR(100)  NOT NULL COMMENT '制片单位',
    `start_date`    DATE          NOT NULL COMMENT '拍摄开始日期',
    `end_date`      DATE          NOT NULL COMMENT '拍摄结束日期',
    `crew_scale`    VARCHAR(50)   NOT NULL COMMENT '剧组规模',
    `contact`       VARCHAR(50)   NOT NULL COMMENT '联系人',
    `phone_number`  VARCHAR(20)   NOT NULL COMMENT '联系电话',
    `crew_position` VARCHAR(50)   NOT NULL COMMENT '剧组职务',
    `user_id`       BIGINT        NOT NULL COMMENT '用户ID，关联到用户表',
    `deleted`       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0：正常，1：删除）',
    `created_at`    DATETIME               DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `status`        varchar(10)   NOT NULL COMMENT '申报状态(未处理，处理中，申请成功，申请失败)',
    `shoot_permit`  varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '拍摄许可',
    `thumb_shoot_permit`  varchar(500)    DEFAULT NULL COMMENT '拍摄许可压缩图片',
    `approval_file`       varchar(500)    DEFAULT NULL COMMENT '立项审批',
    `thumb_approval_file` varchar(500)    DEFAULT NULL COMMENT '立项审批压缩图片',
    `shoot_apply`         varchar(500)    DEFAULT NULL COMMENT '协拍服务许可',
    `thumb_shoot_apply`   varchar(500)    DEFAULT NULL COMMENT '协拍服务许可压缩图片',
    PRIMARY KEY (`id`),
    KEY `name_type_deleted` (`name`,`type`,`deleted`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='影视剧备案信息表';

# locations 拍摄场地表
CREATE TABLE `fi_locations`
(
    `id`                   BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`                 VARCHAR(50)   NOT NULL COMMENT '场地名称',
    `type`                 VARCHAR(20)   NOT NULL COMMENT '类型',
    `status`               TINYINT       NOT NULL DEFAULT 1 COMMENT '可用状态（0：不可用，1：可用）',
    `location_description` VARCHAR(500)  NOT NULL COMMENT '场地介绍',
    `location_principal_phone`        VARCHAR(20)   NOT NULL COMMENT '场地联系人电话',
    `location_principal_name`         VARCHAR(50)   NOT NULL COMMENT '场地联系人',
    `gov_principal_phone`        VARCHAR(20)   NOT NULL COMMENT '政府联系人电话',
    `gov_principal_name`         VARCHAR(50)   NOT NULL COMMENT '政府联系人',
    `address`              VARCHAR(255)  NOT NULL COMMENT '地址',
    `price`                DECIMAL(10,2) NOT NULL COMMENT '价格（元）',
    `user_id`              BIGINT        NOT NULL COMMENT '用户ID，关联到用户表',
    `deleted`              TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0：正常，1：删除）',
    `created_at`           DATETIME               DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`           DATETIME               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `image`                varchar(500)          DEFAULT NULL COMMENT '图片',
    `thumb_image`          varchar(500)          DEFAULT NULL COMMENT '压缩后图片',
    `longitude`       varchar(40)       NOT NULL COMMENT '经度',
    `latitude`        varchar(40)       NOT NULL COMMENT '纬度',
    `drama_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '拍摄地相关的影视',
    PRIMARY KEY (`id`),
    KEY `name_type_status_deleted` (`name`,`type`,`status`,`deleted`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='拍摄场地表';

# article 资讯文章表
CREATE TABLE `fi_articles`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`         VARCHAR(100) NOT NULL COMMENT '文章标题',
    `issue_unit`    VARCHAR(100) NOT NULL COMMENT '发布单位',
    `issue_time`    DATETIME     NOT NULL COMMENT '发布时间',
    `content`       TEXT         NOT NULL COMMENT '内容',
    `user_id`       BIGINT       NOT NULL COMMENT '用户ID，关联到用户表',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除（0：正常，1：删除）',
    `created_at`    DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `image`         varchar(500) DEFAULT NULL COMMENT '图片',
    `thumb_image`   varchar(500) DEFAULT NULL COMMENT '压缩后图片',
    PRIMARY KEY (`id`),
    KEY `id` (`id`,`title`,`issue_time`,`deleted`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='资讯文章表';

# shoot 协拍服务表
CREATE TABLE `fi_shoots`
(
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`         VARCHAR(100)  NOT NULL COMMENT '服务名称',
    `description`  VARCHAR(500)  NOT NULL COMMENT '服务简介',
    `price`        DECIMAL(10,2) NOT NULL COMMENT '价格（元）',
    `status`       TINYINT       NOT NULL DEFAULT 1 COMMENT '状态（0：下线，1：上线）',
    `address`      VARCHAR(255)  NOT NULL COMMENT '服务地址',
    `phone`        VARCHAR(20)   NOT NULL COMMENT '联系电话',
    `contact_name` VARCHAR(50)   NOT NULL COMMENT '联系人',
    `user_id`      BIGINT        NOT NULL COMMENT '用户ID，关联到用户表',
    `deleted`      TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0：正常，1：删除）',
    `created_at`   DATETIME               DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `image`        varchar(500) DEFAULT NULL COMMENT '图片',
    `thumb_image`  varchar(500) DEFAULT NULL COMMENT '压缩后图片',
    PRIMARY KEY (`id`),
    KEY `name_status_deleted` (`name`,`status`,`deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='协拍服务表';

# drama 电视剧备案表
CREATE TABLE `fi_dramas`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`              VARCHAR(100) NOT NULL COMMENT '电视剧名称',
    `filing_num`        VARCHAR(50)  NOT NULL COMMENT '备案号',
    `prod_company`      VARCHAR(255) NOT NULL COMMENT '出品公司',
    `crew_description`  TEXT         NOT NULL COMMENT '公司简介',
    `drama_description` TEXT         NOT NULL COMMENT '电视剧简介',
    `cast`              VARCHAR(255) NOT NULL COMMENT '演员名单',
    `shoot_location`    VARCHAR(255) NOT NULL COMMENT '拍摄地',
    `location_id`       VARCHAR(50)       NOT NULL COMMENT '拍摄地ID，关联locations表',
    `service`           VARCHAR(255) NOT NULL COMMENT '协拍服务',
    `service_id`        VARCHAR(50)       NOT NULL COMMENT '协拍服务ID，关联shoot表',
    `user_id`           BIGINT       NOT NULL COMMENT '用户ID，关联到用户表',
    `deleted`           TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除（0：正常，1：删除）',
    `created_at`        DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `image`             varchar(500)         DEFAULT NULL COMMENT '图片',
    `thumb_image`       varchar(550) DEFAULT NULL COMMENT '压缩后图片',
    PRIMARY KEY (`id`),
    KEY `name_deleted` (`name`,`deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='电视剧备案表';

# 轮播图片表
CREATE TABLE `fi_banner` (
    `id`               INT                NOT NULL AUTO_INCREMENT COMMENT 'id',
    `image_name`       VARCHAR ( 255 )    NOT NULL COMMENT '图片名称',
    `image_url`        VARCHAR ( 255 )    NOT NULL COMMENT '图片地址',
    `target_module`    VARCHAR ( 25 )     NULL COMMENT '跳转模块名称',
    `status`           TINYINT            NOT NULL COMMENT '状态(0:禁用 1:启用)',
    `deleted`          TINYINT            NOT NULL COMMENT '是否删除(0:未删 1:已删)',
    `created_at`       DATETIME           NOT NULL COMMENT '创建时间',
    `updated_at`       DATETIME           NOT NULL COMMENT '更新时间',
    `sort`             INT                NOT NULL COMMENT '排序值，越小越靠前',
    PRIMARY KEY (`id`),
    KEY `idx_status_deleted_sort` (`status`,`deleted`,`sort`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb3;

CREATE TABLE `fi_hotel` (
     `id`              INT                NOT NULL AUTO_INCREMENT,
     `name`            VARCHAR ( 30 )     NOT NULL COMMENT '住宿名称',
     `description`     VARCHAR ( 255 )    NOT NULL COMMENT '介绍',
     `address`         VARCHAR ( 50 )     NOT NULL COMMENT '地址',
     `manager_name`    VARCHAR ( 15 )     NOT NULL COMMENT '负责人名称',
     `manager_phone`   VARCHAR ( 20 )     NOT NULL COMMENT '负责人电话',
     `image`           VARCHAR ( 500 )    DEFAULT NULL COMMENT '图片',
     `created_at`      DATETIME           NOT NULL,
     `updated_at`      DATETIME           NOT NULL,
     `deleted`         TINYINT            NOT NULL,
     `user_id`         int                NOT NULL,
     `thumb_image`     varchar(500)      DEFAULT NULL COMMENT '压缩后图片',
     `longitude`       varchar(40)       NOT NULL COMMENT '经度',
     `latitude`        varchar(40)       NOT NULL COMMENT '纬度',
     PRIMARY KEY (`id`),
     KEY `name_deleted` (`name`,`deleted`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb3;

CREATE TABLE `fi_tours` (
     `id`              int               NOT NULL AUTO_INCREMENT,
     `name`            varchar(255)      NOT NULL COMMENT '体验游名称',
     `description`     varchar(2550)     NOT NULL COMMENT '介绍',
     `theme`           varchar(50)       NOT NULL COMMENT '主题',
     `features`        varchar(2550)     NOT NULL COMMENT '特点',
     `transport`       varchar(2550)     NOT NULL COMMENT '交通方式',
     `hotel`          varchar(2550)     NOT NULL COMMENT '周边旅馆',
     `food`            varchar(2550)     NOT NULL COMMENT '美食推荐',
     `created_at`      datetime          NOT NULL,
     `updated_at`      datetime          NOT NULL,
     `deleted`         tinyint           NOT NULL,
     `image`           varchar(500)     DEFAULT NULL,
     `thumb_image`     varchar(500)     DEFAULT NULL COMMENT '压缩后图片',
     `location_id`     varchar(255)      NOT NULL COMMENT '景点id',
     `drama_id`        varchar(255)      NOT NULL COMMENT '关联影视id',
     PRIMARY KEY (`id`),
     KEY `name_deleted` (`name`,`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE `fi_policy` (
    `id`           int(11)         NOT NULL AUTO_INCREMENT,
    `title`        varchar(255)    NOT NULL COMMENT '政策标题',
    `type`         varchar(255)    NOT NULL COMMENT '政策类型："省级" | "市级"',
    `issue_unit`   varchar(255)    NOT NULL COMMENT '发布单位',
    `issue_time`   datetime        NOT NULL COMMENT '发布时间',
    `content`      text            NOT NULL COMMENT '内容',
    `image`        varchar(500)    DEFAULT NULL COMMENT '图片',
    `thumb_image`  varchar(500)    DEFAULT NULL COMMENT '压缩图片',
    `created_at`   datetime        DEFAULT NULL,
    `updated_at`   datetime        DEFAULT NULL,
    `deleted`      tinyint(4)      NOT NULL,
    `status`       tinyint(4)      NOT NULL COMMENT '0:草稿  1:发布',
     PRIMARY KEY (`id`),
    KEY `title_type_deleted_status` (`title`,`type`,`deleted`,`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

