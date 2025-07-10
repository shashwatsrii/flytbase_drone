import React, { useState, useEffect, useRef } from 'react';
import {
  Card,
  Row,
  Col,
  Button,
  Space,
  Tag,
  Progress,
  Statistic,
  List,
  Avatar,
  message,
  Modal,
  Descriptions,
  Alert,
  Tabs,
  Typography,
  Tooltip,
} from 'antd';
import {
  PlayCircleOutlined,
  PauseCircleOutlined,
  StopOutlined,
  RocketOutlined,
  ThunderboltOutlined,
  EnvironmentOutlined,
  DashboardOutlined,
  AlertOutlined,
  InfoCircleOutlined,
  SignalFilled,
  CompassOutlined,
  FieldTimeOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons';
import { MapContainer, TileLayer, Marker, Polyline, Polygon, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { useSelector } from 'react-redux';
import { RootState } from '../store';
import api from '../services/api';
import { useParams, useNavigate } from 'react-router-dom';

const { Title, Paragraph, Text } = Typography;
// Temporarily use mock WebSocket for testing
const connectWebSocket = (token: string) => {
  console.log('Mock WebSocket connected');
  return Promise.resolve();
};

const disconnectWebSocket = () => {
  console.log('Mock WebSocket disconnected');
};

// Keep track of active subscriptions and mission progress globally
const activeSubscriptions: { [key: string]: () => void } = {};
const missionProgressCache: { [key: string]: { progress: number; waypointIndex: number } } = {};

const subscribeToMission = (missionId: string, callback: (data: any) => void) => {
  console.log('Mock subscription to mission:', missionId);
  
  // Clean up any existing subscription for this mission
  if (activeSubscriptions[missionId]) {
    activeSubscriptions[missionId]();
    delete activeSubscriptions[missionId];
  }
  
  // Restore progress from cache or start fresh
  let currentProgress = missionProgressCache[missionId]?.progress || 0;
  let waypointIndex = missionProgressCache[missionId]?.waypointIndex || 0;
  
  // Simulate some progress updates when a mission is active
  const interval = setInterval(() => {
    // Increment progress
    currentProgress = Math.min(100, currentProgress + Math.random() * 5);
    waypointIndex = Math.floor((currentProgress / 100) * 20);
    
    // Generate position that follows a path pattern within the survey area
    // This creates a lawn-mower pattern typical of survey missions
    const baseLatitude = 51.505;
    const baseLongitude = -0.09;
    
    // Create a systematic pattern
    const row = Math.floor(waypointIndex / 4);
    const col = waypointIndex % 4;
    const isReversed = row % 2 === 1;
    
    const lat = baseLatitude + (row * 0.001) - 0.002; // Center the pattern
    const lng = baseLongitude + ((isReversed ? 3 - col : col) * 0.002) - 0.003;
    
    // Add small random variations to simulate real flight
    const latVariation = (Math.random() - 0.5) * 0.0001;
    const lngVariation = (Math.random() - 0.5) * 0.0001;
    
    const mockProgress = {
      missionId,
      droneId: missionId,
      currentPosition: {
        lat: lat + latVariation,
        lng: lng + lngVariation,
        alt: 100 + Math.random() * 10, // More stable altitude
      },
      batteryLevel: Math.max(20, 100 - Math.floor(currentProgress * 0.8)), // Battery decreases with progress
      progressPercentage: Math.floor(currentProgress),
      status: 'ACTIVE',
      timestamp: new Date().toISOString(),
      currentWaypointIndex: waypointIndex,
      distanceCovered: Math.floor(currentProgress * 50), // 5km total mission
      speed: 8 + Math.random() * 4, // More consistent speed
    };
    
    // Save progress to cache
    missionProgressCache[missionId] = { progress: currentProgress, waypointIndex };
    
    callback(mockProgress);
    
    // When mission reaches 100%, auto-complete it
    if (currentProgress >= 100) {
      clearInterval(interval);
      delete activeSubscriptions[missionId];
      delete missionProgressCache[missionId]; // Clear cache on completion
      
      // Trigger mission completion after a short delay
      setTimeout(() => {
        // Send a final update showing 100% completion
        callback({
          ...mockProgress,
          progressPercentage: 100,
          status: 'COMPLETED'
        });
      }, 1000);
    }
  }, 3000);
  
  // Store the cleanup function
  activeSubscriptions[missionId] = () => clearInterval(interval);
  
  return () => clearInterval(interval);
};

const unsubscribeFromMission = (missionId: string) => {
  console.log('Mock unsubscription from mission:', missionId);
  if (activeSubscriptions[missionId]) {
    activeSubscriptions[missionId]();
    delete activeSubscriptions[missionId];
  }
};

const clearMissionProgress = (missionId: string) => {
  delete missionProgressCache[missionId];
};

interface Mission {
  id: string;
  name: string;
  status: 'PLANNED' | 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'ABORTED';
  droneId: string;
  droneName: string;
  surveyAreaId: string;
  surveyAreaName: string;
  surveyArea?: {
    id: string;
    name: string;
    boundaryPolygon: any;
  };
  patternType: string;
  flightAltitude: number;
  speed: number;
  scheduledStart?: string;
  actualStart?: string;
  actualEnd?: string;
}

interface MissionProgress {
  missionId: string;
  droneId: string;
  currentPosition: {
    lat: number;
    lng: number;
    alt: number;
  };
  batteryLevel: number;
  progressPercentage: number;
  status: string;
  timestamp: string;
  currentWaypointIndex: number;
  distanceCovered: number;
  speed: number;
}

interface FlightPath {
  waypoints: Array<{
    lat: number;
    lng: number;
    alt: number;
  }>;
  totalDistance: number;
  estimatedDuration: number;
}

const droneIcon = new L.Icon({
  iconUrl: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIzMiIgaGVpZ2h0PSIzMiIgdmlld0JveD0iMCAwIDI0IDI0Ij48cGF0aCBmaWxsPSIjMTg5MGZmIiBkPSJNMTIgMkw0IDdWMTBIMlYxMkg0VjE3TDEyIDIyTDIwIDE3VjEySDIyVjEwSDIwVjdMMTIgMk0xMiA3LjVMMTQgOC41TDE0IDEyTDE2IDEzVjE1LjVMMTIgMThMOCAxNS41VjEzTDEwIDEyVjguNUwxMiA3LjVaIi8+PC9zdmc+',
  iconSize: [32, 32],
  iconAnchor: [16, 16],
});

const MissionMonitoring: React.FC = () => {
  const [missions, setMissions] = useState<Mission[]>([]);
  const [selectedMission, setSelectedMission] = useState<Mission | null>(null);
  const [missionProgress, setMissionProgress] = useState<MissionProgress | null>(null);
  const [flightPath, setFlightPath] = useState<FlightPath | null>(null);
  const [surveyAreaBoundary, setSurveyAreaBoundary] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [controlModalVisible, setControlModalVisible] = useState(false);
  const mapRef = useRef<L.Map | null>(null);

  const token = useSelector((state: RootState) => state.auth.token);
  const { id: missionIdFromRoute } = useParams<{ id: string }>();
  const navigate = useNavigate();

  useEffect(() => {
    fetchActiveMissions();
    connectWebSocket(token || '').catch(error => {
      console.error('Failed to connect WebSocket:', error);
    });

    return () => {
      disconnectWebSocket();
    };
  }, []);

  useEffect(() => {
    if (selectedMission) {
      // Clear previous mission progress when switching missions
      setMissionProgress(null);
      
      if (selectedMission.status === 'ACTIVE') {
        // Subscribe to WebSocket for active missions
        subscribeToMission(selectedMission.id, handleProgressUpdate);
      }
      
      // Fetch flight path and survey area
      fetchFlightPath(selectedMission.id);
      fetchSurveyAreaBoundary(selectedMission.surveyAreaId);
    }
    
    // Cleanup function to unsubscribe when mission changes
    return () => {
      if (selectedMission) {
        unsubscribeFromMission(selectedMission.id);
      }
    };
  }, [selectedMission?.id, selectedMission?.status]);

  const fetchActiveMissions = async () => {
    setLoading(true);
    try {
      const response = await api.get('/missions', {
        headers: { Authorization: `Bearer ${token}` },
      });
      // Get all missions, as we want to show all mission states
      const allMissions = response.data.content || response.data || [];
      setMissions(allMissions);
      
      // If we have a mission ID from route, select that mission
      if (missionIdFromRoute) {
        const missionFromRoute = allMissions.find((m: Mission) => m.id === missionIdFromRoute);
        if (missionFromRoute) {
          setSelectedMission(missionFromRoute);
        }
      }
      // Otherwise, if no mission is selected, select the first one
      else if (allMissions.length > 0 && !selectedMission) {
        setSelectedMission(allMissions[0]);
      }
      
      // If we have a selected mission, update it with fresh data
      if (selectedMission) {
        const updatedMission = allMissions.find((m: Mission) => m.id === selectedMission.id);
        if (updatedMission) {
          setSelectedMission(updatedMission);
        }
      }
    } catch (error) {
      message.error('Failed to fetch missions');
    } finally {
      setLoading(false);
    }
  };

  const fetchFlightPath = async (missionId: string) => {
    try {
      const response = await api.get(`/flight-paths/mission/${missionId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      
      // Parse waypoints if it's a string
      const flightPathData = response.data;
      if (flightPathData && typeof flightPathData.waypoints === 'string') {
        try {
          flightPathData.waypoints = JSON.parse(flightPathData.waypoints);
        } catch (e) {
          console.error('Failed to parse waypoints JSON:', e);
          flightPathData.waypoints = [];
        }
      }
      
      setFlightPath(flightPathData);
    } catch (error) {
      console.error('Failed to fetch flight path');
    }
  };

  const fetchSurveyAreaBoundary = async (surveyAreaId: string) => {
    try {
      const response = await api.get(`/survey-areas/${surveyAreaId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setSurveyAreaBoundary(response.data.boundaryPolygon);
    } catch (error) {
      console.error('Failed to fetch survey area boundary');
    }
  };

  const handleProgressUpdate = (progress: MissionProgress) => {
    setMissionProgress(progress);
    
    // Don't auto-center map to avoid Leaflet errors
    // User can manually pan to see drone position if needed
    
    // If progress reaches 100%, offer to complete the mission
    if (progress.progressPercentage >= 100 && selectedMission?.status === 'ACTIVE') {
      message.info('Mission simulation complete! You can now complete the mission using the control panel.');
    }
  };

  const handleMissionControl = async (action: 'start' | 'pause' | 'resume' | 'abort' | 'simulate' | 'complete') => {
    if (!selectedMission) return;

    try {
      if (action === 'simulate') {
        await api.post(`/missions/${selectedMission.id}/simulate?waypointCount=20`, null, {
          headers: { Authorization: `Bearer ${token}` },
        });
        message.success('Simulation started successfully');
      } else {
        await api.post(`/missions/${selectedMission.id}/${action}`, null, {
          headers: { Authorization: `Bearer ${token}` },
        });
        
        // Custom success messages
        let successMessage = `Mission ${action}ed successfully`;
        if (action === 'complete') {
          successMessage = 'Mission completed successfully';
        } else if (action === 'abort') {
          successMessage = 'Mission aborted';
        }
        message.success(successMessage);
        
        // Fetch updated mission data
        const response = await api.get(`/missions/${selectedMission.id}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        
        // Update the selected mission with new data
        setSelectedMission(response.data);
        
        // Update the mission list
        fetchActiveMissions();
        
        // If mission was started or resumed, subscribe to WebSocket updates
        if ((action === 'start' || action === 'resume') && response.data.status === 'ACTIVE') {
          // Clear any existing progress
          setMissionProgress(null);
          // Subscribe to the mission for real-time updates
          subscribeToMission(selectedMission.id, handleProgressUpdate);
        }
        
        // If mission was completed or aborted, clear the progress cache
        if (action === 'complete' || action === 'abort') {
          clearMissionProgress(selectedMission.id);
        }
      }
      setControlModalVisible(false);
    } catch (error: any) {
      console.error(`Failed to ${action} mission:`, error);
      const errorMessage = error.response?.data?.message || `Failed to ${action} mission`;
      message.error(errorMessage);
    }
  };

  const MapUpdater: React.FC<{ position: [number, number] }> = ({ position }) => {
    const map = useMap();
    
    // Set map reference when map is ready
    useEffect(() => {
      if (map) {
        mapRef.current = map;
      }
    }, [map]);
    
    return null;
  };

  const getStatusColor = (status: string) => {
    const colors: Record<string, string> = {
      PLANNED: 'blue',
      ACTIVE: 'green',
      PAUSED: 'orange',
      COMPLETED: 'cyan',
      ABORTED: 'red',
    };
    return colors[status] || 'default';
  };

  const getBatteryColor = (level: number) => {
    if (level > 50) return '#52c41a';
    if (level > 20) return '#faad14';
    return '#ff4d4f';
  };

  const parseBoundary = (boundaryPolygon: any) => {
    try {
      const parsed = typeof boundaryPolygon === 'string' 
        ? JSON.parse(boundaryPolygon) 
        : boundaryPolygon;
      
      if (parsed.type === 'Polygon' && parsed.coordinates) {
        return parsed.coordinates[0].map((coord: number[]) => [coord[1], coord[0]]);
      }
    } catch (error) {
      console.error('Failed to parse boundary polygon');
    }
    return [];
  };

  return (
    <Row gutter={[16, 16]}>
      {/* Mission List */}
      <Col span={6}>
        <Card 
          title={
            <Space>
              <span>All Missions</span>
              <Tag color="blue">{missions.length}</Tag>
            </Space>
          } 
          loading={loading}
        >
          <List
            dataSource={missions}
            renderItem={(mission) => (
              <List.Item
                onClick={() => setSelectedMission(mission)}
                style={{ 
                  cursor: 'pointer',
                  backgroundColor: selectedMission?.id === mission.id ? '#f0f0f0' : 'transparent',
                  borderLeft: selectedMission?.id === mission.id ? '4px solid #1890ff' : '4px solid transparent',
                  paddingLeft: selectedMission?.id === mission.id ? '12px' : '16px',
                  transition: 'all 0.3s ease',
                  marginBottom: '8px',
                  borderRadius: '4px'
                }}
                onMouseEnter={(e) => {
                  if (selectedMission?.id !== mission.id) {
                    e.currentTarget.style.backgroundColor = '#fafafa';
                  }
                }}
                onMouseLeave={(e) => {
                  if (selectedMission?.id !== mission.id) {
                    e.currentTarget.style.backgroundColor = 'transparent';
                  }
                }}
              >
                <List.Item.Meta
                  avatar={
                    <Avatar 
                      icon={
                        mission.status === 'COMPLETED' ? <CheckCircleOutlined /> :
                        mission.status === 'ABORTED' ? <StopOutlined /> :
                        <RocketOutlined />
                      }
                      style={{
                        backgroundColor: 
                          mission.status === 'COMPLETED' ? '#52c41a' :
                          mission.status === 'ABORTED' ? '#ff4d4f' :
                          '#1890ff'
                      }}
                    />
                  }
                  title={mission.name}
                  description={
                    <Space direction="vertical" size="small">
                      <Tag color={getStatusColor(mission.status)}>{mission.status}</Tag>
                      <span>{mission.droneName}</span>
                    </Space>
                  }
                />
              </List.Item>
            )}
          />
        </Card>
      </Col>

      {/* Map View */}
      <Col span={12}>
        <Card 
          title="Live Map" 
          style={{ height: '600px' }}
          extra={
            missionProgress && (
              <Button
                size="small"
                icon={<EnvironmentOutlined />}
                onClick={() => {
                  if (mapRef.current && missionProgress.currentPosition) {
                    mapRef.current.setView(
                      [missionProgress.currentPosition.lat, missionProgress.currentPosition.lng], 
                      16
                    );
                  }
                }}
              >
                Center on Drone
              </Button>
            )
          }
        >
          <MapContainer
            center={[51.505, -0.09]}
            zoom={13}
            style={{ height: '540px', width: '100%' }}
          >
            <TileLayer
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            />
            
            {selectedMission && (
              <>
                <MapUpdater position={[51.505, -0.09]} />
                
                {/* Survey Area Boundary */}
                {surveyAreaBoundary && (
                  <Polygon
                    positions={parseBoundary(surveyAreaBoundary)}
                    color="blue"
                    fillOpacity={0.1}
                  />
                )}

                {/* Flight Path */}
                {flightPath && flightPath.waypoints && Array.isArray(flightPath.waypoints) && (
                  <Polyline
                    positions={flightPath.waypoints.map((wp: any) => [wp.lat, wp.lng])}
                    color="orange"
                    weight={2}
                    dashArray="5, 10"
                  />
                )}

                {/* Drone Position */}
                {missionProgress && missionProgress.currentPosition && (
                  <Marker
                    position={[
                      missionProgress.currentPosition.lat,
                      missionProgress.currentPosition.lng,
                    ]}
                    icon={droneIcon}
                  >
                    <Popup>
                      <div>
                        <strong>{selectedMission.droneName}</strong>
                        <br />
                        Alt: {missionProgress.currentPosition.alt}m
                        <br />
                        Battery: {missionProgress.batteryLevel}%
                      </div>
                    </Popup>
                  </Marker>
                )}
              </>
            )}
          </MapContainer>
        </Card>
      </Col>

      {/* Mission Details & Control */}
      <Col span={6}>
        {selectedMission ? (
          <>
            <Card title="Mission Details" style={{ marginBottom: 16 }}>
              <Descriptions column={1} size="small">
                <Descriptions.Item label="Mission">{selectedMission.name}</Descriptions.Item>
                <Descriptions.Item label="Drone">
                  {selectedMission.droneName}
                </Descriptions.Item>
                <Descriptions.Item label="Survey Area">
                  {selectedMission.surveyAreaName}
                </Descriptions.Item>
                <Descriptions.Item label="Pattern">
                  {selectedMission.patternType}
                </Descriptions.Item>
                <Descriptions.Item label="Altitude">{selectedMission.flightAltitude}m</Descriptions.Item>
                <Descriptions.Item label="Speed">{selectedMission.speed} m/s</Descriptions.Item>
              </Descriptions>

              <Space style={{ marginTop: 16 }}>
                <Button
                  type={selectedMission.status === 'COMPLETED' || selectedMission.status === 'ABORTED' ? 'default' : 'primary'}
                  icon={selectedMission.status === 'COMPLETED' || selectedMission.status === 'ABORTED' ? <InfoCircleOutlined /> : <PlayCircleOutlined />}
                  onClick={() => setControlModalVisible(true)}
                >
                  {selectedMission.status === 'COMPLETED' || selectedMission.status === 'ABORTED' ? 'View Status' : 'Control'}
                </Button>
              </Space>
            </Card>

            {missionProgress && (
              <>
                <Card 
                  title={
                    <Space>
                      <span>Live Telemetry</span>
                      <SignalFilled style={{ color: '#52c41a' }} />
                    </Space>
                  } 
                  style={{ marginBottom: 16 }}
                >
                  <Tabs defaultActiveKey="metrics">
                    <Tabs.TabPane tab="Real-time Metrics" key="metrics">
                      <Space direction="vertical" style={{ width: '100%' }}>
                        <Statistic
                          title="Mission Progress"
                          value={missionProgress.progressPercentage}
                          suffix="%"
                          prefix={<DashboardOutlined />}
                        />
                        <Progress 
                          percent={missionProgress.progressPercentage} 
                          status="active"
                          strokeColor={{
                            '0%': '#108ee9',
                            '100%': '#87d068',
                          }}
                        />

                        <Row gutter={16} style={{ marginTop: 16 }}>
                          <Col span={12}>
                            <Tooltip title="Remaining battery power">
                              <Statistic
                                title="Battery Level"
                                value={missionProgress.batteryLevel}
                                suffix="%"
                                valueStyle={{ color: getBatteryColor(missionProgress.batteryLevel) }}
                                prefix={<ThunderboltOutlined />}
                              />
                            </Tooltip>
                          </Col>
                          <Col span={12}>
                            <Tooltip title="Current flight speed">
                              <Statistic
                                title="Ground Speed"
                                value={missionProgress.speed.toFixed(1)}
                                suffix="m/s"
                                prefix={<DashboardOutlined />}
                              />
                            </Tooltip>
                          </Col>
                        </Row>

                        <Row gutter={16} style={{ marginTop: 16 }}>
                          <Col span={12}>
                            <Tooltip title="Current altitude above ground">
                              <Statistic
                                title="Altitude AGL"
                                value={missionProgress.currentPosition.alt.toFixed(0)}
                                suffix="m"
                                prefix={<CompassOutlined />}
                              />
                            </Tooltip>
                          </Col>
                          <Col span={12}>
                            <Tooltip title="Total distance traveled">
                              <Statistic
                                title="Distance Covered"
                                value={(missionProgress.distanceCovered / 1000).toFixed(2)}
                                suffix="km"
                                prefix={<EnvironmentOutlined />}
                              />
                            </Tooltip>
                          </Col>
                        </Row>

                        <Row gutter={16} style={{ marginTop: 16 }}>
                          <Col span={12}>
                            <Statistic
                              title="Waypoint"
                              value={`${missionProgress.currentWaypointIndex + 1} / ${flightPath?.waypoints?.length || '?'}`}
                              prefix={<EnvironmentOutlined />}
                            />
                          </Col>
                          <Col span={12}>
                            <Statistic
                              title="Est. Time Remaining"
                              value={Math.max(0, Math.floor((100 - missionProgress.progressPercentage) * 0.3))}
                              suffix="min"
                              prefix={<FieldTimeOutlined />}
                            />
                          </Col>
                        </Row>
                      </Space>
                    </Tabs.TabPane>

                    <Tabs.TabPane 
                      tab={
                        <Space>
                          <InfoCircleOutlined />
                          <span>Info</span>
                        </Space>
                      } 
                      key="info"
                    >
                      <Title level={5}>What is Live Telemetry?</Title>
                      <Paragraph>
                        Live telemetry provides real-time data from the drone during an active mission. This includes:
                      </Paragraph>
                      
                      <Space direction="vertical" style={{ width: '100%' }}>
                        <Alert
                          message="Mission Progress"
                          description="Shows the percentage of the mission completed based on waypoints covered."
                          type="info"
                          showIcon
                          icon={<DashboardOutlined />}
                        />
                        
                        <Alert
                          message="Battery Level"
                          description="Monitors drone battery in real-time. Warnings appear below 20%."
                          type="info"
                          showIcon
                          icon={<ThunderboltOutlined />}
                        />
                        
                        <Alert
                          message="Flight Metrics"
                          description="Ground speed, altitude (AGL - Above Ground Level), and distance covered."
                          type="info"
                          showIcon
                          icon={<CompassOutlined />}
                        />
                        
                        <Alert
                          message="Navigation Status"
                          description="Current waypoint number and estimated time to complete the mission."
                          type="info"
                          showIcon
                          icon={<EnvironmentOutlined />}
                        />
                      </Space>
                      
                      <Paragraph style={{ marginTop: 16 }}>
                        <Text strong>Note:</Text> In this simulation environment, telemetry data is 
                        generated randomly. In production, this would stream directly from the drone's flight controller.
                      </Paragraph>
                    </Tabs.TabPane>

                    <Tabs.TabPane tab="GPS Data" key="gps">
                      <Descriptions column={1} size="small">
                        <Descriptions.Item label="Latitude">
                          {missionProgress.currentPosition.lat.toFixed(6)}°
                        </Descriptions.Item>
                        <Descriptions.Item label="Longitude">
                          {missionProgress.currentPosition.lng.toFixed(6)}°
                        </Descriptions.Item>
                        <Descriptions.Item label="Altitude MSL">
                          {(missionProgress.currentPosition.alt + 50).toFixed(1)}m
                        </Descriptions.Item>
                        <Descriptions.Item label="GPS Signal">
                          <Tag color="green">Strong</Tag>
                        </Descriptions.Item>
                        <Descriptions.Item label="Satellites">
                          {12 + Math.floor(Math.random() * 4)}
                        </Descriptions.Item>
                        <Descriptions.Item label="HDOP">
                          {(0.8 + Math.random() * 0.4).toFixed(1)}
                        </Descriptions.Item>
                      </Descriptions>
                    </Tabs.TabPane>
                  </Tabs>
                </Card>

                {missionProgress.batteryLevel < 20 && (
                  <Card style={{ backgroundColor: '#fff2e8', borderColor: '#ffbb96' }}>
                    <Space>
                      <AlertOutlined style={{ color: '#fa8c16' }} />
                      <span>Low battery warning!</span>
                    </Space>
                  </Card>
                )}
              </>
            )}
          </>
        ) : (
          <Card>
            <p>Select a mission to view details</p>
          </Card>
        )}
      </Col>

      {/* Mission Control Modal */}
      <Modal
        title="Mission Control"
        visible={controlModalVisible}
        onCancel={() => setControlModalVisible(false)}
        footer={null}
      >
        {selectedMission && (
          <Space direction="vertical" style={{ width: '100%' }}>
            <p>Current Status: <Tag color={getStatusColor(selectedMission.status)}>{selectedMission.status}</Tag></p>
            
            {/* Show mission progress if available */}
            {selectedMission.status === 'ACTIVE' && missionProgress && (
              <div style={{ marginBottom: 16 }}>
                <Progress 
                  percent={missionProgress.progressPercentage} 
                  status="active"
                  strokeColor={{
                    '0%': '#108ee9',
                    '100%': '#87d068',
                  }}
                />
                <Text type="secondary">
                  Mission Progress: {missionProgress.progressPercentage}% 
                  {missionProgress.progressPercentage >= 95 && ' - Almost complete!'}
                </Text>
              </div>
            )}
            
            {/* Show appropriate UI based on mission status */}
            {(selectedMission.status === 'COMPLETED' || selectedMission.status === 'ABORTED') ? (
              <Alert
                message={selectedMission.status === 'COMPLETED' ? 'Mission Completed' : 'Mission Aborted'}
                description={
                  selectedMission.status === 'COMPLETED' 
                    ? 'This mission has been successfully completed. No further actions are available.'
                    : 'This mission has been aborted. No further actions are available.'
                }
                type={selectedMission.status === 'COMPLETED' ? 'success' : 'warning'}
                showIcon
                style={{ marginTop: 16 }}
              />
            ) : (
              <>
                <Space wrap>
                  {selectedMission.status === 'PLANNED' && (
                    <Button
                      type="primary"
                      icon={<PlayCircleOutlined />}
                      onClick={() => handleMissionControl('start')}
                    >
                      Start Mission
                    </Button>
                  )}
                  
                  {selectedMission.status === 'ACTIVE' && (
                    <Button
                      icon={<PauseCircleOutlined />}
                      onClick={() => handleMissionControl('pause')}
                    >
                      Pause Mission
                    </Button>
                  )}
                  
                  {selectedMission.status === 'PAUSED' && (
                    <Button
                      type="primary"
                      icon={<PlayCircleOutlined />}
                      onClick={() => handleMissionControl('resume')}
                    >
                      Resume Mission
                    </Button>
                  )}
                  
                  {(selectedMission.status === 'ACTIVE' || selectedMission.status === 'PAUSED') && (
                    <Button
                      danger
                      icon={<StopOutlined />}
                      onClick={() => handleMissionControl('abort')}
                    >
                      Abort Mission
                    </Button>
                  )}
                  
                  {selectedMission.status === 'ACTIVE' && (
                    <>
                      <Button
                        type="dashed"
                        icon={<RocketOutlined />}
                        onClick={() => handleMissionControl('simulate')}
                      >
                        Simulate Progress
                      </Button>
                      <Button
                        type="primary"
                        icon={<CheckCircleOutlined />}
                        onClick={() => handleMissionControl('complete')}
                        style={{ backgroundColor: '#52c41a', borderColor: '#52c41a' }}
                      >
                        Complete Mission
                      </Button>
                    </>
                  )}
                </Space>
                
                <Alert
                  message="Note"
                  description="This is a simulation environment. In production, these controls would interface with actual drone hardware."
                  type="info"
                  showIcon
                />
              </>
            )}
          </Space>
        )}
      </Modal>
    </Row>
  );
};

export default MissionMonitoring;