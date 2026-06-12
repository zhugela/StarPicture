-- space 表增加空间类型字段（已有库升级用，仅执行一次）
USE starpicture;

ALTER TABLE `space`
    ADD COLUMN `spaceType` INT NOT NULL DEFAULT 0 COMMENT '空间类型：0-私有 1-团队' AFTER `spaceLevel`;

CREATE INDEX `idx_spaceType` ON `space` (`spaceType`);
