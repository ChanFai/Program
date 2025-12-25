# Program Repository

这个仓库包含多个项目。

---

## 项目列表

### 1. AWS Partner Support Ticket System (SPT)

符合AWS SPP技术验证要求的工单管理系统

**目录**: 当前根目录

**功能特性**:
- ✅ SPT-001: 基础SLA - 可配置的响应时间SLA
- ✅ SPT-002: 客户服务可用性 - 24x7多渠道支持
- ✅ SPT-003: 工单创建 - Web门户、邮件、API
- ✅ SPT-004: 服务台运营 - 优先级和严重性级别管理
- ✅ SPT-005: AWS Support Case监控 - 自动同步AWS Support Case
- ✅ SPT-006: AWS事件管理 - 组织级Health Dashboard监控

**技术栈**: React 18 + TypeScript + Node.js + Express + PostgreSQL + AWS Services

**文档**: 
- [快速开始](./QUICKSTART.md)
- [运行指南](./RUNNING.md)
- [SPT合规文档](./docs/SPT-SUMMARY.md)

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

## 许可证

MIT License
