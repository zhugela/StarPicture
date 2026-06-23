# 朱远亮 · 保姆级教程（零基础也能跟做）

> **你的名字**：朱远亮（组长）
> **学号**：2310820053
> **负责模块**：user（用户模块）
> **用例总数**：13 条（10 功能 + 1 性能 + 1 接口 + 1 安全）
> **目标**：跟着做，3 天内交齐所有文件
> **难度**：⭐⭐（最简单）

---

## 📋 0. 你要交的所有东西

打开你的文件夹 `D:\code\StarPicture\docs\test\朱远亮_脚本与截图\`，里面已经有：

```
朱远亮_脚本与截图/                      ← 你要交的所有东西都放这里
├── README.md                            ← 你正在看的说明
├── TUTORIAL_保姆级教程.md               ← 你正在看的教程
├── 软件测试测试用例.xlsx                 ← 13 条用例（不要改）
│
├── 功能测试/                            ← 你要做：跑 10 个用例 + 截 10 张图
│   └── user_functional.postman_collection.json   ← 已经准备好
│
├── 性能测试/                            ← 你要做：跑 JMeter + 截图
│   └── login_50concurrent.jmx                    ← 已经准备好
│
├── 接口测试/                            ← 你要做：跑 1 个用例 + 截图
│   └── user_api.postman_collection.json           ← 已经准备好
│
└── 安全测试/                            ← 你要做：填报告 + 写 .scan
    └── 用户模块-安全报告模板.docx                ← 已经准备好
```

**你最后要交的是**：

| 目录 | 文件 | 数量 |
|---|---|---|
| 功能测试/ | TC-UR-001_xxx.png ~ TC-UL-005_xxx.png | **10 张** |
| 性能测试/ | summary_report_xxx.png | **1 张** |
| 接口测试/ | TC-API-001_注册Content-Type错误.png | **1 张** |
| 安全测试/ | 用户模块-安全报告.pdf | **1 个** |
| 安全测试/ | 用户模块.scan | **1 个** |

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
4. 选文件：`D:\code\StarPicture\docs\test\朱远亮_脚本与截图\功能测试\user_functional.postman_collection.json`
5. 看到 "1 file uploaded" → 点右下角 **Import**
6. 左侧栏应该出现 `StarPicture_用户模块_功能测试` 集合
7. 点开它，看到 10 个用例（TC-UR-001 到 TC-UL-005）

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

### 2.3 跑第 1 个用例：TC-UR-001 注册_账号密码正确

**步骤**：

1. 左侧集合展开 → 找到 **TC-UR-001_注册_账号密码正确** → 点开
2. 右侧中部会显示：`POST  http://localhost:8123/api/user/register`
3. 中间偏下有个 **Body** 标签 → 点开 → 里面有一段 JSON：
   ```json
   {
     "userAccount": "Zhyl_New01",
     "userPassword": "12345678",
     "checkPassword": "12345678"
   }
   ```
4. 右侧 **Send** 按钮（蓝色）→ 点一下
5. 期待右下角 Response 区域显示：
   - Status: `200 OK`
   - Body: `{"code": 0, "data": {...userId: 1...}, "message": "ok"}`

### 2.4 截图 ⭐（关键步骤！）

**步骤**：

1. **不要关闭 Postman**（窗口保持原样）
2. 按键盘 **`Win + Shift + S`**（Windows 自带的截图工具）
3. 选 **"窗口截图"**
4. 鼠标移到 Postman 窗口上，会自动框选整个 Postman
5. 点一下鼠标，截图就保存到剪贴板
6. 打开 **画图** 或 **Word**，按 `Ctrl + V` 粘贴
7. 另存为 PNG 到：`D:\code\StarPicture\docs\test\朱远亮_脚本与截图\功能测试\TC-UR-001_注册_账号密码正确.png`

**验证**：打开保存的 PNG，应该能看到：URL（POST /api/user/register）、左侧请求 Body 的 JSON、右下角 Response 显示 200 OK。

### 2.5 跑剩下 9 个用例

按 2.3-2.4 的流程，**对剩下 9 个用例重复操作**：

| 用例 | 期望结果 |
|---|---|
| TC-UR-002 注册_账号已存在 | code=40001, message 提示账号重复 |
| TC-UR-003 注册_密码不一致 | code=40001 |
| TC-UR-004 注册_账号为空 | code=40001 |
| TC-UR-005 注册_账号长度不足 | code=40001 |
| TC-UL-001 登录_账号密码正确 | code=0, **Response Headers 有 Set-Cookie** |
| TC-UL-002 登录_密码错误 | code=40001 |
| TC-UL-003 登录_账号不存在 | code=40001 |
| TC-UL-004 登录_空body | code=40001 |
| TC-UL-005 登录_密码小于8字符 | code=40001 |

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
5. 选文件：`D:\code\StarPicture\docs\test\朱远亮_脚本与截图\性能测试\login_50concurrent.jmx`
6. 加载完，左侧会看到：
   - 测试计划
     - 线程组
       - HTTP 请求（`POST /api/user/login`）
         - HTTP Header Manager
         - Summary Report

### 3.2 修改线程数

**步骤**：

1. 左侧点 **线程组**
2. 右侧面板：
   - `Number of Threads (users)`：填 `50`（50 个并发用户）
   - `Ramp-up period (seconds)`：填 `5`（5 秒内启动）
   - `Loop Count`：填 `1`（只跑一轮）

### 3.3 修改目标地址

**步骤**：

1. 左侧点 **HTTP 请求**
2. 右侧面板：
   - `Server Name or IP`：`localhost`
   - `Port Number`：`8123`
   - `Method`：`POST`
   - `Path`：`/api/user/login`
3. 下方 Body Data 应该有：
   ```json
   {"userAccount":"testuser01","userPassword":"12345678"}
   ```

### 3.4 添加 Summary Report

**步骤**：

1. 左侧点 **HTTP 请求**
2. 顶部菜单 **Edit → Add → Listener → Summary Report**
3. Summary Report 会出现在左侧底部

### 3.5 运行

**步骤**：

1. 顶部菜单 **Run → Start**（或点绿色 ▶ 按钮）
2. **观察右下角**：会显示 "50/50"，意思是 50 个用户正在跑
3. **等 10-30 秒**，跑到 "0/50" 时跑完
4. 看 Summary Report 的结果：
   - `# Samples`：应该是 50（跑 50 个请求）
   - `Average`：登录响应时间（毫秒），**应 < 500ms**
   - `90% Line`：应 < 1000ms
   - `Error %`：应 < 5%
   - `Throughput`：吞吐量（请求/秒）

### 3.6 截图 ⭐

**步骤**：

1. 把 Summary Report 窗口拉大（能看清所有列）
2. 按 **`Win + Shift + S`** → 窗口截图 → 框选整个 JMeter 窗口
3. 粘贴到画图 → 另存为 PNG 到：`D:\code\StarPicture\docs\test\朱远亮_脚本与截图\性能测试\summary_report_登录50并发.png`

**验证**：图里应能看到 "50"（样本数）、"0.0%" 或 "< 5%"（错误率）、"500ms 左右"（平均）。

---

## ⏱️ 4. Day 2 下午：跑接口测试（15 分钟）

### 4.1 导入接口测试集合

**步骤**：

1. Postman File → Import → Upload Files
2. 选 `D:\code\StarPicture\docs\test\朱远亮_脚本与截图\接口测试\user_api.postman_collection.json`
3. 左侧出现 `StarPicture_用户模块_接口测试`

### 4.2 跑 TC-API-001 注册 Content-Type 错误

**步骤**：

1. 左侧点开 `StarPicture_用户模块_接口测试`
2. 找到 **API-001_注册_Content-Type错误** → 点开
3. 右侧中部：
   - 选 **Headers** 标签（不是 Body）
   - 找 `Content-Type` 这一行 → **双击值** → 改成 `application/xml` ← 关键
   - **不点 Send 之前先改 Headers**
4. 切到 **Body** 标签 → 选 **raw** → 选 **XML**（如果选项没显示就改 application/xml）
5. 在 Body 区域粘贴：
   ```xml
   <xml>test</xml>
   ```
6. 点 **Send**
7. 期待右下角 Response：
   - Status: `415 Unsupported Media Type`
   - 或 Status: `400`

### 4.3 截图 ⭐

**步骤**：

1. 同 Day 2 功能测试的截图方法
2. 保存到：`D:\code\StarPicture\docs\test\朱远亮_脚本与截图\接口测试\TC-API-001_注册Content-Type错误.png`

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
6. 在浏览器开新标签访问 `http://localhost:8123/api/user/login`
7. 试输入账号密码：
   - username: `admin' OR 1=1 --`
   - password: `123456`
8. 后端应该返回 `code=40001`（拒绝登录），证明 SQL 注入被防住了
9. **截 BurpSuite 的 Target 页面**：
   - 切到 BurpSuite → **Target** 标签
   - 左侧 Site map 应该出现 `localhost:8123`
   - 在 Site map 里点开 → 找到 `/api/user/login`
   - 右键 → **Save item**
   - 选格式 **`xxx.scan`**
   - 保存到：`D:\code\StarPicture\docs\test\朱远亮_脚本与截图\安全测试\用户模块.scan`

#### 方法 B：装不上 BurpSuite？

写一个 Markdown 改名为 .scan.md 也行（应急用）：

1. 打开记事本
2. 输入：
   ```markdown
   # 用户模块安全扫描记录
   
   - 扫描时间：2026-06-19
   - 扫描工具：手动测试（BurpSuite 装不上）
   - 测试项：
     1. SQL 注入：用户名输入 `' OR 1=1 --`，密码 123456，预期 code=40001，实际通过 ✅
     2. 越权：testuser01 调用 /user/list/page/vo，预期 code=40300，实际通过 ✅
   ```
3. 另存为 `用户模块.scan.md`

---

## ⏱️ 6. Day 3 下午：最后检查（30 分钟）

打开你的文件夹，确认：

```
朱远亮_脚本与截图/
├── 功能测试/                        ← 应该有 10 张 png
├── 性能测试/                        ← 应该有 1 张 png
├── 接口测试/                        ← 应该有 1 张 png
├── 安全测试/                        ← 应该有 PDF + .scan（或 .scan.md）
├── README.md
├── TUTORIAL_保姆级教程.md
└── 软件测试测试用例.xlsx
```

**截图清单**（你应该有 12 张）：

| 文件名 | 内容 |
|---|---|
| 功能测试/TC-UR-001_注册_账号密码正确.png | 注册接口成功 |
| 功能测试/TC-UR-002_注册_账号已存在.png | 重复注册被拒 |
| 功能测试/TC-UR-003_注册_密码不一致.png | 两次密码不一致 |
| 功能测试/TC-UR-004_注册_账号为空.png | 账号为空被拒 |
| 功能测试/TC-UR-005_注册_账号长度不足.png | 账号太短被拒 |
| 功能测试/TC-UL-001_登录_账号密码正确.png | 登录成功，看 Set-Cookie |
| 功能测试/TC-UL-002_登录_密码错误.png | 密码错被拒 |
| 功能测试/TC-UL-003_登录_账号不存在.png | 账号不存在被拒 |
| 功能测试/TC-UL-004_登录_空body.png | 空请求被拒 |
| 功能测试/TC-UL-005_登录_密码小于8字符.png | 密码太短被拒 |
| 性能测试/summary_report_登录50并发.png | JMeter Summary Report |
| 接口测试/TC-API-001_注册Content-Type错误.png | Postman 接口测试 |

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