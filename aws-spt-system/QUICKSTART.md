# 快速启动指南

## 前置条件

- Docker和Docker Compose
- AWS账户（配置IAM权限）
- Node.js 18+（本地开发）

## 使用Docker快速启动

### 1. 配置环境变量

```bash
cp .env.example .env
# 编辑.env文件，填入AWS凭证和其他配置
```

### 2. 启动所有服务

```bash
docker-compose up -d
```

这将启动：
- PostgreSQL数据库（端口5432）
- Redis缓存（端口6379）
- 后端API服务（端口3000）

### 3. 初始化数据库

```bash
docker-compose exec backend sh -c "psql -h postgres -U spt_user -d spt_system -f /app/database/migrations/001_initial_schema.sql"
```

### 4. 访问系统

- API: http://localhost:3000
- 健康检查: http://localhost:3000/api/health

## 本地开发

### 1. 安装依赖

```bash
# 根目录
npm install

# 后端
cd backend && npm install

# 前端
cd frontend && npm install
```

### 2. 启动数据库

```bash
docker-compose up -d postgres redis
```

### 3. 运行数据库迁移

```bash
npm run db:migrate
```

### 4. 启动开发服务器

```bash
# 同时启动前后端
npm run dev

# 或分别启动
npm run dev:backend  # 后端: http://localhost:3000
npm run dev:frontend # 前端: http://localhost:5173
```

## AWS配置

### 1. 创建IAM角色

使用以下策略创建IAM角色：

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "support:*",
        "health:Describe*",
        "events:PutRule",
        "events:PutTargets",
        "ses:SendEmail",
        "sqs:*"
      ],
      "Resource": "*"
    }
  ]
}
```

### 2. 配置EventBridge（SPT-006）

启动系统后，调用API配置EventBridge规则：

```bash
curl -X POST http://localhost:3000/api/aws/health/setup-eventbridge
```

### 3. 验证SES邮箱

```bash
aws ses verify-email-identity --email-address support@yourcompany.com
```

## 测试系统

### 创建测试工单

```bash
curl -X POST http://localhost:3000/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customer_id": "test-customer",
    "title": "测试工单",
    "description": "这是一个测试工单",
    "priority": "high"
  }'
```

### 查看SLA指标

```bash
curl "http://localhost:3000/api/sla/metrics?startDate=2024-01-01&endDate=2024-12-31"
```

## 常见问题

### 数据库连接失败

检查PostgreSQL是否运行：
```bash
docker-compose ps postgres
```

### AWS权限错误

确保IAM角色有正确的权限，并且环境变量配置正确。

### 邮件发送失败

确保SES邮箱已验证，并且在正确的区域。

## 下一步

- 阅读 [部署文档](./docs/deployment.md)
- 查看 [SPT合规性说明](./docs/SPT-COMPLIANCE.md)
- 配置监控和告警
- 设置备份策略
