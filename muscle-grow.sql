-- MySQL dump 10.13  Distrib 9.5.0, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: musule_grow
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

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ 'ce1b8b6c-ae8f-11f0-9832-30560f03bbd4:1-6118';

--
-- Table structure for table `address_book`
--

DROP TABLE IF EXISTS `address_book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address_book` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `consignee` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '收货人',
  `sex` varchar(2) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '性别',
  `phone` varchar(11) COLLATE utf8mb3_bin NOT NULL COMMENT '手机号',
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='地址簿';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address_book`
--

LOCK TABLES `address_book` WRITE;
/*!40000 ALTER TABLE `address_book` DISABLE KEYS */;
INSERT INTO `address_book` VALUES (2,6,'梁耀匀','0','15311111234','44','广东省','4409','茂名市','440904','电白区','光华北路隔坑村51号','3',1);
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
  `name` varchar(32) COLLATE utf8mb3_bin NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '顺序',
  `status` int DEFAULT NULL COMMENT '分类状态 0:禁用，1:启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_category_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='补剂及套餐分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
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
  `name` varchar(32) COLLATE utf8mb3_bin NOT NULL COMMENT '姓名',
  `username` varchar(32) COLLATE utf8mb3_bin NOT NULL COMMENT '用户名',
  `password` varchar(64) COLLATE utf8mb3_bin NOT NULL COMMENT '密码',
  `phone` varchar(11) COLLATE utf8mb3_bin NOT NULL COMMENT '手机号',
  `sex` varchar(2) COLLATE utf8mb3_bin NOT NULL COMMENT '性别',
  `id_number` varchar(18) COLLATE utf8mb3_bin NOT NULL COMMENT '身份证号',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态 0:禁用，1:启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='员工信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employee`
--

LOCK TABLES `employee` WRITE;
/*!40000 ALTER TABLE `employee` DISABLE KEYS */;
INSERT INTO `employee` VALUES (1,'管理员','admin','e10adc3949ba59abbe56e057f20f883e','13812312312','1','110101199001010047',1,'2022-02-15 15:51:20','2022-02-17 09:16:20',10,1),(7,'李四五','lisi','e10adc3949ba59abbe56e057f20f883e','13356783434','1','440905200410100431',1,'2026-02-15 13:56:31','2026-02-16 19:48:01',1,1);
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
  `name` varchar(32) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '名字',
  `image` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图片',
  `order_id` bigint NOT NULL COMMENT '订单id',
  `supplement_id` bigint DEFAULT NULL COMMENT '补剂id',
  `setmeal_id` bigint DEFAULT NULL COMMENT '套餐id',
  `voucher_id` bigint DEFAULT NULL COMMENT '关联的优惠券id',
  `supplement_detail` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '补剂的规格等详细信息',
  `number` int NOT NULL DEFAULT '1' COMMENT '数量',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='订单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_detail`
--

LOCK TABLES `order_detail` WRITE;
/*!40000 ALTER TABLE `order_detail` DISABLE KEYS */;
INSERT INTO `order_detail` VALUES (14,'江团鱼2斤','https://sky-itcast.oss-cn-beijing.aliyuncs.com/a101a1e9-8f8b-47b2-afa4-1abd47ea0a87.png',8,66,NULL,NULL,'中辣',1,119.00),(15,'鮰鱼2斤','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/cb7a9b17-5d47-4282-bcad-5f44366ace92.png',8,67,NULL,NULL,'不辣',1,70.00),(16,'江团鱼2斤','https://sky-itcast.oss-cn-beijing.aliyuncs.com/a101a1e9-8f8b-47b2-afa4-1abd47ea0a87.png',9,66,NULL,NULL,'中辣',1,119.00),(17,'鮰鱼2斤','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/cb7a9b17-5d47-4282-bcad-5f44366ace92.png',9,67,NULL,NULL,'不辣',1,70.00),(18,'梅菜扣肉','https://sky-itcast.oss-cn-beijing.aliyuncs.com/6080b118-e30a-4577-aab4-45042e3f88be.png',10,60,NULL,NULL,'不要葱',1,58.00),(19,'白切鸡','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/5212b392-4cd5-4dea-a881-51cb5c680553.png',10,75,NULL,NULL,'不要葱',1,30.00),(20,'梅菜扣肉','https://sky-itcast.oss-cn-beijing.aliyuncs.com/6080b118-e30a-4577-aab4-45042e3f88be.png',11,60,NULL,NULL,'不要葱',1,58.00),(21,'白切鸡','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/5212b392-4cd5-4dea-a881-51cb5c680553.png',11,75,NULL,NULL,'不要葱',1,30.00),(22,'梅菜扣肉','https://sky-itcast.oss-cn-beijing.aliyuncs.com/6080b118-e30a-4577-aab4-45042e3f88be.png',12,60,NULL,NULL,'不要葱',1,58.00),(23,'白切鸡','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/5212b392-4cd5-4dea-a881-51cb5c680553.png',12,75,NULL,NULL,'不要葱',1,30.00),(24,'草鱼2斤','https://sky-itcast.oss-cn-beijing.aliyuncs.com/b544d3ba-a1ae-4d20-a860-81cb5dec9e03.png',13,65,NULL,NULL,'不辣',1,68.00),(25,'鮰鱼2斤','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/cb7a9b17-5d47-4282-bcad-5f44366ace92.png',13,67,NULL,NULL,'不辣',1,70.00),(26,'草鱼2斤','https://sky-itcast.oss-cn-beijing.aliyuncs.com/b544d3ba-a1ae-4d20-a860-81cb5dec9e03.png',14,65,NULL,NULL,'不辣',1,68.00),(27,'鮰鱼2斤','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/cb7a9b17-5d47-4282-bcad-5f44366ace92.png',14,67,NULL,NULL,'不辣',1,70.00),(28,'梅菜扣肉','https://sky-itcast.oss-cn-beijing.aliyuncs.com/6080b118-e30a-4577-aab4-45042e3f88be.png',15,60,NULL,NULL,'不要辣',1,58.00),(29,'剁椒鱼头','https://sky-itcast.oss-cn-beijing.aliyuncs.com/13da832f-ef2c-484d-8370-5934a1045a06.png',15,61,NULL,NULL,NULL,1,66.00),(30,'老坛酸菜鱼','https://sky-itcast.oss-cn-beijing.aliyuncs.com/4a9cefba-6a74-467e-9fde-6e687ea725d7.png',16,51,NULL,NULL,'不要葱,重辣',1,56.00),(31,'经典酸菜鮰鱼','https://sky-itcast.oss-cn-beijing.aliyuncs.com/5260ff39-986c-4a97-8850-2ec8c7583efc.png',16,52,NULL,NULL,'不要葱,不辣',1,66.00),(32,'草鱼2斤','https://sky-itcast.oss-cn-beijing.aliyuncs.com/b544d3ba-a1ae-4d20-a860-81cb5dec9e03.png',17,65,NULL,NULL,'不辣',1,68.00),(33,'江团鱼2斤','https://sky-itcast.oss-cn-beijing.aliyuncs.com/a101a1e9-8f8b-47b2-afa4-1abd47ea0a87.png',17,66,NULL,NULL,'不辣',1,119.00),(34,'鮰鱼2斤','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/cb7a9b17-5d47-4282-bcad-5f44366ace92.png',17,67,NULL,NULL,'不辣',1,70.00),(35,'江团鱼2斤','https://sky-itcast.oss-cn-beijing.aliyuncs.com/a101a1e9-8f8b-47b2-afa4-1abd47ea0a87.png',18,66,NULL,NULL,'不辣',1,119.00),(36,'鮰鱼2斤','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/cb7a9b17-5d47-4282-bcad-5f44366ace92.png',18,67,NULL,NULL,'不辣',1,70.00),(37,'江团鱼2斤','https://sky-itcast.oss-cn-beijing.aliyuncs.com/a101a1e9-8f8b-47b2-afa4-1abd47ea0a87.png',19,66,NULL,NULL,'不辣',1,119.00),(38,'鮰鱼2斤','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/cb7a9b17-5d47-4282-bcad-5f44366ace92.png',19,67,NULL,NULL,'不辣',1,70.00),(39,'清炒西兰花','https://sky-itcast.oss-cn-beijing.aliyuncs.com/e9ec4ba4-4b22-4fc8-9be0-4946e6aeb937.png',20,56,NULL,NULL,'不要葱',1,18.00),(40,'炝炒圆白菜','https://sky-itcast.oss-cn-beijing.aliyuncs.com/22f59feb-0d44-430e-a6cd-6a49f27453ca.png',20,57,NULL,NULL,'不要葱',1,18.00),(41,'草鱼2斤','https://sky-itcast.oss-cn-beijing.aliyuncs.com/b544d3ba-a1ae-4d20-a860-81cb5dec9e03.png',21,65,NULL,NULL,'不辣',1,68.00),(42,'鮰鱼2斤','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/cb7a9b17-5d47-4282-bcad-5f44366ace92.png',21,67,NULL,NULL,'不辣',1,70.00),(43,'江团鱼2斤','https://sky-itcast.oss-cn-beijing.aliyuncs.com/a101a1e9-8f8b-47b2-afa4-1abd47ea0a87.png',22,66,NULL,NULL,'不辣',1,119.00),(44,'鮰鱼2斤','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/cb7a9b17-5d47-4282-bcad-5f44366ace92.png',22,67,NULL,NULL,'不辣',1,70.00),(45,'江团鱼2斤','https://sky-itcast.oss-cn-beijing.aliyuncs.com/a101a1e9-8f8b-47b2-afa4-1abd47ea0a87.png',23,66,NULL,NULL,'不辣',1,119.00),(46,'鮰鱼2斤','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/cb7a9b17-5d47-4282-bcad-5f44366ace92.png',23,67,NULL,NULL,'不辣',1,70.00),(47,'梅菜扣肉','https://sky-itcast.oss-cn-beijing.aliyuncs.com/6080b118-e30a-4577-aab4-45042e3f88be.png',24,60,NULL,NULL,'不要葱',1,58.00),(48,'白切鸡','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/5212b392-4cd5-4dea-a881-51cb5c680553.png',24,75,NULL,NULL,'不要葱',1,30.00),(49,'梅菜扣肉','https://sky-itcast.oss-cn-beijing.aliyuncs.com/6080b118-e30a-4577-aab4-45042e3f88be.png',25,60,NULL,NULL,'不要葱',1,58.00),(50,'白切鸡','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/5212b392-4cd5-4dea-a881-51cb5c680553.png',25,75,NULL,NULL,'不要葱',1,30.00);
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
  `number` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '订单号',
  `status` int NOT NULL DEFAULT '1' COMMENT '订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7退款',
  `user_id` bigint NOT NULL COMMENT '下单用户',
  `address_book_id` bigint NOT NULL COMMENT '地址id',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `checkout_time` datetime DEFAULT NULL COMMENT '结账时间',
  `pay_method` int NOT NULL DEFAULT '1' COMMENT '支付方式 1微信,2支付宝',
  `pay_status` tinyint NOT NULL DEFAULT '0' COMMENT '支付状态 0未支付 1已支付 2退款',
  `amount` decimal(10,2) NOT NULL COMMENT '实收金额',
  `remark` varchar(100) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '备注',
  `phone` varchar(11) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '手机号',
  `address` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '地址',
  `user_name` varchar(32) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '用户名称',
  `consignee` varchar(32) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '收货人',
  `cancel_reason` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '订单取消原因',
  `rejection_reason` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '订单拒绝原因',
  `cancel_time` datetime DEFAULT NULL COMMENT '订单取消时间',
  `estimated_delivery_time` datetime DEFAULT NULL COMMENT '预计送达时间',
  `delivery_time` datetime DEFAULT NULL COMMENT '送达时间',
  `mail_amount` int DEFAULT NULL COMMENT '运费',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (8,'1771749609359',6,6,2,'2026-02-22 16:40:09','2026-02-22 17:12:42',1,1,197.00,'多给额外酱料','15311111234','电白区海心路八号',NULL,'梁耀匀',NULL,NULL,NULL,'2026-02-22 17:40:00',NULL,2),(9,'1771752864404',6,6,2,'2026-02-22 17:34:24','2026-02-22 17:35:01',1,1,197.00,'多给额外酱料','15311111234','电白区海心路八号',NULL,'梁耀匀',NULL,NULL,NULL,'2026-02-22 17:40:00',NULL,2),(10,'1771753113586',6,6,2,'2026-02-22 17:38:34','2026-02-22 17:39:13',1,1,96.00,'','15311111234','电白区海心路八号',NULL,'梁耀匀',NULL,NULL,NULL,'2026-02-22 18:38:00',NULL,2),(11,'1771754269982',6,6,2,'2026-02-22 17:57:50','2026-02-22 17:58:09',1,1,96.00,'','15311111234','电白区海心路八号',NULL,'梁耀匀',NULL,NULL,NULL,'2026-02-22 18:57:00',NULL,2),(12,'1771765436982',5,6,2,'2026-02-22 21:03:57','2026-02-22 21:03:59',1,1,96.00,'','15311111234','电白区海心路八号',NULL,'梁耀匀',NULL,NULL,NULL,'2026-02-22 22:03:00',NULL,2),(13,'1771775131865',6,6,2,'2026-02-22 23:45:32','2026-02-22 23:45:34',1,1,146.00,'','15311111234','电白区海心路八号',NULL,'梁耀匀',NULL,'菜品已销售完，暂时无法接单',NULL,'2026-02-22 00:45:00',NULL,2),(14,'1771775923662',5,6,2,'2026-02-22 23:58:44','2026-02-22 23:58:46',1,1,146.00,'','15311111234','电白区海心路八号',NULL,'梁耀匀',NULL,NULL,NULL,'2026-02-22 00:58:00',NULL,2),(15,'1771776127973',6,6,2,'2026-02-23 00:02:08','2026-02-23 00:02:10',1,1,132.00,'','15311111234','电白区海心路八号',NULL,'梁耀匀','客户电话取消',NULL,'2026-02-23 00:12:40','2026-02-23 01:02:00','2026-02-23 00:02:37',2),(16,'1771776781656',5,6,2,'2026-02-23 00:13:02','2026-02-23 00:13:05',1,1,130.00,'','15311111234','电白区海心路八号',NULL,'梁耀匀',NULL,NULL,NULL,'2026-02-23 01:12:00','2026-02-23 00:13:27',2),(17,'1771838176261',2,6,2,'2026-02-23 17:16:16','2026-02-23 17:16:42',1,1,266.00,'','15311111234','光华北路隔坑村51号',NULL,'梁耀匀',NULL,NULL,NULL,'2026-02-23 18:16:00',NULL,3),(18,'1771838262145',2,6,2,'2026-02-23 17:17:42','2026-02-23 17:17:44',1,1,197.00,'','15311111234','光华北路隔坑村51号',NULL,'梁耀匀',NULL,NULL,NULL,'2026-02-23 18:17:00',NULL,2),(19,'1771838478756',2,6,2,'2026-02-23 17:21:19','2026-02-23 17:21:21',1,1,197.00,'','15311111234','光华北路隔坑村51号',NULL,'梁耀匀',NULL,NULL,NULL,'2026-02-23 18:21:00',NULL,2),(20,'1771838707430',2,6,2,'2026-02-23 17:25:07','2026-02-23 17:25:09',1,1,44.00,'','15311111234','光华北路隔坑村51号',NULL,'梁耀匀',NULL,NULL,NULL,'2026-02-23 18:25:00',NULL,2),(21,'1771838753628',2,6,2,'2026-02-23 17:25:54','2026-02-23 17:25:55',1,1,146.00,'','15311111234','光华北路隔坑村51号',NULL,'梁耀匀',NULL,NULL,NULL,'2026-02-23 18:25:00',NULL,2),(22,'1771838940457',2,6,2,'2026-02-23 17:29:00','2026-02-23 17:29:02',1,1,197.00,'','15311111234','光华北路隔坑村51号',NULL,'梁耀匀',NULL,NULL,NULL,'2026-02-23 18:28:00',NULL,2),(23,'1771839097040',2,6,2,'2026-02-23 17:31:37','2026-02-23 17:31:39',1,1,197.00,'','15311111234','光华北路隔坑村51号',NULL,'梁耀匀',NULL,NULL,NULL,'2026-02-23 18:31:00',NULL,2),(24,'1771839184520',2,6,2,'2026-02-23 17:33:05','2026-02-23 17:33:06',1,1,96.00,'','15311111234','光华北路隔坑村51号',NULL,'梁耀匀',NULL,NULL,NULL,'2026-02-23 18:33:00',NULL,2),(25,'1771844617215',6,6,2,'2026-02-23 19:03:37',NULL,1,0,96.00,'','15311111234','光华北路隔坑村51号',NULL,'梁耀匀','支付超时，自动取消',NULL,'2026-02-24 16:02:00','2026-02-23 20:03:00',NULL,2);
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
  `name` varchar(32) COLLATE utf8mb3_bin NOT NULL COMMENT '套餐名称',
  `price` decimal(10,2) NOT NULL COMMENT '套餐价格',
  `status` int DEFAULT '1' COMMENT '售卖状态 0:停售 1:起售',
  `description` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '描述信息',
  `image` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图片',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_setmeal_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='套餐';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `setmeal`
--

LOCK TABLES `setmeal` WRITE;
/*!40000 ALTER TABLE `setmeal` DISABLE KEYS */;
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
  `name` varchar(32) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '菜品名称 （冗余字段）',
  `price` decimal(10,2) DEFAULT NULL COMMENT '菜品单价（冗余字段）',
  `copies` int DEFAULT NULL COMMENT '补剂份数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='套餐补剂关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `setmeal_supplement`
--

LOCK TABLES `setmeal_supplement` WRITE;
/*!40000 ALTER TABLE `setmeal_supplement` DISABLE KEYS */;
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
  `name` varchar(32) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '商品名称',
  `image` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图片',
  `user_id` bigint NOT NULL COMMENT '主键',
  `supplement_id` bigint DEFAULT NULL COMMENT '补剂id',
  `setmeal_id` bigint DEFAULT NULL COMMENT '套餐id',
  `dish_flavor` varchar(50) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '口味',
  `number` int NOT NULL DEFAULT '1' COMMENT '数量',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='购物车';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shopping_cart`
--

LOCK TABLES `shopping_cart` WRITE;
/*!40000 ALTER TABLE `shopping_cart` DISABLE KEYS */;
INSERT INTO `shopping_cart` VALUES (71,'鮰鱼2斤','https://java-ai-lyy.oss-cn-shenzhen.aliyuncs.com/cb7a9b17-5d47-4282-bcad-5f44366ace92.png',6,67,NULL,'不辣',1,70.00,'2026-03-11 11:00:48'),(72,'草鱼2斤','https://sky-itcast.oss-cn-beijing.aliyuncs.com/b544d3ba-a1ae-4d20-a860-81cb5dec9e03.png',6,65,NULL,'不辣',1,68.00,'2026-03-11 11:00:49');
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
  `name` varchar(32) COLLATE utf8mb3_bin NOT NULL COMMENT '补剂名称',
  `category_id` bigint NOT NULL COMMENT '补剂分类id',
  `price` decimal(10,2) DEFAULT NULL COMMENT '补剂价格',
  `image` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图片',
  `description` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '描述信息',
  `status` int DEFAULT '1' COMMENT '0 停售 1 起售',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_supplement_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='补剂';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplement`
--

LOCK TABLES `supplement` WRITE;
/*!40000 ALTER TABLE `supplement` DISABLE KEYS */;
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
  `name` varchar(32) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '补剂细节的名称，确定购买的补剂品种和规格',
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `value` varchar(255) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '补剂详细信息数据list',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=115 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='补品和补品的相关细节信息关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplement_detail`
--

LOCK TABLES `supplement_detail` WRITE;
/*!40000 ALTER TABLE `supplement_detail` DISABLE KEYS */;
INSERT INTO `supplement_detail` VALUES (10,'甜味',40,'[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]'),(7,'忌口',41,'[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(7,'温度',42,'[\"热饮\",\"常温\",\"去冰\",\"少冰\",\"多冰\"]'),(6,'忌口',45,'[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(6,'辣度',46,'[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]'),(5,'辣度',47,'[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]'),(5,'甜味',48,'[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]'),(2,'甜味',49,'[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]'),(4,'甜味',50,'[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]'),(3,'甜味',51,'[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]'),(3,'忌口',52,'[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(52,'忌口',86,'[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(52,'辣度',87,'[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]'),(51,'忌口',88,'[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(51,'辣度',89,'[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]'),(53,'忌口',92,'[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(53,'辣度',93,'[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]'),(54,'忌口',94,'[\"不要葱\",\"不要蒜\",\"不要香菜\"]'),(56,'忌口',95,'[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(57,'忌口',96,'[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(60,'忌口',97,'[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(66,'辣度',101,'[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]'),(65,'辣度',103,'[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]'),(69,'忌口',107,'[\"不要葱\",\"不要蒜\",\"不要香菜\"]'),(75,'忌口',113,'[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(67,'辣度',114,'[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
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
  `password` varchar(45) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '用户密码',
  `name` varchar(32) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '姓名',
  `phone` varchar(11) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '手机号',
  `sex` varchar(2) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '性别',
  `id_number` varchar(18) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '身份证号',
  `avatar` varchar(500) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '头像',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='用户信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (4,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(5,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(6,'o321R3REjDqoNgN8e2GBjjoqj0p8',NULL,NULL,NULL,NULL,NULL,'2026-02-19 21:30:55');
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

-- Dump completed on 2026-03-12 12:58:50
