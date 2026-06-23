"""
更新 4 份 README 和 4 份 TUTORIAL：
加入"功能测试"段落（之前删了，现在加回来）
5 个子目录：功能/性能/接口/安全 + 顶层 README + 教程 + xlsx
"""
from pathlib import Path

BASE = Path("D:/code/StarPicture/docs/test")

CASES = {
    "朱远亮": {
        "module": "user（用户模块）",
        "feature_points": [
            ("用户注册", "TC-UR-001 ~ TC-UR-005", "账号密码正确/已存在/密码不一致/账号为空/账号长度不足"),
            ("用户登录", "TC-UL-001 ~ TC-UL-005", "账号密码正确/密码错误/账号不存在/空 body/密码小于8字符"),
        ],
        "perf": "登录 50 并发（JMeter）",
        "api": "注册 Content-Type 错误（Postman）",
        "security": "登录 SQL 注入（BurpSuite）",
        "perf_file": "login_50concurrent.jmx",
        "api_file": "user_api.postman_collection.json",
        "sec_doc": "用户模块-安全报告模板.docx",
        "sec_pdf": "用户模块-安全报告.pdf",
        "scan_file": "用户模块.scan",
    },
    "李冠燃": {
        "module": "picture（图片模块）",
        "feature_points": [
            ("图片本地上传", "TC-PU-001 ~ TC-PU-005", "jpg/png/超2MB/非图片/空文件"),
            ("关键字搜索", "TC-PX-001 ~ TC-PX-005", "有结果/无结果/空文本/超长/未登录"),
        ],
        "perf": "上传 20 并发（JMeter）",
        "api": "上传缺 multipart 边界（Postman）",
        "security": "伪 PHP 木马上传（BurpSuite）",
        "perf_file": "picture_upload_20concurrent.jmx",
        "api_file": "picture_api.postman_collection.json",
        "sec_doc": "图片模块-安全报告模板.docx",
        "sec_pdf": "图片模块-安全报告.pdf",
        "scan_file": "图片模块.scan",
    },
    "李坤纬": {
        "module": "space（空间模块）",
        "feature_points": [
            ("空间创建", "TC-SP-001 ~ TC-SP-005", "获取等级/创建/名称空/名称超长/未登录"),
            ("空间成员管理", "TC-SU-001 ~ TC-SU-005", "添加/查询/重复/未登录/删除"),
        ],
        "perf": "空间分析 20 并发（JMeter）",
        "api": "创建空间缺 Content-Type（Postman）",
        "security": "空间名称 XSS（BurpSuite）",
        "perf_file": "space_analyze_20concurrent.jmx",
        "api_file": "space_api.postman_collection.json",
        "sec_doc": "空间模块-安全报告模板.docx",
        "sec_pdf": "空间模块-安全报告.pdf",
        "scan_file": "空间模块.scan",
    },
    "林景彬": {
        "module": "file + wxMp（文件+公众号）",
        "feature_points": [
            ("文件本地上传", "TC-FL-001 ~ TC-FL-005", "本地上传 2MB/超限/空/非图片/未登录"),
            ("微信公众号门户", "TC-WX-001 ~ TC-WX-005", "GET 签名/POST XML/创建菜单/缺字段/无签名"),
        ],
        "perf": "文件上传 50 并发（JMeter）",
        "api": "上传缺 multipart 边界（Postman）",
        "security": "伪 PHP 木马上传（BurpSuite）",
        "perf_file": "file_upload_50concurrent_1MB.jmx",
        "api_file": "file_wxmp_api.postman_collection.json",
        "sec_doc": "文件_公众号模块-安全报告模板.docx",
        "sec_pdf": "文件_公众号模块-安全报告.pdf",
        "scan_file": "文件_公众号模块.scan",
    },
}

README_TEMPLATE = """# {name}_脚本与截图

> **姓名**：{name}
> **负责模块**：{module}
> **用例总数**：13 条（2 个功能点 × 5 条 + 性能 1 + 接口 1 + 安全 1）
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

## 5 个子目录具体放什么

```
{name}_脚本与截图/
├── 软件测试测试用例.xlsx              ← 13 条用例清单
├── 功能测试/                          ← 10 张 Postman 用例截图
├── 安全测试/                          ← PDF + .scan
│   ├── {sec_doc}
│   ├── {sec_pdf}                      ← 待办：填完模板导出 PDF
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
- [ ] 跑 10 条功能用例，每条截 1 张图，存到 `功能测试/`
- [ ] 跑 1 条性能用例（JMeter），截图 Summary Report
- [ ] 跑 1 条接口用例（Postman），截图
- [ ] 跑 1 条安全用例（BurpSuite），截图

### Day 3（6/19 上午 ~ 6/19 晚）
- [ ] WPS 打开 `{sec_doc}`，填实际结果 → 导出 PDF（{sec_pdf}）
- [ ] BurpSuite 扫后端，导出 `{scan_file}`
- [ ] 回归测试
- [ ] 把缺陷填到 `软件测试报告.md`

## 不会做的看 TUTORIAL_保姆级教程.md

---

**有问题群里 @ {name}**
"""

for name, info in CASES.items():
    fp_block = "\n".join(
        f"{i+1}. **{fp[0]}**（{fp[1]}）\n   - {fp[2]}"
        for i, fp in enumerate(info["feature_points"])
    )
    content = README_TEMPLATE.format(
        name=name,
        module=info["module"],
        perf=info["perf"],
        api=info["api"],
        security=info["security"],
        feature_points_block=fp_block,
        perf_file=info["perf_file"],
        api_file=info["api_file"],
        sec_doc=info["sec_doc"],
        sec_pdf=info["sec_pdf"],
        scan_file=info["scan_file"],
    )
    (BASE / f"{name}_脚本与截图/README.md").write_text(content, encoding='utf-8')
    print(f"README: {name}")

# ============= TUTORIAL 也更新 =============
TUTORIAL_TEMPLATE = """# {name} · 软件测试保姆级教程

> **姓名**：{name}
> **负责模块**：{module}
> **用例总数**：13 条（2 个功能点 + 1 性能 + 1 接口 + 1 安全）
> **总工时**：约 3-4 小时，分 3 天
> **零基础也能跟做**

---

## ⚙️ 必装软件（Day 1 上午装好）

| 软件 | 下载 |
|---|---|
| JDK 17 | 已有 |
| IntelliJ IDEA 2024+ | 已有 |
| Postman 9.x | https://www.postman.com/downloads/ |
| JMeter 5.6 | https://jmeter.apache.org/download_jmeter.cgi |
| WPS Office | 已有 |
| BurpSuite Community | https://portswigger.net/burp/communitydownload |

---

## 📁 你的 5 个子目录

```
{name}_脚本与截图/
├── 软件测试测试用例.xlsx     ← 13 条用例（已生成）
├── 功能测试/                 ← 10 张 Postman 用例截图
├── 安全测试/                 ← PDF + .scan（2 小时）
├── 性能测试/                 ← .jmx + 截图（30 分钟）
└── 接口测试/                 ← .json + 截图（20 分钟）
```

---

## 1️⃣ 功能测试（30 分钟）

### 步骤 1：装 Postman

下载 Postman 9.x → 安装 → 注册账号

### 步骤 2：导入你的集合

1. Postman File → Import → Upload Files → 选 `软件测试测试用例.xlsx` 对应模块的接口集合（同接口测试的 json）

### 步骤 3：配置环境

1. 右上角 `Environments` → `+` 新建
2. Variable | Initial Value
   - `baseUrl` | `http://localhost:8123/api`
3. 选这个环境为 active

### 步骤 4：跑 10 条功能用例并截图

1. 左侧展开集合 → 点你要测的功能点用例
2. 右侧 `Send`
3. **期待看到右下 Response** 有 JSON 响应
4. `Win+Shift+S` → 框选整个 Postman → 保存到 `功能测试/TC-XXX-NNN_xxx.png`

### 你的 10 条功能用例

{feature_steps_block}

---

## 2️⃣ 性能测试（30 分钟）

### 步骤 1：装 JMeter

1. 下载 `apache-jmeter-5.6.3.zip` → 解压到 `D:\\Program Files\\apache-jmeter-5.6.3`
2. **配环境变量**：
   - Win+R → `sysdm.cpl` → 高级 → 环境变量
   - 系统变量 → 新建：`JMETER_HOME` = `D:\\Program Files\\apache-jmeter-5.6.3`
   - 系统变量 → 找 `Path` → 编辑 → 新建 → 加 `%JMETER_HOME%\\bin`
3. 验证：Win+R → cmd → 输入 `jmeter` → 期待弹出 JMeter 窗口

### 步骤 2：跑你的 .jmx

1. 打开 JMeter → File → Open → 选 `{perf_file}`
2. 左侧 `线程组` → 改 `Number of Threads (users)` = 50 或 20
3. `Ramp-up period` = 5
4. 左侧 `HTTP 请求` → `Server Name or IP` = `localhost`，`Port Number` = 8123
5. 点运行 ▶
6. 左侧 → Add → Listener → Summary Report

### 步骤 3：截图

1. `Win+Shift+S` → 框选 JMeter
2. 保存到 `性能测试/summary_report_xxx.png`

---

## 3️⃣ 接口测试（20 分钟）

### 步骤 1：导入你的集合

Postman File → Import → Upload Files → 选 `{api_file}`

### 步骤 2：跑接口用例

1. 左侧展开集合 → 点接口用例
2. 右侧 `Send`
3. 期待看到 JSON 错误信息
4. 截图保存到 `接口测试/TC-API-001_xxx.png`

---

## 4️⃣ 安全测试（2 小时）

### 步骤 4.1：导出 PDF 安全报告（30 分钟）

1. 打开 WPS 文字 → File → Open → 选 `{sec_doc}`
2. 找所有 `[待填]` 替换成实际结果
3. 写一段总结（200 字）
4. **导出 PDF**：File → Export as PDF → 保存到 `安全测试/{sec_pdf}`

### 步骤 4.2：生成 .scan 文件（1.5 小时）

#### 方法 A：用 BurpSuite Community（**推荐免费**）

1. 下载 BurpSuite Community → 安装 → 打开
2. Proxy → Intercept → 关闭拦截
3. 浏览器配代理 127.0.0.1:8080（用 FoxyProxy 扩展）
4. {sec_target}
5. Target → Site map → 找到接口 → 右键 → Save item → 选 `xxx.scan`
6. 保存到 `安全测试/{scan_file}`

> 装不上 BurpSuite？写个 markdown 改名为 `xxx.scan.md` 也行。

---

## 📋 Day 1-3 时间表

| 时间 | 做什么 |
|---|---|
| Day 1 上午 | 装软件（Postman、JMeter、BurpSuite） |
| Day 1 下午 | 功能测试，截 10 张图 |
| Day 2 上午 | 性能测试 + 接口测试 |
| Day 2 下午 | 安全测试（BurpSuite 扫） |
| Day 3 上午 | 写安全报告 PDF |
| Day 3 下午 | 回归 + 汇总缺陷 |
| Day 3 晚 | 群里汇报 |

---

## 🆘 出问题 @ 谁

| 问题 | @ 谁 |
|---|---|
| 后端起不来 | 林景彬 |
| 不知道某 API 怎么测 | 同模块的人 |
| BurpSuite 不会用 | 全员一起百度 |
| 完全卡死 | 朱远亮（组长） |

---

**教程作者**：朱远亮（组长）
**最后更新**：2026-06-19
"""

# 每个成员的 10 条功能用例步骤
FEATURE_STEPS = {
    "朱远亮": [
        ("用户注册", "TC-UR-001 ~ TC-UR-005", [
            "1. POST /user/register userAccount=Zyl_New01 userPassword=12345678 checkPassword=12345678 → 期望 code=0",
            "2. 重复注册 → 期望 code=40001",
            "3. 两次密码不一致 → 期望 code=40001",
            "4. userAccount='' → 期望 code=40001",
            "5. userAccount='abc'(3字符) → 期望 code=40001",
        ]),
        ("用户登录", "TC-UL-001 ~ TC-UL-005", [
            "1. POST /user/login userAccount=testuser01 userPassword=12345678 → 期望 code=0, Set-Cookie",
            "2. password=wrongpass → 期望 code=40001",
            "3. userAccount=testuser99 → 期望 code=40001",
            "4. 空 body → 期望 code=40001",
            "5. password='1234567'(7字符) → 期望 code=40001",
        ]),
    ],
    "李冠燃": [
        ("图片本地上传", "TC-PU-001 ~ TC-PU-005", [
            "1. POST /file/upload file=2MB_jpg → 期望 code=0, url",
            "2. file=1MB_png → 期望 code=0, picFormat=png",
            "3. file=5MB_jpg → 期望 code=40001 超限",
            "4. file=test.pdf → 期望 code=40001 非图片",
            "5. file=0byte → 期望 code=40001 空文件",
        ]),
        ("关键字搜索", "TC-PX-001 ~ TC-PX-005", [
            "1. POST /picture/search/picture text='猫' → 期望含'猫'的图片",
            "2. text='xyz999' → 期望空列表",
            "3. text='' → 期望 code=40001",
            "4. text=1000字符 → 期望 code=40001",
            "5. 无 Cookie → 期望 code=40100",
        ]),
    ],
    "李坤纬": [
        ("空间创建", "TC-SP-001 ~ TC-SP-005", [
            "1. GET /space/list/level → 期望含普通版/专业版/旗舰版",
            "2. POST /space/add spaceName='我的空间' spaceLevel=0 → 期望 code=0",
            "3. spaceName='' → 期望 code=40001",
            "4. spaceName=50+'x' → 期望 code=40001",
            "5. 无 Cookie → 期望 code=40100",
        ]),
        ("空间成员管理", "TC-SU-001 ~ TC-SU-005", [
            "1. admin POST /spaceUser/add spaceId=1 userId=2 → 期望 code=0",
            "2. POST /spaceUser/list spaceId=1 → 期望返回成员列表",
            "3. 重复添加 → 期望 code=40001",
            "4. 无 Cookie → 期望 code=40100",
            "5. POST /spaceUser/delete id=1 → 期望 code=0",
        ]),
    ],
    "林景彬": [
        ("文件本地上传", "TC-FL-001 ~ TC-FL-005", [
            "1. POST /file/upload file=2MB_jpg → 期望 code=0",
            "2. file=5MB_jpg → 期望 code=40001",
            "3. file=0byte → 期望 code=40001",
            "4. file=test.pdf → 期望 code=40001",
            "5. 无 Cookie → 期望 code=40100",
        ]),
        ("微信公众号门户", "TC-WX-001 ~ TC-WX-005", [
            "1. GET /wx/mp/portal 带签名 → 期望返回 echostr",
            "2. POST /wx/mp/portal body=XML → 期望 code=0",
            "3. admin POST /wx/mp/menu/create → 期望 code=0",
            "4. POST /wx/mp/menu/create 空 body → 期望 code=40001",
            "5. POST /wx/mp/menu/create 无 Cookie → 期望 code=40100/40300",
        ]),
    ],
}

SEC_TARGET = {
    "朱远亮": "访问 http://localhost:8123/api/user/login → 密码输入 ' OR 1=1 -- 看是否被拒",
    "李冠燃": "访问 http://localhost:8123/api/file/upload → 上传 1.jpg（内容是 <?php system($_GET['c']);?>）看是否被拒",
    "李坤纬": "访问 http://localhost:8123/api/space/add → spaceName=<script>alert(1)</script> 看是否被转义",
    "林景彬": "访问 http://localhost:8123/api/file/upload → 上传 1.jpg（内容是 <?php ...?>）看是否被拒",
}

for name, info in CASES.items():
    steps_block = ""
    for fp_name, fp_ids, fp_steps in FEATURE_STEPS[name]:
        steps_block += f"\n### 功能点：{fp_name}（{fp_ids}）\n\n"
        for s in fp_steps:
            steps_block += f"- {s}\n"
    content = TUTORIAL_TEMPLATE.format(
        name=name,
        module=info["module"],
        perf_file=info["perf_file"],
        api_file=info["api_file"],
        sec_doc=info["sec_doc"],
        sec_pdf=info["sec_pdf"],
        scan_file=info["scan_file"],
        sec_target=SEC_TARGET[name],
        feature_steps_block=steps_block.strip(),
    )
    (BASE / f"{name}_脚本与截图/TUTORIAL_保姆级教程.md").write_text(content, encoding='utf-8')
    print(f"TUTORIAL: {name}")

print("\n完成！")