-- 小程序微信登录：user 表增加 mpOpenId
ALTER TABLE `user`
    ADD COLUMN `mpOpenId` VARCHAR(128) NULL COMMENT '微信小程序 openId' AFTER `userRole`;

CREATE UNIQUE INDEX `uk_user_mpOpenId` ON `user` (`mpOpenId`);
