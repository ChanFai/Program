# SPT流程图说明

本文档用文字描述SPT-005和SPT-006的详细流程，方便理解和向审计人员展示。

---

## SPT-005: AWS Support Case全流程监控

### 流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                    AWS Support Case 监控流程                      │
└─────────────────────────────────────────────────────────────────┘

1. 客户创建工单
   │
   ├─→ [Web门户] → TicketService.createTicket()
   ├─→ [邮件] → SES → 自动创建工单
   └─→ [API] → POST /api/tickets
   │
   ↓
2. 需要AWS支持？
   │
   ├─ 是 → AWSSupportIntegration.createAWSCase()
   │       │
   │       ├─→ 调用AWS Support API
   │       ├─→ 创建AWS Support Case
   │       └─→ 保存 aws_case_id 到工单
   │
   └─ 否 → 内部处理
   │
   ↓
3. 定时同步（每10分钟）
   │
   └─→ AWSSupportIntegration.pollAllActiveCases()
       │
       ├─→ 查询所有活跃工单的 aws_case_id
       │
       └─→ 对每个Case执行：
           │
           ├─→ syncSupportCase(caseId)
           │   ├─→ 调用 DescribeCasesCommand
           │   ├─→ 获取Case状态、主题、严重性
           │   └─→ 更新本地工单状态
           │
           └─→ syncCaseCommunications(caseId, ticketId)
               ├─→ 调用 DescribeCasesCommand (includeCommunications: true)
               ├─→ 获取所有通信记录
               ├─→ 检查是否已存在（通过 aws_communication_id）
               └─→ 添加新通信到 ticket_comments 表
   │
   ↓
4. 状态变更通知
   │
   └─→ NotificationService.notifyTicketUpdated()
       │
       ├─→ 发送邮件给客户
       ├─→ 邮件内容包含AWS工程师的回复
       └─→ 提供工单链接
   │
   ↓
5. 双向通信
   │
   ├─→ 客户在本地工单添加评论
   │   └─→ AWSSupportIntegration.addCommunication()
   │       └─→ 同步到AWS Support Case
   │
   └─→ AWS工程师在Case中回复
       └─→ 定时同步自动获取
           └─→ 添加到本地工单评论
           └─→ 通知客户
   │
   ↓
6. Case解决
   │
   └─→ AWS Case状态变为 resolved
       └─→ 同步更新本地工单状态
           └─→ 通知客户
           └─→ 请求客户确认
```

### 关键代码位置

**文件**: `backend/src/integrations/AWSSupportIntegration.ts`

```typescript
// 1. 创建AWS Case
async createAWSCase(ticketId, subject, body, severity): Promise<string>

// 2. 同步Case状态
async syncSupportCase(caseId): Promise<void>

// 3. 同步通信记录
async syncCaseCommunications(awsCaseId, ticketId): Promise<void>

// 4. 添加通信到AWS Case
async addCommunication(caseId, body): Promise<void>

// 5. 轮询所有活跃Case（每10分钟）
async pollAllActiveCases(): Promise<void>
```

**定时任务**: `backend/src/jobs/index.ts`

```typescript
// 每10分钟执行一次
schedule.scheduleJob('*/10 * * * *', async () => {
  await awsSupportIntegration.pollAllActiveCases();
});
```

### 数据库设计

**tickets表字段**:
```sql
aws_case_id VARCHAR(100),        -- AWS Support Case ID
aws_case_status VARCHAR(50),     -- AWS Case状态
aws_case_subject VARCHAR(500),   -- AWS Case主题
aws_case_severity VARCHAR(20),   -- AWS Case严重性
```

**ticket_comments表字段**:
```sql
aws_communication_id VARCHAR(100),  -- AWS通信记录ID（用于去重）
```

### 如何确保不错过更新？

1. ✅ **定时轮询**: 每10分钟自动检查所有活跃Case
2. ✅ **去重机制**: 通过 `aws_communication_id` 避免重复添加
3. ✅ **自动通知**: 发现新通信立即通知客户
4. ✅ **双向同步**: 本地评论也会同步到AWS Case
5. ✅ **状态跟踪**: 实时更新Case状态到本地工单

---

## SPT-006: AWS Health事件管理流程

### 流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                    AWS Health 事件监控流程                        │
└─────────────────────────────────────────────────────────────────┘

1. 组织级Health Dashboard开启
   │
   └─→ AWS Organizations启用Health API
       ├─→ 使用组织管理账户凭证
       ├─→ Health API region: us-east-1（固定）
       └─→ 可查看所有成员账户的Health事件
   │
   ↓
2. EventBridge规则配置（实时监控）
   │
   └─→ AWSHealthIntegration.setupEventBridgeRule()
       │
       ├─→ 创建EventBridge规则
       │   ├─ 规则名称: aws-health-events-to-spt
       │   ├─ 事件源: aws.health
       │   └─ 事件类型: AWS Health Event
       │
       └─→ 配置目标
           └─→ SQS队列接收事件通知
   │
   ↓
3. 事件触发（两种方式）
   │
   ├─→ 方式1: EventBridge实时推送
   │   │
   │   └─→ AWS Health事件发生
   │       └─→ EventBridge自动触发
   │           └─→ 发送到SQS队列
   │               └─→ 系统消费SQS消息
   │                   └─→ 调用 processHealthEvent(eventArn)
   │
   └─→ 方式2: 定时轮询（备份机制）
       │
       └─→ 每15分钟执行
           └─→ AWSHealthIntegration.pollHealthEvents()
               └─→ 调用 DescribeEventsCommand
                   └─→ 获取所有 open/upcoming 事件
                       └─→ 对每个事件调用 processHealthEvent()
   │
   ↓
4. 处理Health事件
   │
   └─→ processHealthEvent(eventArn)
       │
       ├─→ 步骤1: 获取事件详情
       │   └─→ DescribeEventDetailsCommand
       │       ├─→ 事件类型（issue/accountNotification/scheduledChange）
       │       ├─→ 服务名称（EC2/RDS/S3等）
       │       ├─→ 区域
       │       ├─→ 状态
       │       └─→ 描述
       │
       ├─→ 步骤2: 检查是否已创建工单
       │   └─→ 查询 tickets 表的 aws_health_event_arn
       │       ├─ 已存在 → 跳过
       │       └─ 不存在 → 继续处理
       │
       ├─→ 步骤3: 获取受影响的资源
       │   └─→ DescribeAffectedEntitiesCommand
       │       ├─→ 受影响的AWS账户ID列表
       │       ├─→ 受影响的资源ARN列表
       │       └─→ 资源数量统计
       │
       ├─→ 步骤4: 确定优先级
       │   └─→ determinePriority(eventCategory)
       │       ├─ issue → HIGH
       │       ├─ accountNotification → MEDIUM
       │       ├─ scheduledChange → LOW
       │       └─ 其他 → MEDIUM
       │
       ├─→ 步骤5: 创建工单
       │   └─→ TicketService.createTicket()
       │       ├─→ 标题: "AWS Health Event: [服务] - [事件类型]"
       │       ├─→ 描述: 格式化的事件详情
       │       ├─→ 优先级: 根据事件类型判定
       │       ├─→ 来源: aws_health
       │       └─→ aws_health_event_arn: 事件ARN
       │
       └─→ 步骤6: 通知受影响客户
           └─→ notifyAffectedCustomers(entities, ticket)
               │
               ├─→ 提取所有受影响的AWS账户ID
               │
               ├─→ 查询 customers 表匹配 aws_account_id
               │
               └─→ 对每个客户发送通知
                   ├─→ 邮件标题: "AWS服务事件通知"
                   ├─→ 邮件内容:
                   │   ├─ 事件描述
                   │   ├─ 受影响的资源
                   │   ├─ 影响评估
                   │   ├─ 建议措施
                   │   └─ 工单链接
                   └─→ 记录通知日志
   │
   ↓
5. 运营团队后续流程
   │
   ├─→ 工程师收到工单通知
   │   └─→ 查看工单详情
   │       └─→ 了解事件影响范围
   │
   ├─→ 评估影响
   │   ├─→ 检查受影响的客户资源
   │   ├─→ 评估业务影响程度
   │   └─→ 制定应对方案
   │
   ├─→ 主动联系客户
   │   ├─→ 电话/邮件通知
   │   ├─→ 说明事件情况
   │   ├─→ 提供缓解建议
   │   └─→ 协助客户应对
   │
   ├─→ 持续跟进
   │   ├─→ 监控事件状态变化
   │   ├─→ 更新工单进展
   │   └─→ 定期向客户汇报
   │
   └─→ 事件解决
       ├─→ AWS Health事件状态变为 closed
       ├─→ 确认客户资源恢复正常
       ├─→ 更新工单状态为 resolved
       └─→ 请求客户确认并关闭工单
   │
   ↓
6. 事后总结
   │
   └─→ 记录到知识库
       ├─→ 事件原因分析
       ├─→ 影响范围总结
       ├─→ 应对措施记录
       └─→ 改进建议
```

### 关键代码位置

**文件**: `backend/src/integrations/AWSHealthIntegration.ts`

```typescript
// 1. 配置EventBridge规则
async setupEventBridgeRule(): Promise<void>

// 2. 处理Health事件（核心）
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

**Health Client初始化（组织级）**:
```typescript
// 注意：Health API只在us-east-1区域可用
this.healthClient = new HealthClient({ region: 'us-east-1' });
```

**定时任务**: `backend/src/jobs/index.ts`

```typescript
// 每15分钟执行一次
schedule.scheduleJob('*/15 * * * *', async () => {
  await awsHealthIntegration.pollHealthEvents();
});
```

### EventBridge规则配置

**规则名称**: `aws-health-events-to-spt`

**事件模式**:
```json
{
  "source": ["aws.health"],
  "detail-type": ["AWS Health Event"]
}
```

**目标**: SQS队列（环境变量 `AWS_HEALTH_QUEUE_URL`）

### 数据库设计

**tickets表字段**:
```sql
aws_health_event_arn VARCHAR(255),  -- AWS Health事件ARN
source VARCHAR(20),                 -- 包含 'aws_health' 来源
```

**customers表字段**:
```sql
aws_account_id VARCHAR(12),  -- 用于匹配受影响的AWS账户
```

### 如何开启组织级Health Dashboard？

1. ✅ **AWS Organizations设置**:
   - 在组织管理账户中启用Health API
   - 授予必要的IAM权限

2. ✅ **IAM权限配置**:
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Effect": "Allow",
         "Action": [
           "health:DescribeEvents",
           "health:DescribeEventDetails",
           "health:DescribeAffectedEntities"
         ],
         "Resource": "*"
       }
     ]
   }
   ```

3. ✅ **代码实现**:
   ```typescript
   // 使用us-east-1区域（Health API固定区域）
   this.healthClient = new HealthClient({ region: 'us-east-1' });
   ```

4. ✅ **EventBridge集成**:
   - 创建规则监听Health事件
   - 配置SQS队列作为目标
   - 实现实时事件推送

### 运营团队后续流程详解

#### 阶段1: 事件接收（自动）
- EventBridge实时推送或定时轮询发现事件
- 系统自动创建工单
- 自动判定优先级
- 自动识别受影响客户

#### 阶段2: 初步评估（人工）
- 工程师查看工单详情
- 了解事件类型和影响范围
- 评估业务影响程度
- 制定初步应对方案

#### 阶段3: 客户通知（自动+人工）
- 系统自动发送邮件通知
- 工程师主动电话联系重要客户
- 说明事件情况和影响
- 提供缓解建议和应对措施

#### 阶段4: 持续跟进（人工）
- 监控事件状态变化
- 定期更新工单进展
- 向客户汇报最新情况
- 协助客户应对和恢复

#### 阶段5: 事件解决（人工）
- 确认AWS事件已解决
- 验证客户资源恢复正常
- 更新工单状态
- 请求客户确认

#### 阶段6: 事后总结（人工）
- 分析事件原因
- 总结影响范围
- 记录应对措施
- 提出改进建议
- 更新知识库

---

## 双重保障机制

### SPT-005: AWS Support Case

1. **主要机制**: 定时轮询（每10分钟）
2. **备用机制**: SQS队列接收AWS Support邮件通知
3. **去重机制**: `aws_communication_id` 避免重复

### SPT-006: AWS Health事件

1. **主要机制**: EventBridge实时推送
2. **备用机制**: 定时轮询（每15分钟）
3. **去重机制**: `aws_health_event_arn` 避免重复创建工单

---

## 审计演示建议

### 演示SPT-005

1. 展示 `AWSSupportIntegration.ts` 代码
2. 展示定时任务配置
3. 展示数据库中的 `aws_case_id` 字段
4. 如有实际数据，展示同步日志
5. 展示客户收到的通知邮件

### 演示SPT-006

1. 展示 `AWSHealthIntegration.ts` 代码
2. 展示 EventBridge 规则配置
3. 展示组织级Health API调用（us-east-1）
4. 展示定时任务配置
5. 展示运营团队处理流程文档
6. 如有实际数据，展示Health事件工单

---

**参考链接**:
- SPT-005: https://aws.amazon.com/blogs/business-intelligence/build-an-analytics-pipeline-for-a-multi-account-support-case-dashboard/
- SPT-006: https://aws.amazon.com/cn/premiumsupport/technology/aws-health-dashboard/
