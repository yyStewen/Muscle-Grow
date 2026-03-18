-- MySQL dump 10.13  Distrib 9.5.0, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: muscle_grow
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ 'ce1b8b6c-ae8f-11f0-9832-30560f03bbd4:1-6210';

--
-- Table structure for table `address_book`
--

DROP TABLE IF EXISTS `address_book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address_book` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `consignee` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '收货人',
  `sex` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '性别',
  `phone` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '手机号',
  `province_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '省级区划编号',
  `province_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '省级名称',
  `city_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '市级区划编号',
  `city_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '市级名称',
  `district_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '区级区划编号',
  `district_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '区级名称',
  `detail` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '详细地址',
  `label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '标签',
  `is_default` tinyint NOT NULL DEFAULT '0' COMMENT '默认 0 否 1是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='地址簿';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address_book`
--

LOCK TABLES `address_book` WRITE;
/*!40000 ALTER TABLE `address_book` DISABLE KEYS */;
INSERT INTO `address_book` VALUES (5,7,'张三','1','13456789001','44','广东省','4401','广州市','440106','天河区','中山大道西55号','Home',1);
/*!40000 ALTER TABLE `address_book` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` int DEFAULT NULL COMMENT '类型   1 补剂分类 2 套餐分类',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '顺序',
  `status` int DEFAULT NULL COMMENT '分类状态 0:禁用，1:启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_category_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='补剂及套餐分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,1,'优质矿维',2,1,'2022-06-09 22:09:18','2026-03-14 03:03:11',1,1),(2,1,'增肌蛋白',9,1,'2022-06-09 22:09:32','2022-06-09 22:18:53',1,1),(3,2,'人气套餐',12,1,'2022-06-09 22:11:38','2026-02-17 18:29:54',1,1),(4,2,'商务套餐',1,1,'2022-06-09 22:14:10','2026-03-14 03:05:41',1,1);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employee`
--

DROP TABLE IF EXISTS `employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '姓名',
  `username` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '用户名',
  `password` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '密码',
  `phone` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '手机号',
  `sex` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '性别',
  `id_number` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '身份证号',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态 0:禁用，1:启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='员工信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employee`
--

LOCK TABLES `employee` WRITE;
/*!40000 ALTER TABLE `employee` DISABLE KEYS */;
INSERT INTO `employee` VALUES (1,'管理员','admin','3c0ff35087f9d25fecea83f7432b3afd','13812312312','1','110101199001010047',1,'2022-02-15 15:51:20','2022-02-17 09:16:20',10,1),(7,'李四五','lisi','e10adc3949ba59abbe56e057f20f883e','13356783434','1','440905200410100431',1,'2026-02-15 13:56:31','2026-03-13 03:01:07',1,NULL),(8,'张三','zhangsan','e10adc3949ba59abbe56e057f20f883e','13456789001','1','440903200010080431',1,'2026-03-13 02:53:24','2026-03-13 02:53:24',NULL,NULL);
/*!40000 ALTER TABLE `employee` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_detail`
--

DROP TABLE IF EXISTS `order_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '名字',
  `image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图片',
  `order_id` bigint NOT NULL COMMENT '订单id',
  `supplement_id` bigint DEFAULT NULL COMMENT '补剂id',
  `setmeal_id` bigint DEFAULT NULL COMMENT '套餐id',
  `voucher_id` bigint DEFAULT NULL COMMENT '关联的优惠券id',
  `supplement_detail` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '补剂的规格等详细信息',
  `number` int NOT NULL DEFAULT '1' COMMENT '数量',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='订单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_detail`
--

LOCK TABLES `order_detail` WRITE;
/*!40000 ALTER TABLE `order_detail` DISABLE KEYS */;
INSERT INTO `order_detail` VALUES (79,'锌镁片','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/8778f677-86d5-4cab-a2e6-ba964194a399.png',26,68,NULL,NULL,NULL,1,4.00),(80,'复合维生素','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/cb7a9b17-5d47-4282-bcad-5f44366ace92.png',26,67,NULL,NULL,'包装: 罐装',1,70.00),(81,'蛋白质套餐','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/e1591ae9-223e-4fc3-af67-3ea4b6aeca69.jpg',26,NULL,36,NULL,NULL,1,200.00),(82,'锌镁片','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/8778f677-86d5-4cab-a2e6-ba964194a399.png',27,68,NULL,NULL,NULL,1,4.00),(83,'复合维生素','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/cb7a9b17-5d47-4282-bcad-5f44366ace92.png',27,67,NULL,NULL,'包装: 罐装',1,70.00),(84,'矿维套餐','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/0bf2c1bb-7c9a-4716-8ab3-cca271002685.jpg',28,NULL,37,NULL,NULL,1,220.00);
/*!40000 ALTER TABLE `order_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `number` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '订单号',
  `status` int NOT NULL DEFAULT '1' COMMENT '订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7退款',
  `user_id` bigint NOT NULL COMMENT '下单用户',
  `address_book_id` bigint NOT NULL COMMENT '地址id',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `checkout_time` datetime DEFAULT NULL COMMENT '结账时间',
  `pay_method` int NOT NULL DEFAULT '1' COMMENT '支付方式 1微信,2支付宝',
  `pay_status` tinyint NOT NULL DEFAULT '0' COMMENT '支付状态 0未支付 1已支付 2退款',
  `amount` decimal(10,2) NOT NULL COMMENT '实收金额',
  `remark` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '备注',
  `phone` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '手机号',
  `address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '地址',
  `user_name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '用户名称',
  `consignee` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '收货人',
  `cancel_reason` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '订单取消原因',
  `rejection_reason` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '订单拒绝原因',
  `cancel_time` datetime DEFAULT NULL COMMENT '订单取消时间',
  `estimated_delivery_time` datetime DEFAULT NULL COMMENT '预计送达时间',
  `delivery_time` datetime DEFAULT NULL COMMENT '送达时间',
  `mail_amount` int DEFAULT NULL COMMENT '运费',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (26,'1773801212766',5,7,5,'2026-03-18 10:33:33','2026-03-18 10:33:37',1,1,274.00,'希望早日送达','13456789001','广东省广州市天河区中山大道西55号','steven','张三',NULL,NULL,NULL,'2026-03-18 11:33:11','2026-03-18 10:34:21',0),(27,'1773802048897',6,7,5,'2026-03-18 10:47:29','2026-03-18 10:47:53',1,2,74.00,'','13456789001','广东省广州市天河区中山大道西55号','steven','张三',NULL,'库存不足','2026-03-18 10:48:25','2026-03-18 11:47:21',NULL,0),(28,'1773802142622',5,7,5,'2026-03-18 10:49:03','2026-03-18 10:49:15',1,1,220.00,'','13456789001','广东省广州市天河区中山大道西55号','steven','张三',NULL,NULL,NULL,'2026-03-18 11:48:56','2026-03-18 11:07:20',0);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `setmeal`
--

DROP TABLE IF EXISTS `setmeal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `setmeal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category_id` bigint NOT NULL COMMENT '补剂分类id',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '套餐名称',
  `price` decimal(10,2) NOT NULL COMMENT '套餐价格',
  `status` int DEFAULT '1' COMMENT '售卖状态 0:停售 1:起售',
  `description` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '描述信息',
  `image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图片',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_setmeal_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='套餐';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `setmeal`
--

LOCK TABLES `setmeal` WRITE;
/*!40000 ALTER TABLE `setmeal` DISABLE KEYS */;
INSERT INTO `setmeal` VALUES (36,4,'蛋白质套餐',200.00,1,'蛋白粉和复合维生素，促进肌肉增长','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/e1591ae9-223e-4fc3-af67-3ea4b6aeca69.jpg','2026-03-16 19:20:44','2026-03-17 13:03:41',1,1),(37,3,'矿维套餐',220.00,1,'蛋白粉和复合维生素，促进肌肉增长，以及身体的恢复','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/0bf2c1bb-7c9a-4716-8ab3-cca271002685.jpg','2026-03-16 19:23:26','2026-03-17 13:03:39',1,1);
/*!40000 ALTER TABLE `setmeal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `setmeal_supplement`
--

DROP TABLE IF EXISTS `setmeal_supplement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `setmeal_supplement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `setmeal_id` bigint DEFAULT NULL COMMENT '套餐id',
  `supplement_id` bigint DEFAULT NULL COMMENT '补剂id',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '菜品名称 （冗余字段）',
  `price` decimal(10,2) DEFAULT NULL COMMENT '菜品单价（冗余字段）',
  `copies` int DEFAULT NULL COMMENT '补剂份数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='套餐补剂关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `setmeal_supplement`
--

LOCK TABLES `setmeal_supplement` WRITE;
/*!40000 ALTER TABLE `setmeal_supplement` DISABLE KEYS */;
INSERT INTO `setmeal_supplement` VALUES (65,36,77,'MyProtein蛋白粉',114.00,1),(66,36,75,'康比特香草味乳清蛋白粉',30.00,1),(67,36,67,'复合维生素',70.00,1),(73,37,68,'锌镁片',4.00,3),(74,37,67,'复合维生素',70.00,2),(75,37,77,'MyProtein蛋白粉',114.00,1);
/*!40000 ALTER TABLE `setmeal_supplement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shopping_cart`
--

DROP TABLE IF EXISTS `shopping_cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shopping_cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '商品名称',
  `image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图片',
  `user_id` bigint NOT NULL COMMENT '主键',
  `supplement_id` bigint DEFAULT NULL COMMENT '补剂id',
  `setmeal_id` bigint DEFAULT NULL COMMENT '套餐id',
  `supplement_detail` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '补剂详细信息，规格',
  `number` int NOT NULL DEFAULT '1' COMMENT '数量',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='购物车';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shopping_cart`
--

LOCK TABLES `shopping_cart` WRITE;
/*!40000 ALTER TABLE `shopping_cart` DISABLE KEYS */;
/*!40000 ALTER TABLE `shopping_cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `supplement`
--

DROP TABLE IF EXISTS `supplement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '补剂名称',
  `category_id` bigint NOT NULL COMMENT '补剂分类id',
  `price` decimal(10,2) DEFAULT NULL COMMENT '补剂价格',
  `image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图片',
  `description` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '描述信息',
  `status` int DEFAULT '1' COMMENT '0 停售 1 起售',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_supplement_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='补剂';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplement`
--

LOCK TABLES `supplement` WRITE;
/*!40000 ALTER TABLE `supplement` DISABLE KEYS */;
INSERT INTO `supplement` VALUES (67,'复合维生素',1,70.00,'https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/cb7a9b17-5d47-4282-bcad-5f44366ace92.png','复合维生素片，补充人体必需维生素',1,'2022-06-10 10:43:56','2026-03-16 20:31:46',1,1),(68,'锌镁片',1,4.00,'https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/8778f677-86d5-4cab-a2e6-ba964194a399.png','促进睡眠，促进神经和肌肉的运动恢复',1,'2022-06-10 10:54:25','2026-02-17 03:48:08',1,1),(69,'on双重巧克力味乳清蛋白粉',2,6.00,'https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/b93aceda-b488-4c19-a952-7b6b76116d73.png','优质乳清蛋白',1,'2022-06-10 10:55:02','2026-02-17 02:46:48',1,1),(75,'康比特香草味乳清蛋白粉',2,30.00,'https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/5212b392-4cd5-4dea-a881-51cb5c680553.png','优质乳清蛋白',1,'2026-02-17 17:17:01','2026-02-17 18:20:48',1,1),(77,'MyProtein蛋白粉',2,114.00,'https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/9c7ae1af-386c-40a6-96f1-cd6d7ca7dd20.webp','高质量乳清蛋白',1,'2026-03-16 11:43:59','2026-03-16 11:43:59',1,1);
/*!40000 ALTER TABLE `supplement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `supplement_detail`
--

DROP TABLE IF EXISTS `supplement_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplement_detail` (
  `supplement_id` bigint NOT NULL COMMENT '补剂id',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '补剂细节的名称，确定购买的补剂品种和规格',
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `value` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '补剂详细信息数据list',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=118 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='补品和补品的相关细节信息关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplement_detail`
--

LOCK TABLES `supplement_detail` WRITE;
/*!40000 ALTER TABLE `supplement_detail` DISABLE KEYS */;
INSERT INTO `supplement_detail` VALUES (77,'容量',116,'[\"2磅\"]'),(67,'包装',117,'[\"罐装\"]');
/*!40000 ALTER TABLE `supplement_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `password` varchar(100) COLLATE utf8mb3_bin NOT NULL COMMENT '登录密码，建议保存加密串',
  `name` varchar(32) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '昵称/姓名',
  `phone` varchar(11) COLLATE utf8mb3_bin NOT NULL COMMENT '手机号',
  `sex` varchar(2) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '性别',
  `id_number` varchar(18) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '身份证号',
  `avatar` varchar(500) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '头像',
  `status` int NOT NULL DEFAULT '1' COMMENT '账号状态 1正常 0禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='用户信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (7,'3c0ff35087f9d25fecea83f7432b3afd','steven','13456789001',NULL,NULL,NULL,1,'2026-03-17 17:25:02','2026-03-17 17:25:02'),(8,'e10adc3949ba59abbe56e057f20f883e','lyy','15363143898',NULL,NULL,NULL,1,'2026-03-17 18:37:08','2026-03-17 18:37:08');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `voucher`
--

DROP TABLE IF EXISTS `voucher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `voucher` (
  `title` varchar(255) NOT NULL COMMENT '优惠券标题',
  `pay_value` decimal(10,2) NOT NULL COMMENT '用户为购买该优惠券需要支付的价格。单位：元',
  `actual_value` decimal(10,2) NOT NULL COMMENT '使用该优惠券能够抵扣的金额。单位：元',
  `stock` int NOT NULL COMMENT '该种优惠券的库存',
  `begin_time` datetime DEFAULT NULL COMMENT '秒杀开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '秒杀结束时间',
  `status` tinyint NOT NULL COMMENT '优惠券状态：1：投放中，2：已结束，3：优惠券下架',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，优惠券id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀券数据库表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `voucher`
--

LOCK TABLES `voucher` WRITE;
/*!40000 ALTER TABLE `voucher` DISABLE KEYS */;
/*!40000 ALTER TABLE `voucher` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `voucher_storage`
--

DROP TABLE IF EXISTS `voucher_storage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `voucher_storage` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '所属用户id',
  `voucher_id` bigint NOT NULL COMMENT '关联的优惠券id',
  `order_id` bigint DEFAULT NULL COMMENT '使用该券的补剂订单id',
  `name` varchar(255) DEFAULT NULL COMMENT '优惠券名称（冗余存储，减少联表查询）',
  `actual_value` decimal(10,2) NOT NULL COMMENT '优惠券抵扣金额',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-未使用，2-已使用，3-已过期，4-锁定中',
  `create_time` datetime DEFAULT NULL COMMENT '领取/购买时间',
  `use_time` datetime DEFAULT NULL COMMENT '使用时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户优惠券仓库表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `voucher_storage`
--

LOCK TABLES `voucher_storage` WRITE;
/*!40000 ALTER TABLE `voucher_storage` DISABLE KEYS */;
/*!40000 ALTER TABLE `voucher_storage` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-18 22:46:11
