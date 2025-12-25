import { Card, Row, Col, Statistic } from 'antd';
import { FileTextOutlined, ClockCircleOutlined, CheckCircleOutlined, WarningOutlined } from '@ant-design/icons';
import { useEffect, useState } from 'react';
import axios from 'axios';

export default function Dashboard() {
  const [stats, setStats] = useState({
    pending: 0,
    nearSLA: 0,
    resolved: 0,
    violated: 0
  });

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const response = await axios.get('http://localhost:3000/api/tickets');
      const tickets = response.data;
      
      setStats({
        pending: tickets.filter((t: any) => t.status === 'new' || t.status === 'in_progress').length,
        nearSLA: tickets.filter((t: any) => {
          const slaDue = new Date(t.sla_due_at);
          const now = new Date();
          const hoursLeft = (slaDue.getTime() - now.getTime()) / (1000 * 60 * 60);
          return hoursLeft < 2 && hoursLeft > 0 && t.status !== 'resolved';
        }).length,
        resolved: tickets.filter((t: any) => t.status === 'resolved').length,
        violated: tickets.filter((t: any) => {
          const slaDue = new Date(t.sla_due_at);
          return slaDue < new Date() && t.status !== 'resolved';
        }).length
      });
    } catch (error) {
      console.error('Failed to fetch stats:', error);
    }
  };
  return (
    <div>
      <h1>仪表板</h1>
      <Row gutter={16}>
        <Col span={6}>
          <Card>
            <Statistic
              title="待处理工单"
              value={stats.pending}
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="SLA即将超时"
              value={stats.nearSLA}
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="今日已解决"
              value={stats.resolved}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="SLA违规"
              value={stats.violated}
              prefix={<WarningOutlined />}
              valueStyle={{ color: '#f5222d' }}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
}
