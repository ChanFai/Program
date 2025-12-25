import { Table, Tag, Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import axios from 'axios';
import type { ColumnsType } from 'antd/es/table';

interface Ticket {
  id: string;
  title: string;
  priority: string;
  status: string;
  created_at: string;
}

export default function TicketList() {
  const navigate = useNavigate();
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchTickets();
  }, []);

  const fetchTickets = async () => {
    setLoading(true);
    try {
      const response = await axios.get('http://localhost:3000/api/tickets');
      setTickets(response.data);
    } catch (error) {
      console.error('Failed to fetch tickets:', error);
    } finally {
      setLoading(false);
    }
  };

  const columns: ColumnsType<Ticket> = [
    { title: '工单ID', dataIndex: 'id', key: 'id' },
    { title: '标题', dataIndex: 'title', key: 'title' },
    {
      title: '优先级',
      dataIndex: 'priority',
      key: 'priority',
      render: (priority: string) => {
        const colors = { critical: 'red', high: 'orange', medium: 'blue', low: 'green' };
        return <Tag color={colors[priority as keyof typeof colors]}>{priority.toUpperCase()}</Tag>;
      }
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => <Tag>{status}</Tag>
    },
    { title: '创建时间', dataIndex: 'created_at', key: 'created_at' },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Button type="link" onClick={() => navigate(`/tickets/${record.id}`)}>
          查看详情
        </Button>
      )
    }
  ];

  return (
    <div>
      <h1>工单列表</h1>
      <Button 
        type="primary" 
        onClick={() => navigate('/tickets/new')}
        style={{ marginBottom: 16 }}
      >
        创建工单
      </Button>
      <Table 
        columns={columns} 
        dataSource={tickets} 
        rowKey="id" 
        loading={loading}
      />
    </div>
  );
}
