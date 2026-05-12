-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: ry-vue
-- ------------------------------------------------------
-- Server version	8.0.46

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

--
-- Current Database: `ry-vue`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `ry-vue` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `ry-vue`;

--
-- Table structure for table `audit_ai_finding`
--

DROP TABLE IF EXISTS `audit_ai_finding`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_ai_finding` (
  `finding_id` bigint NOT NULL AUTO_INCREMENT COMMENT '发现项主键',
  `ai_task_id` bigint NOT NULL COMMENT 'AI任务主键',
  `finding_type` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '发现类型',
  `finding_title` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '发现标题',
  `finding_content` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '发现内容',
  `sort_num` int DEFAULT '0' COMMENT '排序号',
  PRIMARY KEY (`finding_id`),
  KEY `idx_audit_ai_finding_task` (`ai_task_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI审核发现项';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_ai_finding`
--

LOCK TABLES `audit_ai_finding` WRITE;
/*!40000 ALTER TABLE `audit_ai_finding` DISABLE KEYS */;
/*!40000 ALTER TABLE `audit_ai_finding` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_ai_task`
--

DROP TABLE IF EXISTS `audit_ai_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_ai_task` (
  `ai_task_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'AI任务主键',
  `review_task_id` bigint DEFAULT NULL COMMENT '审核任务主键',
  `review_version_id` bigint DEFAULT NULL COMMENT '审核任务版本主键',
  `task_no` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务编号',
  `product_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '产品名称',
  `delivery_unit` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '送检单位',
  `submitter` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '提交人',
  `priority` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'medium' COMMENT '优先级',
  `queue_position` int DEFAULT '0' COMMENT '队列位置',
  `task_status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'waiting' COMMENT '任务状态',
  `estimated_duration` varchar(32) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '预计执行时间',
  `progress_percent` int DEFAULT '0' COMMENT '进度百分比',
  `progress_text` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '进度文案',
  `ai_analysis_count` int DEFAULT '0' COMMENT 'AI分析次数',
  `review_status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'pending' COMMENT '审核状态',
  `report_file_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '报告文件名',
  `report_file_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '报告文件地址',
  `ai_summary` text COLLATE utf8mb4_general_ci COMMENT 'AI总结',
  `review_opinion` text COLLATE utf8mb4_general_ci COMMENT '审核意见',
  `reviewer` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '审核人',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记（0存在 2删除）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`ai_task_id`),
  KEY `idx_audit_ai_task_no` (`task_no`),
  KEY `idx_audit_ai_review_version` (`review_task_id`,`review_version_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI任务队列表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_ai_task`
--

LOCK TABLES `audit_ai_task` WRITE;
/*!40000 ALTER TABLE `audit_ai_task` DISABLE KEYS */;
INSERT INTO `audit_ai_task` VALUES (8,7,NULL,'SF-1778483161998','本安-矿用本安型手机','单位1','admin','low',1,'paused','3分钟',100,'任务已暂停，等待恢复',1,'approved','2025520398FB（批注本安部分）(0725)_20260511150600A003.docx','/profile/upload/2026/05/11/2025520398FB（批注本安部分）(0725)_20260511150600A003.docx','未发现关键问题，但存在需人工复核的风险信号','','admin','2026-05-11 15:06:02','0','admin','2026-05-11 15:06:02','admin','2026-05-11 16:05:50','由审核列表任务自动创建'),(9,7,8,'SF-1778483161998','本安-矿用本安型手机','单位1','admin','medium',0,'completed','3分钟',100,'人工审核已完成',1,'approved','2025520398FB（批注本安部分）(0725)_20260511150600A003.docx','/profile/upload/2026/05/11/2025520398FB（批注本安部分）(0725)_20260511150600A003.docx','未发现关键问题，但存在需人工复核的风险信号','建议根据 AI 发现问题继续完善内啥都发容。','admin','2026-05-11 15:06:02','0','admin','2026-05-11 15:11:08','admin','2026-05-11 16:05:50','由审核列表详情入口自动创建：v1.0');
/*!40000 ALTER TABLE `audit_ai_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_asset_ai_step`
--

DROP TABLE IF EXISTS `audit_asset_ai_step`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_asset_ai_step` (
  `step_id` bigint NOT NULL AUTO_INCREMENT COMMENT '步骤主键',
  `version_id` bigint NOT NULL COMMENT '版本主键',
  `step_no` int DEFAULT '1' COMMENT '步骤序号',
  `step_title` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '步骤标题',
  `step_content` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '步骤内容',
  `step_time` datetime DEFAULT NULL COMMENT '步骤时间',
  `sort_num` int DEFAULT '0' COMMENT '排序号',
  PRIMARY KEY (`step_id`),
  KEY `idx_audit_asset_ai_step_version` (`version_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核资产AI分析步骤表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_asset_ai_step`
--

LOCK TABLES `audit_asset_ai_step` WRITE;
/*!40000 ALTER TABLE `audit_asset_ai_step` DISABLE KEYS */;
INSERT INTO `audit_asset_ai_step` VALUES (1,3,1,'排队等待AI分析','当前队列共计30个，该条位于队伍第三位','2025-06-24 15:29:23',1),(2,3,2,'AI正在解析审核','AI解析进度100%，已全部解析完成','2025-06-24 15:30:23',2),(3,3,3,'AI审核初步通过','AI已初步通过审核','2025-06-24 15:32:23',3),(4,4,1,'排队等待AI分析','当前队列共计30个，该条位于队伍第三位','2025-06-24 15:29:23',1),(5,4,2,'AI正在解析审核','AI解析进度100%，已全部解析完成','2025-06-24 15:30:23',2),(6,4,3,'AI审核初步通过','AI已初步通过审核','2025-06-24 15:32:23',3),(7,5,1,'排队等待AI分析','当前队列共计30个，该条位于队伍第三位','2025-06-24 15:29:23',1),(8,5,2,'AI正在解析审核','AI解析进度100%，已全部解析完成','2025-06-24 15:30:23',2),(9,5,3,'待修改','AI检测出问题1,2,3','2025-06-24 15:32:23',3),(10,6,1,'排队等待AI分析','当前队列共计30个，该条位于队伍第三位','2025-06-24 15:29:23',1),(11,6,2,'AI正在解析审核','AI解析进度100%，已全部解析完成','2025-06-24 15:30:23',2),(12,6,3,'待修改','AI检测出问题1,2','2025-06-24 15:32:23',3);
/*!40000 ALTER TABLE `audit_asset_ai_step` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_asset_ai_version`
--

DROP TABLE IF EXISTS `audit_asset_ai_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_asset_ai_version` (
  `version_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'AI版本主键',
  `asset_id` bigint NOT NULL COMMENT '资产主键',
  `version_no` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '版本号',
  `word_count_text` varchar(32) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字数描述',
  `current_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '是否当前版本（0否 1是）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`version_id`),
  KEY `idx_audit_asset_ai_version_asset` (`asset_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核资产AI分析版本表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_asset_ai_version`
--

LOCK TABLES `audit_asset_ai_version` WRITE;
/*!40000 ALTER TABLE `audit_asset_ai_version` DISABLE KEYS */;
INSERT INTO `audit_asset_ai_version` VALUES (1,1,'版本1','单次AI共计审核8千字','0','2025-06-24 15:26:23'),(2,1,'版本2','单次AI共计审核9千字','0','2025-06-24 15:27:23'),(3,1,'版本3','单次AI共计审核1w字','1','2025-06-24 15:29:23'),(4,2,'版本3','单次AI共计审核1w字','1','2025-06-24 15:29:23'),(5,3,'版本3','单次AI共计审核1w字','1','2025-06-24 15:29:23'),(6,4,'版本2','单次AI共计审核9千字','1','2025-06-24 15:29:23');
/*!40000 ALTER TABLE `audit_asset_ai_version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_asset_record`
--

DROP TABLE IF EXISTS `audit_asset_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_asset_record` (
  `asset_id` bigint NOT NULL AUTO_INCREMENT COMMENT '资产主键',
  `review_task_id` bigint DEFAULT NULL COMMENT '审核任务主键',
  `task_no` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务编号',
  `product_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '产品名称',
  `delivery_unit` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '送检单位',
  `submitter` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '提交人',
  `reviewer` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '审核人',
  `permission_owner` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '权限分配对象',
  `ai_analysis_count` int DEFAULT '0' COMMENT 'AI分析次数',
  `current_ai_version` varchar(20) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '当前AI版本',
  `review_status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'pending' COMMENT '审核状态',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `report_file_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '报告文件名称',
  `report_file_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '报告文件地址',
  `ai_opinion` text COLLATE utf8mb4_general_ci COMMENT 'AI观点',
  `final_opinion` text COLLATE utf8mb4_general_ci COMMENT '最终审核意见',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记（0存在 2删除）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`asset_id`),
  KEY `idx_audit_asset_task_no` (`task_no`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核资产记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_asset_record`
--

LOCK TABLES `audit_asset_record` WRITE;
/*!40000 ALTER TABLE `audit_asset_record` DISABLE KEYS */;
INSERT INTO `audit_asset_record` VALUES (1,1,'SF-16542598454','产品名称1','送检单位1','提交人1','审核人1','审核人1',3,'版本3','approved','2025-06-18 15:23:23','煤科院煤炭产品质量检测报告.pdf','/profile/audit/review/防爆电机检验报告_V2.0.pdf','本次报告经AI核查，发现存在内容缺失、格式错误两类问题，具体已汇总如下，对应报告中相关标注位置，便于整改完善。','格式正确，给予审核通过。','0','admin','2026-05-11 11:56:07','admin','2026-05-11 11:56:07','审核通过'),(2,1,'SF-16542598455','产品名称1','送检单位1','提交人1','审核人2','审核人2',3,'版本3','approved','2025-06-18 15:23:23','煤科院煤炭产品质量检测报告.pdf','/profile/audit/review/防爆电机检验报告_V2.0.pdf','AI核查已完成，内容符合提交规范。','格式正确，给予审核通过。','0','admin','2026-05-11 11:56:07','admin','2026-05-11 11:56:07','审核通过'),(3,1,'SF-16542598456','产品名称1','送检单位1','提交人1','审核人1','审核人1',3,'版本3','returned','2025-06-18 15:23:23','煤科院煤炭产品质量检测报告.pdf','/profile/audit/review/防爆电机检验报告_V2.0.pdf','当前版本存在多处待确认问题，需要重新上传。','请根据 AI 标注结果重新上传。','0','admin','2026-05-11 11:56:07','admin','2026-05-11 11:56:07','驳回归档'),(4,1,'SF-16542598457','产品名称1','送检单位1','提交人1','审核人2','审核人2',3,'版本2','returned','2025-06-18 15:23:23','煤科院煤炭产品质量检测报告.pdf','/profile/audit/review/防爆电机检验报告_V2.0.pdf','AI已初步通过，但人工审核要求补充说明。','请补充证明材料后再次提交。','0','admin','2026-05-11 11:56:07','admin','2026-05-11 11:56:07','驳回归档');
/*!40000 ALTER TABLE `audit_asset_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_asset_resubmit_record`
--

DROP TABLE IF EXISTS `audit_asset_resubmit_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_asset_resubmit_record` (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '重提记录主键',
  `asset_id` bigint NOT NULL COMMENT '资产主键',
  `version_no` varchar(20) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '版本号',
  `submitter` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '提交人',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `file_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '文件名称',
  `file_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '文件地址',
  `image_urls` text COLLATE utf8mb4_general_ci COMMENT '修改图片',
  `sort_num` int DEFAULT '0' COMMENT '排序号',
  PRIMARY KEY (`record_id`),
  KEY `idx_audit_asset_resubmit_asset` (`asset_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核资产修改与重提记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_asset_resubmit_record`
--

LOCK TABLES `audit_asset_resubmit_record` WRITE;
/*!40000 ALTER TABLE `audit_asset_resubmit_record` DISABLE KEYS */;
INSERT INTO `audit_asset_resubmit_record` VALUES (1,1,'v1.0版本','提交人1','2025-06-28 15:38:24','防爆电机检验报告 V1.0.pdf','/profile/audit/review/防爆电机检验报告_V1.0.pdf','',1),(2,1,'v2.0版本','提交人1','2025-06-28 15:38:24','防爆电机检验报告 V2.0.pdf','/profile/audit/review/防爆电机检验报告_V2.0.pdf','',2),(3,3,'v1.0版本','提交人1','2025-06-29 09:10:00','防爆电机检验报告 V1.0.pdf','/profile/audit/review/防爆电机检验报告_V1.0.pdf','',1),(4,4,'v1.0版本','提交人1','2025-06-29 11:18:00','防爆电机检验报告 V1.0.pdf','/profile/audit/review/防爆电机检验报告_V1.0.pdf','',1);
/*!40000 ALTER TABLE `audit_asset_resubmit_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_common_resource`
--

DROP TABLE IF EXISTS `audit_common_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_common_resource` (
  `resource_id` bigint NOT NULL AUTO_INCREMENT COMMENT '常用资源主键',
  `document_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '文档名称',
  `folder_id` bigint DEFAULT NULL COMMENT '归属文件库主键',
  `folder_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '归属文件库名称',
  `storage_status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'pending' COMMENT '向量化状态',
  `progress_text` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '文件进度',
  `creator` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `latest_modify_time` datetime DEFAULT NULL COMMENT '最新修改时间',
  `file_size` varchar(32) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '文件大小',
  `file_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '文件名称',
  `file_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '文件地址',
  `current_version_no` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'v1.0' COMMENT '当前版本号',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记（0存在 2删除）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`resource_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='常用文件资源表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_common_resource`
--

LOCK TABLES `audit_common_resource` WRITE;
/*!40000 ALTER TABLE `audit_common_resource` DISABLE KEYS */;
INSERT INTO `audit_common_resource` VALUES (1,'GBT3836.1-2021 爆炸性环境 第1部分： 设备 通用要求',0,'','pending','等待向量化任务执行','admin','2026-05-05 20:39:11','1.31MB','GBT3836.1-2021 爆炸性环境 第1部分： 设备 通用要求_20260505203909A001.docx','/profile/upload/2026/05/05/GBT3836.1-2021 爆炸性环境 第1部分： 设备 通用要求_20260505203909A001.docx','v1.0','0','admin','2026-05-05 20:39:10','admin','2026-05-05 20:39:10',NULL),(2,'GBT3836.9-2021',0,'','pending','等待向量化任务执行','admin','2026-05-05 20:40:13','456.47KB','GBT3836.9-2021_20260505204011A002.docx','/profile/upload/2026/05/05/GBT3836.9-2021_20260505204011A002.docx','v1.0','0','admin','2026-05-05 20:40:12','admin','2026-05-05 20:40:12',NULL),(3,'GBT3836.2-2021',0,'','pending','等待向量化任务执行','admin','2026-05-05 20:50:02','23.90MB','GBT3836.2-2021_20260505205001A001.pdf','/profile/upload/2026/05/05/GBT3836.2-2021_20260505205001A001.pdf','v1.0','0','admin','2026-05-05 20:50:02','admin','2026-05-05 20:50:02',NULL),(4,'2025520398+其它+技术审查',5,'中心资料','pending','等待向量化任务执行','admin','2026-05-05 20:58:31','58.82KB','2025520398+其它+技术审查_20260505205830A002.doc','/profile/upload/2026/05/05/2025520398+其它+技术审查_20260505205830A002.doc','v1.0','0','admin','2026-05-05 20:58:31','admin','2026-05-05 20:58:31',NULL),(5,'2025520398+其它+技术审查',5,'中心资料','pending','等待向量化任务执行','admin','2026-05-05 20:58:41','39.74KB','2025520398+其它+技术审查_20260505205839A003.docx','/profile/upload/2026/05/05/2025520398+其它+技术审查_20260505205839A003.docx','v1.0','0','admin','2026-05-05 20:58:40','admin','2026-05-05 20:58:40',NULL),(6,'2025520398＋合格证＋CCRI25.2513',5,'中心资料','pending','等待向量化任务执行','admin','2026-05-05 20:59:04','270.13KB','2025520398＋合格证＋CCRI25.2513_20260505205903A004.doc','/profile/upload/2026/05/05/2025520398＋合格证＋CCRI25.2513_20260505205903A004.doc','v1.0','0','admin','2026-05-05 20:59:04','admin','2026-05-05 20:59:04',NULL),(7,'任务单【2025520398】',2,'文件库一','pending','等待向量化任务执行','admin','2026-05-05 20:59:17','363.78KB','任务单【2025520398】_20260505205916A005.pdf','/profile/upload/2026/05/05/任务单【2025520398】_20260505205916A005.pdf','v1.0','0','admin','2026-05-05 20:59:17','admin','2026-05-05 20:59:17',NULL),(8,'ZDYZ127-Z矿用隔爆兼本安型监控主机企标',4,'审核标准库','pending','等待向量化任务执行','admin','2026-05-05 20:59:45','265.41KB','ZDYZ127-Z矿用隔爆兼本安型监控主机企标_20260505205943A006.doc','/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机企标_20260505205943A006.doc','v1.0','0','admin','2026-05-05 20:59:44','admin','2026-05-05 20:59:44',NULL),(9,'ZDYZ127-Z矿用隔爆兼本安型监控主机企标',4,'审核标准库','pending','等待向量化任务执行','admin','2026-05-05 20:59:54','136.46KB','ZDYZ127-Z矿用隔爆兼本安型监控主机企标_20260505205952A007.docx','/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机企标_20260505205952A007.docx','v1.0','0','admin','2026-05-05 20:59:53','admin','2026-05-05 20:59:53',NULL),(10,'ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表',4,'审核标准库','pending','等待向量化任务执行','admin','2026-05-05 21:00:05','22.50KB','ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表_20260505210004A008.doc','/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表_20260505210004A008.doc','v1.0','0','admin','2026-05-05 21:00:05','admin','2026-05-05 21:00:05',NULL),(11,'ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表',4,'审核标准库','pending','等待向量化任务执行','admin','2026-05-05 21:00:15','15.16KB','ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表_20260505210013A009.docx','/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表_20260505210013A009.docx','v1.0','0','admin','2026-05-05 21:00:15','admin','2026-05-05 21:00:15',NULL),(12,'ZDYZ127-Z矿用隔爆兼本安型监控主机说明书',4,'审核标准库','pending','等待向量化任务执行','admin','2026-05-05 21:00:29','134.00KB','ZDYZ127-Z矿用隔爆兼本安型监控主机说明书_20260505210026A010.doc','/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机说明书_20260505210026A010.doc','v1.0','0','admin','2026-05-05 21:00:29','admin','2026-05-05 21:00:29',NULL),(13,'KXJ127矿用隔爆兼本安型PLC控制箱企业标准',6,'客户资料','pending','等待向量化任务执行','admin','2026-05-05 21:01:20','86.22KB','KXJ127矿用隔爆兼本安型PLC控制箱企业标准_20260505210118A011.docx','/profile/upload/2026/05/05/KXJ127矿用隔爆兼本安型PLC控制箱企业标准_20260505210118A011.docx','v1.0','0','admin','2026-05-05 21:01:19','admin','2026-05-05 21:01:19',NULL),(14,'KXJ127矿用隔爆兼本安型PLC控制箱说明书',6,'客户资料','pending','等待向量化任务执行','admin','2026-05-05 21:01:28','65.17KB','KXJ127矿用隔爆兼本安型PLC控制箱说明书_20260505210126A012.doc','/profile/upload/2026/05/05/KXJ127矿用隔爆兼本安型PLC控制箱说明书_20260505210126A012.doc','v1.0','0','admin','2026-05-05 21:01:28','admin','2026-05-05 21:01:28',NULL),(15,'安标国家矿用产品安全标志中心（矿用产品安全标志技术审查与产品检验委托书）',7,'中心资料','pending','等待向量化任务执行','admin','2026-05-05 21:01:44','154.15KB','安标国家矿用产品安全标志中心（矿用产品安全标志技术审查与产品检验委托书）_20260505210143A013.pdf','/profile/upload/2026/05/05/安标国家矿用产品安全标志中心（矿用产品安全标志技术审查与产品检验委托书）_20260505210143A013.pdf','v1.0','0','admin','2026-05-05 21:01:44','admin','2026-05-05 21:01:44',NULL),(16,'检测分院实验室业务管理系统',7,'中心资料','pending','等待向量化任务执行','admin','2026-05-05 21:01:56','187.58KB','检测分院实验室业务管理系统_20260505210152A014.pdf','/profile/upload/2026/05/05/检测分院实验室业务管理系统_20260505210152A014.pdf','v1.0','0','admin','2026-05-05 21:01:55','admin','2026-05-05 21:01:55',NULL);
/*!40000 ALTER TABLE `audit_common_resource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_common_resource_version`
--

DROP TABLE IF EXISTS `audit_common_resource_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_common_resource_version` (
  `version_id` bigint NOT NULL AUTO_INCREMENT COMMENT '资源版本主键',
  `resource_id` bigint NOT NULL COMMENT '资源主键',
  `version_no` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '版本号',
  `file_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '文件名称',
  `file_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '文件地址',
  `file_size` varchar(32) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '文件大小',
  `creator` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`version_id`),
  KEY `idx_audit_common_resource_version_resource` (`resource_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='常用文件资源历史版本表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_common_resource_version`
--

LOCK TABLES `audit_common_resource_version` WRITE;
/*!40000 ALTER TABLE `audit_common_resource_version` DISABLE KEYS */;
INSERT INTO `audit_common_resource_version` VALUES (1,1,'v1.0','GBT3836.1-2021 爆炸性环境 第1部分： 设备 通用要求_20260505203909A001.docx','/profile/upload/2026/05/05/GBT3836.1-2021 爆炸性环境 第1部分： 设备 通用要求_20260505203909A001.docx','1.31MB','admin','2026-05-05 20:39:11'),(2,2,'v1.0','GBT3836.9-2021_20260505204011A002.docx','/profile/upload/2026/05/05/GBT3836.9-2021_20260505204011A002.docx','456.47KB','admin','2026-05-05 20:40:13'),(3,3,'v1.0','GBT3836.2-2021_20260505205001A001.pdf','/profile/upload/2026/05/05/GBT3836.2-2021_20260505205001A001.pdf','23.90MB','admin','2026-05-05 20:50:02'),(4,4,'v1.0','2025520398+其它+技术审查_20260505205830A002.doc','/profile/upload/2026/05/05/2025520398+其它+技术审查_20260505205830A002.doc','58.82KB','admin','2026-05-05 20:58:31'),(5,5,'v1.0','2025520398+其它+技术审查_20260505205839A003.docx','/profile/upload/2026/05/05/2025520398+其它+技术审查_20260505205839A003.docx','39.74KB','admin','2026-05-05 20:58:41'),(6,6,'v1.0','2025520398＋合格证＋CCRI25.2513_20260505205903A004.doc','/profile/upload/2026/05/05/2025520398＋合格证＋CCRI25.2513_20260505205903A004.doc','270.13KB','admin','2026-05-05 20:59:04'),(7,7,'v1.0','任务单【2025520398】_20260505205916A005.pdf','/profile/upload/2026/05/05/任务单【2025520398】_20260505205916A005.pdf','363.78KB','admin','2026-05-05 20:59:17'),(8,8,'v1.0','ZDYZ127-Z矿用隔爆兼本安型监控主机企标_20260505205943A006.doc','/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机企标_20260505205943A006.doc','265.41KB','admin','2026-05-05 20:59:45'),(9,9,'v1.0','ZDYZ127-Z矿用隔爆兼本安型监控主机企标_20260505205952A007.docx','/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机企标_20260505205952A007.docx','136.46KB','admin','2026-05-05 20:59:54'),(10,10,'v1.0','ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表_20260505210004A008.doc','/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表_20260505210004A008.doc','22.50KB','admin','2026-05-05 21:00:05'),(11,11,'v1.0','ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表_20260505210013A009.docx','/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表_20260505210013A009.docx','15.16KB','admin','2026-05-05 21:00:15'),(12,12,'v1.0','ZDYZ127-Z矿用隔爆兼本安型监控主机说明书_20260505210026A010.doc','/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机说明书_20260505210026A010.doc','134.00KB','admin','2026-05-05 21:00:29'),(13,13,'v1.0','KXJ127矿用隔爆兼本安型PLC控制箱企业标准_20260505210118A011.docx','/profile/upload/2026/05/05/KXJ127矿用隔爆兼本安型PLC控制箱企业标准_20260505210118A011.docx','86.22KB','admin','2026-05-05 21:01:20'),(14,14,'v1.0','KXJ127矿用隔爆兼本安型PLC控制箱说明书_20260505210126A012.doc','/profile/upload/2026/05/05/KXJ127矿用隔爆兼本安型PLC控制箱说明书_20260505210126A012.doc','65.17KB','admin','2026-05-05 21:01:28'),(15,15,'v1.0','安标国家矿用产品安全标志中心（矿用产品安全标志技术审查与产品检验委托书）_20260505210143A013.pdf','/profile/upload/2026/05/05/安标国家矿用产品安全标志中心（矿用产品安全标志技术审查与产品检验委托书）_20260505210143A013.pdf','154.15KB','admin','2026-05-05 21:01:44'),(16,16,'v1.0','检测分院实验室业务管理系统_20260505210152A014.pdf','/profile/upload/2026/05/05/检测分院实验室业务管理系统_20260505210152A014.pdf','187.58KB','admin','2026-05-05 21:01:56');
/*!40000 ALTER TABLE `audit_common_resource_version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_library_folder`
--

DROP TABLE IF EXISTS `audit_library_folder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_library_folder` (
  `folder_id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件库主键',
  `parent_id` bigint DEFAULT NULL COMMENT '父级文件库主键',
  `folder_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件库名称',
  `intro` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '简介',
  `visible_scope` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'all' COMMENT '可见范围',
  `top_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '是否置顶（0否 1是）',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记（0存在 2删除）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`folder_id`),
  KEY `idx_audit_library_folder_parent` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核文件库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_library_folder`
--

LOCK TABLES `audit_library_folder` WRITE;
/*!40000 ALTER TABLE `audit_library_folder` DISABLE KEYS */;
INSERT INTO `audit_library_folder` VALUES (2,0,'文件库一','','all','0','0','admin','2026-05-05 20:38:14','admin','2026-05-05 20:38:14',NULL),(3,0,'文件库二','','all','0','0','admin','2026-05-05 20:38:23','admin','2026-05-05 20:38:23',NULL),(4,0,'审核标准库','','all','0','0','admin','2026-05-05 20:38:36','admin','2026-05-05 20:38:36',NULL),(5,2,'中心资料','','all','0','0','admin','2026-05-05 20:55:41','admin','2026-05-05 20:55:41',NULL),(6,3,'客户资料','','all','0','0','admin','2026-05-05 21:01:02','admin','2026-05-05 21:01:02',NULL),(7,3,'中心资料','','all','0','0','admin','2026-05-05 21:01:08','admin','2026-05-05 21:01:08',NULL);
/*!40000 ALTER TABLE `audit_library_folder` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_review_issue`
--

DROP TABLE IF EXISTS `audit_review_issue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_review_issue` (
  `issue_id` bigint NOT NULL AUTO_INCREMENT COMMENT '问题主键',
  `version_id` bigint NOT NULL COMMENT '版本主键',
  `issue_type` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '问题类型',
  `issue_title` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '问题标题',
  `issue_content` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '问题内容',
  `sort_num` int DEFAULT '0' COMMENT '排序号',
  PRIMARY KEY (`issue_id`),
  KEY `idx_audit_review_issue_version` (`version_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核问题表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_review_issue`
--

LOCK TABLES `audit_review_issue` WRITE;
/*!40000 ALTER TABLE `audit_review_issue` DISABLE KEYS */;
INSERT INTO `audit_review_issue` VALUES (9,8,'数据错误','识别异常类型：数据错误','报告第 3 页表 3-1 中“防爆等级”填写值与依据文件存在偏差，请人工复核。',1),(10,8,'格式不规范','识别异常类型：格式不规范','报告签字页未完成签章或日期格式不符合模板要求，请补充修正。',2);
/*!40000 ALTER TABLE `audit_review_issue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_review_stage`
--

DROP TABLE IF EXISTS `audit_review_stage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_review_stage` (
  `stage_id` bigint NOT NULL AUTO_INCREMENT COMMENT '阶段主键',
  `version_id` bigint NOT NULL COMMENT '版本主键',
  `stage_code` varchar(30) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '阶段编码',
  `stage_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '阶段名称',
  `stage_status` char(1) COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '阶段状态（0未完成 1完成）',
  `stage_time` datetime DEFAULT NULL COMMENT '阶段时间',
  `stage_summary` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '阶段摘要',
  `stage_detail` text COLLATE utf8mb4_general_ci COMMENT '阶段详情',
  `sort_num` int DEFAULT '0' COMMENT '排序号',
  PRIMARY KEY (`stage_id`),
  KEY `idx_audit_review_stage_version` (`version_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核流转阶段表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_review_stage`
--

LOCK TABLES `audit_review_stage` WRITE;
/*!40000 ALTER TABLE `audit_review_stage` DISABLE KEYS */;
INSERT INTO `audit_review_stage` VALUES (19,8,'upload','报告上传','1','2026-05-11 15:06:02','对应智能体-文件校验智能体','① 格式校验：检测文件为 PDF，符合要求；② 大小校验：文件大小未超过限制；③ 存储校验：已成功存入审核资源目录。',1),(20,8,'parse','报告解析','1','2026-05-11 15:08:02','对应智能体-预处理智能体','① 格式转换：已完成结构化转换；② 字段提取：提取 12 个核心字段；③ 摘要生成：已输出 AI 观点。',2),(21,8,'detect','报告检测','1','2026-05-11 15:10:02','对应智能体：比对智能体 + 检测结果智能体','① 比对智能体：已完成报告与依据文件比对；② 检测结果智能体：已输出问题归类与处理建议。',3);
/*!40000 ALTER TABLE `audit_review_stage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_review_task`
--

DROP TABLE IF EXISTS `audit_review_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_review_task` (
  `task_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务主键',
  `task_no` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务编号',
  `product_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '产品名称',
  `delivery_unit` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '送检单位',
  `sponsor` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '发起人',
  `handler_name` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '经办人',
  `priority` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'medium' COMMENT '优先级',
  `ai_analysis_count` int DEFAULT '0' COMMENT 'AI分析次数',
  `task_status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'uploaded' COMMENT '任务状态',
  `review_status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'pending' COMMENT '审核状态',
  `process_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '处理状态（0正常 1暂停）',
  `current_version_no` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'v1.0' COMMENT '当前版本号',
  `main_report_urls` text COLLATE utf8mb4_general_ci COMMENT '主报告文件',
  `basis_file_urls` text COLLATE utf8mb4_general_ci COMMENT '依据文件',
  `appendix_file_urls` text COLLATE utf8mb4_general_ci COMMENT '补充附件',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记（0存在 2删除）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`task_id`),
  KEY `idx_audit_review_task_no` (`task_no`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核列表任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_review_task`
--

LOCK TABLES `audit_review_task` WRITE;
/*!40000 ALTER TABLE `audit_review_task` DISABLE KEYS */;
INSERT INTO `audit_review_task` VALUES (7,'SF-1778483161998','本安-矿用本安型手机','单位1','admin','若依','low',3,'uploaded','pending','1','v1.0','/profile/upload/2026/05/11/2025520398FB（批注本安部分）(0725)_20260511150600A003.docx','','','2026-05-11 15:06:02','0','admin','2026-05-11 15:06:02','admin','2026-05-11 16:21:10','');
/*!40000 ALTER TABLE `audit_review_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_review_version`
--

DROP TABLE IF EXISTS `audit_review_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_review_version` (
  `version_id` bigint NOT NULL AUTO_INCREMENT COMMENT '版本主键',
  `task_id` bigint NOT NULL COMMENT '任务主键',
  `version_no` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '版本号',
  `report_file_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '报告文件名称',
  `report_file_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '报告文件地址',
  `main_report_urls` text COLLATE utf8mb4_general_ci COMMENT '主报告文件',
  `basis_file_urls` text COLLATE utf8mb4_general_ci COMMENT '依据文件',
  `appendix_file_urls` text COLLATE utf8mb4_general_ci COMMENT '补充附件',
  `detect_status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'uploaded' COMMENT '检测状态',
  `submitter` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '提交人',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `ai_summary` text COLLATE utf8mb4_general_ci COMMENT 'AI分析观点',
  `review_opinion` text COLLATE utf8mb4_general_ci COMMENT '审核意见',
  `current_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '是否当前版本（0否 1是）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`version_id`),
  KEY `idx_audit_review_version_task` (`task_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核任务版本表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_review_version`
--

LOCK TABLES `audit_review_version` WRITE;
/*!40000 ALTER TABLE `audit_review_version` DISABLE KEYS */;
INSERT INTO `audit_review_version` VALUES (8,7,'v1.0','2025520398FB（批注本安部分）(0725)_20260511150600A003.docx','/profile/upload/2026/05/11/2025520398FB（批注本安部分）(0725)_20260511150600A003.docx','/profile/upload/2026/05/11/2025520398FB（批注本安部分）(0725)_20260511150600A003.docx','','','uploaded','admin','2026-05-11 15:06:02','本次报告经 AI 核查，发现存在内容缺失、格式错误两类问题，具体已汇总在检测结果区，便于整改完善。','建议根据 AI 发现问题继续完善内容。','1','admin','2026-05-11 15:06:02');
/*!40000 ALTER TABLE `audit_review_version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_task_resource`
--

DROP TABLE IF EXISTS `audit_task_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_task_resource` (
  `resource_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务资源主键',
  `file_no` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件编号',
  `file_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件名称',
  `archive_time` datetime DEFAULT NULL COMMENT '归档时间',
  `folder_id` bigint DEFAULT NULL COMMENT '归属文件库主键',
  `folder_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '归属文件库名称',
  `collect_status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'processing' COMMENT '文件采集状态',
  `preview_file_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '预览文件地址',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记（0存在 2删除）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任务文件资源表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_task_resource`
--

LOCK TABLES `audit_task_resource` WRITE;
/*!40000 ALTER TABLE `audit_task_resource` DISABLE KEYS */;
/*!40000 ALTER TABLE `audit_task_resource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_vector_eval_case`
--

DROP TABLE IF EXISTS `audit_vector_eval_case`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_vector_eval_case` (
  `case_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评估样例主键',
  `workflow_code` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '工作流编码',
  `query_text` varchar(1000) COLLATE utf8mb4_general_ci NOT NULL COMMENT '检索问题',
  `expected_resource_id` bigint DEFAULT NULL COMMENT '期望资源ID',
  `expected_chunk_uid` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '期望分片UID',
  `expected_rule_code` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '期望规则编号',
  `enabled` char(1) COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '是否启用（1启用 0停用）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`case_id`),
  KEY `idx_audit_vector_eval_case_workflow` (`workflow_code`),
  KEY `idx_audit_vector_eval_case_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核知识库召回评估样例表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_vector_eval_case`
--

LOCK TABLES `audit_vector_eval_case` WRITE;
/*!40000 ALTER TABLE `audit_vector_eval_case` DISABLE KEYS */;
/*!40000 ALTER TABLE `audit_vector_eval_case` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_vector_search_log`
--

DROP TABLE IF EXISTS `audit_vector_search_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_vector_search_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `request_id` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '请求ID',
  `workflow_code` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '工作流编码',
  `task_id` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '工作流任务ID',
  `query_count` int DEFAULT '1' COMMENT '查询数量',
  `permission_mode` varchar(32) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '权限模式',
  `scope_summary` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '范围摘要',
  `retrieval_config` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '检索配置摘要',
  `result_count` int DEFAULT '0' COMMENT '总召回数量',
  `top_resource_ids` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '召回资源摘要',
  `status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'success' COMMENT 'success/failed',
  `error_code` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '错误码',
  `error_msg` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '错误信息',
  `cost_ms` bigint DEFAULT '0' COMMENT '耗时毫秒',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`log_id`),
  KEY `idx_audit_vector_search_log_request` (`request_id`),
  KEY `idx_audit_vector_search_log_workflow` (`workflow_code`,`task_id`),
  KEY `idx_audit_vector_search_log_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核知识库检索日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_vector_search_log`
--

LOCK TABLES `audit_vector_search_log` WRITE;
/*!40000 ALTER TABLE `audit_vector_search_log` DISABLE KEYS */;
INSERT INTO `audit_vector_search_log` VALUES (1,'AUDIT-20260511-000002-KB-BATCH-135365455796100','policy_document_audit','AUDIT-20260511-000002',10,'explicit_scope','folderIds=;resourceIds=;knowledgeBaseCodes=default;categoryCodes=;businessType=;effectiveOnly=true;asOfDate=','topK=8;minScore=null;includeChunkText=null;maxChunkChars=null;hybrid=true;rerank=false',0,'','success','KB_NO_RESULT','success',2385,'','2026-05-11 13:34:07'),(2,'AUDIT-20260511-000002-KB-BATCH-135367967856300','policy_document_audit','AUDIT-20260511-000002',5,'explicit_scope','folderIds=;resourceIds=;knowledgeBaseCodes=default;categoryCodes=;businessType=;effectiveOnly=true;asOfDate=','topK=8;minScore=null;includeChunkText=null;maxChunkChars=null;hybrid=true;rerank=false',0,'','success','KB_NO_RESULT','success',559,'','2026-05-11 13:34:07'),(3,'AUDIT-20260511-000003-KB-BATCH-138244079967500','policy_document_audit','AUDIT-20260511-000003',10,'explicit_scope','folderIds=;resourceIds=;knowledgeBaseCodes=default;categoryCodes=;businessType=;effectiveOnly=true;asOfDate=','topK=8;minScore=null;includeChunkText=null;maxChunkChars=null;hybrid=true;rerank=false',0,'','success','KB_NO_RESULT','success',1984,'','2026-05-11 14:22:05'),(4,'AUDIT-20260511-000003-KB-BATCH-138246153625700','policy_document_audit','AUDIT-20260511-000003',5,'explicit_scope','folderIds=;resourceIds=;knowledgeBaseCodes=default;categoryCodes=;businessType=;effectiveOnly=true;asOfDate=','topK=8;minScore=null;includeChunkText=null;maxChunkChars=null;hybrid=true;rerank=false',0,'','success','KB_NO_RESULT','success',546,'','2026-05-11 14:22:05'),(5,'AUDIT-20260511-000004-KB-BATCH-139501891356200','policy_document_audit','AUDIT-20260511-000004',10,'explicit_scope','folderIds=;resourceIds=;knowledgeBaseCodes=default;categoryCodes=;businessType=;effectiveOnly=true;asOfDate=','topK=8;minScore=null;includeChunkText=null;maxChunkChars=null;hybrid=true;rerank=false',0,'','success','KB_NO_RESULT','success',1598,'','2026-05-11 14:43:02'),(6,'AUDIT-20260511-000004-KB-BATCH-139503543346300','policy_document_audit','AUDIT-20260511-000004',5,'explicit_scope','folderIds=;resourceIds=;knowledgeBaseCodes=default;categoryCodes=;businessType=;effectiveOnly=true;asOfDate=','topK=8;minScore=null;includeChunkText=null;maxChunkChars=null;hybrid=true;rerank=false',0,'','success','KB_NO_RESULT','success',478,'','2026-05-11 14:43:03'),(7,'AUDIT-20260511-000005-KB-BATCH-140944551710000','policy_document_audit','AUDIT-20260511-000005',10,'explicit_scope','folderIds=;resourceIds=;knowledgeBaseCodes=default;categoryCodes=;businessType=;effectiveOnly=true;asOfDate=','topK=8;minScore=null;includeChunkText=null;maxChunkChars=null;hybrid=true;rerank=false',0,'','success','KB_NO_RESULT','success',2034,'','2026-05-11 15:07:05'),(8,'AUDIT-20260511-000005-KB-BATCH-140946641073200','policy_document_audit','AUDIT-20260511-000005',5,'explicit_scope','folderIds=;resourceIds=;knowledgeBaseCodes=default;categoryCodes=;businessType=;effectiveOnly=true;asOfDate=','topK=8;minScore=null;includeChunkText=null;maxChunkChars=null;hybrid=true;rerank=false',0,'','success','KB_NO_RESULT','success',924,'','2026-05-11 15:07:06'),(9,'AUDIT-20260511-000006-KB-BATCH-141245853571300','policy_document_audit','AUDIT-20260511-000006',10,'explicit_scope','folderIds=;resourceIds=;knowledgeBaseCodes=default;categoryCodes=;businessType=;effectiveOnly=true;asOfDate=','topK=8;minScore=null;includeChunkText=null;maxChunkChars=null;hybrid=true;rerank=false',0,'','success','KB_NO_RESULT','success',2770,'','2026-05-11 15:12:07'),(10,'AUDIT-20260511-000006-KB-BATCH-141248679272700','policy_document_audit','AUDIT-20260511-000006',5,'explicit_scope','folderIds=;resourceIds=;knowledgeBaseCodes=default;categoryCodes=;businessType=;effectiveOnly=true;asOfDate=','topK=8;minScore=null;includeChunkText=null;maxChunkChars=null;hybrid=true;rerank=false',0,'','success','KB_NO_RESULT','success',692,'','2026-05-11 15:12:08');
/*!40000 ALTER TABLE `audit_vector_search_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_vector_task`
--

DROP TABLE IF EXISTS `audit_vector_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_vector_task` (
  `task_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务主键',
  `resource_id` bigint NOT NULL COMMENT '文件资源主键',
  `task_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务类型 index/reindex/delete',
  `task_status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'pending' COMMENT 'pending/running/success/failed/skipped/cancelled',
  `retry_count` int DEFAULT '0' COMMENT '重试次数',
  `max_retry_count` int DEFAULT '3' COMMENT '最大重试次数',
  `progress_text` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '任务进度',
  `error_msg` text COLLATE utf8mb4_general_ci COMMENT '错误信息',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`task_id`),
  KEY `idx_audit_vector_task_status` (`task_status`),
  KEY `idx_audit_vector_task_resource` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核文件向量化任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_vector_task`
--

LOCK TABLES `audit_vector_task` WRITE;
/*!40000 ALTER TABLE `audit_vector_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `audit_vector_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gen_table`
--

DROP TABLE IF EXISTS `gen_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gen_table` (
  `table_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) COLLATE utf8mb4_general_ci DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `tpl_web_type` varchar(30) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '前端模板类型（element-ui模版 element-plus模版）',
  `package_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='代码生成业务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gen_table`
--

LOCK TABLES `gen_table` WRITE;
/*!40000 ALTER TABLE `gen_table` DISABLE KEYS */;
/*!40000 ALTER TABLE `gen_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gen_table_column`
--

DROP TABLE IF EXISTS `gen_table_column`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gen_table_column` (
  `column_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) COLLATE utf8mb4_general_ci DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典类型',
  `sort` int DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='代码生成业务表字段';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gen_table_column`
--

LOCK TABLES `gen_table_column` WRITE;
/*!40000 ALTER TABLE `gen_table_column` DISABLE KEYS */;
/*!40000 ALTER TABLE `gen_table_column` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_blob_triggers`
--

DROP TABLE IF EXISTS `qrtz_blob_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_blob_triggers` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `blob_data` blob COMMENT '存放持久化Trigger对象',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_blob_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Blob类型的触发器表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_blob_triggers`
--

LOCK TABLES `qrtz_blob_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_blob_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_blob_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_calendars`
--

DROP TABLE IF EXISTS `qrtz_calendars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_calendars` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `calendar_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '日历名称',
  `calendar` blob NOT NULL COMMENT '存放持久化calendar对象',
  PRIMARY KEY (`sched_name`,`calendar_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='日历信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_calendars`
--

LOCK TABLES `qrtz_calendars` WRITE;
/*!40000 ALTER TABLE `qrtz_calendars` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_calendars` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_cron_triggers`
--

DROP TABLE IF EXISTS `qrtz_cron_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_cron_triggers` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `cron_expression` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'cron表达式',
  `time_zone_id` varchar(80) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '时区',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_cron_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Cron类型的触发器表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_cron_triggers`
--

LOCK TABLES `qrtz_cron_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_cron_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_cron_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_fired_triggers`
--

DROP TABLE IF EXISTS `qrtz_fired_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_fired_triggers` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `entry_id` varchar(95) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度器实例id',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `instance_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度器实例名',
  `fired_time` bigint NOT NULL COMMENT '触发的时间',
  `sched_time` bigint NOT NULL COMMENT '定时器制定的时间',
  `priority` int NOT NULL COMMENT '优先级',
  `state` varchar(16) COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态',
  `job_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务名称',
  `job_group` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务组名',
  `is_nonconcurrent` varchar(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否并发',
  `requests_recovery` varchar(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否接受恢复执行',
  PRIMARY KEY (`sched_name`,`entry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='已触发的触发器表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_fired_triggers`
--

LOCK TABLES `qrtz_fired_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_fired_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_fired_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_job_details`
--

DROP TABLE IF EXISTS `qrtz_job_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_job_details` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `job_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务组名',
  `description` varchar(250) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '相关介绍',
  `job_class_name` varchar(250) COLLATE utf8mb4_general_ci NOT NULL COMMENT '执行任务类名称',
  `is_durable` varchar(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否持久化',
  `is_nonconcurrent` varchar(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否并发',
  `is_update_data` varchar(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否更新数据',
  `requests_recovery` varchar(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否接受恢复执行',
  `job_data` blob COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`,`job_name`,`job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任务详细信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_job_details`
--

LOCK TABLES `qrtz_job_details` WRITE;
/*!40000 ALTER TABLE `qrtz_job_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_job_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_locks`
--

DROP TABLE IF EXISTS `qrtz_locks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_locks` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `lock_name` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '悲观锁名称',
  PRIMARY KEY (`sched_name`,`lock_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='存储的悲观锁信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_locks`
--

LOCK TABLES `qrtz_locks` WRITE;
/*!40000 ALTER TABLE `qrtz_locks` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_locks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_paused_trigger_grps`
--

DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_paused_trigger_grps` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  PRIMARY KEY (`sched_name`,`trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='暂停的触发器表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_paused_trigger_grps`
--

LOCK TABLES `qrtz_paused_trigger_grps` WRITE;
/*!40000 ALTER TABLE `qrtz_paused_trigger_grps` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_paused_trigger_grps` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_scheduler_state`
--

DROP TABLE IF EXISTS `qrtz_scheduler_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_scheduler_state` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `instance_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '实例名称',
  `last_checkin_time` bigint NOT NULL COMMENT '上次检查时间',
  `checkin_interval` bigint NOT NULL COMMENT '检查间隔时间',
  PRIMARY KEY (`sched_name`,`instance_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='调度器状态表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_scheduler_state`
--

LOCK TABLES `qrtz_scheduler_state` WRITE;
/*!40000 ALTER TABLE `qrtz_scheduler_state` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_scheduler_state` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_simple_triggers`
--

DROP TABLE IF EXISTS `qrtz_simple_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_simple_triggers` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `repeat_count` bigint NOT NULL COMMENT '重复的次数统计',
  `repeat_interval` bigint NOT NULL COMMENT '重复的间隔时间',
  `times_triggered` bigint NOT NULL COMMENT '已经触发的次数',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_simple_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='简单触发器的信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_simple_triggers`
--

LOCK TABLES `qrtz_simple_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_simple_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_simple_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_simprop_triggers`
--

DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_simprop_triggers` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `str_prop_1` varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'String类型的trigger的第一个参数',
  `str_prop_2` varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'String类型的trigger的第二个参数',
  `str_prop_3` varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'String类型的trigger的第三个参数',
  `int_prop_1` int DEFAULT NULL COMMENT 'int类型的trigger的第一个参数',
  `int_prop_2` int DEFAULT NULL COMMENT 'int类型的trigger的第二个参数',
  `long_prop_1` bigint DEFAULT NULL COMMENT 'long类型的trigger的第一个参数',
  `long_prop_2` bigint DEFAULT NULL COMMENT 'long类型的trigger的第二个参数',
  `dec_prop_1` decimal(13,4) DEFAULT NULL COMMENT 'decimal类型的trigger的第一个参数',
  `dec_prop_2` decimal(13,4) DEFAULT NULL COMMENT 'decimal类型的trigger的第二个参数',
  `bool_prop_1` varchar(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Boolean类型的trigger的第一个参数',
  `bool_prop_2` varchar(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Boolean类型的trigger的第二个参数',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_simprop_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='同步机制的行锁表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_simprop_triggers`
--

LOCK TABLES `qrtz_simprop_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_simprop_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_simprop_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_triggers`
--

DROP TABLE IF EXISTS `qrtz_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_triggers` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器的名字',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器所属组的名字',
  `job_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details表job_name的外键',
  `job_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details表job_group的外键',
  `description` varchar(250) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '相关介绍',
  `next_fire_time` bigint DEFAULT NULL COMMENT '上一次触发时间（毫秒）',
  `prev_fire_time` bigint DEFAULT NULL COMMENT '下一次触发时间（默认为-1表示不触发）',
  `priority` int DEFAULT NULL COMMENT '优先级',
  `trigger_state` varchar(16) COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器状态',
  `trigger_type` varchar(8) COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器的类型',
  `start_time` bigint NOT NULL COMMENT '开始时间',
  `end_time` bigint DEFAULT NULL COMMENT '结束时间',
  `calendar_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '日程表名称',
  `misfire_instr` smallint DEFAULT NULL COMMENT '补偿执行的策略',
  `job_data` blob COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  KEY `sched_name` (`sched_name`,`job_name`,`job_group`),
  CONSTRAINT `qrtz_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `qrtz_job_details` (`sched_name`, `job_name`, `job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='触发器详细信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_triggers`
--

LOCK TABLES `qrtz_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_config`
--

DROP TABLE IF EXISTS `sys_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_config` (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) COLLATE utf8mb4_general_ci DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参数配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_config`
--

LOCK TABLES `sys_config` WRITE;
/*!40000 ALTER TABLE `sys_config` DISABLE KEYS */;
INSERT INTO `sys_config` VALUES (1,'主框架页-默认皮肤样式名称','sys.index.skinName','skin-blue','Y','admin','2026-05-11 11:55:44','',NULL,'蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow'),(2,'用户管理-账号初始密码','sys.user.initPassword','123456','Y','admin','2026-05-11 11:55:44','',NULL,'初始化密码 123456'),(3,'主框架页-侧边栏主题','sys.index.sideTheme','theme-dark','Y','admin','2026-05-11 11:55:44','',NULL,'深色主题theme-dark，浅色主题theme-light'),(4,'账号自助-验证码开关','sys.account.captchaEnabled','true','Y','admin','2026-05-11 11:55:44','',NULL,'是否开启验证码功能（true开启，false关闭）'),(5,'账号自助-是否开启用户注册功能','sys.account.registerUser','false','Y','admin','2026-05-11 11:55:44','',NULL,'是否开启注册用户功能（true开启，false关闭）'),(6,'用户登录-黑名单列表','sys.login.blackIPList','','Y','admin','2026-05-11 11:55:44','',NULL,'设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）'),(7,'用户管理-初始密码修改策略','sys.account.initPasswordModify','1','Y','admin','2026-05-11 11:55:44','',NULL,'0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框'),(8,'用户管理-账号密码更新周期','sys.account.passwordValidateDays','0','Y','admin','2026-05-11 11:55:44','',NULL,'密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');
/*!40000 ALTER TABLE `sys_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dept`
--

DROP TABLE IF EXISTS `sys_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dept` (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门id',
  `ancestors` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '部门名称',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `leader` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dept`
--

LOCK TABLES `sys_dept` WRITE;
/*!40000 ALTER TABLE `sys_dept` DISABLE KEYS */;
INSERT INTO `sys_dept` VALUES (100,0,'0','若依科技',0,'若依','15888888888','ry@qq.com','0','0','admin','2026-05-11 11:55:43','',NULL),(101,100,'0,100','深圳总公司',1,'若依','15888888888','ry@qq.com','0','0','admin','2026-05-11 11:55:43','',NULL),(102,100,'0,100','长沙分公司',2,'若依','15888888888','ry@qq.com','0','0','admin','2026-05-11 11:55:43','',NULL),(103,101,'0,100,101','研发部门',1,'若依','15888888888','ry@qq.com','0','0','admin','2026-05-11 11:55:43','',NULL),(104,101,'0,100,101','市场部门',2,'若依','15888888888','ry@qq.com','0','0','admin','2026-05-11 11:55:43','',NULL),(105,101,'0,100,101','测试部门',3,'若依','15888888888','ry@qq.com','0','0','admin','2026-05-11 11:55:43','',NULL),(106,101,'0,100,101','财务部门',4,'若依','15888888888','ry@qq.com','0','0','admin','2026-05-11 11:55:43','',NULL),(107,101,'0,100,101','运维部门',5,'若依','15888888888','ry@qq.com','0','0','admin','2026-05-11 11:55:43','',NULL),(108,102,'0,100,102','市场部门',1,'若依','15888888888','ry@qq.com','0','0','admin','2026-05-11 11:55:43','',NULL),(109,102,'0,100,102','财务部门',2,'若依','15888888888','ry@qq.com','0','0','admin','2026-05-11 11:55:43','',NULL);
/*!40000 ALTER TABLE `sys_dept` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dict_data`
--

DROP TABLE IF EXISTS `sys_dict_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_data` (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int DEFAULT '0' COMMENT '字典排序',
  `dict_label` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) COLLATE utf8mb4_general_ci DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`)
) ENGINE=InnoDB AUTO_INCREMENT=2414 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典数据表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict_data`
--

LOCK TABLES `sys_dict_data` WRITE;
/*!40000 ALTER TABLE `sys_dict_data` DISABLE KEYS */;
INSERT INTO `sys_dict_data` VALUES (1,1,'男','0','sys_user_sex','','','Y','0','admin','2026-05-11 11:55:44','',NULL,'性别男'),(2,2,'女','1','sys_user_sex','','','N','0','admin','2026-05-11 11:55:44','',NULL,'性别女'),(3,3,'未知','2','sys_user_sex','','','N','0','admin','2026-05-11 11:55:44','',NULL,'性别未知'),(4,1,'显示','0','sys_show_hide','','primary','Y','0','admin','2026-05-11 11:55:44','',NULL,'显示菜单'),(5,2,'隐藏','1','sys_show_hide','','danger','N','0','admin','2026-05-11 11:55:44','',NULL,'隐藏菜单'),(6,1,'正常','0','sys_normal_disable','','primary','Y','0','admin','2026-05-11 11:55:44','',NULL,'正常状态'),(7,2,'停用','1','sys_normal_disable','','danger','N','0','admin','2026-05-11 11:55:44','',NULL,'停用状态'),(8,1,'正常','0','sys_job_status','','primary','Y','0','admin','2026-05-11 11:55:44','',NULL,'正常状态'),(9,2,'暂停','1','sys_job_status','','danger','N','0','admin','2026-05-11 11:55:44','',NULL,'停用状态'),(10,1,'默认','DEFAULT','sys_job_group','','','Y','0','admin','2026-05-11 11:55:44','',NULL,'默认分组'),(11,2,'系统','SYSTEM','sys_job_group','','','N','0','admin','2026-05-11 11:55:44','',NULL,'系统分组'),(12,1,'是','Y','sys_yes_no','','primary','Y','0','admin','2026-05-11 11:55:44','',NULL,'系统默认是'),(13,2,'否','N','sys_yes_no','','danger','N','0','admin','2026-05-11 11:55:44','',NULL,'系统默认否'),(14,1,'通知','1','sys_notice_type','','warning','Y','0','admin','2026-05-11 11:55:44','',NULL,'通知'),(15,2,'公告','2','sys_notice_type','','success','N','0','admin','2026-05-11 11:55:44','',NULL,'公告'),(16,1,'正常','0','sys_notice_status','','primary','Y','0','admin','2026-05-11 11:55:44','',NULL,'正常状态'),(17,2,'关闭','1','sys_notice_status','','danger','N','0','admin','2026-05-11 11:55:44','',NULL,'关闭状态'),(18,99,'其他','0','sys_oper_type','','info','N','0','admin','2026-05-11 11:55:44','',NULL,'其他操作'),(19,1,'新增','1','sys_oper_type','','info','N','0','admin','2026-05-11 11:55:44','',NULL,'新增操作'),(20,2,'修改','2','sys_oper_type','','info','N','0','admin','2026-05-11 11:55:44','',NULL,'修改操作'),(21,3,'删除','3','sys_oper_type','','danger','N','0','admin','2026-05-11 11:55:44','',NULL,'删除操作'),(22,4,'授权','4','sys_oper_type','','primary','N','0','admin','2026-05-11 11:55:44','',NULL,'授权操作'),(23,5,'导出','5','sys_oper_type','','warning','N','0','admin','2026-05-11 11:55:44','',NULL,'导出操作'),(24,6,'导入','6','sys_oper_type','','warning','N','0','admin','2026-05-11 11:55:44','',NULL,'导入操作'),(25,7,'强退','7','sys_oper_type','','danger','N','0','admin','2026-05-11 11:55:44','',NULL,'强退操作'),(26,8,'生成代码','8','sys_oper_type','','warning','N','0','admin','2026-05-11 11:55:44','',NULL,'生成操作'),(27,9,'清空数据','9','sys_oper_type','','danger','N','0','admin','2026-05-11 11:55:44','',NULL,'清空操作'),(28,1,'成功','0','sys_common_status','','primary','N','0','admin','2026-05-11 11:55:44','',NULL,'正常状态'),(29,2,'失败','1','sys_common_status','','danger','N','0','admin','2026-05-11 11:55:44','',NULL,'停用状态'),(2101,1,'高优先级','high','audit_review_priority',NULL,'danger','N','0','admin','2026-05-11 11:55:58','',NULL,NULL),(2102,2,'中优先级','medium','audit_review_priority',NULL,'warning','Y','0','admin','2026-05-11 11:55:58','',NULL,NULL),(2103,3,'低优先级','low','audit_review_priority',NULL,'info','N','0','admin','2026-05-11 11:55:58','',NULL,NULL),(2201,1,'已上传','uploaded','audit_review_task_status',NULL,'warning','N','0','admin','2026-05-11 11:55:58','',NULL,NULL),(2202,2,'已解析','parsed','audit_review_task_status',NULL,'primary','N','0','admin','2026-05-11 11:55:58','',NULL,NULL),(2203,3,'已检测','detected','audit_review_task_status',NULL,'success','N','0','admin','2026-05-11 11:55:58','',NULL,NULL),(2204,4,'已暂停','paused','audit_review_task_status',NULL,'info','N','0','admin','2026-05-11 11:55:58','',NULL,NULL),(2301,1,'审核通过','approved','audit_review_status',NULL,'success','N','0','admin','2026-05-11 11:55:58','',NULL,NULL),(2302,2,'待修改','pending','audit_review_status',NULL,'warning','Y','0','admin','2026-05-11 11:55:58','',NULL,NULL),(2303,3,'驳回归档','returned','audit_review_status',NULL,'danger','N','0','admin','2026-05-11 11:55:58','',NULL,NULL),(2311,1,'执行中','executing','audit_ai_task_status',NULL,'primary','N','0','admin','2026-05-11 11:56:13','',NULL,NULL),(2312,2,'等待中','waiting','audit_ai_task_status',NULL,'warning','Y','0','admin','2026-05-11 11:56:13','',NULL,NULL),(2313,3,'已暂停','paused','audit_ai_task_status',NULL,'info','N','0','admin','2026-05-11 11:56:13','',NULL,NULL),(2314,4,'已完成','completed','audit_ai_task_status',NULL,'success','N','0','admin','2026-05-11 11:56:13','',NULL,NULL),(2402,4,'已向量化','stored','audit_file_storage_status',NULL,'success','N','0','admin','2026-05-11 11:56:07','',NULL,NULL),(2403,6,'向量化失败','failed','audit_file_storage_status',NULL,'danger','N','0','admin','2026-05-11 11:56:07','',NULL,NULL),(2404,1,'等待向量化','pending','audit_file_storage_status',NULL,'info','Y','0','admin','2026-05-11 11:56:07','',NULL,NULL),(2405,2,'解析中','parsing','audit_file_storage_status',NULL,'primary','N','0','admin','2026-05-11 11:56:07','',NULL,NULL),(2406,3,'向量生成中','embedding','audit_file_storage_status',NULL,'warning','N','0','admin','2026-05-11 11:56:07','',NULL,NULL),(2407,5,'未识别文本','text_empty','audit_file_storage_status',NULL,'danger','N','0','admin','2026-05-11 11:56:07','',NULL,NULL),(2411,1,'归集处理中','processing','audit_task_collect_status',NULL,'primary','N','0','admin','2026-05-11 11:56:07','',NULL,NULL),(2412,2,'已归集','archived','audit_task_collect_status',NULL,'success','Y','0','admin','2026-05-11 11:56:07','',NULL,NULL),(2413,3,'归集失败','failed','audit_task_collect_status',NULL,'danger','N','0','admin','2026-05-11 11:56:07','',NULL,NULL);
/*!40000 ALTER TABLE `sys_dict_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dict_type`
--

DROP TABLE IF EXISTS `sys_dict_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_type` (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典类型',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`),
  UNIQUE KEY `dict_type` (`dict_type`)
) ENGINE=InnoDB AUTO_INCREMENT=2014 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典类型表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict_type`
--

LOCK TABLES `sys_dict_type` WRITE;
/*!40000 ALTER TABLE `sys_dict_type` DISABLE KEYS */;
INSERT INTO `sys_dict_type` VALUES (1,'用户性别','sys_user_sex','0','admin','2026-05-11 11:55:44','',NULL,'用户性别列表'),(2,'菜单状态','sys_show_hide','0','admin','2026-05-11 11:55:44','',NULL,'菜单状态列表'),(3,'系统开关','sys_normal_disable','0','admin','2026-05-11 11:55:44','',NULL,'系统开关列表'),(4,'任务状态','sys_job_status','0','admin','2026-05-11 11:55:44','',NULL,'任务状态列表'),(5,'任务分组','sys_job_group','0','admin','2026-05-11 11:55:44','',NULL,'任务分组列表'),(6,'系统是否','sys_yes_no','0','admin','2026-05-11 11:55:44','',NULL,'系统是否列表'),(7,'通知类型','sys_notice_type','0','admin','2026-05-11 11:55:44','',NULL,'通知类型列表'),(8,'通知状态','sys_notice_status','0','admin','2026-05-11 11:55:44','',NULL,'通知状态列表'),(9,'操作类型','sys_oper_type','0','admin','2026-05-11 11:55:44','',NULL,'操作类型列表'),(10,'系统状态','sys_common_status','0','admin','2026-05-11 11:55:44','',NULL,'登录状态列表'),(2001,'审核优先级','audit_review_priority','0','admin','2026-05-11 11:55:58','',NULL,'审核列表管理'),(2002,'审核任务状态','audit_review_task_status','0','admin','2026-05-11 11:55:58','',NULL,'审核列表管理'),(2003,'审核状态','audit_review_status','0','admin','2026-05-11 11:55:58','',NULL,'审核列表管理'),(2011,'AI任务状态','audit_ai_task_status','0','admin','2026-05-11 11:56:13','',NULL,'AI审核管理'),(2012,'向量化状态','audit_file_storage_status','0','admin','2026-05-11 11:56:07','',NULL,'审核资源库'),(2013,'任务文件采集状态','audit_task_collect_status','0','admin','2026-05-11 11:56:07','',NULL,'审核资源库');
/*!40000 ALTER TABLE `sys_dict_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job`
--

DROP TABLE IF EXISTS `sys_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job` (
  `job_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) COLLATE utf8mb4_general_ci DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`job_id`,`job_name`,`job_group`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='定时任务调度表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job`
--

LOCK TABLES `sys_job` WRITE;
/*!40000 ALTER TABLE `sys_job` DISABLE KEYS */;
INSERT INTO `sys_job` VALUES (1,'系统默认（无参）','DEFAULT','ryTask.ryNoParams','0/10 * * * * ?','3','1','1','admin','2026-05-11 11:55:44','',NULL,''),(2,'系统默认（有参）','DEFAULT','ryTask.ryParams(\'ry\')','0/15 * * * * ?','3','1','1','admin','2026-05-11 11:55:44','',NULL,''),(3,'系统默认（多参）','DEFAULT','ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)','0/20 * * * * ?','3','1','1','admin','2026-05-11 11:55:44','',NULL,''),(20,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','0 */1 * * * ?','3','1','0','admin','2026-05-11 11:56:13','',NULL,'扫描等待中的AI审核任务并调用FastGPT分析');
/*!40000 ALTER TABLE `sys_job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job_log`
--

DROP TABLE IF EXISTS `sys_job_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job_log` (
  `job_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '日志信息',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '异常信息',
  `start_time` datetime DEFAULT NULL COMMENT '执行开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '执行结束时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1172 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='定时任务调度日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job_log`
--

LOCK TABLES `sys_job_log` WRITE;
/*!40000 ALTER TABLE `sys_job_log` DISABLE KEYS */;
INSERT INTO `sys_job_log` VALUES (1,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3491毫秒','0','','2026-05-11 13:18:00','2026-05-11 13:18:04','2026-05-11 13:18:03'),(2,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 13:19:00','2026-05-11 13:19:00','2026-05-11 13:19:00'),(3,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 13:20:00','2026-05-11 13:20:00','2026-05-11 13:20:00'),(4,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 13:21:00','2026-05-11 13:21:00','2026-05-11 13:21:00'),(5,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 13:22:00','2026-05-11 13:22:00','2026-05-11 13:22:00'),(6,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 13:23:00','2026-05-11 13:23:00','2026-05-11 13:23:00'),(7,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 13:24:00','2026-05-11 13:24:00','2026-05-11 13:24:00'),(8,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 13:25:00','2026-05-11 13:25:00','2026-05-11 13:25:00'),(9,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 13:26:00','2026-05-11 13:26:00','2026-05-11 13:26:00'),(10,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 13:27:00','2026-05-11 13:27:00','2026-05-11 13:27:00'),(11,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 13:28:00','2026-05-11 13:28:00','2026-05-11 13:28:00'),(12,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 13:29:00','2026-05-11 13:29:00','2026-05-11 13:29:00'),(13,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 13:30:00','2026-05-11 13:30:00','2026-05-11 13:30:00'),(14,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 13:31:00','2026-05-11 13:31:00','2026-05-11 13:31:00'),(15,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：21毫秒','0','','2026-05-11 13:32:00','2026-05-11 13:32:00','2026-05-11 13:32:00'),(16,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：41毫秒','0','','2026-05-11 13:33:00','2026-05-11 13:33:00','2026-05-11 13:33:00'),(17,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：15333毫秒','0','','2026-05-11 13:34:00','2026-05-11 13:34:15','2026-05-11 13:34:15'),(18,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：20毫秒','0','','2026-05-11 13:35:00','2026-05-11 13:35:00','2026-05-11 13:35:00'),(19,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：18毫秒','0','','2026-05-11 13:36:00','2026-05-11 13:36:00','2026-05-11 13:36:00'),(20,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：18毫秒','0','','2026-05-11 13:37:00','2026-05-11 13:37:00','2026-05-11 13:37:00'),(21,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：20毫秒','0','','2026-05-11 13:38:00','2026-05-11 13:38:00','2026-05-11 13:38:00'),(22,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：15毫秒','0','','2026-05-11 13:39:00','2026-05-11 13:39:00','2026-05-11 13:39:00'),(23,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 13:40:00','2026-05-11 13:40:00','2026-05-11 13:40:00'),(24,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 13:41:00','2026-05-11 13:41:00','2026-05-11 13:41:00'),(25,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 13:42:00','2026-05-11 13:42:00','2026-05-11 13:42:00'),(26,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 13:43:00','2026-05-11 13:43:00','2026-05-11 13:43:00'),(27,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 13:44:00','2026-05-11 13:44:00','2026-05-11 13:44:00'),(28,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 13:45:00','2026-05-11 13:45:00','2026-05-11 13:45:00'),(29,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 13:46:00','2026-05-11 13:46:00','2026-05-11 13:46:00'),(30,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 13:47:00','2026-05-11 13:47:00','2026-05-11 13:47:00'),(31,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 13:48:00','2026-05-11 13:48:00','2026-05-11 13:48:00'),(32,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 13:49:00','2026-05-11 13:49:00','2026-05-11 13:49:00'),(33,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 13:50:00','2026-05-11 13:50:00','2026-05-11 13:50:00'),(34,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 13:51:00','2026-05-11 13:51:00','2026-05-11 13:51:00'),(35,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 13:52:00','2026-05-11 13:52:00','2026-05-11 13:52:00'),(36,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 13:53:00','2026-05-11 13:53:00','2026-05-11 13:53:00'),(37,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 13:54:00','2026-05-11 13:54:00','2026-05-11 13:54:00'),(38,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 13:55:00','2026-05-11 13:55:00','2026-05-11 13:55:00'),(39,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 13:56:00','2026-05-11 13:56:00','2026-05-11 13:56:00'),(40,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 13:57:00','2026-05-11 13:57:00','2026-05-11 13:57:00'),(41,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 13:58:00','2026-05-11 13:58:00','2026-05-11 13:58:00'),(42,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 13:59:00','2026-05-11 13:59:00','2026-05-11 13:59:00'),(43,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 14:00:00','2026-05-11 14:00:00','2026-05-11 14:00:00'),(44,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 14:01:00','2026-05-11 14:01:00','2026-05-11 14:01:00'),(45,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 14:02:00','2026-05-11 14:02:00','2026-05-11 14:02:00'),(46,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 14:03:00','2026-05-11 14:03:00','2026-05-11 14:03:00'),(47,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 14:04:00','2026-05-11 14:04:00','2026-05-11 14:04:00'),(48,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 14:05:00','2026-05-11 14:05:00','2026-05-11 14:05:00'),(49,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 14:06:00','2026-05-11 14:06:00','2026-05-11 14:06:00'),(50,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 14:07:00','2026-05-11 14:07:00','2026-05-11 14:07:00'),(51,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 14:08:00','2026-05-11 14:08:00','2026-05-11 14:08:00'),(52,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 14:09:00','2026-05-11 14:09:00','2026-05-11 14:09:00'),(53,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 14:10:00','2026-05-11 14:10:00','2026-05-11 14:10:00'),(54,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 14:11:00','2026-05-11 14:11:00','2026-05-11 14:11:00'),(55,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 14:12:00','2026-05-11 14:12:00','2026-05-11 14:12:00'),(56,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 14:13:00','2026-05-11 14:13:00','2026-05-11 14:13:00'),(57,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 14:14:00','2026-05-11 14:14:00','2026-05-11 14:14:00'),(58,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 14:15:00','2026-05-11 14:15:00','2026-05-11 14:15:00'),(59,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 14:16:00','2026-05-11 14:16:00','2026-05-11 14:16:00'),(60,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：43毫秒','0','','2026-05-11 14:17:00','2026-05-11 14:17:00','2026-05-11 14:17:00'),(61,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 14:18:00','2026-05-11 14:18:00','2026-05-11 14:18:00'),(62,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 14:19:00','2026-05-11 14:19:00','2026-05-11 14:19:00'),(63,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 14:20:00','2026-05-11 14:20:00','2026-05-11 14:20:00'),(64,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 14:21:00','2026-05-11 14:21:00','2026-05-11 14:21:00'),(65,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：15730毫秒','0','','2026-05-11 14:22:00','2026-05-11 14:22:16','2026-05-11 14:22:15'),(66,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：22毫秒','0','','2026-05-11 14:23:00','2026-05-11 14:23:00','2026-05-11 14:23:00'),(67,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 14:24:00','2026-05-11 14:24:00','2026-05-11 14:24:00'),(68,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 14:25:00','2026-05-11 14:25:00','2026-05-11 14:25:00'),(69,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 14:26:00','2026-05-11 14:26:00','2026-05-11 14:26:00'),(70,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 14:27:00','2026-05-11 14:27:00','2026-05-11 14:27:00'),(71,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 14:28:00','2026-05-11 14:28:00','2026-05-11 14:28:00'),(72,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 14:29:00','2026-05-11 14:29:00','2026-05-11 14:29:00'),(73,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 14:30:00','2026-05-11 14:30:00','2026-05-11 14:30:00'),(74,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 14:31:00','2026-05-11 14:31:00','2026-05-11 14:31:00'),(75,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 14:32:00','2026-05-11 14:32:00','2026-05-11 14:32:00'),(76,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 14:33:00','2026-05-11 14:33:00','2026-05-11 14:33:00'),(77,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 14:34:00','2026-05-11 14:34:00','2026-05-11 14:34:00'),(78,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 14:35:00','2026-05-11 14:35:00','2026-05-11 14:35:00'),(79,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 14:36:00','2026-05-11 14:36:00','2026-05-11 14:36:00'),(80,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 14:37:00','2026-05-11 14:37:00','2026-05-11 14:37:00'),(81,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 14:38:00','2026-05-11 14:38:00','2026-05-11 14:38:00'),(82,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 14:39:00','2026-05-11 14:39:00','2026-05-11 14:39:00'),(83,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 14:40:00','2026-05-11 14:40:00','2026-05-11 14:40:00'),(84,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 14:41:00','2026-05-11 14:41:00','2026-05-11 14:41:00'),(85,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 14:42:00','2026-05-11 14:42:00','2026-05-11 14:42:00'),(86,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12199毫秒','0','','2026-05-11 14:43:00','2026-05-11 14:43:12','2026-05-11 14:43:12'),(87,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 14:44:00','2026-05-11 14:44:00','2026-05-11 14:44:00'),(88,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：17毫秒','0','','2026-05-11 14:45:00','2026-05-11 14:45:00','2026-05-11 14:45:00'),(89,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 14:46:00','2026-05-11 14:46:00','2026-05-11 14:46:00'),(90,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 14:47:00','2026-05-11 14:47:00','2026-05-11 14:47:00'),(91,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 14:48:00','2026-05-11 14:48:00','2026-05-11 14:48:00'),(92,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 14:49:00','2026-05-11 14:49:00','2026-05-11 14:49:00'),(93,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 14:50:00','2026-05-11 14:50:00','2026-05-11 14:50:00'),(94,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 14:51:00','2026-05-11 14:51:00','2026-05-11 14:51:00'),(95,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 14:52:00','2026-05-11 14:52:00','2026-05-11 14:52:00'),(96,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 14:53:00','2026-05-11 14:53:00','2026-05-11 14:53:00'),(97,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 14:54:00','2026-05-11 14:54:00','2026-05-11 14:54:00'),(98,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 14:55:00','2026-05-11 14:55:00','2026-05-11 14:55:00'),(99,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 14:56:00','2026-05-11 14:56:00','2026-05-11 14:56:00'),(100,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 14:57:00','2026-05-11 14:57:00','2026-05-11 14:57:00'),(101,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 14:58:00','2026-05-11 14:58:00','2026-05-11 14:58:00'),(102,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 14:59:00','2026-05-11 14:59:00','2026-05-11 14:59:00'),(103,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 15:00:00','2026-05-11 15:00:00','2026-05-11 15:00:00'),(104,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 15:01:00','2026-05-11 15:01:00','2026-05-11 15:01:00'),(105,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 15:02:00','2026-05-11 15:02:00','2026-05-11 15:02:00'),(106,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 15:03:00','2026-05-11 15:03:00','2026-05-11 15:03:00'),(107,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 15:04:00','2026-05-11 15:04:00','2026-05-11 15:04:00'),(108,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 15:05:00','2026-05-11 15:05:00','2026-05-11 15:05:00'),(109,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：26毫秒','0','','2026-05-11 15:06:00','2026-05-11 15:06:00','2026-05-11 15:06:00'),(110,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：15270毫秒','0','','2026-05-11 15:07:00','2026-05-11 15:07:15','2026-05-11 15:07:15'),(111,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：30毫秒','0','','2026-05-11 15:08:00','2026-05-11 15:08:00','2026-05-11 15:08:00'),(112,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：22毫秒','0','','2026-05-11 15:09:00','2026-05-11 15:09:00','2026-05-11 15:09:00'),(113,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：17毫秒','0','','2026-05-11 15:10:00','2026-05-11 15:10:00','2026-05-11 15:10:00'),(114,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 15:11:00','2026-05-11 15:11:00','2026-05-11 15:11:00'),(115,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：18223毫秒','0','','2026-05-11 15:12:00','2026-05-11 15:12:18','2026-05-11 15:12:18'),(116,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：18毫秒','0','','2026-05-11 15:13:00','2026-05-11 15:13:00','2026-05-11 15:13:00'),(117,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：16毫秒','0','','2026-05-11 15:14:00','2026-05-11 15:14:00','2026-05-11 15:14:00'),(118,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：16毫秒','0','','2026-05-11 15:15:00','2026-05-11 15:15:00','2026-05-11 15:15:00'),(119,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：17毫秒','0','','2026-05-11 15:16:00','2026-05-11 15:16:00','2026-05-11 15:16:00'),(120,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 15:17:00','2026-05-11 15:17:00','2026-05-11 15:17:00'),(121,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：17毫秒','0','','2026-05-11 15:18:00','2026-05-11 15:18:00','2026-05-11 15:18:00'),(122,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：27毫秒','0','','2026-05-11 15:19:00','2026-05-11 15:19:00','2026-05-11 15:19:00'),(123,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 15:20:00','2026-05-11 15:20:00','2026-05-11 15:20:00'),(124,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 15:21:00','2026-05-11 15:21:00','2026-05-11 15:21:00'),(125,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 15:22:00','2026-05-11 15:22:00','2026-05-11 15:22:00'),(126,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 15:23:00','2026-05-11 15:23:00','2026-05-11 15:23:00'),(127,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 15:24:00','2026-05-11 15:24:00','2026-05-11 15:24:00'),(128,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 15:25:00','2026-05-11 15:25:00','2026-05-11 15:25:00'),(129,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 15:26:00','2026-05-11 15:26:00','2026-05-11 15:26:00'),(130,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 15:27:00','2026-05-11 15:27:00','2026-05-11 15:27:00'),(131,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 15:28:00','2026-05-11 15:28:00','2026-05-11 15:28:00'),(132,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 15:29:00','2026-05-11 15:29:00','2026-05-11 15:29:00'),(133,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 15:30:00','2026-05-11 15:30:00','2026-05-11 15:30:00'),(134,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 15:31:00','2026-05-11 15:31:00','2026-05-11 15:31:00'),(135,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 15:32:00','2026-05-11 15:32:00','2026-05-11 15:32:00'),(136,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 15:33:00','2026-05-11 15:33:00','2026-05-11 15:33:00'),(137,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 15:34:00','2026-05-11 15:34:00','2026-05-11 15:34:00'),(138,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 15:35:00','2026-05-11 15:35:00','2026-05-11 15:35:00'),(139,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 15:36:00','2026-05-11 15:36:00','2026-05-11 15:36:00'),(140,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 15:37:00','2026-05-11 15:37:00','2026-05-11 15:37:00'),(141,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 15:38:00','2026-05-11 15:38:00','2026-05-11 15:38:00'),(142,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 15:39:00','2026-05-11 15:39:00','2026-05-11 15:39:00'),(143,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 15:40:00','2026-05-11 15:40:00','2026-05-11 15:40:00'),(144,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 15:41:00','2026-05-11 15:41:00','2026-05-11 15:41:00'),(145,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 15:42:00','2026-05-11 15:42:00','2026-05-11 15:42:00'),(146,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 15:43:00','2026-05-11 15:43:00','2026-05-11 15:43:00'),(147,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 15:44:00','2026-05-11 15:44:00','2026-05-11 15:44:00'),(148,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 15:45:00','2026-05-11 15:45:00','2026-05-11 15:45:00'),(149,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 15:46:00','2026-05-11 15:46:00','2026-05-11 15:46:00'),(150,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 15:47:00','2026-05-11 15:47:00','2026-05-11 15:47:00'),(151,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 15:48:00','2026-05-11 15:48:00','2026-05-11 15:48:00'),(152,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 15:49:00','2026-05-11 15:49:00','2026-05-11 15:49:00'),(153,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 15:50:00','2026-05-11 15:50:00','2026-05-11 15:50:00'),(154,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 15:51:00','2026-05-11 15:51:00','2026-05-11 15:51:00'),(155,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 15:52:00','2026-05-11 15:52:00','2026-05-11 15:52:00'),(156,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 15:53:00','2026-05-11 15:53:00','2026-05-11 15:53:00'),(157,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 15:54:00','2026-05-11 15:54:00','2026-05-11 15:54:00'),(158,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 15:55:00','2026-05-11 15:55:00','2026-05-11 15:55:00'),(159,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 15:56:00','2026-05-11 15:56:00','2026-05-11 15:56:00'),(160,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 15:57:00','2026-05-11 15:57:00','2026-05-11 15:57:00'),(161,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 15:58:00','2026-05-11 15:58:00','2026-05-11 15:58:00'),(162,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 15:59:00','2026-05-11 15:59:00','2026-05-11 15:59:00'),(163,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 16:00:00','2026-05-11 16:00:00','2026-05-11 16:00:00'),(164,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 16:01:00','2026-05-11 16:01:00','2026-05-11 16:01:00'),(165,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 16:02:00','2026-05-11 16:02:00','2026-05-11 16:02:00'),(166,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 16:03:00','2026-05-11 16:03:00','2026-05-11 16:03:00'),(167,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 16:04:00','2026-05-11 16:04:00','2026-05-11 16:04:00'),(168,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 16:05:00','2026-05-11 16:05:00','2026-05-11 16:05:00'),(169,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 16:06:00','2026-05-11 16:06:00','2026-05-11 16:06:00'),(170,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 16:07:00','2026-05-11 16:07:00','2026-05-11 16:07:00'),(171,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 16:08:00','2026-05-11 16:08:00','2026-05-11 16:08:00'),(172,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 16:09:00','2026-05-11 16:09:00','2026-05-11 16:09:00'),(173,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 16:10:00','2026-05-11 16:10:00','2026-05-11 16:10:00'),(174,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 16:11:00','2026-05-11 16:11:00','2026-05-11 16:11:00'),(175,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 16:12:00','2026-05-11 16:12:00','2026-05-11 16:12:00'),(176,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 16:13:00','2026-05-11 16:13:00','2026-05-11 16:13:00'),(177,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 16:14:00','2026-05-11 16:14:00','2026-05-11 16:14:00'),(178,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 16:15:00','2026-05-11 16:15:00','2026-05-11 16:15:00'),(179,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 16:16:00','2026-05-11 16:16:00','2026-05-11 16:16:00'),(180,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 16:17:00','2026-05-11 16:17:00','2026-05-11 16:17:00'),(181,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 16:18:00','2026-05-11 16:18:00','2026-05-11 16:18:00'),(182,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 16:19:00','2026-05-11 16:19:00','2026-05-11 16:19:00'),(183,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 16:20:00','2026-05-11 16:20:00','2026-05-11 16:20:00'),(184,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 16:21:00','2026-05-11 16:21:00','2026-05-11 16:21:00'),(185,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 16:22:00','2026-05-11 16:22:00','2026-05-11 16:22:00'),(186,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 16:23:00','2026-05-11 16:23:00','2026-05-11 16:23:00'),(187,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 16:24:00','2026-05-11 16:24:00','2026-05-11 16:24:00'),(188,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 16:25:00','2026-05-11 16:25:00','2026-05-11 16:25:00'),(189,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 16:26:00','2026-05-11 16:26:00','2026-05-11 16:26:00'),(190,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 16:27:00','2026-05-11 16:27:00','2026-05-11 16:27:00'),(191,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 16:28:00','2026-05-11 16:28:00','2026-05-11 16:28:00'),(192,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 16:29:00','2026-05-11 16:29:00','2026-05-11 16:29:00'),(193,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 16:30:00','2026-05-11 16:30:00','2026-05-11 16:30:00'),(194,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 16:31:00','2026-05-11 16:31:00','2026-05-11 16:31:00'),(195,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 16:32:00','2026-05-11 16:32:00','2026-05-11 16:32:00'),(196,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 16:33:00','2026-05-11 16:33:00','2026-05-11 16:33:00'),(197,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：64毫秒','0','','2026-05-11 16:34:00','2026-05-11 16:34:00','2026-05-11 16:34:00'),(198,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：29毫秒','0','','2026-05-11 16:35:00','2026-05-11 16:35:00','2026-05-11 16:35:00'),(199,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 16:36:00','2026-05-11 16:36:00','2026-05-11 16:36:00'),(200,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 16:37:00','2026-05-11 16:37:00','2026-05-11 16:37:00'),(201,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 16:38:00','2026-05-11 16:38:00','2026-05-11 16:38:00'),(202,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 16:39:00','2026-05-11 16:39:00','2026-05-11 16:39:00'),(203,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 16:40:00','2026-05-11 16:40:00','2026-05-11 16:40:00'),(204,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 16:41:00','2026-05-11 16:41:00','2026-05-11 16:41:00'),(205,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 16:42:00','2026-05-11 16:42:00','2026-05-11 16:42:00'),(206,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 16:43:00','2026-05-11 16:43:00','2026-05-11 16:43:00'),(207,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：2毫秒','0','','2026-05-11 16:44:00','2026-05-11 16:44:00','2026-05-11 16:44:00'),(208,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 16:45:00','2026-05-11 16:45:00','2026-05-11 16:45:00'),(209,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 16:46:00','2026-05-11 16:46:00','2026-05-11 16:46:00'),(210,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 16:47:00','2026-05-11 16:47:00','2026-05-11 16:47:00'),(211,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：16毫秒','0','','2026-05-11 16:48:00','2026-05-11 16:48:00','2026-05-11 16:48:00'),(212,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 16:49:00','2026-05-11 16:49:00','2026-05-11 16:49:00'),(213,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 16:50:00','2026-05-11 16:50:00','2026-05-11 16:50:00'),(214,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 16:51:00','2026-05-11 16:51:00','2026-05-11 16:51:00'),(215,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 16:52:00','2026-05-11 16:52:00','2026-05-11 16:52:00'),(216,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 16:53:00','2026-05-11 16:53:00','2026-05-11 16:53:00'),(217,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 16:54:00','2026-05-11 16:54:00','2026-05-11 16:54:00'),(218,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 16:55:00','2026-05-11 16:55:00','2026-05-11 16:55:00'),(219,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 16:56:00','2026-05-11 16:56:00','2026-05-11 16:56:00'),(220,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 16:57:00','2026-05-11 16:57:00','2026-05-11 16:57:00'),(221,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 16:58:00','2026-05-11 16:58:00','2026-05-11 16:58:00'),(222,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 16:59:00','2026-05-11 16:59:00','2026-05-11 16:59:00'),(223,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 17:00:00','2026-05-11 17:00:00','2026-05-11 17:00:00'),(224,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 17:01:00','2026-05-11 17:01:00','2026-05-11 17:01:00'),(225,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 17:02:00','2026-05-11 17:02:00','2026-05-11 17:02:00'),(226,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 17:03:00','2026-05-11 17:03:00','2026-05-11 17:03:00'),(227,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 17:04:00','2026-05-11 17:04:00','2026-05-11 17:04:00'),(228,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 17:05:00','2026-05-11 17:05:00','2026-05-11 17:05:00'),(229,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 17:06:00','2026-05-11 17:06:00','2026-05-11 17:06:00'),(230,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 17:07:00','2026-05-11 17:07:00','2026-05-11 17:07:00'),(231,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 17:08:00','2026-05-11 17:08:00','2026-05-11 17:08:00'),(232,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 17:09:00','2026-05-11 17:09:00','2026-05-11 17:09:00'),(233,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 17:10:00','2026-05-11 17:10:00','2026-05-11 17:10:00'),(234,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 17:11:00','2026-05-11 17:11:00','2026-05-11 17:11:00'),(235,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 17:12:00','2026-05-11 17:12:00','2026-05-11 17:12:00'),(236,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 17:13:00','2026-05-11 17:13:00','2026-05-11 17:13:00'),(237,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 17:14:00','2026-05-11 17:14:00','2026-05-11 17:14:00'),(238,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 17:15:00','2026-05-11 17:15:00','2026-05-11 17:15:00'),(239,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 17:16:00','2026-05-11 17:16:00','2026-05-11 17:16:00'),(240,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 17:17:00','2026-05-11 17:17:00','2026-05-11 17:17:00'),(241,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 17:18:00','2026-05-11 17:18:00','2026-05-11 17:18:00'),(242,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 17:19:00','2026-05-11 17:19:00','2026-05-11 17:19:00'),(243,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 17:20:00','2026-05-11 17:20:00','2026-05-11 17:20:00'),(244,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 17:21:00','2026-05-11 17:21:00','2026-05-11 17:21:00'),(245,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 17:22:00','2026-05-11 17:22:00','2026-05-11 17:22:00'),(246,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 17:23:00','2026-05-11 17:23:00','2026-05-11 17:23:00'),(247,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 17:24:00','2026-05-11 17:24:00','2026-05-11 17:24:00'),(248,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 17:25:00','2026-05-11 17:25:00','2026-05-11 17:25:00'),(249,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 17:26:00','2026-05-11 17:26:00','2026-05-11 17:26:00'),(250,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 17:27:00','2026-05-11 17:27:00','2026-05-11 17:27:00'),(251,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 17:28:00','2026-05-11 17:28:00','2026-05-11 17:28:00'),(252,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 17:29:00','2026-05-11 17:29:00','2026-05-11 17:29:00'),(253,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：23毫秒','0','','2026-05-11 17:30:00','2026-05-11 17:30:00','2026-05-11 17:30:00'),(254,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 17:31:00','2026-05-11 17:31:00','2026-05-11 17:31:00'),(255,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 17:32:00','2026-05-11 17:32:00','2026-05-11 17:32:00'),(256,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：23毫秒','0','','2026-05-11 17:33:00','2026-05-11 17:33:00','2026-05-11 17:33:00'),(257,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：19毫秒','0','','2026-05-11 17:34:00','2026-05-11 17:34:00','2026-05-11 17:34:00'),(258,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 17:35:00','2026-05-11 17:35:00','2026-05-11 17:35:00'),(259,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 17:36:00','2026-05-11 17:36:00','2026-05-11 17:36:00'),(260,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 17:37:00','2026-05-11 17:37:00','2026-05-11 17:37:00'),(261,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 17:38:00','2026-05-11 17:38:00','2026-05-11 17:38:00'),(262,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：23毫秒','0','','2026-05-11 17:39:00','2026-05-11 17:39:00','2026-05-11 17:39:00'),(263,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 17:40:00','2026-05-11 17:40:00','2026-05-11 17:40:00'),(264,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：31毫秒','0','','2026-05-11 17:41:00','2026-05-11 17:41:00','2026-05-11 17:41:00'),(265,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：16毫秒','0','','2026-05-11 17:42:00','2026-05-11 17:42:00','2026-05-11 17:42:00'),(266,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 17:43:00','2026-05-11 17:43:00','2026-05-11 17:43:00'),(267,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 17:44:00','2026-05-11 17:44:00','2026-05-11 17:44:00'),(268,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：21毫秒','0','','2026-05-11 17:45:00','2026-05-11 17:45:00','2026-05-11 17:45:00'),(269,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：21毫秒','0','','2026-05-11 17:46:00','2026-05-11 17:46:00','2026-05-11 17:46:00'),(270,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 17:47:00','2026-05-11 17:47:00','2026-05-11 17:47:00'),(271,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：19毫秒','0','','2026-05-11 17:48:00','2026-05-11 17:48:00','2026-05-11 17:48:00'),(272,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：16毫秒','0','','2026-05-11 17:49:00','2026-05-11 17:49:00','2026-05-11 17:49:00'),(273,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 17:50:00','2026-05-11 17:50:00','2026-05-11 17:50:00'),(274,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：17毫秒','0','','2026-05-11 17:51:00','2026-05-11 17:51:00','2026-05-11 17:51:00'),(275,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 17:52:00','2026-05-11 17:52:00','2026-05-11 17:52:00'),(276,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：16毫秒','0','','2026-05-11 17:53:00','2026-05-11 17:53:00','2026-05-11 17:53:00'),(277,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：13毫秒','0','','2026-05-11 17:54:00','2026-05-11 17:54:00','2026-05-11 17:54:00'),(278,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：15毫秒','0','','2026-05-11 17:55:00','2026-05-11 17:55:00','2026-05-11 17:55:00'),(279,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 17:56:00','2026-05-11 17:56:00','2026-05-11 17:56:00'),(280,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 17:57:00','2026-05-11 17:57:00','2026-05-11 17:57:00'),(281,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 17:58:00','2026-05-11 17:58:00','2026-05-11 17:58:00'),(282,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 17:59:00','2026-05-11 17:59:00','2026-05-11 17:59:00'),(283,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：23毫秒','0','','2026-05-11 18:00:00','2026-05-11 18:00:00','2026-05-11 18:00:00'),(284,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：18毫秒','0','','2026-05-11 18:01:00','2026-05-11 18:01:00','2026-05-11 18:01:00'),(285,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 18:02:00','2026-05-11 18:02:00','2026-05-11 18:02:00'),(286,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 18:03:00','2026-05-11 18:03:00','2026-05-11 18:03:00'),(287,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 18:04:00','2026-05-11 18:04:00','2026-05-11 18:04:00'),(288,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 18:05:00','2026-05-11 18:05:00','2026-05-11 18:05:00'),(289,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 18:06:00','2026-05-11 18:06:00','2026-05-11 18:06:00'),(290,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 18:07:00','2026-05-11 18:07:00','2026-05-11 18:07:00'),(291,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 18:08:00','2026-05-11 18:08:00','2026-05-11 18:08:00'),(292,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：16毫秒','0','','2026-05-11 18:09:00','2026-05-11 18:09:00','2026-05-11 18:09:00'),(293,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 18:10:00','2026-05-11 18:10:00','2026-05-11 18:10:00'),(294,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：13毫秒','0','','2026-05-11 18:11:00','2026-05-11 18:11:00','2026-05-11 18:11:00'),(295,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 18:12:00','2026-05-11 18:12:00','2026-05-11 18:12:00'),(296,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 18:13:00','2026-05-11 18:13:00','2026-05-11 18:13:00'),(297,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：17毫秒','0','','2026-05-11 18:14:00','2026-05-11 18:14:00','2026-05-11 18:14:00'),(298,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 18:15:00','2026-05-11 18:15:00','2026-05-11 18:15:00'),(299,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 18:16:00','2026-05-11 18:16:00','2026-05-11 18:16:00'),(300,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：15毫秒','0','','2026-05-11 18:17:00','2026-05-11 18:17:00','2026-05-11 18:17:00'),(301,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：13毫秒','0','','2026-05-11 18:18:00','2026-05-11 18:18:00','2026-05-11 18:18:00'),(302,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：16毫秒','0','','2026-05-11 18:19:00','2026-05-11 18:19:00','2026-05-11 18:19:00'),(303,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：18毫秒','0','','2026-05-11 18:20:00','2026-05-11 18:20:00','2026-05-11 18:20:00'),(304,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：13毫秒','0','','2026-05-11 18:21:00','2026-05-11 18:21:00','2026-05-11 18:21:00'),(305,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：15毫秒','0','','2026-05-11 18:22:00','2026-05-11 18:22:00','2026-05-11 18:22:00'),(306,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 18:23:00','2026-05-11 18:23:00','2026-05-11 18:23:00'),(307,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：16毫秒','0','','2026-05-11 18:24:00','2026-05-11 18:24:00','2026-05-11 18:24:00'),(308,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：18毫秒','0','','2026-05-11 18:25:00','2026-05-11 18:25:00','2026-05-11 18:25:00'),(309,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：15毫秒','0','','2026-05-11 18:26:00','2026-05-11 18:26:00','2026-05-11 18:26:00'),(310,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 18:27:00','2026-05-11 18:27:00','2026-05-11 18:27:00'),(311,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：21毫秒','0','','2026-05-11 18:28:00','2026-05-11 18:28:00','2026-05-11 18:28:00'),(312,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：25毫秒','0','','2026-05-11 18:29:00','2026-05-11 18:29:00','2026-05-11 18:29:00'),(313,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 18:30:00','2026-05-11 18:30:00','2026-05-11 18:30:00'),(314,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：22毫秒','0','','2026-05-11 18:31:00','2026-05-11 18:31:00','2026-05-11 18:31:00'),(315,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：13毫秒','0','','2026-05-11 18:32:00','2026-05-11 18:32:00','2026-05-11 18:32:00'),(316,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 18:33:00','2026-05-11 18:33:00','2026-05-11 18:33:00'),(317,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 18:34:00','2026-05-11 18:34:00','2026-05-11 18:34:00'),(318,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 18:35:00','2026-05-11 18:35:00','2026-05-11 18:35:00'),(319,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 18:36:00','2026-05-11 18:36:00','2026-05-11 18:36:00'),(320,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：13毫秒','0','','2026-05-11 18:37:00','2026-05-11 18:37:00','2026-05-11 18:37:00'),(321,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：13毫秒','0','','2026-05-11 18:38:00','2026-05-11 18:38:00','2026-05-11 18:38:00'),(322,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 18:39:00','2026-05-11 18:39:00','2026-05-11 18:39:00'),(323,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 18:40:00','2026-05-11 18:40:00','2026-05-11 18:40:00'),(324,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：18毫秒','0','','2026-05-11 18:41:00','2026-05-11 18:41:00','2026-05-11 18:41:00'),(325,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：16毫秒','0','','2026-05-11 18:42:00','2026-05-11 18:42:00','2026-05-11 18:42:00'),(326,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 18:43:00','2026-05-11 18:43:00','2026-05-11 18:43:00'),(327,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 18:44:00','2026-05-11 18:44:00','2026-05-11 18:44:00'),(328,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 18:45:00','2026-05-11 18:45:00','2026-05-11 18:45:00'),(329,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 18:46:00','2026-05-11 18:46:00','2026-05-11 18:46:00'),(330,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 18:47:00','2026-05-11 18:47:00','2026-05-11 18:47:00'),(331,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 18:48:00','2026-05-11 18:48:00','2026-05-11 18:48:00'),(332,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 18:49:00','2026-05-11 18:49:00','2026-05-11 18:49:00'),(333,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 18:50:00','2026-05-11 18:50:00','2026-05-11 18:50:00'),(334,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 18:51:00','2026-05-11 18:51:00','2026-05-11 18:51:00'),(335,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：23毫秒','0','','2026-05-11 18:52:00','2026-05-11 18:52:00','2026-05-11 18:52:00'),(336,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：13毫秒','0','','2026-05-11 18:53:00','2026-05-11 18:53:00','2026-05-11 18:53:00'),(337,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 18:54:00','2026-05-11 18:54:00','2026-05-11 18:54:00'),(338,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 18:55:00','2026-05-11 18:55:00','2026-05-11 18:55:00'),(339,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 18:56:00','2026-05-11 18:56:00','2026-05-11 18:56:00'),(340,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 18:57:00','2026-05-11 18:57:00','2026-05-11 18:57:00'),(341,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：31毫秒','0','','2026-05-11 18:58:00','2026-05-11 18:58:00','2026-05-11 18:58:00'),(342,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：16毫秒','0','','2026-05-11 18:59:00','2026-05-11 18:59:00','2026-05-11 18:59:00'),(343,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：20毫秒','0','','2026-05-11 19:00:00','2026-05-11 19:00:00','2026-05-11 19:00:00'),(344,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 19:01:00','2026-05-11 19:01:00','2026-05-11 19:01:00'),(345,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 19:02:00','2026-05-11 19:02:00','2026-05-11 19:02:00'),(346,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：13毫秒','0','','2026-05-11 19:03:00','2026-05-11 19:03:00','2026-05-11 19:03:00'),(347,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 19:04:00','2026-05-11 19:04:00','2026-05-11 19:04:00'),(348,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：15毫秒','0','','2026-05-11 19:05:00','2026-05-11 19:05:00','2026-05-11 19:05:00'),(349,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 19:06:00','2026-05-11 19:06:00','2026-05-11 19:06:00'),(350,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 19:07:00','2026-05-11 19:07:00','2026-05-11 19:07:00'),(351,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：13毫秒','0','','2026-05-11 19:08:00','2026-05-11 19:08:00','2026-05-11 19:08:00'),(352,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 19:09:00','2026-05-11 19:09:00','2026-05-11 19:09:00'),(353,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：17毫秒','0','','2026-05-11 19:10:00','2026-05-11 19:10:00','2026-05-11 19:10:00'),(354,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 19:11:00','2026-05-11 19:11:00','2026-05-11 19:11:00'),(355,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 19:12:00','2026-05-11 19:12:00','2026-05-11 19:12:00'),(356,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 19:13:00','2026-05-11 19:13:00','2026-05-11 19:13:00'),(357,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 19:14:00','2026-05-11 19:14:00','2026-05-11 19:14:00'),(358,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 19:15:00','2026-05-11 19:15:00','2026-05-11 19:15:00'),(359,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 19:16:00','2026-05-11 19:16:00','2026-05-11 19:16:00'),(360,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 19:17:00','2026-05-11 19:17:00','2026-05-11 19:17:00'),(361,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：22毫秒','0','','2026-05-11 19:18:00','2026-05-11 19:18:00','2026-05-11 19:18:00'),(362,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：18毫秒','0','','2026-05-11 19:19:00','2026-05-11 19:19:00','2026-05-11 19:19:00'),(363,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 19:20:00','2026-05-11 19:20:00','2026-05-11 19:20:00'),(364,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-11 19:21:00','2026-05-11 19:21:00','2026-05-11 19:21:00'),(365,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 19:22:00','2026-05-11 19:22:00','2026-05-11 19:22:00'),(366,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 19:23:00','2026-05-11 19:23:00','2026-05-11 19:23:00'),(367,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 19:24:00','2026-05-11 19:24:00','2026-05-11 19:24:00'),(368,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 19:25:00','2026-05-11 19:25:00','2026-05-11 19:25:00'),(369,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 19:26:00','2026-05-11 19:26:00','2026-05-11 19:26:00'),(370,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：16毫秒','0','','2026-05-11 19:27:00','2026-05-11 19:27:00','2026-05-11 19:27:00'),(371,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 19:28:00','2026-05-11 19:28:00','2026-05-11 19:28:00'),(372,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 19:29:00','2026-05-11 19:29:00','2026-05-11 19:29:00'),(373,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：13毫秒','0','','2026-05-11 19:30:00','2026-05-11 19:30:00','2026-05-11 19:30:00'),(374,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 19:31:00','2026-05-11 19:31:00','2026-05-11 19:31:00'),(375,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：16毫秒','0','','2026-05-11 19:32:00','2026-05-11 19:32:00','2026-05-11 19:32:00'),(376,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 19:33:00','2026-05-11 19:33:00','2026-05-11 19:33:00'),(377,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：18毫秒','0','','2026-05-11 19:34:00','2026-05-11 19:34:00','2026-05-11 19:34:00'),(378,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 19:35:00','2026-05-11 19:35:00','2026-05-11 19:35:00'),(379,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 19:36:00','2026-05-11 19:36:00','2026-05-11 19:36:00'),(380,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 19:37:00','2026-05-11 19:37:00','2026-05-11 19:37:00'),(381,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 19:38:00','2026-05-11 19:38:00','2026-05-11 19:38:00'),(382,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：15毫秒','0','','2026-05-11 19:39:00','2026-05-11 19:39:00','2026-05-11 19:39:00'),(383,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：15毫秒','0','','2026-05-11 19:40:00','2026-05-11 19:40:00','2026-05-11 19:40:00'),(384,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 19:41:00','2026-05-11 19:41:00','2026-05-11 19:41:00'),(385,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：15毫秒','0','','2026-05-11 19:42:00','2026-05-11 19:42:00','2026-05-11 19:42:00'),(386,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：16毫秒','0','','2026-05-11 19:43:00','2026-05-11 19:43:00','2026-05-11 19:43:00'),(387,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：17毫秒','0','','2026-05-11 19:44:00','2026-05-11 19:44:00','2026-05-11 19:44:00'),(388,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 19:45:00','2026-05-11 19:45:00','2026-05-11 19:45:00'),(389,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 19:46:00','2026-05-11 19:46:00','2026-05-11 19:46:00'),(390,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：19毫秒','0','','2026-05-11 19:47:00','2026-05-11 19:47:00','2026-05-11 19:47:00'),(391,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 19:48:00','2026-05-11 19:48:00','2026-05-11 19:48:00'),(392,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 19:49:00','2026-05-11 19:49:00','2026-05-11 19:49:00'),(393,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-11 19:50:00','2026-05-11 19:50:00','2026-05-11 19:50:00'),(394,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 19:51:00','2026-05-11 19:51:00','2026-05-11 19:51:00'),(395,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 19:52:00','2026-05-11 19:52:00','2026-05-11 19:52:00'),(396,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-11 19:53:00','2026-05-11 19:53:00','2026-05-11 19:53:00'),(397,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：13毫秒','0','','2026-05-11 19:54:00','2026-05-11 19:54:00','2026-05-11 19:54:00'),(398,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：18毫秒','0','','2026-05-11 19:55:00','2026-05-11 19:55:00','2026-05-11 19:55:00'),(399,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：18毫秒','0','','2026-05-11 19:56:00','2026-05-11 19:56:00','2026-05-11 19:56:00'),(400,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：17毫秒','0','','2026-05-11 19:57:00','2026-05-11 19:57:00','2026-05-11 19:57:00'),(401,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 19:58:00','2026-05-11 19:58:00','2026-05-11 19:58:00'),(402,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：19毫秒','0','','2026-05-11 19:59:00','2026-05-11 19:59:00','2026-05-11 19:59:00'),(403,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 20:00:00','2026-05-11 20:00:00','2026-05-11 20:00:00'),(404,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:01:00','2026-05-11 20:01:00','2026-05-11 20:01:00'),(405,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:02:00','2026-05-11 20:02:00','2026-05-11 20:02:00'),(406,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 20:03:00','2026-05-11 20:03:00','2026-05-11 20:03:00'),(407,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 20:04:00','2026-05-11 20:04:00','2026-05-11 20:04:00'),(408,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：2毫秒','0','','2026-05-11 20:05:00','2026-05-11 20:05:00','2026-05-11 20:05:00'),(409,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 20:06:00','2026-05-11 20:06:00','2026-05-11 20:06:00'),(410,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 20:07:00','2026-05-11 20:07:00','2026-05-11 20:07:00'),(411,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:08:00','2026-05-11 20:08:00','2026-05-11 20:08:00'),(412,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:09:00','2026-05-11 20:09:00','2026-05-11 20:09:00'),(413,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:10:00','2026-05-11 20:10:00','2026-05-11 20:10:00'),(414,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:11:00','2026-05-11 20:11:00','2026-05-11 20:11:00'),(415,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 20:12:00','2026-05-11 20:12:00','2026-05-11 20:12:00'),(416,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:13:00','2026-05-11 20:13:00','2026-05-11 20:13:00'),(417,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:14:00','2026-05-11 20:14:00','2026-05-11 20:14:00'),(418,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 20:15:00','2026-05-11 20:15:00','2026-05-11 20:15:00'),(419,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 20:16:00','2026-05-11 20:16:00','2026-05-11 20:16:00'),(420,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:17:00','2026-05-11 20:17:00','2026-05-11 20:17:00'),(421,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:18:00','2026-05-11 20:18:00','2026-05-11 20:18:00'),(422,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 20:19:00','2026-05-11 20:19:00','2026-05-11 20:19:00'),(423,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:20:00','2026-05-11 20:20:00','2026-05-11 20:20:00'),(424,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:21:00','2026-05-11 20:21:00','2026-05-11 20:21:00'),(425,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 20:22:00','2026-05-11 20:22:00','2026-05-11 20:22:00'),(426,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:23:00','2026-05-11 20:23:00','2026-05-11 20:23:00'),(427,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:24:00','2026-05-11 20:24:00','2026-05-11 20:24:00'),(428,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:25:00','2026-05-11 20:25:00','2026-05-11 20:25:00'),(429,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 20:26:00','2026-05-11 20:26:00','2026-05-11 20:26:00'),(430,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:27:00','2026-05-11 20:27:00','2026-05-11 20:27:00'),(431,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:28:00','2026-05-11 20:28:00','2026-05-11 20:28:00'),(432,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:29:00','2026-05-11 20:29:00','2026-05-11 20:29:00'),(433,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:30:00','2026-05-11 20:30:00','2026-05-11 20:30:00'),(434,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:31:00','2026-05-11 20:31:00','2026-05-11 20:31:00'),(435,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:32:00','2026-05-11 20:32:00','2026-05-11 20:32:00'),(436,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:33:00','2026-05-11 20:33:00','2026-05-11 20:33:00'),(437,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 20:34:00','2026-05-11 20:34:00','2026-05-11 20:34:00'),(438,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:35:00','2026-05-11 20:35:00','2026-05-11 20:35:00'),(439,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:36:00','2026-05-11 20:36:00','2026-05-11 20:36:00'),(440,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:37:00','2026-05-11 20:37:00','2026-05-11 20:37:00'),(441,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:38:00','2026-05-11 20:38:00','2026-05-11 20:38:00'),(442,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 20:39:00','2026-05-11 20:39:00','2026-05-11 20:39:00'),(443,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:40:00','2026-05-11 20:40:00','2026-05-11 20:40:00'),(444,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 20:41:00','2026-05-11 20:41:00','2026-05-11 20:41:00'),(445,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:42:00','2026-05-11 20:42:00','2026-05-11 20:42:00'),(446,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:43:00','2026-05-11 20:43:00','2026-05-11 20:43:00'),(447,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:44:00','2026-05-11 20:44:00','2026-05-11 20:44:00'),(448,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:45:00','2026-05-11 20:45:00','2026-05-11 20:45:00'),(449,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:46:00','2026-05-11 20:46:00','2026-05-11 20:46:00'),(450,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:47:00','2026-05-11 20:47:00','2026-05-11 20:47:00'),(451,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 20:48:00','2026-05-11 20:48:00','2026-05-11 20:48:00'),(452,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:49:00','2026-05-11 20:49:00','2026-05-11 20:49:00'),(453,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:50:00','2026-05-11 20:50:00','2026-05-11 20:50:00'),(454,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:51:00','2026-05-11 20:51:00','2026-05-11 20:51:00'),(455,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:52:00','2026-05-11 20:52:00','2026-05-11 20:52:00'),(456,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 20:53:00','2026-05-11 20:53:00','2026-05-11 20:53:00'),(457,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:54:00','2026-05-11 20:54:00','2026-05-11 20:54:00'),(458,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 20:55:00','2026-05-11 20:55:00','2026-05-11 20:55:00'),(459,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 20:56:00','2026-05-11 20:56:00','2026-05-11 20:56:00'),(460,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 20:57:00','2026-05-11 20:57:00','2026-05-11 20:57:00'),(461,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 20:58:00','2026-05-11 20:58:00','2026-05-11 20:58:00'),(462,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 20:59:00','2026-05-11 20:59:00','2026-05-11 20:59:00'),(463,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 21:00:00','2026-05-11 21:00:00','2026-05-11 21:00:00'),(464,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 21:01:00','2026-05-11 21:01:00','2026-05-11 21:01:00'),(465,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 21:02:00','2026-05-11 21:02:00','2026-05-11 21:02:00'),(466,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 21:03:00','2026-05-11 21:03:00','2026-05-11 21:03:00'),(467,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 21:04:00','2026-05-11 21:04:00','2026-05-11 21:04:00'),(468,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 21:05:00','2026-05-11 21:05:00','2026-05-11 21:05:00'),(469,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 21:06:00','2026-05-11 21:06:00','2026-05-11 21:06:00'),(470,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 21:07:00','2026-05-11 21:07:00','2026-05-11 21:07:00'),(471,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 21:08:00','2026-05-11 21:08:00','2026-05-11 21:08:00'),(472,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 21:09:00','2026-05-11 21:09:00','2026-05-11 21:09:00'),(473,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 21:10:00','2026-05-11 21:10:00','2026-05-11 21:10:00'),(474,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 21:11:00','2026-05-11 21:11:00','2026-05-11 21:11:00'),(475,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 21:12:00','2026-05-11 21:12:00','2026-05-11 21:12:00'),(476,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 21:13:00','2026-05-11 21:13:00','2026-05-11 21:13:00'),(477,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 21:14:00','2026-05-11 21:14:00','2026-05-11 21:14:00'),(478,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 21:15:00','2026-05-11 21:15:00','2026-05-11 21:15:00'),(479,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 21:16:00','2026-05-11 21:16:00','2026-05-11 21:16:00'),(480,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 21:17:00','2026-05-11 21:17:00','2026-05-11 21:17:00'),(481,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 21:18:00','2026-05-11 21:18:00','2026-05-11 21:18:00'),(482,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 21:19:00','2026-05-11 21:19:00','2026-05-11 21:19:00'),(483,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 21:20:00','2026-05-11 21:20:00','2026-05-11 21:20:00'),(484,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 21:21:00','2026-05-11 21:21:00','2026-05-11 21:21:00'),(485,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 21:22:00','2026-05-11 21:22:00','2026-05-11 21:22:00'),(486,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 21:23:00','2026-05-11 21:23:00','2026-05-11 21:23:00'),(487,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 21:24:00','2026-05-11 21:24:00','2026-05-11 21:24:00'),(488,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 21:25:00','2026-05-11 21:25:00','2026-05-11 21:25:00'),(489,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 21:26:00','2026-05-11 21:26:00','2026-05-11 21:26:00'),(490,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 21:27:00','2026-05-11 21:27:00','2026-05-11 21:27:00'),(491,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 21:28:00','2026-05-11 21:28:00','2026-05-11 21:28:00'),(492,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 21:29:00','2026-05-11 21:29:00','2026-05-11 21:29:00'),(493,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 21:30:00','2026-05-11 21:30:00','2026-05-11 21:30:00'),(494,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 21:31:00','2026-05-11 21:31:00','2026-05-11 21:31:00'),(495,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 21:32:00','2026-05-11 21:32:00','2026-05-11 21:32:00'),(496,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 21:33:00','2026-05-11 21:33:00','2026-05-11 21:33:00'),(497,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 21:34:00','2026-05-11 21:34:00','2026-05-11 21:34:00'),(498,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 21:35:00','2026-05-11 21:35:00','2026-05-11 21:35:00'),(499,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 21:36:00','2026-05-11 21:36:00','2026-05-11 21:36:00'),(500,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 21:37:00','2026-05-11 21:37:00','2026-05-11 21:37:00'),(501,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 21:38:00','2026-05-11 21:38:00','2026-05-11 21:38:00'),(502,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 21:39:00','2026-05-11 21:39:00','2026-05-11 21:39:00'),(503,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 21:40:00','2026-05-11 21:40:00','2026-05-11 21:40:00'),(504,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 21:41:00','2026-05-11 21:41:00','2026-05-11 21:41:00'),(505,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：2毫秒','0','','2026-05-11 21:42:00','2026-05-11 21:42:00','2026-05-11 21:42:00'),(506,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 21:43:00','2026-05-11 21:43:00','2026-05-11 21:43:00'),(507,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 21:44:00','2026-05-11 21:44:00','2026-05-11 21:44:00'),(508,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 21:45:00','2026-05-11 21:45:00','2026-05-11 21:45:00'),(509,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 21:46:00','2026-05-11 21:46:00','2026-05-11 21:46:00'),(510,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 21:47:00','2026-05-11 21:47:00','2026-05-11 21:47:00'),(511,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 21:48:00','2026-05-11 21:48:00','2026-05-11 21:48:00'),(512,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 21:49:00','2026-05-11 21:49:00','2026-05-11 21:49:00'),(513,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 21:50:00','2026-05-11 21:50:00','2026-05-11 21:50:00'),(514,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 21:51:00','2026-05-11 21:51:00','2026-05-11 21:51:00'),(515,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 21:52:00','2026-05-11 21:52:00','2026-05-11 21:52:00'),(516,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 21:53:00','2026-05-11 21:53:00','2026-05-11 21:53:00'),(517,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 21:54:00','2026-05-11 21:54:00','2026-05-11 21:54:00'),(518,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 21:55:00','2026-05-11 21:55:00','2026-05-11 21:55:00'),(519,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 21:56:00','2026-05-11 21:56:00','2026-05-11 21:56:00'),(520,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 21:57:00','2026-05-11 21:57:00','2026-05-11 21:57:00'),(521,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 21:58:00','2026-05-11 21:58:00','2026-05-11 21:58:00'),(522,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 21:59:00','2026-05-11 21:59:00','2026-05-11 21:59:00'),(523,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:00:00','2026-05-11 22:00:00','2026-05-11 22:00:00'),(524,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 22:01:00','2026-05-11 22:01:00','2026-05-11 22:01:00'),(525,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 22:02:00','2026-05-11 22:02:00','2026-05-11 22:02:00'),(526,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 22:03:00','2026-05-11 22:03:00','2026-05-11 22:03:00'),(527,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:04:00','2026-05-11 22:04:00','2026-05-11 22:04:00'),(528,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 22:05:00','2026-05-11 22:05:00','2026-05-11 22:05:00'),(529,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 22:06:00','2026-05-11 22:06:00','2026-05-11 22:06:00'),(530,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 22:07:00','2026-05-11 22:07:00','2026-05-11 22:07:00'),(531,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:08:00','2026-05-11 22:08:00','2026-05-11 22:08:00'),(532,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 22:09:00','2026-05-11 22:09:00','2026-05-11 22:09:00'),(533,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 22:10:00','2026-05-11 22:10:00','2026-05-11 22:10:00'),(534,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:11:00','2026-05-11 22:11:00','2026-05-11 22:11:00'),(535,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 22:12:00','2026-05-11 22:12:00','2026-05-11 22:12:00'),(536,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 22:13:00','2026-05-11 22:13:00','2026-05-11 22:13:00'),(537,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 22:14:00','2026-05-11 22:14:00','2026-05-11 22:14:00'),(538,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 22:15:00','2026-05-11 22:15:00','2026-05-11 22:15:00'),(539,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 22:16:00','2026-05-11 22:16:00','2026-05-11 22:16:00'),(540,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 22:17:00','2026-05-11 22:17:00','2026-05-11 22:17:00'),(541,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:18:00','2026-05-11 22:18:00','2026-05-11 22:18:00'),(542,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 22:19:00','2026-05-11 22:19:00','2026-05-11 22:19:00'),(543,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 22:20:00','2026-05-11 22:20:00','2026-05-11 22:20:00'),(544,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:21:00','2026-05-11 22:21:00','2026-05-11 22:21:00'),(545,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 22:22:00','2026-05-11 22:22:00','2026-05-11 22:22:00'),(546,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 22:23:00','2026-05-11 22:23:00','2026-05-11 22:23:00'),(547,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:24:00','2026-05-11 22:24:00','2026-05-11 22:24:00'),(548,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 22:25:00','2026-05-11 22:25:00','2026-05-11 22:25:00'),(549,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 22:26:00','2026-05-11 22:26:00','2026-05-11 22:26:00'),(550,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-11 22:27:00','2026-05-11 22:27:00','2026-05-11 22:27:00'),(551,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 22:28:00','2026-05-11 22:28:00','2026-05-11 22:28:00'),(552,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 22:29:00','2026-05-11 22:29:00','2026-05-11 22:29:00'),(553,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 22:30:00','2026-05-11 22:30:00','2026-05-11 22:30:00'),(554,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:31:00','2026-05-11 22:31:00','2026-05-11 22:31:00'),(555,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:32:00','2026-05-11 22:32:00','2026-05-11 22:32:00'),(556,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:33:00','2026-05-11 22:33:00','2026-05-11 22:33:00'),(557,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 22:34:00','2026-05-11 22:34:00','2026-05-11 22:34:00'),(558,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 22:35:00','2026-05-11 22:35:00','2026-05-11 22:35:00'),(559,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 22:36:00','2026-05-11 22:36:00','2026-05-11 22:36:00'),(560,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 22:37:00','2026-05-11 22:37:00','2026-05-11 22:37:00'),(561,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 22:38:00','2026-05-11 22:38:00','2026-05-11 22:38:00'),(562,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 22:39:00','2026-05-11 22:39:00','2026-05-11 22:39:00'),(563,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 22:40:00','2026-05-11 22:40:00','2026-05-11 22:40:00'),(564,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:41:00','2026-05-11 22:41:00','2026-05-11 22:41:00'),(565,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 22:42:00','2026-05-11 22:42:00','2026-05-11 22:42:00'),(566,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 22:43:00','2026-05-11 22:43:00','2026-05-11 22:43:00'),(567,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:44:00','2026-05-11 22:44:00','2026-05-11 22:44:00'),(568,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:45:00','2026-05-11 22:45:00','2026-05-11 22:45:00'),(569,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 22:46:00','2026-05-11 22:46:00','2026-05-11 22:46:00'),(570,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 22:47:00','2026-05-11 22:47:00','2026-05-11 22:47:00'),(571,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:48:00','2026-05-11 22:48:00','2026-05-11 22:48:00'),(572,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:49:00','2026-05-11 22:49:00','2026-05-11 22:49:00'),(573,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 22:50:00','2026-05-11 22:50:00','2026-05-11 22:50:00'),(574,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 22:51:00','2026-05-11 22:51:00','2026-05-11 22:51:00'),(575,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 22:52:00','2026-05-11 22:52:00','2026-05-11 22:52:00'),(576,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:53:00','2026-05-11 22:53:00','2026-05-11 22:53:00'),(577,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:54:00','2026-05-11 22:54:00','2026-05-11 22:54:00'),(578,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 22:55:00','2026-05-11 22:55:00','2026-05-11 22:55:00'),(579,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 22:56:00','2026-05-11 22:56:00','2026-05-11 22:56:00'),(580,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:57:00','2026-05-11 22:57:00','2026-05-11 22:57:00'),(581,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 22:58:00','2026-05-11 22:58:00','2026-05-11 22:58:00'),(582,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 22:59:00','2026-05-11 22:59:00','2026-05-11 22:59:00'),(583,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:00:00','2026-05-11 23:00:00','2026-05-11 23:00:00'),(584,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 23:01:00','2026-05-11 23:01:00','2026-05-11 23:01:00'),(585,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 23:02:00','2026-05-11 23:02:00','2026-05-11 23:02:00'),(586,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 23:03:00','2026-05-11 23:03:00','2026-05-11 23:03:00'),(587,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:04:00','2026-05-11 23:04:00','2026-05-11 23:04:00'),(588,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 23:05:00','2026-05-11 23:05:00','2026-05-11 23:05:00'),(589,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 23:06:00','2026-05-11 23:06:00','2026-05-11 23:06:00'),(590,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:07:00','2026-05-11 23:07:00','2026-05-11 23:07:00'),(591,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 23:08:00','2026-05-11 23:08:00','2026-05-11 23:08:00'),(592,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:09:00','2026-05-11 23:09:00','2026-05-11 23:09:00'),(593,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:10:00','2026-05-11 23:10:00','2026-05-11 23:10:00'),(594,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:11:00','2026-05-11 23:11:00','2026-05-11 23:11:00'),(595,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 23:12:00','2026-05-11 23:12:00','2026-05-11 23:12:00'),(596,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 23:13:00','2026-05-11 23:13:00','2026-05-11 23:13:00'),(597,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:14:00','2026-05-11 23:14:00','2026-05-11 23:14:00'),(598,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-11 23:15:00','2026-05-11 23:15:00','2026-05-11 23:15:00'),(599,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:16:00','2026-05-11 23:16:00','2026-05-11 23:16:00'),(600,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 23:17:00','2026-05-11 23:17:00','2026-05-11 23:17:00'),(601,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:18:00','2026-05-11 23:18:00','2026-05-11 23:18:00'),(602,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 23:19:00','2026-05-11 23:19:00','2026-05-11 23:19:00'),(603,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 23:20:00','2026-05-11 23:20:00','2026-05-11 23:20:00'),(604,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 23:21:00','2026-05-11 23:21:00','2026-05-11 23:21:00'),(605,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:22:00','2026-05-11 23:22:00','2026-05-11 23:22:00'),(606,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 23:23:00','2026-05-11 23:23:00','2026-05-11 23:23:00'),(607,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:24:00','2026-05-11 23:24:00','2026-05-11 23:24:00'),(608,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:25:00','2026-05-11 23:25:00','2026-05-11 23:25:00'),(609,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 23:26:00','2026-05-11 23:26:00','2026-05-11 23:26:00'),(610,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 23:27:00','2026-05-11 23:27:00','2026-05-11 23:27:00'),(611,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 23:28:00','2026-05-11 23:28:00','2026-05-11 23:28:00'),(612,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:29:00','2026-05-11 23:29:00','2026-05-11 23:29:00'),(613,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 23:30:00','2026-05-11 23:30:00','2026-05-11 23:30:00'),(614,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 23:31:00','2026-05-11 23:31:00','2026-05-11 23:31:00'),(615,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 23:32:00','2026-05-11 23:32:00','2026-05-11 23:32:00'),(616,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 23:33:00','2026-05-11 23:33:00','2026-05-11 23:33:00'),(617,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 23:34:00','2026-05-11 23:34:00','2026-05-11 23:34:00'),(618,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 23:35:00','2026-05-11 23:35:00','2026-05-11 23:35:00'),(619,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:36:00','2026-05-11 23:36:00','2026-05-11 23:36:00'),(620,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 23:37:00','2026-05-11 23:37:00','2026-05-11 23:37:00'),(621,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 23:38:00','2026-05-11 23:38:00','2026-05-11 23:38:00'),(622,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 23:39:00','2026-05-11 23:39:00','2026-05-11 23:39:00'),(623,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 23:40:00','2026-05-11 23:40:00','2026-05-11 23:40:00'),(624,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 23:41:00','2026-05-11 23:41:00','2026-05-11 23:41:00'),(625,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:42:00','2026-05-11 23:42:00','2026-05-11 23:42:00'),(626,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-11 23:43:00','2026-05-11 23:43:00','2026-05-11 23:43:00'),(627,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:44:00','2026-05-11 23:44:00','2026-05-11 23:44:00'),(628,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:45:00','2026-05-11 23:45:00','2026-05-11 23:45:00'),(629,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 23:46:00','2026-05-11 23:46:00','2026-05-11 23:46:00'),(630,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 23:47:00','2026-05-11 23:47:00','2026-05-11 23:47:00'),(631,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:48:00','2026-05-11 23:48:00','2026-05-11 23:48:00'),(632,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 23:49:00','2026-05-11 23:49:00','2026-05-11 23:49:00'),(633,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 23:50:00','2026-05-11 23:50:00','2026-05-11 23:50:00'),(634,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-11 23:51:00','2026-05-11 23:51:00','2026-05-11 23:51:00'),(635,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-11 23:52:00','2026-05-11 23:52:00','2026-05-11 23:52:00'),(636,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 23:53:00','2026-05-11 23:53:00','2026-05-11 23:53:00'),(637,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:54:00','2026-05-11 23:54:00','2026-05-11 23:54:00'),(638,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 23:55:00','2026-05-11 23:55:00','2026-05-11 23:55:00'),(639,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 23:56:00','2026-05-11 23:56:00','2026-05-11 23:56:00'),(640,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-11 23:57:00','2026-05-11 23:57:00','2026-05-11 23:57:00'),(641,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-11 23:58:00','2026-05-11 23:58:00','2026-05-11 23:58:00'),(642,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-11 23:59:00','2026-05-11 23:59:00','2026-05-11 23:59:00'),(643,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 00:00:00','2026-05-12 00:00:00','2026-05-12 00:00:00'),(644,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:01:00','2026-05-12 00:01:00','2026-05-12 00:01:00'),(645,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 00:02:00','2026-05-12 00:02:00','2026-05-12 00:02:00'),(646,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 00:03:00','2026-05-12 00:03:00','2026-05-12 00:03:00'),(647,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:04:00','2026-05-12 00:04:00','2026-05-12 00:04:00'),(648,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 00:05:00','2026-05-12 00:05:00','2026-05-12 00:05:00'),(649,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 00:06:00','2026-05-12 00:06:00','2026-05-12 00:06:00'),(650,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:07:00','2026-05-12 00:07:00','2026-05-12 00:07:00'),(651,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 00:08:00','2026-05-12 00:08:00','2026-05-12 00:08:00'),(652,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 00:09:00','2026-05-12 00:09:00','2026-05-12 00:09:00'),(653,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 00:10:00','2026-05-12 00:10:00','2026-05-12 00:10:00'),(654,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 00:11:00','2026-05-12 00:11:00','2026-05-12 00:11:00'),(655,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:12:00','2026-05-12 00:12:00','2026-05-12 00:12:00'),(656,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:13:00','2026-05-12 00:13:00','2026-05-12 00:13:00'),(657,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:14:00','2026-05-12 00:14:00','2026-05-12 00:14:00'),(658,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:15:00','2026-05-12 00:15:00','2026-05-12 00:15:00'),(659,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 00:16:00','2026-05-12 00:16:00','2026-05-12 00:16:00'),(660,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 00:17:00','2026-05-12 00:17:00','2026-05-12 00:17:00'),(661,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 00:18:00','2026-05-12 00:18:00','2026-05-12 00:18:00'),(662,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 00:19:00','2026-05-12 00:19:00','2026-05-12 00:19:00'),(663,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:20:00','2026-05-12 00:20:00','2026-05-12 00:20:00'),(664,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 00:21:00','2026-05-12 00:21:00','2026-05-12 00:21:00'),(665,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 00:22:00','2026-05-12 00:22:00','2026-05-12 00:22:00'),(666,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 00:23:00','2026-05-12 00:23:00','2026-05-12 00:23:00'),(667,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 00:24:00','2026-05-12 00:24:00','2026-05-12 00:24:00'),(668,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:25:00','2026-05-12 00:25:00','2026-05-12 00:25:00'),(669,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 00:26:00','2026-05-12 00:26:00','2026-05-12 00:26:00'),(670,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 00:27:00','2026-05-12 00:27:00','2026-05-12 00:27:00'),(671,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 00:28:00','2026-05-12 00:28:00','2026-05-12 00:28:00'),(672,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:29:00','2026-05-12 00:29:00','2026-05-12 00:29:00'),(673,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 00:30:00','2026-05-12 00:30:00','2026-05-12 00:30:00'),(674,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 00:31:00','2026-05-12 00:31:00','2026-05-12 00:31:00'),(675,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 00:32:00','2026-05-12 00:32:00','2026-05-12 00:32:00'),(676,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:33:00','2026-05-12 00:33:00','2026-05-12 00:33:00'),(677,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 00:34:00','2026-05-12 00:34:00','2026-05-12 00:34:00'),(678,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:35:00','2026-05-12 00:35:00','2026-05-12 00:35:00'),(679,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 00:36:00','2026-05-12 00:36:00','2026-05-12 00:36:00'),(680,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:37:00','2026-05-12 00:37:00','2026-05-12 00:37:00'),(681,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 00:38:00','2026-05-12 00:38:00','2026-05-12 00:38:00'),(682,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 00:39:00','2026-05-12 00:39:00','2026-05-12 00:39:00'),(683,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 00:40:00','2026-05-12 00:40:00','2026-05-12 00:40:00'),(684,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 00:41:00','2026-05-12 00:41:00','2026-05-12 00:41:00'),(685,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 00:42:00','2026-05-12 00:42:00','2026-05-12 00:42:00'),(686,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 00:43:00','2026-05-12 00:43:00','2026-05-12 00:43:00'),(687,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 00:44:00','2026-05-12 00:44:00','2026-05-12 00:44:00'),(688,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 00:45:00','2026-05-12 00:45:00','2026-05-12 00:45:00'),(689,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:46:00','2026-05-12 00:46:00','2026-05-12 00:46:00'),(690,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 00:47:00','2026-05-12 00:47:00','2026-05-12 00:47:00'),(691,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 00:48:00','2026-05-12 00:48:00','2026-05-12 00:48:00'),(692,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 00:49:00','2026-05-12 00:49:00','2026-05-12 00:49:00'),(693,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 00:50:00','2026-05-12 00:50:00','2026-05-12 00:50:00'),(694,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:51:00','2026-05-12 00:51:00','2026-05-12 00:51:00'),(695,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 00:52:00','2026-05-12 00:52:00','2026-05-12 00:52:00'),(696,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:53:00','2026-05-12 00:53:00','2026-05-12 00:53:00'),(697,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:54:00','2026-05-12 00:54:00','2026-05-12 00:54:00'),(698,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:55:00','2026-05-12 00:55:00','2026-05-12 00:55:00'),(699,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 00:56:00','2026-05-12 00:56:00','2026-05-12 00:56:00'),(700,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 00:57:00','2026-05-12 00:57:00','2026-05-12 00:57:00'),(701,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 00:58:00','2026-05-12 00:58:00','2026-05-12 00:58:00'),(702,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 00:59:00','2026-05-12 00:59:00','2026-05-12 00:59:00'),(703,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 01:00:00','2026-05-12 01:00:00','2026-05-12 01:00:00'),(704,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 01:01:00','2026-05-12 01:01:00','2026-05-12 01:01:00'),(705,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 01:02:00','2026-05-12 01:02:00','2026-05-12 01:02:00'),(706,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 01:03:00','2026-05-12 01:03:00','2026-05-12 01:03:00'),(707,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 01:04:00','2026-05-12 01:04:00','2026-05-12 01:04:00'),(708,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 01:05:00','2026-05-12 01:05:00','2026-05-12 01:05:00'),(709,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 01:06:00','2026-05-12 01:06:00','2026-05-12 01:06:00'),(710,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 01:07:00','2026-05-12 01:07:00','2026-05-12 01:07:00'),(711,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 01:08:00','2026-05-12 01:08:00','2026-05-12 01:08:00'),(712,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 01:09:00','2026-05-12 01:09:00','2026-05-12 01:09:00'),(713,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 01:10:00','2026-05-12 01:10:00','2026-05-12 01:10:00'),(714,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 01:11:00','2026-05-12 01:11:00','2026-05-12 01:11:00'),(715,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 01:12:00','2026-05-12 01:12:00','2026-05-12 01:12:00'),(716,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 01:13:00','2026-05-12 01:13:00','2026-05-12 01:13:00'),(717,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 01:14:00','2026-05-12 01:14:00','2026-05-12 01:14:00'),(718,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 01:15:00','2026-05-12 01:15:00','2026-05-12 01:15:00'),(719,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 01:16:00','2026-05-12 01:16:00','2026-05-12 01:16:00'),(720,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 01:17:00','2026-05-12 01:17:00','2026-05-12 01:17:00'),(721,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 01:18:00','2026-05-12 01:18:00','2026-05-12 01:18:00'),(722,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 01:19:00','2026-05-12 01:19:00','2026-05-12 01:19:00'),(723,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 01:20:00','2026-05-12 01:20:00','2026-05-12 01:20:00'),(724,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 01:21:00','2026-05-12 01:21:00','2026-05-12 01:21:00'),(725,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 01:22:00','2026-05-12 01:22:00','2026-05-12 01:22:00'),(726,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 01:23:00','2026-05-12 01:23:00','2026-05-12 01:23:00'),(727,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 01:24:00','2026-05-12 01:24:00','2026-05-12 01:24:00'),(728,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 01:25:00','2026-05-12 01:25:00','2026-05-12 01:25:00'),(729,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 01:26:00','2026-05-12 01:26:00','2026-05-12 01:26:00'),(730,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 01:27:00','2026-05-12 01:27:00','2026-05-12 01:27:00'),(731,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 01:28:00','2026-05-12 01:28:00','2026-05-12 01:28:00'),(732,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 01:29:00','2026-05-12 01:29:00','2026-05-12 01:29:00'),(733,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 01:30:00','2026-05-12 01:30:00','2026-05-12 01:30:00'),(734,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 01:31:00','2026-05-12 01:31:00','2026-05-12 01:31:00'),(735,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 01:32:00','2026-05-12 01:32:00','2026-05-12 01:32:00'),(736,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 01:33:00','2026-05-12 01:33:00','2026-05-12 01:33:00'),(737,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 01:34:00','2026-05-12 01:34:00','2026-05-12 01:34:00'),(738,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 01:35:00','2026-05-12 01:35:00','2026-05-12 01:35:00'),(739,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 01:36:00','2026-05-12 01:36:00','2026-05-12 01:36:00'),(740,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 01:37:00','2026-05-12 01:37:00','2026-05-12 01:37:00'),(741,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 01:38:00','2026-05-12 01:38:00','2026-05-12 01:38:00'),(742,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 01:39:00','2026-05-12 01:39:00','2026-05-12 01:39:00'),(743,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 01:40:00','2026-05-12 01:40:00','2026-05-12 01:40:00'),(744,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 01:41:00','2026-05-12 01:41:00','2026-05-12 01:41:00'),(745,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-12 01:42:00','2026-05-12 01:42:00','2026-05-12 01:42:00'),(746,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 01:43:00','2026-05-12 01:43:00','2026-05-12 01:43:00'),(747,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 01:44:00','2026-05-12 01:44:00','2026-05-12 01:44:00'),(748,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 01:45:00','2026-05-12 01:45:00','2026-05-12 01:45:00'),(749,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 01:46:00','2026-05-12 01:46:00','2026-05-12 01:46:00'),(750,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 01:47:00','2026-05-12 01:47:00','2026-05-12 01:47:00'),(751,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 01:48:00','2026-05-12 01:48:00','2026-05-12 01:48:00'),(752,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 01:49:00','2026-05-12 01:49:00','2026-05-12 01:49:00'),(753,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 01:50:00','2026-05-12 01:50:00','2026-05-12 01:50:00'),(754,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 01:51:00','2026-05-12 01:51:00','2026-05-12 01:51:00'),(755,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 01:52:00','2026-05-12 01:52:00','2026-05-12 01:52:00'),(756,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 01:53:00','2026-05-12 01:53:00','2026-05-12 01:53:00'),(757,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 01:54:00','2026-05-12 01:54:00','2026-05-12 01:54:00'),(758,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-12 01:55:00','2026-05-12 01:55:00','2026-05-12 01:55:00'),(759,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 01:56:00','2026-05-12 01:56:00','2026-05-12 01:56:00'),(760,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 01:57:00','2026-05-12 01:57:00','2026-05-12 01:57:00'),(761,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 01:58:00','2026-05-12 01:58:00','2026-05-12 01:58:00'),(762,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 01:59:00','2026-05-12 01:59:00','2026-05-12 01:59:00'),(763,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 02:00:00','2026-05-12 02:00:00','2026-05-12 02:00:00'),(764,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 02:01:00','2026-05-12 02:01:00','2026-05-12 02:01:00'),(765,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 02:02:00','2026-05-12 02:02:00','2026-05-12 02:02:00'),(766,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 02:03:00','2026-05-12 02:03:00','2026-05-12 02:03:00'),(767,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 02:04:00','2026-05-12 02:04:00','2026-05-12 02:04:00'),(768,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 02:05:00','2026-05-12 02:05:00','2026-05-12 02:05:00'),(769,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 02:06:00','2026-05-12 02:06:00','2026-05-12 02:06:00'),(770,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 02:07:00','2026-05-12 02:07:00','2026-05-12 02:07:00'),(771,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 02:08:00','2026-05-12 02:08:00','2026-05-12 02:08:00'),(772,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 02:09:00','2026-05-12 02:09:00','2026-05-12 02:09:00'),(773,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 02:10:00','2026-05-12 02:10:00','2026-05-12 02:10:00'),(774,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 02:11:00','2026-05-12 02:11:00','2026-05-12 02:11:00'),(775,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 02:12:00','2026-05-12 02:12:00','2026-05-12 02:12:00'),(776,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 02:13:00','2026-05-12 02:13:00','2026-05-12 02:13:00'),(777,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 02:14:00','2026-05-12 02:14:00','2026-05-12 02:14:00'),(778,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 02:15:00','2026-05-12 02:15:00','2026-05-12 02:15:00'),(779,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 02:16:00','2026-05-12 02:16:00','2026-05-12 02:16:00'),(780,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 02:17:00','2026-05-12 02:17:00','2026-05-12 02:17:00'),(781,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 02:18:00','2026-05-12 02:18:00','2026-05-12 02:18:00'),(782,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 02:19:00','2026-05-12 02:19:00','2026-05-12 02:19:00'),(783,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 02:20:00','2026-05-12 02:20:00','2026-05-12 02:20:00'),(784,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 02:21:00','2026-05-12 02:21:00','2026-05-12 02:21:00'),(785,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 02:22:00','2026-05-12 02:22:00','2026-05-12 02:22:00'),(786,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 02:23:00','2026-05-12 02:23:00','2026-05-12 02:23:00'),(787,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 02:24:00','2026-05-12 02:24:00','2026-05-12 02:24:00'),(788,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 02:25:00','2026-05-12 02:25:00','2026-05-12 02:25:00'),(789,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 02:26:00','2026-05-12 02:26:00','2026-05-12 02:26:00'),(790,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 02:27:00','2026-05-12 02:27:00','2026-05-12 02:27:00'),(791,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 02:28:00','2026-05-12 02:28:00','2026-05-12 02:28:00'),(792,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 02:29:00','2026-05-12 02:29:00','2026-05-12 02:29:00'),(793,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 02:30:00','2026-05-12 02:30:00','2026-05-12 02:30:00'),(794,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 02:31:00','2026-05-12 02:31:00','2026-05-12 02:31:00'),(795,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 02:32:00','2026-05-12 02:32:00','2026-05-12 02:32:00'),(796,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 02:33:00','2026-05-12 02:33:00','2026-05-12 02:33:00'),(797,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 02:34:00','2026-05-12 02:34:00','2026-05-12 02:34:00'),(798,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 02:35:00','2026-05-12 02:35:00','2026-05-12 02:35:00'),(799,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 02:36:00','2026-05-12 02:36:00','2026-05-12 02:36:00'),(800,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 02:37:00','2026-05-12 02:37:00','2026-05-12 02:37:00'),(801,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 02:38:00','2026-05-12 02:38:00','2026-05-12 02:38:00'),(802,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 02:39:00','2026-05-12 02:39:00','2026-05-12 02:39:00'),(803,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 02:40:00','2026-05-12 02:40:00','2026-05-12 02:40:00'),(804,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 02:41:00','2026-05-12 02:41:00','2026-05-12 02:41:00'),(805,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 02:42:00','2026-05-12 02:42:00','2026-05-12 02:42:00'),(806,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 02:43:00','2026-05-12 02:43:00','2026-05-12 02:43:00'),(807,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 02:44:00','2026-05-12 02:44:00','2026-05-12 02:44:00'),(808,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 02:45:00','2026-05-12 02:45:00','2026-05-12 02:45:00'),(809,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 02:46:00','2026-05-12 02:46:00','2026-05-12 02:46:00'),(810,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 02:47:00','2026-05-12 02:47:00','2026-05-12 02:47:00'),(811,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 02:48:00','2026-05-12 02:48:00','2026-05-12 02:48:00'),(812,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 02:49:00','2026-05-12 02:49:00','2026-05-12 02:49:00'),(813,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 02:50:00','2026-05-12 02:50:00','2026-05-12 02:50:00'),(814,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 02:51:00','2026-05-12 02:51:00','2026-05-12 02:51:00'),(815,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 02:52:00','2026-05-12 02:52:00','2026-05-12 02:52:00'),(816,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 02:53:00','2026-05-12 02:53:00','2026-05-12 02:53:00'),(817,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 02:54:00','2026-05-12 02:54:00','2026-05-12 02:54:00'),(818,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 02:55:00','2026-05-12 02:55:00','2026-05-12 02:55:00'),(819,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 02:56:00','2026-05-12 02:56:00','2026-05-12 02:56:00'),(820,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 02:57:00','2026-05-12 02:57:00','2026-05-12 02:57:00'),(821,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 02:58:00','2026-05-12 02:58:00','2026-05-12 02:58:00'),(822,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 02:59:00','2026-05-12 02:59:00','2026-05-12 02:59:00'),(823,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 03:00:00','2026-05-12 03:00:00','2026-05-12 03:00:00'),(824,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 03:01:00','2026-05-12 03:01:00','2026-05-12 03:01:00'),(825,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 03:02:00','2026-05-12 03:02:00','2026-05-12 03:02:00'),(826,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 03:03:00','2026-05-12 03:03:00','2026-05-12 03:03:00'),(827,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 03:04:00','2026-05-12 03:04:00','2026-05-12 03:04:00'),(828,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 03:05:00','2026-05-12 03:05:00','2026-05-12 03:05:00'),(829,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 03:06:00','2026-05-12 03:06:00','2026-05-12 03:06:00'),(830,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 03:07:00','2026-05-12 03:07:00','2026-05-12 03:07:00'),(831,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 03:08:00','2026-05-12 03:08:00','2026-05-12 03:08:00'),(832,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 03:09:00','2026-05-12 03:09:00','2026-05-12 03:09:00'),(833,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 03:10:00','2026-05-12 03:10:00','2026-05-12 03:10:00'),(834,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 03:11:00','2026-05-12 03:11:00','2026-05-12 03:11:00'),(835,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 03:12:00','2026-05-12 03:12:00','2026-05-12 03:12:00'),(836,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 03:13:00','2026-05-12 03:13:00','2026-05-12 03:13:00'),(837,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 03:14:00','2026-05-12 03:14:00','2026-05-12 03:14:00'),(838,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 03:15:00','2026-05-12 03:15:00','2026-05-12 03:15:00'),(839,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 03:16:00','2026-05-12 03:16:00','2026-05-12 03:16:00'),(840,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-12 03:17:00','2026-05-12 03:17:00','2026-05-12 03:17:00'),(841,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 03:18:00','2026-05-12 03:18:00','2026-05-12 03:18:00'),(842,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 03:19:00','2026-05-12 03:19:00','2026-05-12 03:19:00'),(843,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 03:20:00','2026-05-12 03:20:00','2026-05-12 03:20:00'),(844,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 03:21:00','2026-05-12 03:21:00','2026-05-12 03:21:00'),(845,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 03:22:00','2026-05-12 03:22:00','2026-05-12 03:22:00'),(846,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 03:23:00','2026-05-12 03:23:00','2026-05-12 03:23:00'),(847,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 03:24:00','2026-05-12 03:24:00','2026-05-12 03:24:00'),(848,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 03:25:00','2026-05-12 03:25:00','2026-05-12 03:25:00'),(849,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 03:26:00','2026-05-12 03:26:00','2026-05-12 03:26:00'),(850,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 03:27:00','2026-05-12 03:27:00','2026-05-12 03:27:00'),(851,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 03:28:00','2026-05-12 03:28:00','2026-05-12 03:28:00'),(852,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 03:29:00','2026-05-12 03:29:00','2026-05-12 03:29:00'),(853,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 03:30:00','2026-05-12 03:30:00','2026-05-12 03:30:00'),(854,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 03:31:00','2026-05-12 03:31:00','2026-05-12 03:31:00'),(855,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 03:32:00','2026-05-12 03:32:00','2026-05-12 03:32:00'),(856,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 03:33:00','2026-05-12 03:33:00','2026-05-12 03:33:00'),(857,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 03:34:00','2026-05-12 03:34:00','2026-05-12 03:34:00'),(858,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 03:35:00','2026-05-12 03:35:00','2026-05-12 03:35:00'),(859,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 03:36:00','2026-05-12 03:36:00','2026-05-12 03:36:00'),(860,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 03:37:00','2026-05-12 03:37:00','2026-05-12 03:37:00'),(861,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 03:38:00','2026-05-12 03:38:00','2026-05-12 03:38:00'),(862,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 03:39:00','2026-05-12 03:39:00','2026-05-12 03:39:00'),(863,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 03:40:00','2026-05-12 03:40:00','2026-05-12 03:40:00'),(864,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 03:41:00','2026-05-12 03:41:00','2026-05-12 03:41:00'),(865,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 03:42:00','2026-05-12 03:42:00','2026-05-12 03:42:00'),(866,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 03:43:00','2026-05-12 03:43:00','2026-05-12 03:43:00'),(867,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 03:44:00','2026-05-12 03:44:00','2026-05-12 03:44:00'),(868,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 03:45:00','2026-05-12 03:45:00','2026-05-12 03:45:00'),(869,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 03:46:00','2026-05-12 03:46:00','2026-05-12 03:46:00'),(870,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 03:47:00','2026-05-12 03:47:00','2026-05-12 03:47:00'),(871,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 03:48:00','2026-05-12 03:48:00','2026-05-12 03:48:00'),(872,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 03:49:00','2026-05-12 03:49:00','2026-05-12 03:49:00'),(873,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 03:50:00','2026-05-12 03:50:00','2026-05-12 03:50:00'),(874,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 03:51:00','2026-05-12 03:51:00','2026-05-12 03:51:00'),(875,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 03:52:00','2026-05-12 03:52:00','2026-05-12 03:52:00'),(876,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 03:53:00','2026-05-12 03:53:00','2026-05-12 03:53:00'),(877,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 03:54:00','2026-05-12 03:54:00','2026-05-12 03:54:00'),(878,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 03:55:00','2026-05-12 03:55:00','2026-05-12 03:55:00'),(879,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 03:56:00','2026-05-12 03:56:00','2026-05-12 03:56:00'),(880,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 03:57:00','2026-05-12 03:57:00','2026-05-12 03:57:00'),(881,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 03:58:00','2026-05-12 03:58:00','2026-05-12 03:58:00'),(882,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 03:59:00','2026-05-12 03:59:00','2026-05-12 03:59:00'),(883,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-12 04:00:00','2026-05-12 04:00:00','2026-05-12 04:00:00'),(884,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 04:01:00','2026-05-12 04:01:00','2026-05-12 04:01:00'),(885,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 04:02:00','2026-05-12 04:02:00','2026-05-12 04:02:00'),(886,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 04:03:00','2026-05-12 04:03:00','2026-05-12 04:03:00'),(887,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 04:04:00','2026-05-12 04:04:00','2026-05-12 04:04:00'),(888,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 04:05:00','2026-05-12 04:05:00','2026-05-12 04:05:00'),(889,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 04:06:00','2026-05-12 04:06:00','2026-05-12 04:06:00'),(890,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：2毫秒','0','','2026-05-12 04:07:00','2026-05-12 04:07:00','2026-05-12 04:07:00'),(891,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 04:08:00','2026-05-12 04:08:00','2026-05-12 04:08:00'),(892,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 04:09:00','2026-05-12 04:09:00','2026-05-12 04:09:00'),(893,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 04:10:00','2026-05-12 04:10:00','2026-05-12 04:10:00'),(894,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 04:11:00','2026-05-12 04:11:00','2026-05-12 04:11:00'),(895,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 04:12:00','2026-05-12 04:12:00','2026-05-12 04:12:00'),(896,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：2毫秒','0','','2026-05-12 04:13:00','2026-05-12 04:13:00','2026-05-12 04:13:00'),(897,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 04:14:00','2026-05-12 04:14:00','2026-05-12 04:14:00'),(898,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 04:15:00','2026-05-12 04:15:00','2026-05-12 04:15:00'),(899,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 04:16:00','2026-05-12 04:16:00','2026-05-12 04:16:00'),(900,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 04:17:00','2026-05-12 04:17:00','2026-05-12 04:17:00'),(901,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 04:18:00','2026-05-12 04:18:00','2026-05-12 04:18:00'),(902,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 04:19:00','2026-05-12 04:19:00','2026-05-12 04:19:00'),(903,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 04:20:00','2026-05-12 04:20:00','2026-05-12 04:20:00'),(904,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 04:21:00','2026-05-12 04:21:00','2026-05-12 04:21:00'),(905,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 04:22:00','2026-05-12 04:22:00','2026-05-12 04:22:00'),(906,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 04:23:00','2026-05-12 04:23:00','2026-05-12 04:23:00'),(907,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 04:24:00','2026-05-12 04:24:00','2026-05-12 04:24:00'),(908,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 04:25:00','2026-05-12 04:25:00','2026-05-12 04:25:00'),(909,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 04:26:00','2026-05-12 04:26:00','2026-05-12 04:26:00'),(910,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 04:27:00','2026-05-12 04:27:00','2026-05-12 04:27:00'),(911,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 04:28:00','2026-05-12 04:28:00','2026-05-12 04:28:00'),(912,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 04:29:00','2026-05-12 04:29:00','2026-05-12 04:29:00'),(913,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 04:30:00','2026-05-12 04:30:00','2026-05-12 04:30:00'),(914,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 04:31:00','2026-05-12 04:31:00','2026-05-12 04:31:00'),(915,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 04:32:00','2026-05-12 04:32:00','2026-05-12 04:32:00'),(916,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 04:33:00','2026-05-12 04:33:00','2026-05-12 04:33:00'),(917,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 04:34:00','2026-05-12 04:34:00','2026-05-12 04:34:00'),(918,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 04:35:00','2026-05-12 04:35:00','2026-05-12 04:35:00'),(919,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 04:36:00','2026-05-12 04:36:00','2026-05-12 04:36:00'),(920,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 04:37:00','2026-05-12 04:37:00','2026-05-12 04:37:00'),(921,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 04:38:00','2026-05-12 04:38:00','2026-05-12 04:38:00'),(922,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 04:39:00','2026-05-12 04:39:00','2026-05-12 04:39:00'),(923,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 04:40:00','2026-05-12 04:40:00','2026-05-12 04:40:00'),(924,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 04:41:00','2026-05-12 04:41:00','2026-05-12 04:41:00'),(925,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 04:42:00','2026-05-12 04:42:00','2026-05-12 04:42:00'),(926,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 04:43:00','2026-05-12 04:43:00','2026-05-12 04:43:00'),(927,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 04:44:00','2026-05-12 04:44:00','2026-05-12 04:44:00'),(928,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 04:45:00','2026-05-12 04:45:00','2026-05-12 04:45:00'),(929,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 04:46:00','2026-05-12 04:46:00','2026-05-12 04:46:00'),(930,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 04:47:00','2026-05-12 04:47:00','2026-05-12 04:47:00'),(931,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 04:48:00','2026-05-12 04:48:00','2026-05-12 04:48:00'),(932,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 04:49:00','2026-05-12 04:49:00','2026-05-12 04:49:00'),(933,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 04:50:00','2026-05-12 04:50:00','2026-05-12 04:50:00'),(934,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 04:51:00','2026-05-12 04:51:00','2026-05-12 04:51:00'),(935,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 04:52:00','2026-05-12 04:52:00','2026-05-12 04:52:00'),(936,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 04:53:00','2026-05-12 04:53:00','2026-05-12 04:53:00'),(937,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 04:54:00','2026-05-12 04:54:00','2026-05-12 04:54:00'),(938,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 04:55:00','2026-05-12 04:55:00','2026-05-12 04:55:00'),(939,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 04:56:00','2026-05-12 04:56:00','2026-05-12 04:56:00'),(940,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 04:57:00','2026-05-12 04:57:00','2026-05-12 04:57:00'),(941,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 04:58:00','2026-05-12 04:58:00','2026-05-12 04:58:00'),(942,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 04:59:00','2026-05-12 04:59:00','2026-05-12 04:59:00'),(943,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 05:00:00','2026-05-12 05:00:00','2026-05-12 05:00:00'),(944,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 05:01:00','2026-05-12 05:01:00','2026-05-12 05:01:00'),(945,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 05:02:00','2026-05-12 05:02:00','2026-05-12 05:02:00'),(946,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 05:03:00','2026-05-12 05:03:00','2026-05-12 05:03:00'),(947,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 05:04:00','2026-05-12 05:04:00','2026-05-12 05:04:00'),(948,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 05:05:00','2026-05-12 05:05:00','2026-05-12 05:05:00'),(949,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 05:06:00','2026-05-12 05:06:00','2026-05-12 05:06:00'),(950,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 05:07:00','2026-05-12 05:07:00','2026-05-12 05:07:00'),(951,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 05:08:00','2026-05-12 05:08:00','2026-05-12 05:08:00'),(952,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 05:09:00','2026-05-12 05:09:00','2026-05-12 05:09:00'),(953,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 05:10:00','2026-05-12 05:10:00','2026-05-12 05:10:00'),(954,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 05:11:00','2026-05-12 05:11:00','2026-05-12 05:11:00'),(955,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 05:12:00','2026-05-12 05:12:00','2026-05-12 05:12:00'),(956,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 05:13:00','2026-05-12 05:13:00','2026-05-12 05:13:00'),(957,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 05:14:00','2026-05-12 05:14:00','2026-05-12 05:14:00'),(958,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 05:15:00','2026-05-12 05:15:00','2026-05-12 05:15:00'),(959,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 05:16:00','2026-05-12 05:16:00','2026-05-12 05:16:00'),(960,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 05:17:00','2026-05-12 05:17:00','2026-05-12 05:17:00'),(961,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 05:18:00','2026-05-12 05:18:00','2026-05-12 05:18:00'),(962,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 05:19:00','2026-05-12 05:19:00','2026-05-12 05:19:00'),(963,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 05:20:00','2026-05-12 05:20:00','2026-05-12 05:20:00'),(964,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 05:21:00','2026-05-12 05:21:00','2026-05-12 05:21:00'),(965,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 05:22:00','2026-05-12 05:22:00','2026-05-12 05:22:00'),(966,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 05:23:00','2026-05-12 05:23:00','2026-05-12 05:23:00'),(967,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-12 05:24:00','2026-05-12 05:24:00','2026-05-12 05:24:00'),(968,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 05:25:00','2026-05-12 05:25:00','2026-05-12 05:25:00'),(969,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 05:26:00','2026-05-12 05:26:00','2026-05-12 05:26:00'),(970,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 05:27:00','2026-05-12 05:27:00','2026-05-12 05:27:00'),(971,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 05:28:00','2026-05-12 05:28:00','2026-05-12 05:28:00'),(972,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 05:29:00','2026-05-12 05:29:00','2026-05-12 05:29:00'),(973,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 05:30:00','2026-05-12 05:30:00','2026-05-12 05:30:00'),(974,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 05:31:00','2026-05-12 05:31:00','2026-05-12 05:31:00'),(975,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 05:32:00','2026-05-12 05:32:00','2026-05-12 05:32:00'),(976,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 05:33:00','2026-05-12 05:33:00','2026-05-12 05:33:00'),(977,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 05:34:00','2026-05-12 05:34:00','2026-05-12 05:34:00'),(978,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 05:35:00','2026-05-12 05:35:00','2026-05-12 05:35:00'),(979,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 05:36:00','2026-05-12 05:36:00','2026-05-12 05:36:00'),(980,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 05:37:00','2026-05-12 05:37:00','2026-05-12 05:37:00'),(981,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 05:38:00','2026-05-12 05:38:00','2026-05-12 05:38:00'),(982,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 05:39:00','2026-05-12 05:39:00','2026-05-12 05:39:00'),(983,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 05:40:00','2026-05-12 05:40:00','2026-05-12 05:40:00'),(984,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-12 05:41:00','2026-05-12 05:41:00','2026-05-12 05:41:00'),(985,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 05:42:00','2026-05-12 05:42:00','2026-05-12 05:42:00'),(986,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 05:43:00','2026-05-12 05:43:00','2026-05-12 05:43:00'),(987,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 05:44:00','2026-05-12 05:44:00','2026-05-12 05:44:00'),(988,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 05:45:00','2026-05-12 05:45:00','2026-05-12 05:45:00'),(989,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 05:46:00','2026-05-12 05:46:00','2026-05-12 05:46:00'),(990,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 05:47:00','2026-05-12 05:47:00','2026-05-12 05:47:00'),(991,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 05:48:00','2026-05-12 05:48:00','2026-05-12 05:48:00'),(992,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 05:49:00','2026-05-12 05:49:00','2026-05-12 05:49:00'),(993,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 05:50:00','2026-05-12 05:50:00','2026-05-12 05:50:00'),(994,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 05:51:00','2026-05-12 05:51:00','2026-05-12 05:51:00'),(995,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 05:52:00','2026-05-12 05:52:00','2026-05-12 05:52:00'),(996,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 05:53:00','2026-05-12 05:53:00','2026-05-12 05:53:00'),(997,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 05:54:00','2026-05-12 05:54:00','2026-05-12 05:54:00'),(998,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 05:55:00','2026-05-12 05:55:00','2026-05-12 05:55:00'),(999,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 05:56:00','2026-05-12 05:56:00','2026-05-12 05:56:00'),(1000,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 05:57:00','2026-05-12 05:57:00','2026-05-12 05:57:00'),(1001,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 05:58:00','2026-05-12 05:58:00','2026-05-12 05:58:00'),(1002,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 05:59:00','2026-05-12 05:59:00','2026-05-12 05:59:00'),(1003,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 06:00:00','2026-05-12 06:00:00','2026-05-12 06:00:00'),(1004,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 06:01:00','2026-05-12 06:01:00','2026-05-12 06:01:00'),(1005,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 06:02:00','2026-05-12 06:02:00','2026-05-12 06:02:00'),(1006,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 06:03:00','2026-05-12 06:03:00','2026-05-12 06:03:00'),(1007,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-12 06:04:00','2026-05-12 06:04:00','2026-05-12 06:04:00'),(1008,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 06:05:00','2026-05-12 06:05:00','2026-05-12 06:05:00'),(1009,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 06:06:00','2026-05-12 06:06:00','2026-05-12 06:06:00'),(1010,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 06:07:00','2026-05-12 06:07:00','2026-05-12 06:07:00'),(1011,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 06:08:00','2026-05-12 06:08:00','2026-05-12 06:08:00'),(1012,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 06:09:00','2026-05-12 06:09:00','2026-05-12 06:09:00'),(1013,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 06:10:00','2026-05-12 06:10:00','2026-05-12 06:10:00'),(1014,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 06:11:00','2026-05-12 06:11:00','2026-05-12 06:11:00'),(1015,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 06:12:00','2026-05-12 06:12:00','2026-05-12 06:12:00'),(1016,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 06:13:00','2026-05-12 06:13:00','2026-05-12 06:13:00'),(1017,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 06:14:00','2026-05-12 06:14:00','2026-05-12 06:14:00'),(1018,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 06:15:00','2026-05-12 06:15:00','2026-05-12 06:15:00'),(1019,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 06:16:00','2026-05-12 06:16:00','2026-05-12 06:16:00'),(1020,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 06:17:00','2026-05-12 06:17:00','2026-05-12 06:17:00'),(1021,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 06:18:00','2026-05-12 06:18:00','2026-05-12 06:18:00'),(1022,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 06:19:00','2026-05-12 06:19:00','2026-05-12 06:19:00'),(1023,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 06:20:00','2026-05-12 06:20:00','2026-05-12 06:20:00'),(1024,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 06:21:00','2026-05-12 06:21:00','2026-05-12 06:21:00'),(1025,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 06:22:00','2026-05-12 06:22:00','2026-05-12 06:22:00'),(1026,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 06:23:00','2026-05-12 06:23:00','2026-05-12 06:23:00'),(1027,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 06:24:00','2026-05-12 06:24:00','2026-05-12 06:24:00'),(1028,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 06:25:00','2026-05-12 06:25:00','2026-05-12 06:25:00'),(1029,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 06:26:00','2026-05-12 06:26:00','2026-05-12 06:26:00'),(1030,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 06:27:00','2026-05-12 06:27:00','2026-05-12 06:27:00'),(1031,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 06:28:00','2026-05-12 06:28:00','2026-05-12 06:28:00'),(1032,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 06:29:00','2026-05-12 06:29:00','2026-05-12 06:29:00'),(1033,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 06:30:00','2026-05-12 06:30:00','2026-05-12 06:30:00'),(1034,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 06:31:00','2026-05-12 06:31:00','2026-05-12 06:31:00'),(1035,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 06:32:00','2026-05-12 06:32:00','2026-05-12 06:32:00'),(1036,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 06:33:00','2026-05-12 06:33:00','2026-05-12 06:33:00'),(1037,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 06:34:00','2026-05-12 06:34:00','2026-05-12 06:34:00'),(1038,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 06:35:00','2026-05-12 06:35:00','2026-05-12 06:35:00'),(1039,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 06:36:00','2026-05-12 06:36:00','2026-05-12 06:36:00'),(1040,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-12 06:37:00','2026-05-12 06:37:00','2026-05-12 06:37:00'),(1041,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 06:38:00','2026-05-12 06:38:00','2026-05-12 06:38:00'),(1042,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 06:39:00','2026-05-12 06:39:00','2026-05-12 06:39:00'),(1043,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 06:40:00','2026-05-12 06:40:00','2026-05-12 06:40:00'),(1044,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 06:41:00','2026-05-12 06:41:00','2026-05-12 06:41:00'),(1045,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 06:42:00','2026-05-12 06:42:00','2026-05-12 06:42:00'),(1046,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 06:43:00','2026-05-12 06:43:00','2026-05-12 06:43:00'),(1047,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 06:44:00','2026-05-12 06:44:00','2026-05-12 06:44:00'),(1048,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 06:45:00','2026-05-12 06:45:00','2026-05-12 06:45:00'),(1049,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：2毫秒','0','','2026-05-12 06:46:00','2026-05-12 06:46:00','2026-05-12 06:46:00'),(1050,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 06:47:00','2026-05-12 06:47:00','2026-05-12 06:47:00'),(1051,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：12毫秒','0','','2026-05-12 06:48:00','2026-05-12 06:48:00','2026-05-12 06:48:00'),(1052,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：2毫秒','0','','2026-05-12 06:49:00','2026-05-12 06:49:00','2026-05-12 06:49:00'),(1053,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 06:50:00','2026-05-12 06:50:00','2026-05-12 06:50:00'),(1054,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 06:51:00','2026-05-12 06:51:00','2026-05-12 06:51:00'),(1055,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 06:52:00','2026-05-12 06:52:00','2026-05-12 06:52:00'),(1056,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 06:53:00','2026-05-12 06:53:00','2026-05-12 06:53:00'),(1057,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 06:54:00','2026-05-12 06:54:00','2026-05-12 06:54:00'),(1058,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 06:55:00','2026-05-12 06:55:00','2026-05-12 06:55:00'),(1059,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 06:56:00','2026-05-12 06:56:00','2026-05-12 06:56:00'),(1060,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 06:57:00','2026-05-12 06:57:00','2026-05-12 06:57:00'),(1061,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 06:58:00','2026-05-12 06:58:00','2026-05-12 06:58:00'),(1062,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 06:59:00','2026-05-12 06:59:00','2026-05-12 06:59:00'),(1063,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 07:00:00','2026-05-12 07:00:00','2026-05-12 07:00:00'),(1064,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 07:01:00','2026-05-12 07:01:00','2026-05-12 07:01:00'),(1065,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 07:02:00','2026-05-12 07:02:00','2026-05-12 07:02:00'),(1066,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 07:03:00','2026-05-12 07:03:00','2026-05-12 07:03:00'),(1067,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 07:04:00','2026-05-12 07:04:00','2026-05-12 07:04:00'),(1068,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 07:05:00','2026-05-12 07:05:00','2026-05-12 07:05:00'),(1069,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-12 07:06:00','2026-05-12 07:06:00','2026-05-12 07:06:00'),(1070,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 07:07:00','2026-05-12 07:07:00','2026-05-12 07:07:00'),(1071,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 07:08:00','2026-05-12 07:08:00','2026-05-12 07:08:00'),(1072,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 07:09:00','2026-05-12 07:09:00','2026-05-12 07:09:00'),(1073,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 07:10:00','2026-05-12 07:10:00','2026-05-12 07:10:00'),(1074,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 07:11:00','2026-05-12 07:11:00','2026-05-12 07:11:00'),(1075,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 07:12:00','2026-05-12 07:12:00','2026-05-12 07:12:00'),(1076,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 07:13:00','2026-05-12 07:13:00','2026-05-12 07:13:00'),(1077,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 07:14:00','2026-05-12 07:14:00','2026-05-12 07:14:00'),(1078,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 07:15:00','2026-05-12 07:15:00','2026-05-12 07:15:00'),(1079,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 07:16:00','2026-05-12 07:16:00','2026-05-12 07:16:00'),(1080,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 07:17:00','2026-05-12 07:17:00','2026-05-12 07:17:00'),(1081,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 07:18:00','2026-05-12 07:18:00','2026-05-12 07:18:00'),(1082,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 07:19:00','2026-05-12 07:19:00','2026-05-12 07:19:00'),(1083,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 07:20:00','2026-05-12 07:20:00','2026-05-12 07:20:00'),(1084,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：11毫秒','0','','2026-05-12 07:21:00','2026-05-12 07:21:00','2026-05-12 07:21:00'),(1085,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：2毫秒','0','','2026-05-12 07:22:00','2026-05-12 07:22:00','2026-05-12 07:22:00'),(1086,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 07:23:00','2026-05-12 07:23:00','2026-05-12 07:23:00'),(1087,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：2毫秒','0','','2026-05-12 07:24:00','2026-05-12 07:24:00','2026-05-12 07:24:00'),(1088,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 07:25:00','2026-05-12 07:25:00','2026-05-12 07:25:00'),(1089,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 07:26:00','2026-05-12 07:26:00','2026-05-12 07:26:00'),(1090,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 07:27:00','2026-05-12 07:27:00','2026-05-12 07:27:00'),(1091,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 07:28:00','2026-05-12 07:28:00','2026-05-12 07:28:00'),(1092,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 07:29:00','2026-05-12 07:29:00','2026-05-12 07:29:00'),(1093,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 07:30:00','2026-05-12 07:30:00','2026-05-12 07:30:00'),(1094,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 07:31:00','2026-05-12 07:31:00','2026-05-12 07:31:00'),(1095,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 07:32:00','2026-05-12 07:32:00','2026-05-12 07:32:00'),(1096,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 07:33:00','2026-05-12 07:33:00','2026-05-12 07:33:00'),(1097,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 07:34:00','2026-05-12 07:34:00','2026-05-12 07:34:00'),(1098,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 07:35:00','2026-05-12 07:35:00','2026-05-12 07:35:00'),(1099,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 07:36:00','2026-05-12 07:36:00','2026-05-12 07:36:00'),(1100,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 07:37:00','2026-05-12 07:37:00','2026-05-12 07:37:00'),(1101,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 07:38:00','2026-05-12 07:38:00','2026-05-12 07:38:00'),(1102,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 07:39:00','2026-05-12 07:39:00','2026-05-12 07:39:00'),(1103,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 07:40:00','2026-05-12 07:40:00','2026-05-12 07:40:00'),(1104,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 07:41:00','2026-05-12 07:41:00','2026-05-12 07:41:00'),(1105,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 07:42:00','2026-05-12 07:42:00','2026-05-12 07:42:00'),(1106,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 07:43:00','2026-05-12 07:43:00','2026-05-12 07:43:00'),(1107,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 07:44:00','2026-05-12 07:44:00','2026-05-12 07:44:00'),(1108,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 07:45:00','2026-05-12 07:45:00','2026-05-12 07:45:00'),(1109,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 07:46:00','2026-05-12 07:46:00','2026-05-12 07:46:00'),(1110,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 07:47:00','2026-05-12 07:47:00','2026-05-12 07:47:00'),(1111,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 07:48:00','2026-05-12 07:48:00','2026-05-12 07:48:00'),(1112,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 07:49:00','2026-05-12 07:49:00','2026-05-12 07:49:00'),(1113,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 07:50:00','2026-05-12 07:50:00','2026-05-12 07:50:00'),(1114,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 07:51:00','2026-05-12 07:51:00','2026-05-12 07:51:00'),(1115,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 07:52:00','2026-05-12 07:52:00','2026-05-12 07:52:00'),(1116,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 07:53:00','2026-05-12 07:53:00','2026-05-12 07:53:00'),(1117,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 07:54:00','2026-05-12 07:54:00','2026-05-12 07:54:00'),(1118,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 07:55:00','2026-05-12 07:55:00','2026-05-12 07:55:00'),(1119,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：10毫秒','0','','2026-05-12 07:56:00','2026-05-12 07:56:00','2026-05-12 07:56:00'),(1120,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 07:57:00','2026-05-12 07:57:00','2026-05-12 07:57:00'),(1121,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 07:58:00','2026-05-12 07:58:00','2026-05-12 07:58:00'),(1122,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 07:59:00','2026-05-12 07:59:00','2026-05-12 07:59:00'),(1123,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 08:00:00','2026-05-12 08:00:00','2026-05-12 08:00:00'),(1124,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 08:01:00','2026-05-12 08:01:00','2026-05-12 08:01:00'),(1125,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 08:02:00','2026-05-12 08:02:00','2026-05-12 08:02:00'),(1126,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 08:03:00','2026-05-12 08:03:00','2026-05-12 08:03:00'),(1127,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 08:04:00','2026-05-12 08:04:00','2026-05-12 08:04:00'),(1128,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 08:05:00','2026-05-12 08:05:00','2026-05-12 08:05:00'),(1129,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 08:06:00','2026-05-12 08:06:00','2026-05-12 08:06:00'),(1130,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 08:07:00','2026-05-12 08:07:00','2026-05-12 08:07:00'),(1131,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 08:08:00','2026-05-12 08:08:00','2026-05-12 08:08:00'),(1132,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 08:09:00','2026-05-12 08:09:00','2026-05-12 08:09:00'),(1133,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 08:10:00','2026-05-12 08:10:00','2026-05-12 08:10:00'),(1134,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 08:11:00','2026-05-12 08:11:00','2026-05-12 08:11:00'),(1135,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 08:12:00','2026-05-12 08:12:00','2026-05-12 08:12:00'),(1136,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 08:13:00','2026-05-12 08:13:00','2026-05-12 08:13:00'),(1137,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 08:14:00','2026-05-12 08:14:00','2026-05-12 08:14:00'),(1138,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 08:15:00','2026-05-12 08:15:00','2026-05-12 08:15:00'),(1139,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 08:16:00','2026-05-12 08:16:00','2026-05-12 08:16:00'),(1140,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 08:17:00','2026-05-12 08:17:00','2026-05-12 08:17:00'),(1141,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 08:18:00','2026-05-12 08:18:00','2026-05-12 08:18:00'),(1142,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 08:19:00','2026-05-12 08:19:00','2026-05-12 08:19:00'),(1143,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 08:20:00','2026-05-12 08:20:00','2026-05-12 08:20:00'),(1144,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 08:21:00','2026-05-12 08:21:00','2026-05-12 08:21:00'),(1145,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 08:22:00','2026-05-12 08:22:00','2026-05-12 08:22:00'),(1146,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 08:23:00','2026-05-12 08:23:00','2026-05-12 08:23:00'),(1147,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：5毫秒','0','','2026-05-12 08:24:00','2026-05-12 08:24:00','2026-05-12 08:24:00'),(1148,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 08:25:00','2026-05-12 08:25:00','2026-05-12 08:25:00'),(1149,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 08:26:00','2026-05-12 08:26:00','2026-05-12 08:26:00'),(1150,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 08:27:00','2026-05-12 08:27:00','2026-05-12 08:27:00'),(1151,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 08:28:00','2026-05-12 08:28:00','2026-05-12 08:28:00'),(1152,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 08:29:00','2026-05-12 08:29:00','2026-05-12 08:29:00'),(1153,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 08:30:00','2026-05-12 08:30:00','2026-05-12 08:30:00'),(1154,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 08:31:00','2026-05-12 08:31:00','2026-05-12 08:31:00'),(1155,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：14毫秒','0','','2026-05-12 08:32:00','2026-05-12 08:32:00','2026-05-12 08:32:00'),(1156,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：13毫秒','0','','2026-05-12 08:33:00','2026-05-12 08:33:00','2026-05-12 08:33:00'),(1157,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：13毫秒','0','','2026-05-12 08:34:00','2026-05-12 08:34:00','2026-05-12 08:34:00'),(1158,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：15毫秒','0','','2026-05-12 08:35:00','2026-05-12 08:35:00','2026-05-12 08:35:00'),(1159,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：15毫秒','0','','2026-05-12 08:36:00','2026-05-12 08:36:00','2026-05-12 08:36:00'),(1160,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：13毫秒','0','','2026-05-12 08:37:00','2026-05-12 08:37:00','2026-05-12 08:37:00'),(1161,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：8毫秒','0','','2026-05-12 08:38:00','2026-05-12 08:38:00','2026-05-12 08:38:00'),(1162,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：15毫秒','0','','2026-05-12 08:39:00','2026-05-12 08:39:00','2026-05-12 08:39:00'),(1163,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：13毫秒','0','','2026-05-12 08:40:00','2026-05-12 08:40:00','2026-05-12 08:40:00'),(1164,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 08:41:00','2026-05-12 08:41:00','2026-05-12 08:41:00'),(1165,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：6毫秒','0','','2026-05-12 08:42:00','2026-05-12 08:42:00','2026-05-12 08:42:00'),(1166,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：3毫秒','0','','2026-05-12 08:43:00','2026-05-12 08:43:00','2026-05-12 08:43:00'),(1167,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：27毫秒','0','','2026-05-12 08:44:00','2026-05-12 08:44:00','2026-05-12 08:44:00'),(1168,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：4毫秒','0','','2026-05-12 08:45:00','2026-05-12 08:45:00','2026-05-12 08:45:00'),(1169,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 08:46:00','2026-05-12 08:46:00','2026-05-12 08:46:00'),(1170,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：9毫秒','0','','2026-05-12 08:47:00','2026-05-12 08:47:00','2026-05-12 08:47:00'),(1171,'AI报告分析任务','DEFAULT','auditAiAnalysisTask.run()','AI报告分析任务 总共耗时：7毫秒','0','','2026-05-12 08:48:00','2026-05-12 08:48:00','2026-05-12 08:48:00');
/*!40000 ALTER TABLE `sys_job_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_logininfor`
--

DROP TABLE IF EXISTS `sys_logininfor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_logininfor` (
  `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '操作系统',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '提示消息',
  `login_time` datetime DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`),
  KEY `idx_sys_logininfor_s` (`status`),
  KEY `idx_sys_logininfor_lt` (`login_time`)
) ENGINE=InnoDB AUTO_INCREMENT=102 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统访问记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_logininfor`
--

LOCK TABLES `sys_logininfor` WRITE;
/*!40000 ALTER TABLE `sys_logininfor` DISABLE KEYS */;
INSERT INTO `sys_logininfor` VALUES (100,'admin','127.0.0.1','内网IP','Edge 148','Windows >=10','0','登录成功','2026-05-11 13:31:25'),(101,'admin','127.0.0.1','内网IP','Edge 148','Windows >=10','0','登录成功','2026-05-11 14:21:04');
/*!40000 ALTER TABLE `sys_logininfor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_menu`
--

DROP TABLE IF EXISTS `sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu` (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `path` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '路由参数',
  `route_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '路由名称',
  `is_frame` int DEFAULT '1' COMMENT '是否为外链（0是 1否）',
  `is_cache` int DEFAULT '0' COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2049 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_menu`
--

LOCK TABLES `sys_menu` WRITE;
/*!40000 ALTER TABLE `sys_menu` DISABLE KEYS */;
INSERT INTO `sys_menu` VALUES (1,'系统管理',0,1,'system',NULL,'','',1,0,'M','0','0','','audit-system-root','admin','2026-05-11 11:55:43','',NULL,'系统管理目录'),(2,'系统监控',0,2,'monitor',NULL,'','',1,0,'M','0','0','','monitor','admin','2026-05-11 11:55:43','',NULL,'系统监控目录'),(3,'系统工具',0,3,'tool',NULL,'','',1,0,'M','0','0','','tool','admin','2026-05-11 11:55:43','',NULL,'系统工具目录'),(100,'用户管理',1,1,'user','system/user/index','','',1,0,'C','0','0','system:user:list','user','admin','2026-05-11 11:55:43','',NULL,'用户管理菜单'),(101,'角色管理',1,2,'role','system/role/index','','',1,0,'C','0','0','system:role:list','peoples','admin','2026-05-11 11:55:43','',NULL,'角色管理菜单'),(102,'菜单管理',1,3,'menu','system/menu/index','','',1,0,'C','0','0','system:menu:list','tree-table','admin','2026-05-11 11:55:43','',NULL,'菜单管理菜单'),(103,'部门管理',1,4,'dept','system/dept/index','','',1,0,'C','0','0','system:dept:list','tree','admin','2026-05-11 11:55:43','',NULL,'部门管理菜单'),(104,'岗位管理',1,5,'post','system/post/index','','',1,0,'C','0','0','system:post:list','post','admin','2026-05-11 11:55:43','',NULL,'岗位管理菜单'),(105,'字典管理',1,6,'dict','system/dict/index','','',1,0,'C','0','0','system:dict:list','dict','admin','2026-05-11 11:55:43','',NULL,'字典管理菜单'),(106,'参数设置',1,7,'config','system/config/index','','',1,0,'C','0','0','system:config:list','edit','admin','2026-05-11 11:55:43','',NULL,'参数设置菜单'),(107,'通知公告',1,8,'notice','system/notice/index','','',1,0,'C','0','0','system:notice:list','message','admin','2026-05-11 11:55:43','',NULL,'通知公告菜单'),(108,'日志管理',1,9,'log','','','',1,0,'M','0','0','','log','admin','2026-05-11 11:55:43','',NULL,'日志管理菜单'),(109,'在线用户',2,1,'online','monitor/online/index','','',1,0,'C','0','0','monitor:online:list','online','admin','2026-05-11 11:55:43','',NULL,'在线用户菜单'),(110,'定时任务',2,2,'job','monitor/job/index','','',1,0,'C','0','0','monitor:job:list','job','admin','2026-05-11 11:55:43','',NULL,'定时任务菜单'),(111,'数据监控',2,3,'druid','monitor/druid/index','','',1,0,'C','0','0','monitor:druid:list','druid','admin','2026-05-11 11:55:43','',NULL,'数据监控菜单'),(112,'服务监控',2,4,'server','monitor/server/index','','',1,0,'C','0','0','monitor:server:list','server','admin','2026-05-11 11:55:43','',NULL,'服务监控菜单'),(113,'缓存监控',2,5,'cache','monitor/cache/index','','',1,0,'C','0','0','monitor:cache:list','redis','admin','2026-05-11 11:55:43','',NULL,'缓存监控菜单'),(114,'缓存列表',2,6,'cacheList','monitor/cache/list','','',1,0,'C','0','0','monitor:cache:list','redis-list','admin','2026-05-11 11:55:43','',NULL,'缓存列表菜单'),(115,'表单构建',3,1,'build','tool/build/index','','',1,0,'C','0','0','tool:build:list','build','admin','2026-05-11 11:55:43','',NULL,'表单构建菜单'),(116,'代码生成',3,2,'gen','tool/gen/index','','',1,0,'C','0','0','tool:gen:list','code','admin','2026-05-11 11:55:43','',NULL,'代码生成菜单'),(117,'系统接口',3,3,'swagger','tool/swagger/index','','',1,0,'C','0','0','tool:swagger:list','swagger','admin','2026-05-11 11:55:43','',NULL,'系统接口菜单'),(500,'操作日志',108,1,'operlog','monitor/operlog/index','','',1,0,'C','0','0','monitor:operlog:list','form','admin','2026-05-11 11:55:43','',NULL,'操作日志菜单'),(501,'登录日志',108,2,'logininfor','monitor/logininfor/index','','',1,0,'C','0','0','monitor:logininfor:list','logininfor','admin','2026-05-11 11:55:43','',NULL,'登录日志菜单'),(1000,'用户查询',100,1,'','','','',1,0,'F','0','0','system:user:query','#','admin','2026-05-11 11:55:43','',NULL,''),(1001,'用户新增',100,2,'','','','',1,0,'F','0','0','system:user:add','#','admin','2026-05-11 11:55:43','',NULL,''),(1002,'用户修改',100,3,'','','','',1,0,'F','0','0','system:user:edit','#','admin','2026-05-11 11:55:43','',NULL,''),(1003,'用户删除',100,4,'','','','',1,0,'F','0','0','system:user:remove','#','admin','2026-05-11 11:55:43','',NULL,''),(1004,'用户导出',100,5,'','','','',1,0,'F','0','0','system:user:export','#','admin','2026-05-11 11:55:43','',NULL,''),(1005,'用户导入',100,6,'','','','',1,0,'F','0','0','system:user:import','#','admin','2026-05-11 11:55:43','',NULL,''),(1006,'重置密码',100,7,'','','','',1,0,'F','0','0','system:user:resetPwd','#','admin','2026-05-11 11:55:43','',NULL,''),(1007,'角色查询',101,1,'','','','',1,0,'F','0','0','system:role:query','#','admin','2026-05-11 11:55:43','',NULL,''),(1008,'角色新增',101,2,'','','','',1,0,'F','0','0','system:role:add','#','admin','2026-05-11 11:55:43','',NULL,''),(1009,'角色修改',101,3,'','','','',1,0,'F','0','0','system:role:edit','#','admin','2026-05-11 11:55:43','',NULL,''),(1010,'角色删除',101,4,'','','','',1,0,'F','0','0','system:role:remove','#','admin','2026-05-11 11:55:43','',NULL,''),(1011,'角色导出',101,5,'','','','',1,0,'F','0','0','system:role:export','#','admin','2026-05-11 11:55:43','',NULL,''),(1012,'菜单查询',102,1,'','','','',1,0,'F','0','0','system:menu:query','#','admin','2026-05-11 11:55:43','',NULL,''),(1013,'菜单新增',102,2,'','','','',1,0,'F','0','0','system:menu:add','#','admin','2026-05-11 11:55:43','',NULL,''),(1014,'菜单修改',102,3,'','','','',1,0,'F','0','0','system:menu:edit','#','admin','2026-05-11 11:55:43','',NULL,''),(1015,'菜单删除',102,4,'','','','',1,0,'F','0','0','system:menu:remove','#','admin','2026-05-11 11:55:43','',NULL,''),(1016,'部门查询',103,1,'','','','',1,0,'F','0','0','system:dept:query','#','admin','2026-05-11 11:55:43','',NULL,''),(1017,'部门新增',103,2,'','','','',1,0,'F','0','0','system:dept:add','#','admin','2026-05-11 11:55:43','',NULL,''),(1018,'部门修改',103,3,'','','','',1,0,'F','0','0','system:dept:edit','#','admin','2026-05-11 11:55:43','',NULL,''),(1019,'部门删除',103,4,'','','','',1,0,'F','0','0','system:dept:remove','#','admin','2026-05-11 11:55:43','',NULL,''),(1020,'岗位查询',104,1,'','','','',1,0,'F','0','0','system:post:query','#','admin','2026-05-11 11:55:43','',NULL,''),(1021,'岗位新增',104,2,'','','','',1,0,'F','0','0','system:post:add','#','admin','2026-05-11 11:55:43','',NULL,''),(1022,'岗位修改',104,3,'','','','',1,0,'F','0','0','system:post:edit','#','admin','2026-05-11 11:55:43','',NULL,''),(1023,'岗位删除',104,4,'','','','',1,0,'F','0','0','system:post:remove','#','admin','2026-05-11 11:55:43','',NULL,''),(1024,'岗位导出',104,5,'','','','',1,0,'F','0','0','system:post:export','#','admin','2026-05-11 11:55:43','',NULL,''),(1025,'字典查询',105,1,'#','','','',1,0,'F','0','0','system:dict:query','#','admin','2026-05-11 11:55:43','',NULL,''),(1026,'字典新增',105,2,'#','','','',1,0,'F','0','0','system:dict:add','#','admin','2026-05-11 11:55:43','',NULL,''),(1027,'字典修改',105,3,'#','','','',1,0,'F','0','0','system:dict:edit','#','admin','2026-05-11 11:55:43','',NULL,''),(1028,'字典删除',105,4,'#','','','',1,0,'F','0','0','system:dict:remove','#','admin','2026-05-11 11:55:43','',NULL,''),(1029,'字典导出',105,5,'#','','','',1,0,'F','0','0','system:dict:export','#','admin','2026-05-11 11:55:43','',NULL,''),(1030,'参数查询',106,1,'#','','','',1,0,'F','0','0','system:config:query','#','admin','2026-05-11 11:55:43','',NULL,''),(1031,'参数新增',106,2,'#','','','',1,0,'F','0','0','system:config:add','#','admin','2026-05-11 11:55:43','',NULL,''),(1032,'参数修改',106,3,'#','','','',1,0,'F','0','0','system:config:edit','#','admin','2026-05-11 11:55:43','',NULL,''),(1033,'参数删除',106,4,'#','','','',1,0,'F','0','0','system:config:remove','#','admin','2026-05-11 11:55:43','',NULL,''),(1034,'参数导出',106,5,'#','','','',1,0,'F','0','0','system:config:export','#','admin','2026-05-11 11:55:43','',NULL,''),(1035,'公告查询',107,1,'#','','','',1,0,'F','0','0','system:notice:query','#','admin','2026-05-11 11:55:43','',NULL,''),(1036,'公告新增',107,2,'#','','','',1,0,'F','0','0','system:notice:add','#','admin','2026-05-11 11:55:43','',NULL,''),(1037,'公告修改',107,3,'#','','','',1,0,'F','0','0','system:notice:edit','#','admin','2026-05-11 11:55:43','',NULL,''),(1038,'公告删除',107,4,'#','','','',1,0,'F','0','0','system:notice:remove','#','admin','2026-05-11 11:55:43','',NULL,''),(1039,'操作查询',500,1,'#','','','',1,0,'F','0','0','monitor:operlog:query','#','admin','2026-05-11 11:55:43','',NULL,''),(1040,'操作删除',500,2,'#','','','',1,0,'F','0','0','monitor:operlog:remove','#','admin','2026-05-11 11:55:43','',NULL,''),(1041,'日志导出',500,3,'#','','','',1,0,'F','0','0','monitor:operlog:export','#','admin','2026-05-11 11:55:43','',NULL,''),(1042,'登录查询',501,1,'#','','','',1,0,'F','0','0','monitor:logininfor:query','#','admin','2026-05-11 11:55:43','',NULL,''),(1043,'登录删除',501,2,'#','','','',1,0,'F','0','0','monitor:logininfor:remove','#','admin','2026-05-11 11:55:43','',NULL,''),(1044,'日志导出',501,3,'#','','','',1,0,'F','0','0','monitor:logininfor:export','#','admin','2026-05-11 11:55:43','',NULL,''),(1045,'账户解锁',501,4,'#','','','',1,0,'F','0','0','monitor:logininfor:unlock','#','admin','2026-05-11 11:55:43','',NULL,''),(1046,'在线查询',109,1,'#','','','',1,0,'F','0','0','monitor:online:query','#','admin','2026-05-11 11:55:43','',NULL,''),(1047,'批量强退',109,2,'#','','','',1,0,'F','0','0','monitor:online:batchLogout','#','admin','2026-05-11 11:55:43','',NULL,''),(1048,'单条强退',109,3,'#','','','',1,0,'F','0','0','monitor:online:forceLogout','#','admin','2026-05-11 11:55:43','',NULL,''),(1049,'任务查询',110,1,'#','','','',1,0,'F','0','0','monitor:job:query','#','admin','2026-05-11 11:55:43','',NULL,''),(1050,'任务新增',110,2,'#','','','',1,0,'F','0','0','monitor:job:add','#','admin','2026-05-11 11:55:43','',NULL,''),(1051,'任务修改',110,3,'#','','','',1,0,'F','0','0','monitor:job:edit','#','admin','2026-05-11 11:55:43','',NULL,''),(1052,'任务删除',110,4,'#','','','',1,0,'F','0','0','monitor:job:remove','#','admin','2026-05-11 11:55:43','',NULL,''),(1053,'状态修改',110,5,'#','','','',1,0,'F','0','0','monitor:job:changeStatus','#','admin','2026-05-11 11:55:43','',NULL,''),(1054,'任务导出',110,6,'#','','','',1,0,'F','0','0','monitor:job:export','#','admin','2026-05-11 11:55:43','',NULL,''),(1055,'生成查询',116,1,'#','','','',1,0,'F','0','0','tool:gen:query','#','admin','2026-05-11 11:55:43','',NULL,''),(1056,'生成修改',116,2,'#','','','',1,0,'F','0','0','tool:gen:edit','#','admin','2026-05-11 11:55:43','',NULL,''),(1057,'生成删除',116,3,'#','','','',1,0,'F','0','0','tool:gen:remove','#','admin','2026-05-11 11:55:43','',NULL,''),(1058,'导入代码',116,4,'#','','','',1,0,'F','0','0','tool:gen:import','#','admin','2026-05-11 11:55:43','',NULL,''),(1059,'预览代码',116,5,'#','','','',1,0,'F','0','0','tool:gen:preview','#','admin','2026-05-11 11:55:43','',NULL,''),(1060,'生成代码',116,6,'#','','','',1,0,'F','0','0','tool:gen:code','#','admin','2026-05-11 11:55:43','',NULL,''),(2001,'审核列表管理',0,8,'audit','Layout',NULL,'AuditRoot',1,0,'M','0','0','','audit-review-root','admin','2026-05-11 11:55:58','',NULL,'审核业务模块'),(2002,'审核列表',2001,1,'review','audit/review/index',NULL,'AuditReview',1,0,'C','0','0','audit:review:list','list','admin','2026-05-11 11:55:58','',NULL,'审核列表管理'),(2003,'审核列表查询',2002,1,'#','',NULL,'',1,0,'F','0','0','audit:review:query','#','admin','2026-05-11 11:55:58','',NULL,''),(2004,'审核列表新增',2002,2,'#','',NULL,'',1,0,'F','0','0','audit:review:add','#','admin','2026-05-11 11:55:58','',NULL,''),(2005,'审核列表修改',2002,3,'#','',NULL,'',1,0,'F','0','0','audit:review:edit','#','admin','2026-05-11 11:55:58','',NULL,''),(2006,'审核列表删除',2002,4,'#','',NULL,'',1,0,'F','0','0','audit:review:remove','#','admin','2026-05-11 11:55:58','',NULL,''),(2007,'审核列表导出',2002,5,'#','',NULL,'',1,0,'F','0','0','audit:review:export','#','admin','2026-05-11 11:55:58','',NULL,''),(2008,'审核详情查看',2002,6,'#','',NULL,'',1,0,'F','0','0','audit:review:detail','#','admin','2026-05-11 11:55:58','',NULL,''),(2009,'审核版本追溯',2002,7,'#','',NULL,'',1,0,'F','0','0','audit:review:history','#','admin','2026-05-11 11:55:58','',NULL,''),(2010,'审核状态切换',2002,8,'#','',NULL,'',1,0,'F','0','0','audit:review:changeStatus','#','admin','2026-05-11 11:55:58','',NULL,''),(2011,'AI审核管理',0,9,'audit-ai','Layout',NULL,'AuditAiRoot',1,0,'M','0','0','','audit-ai-root','admin','2026-05-11 11:56:13','',NULL,'AI审核业务'),(2012,'AI任务队列',2011,1,'queue','audit/ai/index',NULL,'AuditAiQueue',1,0,'C','0','0','audit:ai:list','education','admin','2026-05-11 11:56:13','',NULL,'AI任务队列'),(2013,'AI任务查询',2012,1,'#','',NULL,'',1,0,'F','0','0','audit:ai:query','#','admin','2026-05-11 11:56:13','',NULL,''),(2014,'AI任务详情',2012,2,'#','',NULL,'',1,0,'F','0','0','audit:ai:detail','#','admin','2026-05-11 11:56:13','',NULL,''),(2015,'AI任务导出',2012,3,'#','',NULL,'',1,0,'F','0','0','audit:ai:export','#','admin','2026-05-11 11:56:13','',NULL,''),(2016,'AI任务状态切换',2012,4,'#','',NULL,'',1,0,'F','0','0','audit:ai:changeStatus','#','admin','2026-05-11 11:56:13','',NULL,''),(2017,'AI任务提升优先级',2012,5,'#','',NULL,'',1,0,'F','0','0','audit:ai:raisePriority','#','admin','2026-05-11 11:56:13','',NULL,''),(2018,'AI任务人工审核',2012,6,'#','',NULL,'',1,0,'F','0','0','audit:ai:review','#','admin','2026-05-11 11:56:13','',NULL,''),(2019,'AI任务删除',2012,7,'#','',NULL,'',1,0,'F','0','0','audit:ai:remove','#','admin','2026-05-11 11:56:13','',NULL,''),(2020,'AI任务重新分析',2012,8,'#','',NULL,'',1,0,'F','0','0','audit:ai:analyze','#','admin','2026-05-11 11:56:13','',NULL,'手动触发AI分析按钮权限'),(2021,'审核资产库',0,10,'audit-asset','Layout',NULL,'AuditAssetRoot',1,0,'M','0','0','','audit-asset-root','admin','2026-05-11 11:56:07','',NULL,'审核资产业务'),(2022,'审核资源列表',2021,1,'list','audit/asset/index',NULL,'AuditAssetList',1,0,'C','0','0','audit:asset:list','table','admin','2026-05-11 11:56:07','',NULL,'审核资源列表'),(2023,'审核资源查询',2022,1,'#','',NULL,'',1,0,'F','0','0','audit:asset:query','#','admin','2026-05-11 11:56:07','',NULL,''),(2024,'审核资源详情',2022,2,'#','',NULL,'',1,0,'F','0','0','audit:asset:detail','#','admin','2026-05-11 11:56:07','',NULL,''),(2025,'审核资源导出',2022,3,'#','',NULL,'',1,0,'F','0','0','audit:asset:export','#','admin','2026-05-11 11:56:07','',NULL,''),(2026,'审核资源权限分配',2022,4,'#','',NULL,'',1,0,'F','0','0','audit:asset:assign','#','admin','2026-05-11 11:56:07','',NULL,''),(2027,'审核资源删除',2022,5,'#','',NULL,'',1,0,'F','0','0','audit:asset:remove','#','admin','2026-05-11 11:56:07','',NULL,''),(2028,'审核资源批量下载',2022,6,'#','',NULL,'',1,0,'F','0','0','audit:asset:batchDownload','#','admin','2026-05-11 11:56:07','',NULL,''),(2029,'审核资源一键打包',2022,7,'#','',NULL,'',1,0,'F','0','0','audit:asset:batchPackage','#','admin','2026-05-11 11:56:07','',NULL,''),(2030,'审核资源重新上传',2022,8,'#','',NULL,'',1,0,'F','0','0','audit:asset:reupload','#','admin','2026-05-11 11:56:07','',NULL,''),(2031,'审核资源库',0,11,'audit-library','Layout',NULL,'AuditLibraryRoot',1,0,'M','0','0','','audit-library-root','admin','2026-05-11 11:56:07','',NULL,'审核资源库'),(2032,'审核文件库',2031,1,'folder','audit/library/folder',NULL,'AuditLibraryFolder',1,0,'C','0','0','audit:library:folder:list','tree','admin','2026-05-11 11:56:07','',NULL,'审核文件库'),(2033,'审核文件库查询',2032,1,'#','',NULL,'',1,0,'F','0','0','audit:library:folder:query','#','admin','2026-05-11 11:56:07','',NULL,''),(2034,'审核文件库新增',2032,2,'#','',NULL,'',1,0,'F','0','0','audit:library:folder:add','#','admin','2026-05-11 11:56:07','',NULL,''),(2035,'审核文件库修改',2032,3,'#','',NULL,'',1,0,'F','0','0','audit:library:folder:edit','#','admin','2026-05-11 11:56:07','',NULL,''),(2036,'审核文件库删除',2032,4,'#','',NULL,'',1,0,'F','0','0','audit:library:folder:remove','#','admin','2026-05-11 11:56:07','',NULL,''),(2037,'常用文件资源',2031,2,'common','audit/library/common',NULL,'AuditLibraryCommon',1,0,'C','1','0','audit:library:common:list','documentation','admin','2026-05-11 11:56:07','',NULL,'常用文件资源'),(2038,'常用文件资源查询',2037,1,'#','',NULL,'',1,0,'F','0','0','audit:library:common:query','#','admin','2026-05-11 11:56:07','',NULL,''),(2039,'常用文件资源新增',2037,2,'#','',NULL,'',1,0,'F','0','0','audit:library:common:add','#','admin','2026-05-11 11:56:07','',NULL,''),(2040,'常用文件资源修改',2037,3,'#','',NULL,'',1,0,'F','0','0','audit:library:common:edit','#','admin','2026-05-11 11:56:07','',NULL,''),(2041,'常用文件资源删除',2037,4,'#','',NULL,'',1,0,'F','0','0','audit:library:common:remove','#','admin','2026-05-11 11:56:07','',NULL,''),(2042,'常用文件资源导出',2037,5,'#','',NULL,'',1,0,'F','0','0','audit:library:common:export','#','admin','2026-05-11 11:56:07','',NULL,''),(2043,'常用文件资源归类',2037,6,'#','',NULL,'',1,0,'F','0','0','audit:library:common:assignFolder','#','admin','2026-05-11 11:56:07','',NULL,''),(2044,'任务文件资源',2031,3,'task','audit/library/task',NULL,'AuditLibraryTask',1,0,'C','0','0','audit:library:task:list','guide','admin','2026-05-11 11:56:07','',NULL,'任务文件资源'),(2045,'任务文件资源查询',2044,1,'#','',NULL,'',1,0,'F','0','0','audit:library:task:query','#','admin','2026-05-11 11:56:07','',NULL,''),(2046,'任务文件资源修改',2044,2,'#','',NULL,'',1,0,'F','0','0','audit:library:task:edit','#','admin','2026-05-11 11:56:07','',NULL,''),(2047,'任务文件资源删除',2044,3,'#','',NULL,'',1,0,'F','0','0','audit:library:task:remove','#','admin','2026-05-11 11:56:07','',NULL,''),(2048,'任务文件资源导出',2044,4,'#','',NULL,'',1,0,'F','0','0','audit:library:task:export','#','admin','2026-05-11 11:56:07','',NULL,'');
/*!40000 ALTER TABLE `sys_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_notice`
--

DROP TABLE IF EXISTS `sys_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notice` (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob COMMENT '公告内容',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通知公告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_notice`
--

LOCK TABLES `sys_notice` WRITE;
/*!40000 ALTER TABLE `sys_notice` DISABLE KEYS */;
INSERT INTO `sys_notice` VALUES (1,'温馨提醒：2018-07-01 若依新版本发布啦','2',_binary '新版本内容','0','admin','2026-05-11 11:55:44','',NULL,'管理员'),(2,'维护通知：2018-07-01 若依系统凌晨维护','1',_binary '维护内容','0','admin','2026-05-11 11:55:44','',NULL,'管理员'),(3,'若依开源框架介绍','1',_binary '<p><span style=\"color: rgb(230, 0, 0);\">项目介绍</span></p><p><font color=\"#333333\">RuoYi开源项目是为企业用户定制的后台脚手架框架，为企业打造的一站式解决方案，降低企业开发成本，提升开发效率。主要包括用户管理、角色管理、部门管理、菜单管理、参数管理、字典管理、</font><span style=\"color: rgb(51, 51, 51);\">岗位管理</span><span style=\"color: rgb(51, 51, 51);\">、定时任务</span><span style=\"color: rgb(51, 51, 51);\">、</span><span style=\"color: rgb(51, 51, 51);\">服务监控、登录日志、操作日志、代码生成等功能。其中，还支持多数据源、数据权限、国际化、Redis缓存、Docker部署、滑动验证码、第三方认证登录、分布式事务、</span><font color=\"#333333\">分布式文件存储</font><span style=\"color: rgb(51, 51, 51);\">、分库分表处理等技术特点。</span></p><p><img src=\"https://foruda.gitee.com/images/1773931848342439032/a4d22313_1815095.png\" style=\"width: 64px;\"><br></p><p><span style=\"color: rgb(230, 0, 0);\">官网及演示</span></p><p><span style=\"color: rgb(51, 51, 51);\">若依官网地址：&nbsp;</span><a href=\"http://ruoyi.vip\" target=\"_blank\">http://ruoyi.vip</a><a href=\"http://ruoyi.vip\" target=\"_blank\"></a></p><p><span style=\"color: rgb(51, 51, 51);\">若依文档地址：&nbsp;</span><a href=\"http://doc.ruoyi.vip\" target=\"_blank\">http://doc.ruoyi.vip</a><br></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【不分离版】：&nbsp;</span><a href=\"http://demo.ruoyi.vip\" target=\"_blank\">http://demo.ruoyi.vip</a></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【分离版本】：&nbsp;</span><a href=\"http://vue.ruoyi.vip\" target=\"_blank\">http://vue.ruoyi.vip</a></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【微服务版】：&nbsp;</span><a href=\"http://cloud.ruoyi.vip\" target=\"_blank\">http://cloud.ruoyi.vip</a></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【移动端版】：&nbsp;</span><a href=\"http://h5.ruoyi.vip\" target=\"_blank\">http://h5.ruoyi.vip</a></p><p><br style=\"color: rgb(48, 49, 51); font-family: &quot;Helvetica Neue&quot;, Helvetica, Arial, sans-serif; font-size: 12px;\"></p>','0','admin','2026-05-11 11:55:44','',NULL,'管理员');
/*!40000 ALTER TABLE `sys_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_notice_read`
--

DROP TABLE IF EXISTS `sys_notice_read`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notice_read` (
  `read_id` bigint NOT NULL AUTO_INCREMENT COMMENT '已读主键',
  `notice_id` int NOT NULL COMMENT '公告id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `read_time` datetime NOT NULL COMMENT '阅读时间',
  PRIMARY KEY (`read_id`),
  UNIQUE KEY `uk_user_notice` (`user_id`,`notice_id`) COMMENT '同一用户同一公告只记录一次'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告已读记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_notice_read`
--

LOCK TABLES `sys_notice_read` WRITE;
/*!40000 ALTER TABLE `sys_notice_read` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_notice_read` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_oper_log`
--

DROP TABLE IF EXISTS `sys_oper_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_oper_log` (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '模块标题',
  `business_type` int DEFAULT '0' COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '请求方式',
  `operator_type` int DEFAULT '0' COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '返回参数',
  `status` int DEFAULT '0' COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint DEFAULT '0' COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`),
  KEY `idx_sys_oper_log_bt` (`business_type`),
  KEY `idx_sys_oper_log_s` (`status`),
  KEY `idx_sys_oper_log_ot` (`oper_time`)
) ENGINE=InnoDB AUTO_INCREMENT=105 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_oper_log`
--

LOCK TABLES `sys_oper_log` WRITE;
/*!40000 ALTER TABLE `sys_oper_log` DISABLE KEYS */;
INSERT INTO `sys_oper_log` VALUES (100,'AI任务队列',3,'com.ruoyi.web.controller.audit.AuditAiController.remove()','DELETE',1,'admin','研发部门','/audit/ai/1,2,3,4','127.0.0.1','内网IP','[1,2,3,4] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-05-11 13:32:19',48),(101,'审核列表管理',1,'com.ruoyi.web.controller.audit.AuditReviewController.add()','POST',1,'admin','研发部门','/audit/review','127.0.0.1','内网IP','{\"aiAnalysisCount\":3,\"appendixFileUrls\":\"\",\"basisFileUrls\":\"\",\"createBy\":\"admin\",\"currentVersionNo\":\"v1.0\",\"deliveryUnit\":\"单位1\",\"handlerName\":\"若依\",\"mainReportUrls\":\"/profile/upload/2026/05/11/2025520398FB（批注本安部分）(0725)_20260511133249A001.docx\",\"params\":{},\"priority\":\"medium\",\"processFlag\":\"0\",\"productName\":\"本安-矿用本安型手机\",\"remark\":\"\",\"reviewStatus\":\"pending\",\"sponsor\":\"admin\",\"submitTime\":\"2026-05-11 13:33:04\",\"taskId\":5,\"taskNo\":\"SF-1778477584773\",\"taskStatus\":\"uploaded\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-05-11 13:33:04',69),(102,'AI任务队列',3,'com.ruoyi.web.controller.audit.AuditAiController.remove()','DELETE',1,'admin','研发部门','/audit/ai/5','127.0.0.1','内网IP','[5] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-05-11 14:21:11',15),(103,'审核列表管理',3,'com.ruoyi.web.controller.audit.AuditReviewController.remove()','DELETE',1,'admin','研发部门','/audit/review/5','127.0.0.1','内网IP','[5] ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-05-11 14:21:38',28),(104,'审核列表管理',1,'com.ruoyi.web.controller.audit.AuditReviewController.add()','POST',1,'admin','研发部门','/audit/review','127.0.0.1','内网IP','{\"aiAnalysisCount\":3,\"appendixFileUrls\":\"\",\"basisFileUrls\":\"\",\"createBy\":\"admin\",\"currentVersionNo\":\"v1.0\",\"deliveryUnit\":\"单位i1\",\"handlerName\":\"若依\",\"mainReportUrls\":\"/profile/upload/2026/05/11/2025520398FB（批注本安部分）(0725)_20260511142153A002.docx\",\"params\":{},\"priority\":\"medium\",\"processFlag\":\"0\",\"productName\":\"本安-矿用本安型手机\",\"remark\":\"\",\"reviewStatus\":\"pending\",\"sponsor\":\"admin\",\"submitTime\":\"2026-05-11 14:21:55\",\"taskId\":6,\"taskNo\":\"SF-1778480515928\",\"taskStatus\":\"uploaded\"} ','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2026-05-11 14:21:56',70);
/*!40000 ALTER TABLE `sys_oper_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_post`
--

DROP TABLE IF EXISTS `sys_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_post` (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '显示顺序',
  `status` char(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='岗位信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_post`
--

LOCK TABLES `sys_post` WRITE;
/*!40000 ALTER TABLE `sys_post` DISABLE KEYS */;
INSERT INTO `sys_post` VALUES (1,'ceo','董事长',1,'0','admin','2026-05-11 11:55:43','',NULL,''),(2,'se','项目经理',2,'0','admin','2026-05-11 11:55:43','',NULL,''),(3,'hr','人力资源',3,'0','admin','2026-05-11 11:55:43','',NULL,''),(4,'user','普通员工',4,'0','admin','2026-05-11 11:55:43','',NULL,'');
/*!40000 ALTER TABLE `sys_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) DEFAULT '1' COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) DEFAULT '1' COMMENT '部门树选择项是否关联显示',
  `status` char(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` VALUES (1,'超级管理员','admin',1,'1',1,1,'0','0','admin','2026-05-11 11:55:43','',NULL,'超级管理员'),(2,'普通角色','common',2,'2',1,1,'0','0','admin','2026-05-11 11:55:43','',NULL,'普通角色');
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_dept`
--

DROP TABLE IF EXISTS `sys_role_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_dept` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`,`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色和部门关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_dept`
--

LOCK TABLES `sys_role_dept` WRITE;
/*!40000 ALTER TABLE `sys_role_dept` DISABLE KEYS */;
INSERT INTO `sys_role_dept` VALUES (2,100),(2,101),(2,105);
/*!40000 ALTER TABLE `sys_role_dept` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_menu`
--

DROP TABLE IF EXISTS `sys_role_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色和菜单关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_menu`
--

LOCK TABLES `sys_role_menu` WRITE;
/*!40000 ALTER TABLE `sys_role_menu` DISABLE KEYS */;
INSERT INTO `sys_role_menu` VALUES (1,2001),(1,2002),(1,2003),(1,2004),(1,2005),(1,2006),(1,2007),(1,2008),(1,2009),(1,2010),(1,2011),(1,2012),(1,2013),(1,2014),(1,2015),(1,2016),(1,2017),(1,2018),(1,2019),(1,2020),(1,2021),(1,2022),(1,2023),(1,2024),(1,2025),(1,2026),(1,2027),(1,2028),(1,2029),(1,2030),(1,2031),(1,2032),(1,2033),(1,2034),(1,2035),(1,2036),(1,2037),(1,2038),(1,2039),(1,2040),(1,2041),(1,2042),(1,2043),(1,2044),(1,2045),(1,2046),(1,2047),(1,2048),(2,1),(2,2),(2,3),(2,4),(2,100),(2,101),(2,102),(2,103),(2,104),(2,105),(2,106),(2,107),(2,108),(2,109),(2,110),(2,111),(2,112),(2,113),(2,114),(2,115),(2,116),(2,117),(2,500),(2,501),(2,1000),(2,1001),(2,1002),(2,1003),(2,1004),(2,1005),(2,1006),(2,1007),(2,1008),(2,1009),(2,1010),(2,1011),(2,1012),(2,1013),(2,1014),(2,1015),(2,1016),(2,1017),(2,1018),(2,1019),(2,1020),(2,1021),(2,1022),(2,1023),(2,1024),(2,1025),(2,1026),(2,1027),(2,1028),(2,1029),(2,1030),(2,1031),(2,1032),(2,1033),(2,1034),(2,1035),(2,1036),(2,1037),(2,1038),(2,1039),(2,1040),(2,1041),(2,1042),(2,1043),(2,1044),(2,1045),(2,1046),(2,1047),(2,1048),(2,1049),(2,1050),(2,1051),(2,1052),(2,1053),(2,1054),(2,1055),(2,1056),(2,1057),(2,1058),(2,1059),(2,1060),(2,2001),(2,2002),(2,2003),(2,2004),(2,2005),(2,2006),(2,2007),(2,2008),(2,2009),(2,2010),(2,2011),(2,2012),(2,2013),(2,2014),(2,2015),(2,2016),(2,2017),(2,2018),(2,2019),(2,2021),(2,2022),(2,2023),(2,2024),(2,2025),(2,2026),(2,2027),(2,2028),(2,2029),(2,2030),(2,2031),(2,2032),(2,2033),(2,2034),(2,2035),(2,2036),(2,2037),(2,2038),(2,2039),(2,2040),(2,2041),(2,2042),(2,2043),(2,2044),(2,2045),(2,2046),(2,2047),(2,2048);
/*!40000 ALTER TABLE `sys_role_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) COLLATE utf8mb4_general_ci DEFAULT '00' COMMENT '用户类型（00系统用户）',
  `email` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '手机号码',
  `sex` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '密码',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` datetime DEFAULT NULL COMMENT '密码最后更新时间',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,103,'admin','若依','00','ry@163.com','15888888888','1','','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','0','0','127.0.0.1','2026-05-11 14:21:05','2026-05-11 11:55:43','admin','2026-05-11 11:55:43','',NULL,'管理员'),(2,105,'ry','若依','00','ry@qq.com','15666666666','1','','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','0','0','127.0.0.1','2026-05-11 11:55:43','2026-05-11 11:55:43','admin','2026-05-11 11:55:43','',NULL,'测试员');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_post`
--

DROP TABLE IF EXISTS `sys_user_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_post` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`,`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户与岗位关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_post`
--

LOCK TABLES `sys_user_post` WRITE;
/*!40000 ALTER TABLE `sys_user_post` DISABLE KEYS */;
INSERT INTO `sys_user_post` VALUES (1,1),(2,2);
/*!40000 ALTER TABLE `sys_user_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_role`
--

DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户和角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_role`
--

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
INSERT INTO `sys_user_role` VALUES (1,1),(2,2);
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'ry-vue'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-12  8:56:37
