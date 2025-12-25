import { Card, Descriptions, Tag, Timeline, Button } from 'antd';
import { useParams } from 'react-router-dom';

export default function TicketDetail() {
  const { id } = useParams();

  return (
    <div>
      <h1>工单详情 #{id}</h1>
      <Card title="基本信息" style={{ marginBottom: 16 }}>
        <Descriptions column={2}>
          <Descriptions.Item label="标题">示例工单</Descriptions.Item>
          <Descriptions.Item label="状态"><Tag>进行中</Tag></Descriptions.Item>
          <Descriptions.Item label="优先级"><Tag color="orange">HIGH</Tag></Descriptions.Item>
          <Descriptions.Item label="创建时间">2024-01-01 10:00:00</Descriptions.Item>
          <Descriptions.Item label="SLA截止">2024-01-01 11:00:00</Descriptions.Item>
          <Descriptions.Item label="AWS Case ID">case-123456</Descriptions.Item>
        </Descriptions>
      </Card>
      <Card title="工单历史">
        <Timeline
          items={[
            { children: '工单已创建', color: 'green' },
            { children: '已分配给技术支持', color: 'blue' },
            { children: '正在处理中', color: 'blue' }
          ]}
        />
      </Card>
    </div>
  );
}
