import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Modal,
  Form,
  Input,
  message,
  Space,
  Tag,
  Popconfirm,
  Row,
  Col,
  Statistic,
  Descriptions,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  EnvironmentOutlined,
  ExpandOutlined,
  RocketOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import { MapContainer, TileLayer, Polygon, Marker, useMapEvents, Polyline } from 'react-leaflet';
import L, { LatLng } from 'leaflet';
import 'leaflet/dist/leaflet.css';
import './SurveyAreas.css';
import { useSelector } from 'react-redux';
import { RootState } from '../store';
import api from '../services/api';
import { useNavigate } from 'react-router-dom';

const { TextArea } = Input;

// Fix Leaflet icon issue
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

interface SurveyArea {
  id: string;
  name: string;
  description: string;
  boundaryPolygon: any;
  area: number;
  createdAt: string;
  createdBy: {
    id: string;
    username: string;
  };
}

// Map click handler component
const MapClickHandler: React.FC<{ onMapClick: (latlng: L.LatLng) => void }> = ({ onMapClick }) => {
  useMapEvents({
    click: (e) => {
      onMapClick(e.latlng);
    },
  });
  return null;
};

const SurveyAreas: React.FC = () => {
  const [surveyAreas, setSurveyAreas] = useState<SurveyArea[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [mapModalVisible, setMapModalVisible] = useState(false);
  const [editingSurveyArea, setEditingSurveyArea] = useState<SurveyArea | null>(null);
  const [viewingSurveyArea, setViewingSurveyArea] = useState<SurveyArea | null>(null);
  const [polygonPoints, setPolygonPoints] = useState<L.LatLng[]>([]);
  const [polygonComplete, setPolygonComplete] = useState(false);
  const [form] = Form.useForm();
  const navigate = useNavigate();

  const token = useSelector((state: RootState) => state.auth.token);

  useEffect(() => {
    fetchSurveyAreas();
  }, []);

  const fetchSurveyAreas = async () => {
    setLoading(true);
    try {
      const response = await api.get('/survey-areas', {
        headers: { Authorization: `Bearer ${token}` },
      });
      // Handle paginated response
      const areaData = response.data.content || response.data || [];
      setSurveyAreas(areaData);
    } catch (error) {
      console.error('Error fetching survey areas:', error);
      message.error('Failed to fetch survey areas');
      setSurveyAreas([]); // Ensure surveyAreas is always an array
    } finally {
      setLoading(false);
    }
  };

  const handleMapClick = (latlng: L.LatLng) => {
    if (!polygonComplete) {
      setPolygonPoints([...polygonPoints, latlng]);
    }
  };

  const completePolygon = () => {
    if (polygonPoints.length >= 3) {
      // Close the polygon by adding the first point at the end
      const closedPolygon = [...polygonPoints, polygonPoints[0]];
      setPolygonPoints(closedPolygon);
      setPolygonComplete(true);
      
      // Calculate area
      const area = calculatePolygonArea(polygonPoints);
      message.success(`Polygon completed! Area: ${(area / 10000).toFixed(2)} hectares`);
    }
  };

  const clearPolygon = () => {
    setPolygonPoints([]);
    setPolygonComplete(false);
  };

  const calculatePolygonArea = (polygon: L.LatLng[]): number => {
    // Simple area calculation using shoelace formula
    // This gives approximate area in square meters
    let area = 0;
    const numPoints = polygon.length;
    
    for (let i = 0; i < numPoints; i++) {
      const j = (i + 1) % numPoints;
      const lat1 = polygon[i].lat * Math.PI / 180;
      const lat2 = polygon[j].lat * Math.PI / 180;
      const lng1 = polygon[i].lng * Math.PI / 180;
      const lng2 = polygon[j].lng * Math.PI / 180;
      
      area += (lng2 - lng1) * (Math.sin(lat1) + Math.sin(lat2));
    }
    
    // Earth's radius in meters
    const R = 6371000;
    area = Math.abs(area) * R * R / 2;
    
    return area;
  };

  const handleSubmit = async (values: any) => {
    try {
      if (!polygonComplete || polygonPoints.length < 3) {
        message.error('Please complete the polygon boundary');
        return;
      }

      // Convert polygon points to GeoJSON format
      const boundary = {
        type: 'Polygon',
        coordinates: [[
          ...polygonPoints.map(point => [point.lng, point.lat]),
        ]],
      };

      const area = calculatePolygonArea(polygonPoints.slice(0, -1)); // Remove the closing point for area calculation

      const requestData = {
        ...values,
        boundaryPolygon: JSON.stringify(boundary),
        areaSize: area,
      };

      if (editingSurveyArea) {
        await api.put(`/survey-areas/${editingSurveyArea.id}`, requestData, {
          headers: { Authorization: `Bearer ${token}` },
        });
        message.success('Survey area updated successfully');
      } else {
        await api.post('/survey-areas', requestData, {
          headers: { Authorization: `Bearer ${token}` },
        });
        message.success('Survey area created successfully');
      }

      setModalVisible(false);
      form.resetFields();
      setEditingSurveyArea(null);
      clearPolygon();
      fetchSurveyAreas();
    } catch (error) {
      message.error('Operation failed');
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await api.delete(`/survey-areas/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      message.success('Survey area deleted successfully');
      fetchSurveyAreas();
    } catch (error) {
      message.error('Failed to delete survey area');
    }
  };

  const showEditModal = (surveyArea: SurveyArea) => {
    setEditingSurveyArea(surveyArea);
    form.setFieldsValue({
      name: surveyArea.name,
      description: surveyArea.description,
    });
    
    // Parse existing polygon
    try {
      const parsed = JSON.parse(surveyArea.boundaryPolygon);
      if (parsed.type === 'Polygon' && parsed.coordinates) {
        const latLngs = parsed.coordinates[0].map((coord: number[]) => 
          new L.LatLng(coord[1], coord[0])
        );
        setPolygonPoints(latLngs);
        setPolygonComplete(true);
      }
    } catch (error) {
      console.error('Failed to parse boundary polygon');
    }
    
    setModalVisible(true);
  };

  const showViewModal = (surveyArea: SurveyArea) => {
    setViewingSurveyArea(surveyArea);
    setMapModalVisible(true);
  };

  const createMissionFromArea = (surveyAreaId: string) => {
    navigate('/app/mission-planning', { state: { surveyAreaId } });
  };


  const parseBoundary = (boundaryPolygon: any): L.LatLngExpression[] => {
    try {
      const parsed = typeof boundaryPolygon === 'string' 
        ? JSON.parse(boundaryPolygon) 
        : boundaryPolygon;
      
      if (parsed.type === 'Polygon' && parsed.coordinates) {
        return parsed.coordinates[0].map((coord: number[]) => [coord[1], coord[0]] as L.LatLngExpression);
      }
    } catch (error) {
      console.error('Failed to parse boundary polygon');
    }
    return [];
  };

  const calculateCenter = (positions: L.LatLngExpression[]): L.LatLngExpression => {
    if (positions.length === 0) return [0, 0];
    
    let sumLat = 0, sumLng = 0;
    positions.forEach(pos => {
      const [lat, lng] = pos as [number, number];
      sumLat += lat;
      sumLng += lng;
    });
    
    return [sumLat / positions.length, sumLng / positions.length];
  };

  const columns = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      sorter: (a: SurveyArea, b: SurveyArea) => a.name.localeCompare(b.name),
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: 'Area',
      dataIndex: 'area',
      key: 'area',
      render: (area: number) => `${(area / 10000).toFixed(2)} ha`,
      sorter: (a: SurveyArea, b: SurveyArea) => a.area - b.area,
    },
    {
      title: 'Created By',
      dataIndex: ['createdBy', 'username'],
      key: 'createdBy',
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => new Date(date).toLocaleDateString(),
      sorter: (a: SurveyArea, b: SurveyArea) => 
        new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: any, record: SurveyArea) => (
        <Space>
          <Button
            icon={<EyeOutlined />}
            onClick={() => showViewModal(record)}
          />
          <Button
            icon={<RocketOutlined />}
            onClick={() => createMissionFromArea(record.id)}
            type="primary"
          >
            Create Mission
          </Button>
          <Button
            icon={<EditOutlined />}
            onClick={() => showEditModal(record)}
          />
          <Popconfirm
            title="Are you sure you want to delete this survey area?"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button danger icon={<DeleteOutlined />} />
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
              title="Total Survey Areas"
              value={surveyAreas.length}
              prefix={<EnvironmentOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Total Area"
              value={(surveyAreas.reduce((sum, area) => sum + area.area, 0) / 10000).toFixed(2)}
              suffix="ha"
              prefix={<ExpandOutlined />}
            />
          </Card>
        </Col>
      </Row>

      <Card
        title="Survey Area Management"
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setEditingSurveyArea(null);
              form.resetFields();
              clearPolygon();
              setModalVisible(true);
            }}
          >
            Add Survey Area
          </Button>
        }
      >
        <Table
          columns={columns}
          dataSource={surveyAreas}
          rowKey="id"
          loading={loading}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `Total ${total} survey areas`,
          }}
        />
      </Card>

      {/* Create/Edit Modal */}
      <Modal
        title={editingSurveyArea ? 'Edit Survey Area' : 'Create New Survey Area'}
        visible={modalVisible}
        onCancel={() => {
          setModalVisible(false);
          form.resetFields();
          setEditingSurveyArea(null);
          clearPolygon();
        }}
        footer={null}
        width={800}
        destroyOnClose={true}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="name"
            label="Name"
            rules={[{ required: true, message: 'Please enter survey area name' }]}
          >
            <Input placeholder="Enter survey area name" />
          </Form.Item>

          <Form.Item
            name="description"
            label="Description"
          >
            <TextArea rows={3} placeholder="Enter description" />
          </Form.Item>

          <Form.Item label="Boundary">
            <div style={{ marginBottom: 8 }}>
              <Tag color="blue">Click on the map to add polygon vertices. Add at least 3 points and click "Complete Polygon" to finish.</Tag>
            </div>
            <div style={{ height: '400px', marginBottom: 16 }}>
              <MapContainer
                center={[51.505, -0.09]}
                zoom={13}
                style={{ height: '100%', width: '100%' }}
              >
                <TileLayer
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                  attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                />
                <MapClickHandler onMapClick={handleMapClick} />
                {polygonPoints.length > 0 && (
                  <>
                    {/* Draw lines between points */}
                    {polygonPoints.length > 1 && (
                      <Polyline 
                        positions={polygonPoints} 
                        color="#1890ff"
                        weight={3}
                      />
                    )}
                    {/* Draw polygon if complete */}
                    {polygonComplete && (
                      <Polygon 
                        positions={polygonPoints} 
                        color="#1890ff"
                        fillOpacity={0.3}
                      />
                    )}
                    {/* Draw markers for each vertex */}
                    {polygonPoints.map((point, index) => (
                      <Marker 
                        key={index} 
                        position={point}
                        icon={L.divIcon({
                          className: 'polygon-vertex',
                          html: `<div style="background: #1890ff; width: 12px; height: 12px; border-radius: 50%; border: 2px solid white;"></div>`,
                          iconSize: [12, 12],
                          iconAnchor: [6, 6],
                        })}
                      />
                    ))}
                  </>
                )}
              </MapContainer>
            </div>
            <Space>
              <Button 
                type="dashed" 
                onClick={clearPolygon}
                disabled={polygonPoints.length === 0}
              >
                Clear Points
              </Button>
              <Button 
                type="primary" 
                onClick={completePolygon}
                disabled={polygonPoints.length < 3 || polygonComplete}
              >
                Complete Polygon
              </Button>
              {polygonComplete && <Tag color="green">Polygon Complete</Tag>}
              {!polygonComplete && polygonPoints.length > 0 && (
                <Tag color="blue">{polygonPoints.length} points added</Tag>
              )}
            </Space>
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                {editingSurveyArea ? 'Update' : 'Create'}
              </Button>
              <Button
                onClick={() => {
                  setModalVisible(false);
                  form.resetFields();
                  setEditingSurveyArea(null);
                  clearPolygon();
                }}
              >
                Cancel
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* View Modal */}
      <Modal
        title="Survey Area Details"
        visible={mapModalVisible}
        onCancel={() => {
          setMapModalVisible(false);
          setViewingSurveyArea(null);
        }}
        footer={null}
        width={800}
        destroyOnClose={true}
      >
        {viewingSurveyArea && (
          <>
            <Descriptions bordered style={{ marginBottom: 16 }}>
              <Descriptions.Item label="Name" span={3}>
                {viewingSurveyArea.name}
              </Descriptions.Item>
              <Descriptions.Item label="Description" span={3}>
                {viewingSurveyArea.description || 'No description'}
              </Descriptions.Item>
              <Descriptions.Item label="Area">
                {(viewingSurveyArea.area / 10000).toFixed(2)} ha
              </Descriptions.Item>
              <Descriptions.Item label="Created By">
                {viewingSurveyArea.createdBy.username}
              </Descriptions.Item>
              <Descriptions.Item label="Created At">
                {new Date(viewingSurveyArea.createdAt).toLocaleString()}
              </Descriptions.Item>
            </Descriptions>

            <div style={{ height: '400px' }}>
              {(() => {
                const positions = parseBoundary(viewingSurveyArea.boundaryPolygon);
                const center = calculateCenter(positions);
                
                return (
                  <MapContainer
                    center={center}
                    zoom={15}
                    style={{ height: '100%', width: '100%' }}
                  >
                    <TileLayer
                      url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                      attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                    />
                    {positions.length > 0 && (
                      <Polygon 
                        positions={positions} 
                        color="#1890ff"
                        fillOpacity={0.3}
                      />
                    )}
                  </MapContainer>
                );
              })()}
            </div>
          </>
        )}
      </Modal>
    </div>
  );
};

export default SurveyAreas;