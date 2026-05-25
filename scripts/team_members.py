# -*- coding: utf-8 -*-
"""星图集小组 4 人分工（报告生成共用）"""

MAJOR = '计算机科学与技术'

# 表：学号、姓名、专业、主要工作、课程设计成绩（成绩留空由教师填写）
MEMBER_ROWS = [
    (
        '2310820053',
        '朱远亮',
        MAJOR,
        '（组长）项目总体架构与接口规范；后端核心开发（User/Picture/Space/File/WxMp）；'
        '微信小程序主要页面与 JWT 登录；微信公众号接入与自动回复；MySQL/COS 配置、三端联调与缺陷修复；Git 管理',
        '',
    ),
    (
        '2310820051',
        '林景彬',
        MAJOR,
        'Vue3 Web 前端：HomePage 公共图库、登录/注册页；Axios 封装与 Pinia 登录态；'
        'GlobalHeader 导航、UserProfilePage 个人资料页；Web 端与后端接口联调',
        '',
    ),
    (
        '2310820063',
        '李冠燃',
        MAJOR,
        '数据库表结构与 SQL 脚本整理；功能测试用例设计与黑盒测试；Knife4j 接口测试记录；'
        '小组/课程报告中测试章节、答辩演示脚本撰写',
        '',
    ),
    (
        '2310820043',
        '李坤纬',
        MAJOR,
        '管理员端（用户管理、图片审核页）；空间相关 Web/小程序页面辅助；'
        'UI 样式统一（CSS/WXSS）；答辩 PPT 与关键界面截图整理',
        '',
    ),
]

# 详细贡献表：姓名、学号、角色、主要任务、个人贡献说明
CONTRIBUTION_ROWS = [
    (
        '朱远亮',
        '2310820053',
        '组长/全栈',
        '后端、小程序、公众号、联调',
        '完成 User/Picture/Space/File/WxMp 等 Controller 与 Service；JWT 与审核策略；'
        '小程序首页/上传/我的/空间/资料页；公众号 portal 与自动回复；主导三端缺陷修复（工作量约 45%）',
    ),
    (
        '林景彬',
        '2310820051',
        'Web 前端',
        'Vue 页面、接口联调',
        'HomePage、UserLoginPage、UserRegisterPage、GlobalHeader、UserProfilePage；'
        'request.ts Token 拦截；Web 端 npm run build 通过（工作量约 20%）',
    ),
    (
        '李冠燃',
        '2310820063',
        '测试/文档',
        '测试用例、SQL、报告',
        'create_sql.sql 维护；表3-3~3-6 测试用例执行与记录；Knife4j 接口调试；答辩演示流程脚本（工作量约 18%）',
    ),
    (
        '李坤纬',
        '2310820043',
        '管理端/UI',
        '审核页、空间页、PPT',
        'PictureManagePage、UserManagePage；小程序 space 列表/详情页辅助；品牌 UI 统一；答辩 PPT 与截图（工作量约 17%）',
    ),
]

GRADE_LINES = [
    ('2310820053', '朱远亮', '（组长）'),
    ('2310820051', '林景彬', ''),
    ('2310820063', '李冠燃', ''),
    ('2310820043', '李坤纬', ''),
]
