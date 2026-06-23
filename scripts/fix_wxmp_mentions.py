"""
系统修复 3 份 md 文档里所有"file+wxMp"/"微信公众号"/"公众号门户"的描述
林景彬实际做的是"头像上传 + 获取当前用户"
"""
from pathlib import Path

BASE = Path('D:/code/StarPicture/docs/test')

# 替换表
# 通用模块名替换
REPLACE = [
    # 模块名
    ("file + wxMp", "头像 + 获取当前用户"),
    ("file+wxMp", "头像 + 获取当前用户"),
    ("file+wxMp 模块", "头像 + 获取当前用户 模块"),
    ("file + wxMp 模块", "头像 + 获取当前用户 模块"),
    # 测试人员栏位
    ("负责 file + wxMp 模块（文件上传 + 公众号门户 2 个功能点）的测试",
     "负责 头像上传 + 获取当前用户 模块的测试"),
    # 5.x 工作量统计
    ("| 林景彬 | file + wxMp |", "| 林景彬 | 头像 + 获取当前用户 |"),
    # 8.2.4 测试点
    ("#### 8.2.4 file + wxMp 模块（林景彬）", "#### 8.2.4 头像 + 获取当前用户 模块（林景彬）"),
    ("| file + wxMp | 林景彬 | 文件本地上传 + 微信公众号门户 | /file/upload、/wx/mp/portal |",
     "| 头像 + 获取当前用户 | 林景彬 | 头像上传 + 获取当前用户 | /file/upload/avatar、/user/get/login |"),
    # 8.2.4 详细测试点
    ("- 文件本地上传：2MB/超限/空/非图片/未登录", "- 头像上传：jpg/png/超2MB/非图片/空文件"),
    ("- 微信公众号门户：GET 签名/POST XML/创建菜单/缺字段/无签名", "- 获取当前用户：已登录/未登录/Cookie过期/Cookie伪造/头像URL正确"),
    # 说明文档
    ("在 user、picture、space、file+wxMp 四个模块的测试准备过程",
     "在 user、picture、space、头像+获取当前用户 四个模块的测试准备过程"),
    ("将软件需求整理为 **4 个测试项**（user、picture、space、file+wxMp）",
     "将软件需求整理为 **4 个测试项**（user、picture、space、头像+获取当前用户）"),
    # P0 列表
    ("空间创建、文件本地上传、公众号门户", "空间创建、头像上传、获取当前用户"),
    # 追踪关系
    ("| 7 | 文件本地上传 | /file/upload | 林景彬 | TC-FL-001 ~ TC-FL-005 |",
     "| 7 | 头像上传 | /file/upload/avatar | 林景彬 | TC-AT-001 ~ TC-AT-005 |"),
    ("| 8 | 微信公众号门户 | /wx/mp/portal | 林景彬 | TC-WX-001 ~ TC-WX-005 |",
     "| 8 | 获取当前用户 | /user/get/login | 林景彬 | TC-GC-001 ~ TC-GC-005 |"),
    # 报告
    ("针对 user、picture、space、file+wxMp 四个核心模块的测试过程",
     "针对 user、picture、space、头像+获取当前用户 四个核心模块的测试过程"),
    ("| file+wxMp 模块测试 | 林景彬 |", "| 头像+获取当前用户 模块测试 | 林景彬 |"),
    # 4.3 测试人员栏
    ("| 林景彬 | 文件+公众号模块测试、Postman 自动化脚本、环境搭建 |",
     "| 林景彬 | 头像+获取当前用户模块测试、Postman 自动化脚本、环境搭建 |"),
    # 项目简介
    ("微信**公众号**接入等", "微信公众号接入等"),
    # 报告 4.3 残留
    ("文件+公众号模块测试", "头像+获取当前用户模块测试"),
]

# 3 份文档都改
for doc in ["StarPicture_软件测试计划.md", "StarPicture_软件测试说明.md", "StarPicture_软件测试报告.md"]:
    p = BASE / doc
    text = p.read_text(encoding='utf-8')
    original = text
    cnt = 0
    for old, new in REPLACE:
        if old in text:
            text = text.replace(old, new)
            cnt += 1
    if cnt > 0:
        p.write_text(text, encoding='utf-8')
        print(f"  {doc}: 改了 {cnt} 处")

print("\n=== 验证 ===")
for doc in ["StarPicture_软件测试计划.md", "StarPicture_软件测试说明.md", "StarPicture_软件测试报告.md"]:
    text = (BASE / doc).read_text(encoding='utf-8')
    for kw in ['wxMp', '微信公众号', '公众号门户', 'file + wxMp', 'file+wxMp']:
        cnt = text.count(kw)
        if cnt > 0:
            print(f"  ⚠️ {doc} 还有 '{kw}' {cnt} 处")
        else:
            pass
    # 找下 头像
    for kw in ['头像', '获取当前用户', 'TC-AT', 'TC-GC', '2310820051']:
        if kw in text:
            print(f"  ✓ {doc} 包含 '{kw}'")

print("\n完成！")