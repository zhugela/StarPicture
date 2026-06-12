-- picture 分表迁移（可选：应用启动时 DynamicShardingManager 也会自动执行 NULL→0 与旗舰空间迁表）
USE starpicture;

-- 1. 公共图库：NULL → 0
UPDATE picture SET spaceId = 0 WHERE spaceId IS NULL;

-- 2. 建议改为 NOT NULL DEFAULT 0（首次执行；若已执行可跳过）
-- ALTER TABLE picture MODIFY COLUMN spaceId BIGINT NOT NULL DEFAULT 0 COMMENT '空间 id，0=公共图库';

-- 3. 旗舰版团队空间手动迁表示例（spaceId=5）：
-- CREATE TABLE IF NOT EXISTS picture_5 LIKE picture;
-- INSERT INTO picture_5 SELECT * FROM picture WHERE spaceId = 5;
-- DELETE FROM picture WHERE spaceId = 5;
