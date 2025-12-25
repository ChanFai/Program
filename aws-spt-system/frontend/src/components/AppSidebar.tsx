import { Layout, Menu } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import { DashboardOutlined, FileTextOutlined, BarChartOutlined, PlusOutlined } from '@ant-design/icons';

const { Sider } = Layout;

export default function AppSidebar() {
  const navigate = useNavigate();
  const location = useLocation();

  const menuItems = [
    { key: '/', icon: <DashboardOutlined />, label: '仪表板' },
    { key: '/tickets', icon: <FileTextOutlined />, label: '工单列表' },
    { key: '/tickets/new', icon: <PlusOutlined />, label: '创建工单' },
    { key: '/sla', icon: <BarChartOutlined />, label: 'SLA指标' }
  ];

  return (
    <Sider width={200} style={{ background: '#fff' }}>
      <Menu
        mode="inline"
        selectedKeys={[location.pathname]}
        style={{ height: '100%', borderRight: 0 }}
        items={menuItems}
        onClick={({ key }) => navigate(key)}
      />
    </Sider>
  );
}
