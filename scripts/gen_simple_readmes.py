"""
按新格式（4 个子目录：功能/性能/接口/安全）重写 4 份 README
先删旧的再写新的
"""
from pathlib import Path

BASE = Path("D:/code/StarPicture/docs/test")

CASES_SUMMARY = {
    "朱远亮": {
        "module": "user（用户模块）",
        "cases": 13,
        "feature_points": [
            ("用户注册", "TC-UR-001 ~ TC-UR-005", "账号密码正确/已存在/密码不一致/账号为空/账号长度不足"),
            ("用户登录", "TC-UL-001 ~ TC-UL-005", "账号密码正确/密码错误/账号不存在/空 body/密码小于8字符"),
        ],
        "perf": "登录 50 并发（JMeter）",
        "api": "注册 Content-Type 错误（Postman）",
        "security": "登录 SQL 注入（BurpSuite）",
        "perf_file": "login_50concurrent.jmx",
        "api_file": "user_api.postman_collection.json",
        "sec_file": "用户模块-安全报告模板.docx",
        "perf_pdf": "用户模块-安全报告.pdf",
        "scan_file": "用户模块.scan",
    },
    "李冠燃": {
        "module": "picture（图片模块）",
        "cases": 13,
        "feature_points": [
            ("图片本地上传", "TC-PU-001 ~ TC-PU-005", "jpg/png/超2MB/非图片/空文件"),
            ("关键字搜索", "TC-PX-001 ~ TC-PX-005", "有结果/无结果/空文本/超长/未登录"),
        ],
        "perf": "上传 20 并发（JMeter）",
        "api": "上传缺 multipart 边界（Postman）",
        "security": "伪 PHP 木马上传（BurpSuite）",
        "perf_file": "picture_upload_20concurrent.jmx",
        "api_file": "picture_api.postman_collection.json",
        "sec_file": "图片模块-安全报告模板.docx",
        "perf_pdf": "图片模块-安全报告.pdf",
        "scan_file": "图片模块.scan",
    },
    "李坤纬": {
        "module": "space（空间模块）",
        "cases": 13,
        "feature_points": [
            ("空间创建", "TC-SP-001 ~ TC-SP-005", "获取等级/创建/名称空/名称超长/未登录"),
            ("空间成员管理", "TC-SU-001 ~ TC-SU-005", "添加/查询/重复/未登录/删除"),
        ],
        "perf": "空间分析 20 并发（JMeter）",
        "api": "创建空间缺 Content-Type（Postman）",
        "security": "空间名称 XSS（BurpSuite）",
        "perf_file": "space_analyze_20concurrent.jmx",
        "api_file": "space_api.postman_collection.json",
        "sec_file": "空间模块-安全报告模板.docx",
        "perf_pdf": "空间模块-安全报告.pdf",
        "scan_file": "空间模块.scan",
    },
    "林景彬": {
        "module": "file + wxMp（文件+公众号）",
        "cases": 13,
        "feature_points": [
            ("文件本地上传", "TC-FL-001 ~ TC-FL-005", "本地上传 2MB/超限/空/非图片/未登录"),
            ("微信公众号门户", "TC-WX-001 ~ TC-WX-005", "GET 签名/POST XML/创建菜单/缺字段/无签名"),
        ],
        "perf": "文件上传 50 并发（JMeter）",
        "api": "上传缺 multipart 边界（Postman）",
        "security": "伪 PHP 木马上传（BurpSuite）",
        "perf_file": "file_upload_50concurrent_1MB.jmx",
        "api_file": "file_wxmp_api.postman_collection.json",
        "sec_file": "文件_公众号模块-安全报告模板.docx",
        "perf_pdf": "文件_公众号模块-安全报告.pdf",
        "scan_file": "文件_公众号模块.scan",
    },
}

TEMPLATE = """# {name}_脚本与截图

> **姓名**：{name}
> **负责模块**：{module}
> **用例总数**：{cases} 条（2 个功能点 × 4-5 条 + 性能 1 + 接口 1 + 安全 1）
> **用例文件**：[软件测试测试用例.xlsx](./软件测试测试用例.xlsx)

## 我的工作清单

| 类型 | 用例数 | 工作 |
|---|---|---|
| 功能测试 | 10 | 2 个功能点 |
| 性能测试 | 1 | {perf} |
| 接口测试 | 1 | {api} |
| 安全测试 | 1 | {security} |

## 我的 2 个功能点

{feature_points_block}

## 4 个子目录具体放什么

```
{name}_脚本与截图/
├── 软件测试测试用例.xlsx              ← 13 条用例清单
├── 安全测试/                          ← PDF + .scan
│   ├── {sec_file}
│   ├── {perf_pdf}                     ← 待办：填完模板导出 PDF
│   └── {scan_file}                    ← 待办：BurpSuite 扫出
├── 性能测试/                          ← .jmx + 截图
│   ├── {perf_file}
│   └── summary_report_xxx.png         ← 待办：跑 JMeter 截图
├── 接口测试/                          ← .json + 截图
│   ├── {api_file}
│   └── TC-API-001_xxx.png             ← 待办：Postman 截图
├── TUTORIAL_保姆级教程.md             ← 详细操作步骤
└── README.md                          ← 本文件
```

## 具体待办（按时间）

### Day 1（6/17 下午 ~ 6/18 中午）
- [ ] 评审自己写的 13 条用例
- [ ] 装 Postman + JMeter + BurpSuite

### Day 2（6/18 上午 ~ 6/19 中午）
- [ ] 跑 10 条功能用例，每条截 1 张图
- [ ] 跑 1 条性能用例（JMeter），截图 Summary Report
- [ ] 跑 1 条接口用例（Postman），截图
- [ ] 跑 1 条安全用例（BurpSuite），截图

### Day 3（6/19 上午 ~ 6/19 晚）
- [ ] WPS 打开 `{sec_file}`，填实际结果 → 导出 PDF（{perf_pdf}）
- [ ] BurpSuite 扫后端，导出 `{scan_file}`
- [ ] 回归测试
- [ ] 把缺陷填到 `软件测试报告.xlsx`

## 不会做的看 TUTORIAL_保姆级教程.md

那个文件里有每一步详细操作（Postman/JMeter/BurpSuite/WPS 装 + 用）。

---

**有问题群里 @ {name}**
"""

for name, info in CASES_SUMMARY.items():
    fp_block = "\n".join(
        f"{i+1}. **{fp[0]}**（{fp[1]}）\n   - {fp[2]}"
        for i, fp in enumerate(info["feature_points"])
    )
    content = TEMPLATE.format(
        name=name,
        module=info["module"],
        cases=info["cases"],
        perf=info["perf"],
        api=info["api"],
        security=info["security"],
        feature_points_block=fp_block,
        perf_file=info["perf_file"],
        api_file=info["api_file"],
        sec_file=info["sec_file"],
        perf_pdf=info["perf_pdf"],
        scan_file=info["scan_file"],
    )
    target = BASE / f"{name}_脚本与截图/README.md"
    target.write_text(content, encoding='utf-8')
    print(f"已生成: {target}")

print(f"\n共 {len(CASES_SUMMARY)} 份 README。")