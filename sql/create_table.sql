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
    tags         varchar(512)                       null comment '图片标签 (逗号分隔，格式为JSON数组)',
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
    INDEX idx_tags (tags),
    INDEX idx_introduction (introduction)
) comment '图片' collate = 'utf8mb4_unicode_ci';

-- 图片审核字段
alter table picture
    add column reviewStatus  tinyint default 0 not null comment '审核状态: 0-待审核, 1-审核通过, 2-审核拒绝',
    add column reviewMessage varchar(512)      null comment '审核消息 (审核拒绝时填写)',
    add column reviewId      bigint            null comment '审核人id',
    add column reviewTime    datetime          null comment '审核时间';

-- 图片审核状态索引
create index idx_reviewStatus on picture (reviewStatus);


alter table picture
    add column thumbnailUrl varchar(512) null comment '图片缩略图url';


-- 空间表
create table if not exists space
(
    id         bigint auto_increment comment 'id' primary key,
    userId     bigint                             not null comment '创建用户id',
    spaceName  varchar(128)                       null comment '空间名称',
    spaceLevel tinyint  default 0                 not null comment '空间等级: 0-普通空间, 1-专业空间 2-旗舰空间',
    maxSize    bigint   default 0                 null comment '空间图片的最大大小 (字节)',
    maxCount   bigint   default 0                 null comment '空间图片的最大数量',
    totalSize  bigint   default 0                 null comment '当前空间图片的总大小 (字节)',
    totalCount bigint   default 0                 null comment '当前空间图片的总数量',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId),         -- 根据用户id查询空间
    index idx_spaceLevel (spaceLevel), -- 根据空间等级查询空间
    index idx_spaceName (spaceName)    -- 根据空间名称查询空间
) comment '空间' collate = 'utf8mb4_unicode_ci';


alter table picture
    add column spaceId bigint null comment '空间id(为空则为公共空间)';

create index idx_spaceId on picture (spaceId);
