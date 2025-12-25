// 简化版服务器 - 用于快速演示
const express = require('express');
const cors = require('cors');
const { Pool } = require('pg');

const app = express();
const PORT = process.env.PORT || 3000;

// 数据库连接
const pool = new Pool({
  host: process.env.DB_HOST || 'localhost',
  port: parseInt(process.env.DB_PORT || '5432'),
  database: process.env.DB_NAME || 'spt_system',
  user: process.env.DB_USER || 'spt_user',
  password: process.env.DB_PASSWORD || 'spt_password'
});

app.use(cors());
app.use(express.json());

// 健康检查
app.get('/api/health', (req, res) => {
  res.json({
    status: 'healthy',
    timestamp: new Date().toISOString(),
    database: 'connected'
  });
});

// 获取工单列表
app.get('/api/tickets', async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM tickets ORDER BY created_at DESC LIMIT 50');
    res.json(result.rows);
  } catch (error) {
    console.error('Error fetching tickets:', error);
    res.status(500).json({ error: 'Failed to fetch tickets' });
  }
});

// 创建工单
app.post('/api/tickets', async (req, res) => {
  try {
    const { customer_id, title, description, priority } = req.body;
    
    // 计算SLA截止时间
    const slaMinutes = {
      'critical': 15,
      'high': 60,
      'medium': 240,
      'low': 1440
    }[priority] || 240;
    
    const slaDueAt = new Date();
    slaDueAt.setMinutes(slaDueAt.getMinutes() + slaMinutes);
    
    const query = `
      INSERT INTO tickets (customer_id, title, description, priority, status, source, sla_due_at)
      VALUES ($1, $2, $3, $4, 'new', 'web', $5)
      RETURNING *
    `;
    
    const result = await pool.query(query, [
      customer_id || 'demo-customer',
      title,
      description,
      priority,
      slaDueAt
    ]);
    
    res.status(201).json(result.rows[0]);
  } catch (error) {
    console.error('Error creating ticket:', error);
    res.status(500).json({ error: 'Failed to create ticket' });
  }
});

// 获取单个工单
app.get('/api/tickets/:id', async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM tickets WHERE id = $1', [req.params.id]);
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Ticket not found' });
    }
    res.json(result.rows[0]);
  } catch (error) {
    console.error('Error fetching ticket:', error);
    res.status(500).json({ error: 'Failed to fetch ticket' });
  }
});

// 更新工单
app.patch('/api/tickets/:id', async (req, res) => {
  try {
    const { status, priority, assigned_to } = req.body;
    const updates = [];
    const values = [];
    let paramIndex = 1;
    
    if (status) {
      updates.push(`status = $${paramIndex}`);
      values.push(status);
      paramIndex++;
    }
    if (priority) {
      updates.push(`priority = $${paramIndex}`);
      values.push(priority);
      paramIndex++;
    }
    if (assigned_to) {
      updates.push(`assigned_to = $${paramIndex}`);
      values.push(assigned_to);
      paramIndex++;
    }
    
    updates.push(`updated_at = NOW()`);
    values.push(req.params.id);
    
    const query = `
      UPDATE tickets 
      SET ${updates.join(', ')}
      WHERE id = $${paramIndex}
      RETURNING *
    `;
    
    const result = await pool.query(query, values);
    res.json(result.rows[0]);
  } catch (error) {
    console.error('Error updating ticket:', error);
    res.status(500).json({ error: 'Failed to update ticket' });
  }
});

// SLA指标
app.get('/api/sla/metrics', async (req, res) => {
  try {
    const query = `
      SELECT 
        priority,
        COUNT(*) as total_tickets,
        COUNT(CASE WHEN resolved_at <= sla_due_at THEN 1 END) as met_sla,
        COUNT(CASE WHEN resolved_at > sla_due_at THEN 1 END) as violated_sla
      FROM tickets
      WHERE status IN ('resolved', 'closed')
      GROUP BY priority
    `;
    
    const result = await pool.query(query);
    res.json(result.rows);
  } catch (error) {
    console.error('Error fetching SLA metrics:', error);
    res.status(500).json({ error: 'Failed to fetch SLA metrics' });
  }
});

// 获取SLA配置
app.get('/api/sla/config', async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM sla_config ORDER BY priority');
    res.json(result.rows);
  } catch (error) {
    console.error('Error fetching SLA config:', error);
    res.status(500).json({ error: 'Failed to fetch SLA config' });
  }
});

// 启动服务器
app.listen(PORT, () => {
  console.log(`✅ Server running on http://localhost:${PORT}`);
  console.log(`✅ Health check: http://localhost:${PORT}/api/health`);
  console.log(`✅ Database: ${process.env.DB_HOST}:${process.env.DB_PORT}/${process.env.DB_NAME}`);
});

// 测试数据库连接
pool.query('SELECT NOW()', (err, res) => {
  if (err) {
    console.error('❌ Database connection failed:', err);
  } else {
    console.log('✅ Database connected successfully');
  }
});
