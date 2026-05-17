# Program Repository

这个仓库包含多个项目。

---

## 项目列表

### 1. AWS Partner Support Ticket System (SPT)

符合验证要求的工单管理系统

**目录**: [aws-spt-system/](./aws-spt-system/)

**功能特性**:
-  SPT-001: 基础SLA - 可配置的响应时间SLA
-  SPT-002: 客户服务可用性 - 24x7多渠道支持
-  SPT-003: 工单创建 - Web门户、邮件、API
-  SPT-004: 服务台运营 - 优先级和严重性级别管理
-  SPT-005: AWS Support Case监控 - 自动同步AWS Support Case
-  SPT-006: AWS事件管理 - 组织级Health Dashboard监控

**技术栈**: vue + TypeScript + Node.js + Java + Mysql 

**文档**: 
- [快速开始](./aws-spt-system/QUICKSTART.md)
- [运行指南](./aws-spt-system/RUNNING.md)
- [SPT合规文档](./aws-spt-system/docs/SPT-SUMMARY.md)

---

### 2. 文档资源管理平台

基于Spring Boot的Web文档资源管理平台，支持文档上传、浏览、管理和下载功能。

**功能特性**:
- 文件上传（普通上传、分片上传、断点续传）
- 文件下载（普通下载、流式下载、断点续传下载）
- 文件管理（列表浏览、信息查看、删除）
- 文件去重（同名同内容复用，同名不同内容独立保存）
- 用户体验优化（拖拽上传、实时进度、速度显示）

**技术栈**: Spring Boot 2.7.0 + MySQL 8.0 + Vue.js 2.x + Bootstrap 5

---

### 3. Java + Vue CRM 客户管理系统

前后端分离的 CRM 客户管理系统，支持客户、联系人、跟进记录和商机管道管理。

**目录**: [java-vue-crm/](./java-vue-crm/)

**功能特性**:
- 客户管理：客户列表、搜索筛选、详情、新增、编辑、删除
- 联系人管理：维护客户联系人和主要联系人
- 跟进管理：记录跟进内容、跟进方式、负责人和下次跟进时间
- 商机管理：商机阶段、金额、概率、预计签约日期和下一步动作
- 数据看板：客户统计、待跟进提醒、商机金额和加权预测

**技术栈**: Spring Boot 3.5 + Java 21 + Vue 3 + Vite + H2

---

## 许可证

MIT License
