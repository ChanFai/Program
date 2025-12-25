# SPT合规性检查清单

本文档详细列出所有6个SPT要求在系统中的体现位置，方便审计和检查。

---

## ✅ SPT-001: 基础SLA

**要求**: 描述您的基本SLA。基础SLA是与AWS合作伙伴向其客户的响应时间、操作和通知相关的SLA。

**关键要求**: 存储在文档中心中的文档/客户服务合同中包括计费支持的服务级别协议（SLA）方面的政策的演示或描述。

### 📍 体现位置

#### 1. 文档中心 - SLA政策文档
**文件**: `docs/SLA-POLICY.md`

**包含内容**:
- ✅ 第2章：响应时间SLA（Critical/High/Medium/Low四级）
- ✅ 第3章：操作标准（工单处理流程、升级机制、AWS集成）
- ✅ 第4章：通知要求（客户和内部通知）
- ✅ 第5章：服务可用性（24×7支持、99.9%可用性）
- ✅ 第6章：SLA报告和监控
- ✅ 第7章：SLA违规和补偿

#### 2. 文档中心 - 客户服务合同模板
**文件**: `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md`

**包含内容**:
- ✅ 第二条：服务级别协议（SLA）详细条款
  - 2.1 响应时间承诺表格
  - 2.2 服务可用性
  - 2.3 操作标准
  - 2.4 AWS集成服务
  - 2.5 通知要求
- ✅ 第三条：SLA报告和监控
- ✅ 第四条：SLA违规和补偿

#### 3. Web界面
**文件**: `frontend/src/pages/SLAConfig.tsx`
**访问路径**: `/sla-config`

**展示内容**:
- SLA配置管理界面
- 响应时间标准表格
- 近30天SLA达成率统计
- 操作标准说明
- 直接链接到文档中心的政策文档

#### 4. 后端实现
**文件**: 
- `backend/src/services/SLAService.ts` - SLA服务
- `backend/src/services/TicketService.ts` - 工单服务（自动计算SLA）
- `backend/src/routes/sla.ts` - SLA API路由

**功能**:
- 自动计算SLA截止时间
- SLA指标统计
- SLA违规检测
- SLA报告生成

#### 5. 数据库
**文件**: `database/migrations/001_initial_schema.sql`

**表结构**:
```sql
CREATE TABLE sla_config (
  priority VARCHAR(20) PRIMARY KEY,
  response_time_minutes INTEGER NOT NULL,
  resolution_time_hours INTEGER NOT NULL,
  description TEXT,
  ...
);
```

**默认配置**:
- Critical: 15分钟响应，4小时解决
- High: 1小时响应，8小时解决
- Medium: 4小时响应，24小时解决
- Low: 24小时响应，72小时解决

### 📊 证明材料
- [x] SLA政策文档
- [x] 客户服务合同模板
- [x] SLA配置界面
- [x] 数据库schema
- [x] 代码实现
- [ ] 系统运行截图（需运行后截图）
- [ ] SLA达成率报告（需实际数据）

---

## ✅ SPT-002: 客户服务可用性

**要求**: 客户如何联系您寻求24x7支持？您的运营团队如何配备全天候支持人员，以及相应流程？

**关键要求**: 存储在文档中心中的文档/客户服务合同中反映伙伴支持流程和服务等级政策的演示或描述。

### 📍 体现位置

#### 1. 文档中心 - SLA政策文档
**文件**: `docs/SLA-POLICY.md`

**相关章节**:
- ✅ 第5章：服务可用性
  - 支持时间：24×7全天候支持
  - 值班制度：轮班制确保随时有工程师响应
  - 多渠道接入：Web、邮件、API
  - 系统可用性：目标99.9%
- ✅ 第9章：联系方式
  - 支持邮箱
  - 紧急热线
  - Web门户

#### 2. 文档中心 - 客户服务合同模板
**文件**: `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md`

**相关条款**:
- ✅ 第二条 2.2：服务可用性
  - 支持时间：7×24小时全天候技术支持
  - 系统可用性：支持平台保证99.9%正常运行时间
  - 值班制度：确保任何时间都有合格工程师响应

#### 3. 数据库 - 值班表管理
**文件**: `database/migrations/001_initial_schema.sql`

**表结构**:
```sql
CREATE TABLE on_call_schedule (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES users(id),
  start_time TIMESTAMP NOT NULL,
  end_time TIMESTAMP NOT NULL,
  is_primary BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT NOW()
);
```

#### 4. 多渠道接入实现
**文件**: 
- `frontend/src/pages/CreateTicket.tsx` - Web门户
- `backend/src/routes/tickets.ts` - REST API
- 邮件转工单（通过SES接收）

### 📊 证明材料
- [x] SLA政策文档（第5章）
- [x] 客户服务合同（第二条2.2）
- [x] 值班表数据库设计
- [ ] 值班排班表示例（需实际数据）
- [ ] 多渠道接入演示截图
- [ ] 24×7支持流程文档

---

## ✅ SPT-003: 工单创建

**要求**: 客户如何在您的支持系统中创建活动/事件票证？

**关键要求**: 演示或描述伙伴工单系统的工单创建和流转过程。

### 📍 体现位置

#### 1. 文档中心 - SLA政策文档
**文件**: `docs/SLA-POLICY.md`

**相关章节**:
- ✅ 第3.1章：工单处理流程
  1. 工单创建：客户通过Web门户、邮件或API创建工单
  2. 自动分配：系统根据优先级和专业领域自动分配
  3. 首次响应：工程师在SLA时间内提供首次响应
  4. 持续跟进：定期更新工单状态
  5. 问题解决：完成问题修复
  6. 客户确认：获得客户确认后关闭工单

#### 2. 文档中心 - 客户服务合同模板
**文件**: `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md`

**相关条款**:
- ✅ 第二条 2.3：操作标准 - 工单管理
  - 所有服务请求通过工单系统统一管理
  - 自动分配给最合适的工程师
  - 完整的工单生命周期跟踪

#### 3. Web门户 - 工单创建页面
**文件**: `frontend/src/pages/CreateTicket.tsx`
**访问路径**: `/tickets/create`

**功能**:
- 填写工单标题和描述
- 选择优先级
- 上传附件
- 提交后自动分配

#### 4. REST API
**文件**: `backend/src/routes/tickets.ts`

**API端点**:
```
POST /api/tickets
{
  "title": "问题标题",
  "description": "问题描述",
  "priority": "high",
  "customer_id": "客户ID"
}
```

#### 5. 邮件转工单
**实现**: 通过AWS SES接收邮件并自动创建工单

#### 6. 工单流转
**文件**: `backend/src/services/TicketService.ts`

**状态流转**:
- new → in_progress → waiting_customer → resolved → closed

#### 7. 数据库设计
**文件**: `database/migrations/001_initial_schema.sql`

**工单表**:
```sql
CREATE TABLE tickets (
  id UUID PRIMARY KEY,
  customer_id UUID REFERENCES customers(id),
  title VARCHAR(500) NOT NULL,
  description TEXT,
  priority VARCHAR(20) NOT NULL,
  status VARCHAR(30) NOT NULL,
  source VARCHAR(20) NOT NULL, -- web, email, api
  assigned_to UUID,
  sla_due_at TIMESTAMP NOT NULL,
  ...
);
```

### 📊 证明材料
- [x] SLA政策文档（第3.1章）
- [x] 客户服务合同（第二条2.3）
- [x] Web界面代码
- [x] API实现代码
- [x] 数据库设计
- [ ] 工单创建流程截图
- [ ] API调用示例
- [ ] 邮件转工单演示

---

## ✅ SPT-004: 服务台运营

**要求**: 客户可以在哪里找到您定义和记录的支持优先级和严重性级别？

**关键要求**: 演示或描述伙伴运营部门对故障优先级的定义、严重性级别的定义文档或政策。

### 📍 体现位置

#### 1. 文档中心 - SLA政策文档
**文件**: `docs/SLA-POLICY.md`

**相关章节**:
- ✅ 第2章：响应时间SLA

| 优先级 | 首次响应时间 | 解决目标时间 | 描述 |
|--------|------------|------------|------|
| **Critical** | ≤ 15分钟 | ≤ 4小时 | 生产环境完全中断，业务严重受影响 |
| **High** | ≤ 1小时 | ≤ 8小时 | 生产环境部分功能受影响 |
| **Medium** | ≤ 4小时 | ≤ 24小时 | 非关键功能问题或性能下降 |
| **Low** | ≤ 24小时 | ≤ 72小时 | 一般性咨询、功能请求或轻微问题 |

#### 2. 文档中心 - 客户服务合同模板
**文件**: `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md`

**相关条款**:
- ✅ 第二条 2.1：响应时间承诺（包含完整的优先级定义表格）

#### 3. Web界面 - SLA配置页面
**文件**: `frontend/src/pages/SLAConfig.tsx`
**访问路径**: `/sla-config`

**展示内容**:
- 优先级定义表格
- 每个优先级的详细描述
- 响应时间和解决时间标准

#### 4. 数据库配置
**文件**: `database/migrations/001_initial_schema.sql`

**SLA配置表**:
```sql
INSERT INTO sla_config (priority, response_time_minutes, resolution_time_hours, description) VALUES
  ('critical', 15, 4, '生产环境完全中断，业务严重受影响'),
  ('high', 60, 8, '生产环境部分功能受影响'),
  ('medium', 240, 24, '非关键功能问题或性能下降'),
  ('low', 1440, 72, '一般性咨询、功能请求或轻微问题');
```

#### 5. 知识库系统
**文件**: `database/migrations/001_initial_schema.sql`

**知识库表**:
```sql
CREATE TABLE knowledge_base (
  id UUID PRIMARY KEY,
  title VARCHAR(500) NOT NULL,
  content TEXT NOT NULL,
  category VARCHAR(100),
  tags TEXT[],
  ...
);
```

### 📊 证明材料
- [x] SLA政策文档（第2章）
- [x] 客户服务合同（第二条2.1）
- [x] SLA配置界面
- [x] 数据库配置
- [ ] 优先级定义文档截图
- [ ] 知识库截图
- [ ] 运营手册

---

## ✅ SPT-005: AWS Support Case更新

**要求**: 请提供您的方法，以确保AWS合作伙伴运营人员不会错过AWS关于AWS支持案例的更新。

**关键要求**: 演示或描述伙伴运营部门如何对客户AWS support case状态做全流程监控，以及状态更新后如何通知到客户。

### 📍 体现位置

#### 1. 文档中心 - SLA政策文档
**文件**: `docs/SLA-POLICY.md`

**相关章节**:
- ✅ 第3.3章：AWS Support Case管理
  - 所有AWS Support Case自动同步到系统
  - 每10分钟检查AWS Support Case更新
  - AWS回复后立即通知客户
  - 保持本地工单与AWS Case状态同步

#### 2. 文档中心 - 客户服务合同模板
**文件**: `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md`

**相关条款**:
- ✅ 第二条 2.4：AWS集成服务 - AWS Support Case管理
  - 代表客户创建和管理AWS Support Case
  - 实时同步AWS Support Case状态和回复
  - 每10分钟自动检查更新
  - 及时将AWS工程师的回复转达给客户

#### 3. 后端集成实现
**文件**: `backend/src/integrations/AWSSupportIntegration.ts`

**核心功能**:
```typescript
// 1. 同步AWS Support Case状态
async syncSupportCase(caseId: string): Promise<void>

// 2. 同步Case通信记录
async syncCaseCommunications(awsCaseId: string, ticketId: string): Promise<void>

// 3. 创建AWS Case
async createAWSCase(ticketId: string, subject: string, body: string, severity: string): Promise<string>

// 4. 添加通信到AWS Case
async addCommunication(caseId: string, body: string): Promise<void>

// 5. 轮询所有活跃Case（每10分钟）
async pollAllActiveCases(): Promise<void>
```

**实现方式**:
- ✅ 使用AWS Support API (`@aws-sdk/client-support`)
- ✅ 定时轮询活跃Case（每10分钟）
- ✅ 双向同步（本地工单 ↔ AWS Case）
- ✅ 自动添加新通信到工单评论
- ✅ 状态变更自动通知客户

#### 4. 定时任务
**文件**: `backend/src/jobs/index.ts`

**任务配置**:
```typescript
// 每10分钟轮询AWS Support Case更新
schedule.scheduleJob('*/10 * * * *', async () => {
  await awsSupportIntegration.pollAllActiveCases();
});
```

#### 5. 数据库设计
**文件**: `database/migrations/001_initial_schema.sql`

**工单表字段**:
```sql
CREATE TABLE tickets (
  ...
  aws_case_id VARCHAR(100),           -- AWS Case ID
  aws_case_status VARCHAR(50),        -- AWS Case状态
  aws_case_subject VARCHAR(500),      -- AWS Case主题
  aws_case_severity VARCHAR(20),      -- AWS Case严重性
  ...
);

CREATE TABLE ticket_comments (
  ...
  aws_communication_id VARCHAR(100),  -- AWS通信ID
  ...
);
```

#### 6. 通知机制
**文件**: `backend/src/services/NotificationService.ts`

**功能**:
- AWS Case状态变更通知
- 新通信记录通知
- 邮件/短信通知客户

### 📊 证明材料
- [x] SLA政策文档（第3.3章）
- [x] 客户服务合同（第二条2.4）
- [x] AWS Support集成代码
- [x] 定时任务配置
- [x] 数据库设计
- [ ] AWS Support API调用日志
- [ ] 同步流程演示
- [ ] 客户通知邮件示例

**参考链接**: https://aws.amazon.com/blogs/business-intelligence/build-an-analytics-pipeline-for-a-multi-account-support-case-dashboard/

---

## ✅ SPT-006: AWS事件管理

**要求**: 描述监控AWS服务运行状况仪表板并向支持团队发出这些事件警报的流程。

**关键要求**: 
1. 如何开启组织级别health dashboard
2. 当监测到有健康事件发生时，运营团队后续流程

### 📍 体现位置

#### 1. 文档中心 - SLA政策文档
**文件**: `docs/SLA-POLICY.md`

**相关章节**:
- ✅ 第3.4章：AWS Health事件管理
  - 实时监控AWS Health Dashboard
  - 自动识别受影响的客户账户
  - 主动创建事件工单并通知客户
  - 提供事件影响评估和缓解建议

#### 2. 文档中心 - 客户服务合同模板
**文件**: `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md`

**相关条款**:
- ✅ 第二条 2.4：AWS集成服务 - AWS Health事件监控
  - 实时监控AWS Health Dashboard
  - 主动识别影响客户的AWS事件
  - 自动创建事件工单并通知客户
  - 提供事件影响评估和应对建议
  - 跟踪事件解决进度

#### 3. 后端集成实现
**文件**: `backend/src/integrations/AWSHealthIntegration.ts`

**核心功能**:
```typescript
// 1. 配置EventBridge规则（组织级别）
async setupEventBridgeRule(): Promise<void>

// 2. 处理Health事件
async processHealthEvent(eventArn: string): Promise<void>

// 3. 确定事件优先级
private determinePriority(eventCategory?: string): TicketPriority

// 4. 格式化事件描述
private formatHealthEventDescription(event: any, entities: any[]): string

// 5. 通知受影响客户
private async notifyAffectedCustomers(entities: any[], ticket: any): Promise<void>

// 6. 轮询Health事件（每15分钟）
async pollHealthEvents(): Promise<void>
```

**实现方式**:
- ✅ 使用AWS Health API (`@aws-sdk/client-health`)
- ✅ 组织级Health API集成（us-east-1区域）
- ✅ EventBridge规则自动转发Health事件
- ✅ 自动创建事件工单
- ✅ 识别受影响的客户账户（通过aws_account_id）
- ✅ 批量通知受影响客户
- ✅ 定时轮询Health事件（每15分钟）
- ✅ 事件优先级自动判定

#### 4. EventBridge配置
**实现**: `AWSHealthIntegration.setupEventBridgeRule()`

**规则配置**:
```json
{
  "source": ["aws.health"],
  "detail-type": ["AWS Health Event"]
}
```

**目标**: SQS队列接收事件通知

#### 5. 定时任务
**文件**: `backend/src/jobs/index.ts`

**任务配置**:
```typescript
// 每15分钟轮询AWS Health事件
schedule.scheduleJob('*/15 * * * *', async () => {
  await awsHealthIntegration.pollHealthEvents();
});
```

#### 6. 数据库设计
**文件**: `database/migrations/001_initial_schema.sql`

**工单表字段**:
```sql
CREATE TABLE tickets (
  ...
  aws_health_event_arn VARCHAR(255),  -- AWS Health事件ARN
  source VARCHAR(20) NOT NULL,        -- 包含'aws_health'来源
  ...
);

CREATE TABLE customers (
  ...
  aws_account_id VARCHAR(12),         -- 用于匹配受影响账户
  ...
);
```

#### 7. 组织级Health Dashboard开启
**说明**: 
- 需要在AWS Organizations中启用Health API
- 使用组织管理账户的凭证
- Health API只在us-east-1区域可用
- 可以查看所有成员账户的Health事件

**代码体现**:
```typescript
this.healthClient = new HealthClient({ region: 'us-east-1' }); // Health API只在us-east-1
```

#### 8. 运营团队后续流程
**流程**:
1. EventBridge接收Health事件 → 发送到SQS队列
2. 系统处理SQS消息 → 调用`processHealthEvent()`
3. 获取事件详情和受影响资源
4. 自动创建工单（优先级根据事件类型判定）
5. 识别受影响的客户账户
6. 批量通知受影响客户
7. 工程师跟进处理
8. 持续监控事件状态直到解决

### 📊 证明材料
- [x] SLA政策文档（第3.4章）
- [x] 客户服务合同（第二条2.4）
- [x] AWS Health集成代码
- [x] EventBridge规则配置代码
- [x] 定时任务配置
- [x] 数据库设计
- [ ] EventBridge规则截图
- [ ] Health事件处理流程图
- [ ] 客户通知邮件示例
- [ ] 组织级Health Dashboard截图

**参考链接**: https://aws.amazon.com/cn/premiumsupport/technology/aws-health-dashboard/

---

## 📋 总结：所有SPT要求的体现位置

| SPT要求 | 文档中心 | Web界面 | 后端代码 | 数据库 | 状态 |
|---------|---------|---------|---------|--------|------|
| **SPT-001** 基础SLA | ✅ SLA-POLICY.md<br>✅ CONTRACT-TEMPLATE.md | ✅ SLAConfig.tsx | ✅ SLAService.ts<br>✅ sla.ts | ✅ sla_config表 | ✅ 完成 |
| **SPT-002** 服务可用性 | ✅ SLA-POLICY.md 第5章<br>✅ CONTRACT-TEMPLATE.md 2.2 | ✅ 多渠道接入 | ✅ NotificationService.ts | ✅ on_call_schedule表 | ✅ 完成 |
| **SPT-003** 工单创建 | ✅ SLA-POLICY.md 第3.1章<br>✅ CONTRACT-TEMPLATE.md 2.3 | ✅ CreateTicket.tsx | ✅ TicketService.ts<br>✅ tickets.ts | ✅ tickets表 | ✅ 完成 |
| **SPT-004** 服务台运营 | ✅ SLA-POLICY.md 第2章<br>✅ CONTRACT-TEMPLATE.md 2.1 | ✅ SLAConfig.tsx | ✅ 优先级定义 | ✅ sla_config表<br>✅ knowledge_base表 | ✅ 完成 |
| **SPT-005** Support Case | ✅ SLA-POLICY.md 第3.3章<br>✅ CONTRACT-TEMPLATE.md 2.4 | ✅ 工单详情页 | ✅ AWSSupportIntegration.ts | ✅ aws_case_id字段 | ✅ 完成 |
| **SPT-006** 事件管理 | ✅ SLA-POLICY.md 第3.4章<br>✅ CONTRACT-TEMPLATE.md 2.4 | ✅ 事件工单列表 | ✅ AWSHealthIntegration.ts | ✅ aws_health_event_arn字段 | ✅ 完成 |

---

## 🎯 审计准备建议

### 立即可用的材料
1. ✅ 所有文档中心文件（docs/目录）
2. ✅ 所有代码实现（backend/和frontend/目录）
3. ✅ 数据库设计（database/migrations/）

### 需要系统运行后准备的材料
1. 📸 各个页面的截图
2. 📊 SLA达成率报告
3. 📧 通知邮件示例
4. 📝 实际工单处理案例
5. 📈 AWS Support Case同步日志
6. 🔔 AWS Health事件处理记录

### 审计演示流程建议
1. 展示文档中心的政策文档
2. 展示客户服务合同模板
3. 启动系统并演示Web界面
4. 展示数据库配置
5. 展示代码实现
6. 如有实际数据，展示运行报告

---

**文档版本**: 1.0  
**最后更新**: 2025-12-25  
**维护人**: 合规团队
