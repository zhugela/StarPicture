# 项目：内娱图库（StarPicture）

## 技术栈
- Java 17 + Spring Boot 2.7.6
- MyBatis-Plus 3.5.9
- MySQL 8.x
- 腾讯云 COS 对象存储
- Knife4j 4.4.0 接口文档
- Hutool 工具库
- Lombok

## 包结构
- com.yu.backend
    - annotation    自定义注解（AuthCheck）
    - aop           切面（AuthInterceptor）
    - common        通用类（BaseResponse、ResultUtils、ErrorCode）
    - config        配置类（CosClientConfig、MyBatisPlusConfig、JsonConfig）
    - constant      常量（UserConstant）
    - controller    控制器
    - exception     异常处理（BusinessException、GlobalExceptionHandler）
    - manager       管理器（CosManager、FileManager）
    - mapper        数据访问层
    - model
        - dto         请求参数
        - entity      数据库实体
        - enums       枚举
        - vo          返回对象
    - service       业务逻辑

## 编码规范
- Controller 层只做：参数校验 → 调 Service → 返回结果
- Service 层处理所有业务逻辑
- 用 ThrowUtils.throwIf() 做参数校验
- 用 BeanUtil.copyProperties() 做对象转换
- 返回给前端统一用 ResultUtils.success() 包装
- 敏感信息（密码、isDelete）不能出现在 VO 里
- 管理员接口加 @AuthCheck(mustRole = "admin")
- 普通用户操作需要判断是否是本人或管理员

## 当前进度
- 用户模块：已完成（注册、登录、注销、获取当前用户、权限控制、用户管理 CRUD）
- 图片模块：正在开发
    - 已完成：上传图片、删除图片
    - 待开发：更新图片、查询图片、分页查询、编辑图片

## 数据库表
- user 表：id, userAccount, userPassword, userName, userAvatar, userProfile, userRole, editTime, createTime, updateTime, isDelete
- picture 表：id, url, name, introduction, category, tags(JSON字符串), picSize, picWidth, picHeight, picScale, picFormat, userId, editTime, createTime, updateTime, isDelete