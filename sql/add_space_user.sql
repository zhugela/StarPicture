-- 空间成员表（已有库升级用，仅执行一次）
USE starpicture;

CREATE TABLE IF NOT EXISTS `space_user`
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
USE starpicture;

-- 1) 公共图库 NULL 改为 0
UPDATE picture SET spaceId = 0 WHERE spaceId IS NULL;

-- 2) 建议 spaceId 不允许 NULL（分片键不能空）
ALTER TABLE picture MODIFY COLUMN spaceId BIGINT NOT NULL DEFAULT 0 COMMENT '空间 id，0=公共图库';

-- 3) 为已有旗舰版团队空间建分表（假设 spaceId=5）
-- CREATE TABLE picture_5 LIKE picture;
-- INSERT INTO picture_5 SELECT * FROM picture WHERE spaceId = 5;
-- DELETE FROM picture WHERE spaceId = 5;