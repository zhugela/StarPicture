"""
按 13 条/人 重写 4 份保姆级教程（基于新格式：4 个子目录 + 2 个功能点）
"""
from pathlib import Path

BASE = Path("D:/code/StarPicture/docs/test")

MEMBERS = [
    {
        "name": "朱远亮",
        "module": "user 用户模块",
        "feature_points": [
            ("用户注册", "TC-UR-001 ~ TC-UR-005",
             ["1. POST /user/register userAccount=Zyl_New01 userPassword=12345678 checkPassword=12345678 → 期望 code=0",
              "2. 重复注册 → 期望 code=40001 提示账号重复",
              "3. 两次密码不一致 → 期望 code=40001",
              "4. userAccount='' → 期望 code=40001",
              "5. userAccount='abc'(3字符) → 期望 code=40001 长度不足"]),
            ("用户登录", "TC-UL-001 ~ TC-UL-005",
             ["1. POST /user/login userAccount=testuser01 userPassword=12345678 → 期望 code=0, Set-Cookie 含登录态",
              "2. password=wrongpass → 期望 code=40001",
              "3. userAccount=testuser99 → 期望 code=40001",
              "4. 空 body → 期望 code=40001",
              "5. password='1234567'(7字符) → 期望 code=40001"]),
        ],
        "jmx": "login_50concurrent.jmx",
        "api_col": "user_api.postman_collection.json",
        "sec_doc": "用户模块-安全报告模板.docx",
        "sec_pdf": "用户模块-安全报告.pdf",
        "scan_file": "用户模块.scan",
        "sec_target": "访问 http://localhost:8123/api/user/login  → 密码输入 ' OR 1=1 -- 看是否被拒",
        "api_endpoints": ["POST /user/register", "POST /user/login", "GET /user/get/login", "POST /user/update/my", "POST /user/add", "POST /user/list/page/vo", "POST /user/logout"]
    },
    {
        "name": "李冠燃",
        "module": "picture 图片模块",
        "feature_points": [
            ("图片本地上传", "TC-PU-001 ~ TC-PU-005",
             ["1. POST /file/upload file=2MB_jpg → 期望 code=0, url 字段",
              "2. file=1MB_png → 期望 code=0, picFormat=png",
              "3. file=5MB_jpg → 期望 code=40001 超限",
              "4. file=test.pdf → 期望 code=40001 非图片",
              "5. file=0byte → 期望 code=40001 空文件"]),
            ("关键字搜索", "TC-PX-001 ~ TC-PX-005",
             ["1. POST /picture/search/picture text='猫' → 期望含'猫'的图片列表",
              "2. text='xyz999' → 期望空列表",
              "3. text='' → 期望 code=40001 或全部图片",
              "4. text=1000字符 → 期望 code=40001 超长",
              "5. 无 Cookie 调用 → 期望 code=40100"]),
        ],
        "jmx": "picture_upload_20concurrent.jmx",
        "api_col": "picture_api.postman_collection.json",
        "sec_doc": "图片模块-安全报告模板.docx",
        "sec_pdf": "图片模块-安全报告.pdf",
        "scan_file": "图片模块.scan",
        "sec_target": "访问 http://localhost:8123/api/file/upload → 上传 1.jpg（内容是 <?php system($_GET['c']);?>）看是否被拒",
        "api_endpoints": ["POST /file/upload", "POST /picture/upload/url", "POST /picture/upload/batch", "POST /picture/delete", "POST /picture/edit", "POST /picture/list/page", "POST /picture/review", "POST /picture/search/picture", "GET /picture/proxy/editor"]
    },
    {
        "name": "李坤纬",
        "module": "space 空间模块",
        "feature_points": [
            ("空间创建", "TC-SP-001 ~ TC-SP-005",
             ["1. GET /space/list/level → 期望含普通版/专业版/旗舰版",
              "2. POST /space/add spaceName='我的空间' spaceLevel=0 → 期望 code=0",
              "3. spaceName='' → 期望 code=40001",
              "4. spaceName=50+'x' → 期望 code=40001 超长",
              "5. 无 Cookie → 期望 code=40100"]),
            ("空间成员管理", "TC-SU-001 ~ TC-SU-005",
             ["1. admin POST /spaceUser/add spaceId=1 userId=2 spaceRole='viewer' → 期望 code=0",
              "2. POST /spaceUser/list spaceId=1 → 期望返回成员列表",
              "3. 重复添加 → 期望 code=40001",
              "4. 无 Cookie → 期望 code=40100",
              "5. POST /spaceUser/delete id=1 → 期望 code=0, isDelete=1"]),
        ],
        "jmx": "space_analyze_20concurrent.jmx",
        "api_col": "space_api.postman_collection.json",
        "sec_doc": "空间模块-安全报告模板.docx",
        "sec_pdf": "空间模块-安全报告.pdf",
        "scan_file": "空间模块.scan",
        "sec_target": "访问 http://localhost:8123/api/space/add → spaceName=<script>alert(1)</script> 看是否被转义",
        "api_endpoints": ["GET /space/list/level", "POST /space/add", "POST /space/edit", "POST /space/delete", "POST /spaceUser/add", "POST /spaceUser/list", "POST /spaceUser/delete", "POST /space/analyze/usage"]
    },
    {
        "name": "林景彬",
        "module": "file + wxMp 模块",
        "feature_points": [
            ("文件本地上传", "TC-FL-001 ~ TC-FL-005",
             ["1. POST /file/upload file=2MB_jpg → 期望 code=0",
              "2. file=5MB_jpg → 期望 code=40001 超限",
              "3. file=0byte → 期望 code=40001 空文件",
              "4. file=test.pdf → 期望 code=40001 非图片",
              "5. 无 Cookie → 期望 code=40100"]),
            ("微信公众号门户", "TC-WX-001 ~ TC-WX-005",
             ["1. GET /wx/mp/portal 带 signature/timestamp/nonce/echostr → 期望返回 echostr 明文",
              "2. POST /wx/mp/portal body=XML → 期望 code=0, 响应 XML",
              "3. admin POST /wx/mp/menu/create → 期望 code=0",
              "4. POST /wx/mp/menu/create 空 body → 期望 code=40001",
              "5. POST /wx/mp/menu/create 无 Cookie → 期望 code=40100/40300"]),
        ],
        "jmx": "file_upload_50concurrent_1MB.jmx",
        "api_col": "file_wxmp_api.postman_collection.json",
        "sec_doc": "文件_公众号模块-安全报告模板.docx",
        "sec_pdf": "文件_公众号模块-安全报告.pdf",
        "scan_file": "文件_公众号模块.scan",
        "sec_target": "访问 http://localhost:8123/api/file/upload → 上传 1.jpg（内容是 <?php ...?>）看是否被拒",
        "api_endpoints": ["POST /file/test/upload", "POST /file/upload", "POST /file/upload/avatar", "GET /wx/mp/portal", "POST /wx/mp/menu/create"]
    },
]

TEMPLATE = """# {name} · 软件测试保姆级教程

> **姓名**：{name}
> **负责模块**：{module}
> **用例总数**：13 条（2 个功能点 + 1 性能 + 1 接口 + 1 安全）
> **总工时**：约 3-4 小时，分 3 天
> **零基础也能跟做**，每步给"点击哪里 → 填什么 → 期待看到什么"

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

## 📁 你的 4 个子目录（每个都要交东西）

```
{name}_脚本与截图/
├── 软件测试测试用例.xlsx      ← 13 条用例（已生成）
├── 安全测试/                  ← PDF + .scan（2 小时）
├── 性能测试/                  ← .jmx + 截图（30 分钟）
└── 接口测试/                  ← .json + 截图（20 分钟）
```

---

## 1️⃣ 性能测试（30 分钟）

### 步骤 1：装 JMeter

1. 下载 `apache-jmeter-5.6.3.zip` → 解压到 `D:\\Program Files\\apache-jmeter-5.6.3`
2. **配环境变量**：
   - Win+R → `sysdm.cpl` → 高级 → 环境变量
   - 系统变量 → 新建：`JMETER_HOME` = `D:\\Program Files\\apache-jmeter-5.6.3`
   - 系统变量 → 找 `Path` → 编辑 → 新建 → 加 `%JMETER_HOME%\\bin`
3. 验证：Win+R → cmd → 输入 `jmeter` → 期待弹出 JMeter 窗口

### 步骤 2：跑你的 .jmx

1. 打开 JMeter → File → Open → 选 `{jmx}`
2. 左侧 `线程组` → 改 `Number of Threads (users)` = 50 或 20
3. `Ramp-up period` = 5（5 秒内启动）
4. 左侧 `HTTP 请求` → `Server Name or IP` = `localhost`，`Port Number` = 8123
5. 点运行 ▶
6. 左侧 → Add → Listener → Summary Report

### 步骤 3：截图

1. 把 Summary Report 窗口拉大
2. `Win+Shift+S` → 窗口截图 → 框选 JMeter
3. 保存到 `性能测试/summary_report_xxx.png`

### 判断通过

| 指标 | 目标 |
|---|---|
| Error % | < 5% |
| Average | 登录 < 500ms，上传 < 3s |

---

## 2️⃣ 接口测试（20 分钟）

### 步骤 1：装 Postman

下载 → 安装 → 打开 → 注册账号

### 步骤 2：导入你的集合

Postman File → Import → Upload Files → 选 `{api_col}`

### 步骤 3：配置环境

1. 右上角 `Environments` → `+` 新建
2. Variable | Initial Value
   - `baseUrl` | `http://localhost:8123/api`
3. 选这个环境为 active

### 步骤 4：跑请求并截图

1. 左侧展开集合 → 点第一个用例
2. 右侧 `Send`
3. **期待看到右下 Response** 有 JSON 错误信息
4. `Win+Shift+S` → 框选整个 Postman → 保存到 `接口测试/TC-API-001_xxx.png`

---

## 3️⃣ 安全测试（2 小时）

### 步骤 3.1：导出 PDF 安全报告（30 分钟）

1. 打开 WPS 文字 → File → Open → 选 `{sec_doc}`
2. 找所有 `[待填]` 替换成实际结果
3. 写一段总结（200 字）
4. **导出 PDF**：File → Export as PDF → 保存到 `安全测试/{sec_pdf}`

### 步骤 3.2：生成 .scan 文件（1.5 小时）

#### 方法 A：用 BurpSuite Community（**推荐免费**）

1. 下载 BurpSuite Community → 安装 → 打开
2. Proxy → Intercept → 关闭拦截
3. 浏览器配代理 127.0.0.1:8080（用 FoxyProxy 扩展）
4. {sec_target}
5. Target → Site map → 找到接口 → 右键 → Save item → 选 `xxx.scan`
6. 保存到 `安全测试/{scan_file}`

> 装不上 BurpSuite？写个 markdown 改名为 `xxx.scan.md` 也行。

---

## 📋 你的 2 个功能点（共 10 条用例）

{fp_block}

---

## 📋 Day 1-3 时间表

| 时间 | 做什么 |
|---|---|
| Day 1 上午 | 装软件（Postman、JMeter、BurpSuite） |
| Day 1 下午 | 跑功能用例，截图 |
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

for m in MEMBERS:
    fp_block = ""
    for i, (name, ids, steps) in enumerate(m["feature_points"], 1):
        fp_block += f"### 功能点 {i}：{name}（{ids}）\n\n"
        for s in steps:
            fp_block += f"- {s}\n"
        fp_block += "\n"
    content = TEMPLATE.format(
        name=m["name"],
        module=m["module"],
        jmx=m["jmx"],
        api_col=m["api_col"],
        sec_doc=m["sec_doc"],
        sec_pdf=m["sec_pdf"],
        scan_file=m["scan_file"],
        sec_target=m["sec_target"],
        fp_block=fp_block.strip(),
    )
    target = BASE / f"{m['name']}_脚本与截图/TUTORIAL_保姆级教程.md"
    target.write_text(content, encoding='utf-8')
    print(f"已生成: {target}")

print(f"\n共 {len(MEMBERS)} 份保姆级教程。")