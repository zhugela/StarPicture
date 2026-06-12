-- =============================================================================
-- 星图集 StarPicture 完整数据库脚本（新电脑一键初始化）
-- 适用：MySQL 8.x
-- 执行方式（任选其一）：
--   1) Navicat / IDEA Database：打开本文件 → 运行
--   2) 命令行：mysql -u root -p < starpicture_full.sql
-- 说明：
--   - 会创建库 starpicture，并重建 user / space / picture 三张表
--   - 若库中已有重要数据，请先备份再执行
--   - 文末含可选演示账号（可注释掉 INSERT 段）
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------------------------
-- 1. 创建数据库
-- -----------------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS `starpicture`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `starpicture`;

-- -----------------------------------------------------------------------------
-- 2. 删除旧表（全新安装；有数据请先备份）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `picture`;
DROP TABLE IF EXISTS `space`;
DROP TABLE IF EXISTS `user`;

-- -----------------------------------------------------------------------------
-- 3. 用户表 user（id 由后端雪花算法生成，非自增）
-- -----------------------------------------------------------------------------
CREATE TABLE `user`
(
    `id`           BIGINT       NOT NULL COMMENT 'id（雪花）',
    `userAccount`  VARCHAR(256) NOT NULL COMMENT '账号',
    `userPassword` VARCHAR(512) NOT NULL COMMENT '密码（MD5+盐）',
    `userName`     VARCHAR(256)          DEFAULT NULL COMMENT '用户昵称',
    `userAvatar`   VARCHAR(1024)         DEFAULT NULL COMMENT '用户头像',
    `userProfile`  VARCHAR(512)          DEFAULT NULL COMMENT '用户简介',
    `userRole`     VARCHAR(256) NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin',
    `mpOpenId`     VARCHAR(128)          DEFAULT NULL COMMENT '微信小程序 openId',
    `editTime`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    `createTime`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_userAccount` (`userAccount`),
    UNIQUE KEY `uk_user_mpOpenId` (`mpOpenId`),
    KEY `idx_userName` (`userName`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户';

-- -----------------------------------------------------------------------------
-- 4. 空间表 space
-- -----------------------------------------------------------------------------
CREATE TABLE `space`
(
    `id`         BIGINT AUTO_INCREMENT NOT NULL COMMENT 'id',
    `spaceName`  VARCHAR(128)                   DEFAULT NULL COMMENT '空间名称',
    `spaceLevel` INT                            DEFAULT 0 COMMENT '空间级别：0-普通版 1-专业版 2-旗舰版',
    `spaceType`  INT                  NOT NULL DEFAULT 0 COMMENT '空间类型：0-私有 1-团队',
    `maxSize`    BIGINT                         DEFAULT 0 COMMENT '空间图片最大总大小（字节）',
    `maxCount`   BIGINT                         DEFAULT 0 COMMENT '空间图片最大数量',
    `totalSize`  BIGINT                         DEFAULT 0 COMMENT '当前空间图片总大小',
    `totalCount` BIGINT                         DEFAULT 0 COMMENT '当前空间图片数量',
    `userId`     BIGINT               NOT NULL COMMENT '创建用户 id',
    `createTime` DATETIME             NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `editTime`   DATETIME             NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    `updateTime` DATETIME             NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`   TINYINT              NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_userId` (`userId`),
    KEY `idx_spaceName` (`spaceName`),
    KEY `idx_spaceLevel` (`spaceLevel`),
    KEY `idx_spaceType` (`spaceType`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='空间';

-- -----------------------------------------------------------------------------
-- 4.1 空间成员表 space_user（团队空间成员与角色）
-- -----------------------------------------------------------------------------
CREATE TABLE `space_user`
(
    `id`         BIGINT AUTO_INCREMENT NOT NULL COMMENT 'id',
    `spaceId`    BIGINT                NOT NULL COMMENT '空间 id',
    `userId`     BIGINT                NOT NULL COMMENT '用户 id',
    `spaceRole`  VARCHAR(128)                   DEFAULT 'viewer' COMMENT '空间角色：viewer/editor/admin',
    `createTime` DATETIME              NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME              NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spaceId_userId` (`spaceId`, `userId`),
    KEY `idx_spaceId` (`spaceId`),
    KEY `idx_userId` (`userId`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='空间用户关联';

-- -----------------------------------------------------------------------------
-- 5. 图片表 picture（urls 为 JSON，含原图/主图/缩略图等）
-- -----------------------------------------------------------------------------
CREATE TABLE `picture`
(
    `id`            BIGINT       NOT NULL COMMENT 'id（雪花）',
    `urls`          JSON         NOT NULL COMMENT 'URL JSON：originalUrl/url/thumbnailUrl/transferUrl',
    `name`          VARCHAR(128) NOT NULL COMMENT '图片名称',
    `introduction`  VARCHAR(512)          DEFAULT NULL COMMENT '简介',
    `category`      VARCHAR(64)           DEFAULT NULL COMMENT '分类',
    `tags`          VARCHAR(512)          DEFAULT NULL COMMENT '标签（JSON 数组字符串）',
    `picColor`      VARCHAR(16)           DEFAULT NULL COMMENT '图片主色调',
    `picSize`       BIGINT                DEFAULT NULL COMMENT '图片体积（字节）',
    `picWidth`      INT                   DEFAULT NULL COMMENT '图片宽度',
    `picHeight`     INT                   DEFAULT NULL COMMENT '图片高度',
    `picScale`      DOUBLE                DEFAULT NULL COMMENT '图片宽高比',
    `picFormat`     VARCHAR(32)           DEFAULT NULL COMMENT '图片格式',
    `spaceId`       BIGINT                DEFAULT NULL COMMENT '空间 id（NULL 表示公共图库）',
    `reviewStatus`  INT          NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核 1-通过 2-拒绝',
    `reviewMessage` VARCHAR(512)          DEFAULT NULL COMMENT '审核信息',
    `reviewerId`    BIGINT                DEFAULT NULL COMMENT '审核人 id',
    `reviewTime`    DATETIME              DEFAULT NULL COMMENT '审核时间',
    `userId`        BIGINT       NOT NULL COMMENT '创建用户 id',
    `createTime`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `editTime`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    `updateTime`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_introduction` (`introduction`),
    KEY `idx_category` (`category`),
    KEY `idx_tags` (`tags`),
    KEY `idx_userId` (`userId`),
    KEY `idx_reviewStatus` (`reviewStatus`),
    KEY `idx_spaceId` (`spaceId`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='图片';

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 6. 可选：演示管理员账号（不需要可整段注释或删除）
-- 账号：yupi    密码：12345678    角色：admin
-- 密码算法：MD5("zhuzhu" + 明文密码)，与后端 UserServiceImpl 一致
-- =============================================================================
INSERT INTO `user` (`id`, `userAccount`, `userPassword`, `userName`, `userRole`, `mpOpenId`, `isDelete`)
VALUES (2033484817288159234,
        'yupi',
        '730c4cce81665bf72c0de696891eb05f',
        '管理员',
        'admin',
        NULL,
        0);

-- =============================================================================
-- 7. 可选：演示公共图片（需 COS 上对应图片仍存在；不需要可删除本段）
-- 公共图库条件：spaceId IS NULL 且 reviewStatus = 1
-- =============================================================================
-- INSERT INTO `picture` (`id`, `urls`, `name`, `introduction`, `category`, `tags`,
--                        `picSize`, `picWidth`, `picHeight`, `picScale`, `picFormat`,
--                        `spaceId`, `reviewStatus`, `userId`, `isDelete`)
-- VALUES (2033484817288159235,
--         JSON_OBJECT(
--                 'url', 'https://你的COS域名/示例.jpg',
--                 'thumbnailUrl', 'https://你的COS域名/示例_thumb.jpg'
--         ),
--         '示例图片',
--         '换机后演示用',
--         '模板',
--         '["热门"]',
--         102400,
--         800,
--         600,
--         1.33,
--         'jpeg',
--         NULL,
--         1,
--         2033484817288159234,
--         0);

-- =============================================================================
-- 执行完毕。请在后端 application-local.yml 中配置：
--   spring.datasource.url  → jdbc:mysql://localhost:3306/starpicture?...
--   spring.datasource.username / password → 本机 MySQL 账号密码
-- =============================================================================
