# 简化启动指南（无需Docker）

由于Docker镜像下载较慢，这里提供一个简化的本地开发方案。

## 方案一：仅启动数据库服务（推荐）

如果你已经有PostgreSQL和Redis，只需启动它们：

```bash
# 等待Docker镜像下载完成（可能需要5-10分钟）
# 或者手动安装PostgreSQL和Redis
```

## 方案二：使用SQLite替代PostgreSQL（最快）

修改后端代码使用SQLite，无需安装数据库：

### 1. 安装SQLite依赖

```bash
cd backend
npm install sqlite3
```

### 2. 修改数据库连接

创建 `backend/src/db.ts`:

```typescript
import sqlite3 from 'sqlite3';
import { open } from 'sqlite';

export async function getDb() {
  return open({
    filename: './spt.db',
    driver: sqlite3.Database
  });
}
```

### 3. 简化的数据库初始化

```bash
cd backend
node -e "require('sqlite3').Database('./spt.db')"
```

## 方案三：演示模式（无数据库）

直接运行前端，使用模拟数据：

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173 查看界面。

## 当前Docker状态

Docker正在下载镜像，请耐心等待。你可以在另一个终端运行：

```bash
docker-compose ps
```

查看容器状态。当看到 "Up" 状态时，表示启动成功。

## 推荐做法

1. **让Docker继续在后台下载**（已经在进行中）
2. **先查看代码和文档**，了解系统架构
3. **等待5-10分钟后**，再次运行 `docker ps` 检查状态
4. **容器启动后**，运行数据库初始化脚本

## 检查Docker进度

```bash
# 查看正在下载的镜像
docker images

# 查看容器状态
docker-compose ps
```

## 下一步

Docker下载完成后，继续执行：

```bash
# 初始化数据库
docker-compose exec postgres psql -U spt_user -d spt_system -f /docker-entrypoint-initdb.d/001_initial_schema.sql

# 安装依赖
cd backend && npm install
cd ../frontend && npm install

# 启动开发服务器
npm run dev
```
