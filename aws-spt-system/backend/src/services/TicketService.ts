import { Pool } from 'pg';
import { Ticket, TicketPriority, TicketStatus, TicketSource } from '../models/Ticket';
import { SLAService } from './SLAService';
import { NotificationService } from './NotificationService';
import logger from '../utils/logger';

export class TicketService {
  private pool: Pool;
  private slaService: SLAService;
  private notificationService: NotificationService;

  constructor(pool: Pool) {
    this.pool = pool;
    this.slaService = new SLAService(pool);
    this.notificationService = new NotificationService();
  }

  async createTicket(data: Partial<Ticket>): Promise<Ticket> {
    const slaDueAt = this.slaService.calculateSLADueDate(data.priority!);
    
    const query = `
      INSERT INTO tickets (
        customer_id, title, description, priority, status, source,
        aws_case_id, aws_health_event_arn, sla_due_at
      ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
      RETURNING *
    `;
    
    const values = [
      data.customer_id,
      data.title,
      data.description,
      data.priority,
      TicketStatus.NEW,
      data.source || TicketSource.WEB,
      data.aws_case_id,
      data.aws_health_event_arn,
      slaDueAt
    ];

    const result = await this.pool.query(query, values);
    const ticket = result.rows[0];

    // 发送通知
    await this.notificationService.notifyTicketCreated(ticket);
    
    logger.info(`Ticket created: ${ticket.id}`);
    return ticket;
  }

  async updateTicket(id: string, data: Partial<Ticket>): Promise<Ticket> {
    const updates: string[] = [];
    const values: any[] = [];
    let paramIndex = 1;

    Object.entries(data).forEach(([key, value]) => {
      if (value !== undefined) {
        updates.push(`${key} = $${paramIndex}`);
        values.push(value);
        paramIndex++;
      }
    });

    updates.push(`updated_at = NOW()`);
    values.push(id);

    const query = `
      UPDATE tickets 
      SET ${updates.join(', ')}
      WHERE id = $${paramIndex}
      RETURNING *
    `;

    const result = await this.pool.query(query, values);
    const ticket = result.rows[0];

    await this.notificationService.notifyTicketUpdated(ticket);
    
    return ticket;
  }

  async getTicket(id: string): Promise<Ticket | null> {
    const query = 'SELECT * FROM tickets WHERE id = $1';
    const result = await this.pool.query(query, [id]);
    return result.rows[0] || null;
  }

  async listTickets(filters: any = {}): Promise<Ticket[]> {
    let query = 'SELECT * FROM tickets WHERE 1=1';
    const values: any[] = [];
    let paramIndex = 1;

    if (filters.customer_id) {
      query += ` AND customer_id = $${paramIndex}`;
      values.push(filters.customer_id);
      paramIndex++;
    }

    if (filters.status) {
      query += ` AND status = $${paramIndex}`;
      values.push(filters.status);
      paramIndex++;
    }

    if (filters.priority) {
      query += ` AND priority = $${paramIndex}`;
      values.push(filters.priority);
      paramIndex++;
    }

    query += ' ORDER BY created_at DESC';

    const result = await this.pool.query(query, values);
    return result.rows;
  }

  async checkSLAViolations(): Promise<void> {
    const query = `
      SELECT * FROM tickets 
      WHERE status NOT IN ('resolved', 'closed')
      AND sla_due_at < NOW()
    `;
    
    const result = await this.pool.query(query);
    
    for (const ticket of result.rows) {
      await this.notificationService.notifySLAViolation(ticket);
      logger.warn(`SLA violation for ticket ${ticket.id}`);
    }
  }
}
