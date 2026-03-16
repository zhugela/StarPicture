create database if not exists `starpicture`;

create table if not exists user
(
    id            bigint auto_increment comment 'id' primary key,
    userAccount   varchar(256)                           not null comment '账号',
    userPassword  varchar(512)                           not null comment '密码',
    userName      varchar(256)                           null comment '用户昵称',
    userAvatar    varchar(1024)                          null comment '用户头像',
    userProfile   varchar(512)                           null comment '用户简介',
    userRole      varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime      datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime    datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime    datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete      tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

create table if not exists picture
(
    id           bigint auto_increment comment 'id' primary key,
    url          varchar(512)                           not null comment '图片url',
    name         varchar(128)                           not null comment '图片名称',
    introduction varchar(512)                           null comment '图片简介',
    tags         varchar(512)                           null comment '标签（JSON数组）',
    category     varchar(64)                            null comment '分类',
    picWidth     int                                    null comment '图片宽度',
    picHeight    int                                    null comment '图片高度',
    picSize      bigint                                 null comment '图片大小',
    picFormat    varchar(128)                           null comment '图片格式',
    picScale     double                                 null comment '图片比例',
    userId       bigint                                 not null comment '创建用户id',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    INDEX idx_name (name),
    INDEX idx_userId (userId),
    INDEX idx_category (category),
    INDEX idx_tags (tags)
    ) comment '图片' collate = utf8mb4_unicode_ci;