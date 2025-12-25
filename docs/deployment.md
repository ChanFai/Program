# 部署指南

## AWS基础设施配置

### 1. IAM角色和权限

创建IAM角色用于系统访问AWS服务：

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

### 2. SQS队列配置

创建两个SQS队列：

**AWS Health事件队列**:
```bash
aws sqs create-queue --queue-name aws-health-events
```

**AWS Support更新队列**:
```bash
aws sqs create-queue --queue-name aws-support-updates
```

### 3. EventBridge规则

系统会自动配置EventBridge规则，或手动创建：

```bash
aws events put-rule \
  --name aws-health-to-spt \
  --event-pattern '{"source":["aws.health"]}'
```

### 4. SES配置

验证发件人邮箱：

```bash
aws ses verify-email-identity --email-address support@yourcompany.com
```

## 数据库部署

### PostgreSQL安装

```bash
# Ubuntu/Debian
sudo apt-get install postgresql-14

# 创建数据库
sudo -u postgres createdb spt_system
sudo -u postgres createuser spt_user

# 运行迁移
npm run db:migrate
```

### Redis安装

```bash
# Ubuntu/Debian
sudo apt-get install redis-server
sudo systemctl start redis
```

## 应用部署

### 使用Docker

```bash
# 构建镜像
docker build -t spt-system .

# 运行容器
docker run -d \
  --name spt-backend \
  -p 3000:3000 \
  --env-file .env \
  spt-system
```

### 使用PM2

```bash
# 安装PM2
npm install -g pm2

# 启动应用
pm2 start ecosystem.config.js

# 查看日志
pm2 logs
```

## 监控和告警

### CloudWatch配置

```bash
# 创建日志组
aws logs create-log-group --log-group-name /aws/spt-system

# 配置指标过滤器
aws logs put-metric-filter \
  --log-group-name /aws/spt-system \
  --filter-name SLAViolations \
  --filter-pattern "[time, level=ERROR, msg=*SLA*]" \
  --metric-transformations \
    metricName=SLAViolationCount,metricNamespace=SPT,metricValue=1
```

## 备份策略

### 数据库备份

```bash
# 每日备份脚本
pg_dump spt_system | gzip > backup_$(date +%Y%m%d).sql.gz

# 上传到S3
aws s3 cp backup_$(date +%Y%m%d).sql.gz s3://your-backup-bucket/
```

## 安全配置

1. 启用VPC内部通信
2. 配置安全组限制访问
3. 启用RDS加密
4. 使用Secrets Manager存储敏感信息
5. 启用CloudTrail审计

## 性能优化

1. 配置RDS读副本
2. 启用ElastiCache for Redis
3. 使用CloudFront CDN
4. 配置Auto Scaling

## 故障排查

查看日志：
```bash
# 应用日志
tail -f logs/combined.log

# AWS集成日志
aws logs tail /aws/spt-system --follow
```

检查健康状态：
```bash
curl http://localhost:3000/api/health
```
