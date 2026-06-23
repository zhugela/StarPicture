"""
更新 4 份保姆级教程的 5.1 节
报告已自动填好，5.1 步骤简化为复制 PDF / 重命名
"""
import re
from pathlib import Path

BASE = Path("D:/code/StarPicture/docs/test")

# 新 5.1 节
NEW_5_1 = """### 5.1 复制 PDF 安全报告（5 分钟，无需手工填）

**好消息**：模板里所有 [待填] 都已经按测试结果填好了（SQL 注入/越权/木马等测试结论）。你**只需要确认 PDF 文件存在**，不用自己写。

**步骤**：

1. 打开 `D:\\code\\StarPicture\\docs\\test\\[你的文件夹]\\安全测试\\` 文件夹
2. **检查 PDF 文件**：
   - 理想情况：有 `xxx-安全报告.pdf`（已生成好）→ **直接用，跳到下一步**
   - 如果文件名带"模板"（如 `图片模块-安全报告模板.pdf`）→ 右键重命名，把"模板"删掉，改成 `图片模块-安全报告.pdf`
3. 打开 PDF 看一眼，确认有：
   - 标题"内娱图库（StarPicture）安全测试报告"
   - 高危/中危/低危/建议 4 个等级数字
   - 至少 3 条测试项结论
   - 第 7 节"测试结论"
4. **如果 PDF 完全不存在**：
   - 打开 WPS 文字 → 打开 `安全测试\\xxx-安全报告模板.docx`
   - 检查 [待填] 已填好
   - 文件 → 导出为 PDF → 文件名 `xxx-安全报告.pdf` → 保存到 `安全测试/`

**这一步约 5 分钟**。
"""

for m in ["朱远亮", "李冠燃", "李坤纬", "林景彬"]:
    rp = BASE / f"{m}_脚本与截图/TUTORIAL_保姆级教程.md"
    text = rp.read_text(encoding='utf-8')
    pattern = r'### 5\.1 写安全报告 PDF.*?(?=### 5\.2)'
    new_text = re.sub(
        pattern,
        lambda m: NEW_5_1.strip() + "\n\n---\n\n",
        text,
        count=1,
        flags=re.DOTALL
    )
    rp.write_text(new_text, encoding='utf-8')
    print(f"已更新: {rp}")

print("\n完成！")