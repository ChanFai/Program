import { SupportClient, DescribeCasesCommand, CreateCaseCommand, AddCommunicationToCaseCommand } from '@aws-sdk/client-support';
import { Pool } from 'pg';
import logger from '../utils/logger';

// SPT-005: AWS Support Case集成
export class AWSSupportIntegration {
  private client: SupportClient;
  private pool: Pool;

  constructor(pool: Pool) {
    this.client = new SupportClient({ region: process.env.AWS_REGION });
    this.pool = pool;
  }

  async syncSupportCase(caseId: string): Promise<void> {
    try {
      const command = new DescribeCasesCommand({
        caseIdList: [caseId]
      });

      const response = await this.client.send(command);
      const awsCase = response.cases?.[0];

      if (!awsCase) {
        logger.warn(`AWS Support Case ${caseId} not found`);
        return;
      }

      // 更新本地工单状态
      const query = `
        UPDATE tickets 
        SET 
          aws_case_status = $1,
          aws_case_subject = $2,
          aws_case_severity = $3,
          updated_at = NOW()
        WHERE aws_case_id = $4
        RETURNING *
      `;

      const result = await this.pool.query(query, [
        awsCase.status,
        awsCase.subject,
        awsCase.severityCode,
        caseId
      ]);

      if (result.rows.length > 0) {
        logger.info(`Synced AWS Support Case ${caseId}`);
        
        // 检查是否有新的通信记录
        await this.syncCaseCommunications(caseId, result.rows[0].id);
      }
    } catch (error) {
      logger.error(`Error syncing AWS Support Case ${caseId}:`, error);
      throw error;
    }
  }

  async syncCaseCommunications(awsCaseId: string, ticketId: string): Promise<void> {
    const command = new DescribeCasesCommand({
      caseIdList: [awsCaseId],
      includeCommunications: true
    });

    const response = await this.client.send(command);
    const communications = response.cases?.[0]?.recentCommunications?.communications || [];

    for (const comm of communications) {
      // 检查是否已存在
      const checkQuery = 'SELECT id FROM ticket_comments WHERE aws_communication_id = $1';
      const existing = await this.pool.query(checkQuery, [comm.attachmentSet?.[0]?.attachmentId]);

      if (existing.rows.length === 0) {
        // 添加新评论
        const insertQuery = `
          INSERT INTO ticket_comments (
            ticket_id, content, is_internal, aws_communication_id, created_at
          ) VALUES ($1, $2, $3, $4, $5)
        `;

        await this.pool.query(insertQuery, [
          ticketId,
          comm.body,
          false,
          comm.attachmentSet?.[0]?.attachmentId,
          comm.timeCreated
        ]);

        logger.info(`Added communication to ticket ${ticketId}`);
      }
    }
  }

  async createAWSCase(ticketId: string, subject: string, body: string, severity: string): Promise<string> {
    const command = new CreateCaseCommand({
      subject,
      communicationBody: body,
      severityCode: severity,
      serviceCode: 'general-info',
      categoryCode: 'other'
    });

    const response = await this.client.send(command);
    const caseId = response.caseId!;

    // 更新工单关联AWS Case
    await this.pool.query(
      'UPDATE tickets SET aws_case_id = $1 WHERE id = $2',
      [caseId, ticketId]
    );

    logger.info(`Created AWS Support Case ${caseId} for ticket ${ticketId}`);
    return caseId;
  }

  async addCommunication(caseId: string, body: string): Promise<void> {
    const command = new AddCommunicationToCaseCommand({
      caseId,
      communicationBody: body
    });

    await this.client.send(command);
    logger.info(`Added communication to AWS Case ${caseId}`);
  }

  async pollAllActiveCases(): Promise<void> {
    // 获取所有活跃的AWS Support Case
    const query = `
      SELECT DISTINCT aws_case_id 
      FROM tickets 
      WHERE aws_case_id IS NOT NULL 
      AND status NOT IN ('resolved', 'closed')
    `;

    const result = await this.pool.query(query);

    for (const row of result.rows) {
      await this.syncSupportCase(row.aws_case_id);
    }
  }
}
