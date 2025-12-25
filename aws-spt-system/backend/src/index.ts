import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { ticketRoutes } from './routes/tickets';
import { slaRoutes } from './routes/sla';
import { awsIntegrationRoutes } from './routes/aws-integration';
import { healthCheckRoutes } from './routes/health';
import { errorHandler } from './middleware/errorHandler';
import { startBackgroundJobs } from './jobs';
import logger from './utils/logger';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// 路由
app.use('/api/tickets', ticketRoutes);
app.use('/api/sla', slaRoutes);
app.use('/api/aws', awsIntegrationRoutes);
app.use('/api/health', healthCheckRoutes);

// 错误处理
app.use(errorHandler);

// 启动服务器
app.listen(PORT, () => {
  logger.info(`Server running on port ${PORT}`);
  startBackgroundJobs();
});
