import React, { useEffect, useState } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  Card,
  CardContent,
  LinearProgress,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
} from '@mui/material';
import {
  FlightTakeoff,
  Assignment,
  CheckCircle,
  Error,
  Schedule,
  Visibility,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

interface DashboardStats {
  totalDrones: number;
  activeMissions: number;
  completedMissions: number;
  totalFlightHours: number;
}

interface RecentMission {
  id: string;
  name: string;
  drone: string;
  status: string;
  scheduledStart: string;
  progress: number;
}

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [stats, setStats] = useState<DashboardStats>({
    totalDrones: 0,
    activeMissions: 0,
    completedMissions: 0,
    totalFlightHours: 0,
  });
  const [recentMissions, setRecentMissions] = useState<RecentMission[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      
      // Fetch statistics
      const [dronesRes, missionsRes] = await Promise.all([
        api.get('/drones'),
        api.get('/missions'),
      ]);

      const drones = dronesRes.data.content || dronesRes.data || [];
      const missions = missionsRes.data.content || missionsRes.data || [];

      // Calculate stats
      const activeMissions = missions.filter((m: any) => m.status === 'ACTIVE').length;
      const completedMissions = missions.filter((m: any) => m.status === 'COMPLETED').length;
      
      // Calculate total flight hours from all drones
      const totalFlightHours = drones.reduce((total: number, drone: any) => {
        return total + (drone.totalFlightHours || 0);
      }, 0);

      setStats({
        totalDrones: drones.length,
        activeMissions,
        completedMissions,
        totalFlightHours: totalFlightHours,
      });

      // Get recent missions
      const recent = missions.slice(0, 5).map((m: any) => ({
        id: m.id,
        name: m.name,
        drone: m.droneName || 'Drone ' + m.droneId,
        status: m.status,
        scheduledStart: m.scheduledStart,
        progress: m.status === 'COMPLETED' ? 100 : 
                 m.status === 'ACTIVE' ? (m.progressPercentage || 0) : 
                 m.status === 'PAUSED' ? (m.progressPercentage || 50) : 0,
      }));

      setRecentMissions(recent);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'primary';
      case 'COMPLETED':
        return 'success';
      case 'PLANNED':
        return 'default';
      case 'PAUSED':
        return 'warning';
      case 'ABORTED':
        return 'error';
      default:
        return 'default';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return <FlightTakeoff />;
      case 'COMPLETED':
        return <CheckCircle />;
      case 'PLANNED':
        return <Schedule />;
      case 'ABORTED':
        return <Error />;
      default:
        return null;
    }
  };

  const statCards = [
    {
      title: 'Total Drones',
      value: stats.totalDrones,
      icon: <FlightTakeoff fontSize="large" />,
      color: '#1976d2',
    },
    {
      title: 'Active Missions',
      value: stats.activeMissions,
      icon: <Assignment fontSize="large" />,
      color: '#ff9800',
    },
    {
      title: 'Completed Missions',
      value: stats.completedMissions,
      icon: <CheckCircle fontSize="large" />,
      color: '#4caf50',
    },
    {
      title: 'Total Flight Hours',
      value: stats.totalFlightHours,
      icon: <Schedule fontSize="large" />,
      color: '#9c27b0',
    },
  ];

  if (loading) {
    return (
      <Box sx={{ width: '100%' }}>
        <LinearProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Dashboard
      </Typography>
      
      {/* Statistics Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {statCards.map((card, index) => (
          <Grid item xs={12} sm={6} md={3} key={index}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <Box>
                    <Typography color="textSecondary" gutterBottom>
                      {card.title}
                    </Typography>
                    <Typography variant="h4">
                      {card.value}
                    </Typography>
                  </Box>
                  <Box sx={{ color: card.color }}>
                    {card.icon}
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Recent Missions */}
      <Paper sx={{ p: 2 }}>
        <Typography variant="h6" gutterBottom>
          Recent Missions
        </Typography>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Mission Name</TableCell>
                <TableCell>Drone</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Scheduled Start</TableCell>
                <TableCell>Progress</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {recentMissions.map((mission) => (
                <TableRow key={mission.id}>
                  <TableCell>{mission.name}</TableCell>
                  <TableCell>{mission.drone}</TableCell>
                  <TableCell>
                    <Chip
                      label={mission.status}
                      color={getStatusColor(mission.status) as any}
                      size="small"
                      icon={getStatusIcon(mission.status) as any}
                    />
                  </TableCell>
                  <TableCell>
                    {new Date(mission.scheduledStart).toLocaleString()}
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <Box sx={{ width: '100%', mr: 1 }}>
                        <LinearProgress
                          variant="determinate"
                          value={mission.progress}
                          color={mission.status === 'COMPLETED' ? 'success' : 'primary'}
                        />
                      </Box>
                      <Typography variant="body2" color="text.secondary">
                        {`${mission.progress}%`}
                      </Typography>
                    </Box>
                  </TableCell>
                  <TableCell>
                    <IconButton
                      size="small"
                      onClick={() => navigate(`/app/missions/monitor/${mission.id}`)}
                    >
                      <Visibility />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>
    </Box>
  );
};

export default Dashboard;