import { HealthClient, DescribeEventsCommand, DescribeEventDetailsCommand, DescribeAffectedEntitiesCommand } from '@aws-sdk/client-health';
import { EventBridgeClient, PutRuleCommand, PutTargetsCommand } from '@aws-sdk/client-eventbridge';
import { Pool } from 'pg';
import { TicketService } from '../services/TicketService';
import { TicketPriority, TicketSource } from '../models/Ticket';
import logger from '../utils/logger';

// SPT-006: AWS Health事件监控
export class AWSHealthIntegration {
  private healthClient: HealthClient;
  private eventBridgeClient: EventBridgeClient;
  private pool: Pool;
  private ticketService: TicketService;

  constructor(pool: Pool) {
    this.healthClient = new HealthClient({ region: 'us-east-1' }); // Health API只在us-east-1
    this.eventBridgeClient = new EventBridgeClient({ region: process.env.AWS_REGION });
    this.pool = pool;
    this.ticketService = new TicketService(pool);
  }

  async setupEventBridgeRule(): Promise<void> {
    // 创建EventBridge规则监听Health事件
    const ruleName = 'aws-health-events-to-spt';
    
    const putRuleCommand = new PutRuleCommand({
      Name: ruleName,
      Description: 'Forward AWS Health events to SPT system',
      EventPattern: JSON.stringify({
        source: ['aws.health'],
        'detail-type': ['AWS Health Event']
      }),
      State: 'ENABLED'
    });

    await this.eventBridgeClient.send(putRuleCommand);

    // 添加目标（SQS队列）
    const putTargetsCommand = new PutTargetsCommand({
      Rule: ruleName,
      Targets: [{
        Id: '1',
        Arn: process.env.AWS_HEALTH_QUEUE_URL,
        InputTransformer: {
          InputPathsMap: {
            eventArn: '$.detail.eventArn',
            service: '$.detail.service',
            eventTypeCode: '$.detail.eventTypeCode',
            region: '$.detail.eventRegion'
          },
          InputTemplate: JSON.stringify({
            eventArn: '<eventArn>',
            service: '<service>',
            eventTypeCode: '<eventTypeCode>',
            region: '<region>'
          })
        }
      }]
    });

    await this.eventBridgeClient.send(putTargetsCommand);
    logger.info('EventBridge rule configured for AWS Health events');
  }

  async processHealthEvent(eventArn: string): Promise<void> {
    try {
      // 获取事件详情
      const detailsCommand = new DescribeEventDetailsCommand({
        eventArns: [eventArn]
      });

      const detailsResponse = await this.healthClient.send(detailsCommand);
      const event = detailsResponse.successfulSet?.[0];

      if (!event) {
        logger.warn(`Health event ${eventArn} not found`);
        return;
      }

      // 检查是否已创建工单
      const existingQuery = 'SELECT id FROM tickets WHERE aws_health_event_arn = $1';
      const existing = await this.pool.query(existingQuery, [eventArn]);

      if (existing.rows.length > 0) {
        logger.info(`Ticket already exists for health event ${eventArn}`);
        return;
      }

      // 获取受影响的资源
      const entitiesCommand = new DescribeAffectedEntitiesCommand({
        filter: {
          eventArns: [eventArn]
        }
      });

      const entitiesResponse = await this.healthClient.send(entitiesCommand);
      const affectedEntities = entitiesResponse.entities || [];

      // 确定优先级
      const priority = this.determinePriority(event.event?.eventTypeCategory);

      // 创建工单
      const ticket = await this.ticketService.createTicket({
        customer_id: 'system', // 需要根据受影响的账户映射客户
        title: `AWS Health Event: ${event.event?.service} - ${event.event?.eventTypeCode}`,
        description: this.formatHealthEventDescription(event, affectedEntities),
        priority,
        source: TicketSource.AWS_HEALTH,
        aws_health_event_arn: eventArn
      });

      logger.info(`Created ticket ${ticket.id} for health event ${eventArn}`);

      // 通知受影响的客户
      await this.notifyAffectedCustomers(affectedEntities, ticket);
    } catch (error) {
      logger.error(`Error processing health event ${eventArn}:`, error);
      throw error;
    }
  }

  private determinePriority(eventCategory?: string): TicketPriority {
    switch (eventCategory) {
      case 'issue':
        return TicketPriority.HIGH;
      case 'accountNotification':
        return TicketPriority.MEDIUM;
      case 'scheduledChange':
        return TicketPriority.LOW;
      default:
        return TicketPriority.MEDIUM;
    }
  }

  private formatHealthEventDescription(event: any, entities: any[]): string {
    const eventDetail = event.event;
    const description = event.eventDescription?.latestDescription || 'No description available';

    return `
**AWS Health Event**

**Service**: ${eventDetail?.service}
**Event Type**: ${eventDetail?.eventTypeCode}
**Category**: ${eventDetail?.eventTypeCategory}
**Region**: ${eventDetail?.region}
**Status**: ${eventDetail?.statusCode}

**Description**:
${description}

**Affected Resources**: ${entities.length}
${entities.slice(0, 10).map(e => `- ${e.entityValue}`).join('\n')}
${entities.length > 10 ? `\n... and ${entities.length - 10} more` : ''}

**Start Time**: ${eventDetail?.startTime}
${eventDetail?.endTime ? `**End Time**: ${eventDetail.endTime}` : ''}
    `.trim();
  }

  private async notifyAffectedCustomers(entities: any[], ticket: any): Promise<void> {
    // 根据受影响的资源查找客户并发送通知
    const accountIds = [...new Set(entities.map(e => e.awsAccountId))];
    
    for (const accountId of accountIds) {
      const query = 'SELECT * FROM customers WHERE aws_account_id = $1';
      const result = await this.pool.query(query, [accountId]);
      
      if (result.rows.length > 0) {
        const customer = result.rows[0];
        logger.info(`Notifying customer ${customer.id} about health event`);
        // 发送邮件通知
      }
    }
  }

  async pollHealthEvents(): Promise<void> {
    const command = new DescribeEventsCommand({
      filter: {
        eventStatusCodes: ['open', 'upcoming']
      }
    });

    const response = await this.healthClient.send(command);
    const events = response.events || [];

    for (const event of events) {
      if (event.arn) {
        await this.processHealthEvent(event.arn);
      }
    }
  }
}
