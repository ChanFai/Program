import React, { useState, useEffect } from 'react';

interface SLAConfig {
  priority: string;
  responseTimeMinutes: number;
  resolutionTimeHours: number;
  description: string;
}

const SLAConfig: React.FC = () => {
  const [configs, setConfigs] = useState<SLAConfig[]>([
    {
      priority: 'Critical',
      responseTimeMinutes: 15,
      resolutionTimeHours: 4,
      description: '生产环境完全中断，业务严重受影响'
    },
    {
      priority: 'High',
      responseTimeMinutes: 60,
      resolutionTimeHours: 8,
      description: '生产环境部分功能受影响'
    },
    {
      priority: 'Medium',
      responseTimeMinutes: 240,
      resolutionTimeHours: 24,
      description: '非关键功能问题或性能下降'
    },
    {
      priority: 'Low',
      responseTimeMinutes: 1440,
      resolutionTimeHours: 72,
      description: '一般性咨询、功能请求或轻微问题'
    }
  ]);

  const [metrics, setMetrics] = useState<any>(null);

  useEffect(() => {
    fetchSLAMetrics();
  }, []);

  const fetchSLAMetrics = async () => {
    try {
      const response = await fetch('/api/sla/metrics?days=30');
      const data = await response.json();
      setMetrics(data);
    } catch (error) {
      console.error('Failed to fetch SLA metrics:', error);
    }
  };

  const formatTime = (minutes: number): string => {
    if (minutes < 60) return `${minutes}分钟`;
    const hours = Math.floor(minutes / 60);
    return `${hours}小时`;
  };

  const getPriorityColor = (priority: string): string => {
    const colors: Record<string, string> = {
      'Critical': 'bg-red-100 text-red-800',
      'High': 'bg-orange-100 text-orange-800',
      'Medium': 'bg-yellow-100 text-yellow-800',
      'Low': 'bg-green-100 text-green-800'
    };
    return colors[priority] || 'bg-gray-100 text-gray-800';
  };

  const calculateSLARate = (met: number, total: number): string => {
    if (total === 0) return 'N/A';
    return ((met / total) * 100).toFixed(1) + '%';
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">
          服务级别协议 (SLA) 配置
        </h1>
        <p className="text-gray-600">
          查看和管理服务响应时间标准 - SPT-001合规要求
        </p>
      </div>

      {/* SLA政策文档链接 */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
        <div className="flex items-start">
          <svg className="w-6 h-6 text-blue-600 mr-3 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div>
            <h3 className="text-lg font-semibold text-blue-900 mb-1">SLA政策文档</h3>
            <p className="text-blue-800 mb-2">
              完整的SLA政策和客户服务合同模板已存储在文档中心
            </p>
            <div className="flex gap-3">
              <a 
                href="/docs/SLA-POLICY.md" 
                target="_blank"
                className="text-blue-600 hover:text-blue-800 underline font-medium"
              >
                📄 SLA政策文档
              </a>
              <a 
                href="/docs/CUSTOMER-SERVICE-CONTRACT-TEMPLATE.md" 
                target="_blank"
                className="text-blue-600 hover:text-blue-800 underline font-medium"
              >
                📄 客户服务合同模板
              </a>
            </div>
          </div>
        </div>
      </div>

      {/* SLA配置表格 */}
      <div className="bg-white shadow-md rounded-lg overflow-hidden mb-8">
        <div className="px-6 py-4 bg-gray-50 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-800">响应时间标准</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  优先级
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  首次响应时间
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  解决目标时间
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  描述
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {configs.map((config) => (
                <tr key={config.priority} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-3 py-1 inline-flex text-sm leading-5 font-semibold rounded-full ${getPriorityColor(config.priority)}`}>
                      {config.priority}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-medium">
                    ≤ {formatTime(config.responseTimeMinutes)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-medium">
                    ≤ {config.resolutionTimeHours}小时
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-600">
                    {config.description}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* SLA达成率统计 */}
      {metrics && (
        <div className="bg-white shadow-md rounded-lg overflow-hidden">
          <div className="px-6 py-4 bg-gray-50 border-b border-gray-200">
            <h2 className="text-xl font-semibold text-gray-800">近30天SLA达成情况</h2>
          </div>
          <div className="p-6">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {metrics.map((metric: any) => {
                const slaRate = parseFloat(calculateSLARate(metric.met_sla, metric.total_tickets));
                const isGood = slaRate >= 95;
                
                return (
                  <div key={metric.priority} className="border rounded-lg p-4">
                    <div className="flex items-center justify-between mb-3">
                      <span className={`px-2 py-1 text-xs font-semibold rounded ${getPriorityColor(metric.priority)}`}>
                        {metric.priority}
                      </span>
                      <span className={`text-2xl font-bold ${isGood ? 'text-green-600' : 'text-red-600'}`}>
                        {calculateSLARate(metric.met_sla, metric.total_tickets)}
                      </span>
                    </div>
                    <div className="space-y-2 text-sm text-gray-600">
                      <div className="flex justify-between">
                        <span>总工单数:</span>
                        <span className="font-medium">{metric.total_tickets}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>达标:</span>
                        <span className="font-medium text-green-600">{metric.met_sla}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>违规:</span>
                        <span className="font-medium text-red-600">{metric.violated_sla}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>平均解决时间:</span>
                        <span className="font-medium">{Math.round(metric.avg_resolution_time)}分钟</span>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {/* 操作标准说明 */}
      <div className="mt-8 bg-white shadow-md rounded-lg p-6">
        <h2 className="text-xl font-semibold text-gray-800 mb-4">操作标准</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <h3 className="font-semibold text-gray-700 mb-2">📋 工单处理流程</h3>
            <ol className="list-decimal list-inside space-y-1 text-sm text-gray-600">
              <li>客户创建工单（Web/邮件/API）</li>
              <li>系统自动分配给相应工程师</li>
              <li>工程师在SLA时间内首次响应</li>
              <li>持续跟进并定期更新状态</li>
              <li>完成问题解决</li>
              <li>获得客户确认后关闭</li>
            </ol>
          </div>
          <div>
            <h3 className="font-semibold text-gray-700 mb-2">🔔 通知机制</h3>
            <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
              <li>工单创建立即发送确认邮件</li>
              <li>每次状态变更通知客户</li>
              <li>接近SLA截止时间前30分钟提醒</li>
              <li>问题解决后通知并请求确认</li>
              <li>AWS Support Case更新实时同步</li>
              <li>AWS Health事件主动通知</li>
            </ul>
          </div>
          <div>
            <h3 className="font-semibold text-gray-700 mb-2">⬆️ 升级机制</h3>
            <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
              <li>接近SLA截止时间自动升级</li>
              <li>客户可随时请求升级</li>
              <li>Critical级别自动通知管理层</li>
              <li>升级后由高级工程师接手</li>
            </ul>
          </div>
          <div>
            <h3 className="font-semibold text-gray-700 mb-2">🔄 AWS集成</h3>
            <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
              <li>AWS Support Case自动同步</li>
              <li>每10分钟检查更新</li>
              <li>AWS Health事件实时监控</li>
              <li>主动识别受影响客户</li>
              <li>自动创建事件工单</li>
            </ul>
          </div>
        </div>
      </div>

      {/* 服务可用性 */}
      <div className="mt-8 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg p-6 border border-blue-200">
        <h2 className="text-xl font-semibold text-gray-800 mb-4">🌐 服务可用性承诺</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-white rounded-lg p-4 shadow-sm">
            <div className="text-3xl font-bold text-blue-600 mb-1">24×7</div>
            <div className="text-sm text-gray-600">全天候技术支持</div>
          </div>
          <div className="bg-white rounded-lg p-4 shadow-sm">
            <div className="text-3xl font-bold text-green-600 mb-1">99.9%</div>
            <div className="text-sm text-gray-600">系统正常运行时间</div>
          </div>
          <div className="bg-white rounded-lg p-4 shadow-sm">
            <div className="text-3xl font-bold text-purple-600 mb-1">多渠道</div>
            <div className="text-sm text-gray-600">Web、邮件、API接入</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SLAConfig;
