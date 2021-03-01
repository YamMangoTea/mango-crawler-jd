create database crawler;
use crawler;
create table `jd_item` (
    `id` bigint(10) not null auto_increment comment '主键id',
    `spu` bigint(15) default null comment '商品集合id',
    `sku` bigint(15) default null comment '商品最小品类单元id',
    `title` varchar(100) default null comment '商品标题',
    `price` bigint(10) default null comment '商品价格',
    `pic` varchar(200) default null comment '商品图片',
    `url` varchar(200) default null comment '商品详情地址',
    `created` datetime default null comment '创建时间',
    `updated` datetime default null comment '更新时间',
    primary key (`id`),
    key `sku` (`sku`) using btree
) engine=InnoDB default  charset=utf8 comment='京东商品表';