# Java + Vue CRM 客户管理系统

这是一个前后端分离的 CRM 示例项目：

- 后端：Spring Boot 3、Spring Data JPA、H2 内存数据库
- 前端：Vue 3、Vite、lucide-vue-next
- 功能：客户列表、搜索筛选、客户详情、新增编辑删除、联系人管理、跟进记录、客户看板统计

## 目录结构

```text
.
├── backend   # Java Spring Boot REST API
└── frontend  # Vue 3 管理后台
```

## 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认运行在：

```text
http://localhost:8080
```

H2 控制台：

```text
http://localhost:8080/h2-console
```

连接参数：

```text
JDBC URL: jdbc:h2:mem:crm
User Name: sa
Password:
```

## 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在：

```text
http://localhost:5173
```

Vite 已经把 `/api` 代理到 `http://localhost:8080`。

## 常用接口

```text
GET    /api/dashboard
GET    /api/customers?keyword=&stage=
GET    /api/customers/{id}
POST   /api/customers
PUT    /api/customers/{id}
DELETE /api/customers/{id}
POST   /api/customers/{id}/follow-ups
```

## 后续可扩展

- 把 H2 换成 MySQL 或 PostgreSQL
- 增加登录、角色权限和操作日志
- 增加销售漏斗、客户分配、导入导出
- 增加分页、排序和更细的查询条件
