"""
一键按老师要求格式化：
1. 顶层 5 个文件改名加 StarPicture_ 前缀
2. 4 个成员文件夹保留 6 个子目录
3. 4 份分模块 xlsx 里的 4 份就放着（成员文件夹里）
4. 把每个成员的 README 里"功能测试"段落删掉（之前没改过来）
"""
import os
import shutil
from pathlib import Path

BASE = Path("D:/code/StarPicture/docs/test")

# 1. 顶层文件改名
renames_top = {
    "01_软件测试计划_CS001.md":    "StarPicture_软件测试计划.md",
    "02_软件测试说明_CS002.md":    "StarPicture_软件测试说明.md",
    "03_软件测试报告_CS003.md":    "StarPicture_软件测试报告.md",
    "01_软件测试计划_CS001.docx":  "StarPicture_软件测试计划.docx",
    "02_软件测试说明_CS002.docx":  "StarPicture_软件测试说明.docx",
    "03_软件测试报告_CS003.docx":  "StarPicture_软件测试报告.docx",
    "评分表.xlsx":                  "StarPicture_评分表.xlsx",
    "软件测试用例汇总.xlsx":         "StarPicture_测试用例.xlsx",
}
for old, new in renames_top.items():
    old_p = BASE / old
    if old_p.exists():
        new_p = BASE / new
        if not new_p.exists():
            shutil.move(str(old_p), str(new_p))
            print(f"移动: {old} -> {new}")
        else:
            print(f"已存在: {new}")
    else:
        print(f"找不到: {old}")

# 2. 4 个成员文件夹（已经按 6 个子目录结构），不动
# 3. 更新 4 份 README —— 把"功能测试/"段落删掉，其他保持
print("\n--- 更新 4 份 README（删 功能测试 段落）---")
import re
for m in ["朱远亮", "李冠燃", "李坤纬", "林景彬"]:
    rp = BASE / f"{m}_脚本与截图/README.md"
    if not rp.exists():
        continue
    text = rp.read_text(encoding='utf-8')
    # 删掉"### 1. `功能测试/` ... ### 2. `性能测试/` 整段
    # 用正则匹配 "### 1. `功能测试/`" 开头到 "### 2." 之前
    new_text = re.sub(
        r'### 1\. `功能测试/`.*?(?=### 2\.)',
        '',
        text,
        flags=re.DOTALL
    )
    # 重新编号（### 2 → ### 1, ### 3 → ### 2, ...）
    counter = [0]
    def renumber(m):
        counter[0] += 1
        return f"### {counter[0]}. `{m.group(1)}`"
    new_text = re.sub(r'### \d\. `([^`]+)`', renumber, new_text)
    # 工作清单里的"功能测试"行也删掉
    lines = new_text.split('\n')
    new_lines = []
    for line in lines:
        if line.strip().startswith('| 功能测试'):
            continue
        new_lines.append(line)
    final = '\n'.join(new_lines)
    rp.write_text(final, encoding='utf-8')
    print(f"已更新: {rp}")

# 4. 验证最终顶层
print("\n--- 最终顶层 ---")
for p in sorted(BASE.iterdir()):
    if p.is_file():
        print(f"  {p.name}")
    else:
        print(f"  {p.name}/")
        for sp in sorted(p.iterdir()):
            tag = "/" if sp.is_dir() else ""
            print(f"    {sp.name}{tag}")
