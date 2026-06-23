#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
星图库演示数据灌入脚本

目标：累计用户 320+、图片 8000+ 张、总存储约 120GB（仅 picSize 字段，不实际上传 COS）

用法：
  pip install pymysql
  python scripts/seed_platform_data.py --host 127.0.0.1 --user root --password your_pass

  # 仅预览，不写库
  python scripts/seed_platform_data.py --dry-run

  # 自定义目标
  python scripts/seed_platform_data.py --target-users 320 --target-pictures 8000 --target-gb 120

环境变量（可选，优先级低于命令行）：
  MYSQL_HOST, MYSQL_PORT, MYSQL_USER, MYSQL_PASSWORD, MYSQL_DATABASE

注意：
  - 不会向腾讯云 COS 上传文件，urls 使用占位图地址（默认 picsum.photos）
  - 公共图库图片 spaceId=0、reviewStatus=1，首页可直接展示
  - 密码算法与 UserServiceImpl 一致：MD5("zhuzhu" + 明文密码)
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import random
import sys
import time
from datetime import datetime, timedelta
from typing import Iterable, List, Sequence, Tuple

try:
    import pymysql
except ImportError:
    pymysql = None

SALT = "zhuzhu"
DEFAULT_PASSWORD = "12345678"
CATEGORIES = ["明星", "活动", "写真", "路透", "杂志", "饭拍"]
TAG_POOL = ["热门", "高清", "壁纸", "路透", "舞台", "机场", "综艺", "影视"]


def md5_password(plain: str) -> str:
    return hashlib.md5((SALT + plain).encode("utf-8")).hexdigest()


def snowflake_like_id(offset: int) -> int:
    """生成与后端雪花风格相近的唯一 id（批量脚本专用）。"""
    base = (int(time.time() * 1000) << 22) + (os.getpid() % 1024 << 12)
    return base + offset


def connect_mysql(args: argparse.Namespace):
    if pymysql is None:
        print("请先安装依赖：pip install pymysql", file=sys.stderr)
        sys.exit(1)
    return pymysql.connect(
        host=args.host,
        port=args.port,
        user=args.user,
        password=args.password,
        database=args.database,
        charset="utf8mb4",
        autocommit=False,
    )


def count_rows(conn, sql: str) -> int:
    with conn.cursor() as cur:
        cur.execute(sql)
        row = cur.fetchone()
        return int(row[0] if row else 0)


def fetch_existing_user_ids(conn) -> List[int]:
    with conn.cursor() as cur:
        cur.execute("SELECT id FROM user WHERE isDelete = 0")
        return [int(r[0]) for r in cur.fetchall()]


def build_users(need: int, start_index: int, password_hash: str) -> List[Tuple]:
    rows = []
    for i in range(need):
        idx = start_index + i
        user_id = snowflake_like_id(1_000_000 + idx)
        account = f"seed{idx:04d}"
        rows.append(
            (
                user_id,
                account,
                password_hash,
                f"演示用户{idx}",
                "user",
                None,
                0,
            )
        )
    return rows


def distribute_sizes(total_bytes: int, count: int, rng: random.Random) -> List[int]:
    """在总量固定前提下，为每张图分配 picSize（字节）。"""
    if count <= 0:
        return []
    avg = total_bytes // count
    sizes = []
    remaining = total_bytes
    for i in range(count - 1):
        # 在 80% ~ 120% 之间波动
        size = int(avg * rng.uniform(0.8, 1.2))
        size = max(size, 512 * 1024)  # 至少 512KB
        sizes.append(size)
        remaining -= size
    sizes.append(max(remaining, 512 * 1024))
    return sizes


def build_pictures(
    need: int,
    start_index: int,
    user_ids: Sequence[int],
    sizes: Sequence[int],
    url_template: str,
    admin_id: int | None,
) -> List[Tuple]:
    rows = []
    now = datetime.now()
    for i in range(need):
        idx = start_index + i
        pic_id = snowflake_like_id(2_000_000 + idx)
        user_id = user_ids[idx % len(user_ids)]
        category = CATEGORIES[idx % len(CATEGORIES)]
        tag = TAG_POOL[idx % len(TAG_POOL)]
        tags_json = json.dumps([tag, "演示"], ensure_ascii=False)
        width = 1920 if idx % 3 else 1080
        height = 1080 if idx % 3 else 1440
        pic_scale = round(width / height, 4)
        pic_format = "webp" if idx % 2 == 0 else "jpeg"
        pic_size = sizes[i]

        seed = f"starpicture-{idx}"
        main_url = url_template.format(seed=seed, idx=idx, w=width, h=height)
        thumb_url = url_template.format(seed=f"{seed}-thumb", idx=idx, w=200, h=200)
        urls_json = json.dumps(
            {"url": main_url, "thumbnailUrl": thumb_url, "originalUrl": main_url},
            ensure_ascii=False,
        )

        review_time = now - timedelta(days=rng_days(idx), hours=idx % 24)
        rows.append(
            (
                pic_id,
                urls_json,
                f"内娱素材_{idx:05d}",
                f"批量演示数据 #{idx}",
                category,
                tags_json,
                None,
                pic_size,
                width,
                height,
                pic_scale,
                pic_format,
                0,  # 公共图库 spaceId
                1,  # 审核通过
                None,
                admin_id,
                review_time,
                user_id,
                0,
            )
        )
    return rows


def rng_days(idx: int) -> int:
    return idx % 180


def batch_insert(conn, sql: str, rows: Iterable[Tuple], batch_size: int = 500) -> int:
    rows = list(rows)
    total = 0
    with conn.cursor() as cur:
        for start in range(0, len(rows), batch_size):
            chunk = rows[start : start + batch_size]
            cur.executemany(sql, chunk)
            total += len(chunk)
            print(f"  已写入 {total}/{len(rows)} 条...")
    return total


def format_bytes(num: int) -> str:
    gb = num / 1024 / 1024 / 1024
    if gb >= 1:
        return f"{gb:.2f} GB"
    mb = num / 1024 / 1024
    return f"{mb:.2f} MB"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="星图库批量灌入演示数据")
    parser.add_argument("--host", default=os.getenv("MYSQL_HOST", "127.0.0.1"))
    parser.add_argument("--port", type=int, default=int(os.getenv("MYSQL_PORT", "3306")))
    parser.add_argument("--user", default=os.getenv("MYSQL_USER", "root"))
    parser.add_argument("--password", default=os.getenv("MYSQL_PASSWORD", ""))
    parser.add_argument("--database", default=os.getenv("MYSQL_DATABASE", "starpicture"))
    parser.add_argument("--target-users", type=int, default=320)
    parser.add_argument("--target-pictures", type=int, default=8000)
    parser.add_argument("--target-gb", type=float, default=120.0)
    parser.add_argument(
        "--demo-password",
        default=DEFAULT_PASSWORD,
        help=f"灌入用户的统一明文密码（默认 {DEFAULT_PASSWORD}）",
    )
    parser.add_argument(
        "--image-url-template",
        default="https://picsum.photos/seed/{seed}/{w}/{h}",
        help="占位图 URL 模板，可用变量：{seed} {idx} {w} {h}",
    )
    parser.add_argument("--dry-run", action="store_true", help="只统计缺口，不写入数据库")
    parser.add_argument("--batch-size", type=int, default=500)
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    target_total_bytes = int(args.target_gb * 1024 * 1024 * 1024)
    password_hash = md5_password(args.demo_password)

    print("=" * 60)
    print("星图库演示数据灌入")
    print(f"目标：用户 {args.target_users}+ | 图片 {args.target_pictures}+ | 约 {args.target_gb} GB")
    print(f"数据库：{args.user}@{args.host}:{args.port}/{args.database}")
    print(f"占位图：{args.image_url_template}")
    print("=" * 60)

    if args.dry_run:
        print("[dry-run] 跳过数据库连接，按目标值计算：")
        print(f"  将新增用户：{args.target_users}")
        print(f"  将新增图片：{args.target_pictures}")
        print(f"  预计总存储：{format_bytes(target_total_bytes)}")
        return

    conn = connect_mysql(args)
    try:
        user_count = count_rows(conn, "SELECT COUNT(*) FROM user WHERE isDelete = 0")
        pic_count = count_rows(conn, "SELECT COUNT(*) FROM picture WHERE isDelete = 0")
        total_size = count_rows(conn, "SELECT IFNULL(SUM(picSize), 0) FROM picture WHERE isDelete = 0")

        need_users = max(0, args.target_users - user_count)
        need_pics = max(0, args.target_pictures - pic_count)

        print(f"当前：用户 {user_count} | 图片 {pic_count} | 存储 {format_bytes(total_size)}")
        print(f"缺口：用户 {need_users} | 图片 {need_pics}")

        if need_users == 0 and need_pics == 0:
            print("已达目标，无需灌入。")
            return

        admin_id = None
        with conn.cursor() as cur:
            cur.execute("SELECT id FROM user WHERE userRole = 'admin' AND isDelete = 0 LIMIT 1")
            row = cur.fetchone()
            if row:
                admin_id = int(row[0])

        if need_users > 0:
            print(f"\n>>> 灌入用户 {need_users} 条（账号 seed0001…，密码 {args.demo_password}）")
            user_rows = build_users(need_users, user_count + 1, password_hash)
            user_sql = """
                INSERT INTO user
                (id, userAccount, userPassword, userName, userRole, mpOpenId, isDelete)
                VALUES (%s, %s, %s, %s, %s, %s, %s)
            """
            batch_insert(conn, user_sql, user_rows, args.batch_size)

        user_ids = fetch_existing_user_ids(conn)
        if not user_ids:
            raise RuntimeError("没有可用 userId，请先保留至少一个用户或成功灌入用户")

        if need_pics > 0:
            print(f"\n>>> 灌入图片 {need_pics} 条（spaceId=0, reviewStatus=1）")
            rng = random.Random(42)
            bytes_gap = max(0, target_total_bytes - total_size)
            if bytes_gap == 0:
                bytes_gap = need_pics * 15 * 1024 * 1024  # 默认每张约 15MB
            sizes = distribute_sizes(bytes_gap, need_pics, rng)
            pic_rows = build_pictures(
                need_pics,
                pic_count + 1,
                user_ids,
                sizes,
                args.image_url_template,
                admin_id,
            )
            pic_sql = """
                INSERT INTO picture
                (id, urls, name, introduction, category, tags, picColor, picSize,
                 picWidth, picHeight, picScale, picFormat, spaceId, reviewStatus,
                 reviewMessage, reviewerId, reviewTime, userId, isDelete)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            """
            batch_insert(conn, pic_sql, pic_rows, args.batch_size)

        conn.commit()

        user_count = count_rows(conn, "SELECT COUNT(*) FROM user WHERE isDelete = 0")
        pic_count = count_rows(conn, "SELECT COUNT(*) FROM picture WHERE isDelete = 0")
        total_size = count_rows(conn, "SELECT IFNULL(SUM(picSize), 0) FROM picture WHERE isDelete = 0")

        print("\n完成！")
        print(f"  用户：{user_count}+")
        print(f"  图片：{pic_count}+")
        print(f"  存储：{format_bytes(total_size)}（约 {total_size / 1024**3:.1f} GB）")
        print("\n验证 SQL：")
        print("  SELECT COUNT(*) FROM user WHERE isDelete = 0;")
        print("  SELECT COUNT(*), IFNULL(SUM(picSize),0) FROM picture WHERE isDelete = 0;")
    except Exception as exc:
        conn.rollback()
        print(f"\n失败，已回滚：{exc}", file=sys.stderr)
        raise
    finally:
        conn.close()


if __name__ == "__main__":
    main()
