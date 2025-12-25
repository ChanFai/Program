import { Pool } from 'pg';
import { TicketPriority } from '../models/Ticket';

export class SLAService {
  private pool: Pool;
  
  // SPT-001: 基础SLA配置（分钟）
  private slaConfig = {
    [TicketPriority.CRITICAL]: parseInt(process.env.SLA_CRITICAL_RESPONSE || '15'),
    [TicketPriority.HIGH]: parseInt(process.env.SLA_HIGH_RESPONSE || '60'),
    [TicketPriority.MEDIUM]: parseInt(process.env.SLA_MEDIUM_RESPONSE || '240'),
    [TicketPriority.LOW]: parseInt(process.env.SLA_LOW_RESPONSE || '1440')
  };

  constructor(pool: Pool) {
    this.pool = pool;
  }

  calculateSLADueDate(priority: TicketPriority): Date {
    const minutes = this.slaConfig[priority];
    const dueDate = new Date();
    dueDate.setMinutes(dueDate.getMinutes() + minutes);
    return dueDate;
  }

  async getSLAMetrics(startDate: Date, endDate: Date) {
    const query = `
      SELECT 
        priority,
        COUNT(*) as total_tickets,
        COUNT(CASE WHEN resolved_at <= sla_due_at THEN 1 END) as met_sla,
        COUNT(CASE WHEN resolved_at > sla_due_at THEN 1 END) as violated_sla,
        AVG(EXTRACT(EPOCH FROM (resolved_at - created_at))/60) as avg_resolution_time
      FROM tickets
      WHERE created_at BETWEEN $1 AND $2
      AND status IN ('resolved', 'closed')
      GROUP BY priority
    `;
    
    const result = await this.pool.query(query, [startDate, endDate]);
    return result.rows;
  }

  async updateSLAConfig(priority: TicketPriority, minutes: number): Promise<void> {
    this.slaConfig[priority] = minutes;
    
    const query = `
      INSERT INTO sla_config (priority, response_time_minutes)
      VALUES ($1, $2)
      ON CONFLICT (priority) 
      DO UPDATE SET response_time_minutes = $2, updated_at = NOW()
    `;
    
    await this.pool.query(query, [priority, minutes]);
  }
}
