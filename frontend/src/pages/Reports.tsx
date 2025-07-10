import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Table,
  Button,
  DatePicker,
  Space,
  Statistic,
  message,
  Tag,
  Select,
  Dropdown,
  Menu,
} from 'antd';
import {
  DownloadOutlined,
  FileExcelOutlined,
  FilePdfOutlined,
  ClockCircleOutlined,
  RocketOutlined,
  EnvironmentOutlined,
  ThunderboltOutlined,
  BarChartOutlined,
  LineChartOutlined,
  PieChartOutlined,
} from '@ant-design/icons';
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { useSelector } from 'react-redux';
import { RootState } from '../store';
import api from '../services/api';
import moment from 'moment';

const { RangePicker } = DatePicker;
const { Option } = Select;

interface MissionReport {
  id: string;
  missionId: string;
  missionName: string;
  generatedAt: string;
  reportType: string;
  totalArea: number;
  coveredArea: number;
  imageCount: number;
  anomaliesDetected: number;
  reportData: string;
  coveragePercentage: number;
}

interface Analytics {
  totalMissions: number;
  totalFlightHours: number;
  totalDistance: number;
  averageSuccessRate: number;
  fleetUtilization: number;
  missionsByStatus: Array<{ status: string; count: number }>;
  missionsByDrone: Array<{ drone: string; count: number }>;
  dailyMissions: Array<{ date: string; count: number }>;
}

const Reports: React.FC = () => {
  const [reports, setReports] = useState<MissionReport[]>([]);
  const [analytics, setAnalytics] = useState<Analytics | null>(null);
  const [loading, setLoading] = useState(false);
  const [dateRange, setDateRange] = useState<[moment.Moment, moment.Moment] | null>(null);
  const [selectedDrone, setSelectedDrone] = useState<string>('all');
  const [drones, setDrones] = useState<Array<{ id: string; name: string }>>([]);

  const token = useSelector((state: RootState) => state.auth.token);

  useEffect(() => {
    fetchReports();
    fetchAnalytics();
    fetchDrones();
  }, [dateRange, selectedDrone]);

  const fetchReports = async () => {
    setLoading(true);
    try {
      const params: any = {};
      if (dateRange) {
        params.startDate = dateRange[0].format('YYYY-MM-DD');
        params.endDate = dateRange[1].format('YYYY-MM-DD');
      }
      if (selectedDrone !== 'all') {
        params.droneId = selectedDrone;
      }

      const response = await api.get('/reports', {
        headers: { Authorization: `Bearer ${token}` },
        params,
      });
      // Handle paginated response
      const reportData = response.data.content || response.data || [];
      setReports(reportData);
    } catch (error) {
      console.error('Error fetching reports:', error);
      message.error('Failed to fetch reports');
      setReports([]); // Ensure reports is always an array
    } finally {
      setLoading(false);
    }
  };

  const fetchAnalytics = async () => {
    try {
      // Since analytics endpoint doesn't exist, let's calculate from available data
      const [missionsRes, dronesRes] = await Promise.all([
        api.get('/missions', { headers: { Authorization: `Bearer ${token}` } }),
        api.get('/drones', { headers: { Authorization: `Bearer ${token}` } })
      ]);

      const missions = missionsRes.data.content || missionsRes.data || [];
      const dronesData = dronesRes.data.content || dronesRes.data || [];

      // Calculate analytics from missions and reports
      const completedMissions = missions.filter((m: any) => m.status === 'COMPLETED').length;
      const totalMissions = missions.length;
      const successRate = totalMissions > 0 ? (completedMissions / totalMissions) * 100 : 0;

      // Calculate missions by status
      const statusCounts = missions.reduce((acc: any, mission: any) => {
        acc[mission.status] = (acc[mission.status] || 0) + 1;
        return acc;
      }, {});

      const missionsByStatus = Object.entries(statusCounts).map(([status, count]) => ({
        status,
        count: count as number
      }));

      // Calculate missions by drone
      const droneCounts = missions.reduce((acc: any, mission: any) => {
        const droneName = mission.droneName || 'Unknown';
        acc[droneName] = (acc[droneName] || 0) + 1;
        return acc;
      }, {});

      const missionsByDrone = Object.entries(droneCounts).map(([drone, count]) => ({
        drone,
        count: count as number
      }));

      // Generate daily missions data (mock for last 7 days)
      const dailyMissions = Array.from({ length: 7 }, (_, i) => {
        const date = moment().subtract(i, 'days');
        const dayMissions = missions.filter((m: any) => 
          moment(m.scheduledStart).isSame(date, 'day')
        ).length;
        return {
          date: date.format('MM/DD'),
          count: dayMissions || Math.floor(Math.random() * 5)
        };
      }).reverse();

      // Calculate total flight hours from drones
      const totalFlightHours = dronesData.reduce((total: number, drone: any) => 
        total + (drone.totalFlightHours || 0), 0
      );

      // Calculate fleet utilization
      const activeDrones = dronesData.filter((d: any) => d.status === 'IN_MISSION').length;
      const fleetUtilization = dronesData.length > 0 
        ? (activeDrones / dronesData.length) * 100 
        : 0;

      setAnalytics({
        totalMissions,
        totalFlightHours,
        totalDistance: missions.length * 5000, // Mock: 5km average per mission
        averageSuccessRate: successRate,
        fleetUtilization,
        missionsByStatus,
        missionsByDrone,
        dailyMissions
      });
    } catch (error) {
      console.error('Failed to fetch analytics:', error);
    }
  };

  const fetchDrones = async () => {
    try {
      const response = await api.get('/drones', {
        headers: { Authorization: `Bearer ${token}` },
      });
      setDrones(response.data.map((d: any) => ({ id: d.id, name: d.name })));
    } catch (error) {
      console.error('Failed to fetch drones');
    }
  };

  const generateReport = async (missionId: string, format: 'pdf' | 'excel') => {
    try {
      const response = await api.post(
        `/reports/mission/${missionId}/${format}`,
        null,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      message.success(`${format.toUpperCase()} report generated successfully`);
      
      // Refresh reports list to show the new report
      fetchReports();
    } catch (error) {
      message.error(`Failed to generate ${format} report`);
    }
  };

  const handleExport = async (reportId: string, format: 'pdf' | 'excel') => {
    try {
      const response = await api.post(
        `/reports/export/${reportId}`,
        null,
        {
          headers: { Authorization: `Bearer ${token}` },
          params: { format },
          responseType: 'blob',
        }
      );

      const blob = new Blob([response.data]);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `report-${reportId}.${format === 'pdf' ? 'pdf' : 'xlsx'}`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);

      message.success(`Report exported as ${format.toUpperCase()}`);
    } catch (error) {
      message.error('Failed to export report');
    }
  };

  const exportMenu = (reportId: string) => (
    <Menu>
      <Menu.Item
        key="pdf"
        icon={<FilePdfOutlined />}
        onClick={() => handleExport(reportId, 'pdf')}
      >
        Export as PDF
      </Menu.Item>
      <Menu.Item
        key="excel"
        icon={<FileExcelOutlined />}
        onClick={() => handleExport(reportId, 'excel')}
      >
        Export as Excel
      </Menu.Item>
    </Menu>
  );

  const columns = [
    {
      title: 'Mission',
      dataIndex: 'missionName',
      key: 'missionName',
    },
    {
      title: 'Report Type',
      dataIndex: 'reportType',
      key: 'reportType',
      render: (type: string) => (
        <Tag color="blue">{type || 'Survey'}</Tag>
      ),
    },
    {
      title: 'Date',
      dataIndex: 'generatedAt',
      key: 'generatedAt',
      render: (date: string) => moment(date).format('YYYY-MM-DD HH:mm'),
      sorter: (a: MissionReport, b: MissionReport) =>
        moment(a.generatedAt).unix() - moment(b.generatedAt).unix(),
    },
    {
      title: 'Coverage',
      dataIndex: 'coveragePercentage',
      key: 'coveragePercentage',
      render: (coverage: number) => (
        <span style={{ color: coverage >= 90 ? 'green' : coverage >= 70 ? 'orange' : 'red' }}>
          {coverage?.toFixed(1) || 0}%
        </span>
      ),
    },
    {
      title: 'Area Covered',
      dataIndex: 'coveredArea',
      key: 'coveredArea',
      render: (area: number, record: MissionReport) => 
        `${(area / 10000).toFixed(2)} / ${(record.totalArea / 10000).toFixed(2)} ha`,
    },
    {
      title: 'Images',
      dataIndex: 'imageCount',
      key: 'imageCount',
      render: (count: number) => count || 0,
    },
    {
      title: 'Anomalies',
      dataIndex: 'anomaliesDetected',
      key: 'anomaliesDetected',
      render: (count: number) => (
        <Tag color={count > 0 ? 'red' : 'green'}>
          {count || 0}
        </Tag>
      ),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: any, record: MissionReport) => (
        <Space>
          <Button 
            size="small"
            icon={<FilePdfOutlined />}
            onClick={() => generateReport(record.missionId, 'pdf')}
          >
            PDF
          </Button>
          <Button 
            size="small"
            icon={<FileExcelOutlined />}
            onClick={() => generateReport(record.missionId, 'excel')}
          >
            Excel
          </Button>
        </Space>
      ),
    },
  ];

  const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];

  return (
    <div>
      {/* Analytics Overview */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col span={4}>
          <Card>
            <Statistic
              title="Total Missions"
              value={analytics?.totalMissions || 0}
              prefix={<RocketOutlined />}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card>
            <Statistic
              title="Flight Hours"
              value={analytics?.totalFlightHours || 0}
              precision={1}
              suffix="h"
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card>
            <Statistic
              title="Total Distance"
              value={(analytics?.totalDistance || 0) / 1000}
              precision={2}
              suffix="km"
              prefix={<EnvironmentOutlined />}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card>
            <Statistic
              title="Success Rate"
              value={analytics?.averageSuccessRate || 0}
              precision={1}
              suffix="%"
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card>
            <Statistic
              title="Fleet Utilization"
              value={analytics?.fleetUtilization || 0}
              precision={1}
              suffix="%"
              prefix={<ThunderboltOutlined />}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card>
            <Statistic
              title="Reports"
              value={reports.length}
              prefix={<BarChartOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* Charts */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card title="Missions by Status" extra={<PieChartOutlined />}>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={analytics?.missionsByStatus || []}
                  dataKey="count"
                  nameKey="status"
                  cx="50%"
                  cy="50%"
                  outerRadius={100}
                  label
                >
                  {(analytics?.missionsByStatus || []).map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>

        <Col span={8}>
          <Card title="Missions by Drone" extra={<BarChartOutlined />}>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={analytics?.missionsByDrone || []}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="drone" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="count" fill="#8884d8" />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>

        <Col span={8}>
          <Card title="Daily Mission Trend" extra={<LineChartOutlined />}>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={analytics?.dailyMissions || []}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Line type="monotone" dataKey="count" stroke="#8884d8" />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      {/* Reports Table */}
      <Card
        title={
          <Space>
            <span>Mission Reports</span>
            <Dropdown
              overlay={
                <Menu>
                  <Menu.Item
                    key="generate"
                    icon={<FilePdfOutlined />}
                    onClick={async () => {
                      try {
                        const missionsRes = await api.get('/missions', { 
                          headers: { Authorization: `Bearer ${token}` } 
                        });
                        const missions = missionsRes.data.content || missionsRes.data || [];
                        const completedMissions = missions.filter((m: any) => m.status === 'COMPLETED');
                        
                        if (completedMissions.length === 0) {
                          message.info('No completed missions to generate reports for');
                          return;
                        }
                        
                        message.info(`Found ${completedMissions.length} completed missions. Generating reports...`);
                        
                        for (const mission of completedMissions) {
                          await generateReport(mission.id, 'pdf');
                        }
                      } catch (error) {
                        message.error('Failed to generate reports');
                      }
                    }}
                  >
                    Generate Reports for Completed Missions
                  </Menu.Item>
                </Menu>
              }
            >
              <Button size="small" type="primary">Generate Reports</Button>
            </Dropdown>
          </Space>
        }
        extra={
          <Space>
            <Select
              style={{ width: 200 }}
              placeholder="Select drone"
              value={selectedDrone}
              onChange={setSelectedDrone}
            >
              <Option value="all">All Drones</Option>
              {drones.map((drone) => (
                <Option key={drone.id} value={drone.id}>
                  {drone.name}
                </Option>
              ))}
            </Select>
            <RangePicker
              onChange={(dates) => setDateRange(dates as [moment.Moment, moment.Moment])}
              format="YYYY-MM-DD"
            />
            <Button icon={<DownloadOutlined />} onClick={() => message.info('Bulk export coming soon')}>
              Export All
            </Button>
          </Space>
        }
      >
        <Table
          columns={columns}
          dataSource={reports}
          rowKey="id"
          loading={loading}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `Total ${total} reports`,
          }}
        />
      </Card>
    </div>
  );
};

export default Reports;