import { Router } from 'express';
import { Pool } from 'pg';
import { AWSSupportIntegration } from '../integrations/AWSSupportIntegration';
import { AWSHealthIntegration } from '../integrations/AWSHealthIntegration';

const router = Router();
const pool = new Pool({
  host: process.env.DB_HOST,
  port: parseInt(process.env.DB_PORT || '5432'),
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD
});

const supportIntegration = new AWSSupportIntegration(pool);
const healthIntegration = new AWSHealthIntegration(pool);

// SPT-005: 同步AWS Support Case
router.post('/support/sync/:caseId', async (req, res, next) => {
  try {
    await supportIntegration.syncSupportCase(req.params.caseId);
    res.json({ success: true });
  } catch (error) {
    next(error);
  }
});

router.post('/support/create', async (req, res, next) => {
  try {
    const { ticketId, subject, body, severity } = req.body;
    const caseId = await supportIntegration.createAWSCase(ticketId, subject, body, severity);
    res.json({ caseId });
  } catch (error) {
    next(error);
  }
});

// SPT-006: 处理Health事件
router.post('/health/process/:eventArn', async (req, res, next) => {
  try {
    await healthIntegration.processHealthEvent(req.params.eventArn);
    res.json({ success: true });
  } catch (error) {
    next(error);
  }
});

router.post('/health/setup-eventbridge', async (req, res, next) => {
  try {
    await healthIntegration.setupEventBridgeRule();
    res.json({ success: true });
  } catch (error) {
    next(error);
  }
});

export const awsIntegrationRoutes = router;
