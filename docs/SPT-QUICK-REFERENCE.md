# SPT合规快速参考

## 📍 6个SPT要求的体现位置速查表

### SPT-001: 基础SLA
**要求**: 存储在文档中心中的文档/客户服务合同中包括SLA政策

| 类型 | 位置 | 说明 |
|------|------|------|
| 📄 文档 | `docs/SLA-POLICY.md` | 完整SLA政策（响应时间、操作、通知） |
| 📄 合同 | `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md` | 第二条：SLA详细条款 |
| 💻 界面 | `frontend/src/pages/SLAConfig.tsx` | SLA配置管理页面 `/sla-config` |
| ⚙️ 代码 | `backend/src/services/SLAService.ts` | SLA服务实现 |
| 🗄️ 数据库 | `sla_config` 表 | 存储4级优先级配置 |

---

### SPT-002: 客户服务可用性
**要求**: 24×7支持流程和人员配备

| 类型 | 位置 | 说明 |
|------|------|------|
| 📄 文档 | `docs/SLA-POLICY.md` 第5章 | 服务可用性（24×7、99.9%） |
| 📄 合同 | `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md` 2.2 | 服务可用性承诺 |
| 💻 界面 | 多渠道接入 | Web门户、邮件、API |
| ⚙️ 代码 | `backend/src/services/NotificationService.ts` | 通知服务 |
| 🗄️ 数据库 | `on_call_schedule` 表 | 值班表管理 |

---

### SPT-003: 工单创建
**要求**: 客户如何创建工单

| 类型 | 位置 | 说明 |
|------|------|------|
| 📄 文档 | `docs/SLA-POLICY.md` 第3.1章 | 工单处理流程 |
| 📄 合同 | `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md` 2.3 | 工单管理流程 |
| 💻 界面 | `frontend/src/pages/CreateTicket.tsx` | 工单创建页面 `/tickets/create` |
| ⚙️ 代码 | `backend/src/services/TicketService.ts` | 工单服务 |
| 🗄️ 数据库 | `tickets` 表 | 工单存储和状态流转 |

---

### SPT-004: 服务台运营
**要求**: 定义和记录支持优先级和严重性级别

| 类型 | 位置 | 说明 |
|------|------|------|
| 📄 文档 | `docs/SLA-POLICY.md` 第2章 | 4级优先级定义表格 |
| 📄 合同 | `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md` 2.1 | 响应时间承诺表格 |
| 💻 界面 | `frontend/src/pages/SLAConfig.tsx` | 优先级展示 |
| ⚙️ 代码 | 优先级枚举定义 | Critical/High/Medium/Low |
| 🗄️ 数据库 | `sla_config` 表 + `knowledge_base` 表 | 配置和知识库 |

**优先级定义**:
- **Critical**: ≤15分钟响应，≤4小时解决 - 生产环境完全中断
- **High**: ≤1小时响应，≤8小时解决 - 生产环境部分功能受影响
- **Medium**: ≤4小时响应，≤24小时解决 - 非关键功能问题
- **Low**: ≤24小时响应，≤72小时解决 - 一般性咨询

---

### SPT-005: AWS Support Case更新
**要求**: 确保不错过AWS Support Case更新，全流程监控并通知客户

| 类型 | 位置 | 说明 |
|------|------|------|
| 📄 文档 | `docs/SLA-POLICY.md` 第3.3章 | AWS Support Case管理 |
| 📄 合同 | `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md` 2.4 | AWS集成服务 |
| ⚙️ 代码 | `backend/src/integrations/AWSSupportIntegration.ts` | AWS Support API集成 |
| 🔄 定时任务 | `backend/src/jobs/index.ts` | 每10分钟轮询活跃Case |
| 🗄️ 数据库 | `tickets` 表 | `aws_case_id`, `aws_case_status` 字段 |

**核心功能**:
- ✅ 双向同步（本地工单 ↔ AWS Case）
- ✅ 每10分钟自动检查更新
- ✅ 新通信自动添加到工单评论
- ✅ 状态变更自动通知客户

---

### SPT-006: AWS事件管理
**要求**: 监控AWS Health Dashboard，组织级别，运营团队后续流程

| 类型 | 位置 | 说明 |
|------|------|------|
| 📄 文档 | `docs/SLA-POLICY.md` 第3.4章 | AWS Health事件管理 |
| 📄 合同 | `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md` 2.4 | AWS Health监控 |
| ⚙️ 代码 | `backend/src/integrations/AWSHealthIntegration.ts` | AWS Health API集成（组织级） |
| 🔄 定时任务 | `backend/src/jobs/index.ts` | 每15分钟轮询Health事件 |
| 🗄️ 数据库 | `tickets` 表 | `aws_health_event_arn` 字段 |

**核心功能**:
- ✅ 组织级Health API集成（us-east-1）
- ✅ EventBridge规则自动转发Health事件
- ✅ 自动创建事件工单
- ✅ 识别受影响的客户账户
- ✅ 批量通知受影响客户
- ✅ 事件优先级自动判定

**运营流程**:
1. EventBridge接收Health事件 → SQS队列
2. 系统处理事件 → 获取详情和受影响资源
3. 自动创建工单 → 判定优先级
4. 识别受影响客户 → 批量通知
5. 工程师跟进 → 持续监控直到解决

---

## 🎯 审计时快速展示路径

### 1. 文档中心（最重要）
```
docs/
├── SLA-POLICY.md                          ← SPT-001, 002, 003, 004, 005, 006
├── CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md  ← SPT-001, 002, 003, 004, 005, 006
├── SPT-COMPLIANCE.md                      ← 完整合规说明
├── SPT-COMPLIANCE-CHECKLIST.md            ← 详细检查清单
└── SPT-001-AUDIT-GUIDE.md                 ← SPT-001审计指南
```

### 2. Web界面演示
```
/sla-config          ← SPT-001, 004: SLA配置和优先级定义
/tickets/create      ← SPT-003: 工单创建
/tickets             ← SPT-003, 005, 006: 工单列表和详情
```

### 3. 代码实现
```
backend/src/
├── services/
│   ├── SLAService.ts           ← SPT-001: SLA管理
│   ├── TicketService.ts        ← SPT-003: 工单服务
│   └── NotificationService.ts  ← SPT-002: 通知服务
├── integrations/
│   ├── AWSSupportIntegration.ts  ← SPT-005: Support Case集成
│   └── AWSHealthIntegration.ts   ← SPT-006: Health事件集成
└── jobs/
    └── index.ts                  ← SPT-005, 006: 定时任务
```

### 4. 数据库设计
```sql
-- SPT-001, 004: SLA配置
sla_config (priority, response_time_minutes, resolution_time_hours, description)

-- SPT-002: 值班表
on_call_schedule (user_id, start_time, end_time, is_primary)

-- SPT-003, 005, 006: 工单
tickets (
  id, customer_id, title, description, priority, status,
  aws_case_id,           -- SPT-005
  aws_health_event_arn,  -- SPT-006
  sla_due_at             -- SPT-001
)

-- SPT-004: 知识库
knowledge_base (title, content, category, tags)
```

---

## ✅ 合规状态总览

| SPT | 要求 | 文档 | 代码 | 数据库 | 状态 |
|-----|------|------|------|--------|------|
| 001 | 基础SLA | ✅ | ✅ | ✅ | ✅ 完成 |
| 002 | 服务可用性 | ✅ | ✅ | ✅ | ✅ 完成 |
| 003 | 工单创建 | ✅ | ✅ | ✅ | ✅ 完成 |
| 004 | 服务台运营 | ✅ | ✅ | ✅ | ✅ 完成 |
| 005 | Support Case | ✅ | ✅ | ✅ | ✅ 完成 |
| 006 | 事件管理 | ✅ | ✅ | ✅ | ✅ 完成 |

**所有6个SPT要求均已满足！** 🎉

---

## 📞 审计支持

如需审计支持，请联系：
- 合规负责人: compliance@example.com
- 技术负责人: tech@example.com

**文档版本**: 1.0  
**最后更新**: 2025-12-25
