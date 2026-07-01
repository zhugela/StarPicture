# StarPicture 软件测试计划

**文档编号**：StarPicture-CS001  
**版本**：V1.0  
**项目名称**：内娱图库（StarPicture）  
**编写**：朱远亮  
**审核**：李冠燃、李坤纬、林景彬  
**批准**：朱远亮  

---

## 一、小组项目分工及完成情况

| 学号 | 姓名 | 专业 | 主要工作 |
|---|---|---|---|
| 2310820053 | 朱远亮（组长） | 计算机科学与技术 | 负责 user 模块测试（31 条）；编写测试计划 5.2/5.3 节；参与测试用例第 1-2 章撰写；完成测试报告 1、2 章撰写 |
| 2310820063 | 李冠燃 | 计算机科学与技术 | 负责 picture 模块测试（45 条）；编写测试计划 5.4/5.5 节；参与测试用例第 3-4 章撰写；完成测试报告 3、4 章撰写 |
| 2310820043 | 李坤纬 | 计算机科学与技术 | 负责 space 模块测试（34 条）；编写测试计划 5.7/5.6 节；参与测试用例第 5 章撰写；完成测试报告 5 章撰写 |
| 2310820051 | 林景彬 | 计算机科学与技术 | 负责 file + wxMp 模块测试（25 条）；编写测试计划 5.7/5.6 节；参与测试用例第 6 章撰写；完成测试报告 6 章撰写 |

**教师评语**：___________

---

## 二、测试用例、脚本工作量统计

| 姓名 | 功能用例 | 性能用例 | 接口用例 | 安全用例 | 兼容用例 | 单元用例 | 自动化用例 | 合计 |
|---|---|---|---|---|---|---|---|---|
| 朱远亮 | 11 | 6 | 3 | 6 | 0 | 0 | 5 | **31** |
| 李冠燃 | 18 | 6 | 3 | 6 | 0 | 0 | 12 | **45** |
| 李坤纬 | 23 | 6 | 3 | 6 | 0 | 0 | 6 | **34** |
| 林景彬 | 6 | 6 | 3 | 6 | 0 | 0 | 4 | **25** |
| **合计** | **58** | **24** | **12** | **24** | **0** | **0** | **27** | **135** |

---

## 三、测试范围

本项目覆盖以下核心模块：

### 3.1 user 模块（朱远亮负责）
- **接口数**：11 个
- **用例数**：31 条
- **覆盖**：/user/register、/user/login、/user/get/login、/user/update/my、/user/add、/user/get、/user/get/vo、/user/delete、/user/update、/user/list/page/vo、/user/logout

### 3.2 picture 模块（李冠燃负责）
- **接口数**：18 个
- **用例数**：45 条
- **覆盖**：/picture/upload、/picture/upload/url、/picture/upload/batch、/picture/delete、/picture/get、/picture/get/vo、/picture/list/page、/picture/list/page/vo、/picture/list/page/vo/cache、/picture/edit、/picture/edit/batch、/picture/update、/picture/tag_category、/picture/review、/picture/search/picture、/picture/search/color、/picture/out_painting/create_task、/picture/out_painting/get_task、/picture/proxy/editor

### 3.3 space 模块（李坤纬负责）
- **接口数**：23 个
- **用例数**：34 条
- **覆盖**：Space（10 个）、SpaceUser（6 个）、SpaceAnalyze（7 个）

### 3.4 file + wxMp 模块（林景彬负责）
- **接口数**：6 个
- **用例数**：25 条
- **覆盖**：/file/test/upload、/file/upload、/file/upload/avatar、/wx/mp/portal、/wx/mp/menu/create

---

## 四、测试策略

- **功能测试**：覆盖所有 API 接口的正常流程和异常流程
- **性能测试**：对核心接口（登录、上传、查询）进行并发性能测试
- **接口测试**：验证接口参数校验、错误处理
- **安全测试**：SQL 注入、越权、XSS、SSRF、伪造 Cookie 等
- **兼容性测试**：不同文件格式（jpg/png/bmp/gif/webp）
- **自动化测试**：Postman Runner 自动化执行
- **单元测试**：核心业务逻辑的单元测试

---

## 五、测试进度安排

| 阶段 | 工作内容 | 负责人 | 时间 |
|---|---|---|---|
| 1 | 测试计划、测试用例设计 | 全体 | 2026-06-17 ~ 2026-06-18 |
| 2 | 功能测试 + 性能测试 | 朱远亮、李冠燃、李坤纬、林景彬 | 2026-06-18 ~ 2026-06-19 |
| 3 | 接口测试 + 安全测试 | 全体 | 2026-06-19 |
| 4 | 回归测试 + 测试报告 | 全体 | 2026-06-19 |

---

## 六、测试环境

- **操作系统**：Windows 11
- **浏览器**：Chrome 125+
- **JDK**：OpenJDK 17.0.10
- **数据库**：MySQL 8.0.x
- **测试工具**：Postman 9.x、JMeter 5.6、BurpSuite
- **接口文档**：http://localhost:8123/api/doc.html

---

## 七、风险及应对措施

| 风险 | 概率 | 影响 | 应对措施 |
|---|---|---|---|
| 后端未部署 | 中 | 无法执行功能测试 | 使用本地环境 |
| 缺少真实数据 | 低 | 测试覆盖不全面 | 使用测试账号和种子数据 |
| 时间紧张 | 中 | 部分用例未执行 | 优先执行 P1 用例 |

---

**附件**：`StarPicture_测试用例.xlsx`（135 条用例汇总）