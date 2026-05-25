-- MySQL：将 picture.url / thumbnailUrl 合并为 JSON 列 urls（执行前请备份）
-- 1) 新增列
ALTER TABLE picture ADD COLUMN urls JSON NULL COMMENT 'URL JSON（originalUrl/url/thumbnailUrl/transferUrl）';

-- 2) 迁移旧数据（MySQL 5.7+）
UPDATE picture
SET urls = JSON_OBJECT(
        'originalUrl', NULL,
        'url', url,
        'thumbnailUrl', thumbnailUrl,
        'transferUrl', NULL
    )
WHERE urls IS NULL
  AND (url IS NOT NULL OR thumbnailUrl IS NOT NULL);

-- 3) 删除旧列（确认应用已发布且数据无误后再执行）
-- ALTER TABLE picture DROP COLUMN url, DROP COLUMN thumbnailUrl;
