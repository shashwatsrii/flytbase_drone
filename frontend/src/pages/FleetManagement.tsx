import React, { useState, useEffect } from 'react';
import {
  Table,
  Button,
  Modal,
  Form,
  Input,
  InputNumber,
  Select,
  message,
  Space,
  Tag,
  Popconfirm,
  Card,
  Row,
  Col,
  Statistic,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SyncOutlined,
  RocketOutlined,
  StopOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons';
import { useSelector } from 'react-redux';
import { RootState } from '../store';
import api from '../services/api';

interface Drone {
  id: string;
  name: string;
  model: string;
  serialNumber: string;
  status: 'AVAILABLE' | 'IN_MISSION' | 'MAINTENANCE' | 'OFFLINE';
  batteryLevel: number;
  lastMaintenanceDate: string;
  totalFlightHours: number;
  homeLocation: {
    latitude: number;
    longitude: number;
  };
}

const { Option } = Select;

const FleetManagement: React.FC = () => {
  const [drones, setDrones] = useState<Drone[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingDrone, setEditingDrone] = useState<Drone | null>(null);
  const [form] = Form.useForm();
  const [statistics, setStatistics] = useState({
    total: 0,
    available: 0,
    inMission: 0,
    maintenance: 0,
  });

  const token = useSelector((state: RootState) => state.auth.token);

  useEffect(() => {
    fetchDrones();
  }, []);

  const fetchDrones = async () => {
    setLoading(true);
    try {
      const response = await api.get('/drones', {
        headers: { Authorization: `Bearer ${token}` },
      });
      // Handle paginated response
      const droneData = response.data.content || [];
      setDrones(droneData);
      calculateStatistics(droneData);
    } catch (error) {
      console.error('Error fetching drones:', error);
      message.error('Failed to fetch drones');
      setDrones([]); // Ensure drones is always an array
    } finally {
      setLoading(false);
    }
  };

  const calculateStatistics = (droneList: Drone[]) => {
    const stats = {
      total: droneList.length,
      available: droneList.filter((d) => d.status === 'AVAILABLE').length,
      inMission: droneList.filter((d) => d.status === 'IN_MISSION').length,
      maintenance: droneList.filter((d) => d.status === 'MAINTENANCE').length,
    };
    setStatistics(stats);
  };

  const handleSubmit = async (values: any) => {
    try {
      // Transform form data to match backend structure
      const droneData = {
        name: values.name,
        model: values.model,
        serialNumber: values.serialNumber,
        status: values.status,
        batteryLevel: values.batteryLevel,
        totalFlightHours: values.totalFlightHours || 0,
        lastMaintenanceDate: values.lastMaintenanceDate || null,
        homeLocation: {
          latitude: values.homeLatitude,
          longitude: values.homeLongitude,
        },
      };

      if (editingDrone) {
        await api.put(`/drones/${editingDrone.id}`, droneData, {
          headers: { Authorization: `Bearer ${token}` },
        });
        message.success('Drone updated successfully');
      } else {
        await api.post('/drones', droneData, {
          headers: { Authorization: `Bearer ${token}` },
        });
        message.success('Drone added successfully');
      }
      setModalVisible(false);
      form.resetFields();
      setEditingDrone(null);
      fetchDrones();
    } catch (error: any) {
      console.error('Error submitting drone:', error);
      message.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await api.delete(`/drones/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      message.success('Drone deleted successfully');
      fetchDrones();
    } catch (error) {
      message.error('Failed to delete drone');
    }
  };

  const showEditModal = (drone: Drone) => {
    setEditingDrone(drone);
    form.setFieldsValue({
      ...drone,
      homeLatitude: drone.homeLocation.latitude,
      homeLongitude: drone.homeLocation.longitude,
      lastMaintenanceDate: drone.lastMaintenanceDate ? drone.lastMaintenanceDate.split('T')[0] : '',
    });
    setModalVisible(true);
  };

  const getStatusTag = (status: string) => {
    const statusConfig = {
      AVAILABLE: { color: 'green', icon: <CheckCircleOutlined /> },
      IN_MISSION: { color: 'blue', icon: <RocketOutlined /> },
      MAINTENANCE: { color: 'orange', icon: <SyncOutlined /> },
      OFFLINE: { color: 'red', icon: <StopOutlined /> },
    };

    const config = statusConfig[status as keyof typeof statusConfig];
    return (
      <Tag color={config.color} icon={config.icon}>
        {status.replace('_', ' ')}
      </Tag>
    );
  };

  const columns = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      sorter: (a: Drone, b: Drone) => a.name.localeCompare(b.name),
    },
    {
      title: 'Model',
      dataIndex: 'model',
      key: 'model',
    },
    {
      title: 'Serial Number',
      dataIndex: 'serialNumber',
      key: 'serialNumber',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => getStatusTag(status),
      filters: [
        { text: 'Available', value: 'AVAILABLE' },
        { text: 'In Mission', value: 'IN_MISSION' },
        { text: 'Maintenance', value: 'MAINTENANCE' },
        { text: 'Offline', value: 'OFFLINE' },
      ],
      onFilter: (value: any, record: Drone) => record.status === value,
    },
    {
      title: 'Battery',
      dataIndex: 'batteryLevel',
      key: 'battery',
      render: (battery: number) => (
        <Tag color={battery > 50 ? 'green' : battery > 20 ? 'orange' : 'red'}>
          {battery}%
        </Tag>
      ),
      sorter: (a: Drone, b: Drone) => a.batteryLevel - b.batteryLevel,
    },
    {
      title: 'Flight Hours',
      dataIndex: 'totalFlightHours',
      key: 'flightHours',
      render: (hours: number) => `${hours.toFixed(1)}h`,
      sorter: (a: Drone, b: Drone) => a.totalFlightHours - b.totalFlightHours,
    },
    {
      title: 'Last Maintenance',
      dataIndex: 'lastMaintenanceDate',
      key: 'maintenance',
      render: (date: string) => date ? new Date(date).toLocaleDateString() : 'Not scheduled',
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: any, record: Drone) => (
        <Space>
          <Button
            icon={<EditOutlined />}
            onClick={() => showEditModal(record)}
            disabled={record.status === 'IN_MISSION'}
          />
          <Popconfirm
            title="Are you sure you want to delete this drone?"
            onConfirm={() => handleDelete(record.id)}
            disabled={record.status === 'IN_MISSION'}
          >
            <Button
              danger
              icon={<DeleteOutlined />}
              disabled={record.status === 'IN_MISSION'}
            />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="Total Drones"
              value={statistics.total}
              prefix={<RocketOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Available"
              value={statistics.available}
              valueStyle={{ color: '#3f8600' }}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="In Mission"
              value={statistics.inMission}
              valueStyle={{ color: '#1890ff' }}
              prefix={<RocketOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Maintenance"
              value={statistics.maintenance}
              valueStyle={{ color: '#faad14' }}
              prefix={<SyncOutlined />}
            />
          </Card>
        </Col>
      </Row>

      <Card
        title="Fleet Management"
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setEditingDrone(null);
              form.resetFields();
              setModalVisible(true);
            }}
          >
            Add Drone
          </Button>
        }
      >
        <Table
          columns={columns}
          dataSource={drones}
          rowKey="id"
          loading={loading}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `Total ${total} drones`,
          }}
        />
      </Card>

      <Modal
        title={editingDrone ? 'Edit Drone' : 'Add New Drone'}
        visible={modalVisible}
        onCancel={() => {
          setModalVisible(false);
          form.resetFields();
          setEditingDrone(null);
        }}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="name"
            label="Drone Name"
            rules={[{ required: true, message: 'Please enter drone name' }]}
          >
            <Input placeholder="Enter drone name" />
          </Form.Item>

          <Form.Item
            name="model"
            label="Model"
            rules={[{ required: true, message: 'Please enter drone model' }]}
          >
            <Input placeholder="Enter drone model" />
          </Form.Item>

          <Form.Item
            name="serialNumber"
            label="Serial Number"
            rules={[{ required: true, message: 'Please enter serial number' }]}
          >
            <Input placeholder="Enter serial number" />
          </Form.Item>

          <Form.Item
            name="status"
            label="Status"
            rules={[{ required: true, message: 'Please select status' }]}
            initialValue="AVAILABLE"
          >
            <Select>
              <Option value="AVAILABLE">Available</Option>
              <Option value="MAINTENANCE">Maintenance</Option>
              <Option value="OFFLINE">Offline</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="batteryLevel"
            label="Battery Level (%)"
            rules={[
              { required: true, message: 'Please enter battery level' },
              {
                validator: (_, value) => {
                  if (value === undefined || value === null) {
                    return Promise.reject('Please enter battery level');
                  }
                  if (value < 0 || value > 100) {
                    return Promise.reject('Must be between 0-100');
                  }
                  return Promise.resolve();
                },
              },
            ]}
            initialValue={100}
          >
            <InputNumber min={0} max={100} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            name="lastMaintenanceDate"
            label="Last Maintenance Date"
          >
            <Input type="date" style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            name="totalFlightHours"
            label="Total Flight Hours"
            initialValue={0}
            hidden
          >
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="homeLatitude"
                label="Home Latitude"
                rules={[{ required: true, message: 'Please enter latitude' }]}
              >
                <InputNumber
                  step={0.000001}
                  style={{ width: '100%' }}
                  placeholder="37.7749"
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="homeLongitude"
                label="Home Longitude"
                rules={[{ required: true, message: 'Please enter longitude' }]}
              >
                <InputNumber
                  step={0.000001}
                  style={{ width: '100%' }}
                  placeholder="-122.4194"
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                {editingDrone ? 'Update' : 'Create'}
              </Button>
              <Button
                onClick={() => {
                  setModalVisible(false);
                  form.resetFields();
                  setEditingDrone(null);
                }}
              >
                Cancel
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default FleetManagement;