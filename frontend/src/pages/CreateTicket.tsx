import { Form, Input, Select, Button, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function CreateTicket() {
  const [form] = Form.useForm();
  const navigate = useNavigate();

  const onFinish = async (values: any) => {
    try {
      await axios.post('http://localhost:3000/api/tickets', {
        ...values,
        customer_id: '8cd53386-6a3c-4eb4-86bd-4e36a297873f' // 使用演示客户ID
      });
      message.success('工单创建成功');
      navigate('/tickets');
    } catch (error) {
      console.error('Failed to create ticket:', error);
      message.error('创建失败');
    }
  };

  return (
    <div>
      <h1>创建工单</h1>
      <Form form={form} layout="vertical" onFinish={onFinish} style={{ maxWidth: 600 }}>
        <Form.Item name="title" label="标题" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="description" label="描述" rules={[{ required: true }]}>
          <Input.TextArea rows={6} />
        </Form.Item>
        <Form.Item name="priority" label="优先级" rules={[{ required: true }]}>
          <Select>
            <Select.Option value="critical">紧急</Select.Option>
            <Select.Option value="high">高</Select.Option>
            <Select.Option value="medium">中</Select.Option>
            <Select.Option value="low">低</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit">提交</Button>
          <Button style={{ marginLeft: 8 }} onClick={() => navigate('/tickets')}>取消</Button>
        </Form.Item>
      </Form>
    </div>
  );
}
