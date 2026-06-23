"""
把 StarPicture_测试操作保姆级教程.md 拆成 4 份
每份按成员名 + 模块定制
放到各成员文件夹下，文件名 TUTORIAL_保姆级教程.md
"""
from pathlib import Path
import re

BASE = Path("D:/code/StarPicture/docs/test")
src = BASE / "StarPicture_测试操作保姆级教程.md"
base_text = src.read_text(encoding='utf-8')

MEMBERS = [
    {
        "name": "朱远亮",
        "module": "user 用户模块",
        "jmx": "login_50concurrent.jmx",
        "api_col": "user_api.postman_collection.json",
        "auto_col": "user_crud_automation.postman_collection.json",
        "unit_java": "UserServiceTest.java",
        "security_doc": "用户模块-安全报告模板.docx",
        "security_pdf": "用户模块-安全报告.pdf",
        "scan_file": "用户模块.scan",
        "api_test_cases": ["API-001_注册_Content-Type错误", "API-002_登录_无Content-Type", "API-003_分页_缺current", "API-004_获取_负数id", "API-005_注册_缺字段"],
        "security_targets": [
            "访问 http://localhost:8123/api/user/login  → 用户名输入 ' OR 1=1 -- 看是否被拒",
            "访问 http://localhost:8123/api/user/add → 用 testuser01（非 admin）调用看是否返回 40300",
            "访问 http://localhost:8123/api/user/list/page/vo → 用 testuser01 调用看是否返回 40300",
        ],
        "api_endpoints": [
            "POST /user/register", "POST /user/login", "GET /user/get/login",
            "POST /user/update/my", "POST /user/add", "GET /user/get",
            "GET /user/get/vo", "POST /user/list/page/vo", "POST /user/update",
            "POST /user/delete", "POST /user/logout"
        ]
    },
    {
        "name": "李冠燃",
        "module": "picture 图片模块",
        "jmx": ["picture_upload_20concurrent.jmx", "picture_query_50concurrent.jmx"],
        "api_col": "picture_api.postman_collection.json",
        "auto_col": "picture_automation.postman_collection.json",
        "unit_java": ["FileManagerTest.java", "PictureServiceTest.java"],
        "security_doc": "图片模块-安全报告模板.docx",
        "security_pdf": "图片模块-安全报告.pdf",
        "scan_file": "图片模块.scan",
        "api_test_cases": ["API-001_上传_缺multipart边界", "API-002_编辑_id类型错误", "API-003_查询_负数id", "API-004_分页_pageSize超限", "API-005_搜索_空文本", "API-006_搜索_颜色_非法值"],
        "security_targets": [
            "访问 http://localhost:8123/api/picture/upload → 上传 1.jpg（内容是 <?php system($_GET['c']);?>）看是否被拒",
            "访问 http://localhost:8123/api/picture/upload/url → fileUrl=file:///etc/passwd 看是否被拒",
            "访问 http://localhost:8123/api/picture/edit → name=<img src=x onerror=alert(1)> 看是否被转义",
        ],
        "api_endpoints": [
            "POST /picture/upload", "POST /picture/upload/url", "POST /picture/upload/batch",
            "POST /picture/delete", "POST /picture/edit", "POST /picture/edit/batch",
            "POST /picture/update", "POST /picture/list/page", "POST /picture/list/page/vo",
            "POST /picture/list/page/vo/cache", "GET /picture/get", "GET /picture/get/vo",
            "GET /picture/tag_category", "POST /picture/review",
            "POST /picture/search/picture", "POST /picture/search/color",
            "POST /picture/out_painting/create_task", "GET /picture/out_painting/get_task",
            "GET /picture/proxy/editor"
        ]
    },
    {
        "name": "李坤纬",
        "module": "space 空间模块",
        "jmx": "space_analyze_20concurrent.jmx",
        "api_col": "space_api.postman_collection.json",
        "auto_col": "space_automation.postman_collection.json",
        "unit_java": "SpaceServiceTest.java",
        "security_doc": "空间模块-安全报告模板.docx",
        "security_pdf": "空间模块-安全报告.pdf",
        "scan_file": "空间模块.scan",
        "api_test_cases": ["API-001_空间_缺Content-Type", "API-002_成员_缺字段", "API-003_分析_缺spaceId", "API-004_编辑_负数id"],
        "security_targets": [
            "访问 http://localhost:8123/api/space/add → spaceName=<script>alert(1)</script> 看是否被转义",
            "访问 http://localhost:8123/api/spaceUser/add → 用普通成员调用 add 看是否返回 40300",
        ],
        "api_endpoints": [
            "GET /space/list/level", "POST /space/save", "POST /space/add",
            "GET /space/get", "GET /space/get/vo", "POST /space/list/page",
            "POST /space/list/page/vo", "POST /space/edit", "POST /space/delete",
            "POST /space/update",
            "POST /spaceUser/add", "POST /spaceUser/get", "POST /spaceUser/list",
            "POST /spaceUser/list/my", "POST /spaceUser/edit", "POST /spaceUser/delete",
            "POST /space/analyze/usage", "POST /space/analyze/category",
            "POST /space/analyze/tag", "POST /space/analyze/size",
            "POST /space/analyze/user", "POST /space/analyze/rank"
        ]
    },
    {
        "name": "林景彬",
        "module": "file + wxMp 模块",
        "jmx": "file_upload_50concurrent_1MB.jmx",
        "api_col": "file_wxmp_api.postman_collection.json",
        "auto_col": "file_automation.postman_collection.json",
        "unit_java": "CosManagerTest.java",
        "security_doc": "文件_公众号模块-安全报告模板.docx",
        "security_pdf": "文件_公众号模块-安全报告.pdf",
        "scan_file": "文件_公众号模块.scan",
        "api_test_cases": ["API-001_upload_缺multipart边界", "API-002_upload_Content-Type错误", "API-003_avatar_无文件", "API-004_wx_portal_无签名", "API-005_wx_menu_无body"],
        "security_targets": [
            "访问 http://localhost:8123/api/file/upload → 上传 1.jpg（内容是 <?php ...?>）看是否被拒",
            "访问 http://localhost:8123/api/file/upload → 上传双扩展名 1.jpg.php 看是否被拒",
            "访问 http://localhost:8123/api/file/upload/avatar → 用 testuser01 上传（看是否只改本人头像）",
        ],
        "api_endpoints": [
            "POST /file/test/upload", "POST /file/upload", "POST /file/upload/avatar",
            "GET /wx/mp/portal", "POST /wx/mp/portal", "POST /wx/mp/menu/create"
        ]
    },
]


def render_tutorial(m):
    jmx_list = m['jmx'] if isinstance(m['jmx'], list) else [m['jmx']]
    unit_list = m['unit_java'] if isinstance(m['unit_java'], list) else [m['unit_java']]

    jmx_section = '\n'.join(f"- `{jx}`" for jx in jmx_list)
    unit_section = '\n'.join(f"- `{u}`" for u in unit_list)
    api_cases = '\n'.join(f"- {c}" for c in m['api_test_cases'])
    sec_targets = '\n'.join(f"- {t}" for t in m['security_targets'])
    api_endpoints = '\n'.join(f"- `{e}`" for e in m['api_endpoints'])

    return f"""# {m['name']} · 软件测试保姆级教程

> **你的名字**：{m['name']}
> **负责模块**：{m['module']}
> **用例总数**：见 `软件测试测试用例.xlsx`
> **总工时**：约 6-8 小时，分 3 天
> **零基础也能跟做**，每一步给出"点击哪里 → 填什么 → 期待看到什么"

---

## ⚙️ 必装软件（Day 1 上午装好）

| 软件 | 下载 | 必装 |
|---|---|---|
| JDK 17 | 已有 | ✅ |
| IntelliJ IDEA 2024+ | 已有 | ✅ |
| Postman 9.x | https://www.postman.com/downloads/ | ✅ |
| JMeter 5.6 | https://jmeter.apache.org/download_jmeter.cgi | ✅ |
| WPS Office | 已有 | ✅ |
| BurpSuite Community | https://portswigger.net/burp/communitydownload | ✅（免费） |

---

## 📁 你的 5 个子目录（每个都要交东西）

```
{m['name']}_脚本与截图/
├── README.md
├── 软件测试测试用例.xlsx
├── 性能测试/    ← 跑 .jmx + 截图（30 分钟）
├── 接口测试/    ← 跑 .json + 截图（20 分钟）
├── 安全测试/    ← PDF + .scan（2 小时）
├── 自动化测试/  ← 跑 Runner + 截图（20 分钟）
└── 单元测试/    ← 复制 .java + 跑 + 截图（30 分钟）
```

---

## 1️⃣ 性能测试（30 分钟）

### 步骤 1：装 JMeter

1. 下载 `apache-jmeter-5.6.3.zip` → 解压到 `D:\\Program Files\\apache-jmeter-5.6.3`
2. **配环境变量**（重要！）：
   - Win+R → `sysdm.cpl` → 高级 → 环境变量
   - 系统变量 → 新建：变量名 `JMETER_HOME`，变量值 `D:\\Program Files\\apache-jmeter-5.6.3`
   - 系统变量 → 找 `Path` → 编辑 → 新建 → 加 `%JMETER_HOME%\\bin`
3. 验证：Win+R → `cmd` → 输入 `jmeter` → 期待弹出 JMeter 窗口

### 步骤 2：跑你的 .jmx

1. 打开 JMeter
2. File → Open → 选你的脚本：
{jmx_section}
3. **改线程组**：
   - 左侧 `线程组` → 右侧 `Number of Threads (users)` 改 50 或 20（看脚本）
   - `Ramp-up period` 改 5（5 秒内启动）
4. **改目标地址**：
   - 左侧 `HTTP 请求` → 右侧 `Server Name or IP` = `localhost`，`Port Number` = `8123`
5. **点运行**：左上角绿色 ▶ 按钮
6. **加 Summary Report**：左侧 → Add → Listener → Summary Report
7. 等跑完（10-30 秒）

### 步骤 3：截图

1. 把 Summary Report 窗口拉大
2. `Win+Shift+S` → 窗口截图 → 框选 JMeter
3. 保存到 `性能测试/` 目录，文件名 `summary_report_xxx.png`

### 判断通过

| 指标 | 目标 |
|---|---|
| Error % | < 5% |
| Average | 登录 < 500ms，上传 < 3s |
| 90% Line | 登录 < 1s，上传 < 5s |

---

## 2️⃣ 接口测试（20 分钟）

### 步骤 1：装 Postman

下载 Postman 9.x → 安装 → 打开 → 注册账号

### 步骤 2：导入你的集合

1. Postman File → Import → Upload Files
2. 选 `{m['api_col']}`
3. 左侧出现 `StarPicture_{m['module']}_接口测试`

### 步骤 3：配置环境变量

1. 右上角 `Environments` → `+` 新建
2. Variable | Initial Value
   - `baseUrl` | `http://localhost:8123/api`
3. 选这个环境为 active

### 步骤 4：跑每个请求并截图

1. 左侧展开集合 → 点第一个用例（{m['api_test_cases'][0]}）
2. 右侧点 `Send`
3. **期待看到右下 Response 区域**有 JSON 错误信息（如 `{{"code": 40001, "message": "..."}}`）
4. **截图**（关键！）：
   - `Win+Shift+S` → 窗口截图 → 框选整个 Postman 窗口
   - **必须看到**：URL、请求 body、响应 code、响应 message
5. 保存到 `接口测试/` 目录，文件名 `TC-API-001_xxx.png`

### 你的 5 个接口用例
{api_cases}

---

## 3️⃣ 自动化测试（20 分钟）

### 步骤 1：导入 Runner 集合

导入文件：`{m['auto_col']}`

### 步骤 2：Runner 跑

1. 左侧点集合名旁的 `...` → Run collection
2. Runner 窗口：
   - **Iterations**：填 10（循环 10 次）
   - **Delay**：填 100 ms（每个请求间隔 100ms）
3. 点 `Run StarPicture_xxx`
4. 跑完显示结果：所有请求都是绿色 ✓

### 步骤 3：截图

框选 Runner 窗口，保存到 `自动化测试/`，文件名 `runner_xxx.png`

---

## 4️⃣ 单元测试（30 分钟）

### 步骤 1：复制 .java 到项目

1. 打开 IntelliJ IDEA → 打开 `D:\\code\\StarPicture`
2. 左侧 `src` → `test` → `java`（没有就右键 src → New → Directory → 命名 test → 再 New → Directory → 命名 java）
3. 在 `com.yu.backend` 包下 → 右键 → New → Java Class
4. 类名 = 你的 .java 文件名：
{unit_section}
5. 复制 `{m['name']}_脚本与截图/单元测试/xxx.java` 内容粘贴进去

### 步骤 2：跑测试

1. 类名左边点绿色 ▶ → Run 'xxxTest'
2. IDEA 底部 Run 面板显示绿色 = 通过
3. **如果报错**（很常见！）：
   - `找不到类 UserMapper` → 改 `import` 路径
   - `找不到类 UserRegisterRequest` → 看项目里实际叫什么，改名
   - `Mockito cannot mock final class` → 加 `@SpringBootTest` 在类名前
   - 都搞不定 → 群里 @ 朱远亮

### 步骤 3：截图

IDEA 底部 Run 面板 → 框选整个 IDEA → 保存到 `单元测试/单元测试.png`

---

## 5️⃣ 安全测试（2 小时，最复杂）

### 步骤 5.1：导出 PDF 安全报告（30 分钟）

1. 打开 WPS 文字
2. File → Open → 选 `{m['security_doc']}`
3. 找所有 `[待填]` 替换成实际结果
4. 写一段总结（200 字）：覆盖了哪些、发现哪些漏洞、风险等级
5. **导出 PDF**：File → Export as PDF → 保存到 `安全测试/{m['security_pdf']}`

### 步骤 5.2：生成 .scan 文件（1.5 小时）

#### 方法 A：用 BurpSuite Community（**推荐免费**）

1. 下载 BurpSuite Community Edition
2. 安装 → 打开 → 接受条款
3. Proxy → Intercept → 关闭拦截
4. 配置浏览器代理 127.0.0.1:8080（用 FoxyProxy 扩展）
5. 在浏览器测：
{sec_targets}
6. **Target → Site map** → 找到测过的接口 → 右键 → Save item → 选 `xxx.scan` 格式
7. 保存到 `安全测试/{m['scan_file']}`

> 装不上 BurpSuite？**写个 markdown** 改名为 `xxx.scan.md`，写明测过哪些、发现什么风险。

---

## 📋 你的 API 清单（共 {len(m['api_endpoints'])} 个）

{api_endpoints}

---

## 📋 Day 2-3 时间表

| 时间 | 做什么 |
|---|---|
| **Day 1 上午** | 装软件（Postman、JMeter、BurpSuite、WPS） |
| **Day 1 下午** | 单元测试 + 性能测试 |
| **Day 2 上午** | 接口测试 + 自动化测试 |
| **Day 2 下午** | 跑功能用例截图（58/65/39/19 条） |
| **Day 3 上午** | 安全报告 PDF |
| **Day 3 下午** | BurpSuite 扫出 .scan |
| **Day 3 晚** | 汇总缺陷 + 群里汇报 |

---

## 🆘 出问题找谁

| 问题 | @ 谁 |
|---|---|
| 后端起不来 | 林景彬 |
| SQL 不会写 | 朱远亮 |
| BurpSuite 不会用 | 全员一起百度 |
| 不知道某 API 怎么测 | 同模块的人 |
| 完全卡死 | 朱远亮（组长） |

---

**教程作者**：朱远亮（组长）
**最后更新**：2026-06-19
"""


for m in MEMBERS:
    target = BASE / f"{m['name']}_脚本与截图/TUTORIAL_保姆级教程.md"
    content = render_tutorial(m)
    target.write_text(content, encoding='utf-8')
    print(f"已生成: {target}")

print(f"\n共 {len(MEMBERS)} 份保姆级教程。")
