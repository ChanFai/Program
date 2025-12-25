import cron from 'node-cron';
import { Pool } from 'pg';
import { TicketService } from '../services/TicketService';
import { AWSSupportIntegration } from '../integrations/AWSSupportIntegration';
import { AWSHealthIntegration } from '../integrations/AWSHealthIntegration';
import logger from '../utils/logger';

const pool = new Pool({
  host: process.env.DB_HOST,
  port: parseInt(process.env.DB_PORT || '5432'),
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD
});

export function startBackgroundJobs() {
  const ticketService = new TicketService(pool);
  const supportIntegration = new AWSSupportIntegration(pool);
  const healthIntegration = new AWSHealthIntegration(pool);

  // 每5分钟检查SLA违规
  cron.schedule('*/5 * * * *', async () => {
    logger.info('Running SLA violation check');
    try {
      await ticketService.checkSLAViolations();
    } catch (error) {
      logger.error('SLA check failed:', error);
    }
  });

  // 每10分钟同步AWS Support Cases
  cron.schedule('*/10 * * * *', async () => {
    logger.info('Syncing AWS Support Cases');
    try {
      await supportIntegration.pollAllActiveCases();
    } catch (error) {
      logger.error('AWS Support sync failed:', error);
    }
  });

  // 每15分钟检查AWS Health事件
  cron.schedule('*/15 * * * *', async () => {
    logger.info('Polling AWS Health events');
    try {
      await healthIntegration.pollHealthEvents();
    } catch (error) {
      logger.error('AWS Health poll failed:', error);
    }
  });

  logger.info('Background jobs started');
}
