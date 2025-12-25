# SPT-001 审计指南

## 审计要求

**SPT-001: AWS 身份和访问管理最佳实践 - 基础 SLA**

> 描述您的基本 SLA。基础 SLA 是与 AWS 合作伙伴向其客户的响应时间、操作和通知相关的 SLA。SLA 可能包括客户打开票证/发起请求时的响应时间、从事件或事件触发到补救的时间以及客户发起的更改/请求的周转时间。
>
> **关键要求**: 存储在文档中心中的文档/客户服务合同中包括计费支持的服务级别协议（SLA）方面的政策的演示或描述。

## 如何向审计人员展示合规性

### 第一步：展示文档中心的SLA政策文档

**位置**: `docs/SLA-POLICY.md`

**展示内容**:
1. 打开文档，向审计人员展示完整的SLA政策
2. 重点指出以下章节：
   - ✅ **第2章 响应时间SLA**: 四级优先级的详细响应时间表格
   - ✅ **第3章 操作标准**: 工单处理流程、升级机制、AWS集成
   - ✅ **第4章 通知要求**: 客户和内部通知机制
   - ✅ **第5章 服务可用性**: 24×7支持承诺
   - ✅ **第6章 SLA报告和监控**: 实时监控和定期报告

**关键说明点**:
- 这是正式的政策文档，存储在文档中心
- 包含完整的响应时间、操作和通知标准
- 定期审查和更新

### 第二步：展示客户服务合同模板

**位置**: `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md`

**展示内容**:
1. 打开合同模板
2. 重点展示以下条款：
   - ✅ **第二条 服务级别协议（SLA）**: 
     - 2.1 响应时间承诺表格
     - 2.2 服务可用性（24×7、99.9%）
     - 2.3 操作标准
     - 2.4 AWS集成服务
     - 2.5 通知要求
   - ✅ **第三条 SLA报告和监控**: 实时监控和定期报告条款
   - ✅ **第四条 SLA违规和补偿**: 补偿机制

**关键说明点**:
- 这是与客户签订的正式合同模板
- SLA条款具有法律约束力
- 包含明确的补偿机制

### 第三步：展示系统实现

#### 3.1 Web界面展示

**访问**: 启动系统后访问 `/sla-config` 页面

**展示内容**:
1. SLA配置管理界面
2. 响应时间标准表格
3. 近30天SLA达成率统计
4. 操作标准说明
5. 服务可用性承诺展示

**截图建议**:
- 📸 SLA配置表格截图
- 📸 SLA达成率统计截图
- 📸 文档链接展示截图

#### 3.2 数据库配置

**展示**: `database/migrations/001_initial_schema.sql`

```sql
-- SLA配置表 (SPT-001: 基础SLA)
CREATE TABLE sla_config (
  priority VARCHAR(20) PRIMARY KEY,
  response_time_minutes INTEGER NOT NULL,
  resolution_time_hours INTEGER NOT NULL,
  description TEXT,
  ...
);

-- 默认SLA配置
INSERT INTO sla_config (priority, response_time_minutes, resolution_time_hours, description) VALUES
  ('critical', 15, 4, '生产环境完全中断，业务严重受影响'),
  ('high', 60, 8, '生产环境部分功能受影响'),
  ('medium', 240, 24, '非关键功能问题或性能下降'),
  ('low', 1440, 72, '一般性咨询、功能请求或轻微问题');
```

**关键说明点**:
- SLA配置存储在数据库中
- 可以动态调整
- 系统自动计算SLA截止时间

#### 3.3 代码实现

**展示**: `backend/src/services/SLAService.ts`

**关键功能**:
- ✅ `calculateSLADueDate()`: 自动计算SLA截止时间
- ✅ `getSLAMetrics()`: 生成SLA达成率报告
- ✅ `updateSLAConfig()`: 更新SLA配置

**展示**: `backend/src/services/TicketService.ts`

**关键功能**:
- ✅ 创建工单时自动设置SLA截止时间
- ✅ `checkSLAViolations()`: 检测SLA违规并告警

### 第四步：展示API接口

**SLA相关API**:

```bash
# 获取SLA配置
GET /api/sla/config

# 获取SLA指标
GET /api/sla/metrics?days=30

# 获取SLA违规工单
GET /api/sla/violations

# 生成SLA报告
GET /api/sla/report?start_date=2025-01-01&end_date=2025-01-31
```

**演示建议**:
- 使用Postman或curl调用API
- 展示返回的SLA统计数据
- 说明这些API用于实时监控和报告

### 第五步：展示实际运行数据（如果有）

如果系统已经运行并有实际数据：

1. **SLA达成率报告**
   - 展示近30天的SLA达成率
   - 按优先级分类的统计数据
   - 平均响应时间和解决时间

2. **工单示例**
   - 展示几个已解决的工单
   - 指出SLA截止时间和实际解决时间
   - 说明SLA达标情况

3. **通知邮件示例**
   - 工单创建确认邮件
   - 状态更新通知邮件
   - SLA预警邮件

## 审计检查清单

向审计人员提供以下材料：

- [ ] ✅ SLA政策文档 (`docs/SLA-POLICY.md`)
- [ ] ✅ 客户服务合同模板 (`docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md`)
- [ ] ✅ SLA配置界面截图
- [ ] ✅ 数据库schema展示 (`database/migrations/001_initial_schema.sql`)
- [ ] ✅ SLA服务代码 (`backend/src/services/SLAService.ts`)
- [ ] ✅ API文档和示例调用
- [ ] 📊 SLA达成率报告（如有实际数据）
- [ ] 📧 通知邮件示例（如有实际数据）
- [ ] 📸 系统运行截图

## 常见审计问题及回答

### Q1: SLA政策存储在哪里？
**A**: 存储在文档中心的 `docs/SLA-POLICY.md` 和 `docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md` 文件中。这些是正式的政策文档，定期审查和更新。

### Q2: 如何确保遵守SLA承诺？
**A**: 
1. 系统自动计算每个工单的SLA截止时间
2. 实时监控SLA合规情况
3. 接近截止时间自动告警和升级
4. 定期生成SLA报告并审查
5. SLA违规自动通知管理层

### Q3: 响应时间标准是什么？
**A**: 
- Critical: ≤15分钟首次响应，≤4小时解决
- High: ≤1小时首次响应，≤8小时解决
- Medium: ≤4小时首次响应，≤24小时解决
- Low: ≤24小时首次响应，≤72小时解决

### Q4: 如何通知客户？
**A**: 
- 工单创建立即发送确认邮件
- 每次状态变更通知客户
- 接近SLA截止时间前30分钟提醒
- 问题解决后通知并请求确认
- AWS Support Case更新实时同步
- AWS Health事件主动通知

### Q5: 服务可用性如何保证？
**A**: 
- 24×7全天候支持
- 轮班制确保随时有工程师响应
- 系统目标99.9%正常运行时间
- 多渠道接入（Web、邮件、API）

### Q6: 如何处理SLA违规？
**A**: 
- 自动检测SLA违规
- 立即通知相关人员
- 分析根本原因
- 提供服务费用补偿（根据合同条款）
- 持续改进流程

### Q7: 客户如何查看SLA信息？
**A**: 
- 客户门户显示SLA配置
- 每个工单显示SLA截止时间
- 定期发送SLA报告
- 实时仪表板展示SLA状态

## 合规状态

✅ **SPT-001 已满足**

我们的系统完全满足SPT-001的所有要求：
1. ✅ 文档中心存储完整的SLA政策文档
2. ✅ 客户服务合同包含详细的SLA条款
3. ✅ 系统实现支持SLA自动管理
4. ✅ 实时监控和报告机制
5. ✅ 明确的响应时间、操作和通知标准

## 联系方式

如有审计相关问题，请联系：
- 合规负责人: compliance@example.com
- 技术负责人: tech@example.com
