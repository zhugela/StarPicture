"""
给李冠燃/李坤纬/林景彬生成保姆级教程
基于朱远亮的模板，换 6 个变量
"""
from pathlib import Path

BASE = Path("D:/code/StarPicture/docs/test")

TEMPLATE = """# {name} · 保姆级教程（零基础也能跟做）

> **你的名字**：{name}
> **学号**：{student_id}
> **负责模块**：{module_desc}
> **用例总数**：13 条（10 功能 + 1 性能 + 1 接口 + 1 安全）
> **目标**：跟着做，3 天内交齐所有文件
> **难度**：⭐⭐⭐

---

## 📋 0. 你要交的所有东西

打开你的文件夹 `D:\\code\\StarPicture\\docs\\test\\{folder}\`，里面已经有：

```
{folder}/
├── README.md                            ← 说明
├── TUTORIAL_保姆级教程.md               ← 你正在看的教程
├── 软件测试测试用例.xlsx                 ← 13 条用例（不要改）
│
├── 功能测试/                            ← 你要做：跑 10 个用例 + 截 10 张图
│   └── {functional_json}
│
├── 性能测试/                            ← 你要做：跑 JMeter + 截图
│   └── {perf_jmx}
│
├── 接口测试/                            ← 你要做：跑 1 个用例 + 截图
│   └── {api_json}
│
└── 安全测试/                            ← 你要做：填报告 + 写 .scan
    └── {security_docx}
```

**你最后要交的是**：

| 目录 | 文件 | 数量 |
|---|---|---|
| 功能测试/ | {tc1}_xxx.png ~ {tc2}_xxx.png | **10 张** |
| 性能测试/ | summary_report_xxx.png | **1 张** |
| 接口测试/ | {api_first_name}.png | **1 张** |
| 安全测试/ | {sec_pdf_name} | **1 个** |
| 安全测试/ | {scan_name} | **1 个** |

---

## ⏱️ 1. Day 1 下午：装软件（1 小时）

### 1.1 装 Postman（接口测试工具）

**步骤**：

1. 打开浏览器，访问 https://www.postman.com/downloads/
2. 点 "Windows 64-bit" 下载（约 200MB）
3. 双击下载的 `Postman-win64-Setup.exe` 安装
4. 安装完自动打开 Postman
5. 第一次打开要注册：点 "Create Account" → 用你的邮箱注册 → 登录
6. **看到主界面就 OK**

**验证**：打开 Postman，左上角应该有 "Workspaces"，右侧应该有 "Send" 按钮。

---

### 1.2 装 JMeter（性能测试工具）

**步骤**：

1. 访问 https://jmeter.apache.org/download_jmeter.cgi
2. 找 "Apache JMeter 5.6.3" 下的 "Binaries" → 下载 `apache-jmeter-5.6.3.zip`
3. 下载完（约 80MB），**右键 → 解压到 `D:\\Program Files\\`**
4. 解压完会有 `D:\\Program Files\\apache-jmeter-5.6.3\\bin\\jmeter.bat`

**配环境变量**（必须做！）：

1. 按 `Win + R` 键，输入 `sysdm.cpl`，回车
2. 切到 "高级" 标签页
3. 点底部的 "环境变量(N)..."
4. 在 "系统变量(S)" 那里点 "新建(W)..."
   - 变量名：`JMETER_HOME`
   - 变量值：`D:\\Program Files\\apache-jmeter-5.6.3`
   - 点确定
5. 在 "系统变量(S)" 那里找 `Path` → 选中 → 点 "编辑(I)..."
6. 点右侧 "新建(N)" → 输入 `%JMETER_HOME%\\bin` → 确定
7. 一路确定关掉所有窗口

**验证**：

1. 按 `Win + R`，输入 `cmd`，回车
2. 在黑窗口输入 `jmeter`，回车
3. **期待弹出 JMeter 图形窗口**（中间有一个 "Apache JMeter" 标题的窗口）
4. 如果弹出，**关掉 JMeter 窗口**就行，环境配好了

---

### 1.3 装 BurpSuite（安全测试工具）

**步骤**：

1. 访问 https://portswigger.net/burp/communitydownload
2. 选 "Windows 64-bit" → 下载（约 500MB）
3. 双击安装 `burp-suite-windows-installer.exe`
4. 安装完桌面会有 "Burp Suite Community Edition" 图标
5. 双击打开
6. 第一次会弹窗 "Welcome to Burp Suite" → 直接点 "Next" → 接受条款 → 选 "Temporary project" → Start Burp
7. **主界面长这样**：左边 Dashboard / Proxy / Intruder 等标签

---

### 1.4 验证后端能起来（重要！）

群里问林景彬：**"后端起来了没？"**。如果他说"起了"，打开浏览器访问：

```
http://localhost:8123/api/doc.html
```

**期待看到**：Knife4j 的接口文档页面。如果没看到，告诉群里。

---

## ⏱️ 2. Day 2 上午：跑功能测试（1 小时）

### 2.1 打开 Postman + 导入集合

**步骤**：

1. 打开 Postman（桌面上找图标）
2. 顶部菜单 **File → Import**
3. 弹窗里点 **Upload Files**
4. 选文件：`D:\\code\\StarPicture\\docs\\test\\{folder}\\功能测试\\{functional_json}`
5. 看到 "1 file uploaded" → 点右下角 **Import**
6. 左侧栏应该出现 `{functional_collection_name}` 集合
7. 点开它，看到 10 个用例（5 个 {fp1} + 5 个 {fp2}）

### 2.2 配置环境变量

**步骤**：

1. Postman 右上角找到一个齿轮图标（Environments） → 点 **+** 创建
2. **Environment Name**：随便填，比如 `StarPicture本地`
3. 下面表格：
   - Variable 列填：`baseUrl`
   - Type 列保持 `default`
   - Initial Value 列填：`http://localhost:8123/api`
4. 点右下角 **Save**
5. **右上角下拉框** 选 `StarPicture本地`（让环境生效）

### 2.3 跑第 1 个用例：{tc1_first}

**步骤**：

1. 左侧集合展开 → 找到 **{tc1_first}** → 点开
2. 右侧中部会显示 `{first_method}  http://localhost:8123/api{first_path}`
3. {first_body_intro}
4. 右侧 **Send** 按钮（蓝色）→ 点一下
5. 期待右下角 Response 区域显示 {first_expected}

### 2.4 截图 ⭐（关键步骤！）

**步骤**：

1. **不要关闭 Postman**（窗口保持原样）
2. 按键盘 **`Win + Shift + S`**（Windows 自带的截图工具）
3. 选 **"窗口截图"**
4. 鼠标移到 Postman 窗口上，会自动框选整个 Postman
5. 点一下鼠标，截图就保存到剪贴板
6. 打开 **画图** 或 **Word**，按 `Ctrl + V` 粘贴
7. 另存为 PNG 到：`D:\\code\\StarPicture\\docs\\test\\{folder}\\功能测试\\{tc1_first}.png`

**验证**：打开保存的 PNG，应该能看到：URL、请求 Body 的 JSON/参数、右下角 Response 显示的 code。

### 2.5 跑剩下 9 个用例

按 2.3-2.4 的流程，**对剩下 9 个用例重复操作**：

| 用例 | 期望结果 |
|---|---|
{expected_table}

**每个用例截图 1 张，命名 `TC-XXX-NNN_标题.png`，存到 `功能测试/` 目录。**

10 张图都要截。**这一步约 30-45 分钟**。

---

## ⏱️ 3. Day 2 下午：跑性能测试（30 分钟）

### 3.1 打开 JMeter + 加载 .jmx

**步骤**：

1. 按 `Win + R`，输入 `cmd`，回车
2. 在黑窗口输入 `jmeter`，回车
3. JMeter 主窗口弹出（左侧是测试计划树）
4. 顶部菜单 **File → Open**
5. 选文件：`D:\\code\\StarPicture\\docs\\test\\{folder}\\性能测试\\{perf_jmx}`
6. 加载完，左侧会看到测试计划 → 线程组 → HTTP 请求 → HTTP Header Manager → Summary Report

### 3.2 修改线程数

**步骤**：

1. 左侧点 **线程组**
2. 右侧面板：
   - `Number of Threads (users)`：填 `{threads}`（{threads} 个并发用户）
   - `Ramp-up period (seconds)`：填 `5`（5 秒内启动）
   - `Loop Count`：填 `1`（只跑一轮）

### 3.3 修改目标地址

**步骤**：

1. 左侧点 **HTTP 请求**
2. 右侧面板：
   - `Server Name or IP`：`localhost`
   - `Port Number`：`8123`
   - `Method`：`{perf_method}`
   - `Path`：`{perf_path}`
3. 下方 Body Data 应该有：{perf_body}

### 3.4 添加 Summary Report

**步骤**：

1. 左侧点 **HTTP 请求**
2. 顶部菜单 **Edit → Add → Listener → Summary Report**
3. Summary Report 会出现在左侧底部

### 3.5 运行

**步骤**：

1. 顶部菜单 **Run → Start**（或点绿色 ▶ 按钮）
2. **观察右下角**：会显示 "{threads}/{threads}"
3. **等 10-30 秒**，跑到 "0/{threads}" 时跑完
4. 看 Summary Report 的结果：
   - `# Samples`：应该是 {threads}
   - `Average`：响应时间（毫秒），{perf_target}
   - `90% Line`：应 < {perf_p90}
   - `Error %`：应 < 5%
   - `Throughput`：吞吐量（请求/秒）

### 3.6 截图 ⭐

**步骤**：

1. 把 Summary Report 窗口拉大（能看清所有列）
2. 按 **`Win + Shift + S`** → 窗口截图 → 框选整个 JMeter 窗口
3. 粘贴到画图 → 另存为 PNG 到：`D:\\code\\StarPicture\\docs\\test\\{folder}\\性能测试\\{perf_summary_name}`

**验证**：图里应能看到 "{threads}"（样本数）、"0.0%" 或 "< 5%"（错误率）、{perf_expected_time}。

---

## ⏱️ 4. Day 2 下午：跑接口测试（15 分钟）

### 4.1 导入接口测试集合

**步骤**：

1. Postman File → Import → Upload Files
2. 选 `D:\\code\\StarPicture\\docs\\test\\{folder}\\接口测试\\{api_json}`
3. 左侧出现 `{api_collection_name}`

### 4.2 跑接口用例：{api_first_name}

**步骤**：

1. 左侧点开 `{api_collection_name}`
2. 找到 **{api_first_name}** → 点开
3. {api_first_steps}
4. 点 **Send**
5. 期待右下角 Response：{api_first_expected}

### 4.3 截图 ⭐

**步骤**：

1. 同 Day 2 功能测试的截图方法
2. 保存到：`D:\\code\\StarPicture\\docs\\test\\{folder}\\接口测试\\{api_first_name}.png`

---

## ⏱️ 5. Day 3 上午：跑安全测试（2 小时）

### 5.1 写安全报告 PDF

**步骤**：

1. 打开 **WPS 文字**（桌面找图标）
2. 顶部菜单 **文件 → 打开**
3. 选 `D:\\code\\StarPicture\\docs\\test\\{folder}\\安全测试\\{security_docx}`
4. 文档打开，找所有 `[待填]` 字样，**逐个替换为你的实际测试结果**
5. **至少填这些字段**：
   - 高危/中危/低危/建议 4 个等级的数量
   - 至少 3 条测试项的具体结论
   - 第 7 节"测试结论"的"整体评价"+"统计"
6. **保存为 PDF**：
   - 顶部菜单 **文件 → 导出为 PDF**
   - 文件名：`{sec_pdf_name}`
   - 保存到：`D:\\code\\StarPicture\\docs\\test\\{folder}\\安全测试\\`
   - 点完成

### 5.2 生成 .scan 文件

#### 方法 A：用 BurpSuite（推荐，免费）

**步骤**：

1. 双击桌面 "Burp Suite Community Edition" 打开
2. **临时项目** 启动 → 进入主界面
3. 左侧点 **Proxy** 标签
4. 顶部 **Intercept is on** → **点一下变成 off**（关闭拦截）
5. 安装浏览器代理扩展：
   - Chrome 访问 https://chromewebstore.google.com/detail/foxyproxy-standard/gcknhkkoolaabfmlnjonogaaifmjljia
   - 安装 FoxyProxy Standard
   - 装完浏览器右上角有狐狸图标 → 点开 → Options
   - 点 Add New Proxy：
     - Title: `BurpSuite`
     - Proxy Type: HTTP
     - Proxy IP: `127.0.0.1`
     - Port: `8080`
   - 点 Save
6. 在浏览器开新标签访问 `{sec_target_url}`
7. {sec_steps}
8. 后端应该返回 `{sec_expected_code}`，证明 {sec_proof} 被防住了
9. **保存 .scan 文件**：
   - 切到 BurpSuite → **Target** 标签
   - 左侧 Site map 应该出现 `localhost:8123`
   - 在 Site map 里点开 → 找到 `{sec_endpoint}`
   - 右键 → **Save item**
   - 选格式 **`xxx.scan`**
   - 保存到：`D:\\code\\StarPicture\\docs\\test\\{folder}\\安全测试\\{scan_name}`

#### 方法 B：装不上 BurpSuite？

写一个 Markdown 改名为 .scan.md 也行（应急用）：

1. 打开记事本
2. 输入：
   ```markdown
   # {module_short}模块安全扫描记录

   - 扫描时间：2026-06-19
   - 扫描工具：手动测试（BurpSuite 装不上）
   - 测试项：
     1. {sec_name}：{sec_test_desc}，预期 {sec_expected_code}，实际通过
   ```
3. 另存为 `{scan_md_name}`

---

## ⏱️ 6. Day 3 下午：最后检查（30 分钟）

打开你的文件夹，确认：

```
{folder}/
├── 功能测试/                        ← 应该有 10 张 png
├── 性能测试/                        ← 应该有 1 张 png
├── 接口测试/                        ← 应该有 1 张 png
├── 安全测试/                        ← 应该有 PDF + .scan（或 .scan.md）
├── README.md
├── TUTORIAL_保姆级教程.md
└── 软件测试测试用例.xlsx
```

**截图清单**（你应该有 12 张）：

| 文件名 |
|---|
{file_checklist}

**群里汇报**：把 12 张图、1 个 PDF、1 个 .scan 准备好后，告诉群里"我的搞定了"。

---

## 🆘 出问题找谁

| 问题 | 找谁 |
|---|---|
| 后端起不来 / 接口不通 | 林景彬 |
| 不知道某步怎么点 | 群里截图问 |
| BurpSuite 装不上 | 用方法 B（写 .scan.md） |
| JMeter 报错 | 把错误截图发群里 |

---

**教程作者**：朱远亮（组长）
**最后更新**：2026-06-19
"""

MEMBERS = [
    {
        "name": "李冠燃",
        "student_id": "2310820063",
        "folder": "李冠燃_脚本与截图",
        "module_desc": "picture（图片模块）",
        "module_short": "图片",
        "functional_json": "picture_functional.postman_collection.json",
        "functional_collection_name": "StarPicture_图片模块_功能测试",
        "fp1": "图片本地上传",
        "fp2": "关键字搜索",
        "tc1": "TC-PU-001_本地上传_jpg",
        "tc2": "TC-PX-005_搜图_未登录",
        "tc1_first": "TC-PU-001_本地上传_jpg",
        "first_method": "POST",
        "first_path": "/file/upload",
        "first_body_intro": "中间偏下选 **Body** 标签 → 选 **form-data** → key 填 `file`（不要填 file，要选 File 类型）→ 选一张 jpg 图（2MB 以内）",
        "first_expected": "Status: `200 OK`, Body 含 `code: 0` 和 `url`",
        "expected_table": """| TC-PU-002 本地上传 png | code=0, picFormat=png |
| TC-PU-003 超过 2MB | code=40001 提示文件过大 |
| TC-PU-004 非图片 | code=40001 提示格式错误 |
| TC-PU-005 空文件 | code=40001 提示文件为空 |
| TC-PX-001 搜图_有结果 | code=0, 返回含'猫'的图片列表 |
| TC-PX-002 搜图_无结果 | 返回空列表 |
| TC-PX-003 搜图_空文本 | code=40001 |
| TC-PX-004 搜图_超长 | code=40001 |
| TC-PX-005 搜图_未登录 | code=40100 |""",
        "perf_jmx": "picture_upload_20concurrent.jmx",
        "threads": "20",
        "perf_method": "POST",
        "perf_path": "/file/upload",
        "perf_body": "form-data 上传一张 2MB 的 jpg 图",
        "perf_target": "应 < 3000ms（3 秒）",
        "perf_p90": "5000ms（5 秒）",
        "perf_expected_time": "3000ms 左右（上传 2MB 图）",
        "perf_summary_name": "summary_report_上传20并发.png",
        "api_json": "picture_api.postman_collection.json",
        "api_collection_name": "StarPicture_图片模块_接口测试",
        "api_first_name": "API-001_上传_缺multipart边界",
        "api_first_steps": "右侧中部：\n   - 选 **Headers** 标签 → 双击 `Content-Type` 值改成 `multipart/form-data; boundary=xxx`（不完整 boundary）\n   - 切到 **Body** 标签 → 选 **raw**\n   - 写一些乱码或不完整 multipart 内容",
        "api_first_expected": "Status: `400` 或 `415`",
        "security_docx": "图片模块-安全报告模板.docx",
        "sec_pdf_name": "图片模块-安全报告.pdf",
        "scan_name": "图片模块.scan",
        "scan_md_name": "图片模块.scan.md",
        "sec_target_url": "http://localhost:8123/api/file/upload",
        "sec_steps": "试上传 `1.jpg`（文件名是 .jpg，但**内容是 `<?php system($_GET['c']); ?>`**，记事本写然后保存为 1.jpg 即可）",
        "sec_expected_code": "code=40001",
        "sec_proof": "PHP 木马上传",
        "sec_endpoint": "/api/file/upload",
        "sec_name": "伪 PHP 木马上传",
        "sec_test_desc": "上传 1.jpg（内容是 PHP 木马），预期 code=40001",
        "file_checklist": """| 功能测试/TC-PU-001_本地上传_jpg.png |
| 功能测试/TC-PU-002_本地上传_png.png |
| 功能测试/TC-PU-003_超过2MB.png |
| 功能测试/TC-PU-004_非图片.png |
| 功能测试/TC-PU-005_空文件.png |
| 功能测试/TC-PX-001_搜图_有结果.png |
| 功能测试/TC-PX-002_搜图_无结果.png |
| 功能测试/TC-PX-003_搜图_空文本.png |
| 功能测试/TC-PX-004_搜图_超长.png |
| 功能测试/TC-PX-005_搜图_未登录.png |
| 性能测试/summary_report_上传20并发.png |
| 接口测试/API-001_上传_缺multipart边界.png |""",
    },
    {
        "name": "李坤纬",
        "student_id": "2310820043",
        "folder": "李坤纬_脚本与截图",
        "module_desc": "space（空间模块）",
        "module_short": "空间",
        "functional_json": "space_functional.postman_collection.json",
        "functional_collection_name": "StarPicture_空间模块_功能测试",
        "fp1": "空间创建",
        "fp2": "空间成员管理",
        "tc1": "TC-SP-002_创建空间_普通版",
        "tc2": "TC-SU-005_删除空间成员",
        "tc1_first": "TC-SP-001_获取空间等级列表",
        "first_method": "GET",
        "first_path": "/space/list/level",
        "first_body_intro": "中间 Body 标签保持空（GET 没 body）",
        "first_expected": "Status: `200 OK`, Body: `{\"code\": 0, \"data\": [\"普通版\", \"专业版\", \"旗舰版\"], \"message\": \"ok\"}`",
        "expected_table": """| TC-SP-002 创建空间_普通版 | code=0, spaceId 存在 |
| TC-SP-003 创建空间_名称为空 | code=40001 |
| TC-SP-004 创建空间_名称超长 | code=40001 |
| TC-SP-005 创建空间_未登录 | code=40100 |
| TC-SU-001 添加空间成员 | code=0 |
| TC-SU-002 查询空间成员 | 返回成员列表 |
| TC-SU-003 添加成员_重复 | code=40001 |
| TC-SU-004 添加成员_未登录 | code=40100 |
| TC-SU-005 删除空间成员 | code=0 |""",
        "perf_jmx": "space_analyze_20concurrent.jmx",
        "threads": "20",
        "perf_method": "POST",
        "perf_path": "/space/analyze/usage",
        "perf_body": '{"spaceId": 1}',
        "perf_target": "应 < 1000ms（1 秒）",
        "perf_p90": "2000ms（2 秒）",
        "perf_expected_time": "1000ms 左右",
        "perf_summary_name": "summary_report_空间分析20并发.png",
        "api_json": "space_api.postman_collection.json",
        "api_collection_name": "StarPicture_空间模块_接口测试",
        "api_first_name": "API-001_创建空间_缺Content-Type",
        "api_first_steps": "右侧中部：\n   - 选 **Headers** 标签 → 把 `Content-Type` 这一行删掉（点 ×）\n   - 切到 **Body** 标签 → 选 **raw** → 选 **JSON**\n   - 写：`{\"spaceName\": \"test\", \"spaceLevel\": 0}`",
        "api_first_expected": "Status: `415` 或服务端能解析",
        "security_docx": "空间模块-安全报告模板.docx",
        "sec_pdf_name": "空间模块-安全报告.pdf",
        "scan_name": "空间模块.scan",
        "scan_md_name": "空间模块.scan.md",
        "sec_target_url": "http://localhost:8123/api/space/add",
        "sec_steps": "在 Body 写：`{\"spaceName\": \"<script>alert(1)</script>\", \"spaceLevel\": 0}`",
        "sec_expected_code": "code=0（但渲染时被转义，不弹窗）",
        "sec_proof": "XSS 注入",
        "sec_endpoint": "/api/space/add",
        "sec_name": "空间名称 XSS 注入",
        "sec_test_desc": "spaceName 输入 `<script>alert(1)</script>`，预期渲染时被转义",
        "file_checklist": """| 功能测试/TC-SP-001_获取空间等级列表.png |
| 功能测试/TC-SP-002_创建空间_普通版.png |
| 功能测试/TC-SP-003_创建空间_名称为空.png |
| 功能测试/TC-SP-004_创建空间_名称超长.png |
| 功能测试/TC-SP-005_创建空间_未登录.png |
| 功能测试/TC-SU-001_添加空间成员.png |
| 功能测试/TC-SU-002_查询空间成员.png |
| 功能测试/TC-SU-003_添加成员_重复.png |
| 功能测试/TC-SU-004_添加成员_未登录.png |
| 功能测试/TC-SU-005_删除空间成员.png |
| 性能测试/summary_report_空间分析20并发.png |
| 接口测试/API-001_创建空间_缺Content-Type.png |""",
    },
    {
        "name": "林景彬",
        "student_id": "2310820051",
        "folder": "林景彬_脚本与截图",
        "module_desc": "file + wxMp（文件+公众号）",
        "module_short": "文件",
        "functional_json": "file_wxmp_functional.postman_collection.json",
        "functional_collection_name": "StarPicture_文件_公众号模块_功能测试",
        "fp1": "文件本地上传",
        "fp2": "微信公众号门户",
        "tc1": "TC-FL-001_本地上传_jpg_2MB",
        "tc2": "TC-WX-005_创建菜单_未登录",
        "tc1_first": "TC-FL-001_本地上传_jpg_2MB",
        "first_method": "POST",
        "first_path": "/file/upload",
        "first_body_intro": "中间选 **Body** → **form-data** → key 填 `file`（File 类型）→ 选一张 jpg 图（2MB 以内）",
        "first_expected": "Status: `200 OK`, Body 含 `code: 0` 和 `url`",
        "expected_table": """| TC-FL-002 超过2MB | code=40001 |
| TC-FL-003 空文件 | code=40001 |
| TC-FL-004 非图片 | code=40001 |
| TC-FL-005 未登录 | code=40100 |
| TC-WX-001 门户_GET_签名 | 返回 echostr 明文 |
| TC-WX-002 门户_POST_XML | code=0, 响应 XML |
| TC-WX-003 创建菜单 | code=0 |
| TC-WX-004 创建菜单_空body | code=40001 |
| TC-WX-005 创建菜单_未登录 | code=40100 |""",
        "perf_jmx": "file_upload_50concurrent_1MB.jmx",
        "threads": "50",
        "perf_method": "POST",
        "perf_path": "/file/upload",
        "perf_body": "form-data 上传一张 1MB 的 jpg 图",
        "perf_target": "应 < 2000ms（2 秒）",
        "perf_p90": "4000ms（4 秒）",
        "perf_expected_time": "2000ms 左右",
        "perf_summary_name": "summary_report_文件上传50并发.png",
        "api_json": "file_wxmp_api.postman_collection.json",
        "api_collection_name": "StarPicture_文件_公众号模块_接口测试",
        "api_first_name": "API-001_upload_缺multipart边界",
        "api_first_steps": "右侧中部：\n   - 选 **Headers** → 把 Content-Type 改成 `application/xml`（错的）\n   - 切到 **Body** → raw → XML → 写乱码",
        "api_first_expected": "Status: `415` 或 `400`",
        "security_docx": "文件_公众号模块-安全报告模板.docx",
        "sec_pdf_name": "文件_公众号模块-安全报告.pdf",
        "scan_name": "文件_公众号模块.scan",
        "scan_md_name": "文件_公众号模块.scan.md",
        "sec_target_url": "http://localhost:8123/api/file/upload",
        "sec_steps": "试上传 `1.jpg`（文件名是 .jpg，但**内容是 `<?php system($_GET['c']); ?>`**，记事本写然后保存为 1.jpg 即可）",
        "sec_expected_code": "code=40001",
        "sec_proof": "PHP 木马上传",
        "sec_endpoint": "/api/file/upload",
        "sec_name": "伪 PHP 木马上传",
        "sec_test_desc": "上传 1.jpg（内容是 PHP 木马），预期 code=40001",
        "file_checklist": """| 功能测试/TC-FL-001_本地上传_jpg_2MB.png |
| 功能测试/TC-FL-002_超过2MB.png |
| 功能测试/TC-FL-003_空文件.png |
| 功能测试/TC-FL-004_非图片.png |
| 功能测试/TC-FL-005_未登录.png |
| 功能测试/TC-WX-001_门户_GET_签名.png |
| 功能测试/TC-WX-002_门户_POST_XML.png |
| 功能测试/TC-WX-003_创建菜单.png |
| 功能测试/TC-WX-004_创建菜单_空body.png |
| 功能测试/TC-WX-005_创建菜单_未登录.png |
| 性能测试/summary_report_文件上传50并发.png |
| 接口测试/API-001_upload_缺multipart边界.png |""",
    },
]

for m in MEMBERS:
    target = BASE / f"{m['folder']}/TUTORIAL_保姆级教程.md"
    target.write_text(TEMPLATE.format(**m), encoding='utf-8')
    print(f"已生成: {target}")

print(f"\n共 {len(MEMBERS)} 份保姆级教程。")