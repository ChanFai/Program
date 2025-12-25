import { Router } from 'express';
import { Pool } from 'pg';
import { SLAService } from '../services/SLAService';

export function createSLARouter(pool: Pool): Router {
  const router = Router();
  const slaService = new SLAService(pool);

  // 获取SLA配置
  router.get('/config', async (req, res) => {
    try {
      const query = `
        SELECT priority, response_time_minutes, resolution_time_hours, description
        FROM sla_config
        ORDER BY 
          CASE priority
            WHEN 'critical' THEN 1
            WHEN 'high' THEN 2
            WHEN 'medium' THEN 3
            WHEN 'low' THEN 4
          END
      `;
      const result = await pool.query(query);
      res.json(result.rows);
    } catch (error) {
      console.error('Error fetching SLA config:', error);
      res.status(500).json({ error: 'Failed to fetch SLA config' });
    }
  });

  // 获取SLA指标
  router.get('/metrics', async (req, res) => {
    try {
      const days = parseInt(req.query.days as string) || 30;
      const endDate = new Date();
      const startDate = new Date();
      startDate.setDate(startDate.getDate() - days);

      const metrics = await slaService.getSLAMetrics(startDate, endDate);
      res.json(metrics);
    } catch (error) {
      console.error('Error fetching SLA metrics:', error);
      res.status(500).json({ error: 'Failed to fetch SLA metrics' });
    }
  });

  // 更新SLA配置
  router.put('/config/:priority', async (req, res) => {
    try {
      const { priority } = req.params;
      const { response_time_minutes, resolution_time_hours } = req.body;

      const query = `
        INSERT INTO sla_config (priority, response_time_minutes, resolution_time_hours)
        VALUES ($1, $2, $3)
        ON CONFLICT (priority) 
        DO UPDATE SET 
          response_time_minutes = $2,
          resolution_time_hours = $3,
          updated_at = NOW()
        RETURNING *
      `;

      const result = await pool.query(query, [
        priority.toLowerCase(),
        response_time_minutes,
        resolution_time_hours
      ]);

      res.json(result.rows[0]);
    } catch (error) {
      console.error('Error updating SLA config:', error);
      res.status(500).json({ error: 'Failed to update SLA config' });
    }
  });

  // 获取SLA违规工单
  router.get('/violations', async (req, res) => {
    try {
      const query = `
        SELECT 
          t.*,
          c.name as customer_name,
          EXTRACT(EPOCH FROM (NOW() - t.sla_due_at))/60 as minutes_overdue
        FROM tickets t
        LEFT JOIN customers c ON t.customer_id = c.id
        WHERE t.status NOT IN ('resolved', 'closed')
        AND t.sla_due_at < NOW()
        ORDER BY t.sla_due_at ASC
      `;

      const result = await pool.query(query);
      res.json(result.rows);
    } catch (error) {
      console.error('Error fetching SLA violations:', error);
      res.status(500).json({ error: 'Failed to fetch SLA violations' });
    }
  });

  // 获取SLA报告
  router.get('/report', async (req, res) => {
    try {
      const { start_date, end_date } = req.query;
      
      const startDate = start_date ? new Date(start_date as string) : new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
      const endDate = end_date ? new Date(end_date as string) : new Date();

      // 总体统计
      const overallQuery = `
        SELECT 
          COUNT(*) as total_tickets,
          COUNT(CASE WHEN resolved_at <= sla_due_at THEN 1 END) as met_sla,
          COUNT(CASE WHEN resolved_at > sla_due_at THEN 1 END) as violated_sla,
          ROUND(COUNT(CASE WHEN resolved_at <= sla_due_at THEN 1 END)::numeric / 
                NULLIF(COUNT(*)::numeric, 0) * 100, 2) as sla_compliance_rate
        FROM tickets
        WHERE created_at BETWEEN $1 AND $2
        AND status IN ('resolved', 'closed')
      `;

      const overallResult = await pool.query(overallQuery, [startDate, endDate]);

      // 按优先级统计
      const byPriorityMetrics = await slaService.getSLAMetrics(startDate, endDate);

      // 按客户统计
      const byCustomerQuery = `
        SELECT 
          c.id,
          c.name,
          COUNT(*) as total_tickets,
          COUNT(CASE WHEN t.resolved_at <= t.sla_due_at THEN 1 END) as met_sla,
          ROUND(COUNT(CASE WHEN t.resolved_at <= t.sla_due_at THEN 1 END)::numeric / 
                NULLIF(COUNT(*)::numeric, 0) * 100, 2) as sla_compliance_rate
        FROM tickets t
        JOIN customers c ON t.customer_id = c.id
        WHERE t.created_at BETWEEN $1 AND $2
        AND t.status IN ('resolved', 'closed')
        GROUP BY c.id, c.name
        ORDER BY total_tickets DESC
        LIMIT 10
      `;

      const byCustomerResult = await pool.query(byCustomerQuery, [startDate, endDate]);

      res.json({
        period: {
          start: startDate,
          end: endDate
        },
        overall: overallResult.rows[0],
        by_priority: byPriorityMetrics,
        by_customer: byCustomerResult.rows
      });
    } catch (error) {
      console.error('Error generating SLA report:', error);
      res.status(500).json({ error: 'Failed to generate SLA report' });
    }
  });

  return router;
}
