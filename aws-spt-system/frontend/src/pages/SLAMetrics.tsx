import { Card, Row, Col, Progress } from 'antd';

export default function SLAMetrics() {
  return (
    <div>
      <h1>SLA指标</h1>
      <Row gutter={16}>
        <Col span={12}>
          <Card title="本月SLA达成率">
            <Progress type="circle" percent={95} />
          </Card>
        </Col>
        <Col span={12}>
          <Card title="各优先级SLA表现">
            <div style={{ marginBottom: 16 }}>
              <div>紧急 (Critical)</div>
              <Progress percent={98} status="success" />
            </div>
            <div style={{ marginBottom: 16 }}>
              <div>高 (High)</div>
              <Progress percent={96} status="success" />
            </div>
            <div style={{ marginBottom: 16 }}>
              <div>中 (Medium)</div>
              <Progress percent={92} />
            </div>
            <div>
              <div>低 (Low)</div>
              <Progress percent={88} />
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
