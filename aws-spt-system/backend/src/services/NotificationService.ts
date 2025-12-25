import { SESClient, SendEmailCommand } from '@aws-sdk/client-ses';
import { Ticket } from '../models/Ticket';
import logger from '../utils/logger';

// SPT-002: 客户服务可用性 - 通知系统
export class NotificationService {
  private sesClient: SESClient;

  constructor() {
    this.sesClient = new SESClient({ region: process.env.SES_REGION });
  }

  async notifyTicketCreated(ticket: Ticket): Promise<void> {
    const subject = `[工单 #${ticket.id}] ${ticket.title}`;
    const body = `
您好，

您的工单已创建成功。

工单编号: ${ticket.id}
标题: ${ticket.title}
优先级: ${ticket.priority}
SLA响应时间: ${ticket.sla_due_at}

我们的支持团队将尽快处理您的请求。

感谢您的耐心等待。

此致
技术支持团队
    `.trim();

    await this.sendEmail(ticket.customer_id, subject, body);
  }

  async notifyTicketUpdated(ticket: Ticket): Promise<void> {
    const subject = `[工单 #${ticket.id}] 状态更新`;
    const body = `
您好，

您的工单状态已更新。

工单编号: ${ticket.id}
当前状态: ${ticket.status}
更新时间: ${ticket.updated_at}

请登录系统查看详情。

此致
技术支持团队
    `.trim();

    await this.sendEmail(ticket.customer_id, subject, body);
  }

  async notifySLAViolation(ticket: Ticket): Promise<void> {
    const subject = `[紧急] 工单 #${ticket.id} SLA超时`;
    const body = `
警告：工单 #${ticket.id} 已超过SLA响应时间。

工单编号: ${ticket.id}
标题: ${ticket.title}
优先级: ${ticket.priority}
SLA截止时间: ${ticket.sla_due_at}
当前状态: ${ticket.status}

请立即处理此工单。
    `.trim();

    // 发送给运营团队
    await this.sendEmail('operations@company.com', subject, body);
  }

  private async sendEmail(to: string, subject: string, body: string): Promise<void> {
    try {
      const command = new SendEmailCommand({
        Source: process.env.EMAIL_FROM,
        Destination: {
          ToAddresses: [to]
        },
        Message: {
          Subject: {
            Data: subject,
            Charset: 'UTF-8'
          },
          Body: {
            Text: {
              Data: body,
              Charset: 'UTF-8'
            }
          }
        }
      });

      await this.sesClient.send(command);
      logger.info(`Email sent to ${to}: ${subject}`);
    } catch (error) {
      logger.error(`Failed to send email to ${to}:`, error);
    }
  }
}
