CREATE DATABASE if not exists film_fusion;

USE film_fusion;

# user 用户表
CREATE TABLE `fi_users`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `username`    VARCHAR(255) NOT NULL COMMENT '用户名',
    `password`    VARCHAR(255) NOT NULL COMMENT '密码',
    `phoneNumber` VARCHAR(15)  DEFAULT NULL COMMENT '电话号码',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `role`        VARCHAR(255) DEFAULT NULL COMMENT '角色',
    `enabled`     BOOLEAN      DEFAULT TRUE COMMENT '是否启用',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除（0：正常，1：删除）',
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
    `image`         varchar(2550) DEFAULT NULL COMMENT '图片',
    PRIMARY KEY (`id`)
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
    `contact_phone`        VARCHAR(20)   NOT NULL COMMENT '联系人电话',
    `contact_name`         VARCHAR(50)   NOT NULL COMMENT '联系人',
    `address`              VARCHAR(255)  NOT NULL COMMENT '地址',
    `price`                DECIMAL(10,2) NOT NULL COMMENT '价格（元）',
    `user_id`              BIGINT        NOT NULL COMMENT '用户ID，关联到用户表',
    `deleted`              TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0：正常，1：删除）',
    `created_at`           DATETIME               DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`           DATETIME               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `cover`                varchar(200)  NOT NULL,
    `image`                varchar(2550) DEFAULT NULL COMMENT '图片',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='拍摄场地表';

# article 资讯文章表
CREATE TABLE `fi_articles`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`      VARCHAR(100) NOT NULL COMMENT '文章标题',
    `issue_unit` VARCHAR(100) NOT NULL COMMENT '发布单位',
    `issue_time` DATETIME     NOT NULL COMMENT '发布时间',
    `content`    TEXT         NOT NULL COMMENT '内容',
    `user_id`    BIGINT       NOT NULL COMMENT '用户ID，关联到用户表',
    `deleted`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除（0：正常，1：删除）',
    `created_at` DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `cover`      varchar(200)  NOT NULL,
    `image`      varchar(2550) DEFAULT NULL COMMENT '图片',
    PRIMARY KEY (`id`)
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
    `cover`        varchar(200)  NOT NULL,
    `image`        varchar(2550) DEFAULT NULL COMMENT '图片',
    PRIMARY KEY (`id`)
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
    `location_id`       BIGINT       NOT NULL COMMENT '拍摄地ID，关联locations表',
    `service`           VARCHAR(255) NOT NULL COMMENT '协拍服务',
    `service_id`        BIGINT       NOT NULL COMMENT '协拍服务ID，关联shoot表',
    `user_id`           BIGINT       NOT NULL COMMENT '用户ID，关联到用户表',
    `deleted`           TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除（0：正常，1：删除）',
    `created_at`        DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `cover`             varchar(200) NOT NULL,
    `image`             varchar(2550)         DEFAULT NULL COMMENT '图片',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='电视剧备案表';

# 轮播图片表
CREATE TABLE `fi_banner` (
    `id`               INT                NOT NULL AUTO_INCREMENT COMMENT 'id',
    `image_name`       VARCHAR ( 255 )    NOT NULL COMMENT '图片名称',
    `image_url`        VARCHAR ( 255 )    NOT NULL COMMENT '图片地址',
    `target_module`    VARCHAR ( 25 )     NOT NULL COMMENT '跳转模块名称',
    `status`           TINYINT            NOT NULL COMMENT '状态(0:禁用 1:启用)',
    `deleted`          TINYINT            NOT NULL COMMENT '是否删除(0:未删 1:已删)',
    `created_at`       DATETIME           NOT NULL COMMENT '创建时间',
    `updated_at`       DATETIME           NOT NULL COMMENT '更新时间',
    `sort`             INT                NOT NULL COMMENT '排序值，越小越靠前',
    PRIMARY KEY ( `id` )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb3;

# ==================== 插入Mock数据 ====================

# 插入用户数据（密码都是123456的BCrypt加密值）
INSERT INTO `fi_users` (`username`, `password`, `phoneNumber`, `avatar`, `role`, `enabled`, `deleted`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ5O', '13800000001', 'https://cetide-1325039295.cos.ap-chengdu.myqcloud.com/codeforge/course/Docker/default-avatar.png', 'ADMIN', TRUE, 0),
('张导演', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ5O', '13800000002', 'https://cetide-1325039295.cos.ap-chengdu.myqcloud.com/codeforge/course/Docker/default-avatar.png', 'USER', TRUE, 0),
('李制片', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ5O', '13800000003', 'https://cetide-1325039295.cos.ap-chengdu.myqcloud.com/codeforge/course/Docker/default-avatar.png', 'USER', TRUE, 0),
('王编剧', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ5O', '13800000004', 'https://cetide-1325039295.cos.ap-chengdu.myqcloud.com/codeforge/course/Docker/default-avatar.png', 'USER', TRUE, 0),
('赵演员', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ5O', '13800000005', 'https://cetide-1325039295.cos.ap-chengdu.myqcloud.com/codeforge/course/Docker/default-avatar.png', 'USER', TRUE, 0);

# 插入拍摄场地数据
INSERT INTO `fi_locations` (`name`, `type`, `status`, `location_description`, `contact_phone`, `contact_name`, `address`, `price`, `user_id`, `deleted`) VALUES
('雅安古城墙', '历史文化', 1, '雅安古城墙是雅安市重要的历史文化遗址，始建于明代，是研究古代城市防御体系的重要实物资料。城墙保存较为完整，展现了古代建筑工艺的精湛。', '0835-1234567', '张主任', '四川省雅安市雨城区', 5000.00, 1, 0),
('碧峰峡风景区', '自然风光', 1, '碧峰峡是雅安著名的自然风景区，以峡谷、瀑布、原始森林为主要景观。这里山清水秀，空气清新，是拍摄自然风光和生态题材的理想取景地。', '0835-2345678', '李经理', '四川省雅安市雨城区碧峰峡镇', 8000.00, 1, 0),
('上里古镇', '古镇建筑', 1, '上里古镇是四川历史文化名镇，保存了大量明清时期的古建筑，街道古朴，适合拍摄古装剧和历史题材影视作品。', '0835-3456789', '王馆长', '四川省雅安市雨城区上里镇', 6000.00, 1, 0),
('蒙顶山茶园', '自然景观', 1, '蒙顶山是中国茶文化的发源地之一，茶园风光优美，适合拍摄茶文化、田园风光等题材。', '0835-4567890', '赵场长', '四川省雅安市名山区蒙顶山镇', 4000.00, 1, 0),
('熊猫基地', '特色景点', 1, '雅安是大熊猫的故乡，熊猫基地环境优美，适合拍摄动物题材和生态纪录片。', '0835-5678901', '陈主任', '四川省雅安市雨城区', 10000.00, 1, 0);

# 插入协拍服务数据
INSERT INTO `fi_shoots` (`name`, `description`, `price`, `status`, `address`, `phone`, `contact_name`, `user_id`, `deleted`) VALUES
('专业摄影团队', '提供专业摄影师、摄像师、灯光师等全套拍摄团队服务，设备齐全，经验丰富。', 15000.00, 1, '四川省雅安市雨城区', '0835-1111111', '张摄影师', 1, 0),
('场地协调服务', '协助剧组联系拍摄场地，办理相关手续，提供场地使用指导。', 5000.00, 1, '四川省雅安市雨城区', '0835-2222222', '李协调员', 1, 0),
('道具服装租赁', '提供各类影视道具、服装租赁服务，库存丰富，可满足不同题材需求。', 8000.00, 1, '四川省雅安市雨城区', '0835-3333333', '王道具师', 1, 0),
('后期制作服务', '提供视频剪辑、特效制作、音效处理等后期制作服务，设备先进。', 20000.00, 1, '四川省雅安市雨城区', '0835-4444444', '赵剪辑师', 1, 0),
('交通住宿安排', '为剧组提供交通车辆租赁和住宿安排服务，价格优惠，服务周到。', 3000.00, 1, '四川省雅安市雨城区', '0835-5555555', '陈助理', 1, 0);

# 插入文章数据
INSERT INTO `fi_articles` (`title`, `issue_unit`, `issue_time`, `content`, `user_id`, `deleted`) VALUES
('雅安市影视产业发展规划正式发布', '雅安市文化和旅游局', '2024-01-15 10:00:00', '为促进雅安市影视产业发展，提升城市文化软实力，雅安市文化和旅游局正式发布《雅安市影视产业发展规划（2024-2030）》。规划提出，将雅安打造成为西南地区重要的影视拍摄基地和影视产业集聚区。', 1, 0),
('碧峰峡成为热门影视取景地', '雅安日报', '2024-02-20 14:30:00', '近年来，碧峰峡风景区凭借其优美的自然风光和独特的地理环境，吸引了众多影视剧组前来取景拍摄。据统计，今年已有10余部影视作品在此完成拍摄。', 1, 0),
('上里古镇影视拍摄优惠政策出台', '上里镇人民政府', '2024-03-10 09:15:00', '为吸引更多影视剧组前来拍摄，上里镇人民政府出台了一系列优惠政策，包括场地费用减免、住宿优惠等，旨在推动古镇文旅融合发展。', 1, 0),
('雅安影视服务联盟正式成立', '雅安市影视协会', '2024-04-05 16:20:00', '由雅安市多家影视服务企业共同发起的雅安影视服务联盟正式成立，联盟将整合各方资源，为来雅拍摄的剧组提供一站式服务。', 1, 0),
('蒙顶山茶园影视拍摄基地揭牌', '名山区文化和旅游局', '2024-05-12 11:00:00', '蒙顶山茶园影视拍摄基地正式揭牌，该基地将重点打造茶文化主题影视拍摄场景，推动茶文化与影视产业深度融合。', 1, 0);

# 插入影视剧备案数据
INSERT INTO `fi_reports` (`name`, `type`, `genre`, `episodes`, `invest_amount`, `main_creators`, `lead_producer`, `producer_unit`, `start_date`, `end_date`, `crew_scale`, `contact`, `phone_number`, `crew_position`, `user_id`, `deleted`) VALUES
('雅安往事', '电视剧', '历史', 40, 5000.00, '张导演、李编剧、王制片', '雅安影视文化有限公司', '雅安影视制作中心', '2024-06-01', '2024-12-31', '大型', '张导演', '13800000002', '导演', 2, 0),
('茶香雅安', '纪录片', '文化', 6, 800.00, '李制片、赵摄像', '四川文化传播有限公司', '雅安纪录片工作室', '2024-07-01', '2024-09-30', '中型', '李制片', '13800000003', '制片人', 3, 0),
('古镇风云', '网络剧', '悬疑', 24, 2000.00, '王编剧、陈导演', '成都网络影视公司', '上里古镇影视基地', '2024-08-01', '2024-11-30', '中型', '王编剧', '13800000004', '编剧', 4, 0);

# 插入电视剧备案数据
INSERT INTO `fi_dramas` (`name`, `filing_num`, `prod_company`, `crew_description`, `drama_description`, `cast`, `shoot_location`, `location_id`, `service`, `service_id`, `user_id`, `deleted`) VALUES
('雅安往事', '川剧审字[2024]001号', '雅安影视文化有限公司', '雅安影视文化有限公司成立于2020年，专注于影视剧制作和发行，拥有专业的制作团队和丰富的行业经验。', '《雅安往事》是一部以雅安历史为背景的年代剧，讲述了上世纪80年代雅安人民的生活变迁和奋斗历程。', '张演员、李演员、王演员、赵演员', '雅安市雨城区、名山区', 1, '专业摄影团队', 1, 2, 0),
('茶香雅安', '川剧审字[2024]002号', '四川文化传播有限公司', '四川文化传播有限公司是一家专业从事文化传播和纪录片制作的公司，致力于传播四川优秀文化。', '《茶香雅安》是一部以雅安茶文化为主题的纪录片，深入展现蒙顶山茶的历史文化和制作工艺。', '李主持人、王摄像', '雅安市名山区蒙顶山', 4, '场地协调服务', 2, 3, 0),
('古镇风云', '川剧审字[2024]003号', '成都网络影视公司', '成都网络影视公司专注于网络剧制作，作品风格多样，深受年轻观众喜爱。', '《古镇风云》是一部悬疑题材的网络剧，以上里古镇为背景，讲述了一系列离奇案件。', '陈演员、刘演员、周演员', '雅安市雨城区上里古镇', 3, '道具服装租赁', 3, 4, 0);

# 插入反馈数据
INSERT INTO `fi_feedbacks` (`user_id`, `content`, `status`, `type`, `deleted`) VALUES
(2, '希望增加更多拍摄场地的信息，方便我们选择。', '未处理', '建议', 0),
(3, '协拍服务的价格是否可以更透明一些？', '处理中', '建议', 0),
(4, '网站加载速度有点慢，希望能优化一下。', '已解决', '投诉', 0),
(5, '建议增加在线预约功能，方便提前安排拍摄计划。', '未处理', '建议', 0);
