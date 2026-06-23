"""
把 4 份 README 里所有"兼容测试"相关内容删掉：
1. 工作清单表格里的"兼容测试"行
2. 树状图里的 `├── 兼容测试/`
3. `### N. 兼容测试/` 整段
4. 把 6 改回 5（标题"6 个子目录"也改）
"""
import re
from pathlib import Path

BASE = Path("D:/code/StarPicture/docs/test")

for m in ["朱远亮", "李冠燃", "李坤纬", "林景彬"]:
    rp = BASE / f"{m}_脚本与截图/README.md"
    if not rp.exists():
        continue
    text = rp.read_text(encoding='utf-8')
    # 1. 删工作清单的"兼容测试"行
    lines = text.split('\n')
    lines = [l for l in lines if not l.strip().startswith('| 兼容测试')]
    text = '\n'.join(lines)
    # 2. 删树状图里的兼容测试行
    text = re.sub(r'├── 兼容测试/.*\n', '', text)
    text = re.sub(r'└── 兼容测试/.*\n', '', text)
    # 3. 删 `### N. 兼容测试/` 整段
    text = re.sub(r'### \d+\. `兼容测试/`.*?(?=### \d+\.)', '', text, flags=re.DOTALL)
    # 4. 标题里的 7/6 → 5
    text = re.sub(r'## 7 个子目录说明', '## 5 个子目录说明', text)
    text = re.sub(r'## 7 个子目录具体放什么', '## 5 个子目录具体放什么', text)
    # 5. Day 1/2 待办里可能提到兼容测试，删
    lines = text.split('\n')
    new_lines = []
    for l in lines:
        if '兼容' in l and ('张' in l or '浏览器' in l):
            continue
        new_lines.append(l)
    text = '\n'.join(new_lines)
    rp.write_text(text, encoding='utf-8')
    print(f"已更新: {rp.name}")
print("\n--- 验证: 不应再出现 兼容测试 ---")
for m in ["朱远亮", "李冠燃", "李坤纬", "林景彬"]:
    rp = BASE / f"{m}_脚本与截图/README.md"
    content = rp.read_text(encoding='utf-8')
    cnt = content.count('兼容')
    print(f"{m}_脚本与截图/README.md: 出现 {cnt} 次 '兼容'")
