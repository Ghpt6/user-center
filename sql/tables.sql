-- auto-generated definition
create table user
(
    id           bigint auto_increment
        primary key,
    username     varchar(256)                       null comment '用户昵称',
    userAccount  varchar(256)                       null comment '账号',
    avatarUrl    varchar(1024)                      null comment '头像',
    gender       tinyint                            null comment '性别',
    userPassword varchar(256)                       not null comment '密码',
    phone        varchar(128)                       null comment '电话',
    email        varchar(256)                       null comment '邮箱',
    userStatus   int      default 0                 null comment '用户状态(0-正常)',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    userRole     int      default 0                 not null comment '普通用户0，管理员1',
    tags         varchar(1024)                      null comment '标签列表json'
)
    comment '用户';



-- auto-generated definition
create table tag
(
    id         bigint auto_increment
        primary key,
    tagName    varchar(256)                       not null comment '标签名称',
    userId     bigint                             null comment '用户ID',
    parentId   bigint                             null comment '父标签',
    isParent   tinyint                            null comment '是否为父标签（0不是，1是）',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '标签';

create index idx_userId
    on tag (userId);




-- auto-generated definition
create table team
(
    id          bigint auto_increment
        primary key,
    name        varchar(256)                       null comment '队伍名称',
    description varchar(1024)                      null comment '队伍描述',
    maxNum      int                                null comment '最大人数',
    expireTime  datetime                           null comment '队伍过期时间',
    founderId   bigint                             null comment '队伍创建人id',
    status      int      default 0                 null comment '状态（0-公开，1-私有，2-加密）',
    password    varchar(256)                       null comment '密码',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null comment '更新时间',
    isDelete    tinyint  default 0                 null comment '是否删除'
)
    comment '队伍表';

-- auto-generated definition
create table user_team
(
    id         bigint auto_increment
        primary key,
    userId     bigint                             null comment '用户id',
    teamId     bigint                             null comment '队伍id',
    joinTime   datetime                           null comment '加入时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null comment '更新时间',
    isDelete   tinyint  default 0                 null comment '是否删除'
)
    comment '用户队伍关系表';

