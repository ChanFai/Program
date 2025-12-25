# SPT合规文档索引

快速查找所有SPT相关文档和代码位置。

---

## 📚 文档导航

### 快速开始
- **[SPT总结](./SPT-SUMMARY.md)** ⭐ - 最快速的答案：6个SPT要求分别在哪里体现
- **[SPT快速参考](./SPT-QUICK-REFERENCE.md)** - 速查表，包含所有位置信息

### 详细文档
- **[SPT合规说明](./SPT-COMPLIANCE.md)** - 完整的合规实现说明
- **[SPT合规检查清单](./SPT-COMPLIANCE-CHECKLIST.md)** - 详细检查清单和审计准备
- **[SPT流程图](./SPT-PROCESS-FLOWS.md)** - SPT-005和SPT-006的详细流程说明

### 政策文档（最重要）
- **[SLA政策文档](./SLA-POLICY.md)** ⭐⭐⭐ - 满足所有6个SPT要求的核心文档
- **[客户服务合同模板](./CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md)** ⭐⭐⭐ - 正式合同模板

### 专项指南
- **[SPT-001审计指南](./SPT-001-AUDIT-GUIDE.md)** - SPT-001专项审计指南

---

## 🎯 按SPT要求查找

### SPT-001: 基础SLA

**文档**:
- `docs/SLA-POLICY.md` - 第2、3、4、5、6章
- `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md` - 第二、三、四条

**代码**:
- `frontend/src/pages/SLAConfig.tsx` - SLA配置界面
- `backend/src/services/SLAService.ts` - SLA服务
- `backend/src/routes/sla.ts` - SLA API

**数据库**:
- `database/migrations/001_initial_schema.sql` - `sla_config` 表

---

### SPT-002: 客户服务可用性

**文档**:
- `docs/SLA-POLICY.md` - 第5章（服务可用性）
- `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md` - 第二条2.2

**代码**:
- `frontend/src/pages/CreateTicket.tsx` - Web门户
- `backend/src/routes/tickets.ts` - REST API
- `backend/src/services/NotificationService.ts` - 通知服务

**数据库**:
- `database/migrations/001_initial_schema.sql` - `on_call_schedule` 表

---

### SPT-003: 工单创建

**文档**:
- `docs/SLA-POLICY.md` - 第3.1章（工单处理流程）
- `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md` - 第二条2.3

**代码**:
- `frontend/src/pages/CreateTicket.tsx` - 工单创建页面
- `backend/src/services/TicketService.ts` - 工单服务
- `backend/src/routes/tickets.ts` - 工单API

**数据库**:
- `database/migrations/001_initial_schema.sql` - `tickets` 表

---

### SPT-004: 服务台运营

**文档**:
- `docs/SLA-POLICY.md` - 第2章（优先级定义表格）
- `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md` - 第二条2.1

**代码**:
- `frontend/src/pages/SLAConfig.tsx` - 优先级展示
- `backend/src/models/Ticket.ts` - 优先级枚举定义

**数据库**:
- `database/migrations/001_initial_schema.sql` - `sla_config` 表、`knowledge_base` 表

---

### SPT-005: AWS Support Case更新

**文档**:
- `docs/SLA-POLICY.md` - 第3.3章（AWS Support Case管理）
- `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md` - 第二条2.4
- `docs/SPT-PROCESS-FLOWS.md` - SPT-005详细流程图

**代码**:
- `backend/src/integrations/AWSSupportIntegration.ts` ⭐ - 核心实现
- `backend/src/jobs/index.ts` - 定时任务（每10分钟）

**数据库**:
- `database/migrations/001_initial_schema.sql` - `tickets` 表的 `aws_case_id` 等字段

**关键功能**:
```typescript
syncSupportCase()           // 同步Case状态
syncCaseCommunications()    // 同步通信记录
pollAllActiveCases()        // 每10分钟轮询
```

---

### SPT-006: AWS事件管理

**文档**:
- `docs/SLA-POLICY.md` - 第3.4章（AWS Health事件管理）
- `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md` - 第二条2.4
- `docs/SPT-PROCESS-FLOWS.md` - SPT-006详细流程图

**代码**:
- `backend/src/integrations/AWSHealthIntegration.ts` ⭐ - 核心实现
- `backend/src/jobs/index.ts` - 定时任务（每15分钟）

**数据库**:
- `database/migrations/001_initial_schema.sql` - `tickets` 表的 `aws_health_event_arn` 字段

**关键功能**:
```typescript
setupEventBridgeRule()      // 配置EventBridge规则（组织级）
processHealthEvent()        // 处理Health事件
notifyAffectedCustomers()   // 通知受影响客户
pollHealthEvents()          // 每15分钟轮询
```

---

## 📂 文件结构

```
docs/
├── SPT-INDEX.md                          ← 你在这里
├── SPT-SUMMARY.md                        ← 最快速的答案
├── SPT-QUICK-REFERENCE.md                ← 速查表
├── SPT-COMPLIANCE.md                     ← 完整合规说明
├── SPT-COMPLIANCE-CHECKLIST.md           ← 详细检查清单
├── SPT-PROCESS-FLOWS.md                  ← 流程图说明
├── SPT-001-AUDIT-GUIDE.md                ← SPT-001审计指南
├── SLA-POLICY.md                         ← ⭐⭐⭐ 核心政策文档
├── CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md ← ⭐⭐⭐ 合同模板
└── README.md                             ← 文档中心说明

backend/src/
├── services/
│   ├── SLAService.ts                     ← SPT-001
│   ├── TicketService.ts                  ← SPT-001, 003
│   └── NotificationService.ts            ← SPT-002
├── integrations/
│   ├── AWSSupportIntegration.ts          ← SPT-005 ⭐
│   └── AWSHealthIntegration.ts           ← SPT-006 ⭐
├── routes/
│   ├── sla.ts                            ← SPT-001
│   └── tickets.ts                        ← SPT-003
└── jobs/
    └── index.ts                          ← SPT-005, 006 定时任务

frontend/src/pages/
├── SLAConfig.tsx                         ← SPT-001, 004
├── CreateTicket.tsx                      ← SPT-003
└── TicketList.tsx                        ← SPT-003

database/migrations/
└── 001_initial_schema.sql                ← 所有数据库表
```

---

## 🔍 按关键词查找

### 响应时间、SLA
→ `docs/SLA-POLICY.md` 第2章
→ `frontend/src/pages/SLAConfig.tsx`
→ `backend/src/services/SLAService.ts`

### 24×7支持、服务可用性
→ `docs/SLA-POLICY.md` 第5章
→ `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md` 第二条2.2
→ `database/migrations/001_initial_schema.sql` - `on_call_schedule` 表

### 工单创建、工单流转
→ `docs/SLA-POLICY.md` 第3.1章
→ `frontend/src/pages/CreateTicket.tsx`
→ `backend/src/services/TicketService.ts`

### 优先级、严重性级别
→ `docs/SLA-POLICY.md` 第2章（表格）
→ `frontend/src/pages/SLAConfig.tsx`
→ `database/migrations/001_initial_schema.sql` - `sla_config` 表

### AWS Support Case、同步、通知
→ `docs/SPT-PROCESS-FLOWS.md` - SPT-005流程
→ `backend/src/integrations/AWSSupportIntegration.ts`
→ `backend/src/jobs/index.ts` - 每10分钟轮询

### AWS Health、事件管理、组织级
→ `docs/SPT-PROCESS-FLOWS.md` - SPT-006流程
→ `backend/src/integrations/AWSHealthIntegration.ts`
→ `backend/src/jobs/index.ts` - 每15分钟轮询

---

## 📊 审计材料清单

### 立即可用
- [x] `docs/SLA-POLICY.md`
- [x] `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md`
- [x] 所有代码文件
- [x] 数据库设计

### 需要系统运行后准备
- [ ] SLA配置界面截图
- [ ] 工单创建流程截图
- [ ] SLA达成率报告
- [ ] AWS Support Case同步日志
- [ ] AWS Health事件处理记录
- [ ] 客户通知邮件示例

---

## 🎯 给审计人员的演示顺序

1. **打开 `docs/SLA-POLICY.md`** - 展示完整的SLA政策
2. **打开 `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md`** - 展示正式合同
3. **启动系统** - 访问 `/sla-config` 页面
4. **展示代码** - 打开关键的集成文件
5. **展示数据库** - 打开 `001_initial_schema.sql`
6. **如有数据** - 展示实际运行报告

---

## ✅ 快速检查

所有6个SPT要求是否都有对应的：

| SPT | 文档 | 代码 | 数据库 | 状态 |
|-----|------|------|--------|------|
| 001 | ✅ | ✅ | ✅ | ✅ |
| 002 | ✅ | ✅ | ✅ | ✅ |
| 003 | ✅ | ✅ | ✅ | ✅ |
| 004 | ✅ | ✅ | ✅ | ✅ |
| 005 | ✅ | ✅ | ✅ | ✅ |
| 006 | ✅ | ✅ | ✅ | ✅ |

**所有要求均已满足！** 🎉

---

## 📞 需要帮助？

- 查看 `docs/SPT-SUMMARY.md` 获取最快速的答案
- 查看 `docs/SPT-QUICK-REFERENCE.md` 获取速查表
- 查看 `docs/SPT-COMPLIANCE-CHECKLIST.md` 获取详细检查清单

**文档版本**: 1.0  
**最后更新**: 2025-12-25
