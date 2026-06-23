-- =============================================================================
-- 将演示数据收敛到：约 50 用户 + 清空批量灌入的假图片
-- 在 phpMyAdmin 选中 starpicture 库后执行（执行前请先备份！）
-- =============================================================================

USE starpicture;

-- 1) 删除多余 seed 用户，保留 seed0001 ~ seed0049（共 49 个）+ 原有管理员
DELETE FROM user
WHERE userAccount REGEXP '^seed[0-9]+$'
  AND CAST(SUBSTRING(userAccount, 5) AS UNSIGNED) > 49;

-- 2) 删除脚本灌入的假图片（名称 内娱素材_xxxxx）
DELETE FROM picture
WHERE name LIKE '内娱素材_%';

-- 3) 验证
SELECT COUNT(*) AS users FROM user WHERE isDelete = 0;
SELECT COUNT(*) AS pics, ROUND(IFNULL(SUM(picSize), 0) / 1024 / 1024 / 1024, 2) AS gb
FROM picture WHERE isDelete = 0;

-- 执行完 pics 应为 0（或很少），接下来用网站「批量 URL 上传」导入 300~500 张真图到 COS
