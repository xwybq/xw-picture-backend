create database if not exists xw_picture;

use xw_picture;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色:user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount_isDelete (userAccount, isDelete),
    INDEX idx_userName (userName)
) comment '用户' collate = 'utf8mb4_unicode_ci';



# SHOW INDEX FROM `user` WHERE Key_name LIKE '%userAccount%';
#
# ALTER TABLE `user`
#     DROP INDEX `uk_userAccount`;
#
# ALTER TABLE `user`
#     ADD UNIQUE INDEX `uk_userAccount_isDelete` (`userAccount`, `isDelete`);


-- picture 表
create table if not exists picture
(
    id           bigint auto_increment comment 'id' primary key,
    userId       bigint                             not null comment '创建用户id',
    url          varchar(512)                       not null comment '图片url',
    name         varchar(128)                       null comment '图片名称',
    introduction varchar(512)                       null comment '图片介绍',
    category     varchar(64)                        null comment '图片分类',
    tag          varchar(512)                       null comment '图片标签 (逗号分隔，格式为JSON数组)',
    picSize      bigint                             null comment '图片大小 (字节)',
    picHeight    int                                null comment '图片高度',
    picWidth     int                                null comment '图片宽度',
    picScale     double                             null comment '图片缩放比例',
    picFormat    varchar(32)                        null comment '图片格式 (jpg/png等)',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_userId (userId),
    INDEX idx_name (name),
    INDEX idx_category (category),
    INDEX idx_tag (tag),
    INDEX idx_introduction (introduction)
) comment '图片' collate = 'utf8mb4_unicode_ci';
