/*
 Navicat Premium Dump SQL

 Source Server         : java
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : shengwei_bookstore

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 19/04/2026 22:22:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for book_info
-- ----------------------------
create database shengwei_bookstore;
use database shegnwei_bookstore;
DROP TABLE IF EXISTS `book_info`;
CREATE TABLE `book_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图书ID',
  `book_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图书名称',
  `author` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '作者',
  `editor` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '编者',
  `publisher` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '出版社',
  `publish_date` date NULL DEFAULT NULL COMMENT '出版日期',
  `edition` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '版次',
  `print_date` date NULL DEFAULT NULL COMMENT '印刷日期',
  `isbn` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'ISBN',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '定价',
  `selling_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '售价',
  `discount` decimal(3, 2) NULL DEFAULT NULL COMMENT '优惠',
  `stock_count` int NULL DEFAULT 0 COMMENT '库存数量',
  `sales_volume` int NULL DEFAULT 0 COMMENT '销量',
  `shelf_status` tinyint NULL DEFAULT 1 COMMENT '上架状态：1上架0下架',
  `shelf_time` datetime NULL DEFAULT NULL COMMENT '上架时间',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '简介',
  `is_donation` int NULL DEFAULT 0 COMMENT '是否捐赠图书 0否1是',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category_id` ASC) USING BTREE,
  INDEX `idx_status`(`shelf_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '图书信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of book_info
-- ----------------------------
INSERT INTO `book_info` VALUES (1, '红楼梦', '曹雪芹', NULL, '人民文学出版社', '2020-01-01', '第1版', '2020-01-01', '9787020002207', 2, NULL, 49.80, NULL, 100, 0, 1, '2026-04-07 00:50:15', '/uploads/books/book_1.jpg', '中国古典文学四大名著之一', 0, '2026-04-15 13:41:05');
INSERT INTO `book_info` VALUES (2, '三国演义', '罗贯中', NULL, '人民文学出版社', '2019-05-01', '第1版', '2019-05-01', '9787020002208', 2, NULL, 59.80, NULL, 80, 0, 1, '2026-04-08 00:50:15', '/uploads/books/book_2.jpg', '三国时期的政治军事斗争', 0, '2026-04-15 13:41:05');
INSERT INTO `book_info` VALUES (3, '西游记', '吴承恩', NULL, '人民文学出版社', '2021-03-01', '第1版', '2021-03-01', '9787020002209', 2, NULL, 55.00, NULL, 90, 0, 1, '2026-04-09 00:50:15', '/uploads/books/book_3.jpg', '唐僧师徒四人西天取经的故事', 0, '2026-04-15 13:41:05');
INSERT INTO `book_info` VALUES (4, '经济学原理', '曼昆', NULL, '北京大学出版社', '2015-05-01', '第1版', '2015-05-01', '9787301255888', 6, NULL, 86.00, NULL, 55, 6, 1, '2026-04-10 00:50:15', '/uploads/books/book_4.jpg', '经济学入门经典', 0, '2026-04-15 13:42:22');
INSERT INTO `book_info` VALUES (5, '穷爸爸富爸爸', '罗伯特清崎', NULL, '四川人民出版社', '2017-09-01', '第1版', '2017-09-01', '9787220103987', 6, NULL, 39.00, NULL, 90, 5, 1, '2026-04-11 00:50:15', '/uploads/books/book_5.jpg', '理财启蒙书', 0, '2026-04-15 13:42:22');
INSERT INTO `book_info` VALUES (6, '小王子', '安托万德圣 - 埃克苏佩里', NULL, '人民文学出版社', '2000-05-01', '第1版', '2000-05-01', '9787020002212', 2, NULL, 20.00, NULL, 150, 11, 1, '2026-04-12 00:50:15', '/uploads/books/book_6.jpg', '经典儿童文学', 0, '2026-04-15 13:42:22');
INSERT INTO `book_info` VALUES (7, '百年孤独', '加西亚马尔克斯', NULL, '南海出版公司', '2011-06-01', '第1版', '2011-06-01', '9787544253994', 2, NULL, 35.00, NULL, 65, 3, 1, '2026-04-13 00:50:15', '/uploads/books/book_7.jpg', '魔幻现实主义代表作', 0, '2026-04-15 13:42:22');
INSERT INTO `book_info` VALUES (8, '霍乱时期的爱情', '加西亚马尔克斯', NULL, '南海出版公司', '2012-09-01', '第1版', '2012-09-01', '9787544258975', 2, NULL, 42.00, NULL, 50, 1, 1, '2026-04-14 00:50:15', '/uploads/books/book_8.jpg', '马尔克斯另一经典', 0, '2026-04-15 13:42:22');
INSERT INTO `book_info` VALUES (9, '用我一辈子去忘记', '柴静', NULL, '海南出版社', '2013-01-01', '第1版', '2013-01-01', '9787549529322', 3, NULL, 35.00, NULL, 75, 0, 1, '2026-04-15 00:50:15', '/uploads/books/book_9.jpg', '柴静个人成长记录', 0, '2026-04-15 13:42:22');
INSERT INTO `book_info` VALUES (10, '我们仨', '杨绛', NULL, '生活读书新知三联书店', '2003-07-01', '第1版', '2003-07-01', '9787108018809', 3, NULL, 20.00, NULL, 80, 1, 1, '2026-04-16 00:50:15', '/uploads/books/book_10.jpg', '杨绛回忆录', 0, '2026-04-15 13:42:22');
INSERT INTO `book_info` VALUES (11, '人类简史', '尤瓦尔赫拉利', NULL, '中信出版社', '2014-11-01', '第1版', '2014-11-01', '9787508647357', 4, NULL, 58.00, NULL, 60, 0, 1, '2026-04-17 00:50:15', '/uploads/books/book_11.jpg', '人类历史宏观视角', 0, '2026-04-15 13:42:22');
INSERT INTO `book_info` VALUES (23, '上下五千年', '林汉达', NULL, '少年儿童出版社', '2020-01-01', '第1版', '2020-01-15', '9787532489374', NULL, 50.00, 50.00, NULL, 1, 0, 1, '2026-04-19 06:01:55', '/uploads/books/book_5a9260278fed44ceb852cef840627a66.png', '上下五千年 上中下（全三册） \n上下五千年 精装版 ', 1, '2026-04-19 12:33:47');
INSERT INTO `book_info` VALUES (24, '平凡的世界', '路遥', NULL, '北京出版社', '2020-01-01', '第1版', '2020-01-15', '9787530220481', NULL, 0.00, 30.00, NULL, 6, 0, 1, '2026-04-19 18:06:56', '/uploads/books/book_b3369da0abb84765bfe7e872fde7c9cf.png', '平凡的世界正版原著精华版普及本\n初中生必读课外书阅读\n学生世界名著书籍\n茅盾文学奖作品小说\n现当代文学经典畅销书', 1, '2026-04-19 12:33:47');

-- ----------------------------
-- Table structure for book_review
-- ----------------------------
DROP TABLE IF EXISTS `book_review`;
CREATE TABLE `book_review`  (
  `review_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_item_id` bigint NULL DEFAULT NULL COMMENT '订单明细ID',
  `book_id` bigint NOT NULL COMMENT '图书ID',
  `member_id` bigint NULL DEFAULT NULL COMMENT '会员ID',
  `customer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户名称',
  `rating` int NULL DEFAULT 5 COMMENT '评分',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价内容',
  `images` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评价图片',
  `is_anonymous` int NULL DEFAULT 0 COMMENT '是否匿名 0否1是',
  `store_rating` int NULL DEFAULT 5 COMMENT '店铺评分',
  `business_rating` int NULL DEFAULT 5 COMMENT '商品评分',
  `service_rating` int NULL DEFAULT 5 COMMENT '服务评分',
  `audit_status` int NULL DEFAULT 0 COMMENT '审核状态 0待审核 1通过 2拒绝',
  `audit_remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核备注',
  `audit_user_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `reply_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '回复内容',
  `reply_user_id` bigint NULL DEFAULT NULL COMMENT '回复人ID',
  `reply_time` datetime NULL DEFAULT NULL COMMENT '回复时间',
  `helpful_count` int NULL DEFAULT 0 COMMENT ' helpful数量',
  `status` int NULL DEFAULT 1 COMMENT '状态 1启用0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`review_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic; COMMENT = '图书评价表'

-- ----------------------------
-- Records of book_review
-- ----------------------------
INSERT INTO `book_review` VALUES (1, 34, 6, 2, '001', 5, '非常的好,五星好评', NULL, 0, 5, 5, 5, 2, '审核通过', 1, '2026-04-19 12:44:56', NULL, NULL, NULL, 0, 1, '2026-04-19 12:44:33');

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父分类ID',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1启用0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '图书分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '??', 0, 1, 1, '2026-04-15 10:49:39');
INSERT INTO `category` VALUES (2, '??', 1, 1, 1, '2026-04-15 10:49:39');
INSERT INTO `category` VALUES (3, '??', 1, 2, 1, '2026-04-15 10:49:39');
INSERT INTO `category` VALUES (4, '??', 0, 2, 1, '2026-04-15 10:49:39');
INSERT INTO `category` VALUES (5, '???', 4, 1, 1, '2026-04-15 10:49:39');
INSERT INTO `category` VALUES (6, '??', 0, 3, 1, '2026-04-15 10:49:39');

-- ----------------------------
-- Table structure for donation_person
-- ----------------------------
DROP TABLE IF EXISTS `donation_person`;
CREATE TABLE `donation_person`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '捐赠人ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '关联用户ID',
  `id_card` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号',
  `real_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `gender` tinyint NULL DEFAULT NULL COMMENT '性别：0-女 1-男',
  `ethnicity` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '民族',
  `hometown` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '籍贯',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期',
  `education` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学历',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '住址',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话',
  `donation_count` int NULL DEFAULT 0 COMMENT '捐赠数量',
  `donation_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '捐赠金额',
  `points` int NULL DEFAULT 0 COMMENT '积分',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-待审核 1-已通过 2-已拒绝',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '爱心赠书人士表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of donation_person
-- ----------------------------
INSERT INTO `donation_person` VALUES (2, 1, '411111111111', '001', 1, '汉', '北京', '2020-06-23', '博士', NULL, NULL, 1, 55.00, 10, 1, '2026-04-16 20:30:58', '2026-04-17 09:56:11');

-- ----------------------------
-- Table structure for donation_record
-- ----------------------------
DROP TABLE IF EXISTS `donation_record`;
CREATE TABLE `donation_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '捐赠记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `book_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '书名',
  `publisher` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出版社',
  `quantity` int NULL DEFAULT 1 COMMENT '数量',
  `author` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '作者',
  `isbn` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ISBN',
  `edition` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版次',
  `binding` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '装帧',
  `language` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '语言',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '估价',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-待审核 1-已通过 2-已拒绝',
  `review_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核备注',
  `reviewer_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '捐赠记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of donation_record
-- ----------------------------
INSERT INTO `donation_record` VALUES (1, 1, '平凡的世界', '人民出版社', 1, '', '', '', '', '', 50.00, '', 1, '', 1, '2026-04-16 20:13:20', '2026-04-16 20:13:07', '2026-04-16 20:28:19');
INSERT INTO `donation_record` VALUES (2, 1, '上下五千年', '人民出版社', 1, '', '', '', '', '', 0.00, '', 1, '', 1, '2026-04-16 20:30:58', '2026-04-16 20:30:50', '2026-04-16 20:30:58');

-- ----------------------------
-- Table structure for donor_info
-- ----------------------------
DROP TABLE IF EXISTS `donor_info`;
CREATE TABLE `donor_info`  (
  `donor_id` bigint NOT NULL AUTO_INCREMENT COMMENT '捐赠人ID',
  `donor_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '捐赠人编号',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `gender` int NULL DEFAULT NULL COMMENT '性别 0女1男',
  `nation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '民族',
  `native_place` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '籍贯',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `education` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学历',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `total_books` int NULL DEFAULT 0 COMMENT '捐赠图书总数',
  `total_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '捐赠总金额',
  `total_score` int NULL DEFAULT 0 COMMENT '总积分',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `status` int NULL DEFAULT 1 COMMENT '状态 1启用0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`donor_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic; COMMENT = '捐赠人信息表'

-- ----------------------------
-- Records of donor_info
-- ----------------------------

-- ----------------------------
-- Table structure for inventory
-- ----------------------------
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory`  (
  `inventory_id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `book_id` bigint NULL DEFAULT NULL COMMENT '图书ID',
  `stock_quantity` int NULL DEFAULT NULL COMMENT '库存数量',
  `available_quantity` int NULL DEFAULT NULL COMMENT '可用数量',
  `locked_quantity` int NULL DEFAULT NULL COMMENT '锁定数量',
  `warehouse_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '仓库编码',
  `warehouse_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '仓库名称',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '存放位置',
  `min_stock` int NULL DEFAULT NULL COMMENT '最小库存',
  `max_stock` int NULL DEFAULT NULL COMMENT '最大库存',
  `stock_status` int NULL DEFAULT NULL COMMENT '库存状态',
  `last_check_time` datetime NULL DEFAULT NULL COMMENT '最后盘点时间',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`inventory_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic; COMMENT = '库存表'

-- ----------------------------
-- Records of inventory
-- ----------------------------
INSERT INTO `inventory` VALUES (1, 1, 100, 100, 0, 'DEFAULT', '主仓库', NULL, 10, 1000, 1, NULL, '2026-04-16 00:14:21', '2026-04-16 00:14:21');
INSERT INTO `inventory` VALUES (2, 2, 100, 100, 0, 'DEFAULT', '主仓库', NULL, 10, 1000, 1, NULL, '2026-04-16 00:14:21', '2026-04-16 00:14:21');
INSERT INTO `inventory` VALUES (3, 3, 100, 100, 0, 'DEFAULT', '主仓库', NULL, 10, 1000, 1, NULL, '2026-04-16 00:14:21', '2026-04-16 00:14:21');
INSERT INTO `inventory` VALUES (4, 4, 100, 100, 0, 'DEFAULT', '主仓库', NULL, 10, 1000, 1, NULL, '2026-04-16 00:14:21', '2026-04-16 00:14:21');
INSERT INTO `inventory` VALUES (5, 5, 100, 100, 0, 'DEFAULT', '主仓库', NULL, 10, 1000, 1, NULL, '2026-04-16 00:14:21', '2026-04-16 00:14:21');
INSERT INTO `inventory` VALUES (6, 6, 100, 100, 0, 'DEFAULT', '主仓库', NULL, 10, 1000, 1, NULL, '2026-04-16 00:14:21', '2026-04-16 00:14:21');
INSERT INTO `inventory` VALUES (7, 7, 100, 100, 0, 'DEFAULT', '主仓库', NULL, 10, 1000, 1, NULL, '2026-04-16 00:14:21', '2026-04-16 00:14:21');
INSERT INTO `inventory` VALUES (8, 8, 100, 100, 0, 'DEFAULT', '主仓库', NULL, 10, 1000, 1, NULL, '2026-04-16 00:14:21', '2026-04-16 00:14:21');
INSERT INTO `inventory` VALUES (9, 9, 100, 100, 0, 'DEFAULT', '主仓库', NULL, 10, 1000, 1, NULL, '2026-04-16 00:14:21', '2026-04-16 00:14:21');
INSERT INTO `inventory` VALUES (10, 10, 100, 100, 0, 'DEFAULT', '主仓库', NULL, 10, 1000, 1, NULL, '2026-04-16 00:14:21', '2026-04-16 00:14:21');
INSERT INTO `inventory` VALUES (11, 11, 94, 94, 0, 'DEFAULT', '主仓库', NULL, 10, 1000, 1, NULL, '2026-04-16 00:14:21', '2026-04-16 00:14:21');

-- ----------------------------
-- Table structure for shopping_cart
-- ----------------------------
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `book_id` bigint NOT NULL COMMENT '图书 ID',
  `quantity` int NULL DEFAULT 1 COMMENT '数量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_book`(`user_id` ASC, `book_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shopping_cart
-- ----------------------------
INSERT INTO `shopping_cart` VALUES (41, 2, 4, 1, '2026-04-17 14:03:13', '2026-04-17 14:03:13');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `menu_type` int NULL DEFAULT 1 COMMENT '菜单类型 1目录2菜单3按钮',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由路径',
  `component` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件路径',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` int NULL DEFAULT 1 COMMENT '状态 1启用0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic; COMMENT = '系统菜单表'

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '首页', 1, '/admin/home', NULL, NULL, NULL, 1, 1, '2026-04-19 11:02:52');
INSERT INTO `sys_menu` VALUES (2, 0, '图书管理', 1, '/admin/book', NULL, NULL, NULL, 2, 1, '2026-04-19 11:02:52');
INSERT INTO `sys_menu` VALUES (3, 0, '库存管理', 1, '/admin/inventory', NULL, NULL, NULL, 3, 1, '2026-04-19 11:02:52');
INSERT INTO `sys_menu` VALUES (4, 0, '订单管理', 1, '/admin/order', NULL, NULL, NULL, 4, 1, '2026-04-19 11:02:52');
INSERT INTO `sys_menu` VALUES (5, 0, '用户管理', 1, '/admin/member', NULL, NULL, NULL, 5, 1, '2026-04-19 11:02:52');
INSERT INTO `sys_menu` VALUES (6, 0, '评价管理', 1, '/admin/review', NULL, NULL, NULL, 6, 1, '2026-04-19 11:02:52');
INSERT INTO `sys_menu` VALUES (7, 0, '捐赠管理', 1, '/admin/donation-manage', NULL, NULL, NULL, 7, 1, '2026-04-19 11:02:52');
INSERT INTO `sys_menu` VALUES (8, 0, '角色管理', 1, '/admin/role', NULL, NULL, NULL, 8, 1, '2026-04-19 11:02:52');
INSERT INTO `sys_menu` VALUES (9, 0, '赠书人士管理', 1, '/admin/donor-manage', NULL, NULL, NULL, 9, 1, '2026-04-19 11:02:52');
INSERT INTO `sys_menu` VALUES (10, 0, '修改密码', 1, '/admin/password', NULL, NULL, NULL, 10, 1, '2026-04-19 11:02:52');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色描述',
  `status` int NULL DEFAULT 1 COMMENT '状态 1启用0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic; COMMENT = '系统角色表'

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'SUPER_ADMIN', '拥有所有权限', 1, '2026-04-19 11:02:52');
INSERT INTO `sys_role` VALUES (2, '店员', 'STAFF', '管理员后台操作权限', 1, '2026-04-19 11:02:52');
INSERT INTO `sys_role` VALUES (3, '用户', 'USER', '普通用户/会员', 1, '2026-04-19 11:02:52');

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic; COMMENT = '角色菜单关联表'

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `gender` int NULL DEFAULT NULL COMMENT '性别 1男2女',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '身份证号',
  `nation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '民族',
  `native_place` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '籍贯',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `education` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学历',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '住址',
  `preferences` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '喜好',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `user_type` tinyint NOT NULL DEFAULT 2 COMMENT '用户类型：1管理员2普通用户',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用0禁用',
  `member_level` tinyint NULL DEFAULT 0 COMMENT '会员等级：0-非会员 1-普通会员 2-银卡会员 3-金卡会员 4-钻石卡会员',
  `points` int NULL DEFAULT 0 COMMENT '积分',
  `total_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '累计消费金额',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '111111', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'admin@shengwei.com', '13800138000', 1, 1, 0, 0, 0.00, '2026-04-15 10:49:39', '2026-04-16 21:45:11');
INSERT INTO `sys_user` VALUES (2, '001', '111111', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'test2@test.com', '13900139001', 2, 1, 2, 1092, 1092.00, '2026-04-15 12:12:10', '2026-04-16 21:45:54');
INSERT INTO `sys_user` VALUES (5, 'teststaff', '123456', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'staff@test.com', '13800000001', 1, 1, 0, 0, 0.00, '2026-04-19 15:02:36', '2026-04-19 15:02:36');
INSERT INTO `sys_user` VALUES (6, 'dianyuan001', '111111', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '13000000002', 1, 1, 0, 0, 0.00, '2026-04-19 15:14:24', '2026-04-19 15:14:24');

-- ----------------------------
-- Table structure for trade_order
-- ----------------------------
DROP TABLE IF EXISTS `trade_order`;
CREATE TABLE `trade_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `pay_amount` decimal(10, 2) NOT NULL COMMENT '实付金额',
  `status` int NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已完成',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `discount` decimal(3, 2) NULL DEFAULT 1.00 COMMENT '折扣',
  `member_level` tinyint NULL DEFAULT 0 COMMENT '会员等级',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of trade_order
-- ----------------------------
INSERT INTO `trade_order` VALUES (18, 'ORD17763111216877745', 2, 59.00, 59.00, 1, '2026-04-16 12:27:31', '2026-04-16 11:45:22', '2026-04-16 21:45:11', 1.00, 0);
INSERT INTO `trade_order` VALUES (19, 'ORD1776313774416154', 2, 35.00, 35.00, 1, '2026-04-16 15:17:00', '2026-04-16 12:29:34', '2026-04-16 21:45:11', 1.00, 0);
INSERT INTO `trade_order` VALUES (20, 'ORD17763138379931975', 2, 78.00, 78.00, 1, '2026-04-16 12:34:50', '2026-04-16 12:30:38', '2026-04-16 21:45:11', 1.00, 0);
INSERT INTO `trade_order` VALUES (21, 'ORD17763144915388159', 2, 35.00, 35.00, 1, '2026-04-16 15:16:58', '2026-04-16 12:41:32', '2026-04-16 21:45:11', 1.00, 0);
INSERT INTO `trade_order` VALUES (22, 'ORD17763157031536059', 2, 85.00, 85.00, 1, '2026-04-16 13:08:08', '2026-04-16 13:01:43', '2026-04-16 21:45:11', 1.00, 0);
INSERT INTO `trade_order` VALUES (23, 'ORD17763166619457961', 2, 42.00, 42.00, 1, '2026-04-16 13:29:47', '2026-04-16 13:17:42', '2026-04-16 21:45:11', 1.00, 0);
INSERT INTO `trade_order` VALUES (24, 'ORD17763179301484816', 2, 85.00, 85.00, 1, '2026-04-16 15:16:55', '2026-04-16 13:38:50', '2026-04-16 21:45:11', 1.00, 0);
INSERT INTO `trade_order` VALUES (25, 'ORD1776317952960122', 2, 85.00, 85.00, 1, '2026-04-16 14:14:32', '2026-04-16 13:39:13', '2026-04-16 21:45:11', 1.00, 0);
INSERT INTO `trade_order` VALUES (26, 'ORD17763211487255076', 2, 20.00, 20.00, 1, '2026-04-16 14:36:27', '2026-04-16 14:32:29', '2026-04-16 21:45:11', 1.00, 0);
INSERT INTO `trade_order` VALUES (27, 'ORD17763217885183253', 2, 39.00, 39.00, 1, '2026-04-16 14:46:24', '2026-04-16 14:43:09', '2026-04-16 21:45:11', 1.00, 0);
INSERT INTO `trade_order` VALUES (28, 'ORD17763227125384829', 2, 85.00, 85.00, 1, '2026-04-16 15:00:07', '2026-04-16 14:58:33', '2026-04-16 21:45:11', 1.00, 0);
INSERT INTO `trade_order` VALUES (29, 'ORD17763230801397306', 2, 35.00, 35.00, 1, '2026-04-16 15:13:31', '2026-04-16 15:04:40', '2026-04-16 21:45:11', 1.00, 0);
INSERT INTO `trade_order` VALUES (30, 'ORD17763237596157430', 2, 39.00, 39.00, 1, '2026-04-16 15:16:01', '2026-04-16 15:16:00', '2026-04-16 21:45:11', 1.00, 0);
INSERT INTO `trade_order` VALUES (31, 'ORD17763238570657117', 2, 170.00, 170.00, 1, '2026-04-16 15:17:38', '2026-04-16 15:17:37', '2026-04-16 21:45:11', 1.00, 0);
INSERT INTO `trade_order` VALUES (32, 'ORD17763294355151957', 2, 85.00, 85.00, 4, '2026-04-16 16:50:37', '2026-04-16 16:50:36', '2026-04-16 21:45:11', 1.00, 0);
INSERT INTO `trade_order` VALUES (33, 'ORD17763461572471165', 2, 200.00, 200.00, 1, '2026-04-16 21:29:19', '2026-04-16 21:29:17', '2026-04-16 21:45:11', 1.00, 0);

-- ----------------------------
-- Table structure for trade_order_item
-- ----------------------------
DROP TABLE IF EXISTS `trade_order_item`;
CREATE TABLE `trade_order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` bigint NOT NULL COMMENT '订单 ID',
  `book_id` bigint NOT NULL COMMENT '图书 ID',
  `book_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图书名称',
  `price` decimal(10, 2) NOT NULL COMMENT '单价',
  `quantity` int NOT NULL COMMENT '数量',
  `subtotal` decimal(10, 2) NOT NULL COMMENT '小计金额',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of trade_order_item
-- ----------------------------
INSERT INTO `trade_order_item` VALUES (1, 1, 13, '小王子', 20.00, 1, 20.00, '2026-04-15 23:16:34');
INSERT INTO `trade_order_item` VALUES (2, 2, 13, '小王子', 20.00, 1, 20.00, '2026-04-15 23:16:53');
INSERT INTO `trade_order_item` VALUES (3, 3, 1, '红楼梦', 49.80, 1, 49.80, '2026-04-15 23:18:33');
INSERT INTO `trade_order_item` VALUES (4, 4, 13, '小王子', 20.00, 1, 20.00, '2026-04-15 23:20:23');
INSERT INTO `trade_order_item` VALUES (5, 5, 12, '穷爸爸富爸爸', 39.00, 1, 39.00, '2026-04-15 23:21:17');
INSERT INTO `trade_order_item` VALUES (6, 6, 13, '小王子', 20.00, 1, 20.00, '2026-04-15 23:23:08');
INSERT INTO `trade_order_item` VALUES (7, 7, 13, '小王子', 20.00, 1, 20.00, '2026-04-15 23:40:25');
INSERT INTO `trade_order_item` VALUES (8, 8, 13, '小王子', 20.00, 1, 20.00, '2026-04-15 23:40:40');
INSERT INTO `trade_order_item` VALUES (9, 9, 13, '小王子', 20.00, 1, 20.00, '2026-04-15 23:40:51');
INSERT INTO `trade_order_item` VALUES (10, 10, 11, '经济学原理', 85.00, 3, 255.00, '2026-04-15 23:41:27');
INSERT INTO `trade_order_item` VALUES (11, 11, 13, '小王子', 20.00, 1, 20.00, '2026-04-15 23:46:30');
INSERT INTO `trade_order_item` VALUES (12, 12, 13, '小王子', 20.00, 1, 20.00, '2026-04-15 23:47:42');
INSERT INTO `trade_order_item` VALUES (13, 13, 13, '小王子', 20.00, 1, 20.00, '2026-04-16 00:00:59');
INSERT INTO `trade_order_item` VALUES (14, 14, 13, '小王子', 20.00, 1, 20.00, '2026-04-16 00:17:10');
INSERT INTO `trade_order_item` VALUES (15, 15, 13, '小王子', 20.00, 1, 20.00, '2026-04-16 10:29:25');
INSERT INTO `trade_order_item` VALUES (16, 16, 13, '小王子', 20.00, 1, 20.00, '2026-04-16 11:25:16');
INSERT INTO `trade_order_item` VALUES (17, 17, 1, '红楼梦', 49.80, 1, 49.80, '2026-04-16 11:30:02');
INSERT INTO `trade_order_item` VALUES (18, 18, 13, '小王子', 20.00, 1, 20.00, '2026-04-16 11:45:22');
INSERT INTO `trade_order_item` VALUES (19, 18, 12, '穷爸爸富爸爸', 39.00, 1, 39.00, '2026-04-16 11:45:22');
INSERT INTO `trade_order_item` VALUES (20, 19, 14, '百年孤独', 35.00, 1, 35.00, '2026-04-16 12:29:34');
INSERT INTO `trade_order_item` VALUES (21, 20, 12, '穷爸爸富爸爸', 39.00, 2, 78.00, '2026-04-16 12:30:38');
INSERT INTO `trade_order_item` VALUES (22, 21, 14, '百年孤独', 35.00, 1, 35.00, '2026-04-16 12:41:32');
INSERT INTO `trade_order_item` VALUES (23, 22, 11, '经济学原理', 85.00, 1, 85.00, '2026-04-16 13:01:43');
INSERT INTO `trade_order_item` VALUES (24, 23, 15, '霍乱时期的爱情', 42.00, 1, 42.00, '2026-04-16 13:17:42');
INSERT INTO `trade_order_item` VALUES (25, 24, 11, '经济学原理', 85.00, 1, 85.00, '2026-04-16 13:38:50');
INSERT INTO `trade_order_item` VALUES (26, 25, 11, '经济学原理', 85.00, 1, 85.00, '2026-04-16 13:39:13');
INSERT INTO `trade_order_item` VALUES (27, 26, 17, '我们仨', 20.00, 1, 20.00, '2026-04-16 14:32:29');
INSERT INTO `trade_order_item` VALUES (28, 27, 12, '穷爸爸富爸爸', 39.00, 1, 39.00, '2026-04-16 14:43:09');
INSERT INTO `trade_order_item` VALUES (29, 28, 11, '经济学原理', 85.00, 1, 85.00, '2026-04-16 14:58:33');
INSERT INTO `trade_order_item` VALUES (30, 29, 14, '百年孤独', 35.00, 1, 35.00, '2026-04-16 15:04:40');
INSERT INTO `trade_order_item` VALUES (31, 30, 12, '穷爸爸富爸爸', 39.00, 1, 39.00, '2026-04-16 15:16:00');
INSERT INTO `trade_order_item` VALUES (32, 31, 11, '经济学原理', 85.00, 2, 170.00, '2026-04-16 15:17:37');
INSERT INTO `trade_order_item` VALUES (33, 32, 11, '经济学原理', 85.00, 1, 85.00, '2026-04-16 16:50:36');
INSERT INTO `trade_order_item` VALUES (34, 33, 13, '小王子', 20.00, 10, 200.00, '2026-04-16 21:29:17');

-- ----------------------------
-- Table structure for trade_order_refund
-- ----------------------------
DROP TABLE IF EXISTS `trade_order_refund`;
CREATE TABLE `trade_order_refund`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '退款ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '退款金额',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '退款原因',
  `status` tinyint NULL DEFAULT 1 COMMENT '退款状态:0-无 1-审核中 2-已退款 3-已拒绝',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理备注',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单退款表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of trade_order_refund
-- ----------------------------
INSERT INTO `trade_order_refund` VALUES (1, 'ORD17763294355151957', 1, 85.00, '111', 2, '', '2026-04-16 17:14:52', '2026-04-16 17:13:57', '2026-04-16 17:14:52');

SET FOREIGN_KEY_CHECKS = 1;
