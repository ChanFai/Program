import { Layout, Typography } from 'antd';
import { CustomerServiceOutlined } from '@ant-design/icons';

const { Header } = Layout;
const { Title } = Typography;

export default function AppHeader() {
  return (
    <Header style={{ display: 'flex', alignItems: 'center', background: '#001529' }}>
      <CustomerServiceOutlined style={{ fontSize: 24, color: '#fff', marginRight: 16 }} />
      <Title level={3} style={{ color: '#fff', margin: 0 }}>
        AWS Partner Support System
      </Title>
    </Header>
  );
}
