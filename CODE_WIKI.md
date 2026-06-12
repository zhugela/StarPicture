# 星图集（Star Picture）后端 · Code Wiki

> 一份面向开发者的、结构化的项目说明文档。涵盖项目背景、整体架构、模块职责、关键类、数据模型、依赖关系、运行方式与扩展点。

---

## 1. 项目概述

### 1.1 项目简介
**星图集（backend2）** 是一个面向团队与个人的 **图床/图片管理系统**，同时内置了团队空间、图片编辑协作、权限控制、审核、AI 扩图、按色搜图与数据统计分析等能力。后端基于 Spring Boot 2.7 + Java 17 构建，并引入 MyBatis-Plus、ShardingSphere、Caffeine、WebSocket + Disruptor 等组件。

### 1.2 核心能力

| 能力模块            | 说明                                                                 |
| ------------------- | -------------------------------------------------------------------- |
| 图片上传与图床服务  | 本地文件 / URL 抓取；主图、缩略图、原图、迁移图；腾讯云 COS 存储    |
| 团队 / 个人空间     | 私有空间、团队空间（普通 / 旗舰），独立空间配额与成员管理           |
| 分表存储（ShardingSphere） | picture 表按 **spaceId** 动态分表，旗舰空间独立建表 `picture_{spaceId}` |
| 图片编辑协作        | WebSocket + Disruptor 高并发的"加锁式"编辑，一人编辑，多人实时广播 |
| 图片审核            | 待审核 / 通过 / 拒绝，支持管理员回填审核信息                          |
| AI 扩图             | 接入阿里云 AI（通义万相）进行 Outpainting                             |
| 按色搜图 / 以图搜图 | 基于主色调十六进制值的相似度搜索；抓取图片 API 汇总搜索              |
| 空间 / 用户分析     | 空间用量、分类、标签、尺寸分段、活跃用户、上传趋势                    |
| 权限体系            | 系统管理员 / 普通用户 + 空间级角色（viewer / editor / admin）       |
| 微信公众号接入      | 图文消息回复、菜单管理、关注欢迎语                                    |

### 1.3 代码版本

-   **Group / Artifact**: `com.yu:backend2`
-   **Version**: `0.0.1-SNAPSHOT`
-   **Java**: 17
-   **Spring Boot**: 2.7.6
-   **启动类**: [Backend2Application.java](file:///workspace/src/main/java/com/yu/backend/Backend2Application.java)

---

## 2. 整体架构

### 2.1 架构分层

```
┌──────────────────────────────────────────────────────────────────────┐
│                     客户端（Web / H5 / 微信公众号）                    │
└────────────────────────────────┬─────────────────────────────────────┘
                                 │ HTTP / WebSocket
┌────────────────────────────────┴─────────────────────────────────────┐
│               Controller 层（REST + WebSocket）                        │
│  PictureController / SpaceController / SpaceAnalyzeController          │
│  UserController / SpaceUserController / WxMpController / FileController│
├──────────────────────────────────────────────────────────────────────┤
│               Service 层（业务）                                        │
│  PictureService / SpaceService / SpaceAnalyzeService                   │
│  UserService / SpaceUserService / WxMpService                          │
├──────────────────────────────────────────────────────────────────────┤
│               Manager 层（跨领域能力 / 技术能力封装）                  │
│  CosManager / FileManager / UploadFactory / PictureUploadTemplate      │
│  SpaceUserAuthManager / SpaceUserAuthRouteChecker                       │
│  DynamicShardingManager（ShardingSphere 动态分表）                     │
│  PictureEditEventProducer / PictureEditEventWorkHandler（Disruptor）    │
├──────────────────────────────────────────────────────────────────────┤
│               Mapper 层（MyBatis-Plus）                                │
│  PictureMapper / SpaceMapper / SpaceUserMapper / UserMapper            │
├──────────────────────────────────────────────────────────────────────┤
│               基础设施 / 外部系统                                       │
│  MySQL（ShardingSphere）│ Redis │ 腾讯云 COS │ 阿里云 AI ｜ 微信公众号  │
└──────────────────────────────────────────────────────────────────────┘
```

### 2.2 关键模块划分（Package）

| 包路径                | 作用                                                   |
| --------------------- | ------------------------------------------------------ |
| `annotation`          | `@AuthCheck` 注解，AOP 入口                            |
| `aop`                 | `AuthInterceptor` —— 基于角色的登录 / 权限校验          |
| `api/aliyunai`        | 阿里云 AI（OutPainting 扩图）任务创建与查询             |
| `api/imagesearch`     | 外部图片搜索 / 抓取聚合接口                            |
| `common`              | 统一响应 `BaseResponse`、工具 `ResultUtils`、分页 / 删除请求体 |
| `config`              | Spring、CORS、JWT、COS、WebSocket、MyBatis-Plus、事务、异步、Disruptor 配置 |
| `constant`            | `PictureConstant`、`SpaceUserPermissionConstant`、`UserConstant` |
| `context`             | 请求线程上下文：`SpaceUserAuthContextHolder`           |
| `controller`          | REST / WebSocket 入口                                  |
| `exception`           | 业务异常、错误码、全局异常处理                          |
| `filter`              | 可重复读的 HTTP 请求包装                                |
| `interceptor`         | Spring MVC 拦截器，空间级权限校验                      |
| `manager`             | 跨领域能力（Sharding 动态分表、上传、对象存储、鉴权路由） |
| `mapper`              | MyBatis-Plus Mapper                                    |
| `model/dto`           | 各接口请求 / 响应对象（picture / space / user / file） |
| `model/entity`        | 数据库实体                                             |
| `model/enums`         | 角色、空间类型、空间等级、图片审核、编辑动作等枚举     |
| `model/vo`            | 返回给前端的视图对象                                   |
| `service`             | 业务接口 + 实现                                        |
| `utils`               | JWT、颜色相似度、微信签名                              |
| `websocket`           | 图片协作编辑消息处理                                   |
| `websocket/disruptor` | 高并发编辑消息事件环（Ring Buffer）                    |
| `wx/mp`               | 微信公众号签名与 XML 工具                              |

### 2.3 请求流程概览

1.  **请求进入**: `CorsConfig` → `HttpRequestWrapperFilter`（缓存请求体） → `DispatcherServlet`。
2.  **登录态解析（JWT）**: `UserServiceImpl#getLoginUser` 读取 `Authorization: Bearer <token>`，由 `JwtUtils#getUserId` 解析用户 ID，查询 `User` 实体并缓存。
3.  **系统级权限（AOP）**: 被 `@AuthCheck(mustRole = "admin")` 标记的方法会被 `AuthInterceptor` 拦截，校验用户角色。
4.  **空间级权限（MVC 拦截器）**: `SpaceUserAuthInterceptor` 结合 `SpaceUserAuthRouteChecker` 判断当前 URL 是否属于空间级权限，再由 `SpaceUserAuthManager` 基于 `spaceUserAuthConfig.json` 的角色-权限映射完成检查。
5.  **业务 Service**: 完成业务逻辑后，通过 `ResultUtils.success` 包装为 `BaseResponse<T>`。
6.  **异常处理**: `BusinessException` / `ErrorCode` 由 `GlobalExceptionHandler` 统一响应。

---

## 3. 关键类与函数说明

### 3.1 启动与配置

| 类                                                                                            | 作用                                                 |
| --------------------------------------------------------------------------------------------- | ---------------------------------------------------- |
| [Backend2Application](file:///workspace/src/main/java/com/yu/backend/Backend2Application.java) | Spring Boot 入口；`@EnableAsync`、`@MapperScan`；静态块移除 BC JCE Provider |
| [application.yml](file:///workspace/src/main/resources/application.yml)                       | 默认端口 `8123`、context-path `/api`、JWT、公众号配置 |
| [application-local.yml](file:///workspace/src/main/resources/application-local.yml)           | 本地 profile：ShardingSphere `ds0`、COS、阿里云 AI   |

### 3.2 Controller 层

| Controller                                                                                                   | 核心职责 | 关键接口                                                                                                                               |
| ------------------------------------------------------------------------------------------------------------ | -------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| [PictureController](file:///workspace/src/main/java/com/yu/backend/controller/picture/PictureController.java) | 图片 CRUD、审核、搜索、扩图、编辑协作 | `uploadPicture`、`uploadPictureByBatch`、`getPictureVOById`、`listPictureVOByPage`、`deletePicture`、`editPicture`、`doPictureReview`、`searchPictureByColor`、`createPictureOutPaintingTask` |
| [SpaceController](file:///workspace/src/main/java/com/yu/backend/controller/space/SpaceController.java)       | 空间 CRUD | `addSpace`、`deleteSpace`、`editSpace`、`getSpaceVOById`、`listSpaceVOByPage`                                              |
| [SpaceAnalyzeController](file:///workspace/src/main/java/com/yu/backend/controller/space/SpaceAnalyzeController.java) | 数据分析 | 分类、标签、尺寸分段、用量、用户趋势、排行                                                                                             |
| [SpaceUserController](file:///workspace/src/main/java/com/yu/backend/controller/space/SpaceUserController.java)     | 空间成员 | 添加、删除、分页查询                                                                                                                   |
| [UserController](file:///workspace/src/main/java/com/yu/backend/controller/user/UserController.java)                | 用户管理 | 注册、登录、注销、修改个人信息、管理员添加 / 更新用户                                                                              |
| [WxMpController](file:///workspace/src/main/java/com/yu/backend/controller/wx/WxMpController.java)                  | 公众号   | 签名校验 / 消息回复                                                                                                                   |
| [FileController](file:///workspace/src/main/java/com/yu/backend/controller/FileController.java)                      | 文件上传 | 本地 multipart 文件上传到 COS，返回可访问 URL                                                                                         |

### 3.3 Service 层（接口 & 重要方法）

#### 3.3.1 [UserService](file:///workspace/src/main/java/com/yu/backend/service/UserService.java)

| 方法                                       | 职责                                       |
| ------------------------------------------ | ------------------------------------------ |
| `userRegister(account, pwd, checkPwd)`     | 注册校验 + 写入 `user` 表                 |
| `getEncryptPassword(password)`             | 使用 `userPassword` 的 md5 + salt 加密     |
| `userLogin(account, pwd, request)`         | 账号密码校验 + 签发 JWT；返回 `LoginUserVo` |
| `getLoginUser(request)`                    | 从 JWT 获取用户 id 并查询 User              |
| `isAdmin(user)`                            | 判断是否管理员                              |
| `getQueryWrapper(UserQueryRequest)`        | 生成用户分页查询条件                       |

#### 3.3.2 [SpaceService](file:///workspace/src/main/java/com/yu/backend/service/SpaceService.java)

| 方法                                              | 职责                                                     |
| ------------------------------------------------- | -------------------------------------------------------- |
| `addSpace(SpaceAddRequest, User)`                 | 创建空间，**旗舰团队空间触发 `DynamicShardingManager#createSpacePictureTable`** |
| `validSpace(Space, boolean)`                      | 空间合法性校验（名称、配额一致性）                      |
| `fillSpaceBySpaceLevel(Space)`                    | 按 `SpaceLevelEnum` 自动填充 `maxSize / maxCount`       |
| `deleteSpace(SpaceDeleteRequest, User)`           | 逻辑删除空间 + 清理图片；同时尝试删除 COS 文件          |
| `increaseUsageForNewPicture(spaceId, picSize)`    | 上传图片时：`totalCount++` 与 `totalSize += picSize`    |
| `adjustTotalSizeByDelta(spaceId, deltaPicSize)`   | 图片编辑 / 重绘时调整 totalSize                          |
| `delPictureUpdateSpaceUsage(spaceId, picSize)`    | 删除图片时回填用量                                      |
| `checkSpaceAuth(User, Space)`                     | 空间创建者或管理员方可管理空间                          |

#### 3.3.3 [PictureService](file:///workspace/src/main/java/com/yu/backend/service/PictureService.java)

| 方法                                                         | 职责                                                                 |
| ------------------------------------------------------------ | -------------------------------------------------------------------- |
| `uploadPicture(inputSource, PictureUploadWithUserDTO)`       | **文件上传入口**；通过 `UploadFactory` 选定 `FilePictureUpload` 或 `UrlPictureUpload`，调用 `PictureUploadTemplate` 上传到 COS 并入库 |
| `getPicture(id, spaceId)`                                    | 指定 spaceId，使 ShardingSphere 能精准路由到 `picture_{spaceId}` 分表 |
| `checkPictureAuth(User, Picture)`                            | 空间图片 → 结合空间成员权限；公共图库 → 本人或管理员                |
| `deletePicture(pictureId, spaceId, User)`                    | 逻辑删除 + 用量回填 + 可选 COS 清理                                |
| `searchPictureByColor(spaceId, picColor, User)`              | 按十六进制主色调相似度排序（`ColorSimilarUtils`）                    |
| `createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest, User)` | 调用 `AliYunAiApi` 创建扩图任务                                      |
| `doPictureReview(PictureReviewRequest, User)`                 | 管理员回填 reviewStatus / reviewMessage / reviewerId               |
| `fillReviewParams(Picture, User)`                             | 上传公共图库时默认填充审核信息（1：通过）                           |

#### 3.3.4 [SpaceAnalyzeService](file:///workspace/src/main/java/com/yu/backend/service/SpaceAnalyzeService.java)

| 方法                                                       | 职责                                                     |
| ---------------------------------------------------------- | -------------------------------------------------------- |
| `getSpaceAnalyze(request, User)`                           | 综合统计：图片数、体积、按分类、按标签、按尺寸分布       |
| `getSpaceUsageAnalyze(request, User)`                      | 容量、条数、占比；空间等级                               |
| `getSpaceCategoryAnalyze / getSpaceTagAnalyze / getSpaceSizeAnalyze` | 分类 / 标签 / 尺寸分段 Top N                             |
| `getSpaceUserAnalyze(request, User)`                       | 按时间维度统计用户上传（小时 / 天 / 月）                 |
| `getSpaceRankAnalyze(request, User)`                       | 管理员查看空间使用排行                                   |

#### 3.3.5 [WxMpService](file:///workspace/src/main/java/com/yu/backend/service/WxMpService.java)

| 方法                                                       | 职责                                                 |
| ---------------------------------------------------------- | ---------------------------------------------------- |
| `verifyPortal(signature, timestamp, nonce, echostr)`      | 公众号首次接入签名校验，直接返回 echostr             |
| `handleMessage(requestBody, signature, timestamp, nonce)` | 文本 / 事件消息回复；关注欢迎、关键字回复、默认回复  |
| `createDefaultMenu()`                                      | 编程式创建菜单（需要 appId / appSecret / access_token） |

### 3.4 Manager 层

| 类                                                                                                                             | 职责                                                          |
| ------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------- |
| [CosManager](file:///workspace/src/main/java/com/yu/backend/manager/CosManager.java)                                           | 腾讯云 COS 封装：上传、删除对象、生成上传结果                 |
| [FileManager](file:///workspace/src/main/java/com/yu/backend/manager/FileManager.java)                                         | 文件上传 Controller 能力封装（multipart → COS URL）            |
| [UploadFactory](file:///workspace/src/main/java/com/yu/backend/manager/factory/UploadFactory.java)                             | 基于输入源类型（MultipartFile / String URL）选择对应上传实现   |
| [PictureUploadTemplate](file:///workspace/src/main/java/com/yu/backend/manager/upload/PictureUploadTemplate.java)             | 模板方法：校验 → 下载临时文件 → COS → 解析 metadata → 清理   |
| [FilePictureUpload](file:///workspace/src/main/java/com/yu/backend/manager/upload/FilePictureUpload.java)                      | 本地 MultipartFile 上传实现                                   |
| [UrlPictureUpload](file:///workspace/src/main/java/com/yu/backend/manager/upload/UrlPictureUpload.java)                        | 从 URL 抓取的上传实现                                         |
| [DynamicShardingManager](file:///workspace/src/main/java/com/yu/backend/manager/sharding/DynamicShardingManager.java)           | 启动时：迁移 `spaceId` NULL→0；为已有旗舰空间迁移数据至 `picture_{spaceId}` 并刷新 `actual-data-nodes` |
| [PictureShardingAlgorithm](file:///workspace/src/main/java/com/yu/backend/manager/sharding/PictureShardingAlgorithm.java)       | 按 spaceId 的 Standard 分片算法；非旗舰或 NULL 路由到 `picture`；旗舰空间路由到 `picture_{spaceId}` |
| [SpaceUserAuthManager](file:///workspace/src/main/java/com/yu/backend/manager/SpaceUserAuthManager.java)                       | 读取 `spaceUserAuthConfig.json`，判断角色-权限-路由映射        |
| [SpaceUserAuthRouteChecker](file:///workspace/src/main/java/com/yu/backend/manager/SpaceUserAuthRouteChecker.java)             | 判断当前请求路径是否为空间级资源（需要 spaceId 鉴权）          |

### 3.5 图片编辑协作（WebSocket + Disruptor）

| 类                                                                                                                             | 职责                                                                      |
| ------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------- |
| [PictureEditHandler](file:///workspace/src/main/java/com/yu/backend/websocket/PictureEditHandler.java)                         | WebSocket 处理器；维护 `pictureEditingUsers`、`pictureSessions`、`pictureEditRecodes` 三张映射表；一人编辑、多人监听 |
| [WsHandshakeInterceptor](file:///workspace/src/main/java/com/yu/backend/websocket/WsHandshakeInterceptor.java)                  | 握手时解析 JWT / user / pictureId，并塞入 session attributes              |
| [PictureEditEvent](file:///workspace/src/main/java/com/yu/backend/websocket/disruptor/PictureEditEvent.java)                   | 事件模型（message、session、user、pictureId）                             |
| [PictureEditEventProducer](file:///workspace/src/main/java/com/yu/backend/websocket/disruptor/PictureEditEventProducer.java)   | 写入 RingBuffer                                                           |
| [PictureEditEventWorkHandler](file:///workspace/src/main/java/com/yu/backend/websocket/disruptor/PictureEditEventWorkHandler.java) | 消费事件，转发到 `PictureEditHandler#handleEnterEditMessage / handleEditActionMessage / handleExitEditMessage` |
| [PictureEditEventDisruptorConfig](file:///workspace/src/main/java/com/yu/backend/config/PictureEditEventDisruptorConfig.java)  | Disruptor RingBuffer 大小、等待策略配置                                  |

**消息类型**：
-   `ENTER_EDIT` —— 锁定当前 picture 给第一个发起编辑的用户
-   `EXIT_EDIT` —— 解除锁定
-   `EDIT_ACTION` —— 广播编辑动作（旋转、翻转、裁剪等，动作枚举来自 `PictureEditActionEnum`）
-   `INFO / ERROR` —— 纯文本广播 / 单播

### 3.6 权限与鉴权

| 类                                                                                             | 职责                                                                 |
| ---------------------------------------------------------------------------------------------- | -------------------------------------------------------------------- |
| [AuthCheck](file:///workspace/src/main/java/com/yu/backend/annotation/AuthCheck.java)          | `@AuthCheck(mustRole = "admin")` 注解                                 |
| [AuthInterceptor](file:///workspace/src/main/java/com/yu/backend/aop/AuthInterceptor.java)     | AOP 拦截器；拿 `UserService#getLoginUser`，比对 `UserRoleEnums`       |
| [SpaceUserAuthInterceptor](file:///workspace/src/main/java/com/yu/backend/interceptor/SpaceUserAuthInterceptor.java) | MVC 拦截器；根据 URL 中 spaceId / userId / 权限 key 决定放行或拒绝  |
| [SpaceUserAuthContextHolder](file:///workspace/src/main/java/com/yu/backend/context/SpaceUserAuthContextHolder.java) | 线程上下文：保存当前请求的空间鉴权结果                             |
| [JwtUtils](file:///workspace/src/main/java/com/yu/backend/utils/JwtUtils.java)                  | JWT 手动实现（HS256，无第三方依赖）；`createToken / getUserId`       |
| [JwtProperties](file:///workspace/src/main/java/com/yu/backend/config/JwtProperties.java)       | `auth.jwt.secret / expire-days`                                     |

权限配置示例（运行时加载）：[spaceUserAuthConfig.json](file:///workspace/src/main/resources/biz/spaceUserAuthConfig.json)

```
viewer  -> picture:view
editor  -> picture:view + upload + edit + delete
admin   -> spaceUser:manage + picture:*
```

### 3.7 工具类

| 类                                                                                          | 说明                                          |
| ------------------------------------------------------------------------------------------- | --------------------------------------------- |
| [ColorSimilarUtils](file:///workspace/src/main/java/com/yu/backend/utils/ColorSimilarUtils.java) | 十六进制颜色 → RGB；基于欧氏距离 / 色差计算  |
| [WxMpSignUtils](file:///workspace/src/main/java/com/yu/backend/utils/WxMpSignUtils.java)     | 公众号 `signature=sha1(token, timestamp, nonce)` |

---

## 4. 数据模型（实体 & 枚举）

### 4.1 实体 / 表

| 实体                                                                                     | 表                  | 核心字段                                                                               |
| ---------------------------------------------------------------------------------------- | ------------------- | -------------------------------------------------------------------------------------- |
| [User](file:///workspace/src/main/java/com/yu/backend/model/entity/User.java)            | `user`              | `id / userAccount / userPassword / userName / userAvatar / userProfile / userRole / mpOpenId` |
| [Picture](file:///workspace/src/main/java/com/yu/backend/model/entity/Picture.java)      | `picture` + 分表 | `id / spaceId / urls(Urls) / name / introduction / category / picColor / tags / picSize / picWidth / picHeight / picScale / picFormat / userId / reviewStatus / reviewMessage / reviewerId / reviewTime` |
| [Urls](file:///workspace/src/main/java/com/yu/backend/model/entity/Urls.java)            | 嵌入 Picture JSON   | `originalUrl / url / thumbnailUrl / transferUrl`                                        |
| [Space](file:///workspace/src/main/java/com/yu/backend/model/entity/Space.java)          | `space`             | `id / spaceName / spaceLevel / spaceType / maxSize / maxCount / totalSize / totalCount / userId` |
| [SpaceUser](file:///workspace/src/main/java/com/yu/backend/model/entity/SpaceUser.java)  | `space_user`        | `id / spaceId / userId / spaceRole(viewer/editor/admin)`                                |

建表与迁移脚本：
-   [create_sql.sql](file:///workspace/sql/create_sql.sql) —— 首次建表 / 索引
-   [add_space_type.sql](file:///workspace/sql/add_space_type.sql) / [add_space_user.sql](file:///workspace/sql/add_space_user.sql) —— 后续变更
-   [add_user_mp_open_id.sql](file:///workspace/sql/add_user_mp_open_id.sql)
-   [migrate_picture_sharding.sql](file:///workspace/sql/migrate_picture_sharding.sql) —— ShardingSphere 分表迁移
-   [starpicture_full.sql](file:///workspace/sql/starpicture_full.sql) —— 全量建表 / 数据合集

### 4.2 枚举

| 枚举                                                                                                | 值                                                         |
| --------------------------------------------------------------------------------------------------- | ---------------------------------------------------------- |
| [UserRoleEnums](file:///workspace/src/main/java/com/yu/backend/model/enums/UserRoleEnums.java)       | `user` / `admin`                                          |
| [SpaceLevelEnum](file:///workspace/src/main/java/com/yu/backend/model/enums/SpaceLevelEnum.java)    | 免费（100MB/100 张） / 专业（2GB/2000 张） / 旗舰（20GB/20000 张） |
| [SpaceTypeEnum](file:///workspace/src/main/java/com/yu/backend/model/enums/SpaceTypeEnum.java)      | `PRIVATE(0)` / `TEAM(1)`                                  |
| [SpaceRoleEnum](file:///workspace/src/main/java/com/yu/backend/model/enums/SpaceRoleEnum.java)      | `viewer / editor / admin`                                 |
| [PictureReviewStatusEnum](file:///workspace/src/main/java/com/yu/backend/model/enums/PictureReviewStatusEnum.java) | `REVIEWING(0) / PASS(1) / REJECT(2)`                      |
| [PictureEditMessageTypeEnum](file:///workspace/src/main/java/com/yu/backend/model/enums/PictureEditMessageTypeEnum.java) | `ENTER_EDIT / EXIT_EDIT / EDIT_ACTION / INFO / ERROR`    |
| [TimeDimensionEnum](file:///workspace/src/main/java/com/yu/backend/model/enums/TimeDimensionEnum.java) | `HOUR / DAY / MONTH`                                      |
| [FileUploadEnum](file:///workspace/src/main/java/com/yu/backend/model/enums/FileUploadEnum.java)    | `MULTIPART_FILE / URL`                                    |

### 4.3 ER 概览

```
          user (1) ─────┐
                         ├─ picture (N)
                         │
          space (1) ─────┤
           │             ├─ picture (N，spaceId 分表)
           │
           └─ space_user (N) ── user (N)
                    │
                    └─ spaceRole {viewer, editor, admin}
```

---

## 5. 依赖关系

### 5.1 核心 Maven 依赖

| 依赖                              | 版本               | 用途                                      |
| --------------------------------- | ------------------ | ----------------------------------------- |
| `spring-boot-starter-web`         | 2.7.6（parent）    | Spring MVC                                |
| `spring-boot-starter-data-redis`  | 2.7.6              | 缓存 / 轻量会话                           |
| `spring-boot-starter-websocket`   | 2.7.6              | WebSocket                                 |
| `spring-boot-starter-aop`         | 2.7.6              | 权限拦截器                                |
| `mysql-connector-j`               | runtime            | MySQL 驱动                                |
| `mybatis-plus-boot-starter`       | 3.5.9              | ORM / 分页插件 / Wrapper                 |
| `shardingsphere-jdbc-core-spring-boot-starter` | 5.2.0 | 分表（按 spaceId）、自动分片                |
| `disruptor`                       | 3.4.2              | 高并发图片编辑消息环                       |
| `caffeine`                        | 2.9.3              | 本地缓存（Controller 内使用）              |
| `hutool-all`                      | 5.8.26             | 工具类集合（Http、JSON、字符串、日期等）   |
| `jsoup`                           | 1.15.3             | 解析 URL 图片所在 HTML                    |
| `commons-lang3`                   | —                  | 字符串 / 反射工具                         |
| `cos_api`                         | 5.6.227            | 腾讯云对象存储                            |
| `dashscope-sdk-java`（阿里云 AI） | 2.17.1             | OutPainting 扩图                          |
| `httpclient`                      | 4.5.13             | 外部图片搜索 / 公众号 token 获取           |
| `lombok`                          | 1.18.34            | 编译期 annotation 处理                    |

### 5.2 模块依赖简图

```
Backend2Application
  └─ Spring 上下文启动（含 @EnableAsync、@MapperScan）
     ├─ Config （Cors、Caffeine/Redis、JWT、COS、WebSocket、Transaction、Async、Disruptor、MyBatis-Plus）
     ├─ Controller → Service → Mapper
     └─ Manager
         ├─ CosManager → 腾讯云 COS
         ├─ UploadFactory → PictureUploadTemplate → {File,Url}PictureUpload
         ├─ DynamicShardingManager + PictureShardingAlgorithm → ShardingSphere
         ├─ SpaceUserAuthManager + SpaceUserAuthRouteChecker → Spring Interceptor
         └─ PictureEditEventProducer / WorkHandler → Disruptor RingBuffer
                                              └→ PictureEditHandler（WebSocket）
```

---

## 6. 项目运行方式

### 6.1 环境要求

| 组件    | 版本                 |
| ------- | -------------------- |
| JDK     | 17                   |
| Maven   | 3.8+                 |
| MySQL   | 5.7 / 8.0            |
| Redis   | 5+                   |
| 浏览器  | 任意现代浏览器        |

### 6.2 配置要点（`application.yml` / `application-local.yml`）

```yaml
server:
  port: 8123
  servlet:
    context-path: /api

spring:
  profiles:
    active: prod   # 本地开发使用 local
  shardingsphere:
    datasource:
      names: ds0
      ds0:
        jdbc-url: jdbc:mysql://localhost:3306/starpicture?...
        username: root
        password: ${MYSQL_PASSWORD:CHANGE_ME}
    rules:
      sharding:
        tables:
          picture:
            actual-data-nodes: ds0.picture   # 由 DynamicShardingManager 动态刷新
            table-strategy:
              standard:
                sharding-column: spaceId
                sharding-algorithm-name: picture_sharding_algorithm
        sharding-algorithms:
          picture_sharding_algorithm:
            type: CLASS_BASED
            props:
              algorithmClassName: com.yu.backend.manager.sharding.PictureShardingAlgorithm

cos:
  client:
    host: ${COS_HOST:...}
    secretId: ${COS_SECRET_ID:CHANGE_ME}
    secretKey: ${COS_SECRET_KEY:CHANGE_ME}
    region: ${COS_REGION:ap-guangzhou}
    bucket: ${COS_BUCKET:your-bucket}

auth:
  jwt:
    secret: star-picture-jwt-secret-change-me
    expire-days: 30

wx:
  mp:
    enabled: true
    app-id: ${WX_MP_APP_ID:}
    app-secret: ${WX_MP_APP_SECRET:}
    token: starpicture

aliYunAi:
  apiKey: ${ALIYUN_AI_API_KEY:CHANGE_ME}
```

### 6.3 数据库初始化

1.  执行 [create_sql.sql](file:///workspace/sql/create_sql.sql)，创建 `starpicture` 数据库及 `user / picture / space / space_user` 表。
2.  需要 ShardingSphere 动态分表的历史数据：执行 [migrate_picture_sharding.sql](file:///workspace/sql/migrate_picture_sharding.sql)；应用启动时 `DynamicShardingManager` 也会完成 `spaceId` NULL→0 的修复、旗舰空间迁移并刷新 `actual-data-nodes`。

### 6.4 启动

开发态：
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

生产打包：
```bash
mvn clean package -DskipTests
java -jar target/backend2-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

启动成功后：
-   接口根路径：`http://localhost:8123/api`
-   Knife4j 文档：`http://localhost:8123/api/doc.html`

### 6.5 初始登录账号

| 字段         | 示例        |
| ------------ | ----------- |
| userAccount  | `admin`     |
| userPassword | `admin123`  |
| userRole     | `admin`     |

> 生产环境请务必替换初始密码、JWT secret、COS / 公众号 / 阿里云 AI 等凭证。

### 6.6 登录流程（JWT）

1.  调用 `POST /api/user/login`，提交 `{userAccount, userPassword}`。
2.  后端返回 `{ token, loginUserVO }`。
3.  后续请求头携带 `Authorization: Bearer <token>` 即可。
4.  管理员访问带 `@AuthCheck(mustRole = "admin")` 的接口。

---

## 7. 关键设计决策

### 7.1 空间级分表（ShardingSphere）
-   公共图库（`spaceId=0`）使用主表 `picture`；
-   **旗舰版团队空间** 按 `spaceId` 独立建表 `picture_{spaceId}`，避免大表扫描；
-   通过 `DynamicShardingManager#initialize` 在启动时 **动态刷新 actual-data-nodes**，避免手动维护 YAML。

### 7.2 空间级权限（JSON 驱动）
-   [spaceUserAuthConfig.json](file:///workspace/src/main/resources/biz/spaceUserAuthConfig.json) 定义 `permission / role / permission→role` 关系；
-   `SpaceUserAuthRouteChecker` 将 URL（如 `/picture/add?spaceId=xx`）映射到权限 key；
-   `SpaceUserAuthManager` 读取 `space_user.spaceRole`，查表判定是否放行。

### 7.3 JWT 手工实现
-   为了避免 BC JCE 认证问题，使用 `HmacSHA256` + `Base64 URL-safe` 手工实现 JWT 格式（`JwtUtils`）；
-   启动类静态块执行 `Security.removeProvider("BC")` 进一步规避 BouncyCastle 兼容性问题。

### 7.4 WebSocket 编辑并发
-   使用 `pictureEditingUsers` 维护每张图的"编辑锁"；
-   通过 Disruptor RingBuffer 把并发消息串行化消费，避免对同一图片产生冲突；
-   `pictureEditRecodes` 为新加入连接的用户回放最近编辑动作。

---

## 8. 扩展点与二次开发建议

| 方向               | 建议入口                                                                 |
| ------------------ | ------------------------------------------------------------------------ |
| 新增权限项         | 修改 [spaceUserAuthConfig.json](file:///workspace/src/main/resources/biz/spaceUserAuthConfig.json) 并在 Controller 中添加 `@AuthCheck` 或权限路由检查 |
| 新增空间等级       | 在 `SpaceLevelEnum` 中增加枚举值，并在 `SpaceServiceImpl#fillSpaceBySpaceLevel` 补充配额逻辑 |
| 新增图片编辑动作   | 扩展 `PictureEditActionEnum`，在前端按枚举值发 `EDIT_ACTION` 消息        |
| 替换对象存储       | 新增 `XxxManager` 并实现与 `CosManager` 相同的接口 / 语义；替换 UploadTemplate 调用 |
| 替换 AI 服务       | 仿照 `AliYunAiApi` 封装新的服务；在 `PictureController#createPictureOutPaintingTask` 切换注入 |
| 增加数据报表       | 在 `SpaceAnalyzeService` 扩展新方法，`SpaceAnalyzeController` 增加 API     |

---

## 9. 附录：目录速查

-   配置文件：[application.yml](file:///workspace/src/main/resources/application.yml)、[application-local.yml](file:///workspace/src/main/resources/application-local.yml)
-   权限配置：[spaceUserAuthConfig.json](file:///workspace/src/main/resources/biz/spaceUserAuthConfig.json)
-   SQL 脚本：[sql/](file:///workspace/sql)
-   源码根目录：[src/main/java/com/yu/backend](file:///workspace/src/main/java/com/yu/backend)
-   报告与文档：[docs/](file:///workspace/docs)

```
/workspace
├─ src/main/java/com/yu/backend
│   ├─ annotation        @AuthCheck
│   ├─ aop               AuthInterceptor
│   ├─ api               aliyunai / imagesearch
│   ├─ common            BaseResponse, ResultUtils
│   ├─ config            各类配置类
│   ├─ constant          常量
│   ├─ context           线程上下文
│   ├─ controller        REST 接口
│   ├─ exception         异常与错误码
│   ├─ filter            RequestWrapper
│   ├─ interceptor       SpaceUserAuthInterceptor
│   ├─ manager           技术能力封装（上传、COS、Sharding、鉴权路由）
│   ├─ mapper            MyBatis-Plus Mapper
│   ├─ model             dto / entity / enums / vo
│   ├─ service           业务服务
│   ├─ utils             JWT、颜色、签名
│   ├─ websocket         图片协作编辑
│   └─ wx/mp             公众号
├─ src/main/resources
│   ├─ application.yml
│   ├─ application-local.yml
│   ├─ application-prod.yml
│   ├─ biz/spaceUserAuthConfig.json
│   ├─ db/picture_urls_migration.sql
│   └─ mapper/*.xml
├─ sql/                  数据库脚本
├─ docs/                 课程设计 / 部署文档
└─ pom.xml
```

---

**维护者**: backend2 Team
**最后更新**: 2026
