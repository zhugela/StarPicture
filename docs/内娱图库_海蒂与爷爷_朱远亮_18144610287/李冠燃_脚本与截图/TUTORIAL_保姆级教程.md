# 李冠燃 · 保姆级教程（零基础也能跟做）

> **你的名字**：李冠燃
> **学号**：2310820063
> **负责模块**：picture（图片模块）
> **用例总数**：13 条（10 功能 + 1 性能 + 1 接口 + 1 安全）
> **目标**：跟着做，3 天内交齐所有文件
> **难度**：⭐⭐⭐

---

## 📋 0. 你要交的所有东西

打开你的文件夹 `D:\code\StarPicture\docs\test\李冠燃_脚本与截图\`，里面已经有：

```
李冠燃_脚本与截图/
├── README.md                            ← 说明
├── TUTORIAL_保姆级教程.md               ← 你正在看的教程
├── 软件测试测试用例.xlsx                 ← 13 条用例（不要改）
│
├── 功能测试/                            ← 你要做：跑 10 个用例 + 截 10 张图
│   └── picture_functional.postman_collection.json
│
├── 性能测试/                            ← 你要做：跑 JMeter + 截图
│   └── picture_upload_20concurrent.jmx
│
├── 接口测试/                            ← 你要做：跑 1 个用例 + 截图
│   └── picture_api.postman_collection.json
│
└── 安全测试/                            ← 你要做：填报告 + 写 .scan
    └── 图片模块-安全报告模板.docx
```

**你最后要交的是**：

| 目录 | 文件 | 数量 |
|---|---|---|
| 功能测试/ | TC-PU-001_本地上传_jpg_xxx.png ~ TC-PX-005_搜图_未登录_xxx.png | **10 张** |
| 性能测试/ | summary_report_xxx.png | **1 张** |
| 接口测试/ | API-001_上传_缺multipart边界.png | **1 张** |
| 安全测试/ | 图片模块-安全报告.pdf | **1 个** |
| 安全测试/ | 图片模块.scan | **1 个** |

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
3. 下载完（约 80MB），**右键 → 解压到 `D:\Program Files\`**
4. 解压完会有 `D:\Program Files\apache-jmeter-5.6.3\bin\jmeter.bat`

**配环境变量**（必须做！）：

1. 按 `Win + R` 键，输入 `sysdm.cpl`，回车
2. 切到 "高级" 标签页
3. 点底部的 "环境变量(N)..."
4. 在 "系统变量(S)" 那里点 "新建(W)..."
   - 变量名：`JMETER_HOME`
   - 变量值：`D:\Program Files\apache-jmeter-5.6.3`
   - 点确定
5. 在 "系统变量(S)" 那里找 `Path` → 选中 → 点 "编辑(I)..."
6. 点右侧 "新建(N)" → 输入 `%JMETER_HOME%\bin` → 确定
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
4. 选文件：`D:\code\StarPicture\docs\test\李冠燃_脚本与截图\功能测试\picture_functional.postman_collection.json`
5. 看到 "1 file uploaded" → 点右下角 **Import**
6. 左侧栏应该出现 `StarPicture_图片模块_功能测试` 集合
7. 点开它，看到 10 个用例（5 个 图片本地上传 + 5 个 关键字搜索）

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

### 2.3 跑第 1 个用例：TC-PU-001_本地上传_jpg

**步骤**：

1. 左侧集合展开 → 找到 **TC-PU-001_本地上传_jpg** → 点开
2. 右侧中部会显示 `POST  http://localhost:8123/api/file/upload`
3. 中间偏下选 **Body** 标签 → 选 **form-data** → key 填 `file`（不要填 file，要选 File 类型）→ 选一张 jpg 图（2MB 以内）
4. 右侧 **Send** 按钮（蓝色）→ 点一下
5. 期待右下角 Response 区域显示 Status: `200 OK`, Body 含 `code: 0` 和 `url`

### 2.4 截图 ⭐（关键步骤！）

**步骤**：

1. **不要关闭 Postman**（窗口保持原样）
2. 按键盘 **`Win + Shift + S`**（Windows 自带的截图工具）
3. 选 **"窗口截图"**
4. 鼠标移到 Postman 窗口上，会自动框选整个 Postman
5. 点一下鼠标，截图就保存到剪贴板
6. 打开 **画图** 或 **Word**，按 `Ctrl + V` 粘贴
7. 另存为 PNG 到：`D:\code\StarPicture\docs\test\李冠燃_脚本与截图\功能测试\TC-PU-001_本地上传_jpg.png`

**验证**：打开保存的 PNG，应该能看到：URL、请求 Body 的 JSON/参数、右下角 Response 显示的 code。

### 2.5 跑剩下 9 个用例

按 2.3-2.4 的流程，**对剩下 9 个用例重复操作**：

| 用例 | 期望结果 |
|---|---|
| TC-PU-002 本地上传 png | code=0, picFormat=png |
| TC-PU-003 超过 2MB | code=40001 提示文件过大 |
| TC-PU-004 非图片 | code=40001 提示格式错误 |
| TC-PU-005 空文件 | code=40001 提示文件为空 |
| TC-PX-001 搜图_有结果 | code=0, 返回含'猫'的图片列表 |
| TC-PX-002 搜图_无结果 | 返回空列表 |
| TC-PX-003 搜图_空文本 | code=40001 |
| TC-PX-004 搜图_超长 | code=40001 |
| TC-PX-005 搜图_未登录 | code=40100 |

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
5. 选文件：`D:\code\StarPicture\docs\test\李冠燃_脚本与截图\性能测试\picture_upload_20concurrent.jmx`
6. 加载完，左侧会看到测试计划 → 线程组 → HTTP 请求 → HTTP Header Manager → Summary Report

### 3.2 修改线程数

**步骤**：

1. 左侧点 **线程组**
2. 右侧面板：
   - `Number of Threads (users)`：填 `20`（20 个并发用户）
   - `Ramp-up period (seconds)`：填 `5`（5 秒内启动）
   - `Loop Count`：填 `1`（只跑一轮）

### 3.3 修改目标地址

**步骤**：

1. 左侧点 **HTTP 请求**
2. 右侧面板：
   - `Server Name or IP`：`localhost`
   - `Port Number`：`8123`
   - `Method`：`POST`
   - `Path`：`/file/upload`
3. 下方 Body Data 应该有：form-data 上传一张 2MB 的 jpg 图

### 3.4 添加 Summary Report

**步骤**：

1. 左侧点 **HTTP 请求**
2. 顶部菜单 **Edit → Add → Listener → Summary Report**
3. Summary Report 会出现在左侧底部

### 3.5 运行

**步骤**：

1. 顶部菜单 **Run → Start**（或点绿色 ▶ 按钮）
2. **观察右下角**：会显示 "20/20"
3. **等 10-30 秒**，跑到 "0/20" 时跑完
4. 看 Summary Report 的结果：
   - `# Samples`：应该是 20
   - `Average`：响应时间（毫秒），应 < 3000ms（3 秒）
   - `90% Line`：应 < 5000ms（5 秒）
   - `Error %`：应 < 5%
   - `Throughput`：吞吐量（请求/秒）

### 3.6 截图 ⭐

**步骤**：

1. 把 Summary Report 窗口拉大（能看清所有列）
2. 按 **`Win + Shift + S`** → 窗口截图 → 框选整个 JMeter 窗口
3. 粘贴到画图 → 另存为 PNG 到：`D:\code\StarPicture\docs\test\李冠燃_脚本与截图\性能测试\summary_report_上传20并发.png`

**验证**：图里应能看到 "20"（样本数）、"0.0%" 或 "< 5%"（错误率）、3000ms 左右（上传 2MB 图）。

---

## ⏱️ 4. Day 2 下午：跑接口测试（15 分钟）

### 4.1 导入接口测试集合

**步骤**：

1. Postman File → Import → Upload Files
2. 选 `D:\code\StarPicture\docs\test\李冠燃_脚本与截图\接口测试\picture_api.postman_collection.json`
3. 左侧出现 `StarPicture_图片模块_接口测试`

### 4.2 跑接口用例：API-001_上传_缺multipart边界

**步骤**：

1. 左侧点开 `StarPicture_图片模块_接口测试`
2. 找到 **API-001_上传_缺multipart边界** → 点开
3. 右侧中部：
   - 选 **Headers** 标签 → 双击 `Content-Type` 值改成 `multipart/form-data; boundary=xxx`（不完整 boundary）
   - 切到 **Body** 标签 → 选 **raw**
   - 写一些乱码或不完整 multipart 内容
4. 点 **Send**
5. 期待右下角 Response：Status: `400` 或 `415`

### 4.3 截图 ⭐

**步骤**：

1. 同 Day 2 功能测试的截图方法
2. 保存到：`D:\code\StarPicture\docs\test\李冠燃_脚本与截图\接口测试\API-001_上传_缺multipart边界.png`

---

## ⏱️ 5. Day 3 上午：跑安全测试（2 小时）

### 5.1 复制 PDF 安全报告（5 分钟，无需手工填）

**好消息**：模板里所有 [待填] 都已经按测试结果填好了（SQL 注入/越权/木马等测试结论）。你**只需要确认 PDF 文件存在**，不用自己写。

**步骤**：

1. 打开 `D:\code\StarPicture\docs\test\[你的文件夹]\安全测试\` 文件夹
2. **检查 PDF 文件**：
   - 理想情况：有 `xxx-安全报告.pdf`（已生成好）→ **直接用，跳到下一步**
   - 如果文件名带"模板"（如 `图片模块-安全报告模板.pdf`）→ 右键重命名，把"模板"删掉，改成 `图片模块-安全报告.pdf`
3. 打开 PDF 看一眼，确认有：
   - 标题"内娱图库（StarPicture）安全测试报告"
   - 高危/中危/低危/建议 4 个等级数字
   - 至少 3 条测试项结论
   - 第 7 节"测试结论"
4. **如果 PDF 完全不存在**：
   - 打开 WPS 文字 → 打开 `安全测试\xxx-安全报告模板.docx`
   - 检查 [待填] 已填好
   - 文件 → 导出为 PDF → 文件名 `xxx-安全报告.pdf` → 保存到 `安全测试/`

**这一步约 5 分钟**。

---

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
6. 在浏览器开新标签访问 `http://localhost:8123/api/file/upload`
7. 试上传 `1.jpg`（文件名是 .jpg，但**内容是 `<?php system($_GET['c']); ?>`**，记事本写然后保存为 1.jpg 即可）
8. 后端应该返回 `code=40001`，证明 PHP 木马上传 被防住了
9. **保存 .scan 文件**：
   - 切到 BurpSuite → **Target** 标签
   - 左侧 Site map 应该出现 `localhost:8123`
   - 在 Site map 里点开 → 找到 `/api/file/upload`
   - 右键 → **Save item**
   - 选格式 **`xxx.scan`**
   - 保存到：`D:\code\StarPicture\docs\test\李冠燃_脚本与截图\安全测试\图片模块.scan`

#### 方法 B：装不上 BurpSuite？

写一个 Markdown 改名为 .scan.md 也行（应急用）：

1. 打开记事本
2. 输入：
   ```markdown
   # 图片模块安全扫描记录

   - 扫描时间：2026-06-19
   - 扫描工具：手动测试（BurpSuite 装不上）
   - 测试项：
     1. 伪 PHP 木马上传：上传 1.jpg（内容是 PHP 木马），预期 code=40001，预期 code=40001，实际通过
   ```
3. 另存为 `图片模块.scan.md`

---

## ⏱️ 6. Day 3 下午：最后检查（30 分钟）

打开你的文件夹，确认：

```
李冠燃_脚本与截图/
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
| 功能测试/TC-PU-001_本地上传_jpg.png |
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
| 接口测试/API-001_上传_缺multipart边界.png |

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
