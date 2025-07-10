import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  Button,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Stepper,
  Step,
  StepLabel,
  Card,
  CardContent,
  Slider,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
} from '@mui/material';
import {
  Add,
  PlayArrow,
} from '@mui/icons-material';
import { MapContainer, TileLayer, Polygon, Marker, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { useLocation } from 'react-router-dom';
import api from '../services/api';

// Fix Leaflet icon issue
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

interface Drone {
  id: string;
  name: string;
  model: string;
  status: string;
}

interface SurveyArea {
  id: string;
  name: string;
  boundaryPolygon: any;
}

interface Mission {
  name: string;
  description: string;
  droneId: string;
  surveyAreaId: string;
  type: string;
  patternType: string;
  flightAltitude: number;
  speed: number;
  overlapPercentage: number;
  scheduledStart: string;
}

const MissionPlanning: React.FC = () => {
  const location = useLocation();
  const { surveyAreaId } = location.state || {};
  
  const [activeStep, setActiveStep] = useState(0);
  const [drones, setDrones] = useState<Drone[]>([]);
  const [surveyAreas, setSurveyAreas] = useState<SurveyArea[]>([]);
  const [selectedArea, setSelectedArea] = useState<any>(null);
  const [polygonPoints, setPolygonPoints] = useState<[number, number][]>([]);
  const [isDrawing, setIsDrawing] = useState(false);
  const [showSaveAreaDialog, setShowSaveAreaDialog] = useState(false);
  const [areaName, setAreaName] = useState('');
  const [mission, setMission] = useState<Mission>({
    name: '',
    description: '',
    droneId: '',
    surveyAreaId: surveyAreaId || '',
    type: 'MAPPING',
    patternType: 'LINEAR',
    flightAltitude: 100,
    speed: 10,
    overlapPercentage: 70,
    scheduledStart: new Date().toISOString(),
  });

  const steps = ['Select Survey Area', 'Configure Mission', 'Review & Launch'];

  useEffect(() => {
    fetchData();
  }, []);
  
  useEffect(() => {
    // Pre-select survey area if coming from survey areas page
    if (surveyAreaId && surveyAreas.length > 0) {
      const area = surveyAreas.find(a => a.id === surveyAreaId);
      if (area) {
        setSelectedArea(area);
        setMission(prev => ({ ...prev, surveyAreaId: area.id }));
      }
    }
  }, [surveyAreaId, surveyAreas]);

  const fetchData = async () => {
    try {
      const [dronesRes, areasRes] = await Promise.all([
        api.get('/drones'),
        api.get('/survey-areas'),
      ]);
      
      const droneData = dronesRes.data.content || dronesRes.data || [];
      const areaData = areasRes.data.content || areasRes.data || [];
      
      setDrones(droneData.filter((d: Drone) => d.status === 'AVAILABLE'));
      setSurveyAreas(areaData);
    } catch (error) {
      console.error('Error fetching data:', error);
    }
  };

  const MapClickHandler = () => {
    useMapEvents({
      click: (e) => {
        if (isDrawing) {
          setPolygonPoints([...polygonPoints, [e.latlng.lat, e.latlng.lng]]);
        }
      },
    });
    return null;
  };

  const handleStartDrawing = () => {
    setIsDrawing(true);
    setPolygonPoints([]);
    setSelectedArea(null);
  };

  const handleFinishDrawing = () => {
    if (polygonPoints.length >= 3) {
      setIsDrawing(false);
      setShowSaveAreaDialog(true);
    }
  };

  const handleSaveArea = async () => {
    try {
      // Ensure polygon is closed by adding first point at the end if not already there
      const coords = polygonPoints.map(p => [p[1], p[0]]);
      if (coords.length > 0 && (coords[0][0] !== coords[coords.length - 1][0] || coords[0][1] !== coords[coords.length - 1][1])) {
        coords.push([coords[0][0], coords[0][1]]);
      }
      const geoJson = {
        type: 'Polygon',
        coordinates: [coords],
      };

      const response = await api.post('/survey-areas', {
        name: areaName,
        description: `Survey area created on ${new Date().toLocaleDateString()}`,
        boundaryPolygon: JSON.stringify(geoJson),
        areaSize: 100, // Mock value - should be calculated
      });

      setSurveyAreas([...surveyAreas, response.data]);
      setSelectedArea(response.data);
      setMission({ ...mission, surveyAreaId: response.data.id });
      setShowSaveAreaDialog(false);
      setAreaName('');
    } catch (error) {
      console.error('Error saving area:', error);
    }
  };

  const handleNext = () => {
    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };

  const handleCreateMission = async () => {
    try {
      const response = await api.post('/missions', mission);
      
      // Generate pattern
      await api.post(`/missions/${response.data.id}/generate-pattern`, {
        patternType: mission.patternType,
        altitude: mission.flightAltitude,
        overlapPercentage: mission.overlapPercentage,
      });

      alert('Mission created successfully!');
      // Navigate to mission monitoring
      window.location.href = `/app/missions/monitor/${response.data.id}`;
    } catch (error: any) {
      console.error('Error creating mission:', error);
      const errorMessage = error.response?.data?.message || 'Error creating mission';
      alert(errorMessage);
    }
  };

  const renderStepContent = (step: number) => {
    switch (step) {
      case 0:
        return (
          <Grid container spacing={3}>
            <Grid item xs={12} md={4}>
              <Paper sx={{ p: 2, height: '100%' }}>
                <Typography variant="h6" gutterBottom>
                  Survey Areas
                </Typography>
                <Button
                  variant="contained"
                  startIcon={<Add />}
                  onClick={handleStartDrawing}
                  fullWidth
                  sx={{ mb: 2 }}
                >
                  Draw New Area
                </Button>
                {isDrawing && (
                  <Button
                    variant="outlined"
                    onClick={handleFinishDrawing}
                    fullWidth
                    sx={{ mb: 2 }}
                  >
                    Finish Drawing
                  </Button>
                )}
                <Box sx={{ maxHeight: 400, overflow: 'auto' }}>
                  {surveyAreas.map((area) => (
                    <Card
                      key={area.id}
                      sx={{
                        mb: 1,
                        cursor: 'pointer',
                        border: selectedArea?.id === area.id ? '2px solid #1976d2' : 'none',
                      }}
                      onClick={() => {
                        setSelectedArea(area);
                        setMission({ ...mission, surveyAreaId: area.id });
                      }}
                    >
                      <CardContent>
                        <Typography variant="subtitle1">{area.name}</Typography>
                      </CardContent>
                    </Card>
                  ))}
                </Box>
              </Paper>
            </Grid>
            <Grid item xs={12} md={8}>
              <Paper sx={{ p: 2, height: 500 }}>
                <MapContainer
                  center={[51.505, -0.09]}
                  zoom={13}
                  style={{ height: '100%', width: '100%' }}
                >
                  <TileLayer
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                  />
                  <MapClickHandler />
                  {polygonPoints.length > 0 && (
                    <>
                      <Polygon positions={polygonPoints} />
                      {polygonPoints.map((point, index) => (
                        <Marker key={index} position={point} />
                      ))}
                    </>
                  )}
                  {selectedArea && selectedArea.boundaryPolygon && (
                    <Polygon
                      positions={JSON.parse(selectedArea.boundaryPolygon).coordinates[0].map((coord: number[]) => [coord[1], coord[0]])}
                      pathOptions={{ color: 'blue' }}
                    />
                  )}
                </MapContainer>
              </Paper>
            </Grid>
          </Grid>
        );
      case 1:
        return (
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Mission Details
                </Typography>
                <TextField
                  fullWidth
                  label="Mission Name"
                  value={mission.name}
                  onChange={(e) => setMission({ ...mission, name: e.target.value })}
                  margin="normal"
                />
                <TextField
                  fullWidth
                  label="Description"
                  value={mission.description}
                  onChange={(e) => setMission({ ...mission, description: e.target.value })}
                  margin="normal"
                  multiline
                  rows={3}
                />
                <FormControl fullWidth margin="normal">
                  <InputLabel>Select Drone</InputLabel>
                  <Select
                    value={mission.droneId}
                    onChange={(e) => setMission({ ...mission, droneId: e.target.value })}
                  >
                    {drones.map((drone) => (
                      <MenuItem key={drone.id} value={drone.id}>
                        {drone.name} - {drone.model}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
                <FormControl fullWidth margin="normal">
                  <InputLabel>Mission Type</InputLabel>
                  <Select
                    value={mission.type}
                    onChange={(e) => setMission({ ...mission, type: e.target.value })}
                  >
                    <MenuItem value="MAPPING">Mapping</MenuItem>
                    <MenuItem value="INSPECTION">Inspection</MenuItem>
                    <MenuItem value="SECURITY">Security</MenuItem>
                  </Select>
                </FormControl>
                <TextField
                  fullWidth
                  label="Scheduled Start"
                  type="datetime-local"
                  value={mission.scheduledStart.slice(0, 16)}
                  onChange={(e) => setMission({ ...mission, scheduledStart: new Date(e.target.value).toISOString() })}
                  margin="normal"
                  InputLabelProps={{ shrink: true }}
                />
              </Paper>
            </Grid>
            <Grid item xs={12} md={6}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Flight Parameters
                </Typography>
                <FormControl fullWidth margin="normal">
                  <InputLabel>Pattern Type</InputLabel>
                  <Select
                    value={mission.patternType}
                    onChange={(e) => setMission({ ...mission, patternType: e.target.value })}
                  >
                    <MenuItem value="LINEAR">Linear</MenuItem>
                    <MenuItem value="CROSSHATCH">Crosshatch</MenuItem>
                    <MenuItem value="PERIMETER">Perimeter</MenuItem>
                  </Select>
                </FormControl>
                <Box sx={{ mt: 3 }}>
                  <Typography gutterBottom>Flight Altitude: {mission.flightAltitude}m</Typography>
                  <Slider
                    value={mission.flightAltitude}
                    onChange={(_, value) => setMission({ ...mission, flightAltitude: value as number })}
                    min={50}
                    max={400}
                    step={10}
                    marks
                    valueLabelDisplay="auto"
                  />
                </Box>
                <Box sx={{ mt: 3 }}>
                  <Typography gutterBottom>Flight Speed: {mission.speed} m/s</Typography>
                  <Slider
                    value={mission.speed}
                    onChange={(_, value) => setMission({ ...mission, speed: value as number })}
                    min={1}
                    max={20}
                    step={1}
                    marks
                    valueLabelDisplay="auto"
                  />
                </Box>
                <Box sx={{ mt: 3 }}>
                  <Typography gutterBottom>Overlap Percentage: {mission.overlapPercentage}%</Typography>
                  <Slider
                    value={mission.overlapPercentage}
                    onChange={(_, value) => setMission({ ...mission, overlapPercentage: value as number })}
                    min={50}
                    max={90}
                    step={5}
                    marks
                    valueLabelDisplay="auto"
                  />
                </Box>
              </Paper>
            </Grid>
          </Grid>
        );
      case 2:
        return (
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Mission Summary
                </Typography>
                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <Typography variant="subtitle2" color="text.secondary">Mission Name</Typography>
                    <Typography variant="body1" gutterBottom>{mission.name}</Typography>
                    
                    <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 2 }}>Description</Typography>
                    <Typography variant="body1" gutterBottom>{mission.description}</Typography>
                    
                    <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 2 }}>Selected Drone</Typography>
                    <Typography variant="body1" gutterBottom>
                      {drones.find(d => d.id === mission.droneId)?.name || 'Not selected'}
                    </Typography>
                    
                    <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 2 }}>Survey Area</Typography>
                    <Typography variant="body1" gutterBottom>
                      {surveyAreas.find(a => a.id === mission.surveyAreaId)?.name || 'Not selected'}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Typography variant="subtitle2" color="text.secondary">Mission Type</Typography>
                    <Chip label={mission.type} color="primary" size="small" sx={{ mb: 2 }} />
                    
                    <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 2 }}>Pattern Type</Typography>
                    <Chip label={mission.patternType} color="secondary" size="small" sx={{ mb: 2 }} />
                    
                    <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 2 }}>Flight Parameters</Typography>
                    <Typography variant="body2">Altitude: {mission.flightAltitude}m</Typography>
                    <Typography variant="body2">Overlap: {mission.overlapPercentage}%</Typography>
                    
                    <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 2 }}>Scheduled Start</Typography>
                    <Typography variant="body1">
                      {new Date(mission.scheduledStart).toLocaleString()}
                    </Typography>
                  </Grid>
                </Grid>
                {(!mission.name || !mission.droneId || !mission.surveyAreaId) && (
                  <Alert severity="warning" sx={{ mt: 2 }}>
                    Please fill in all required fields before creating the mission.
                  </Alert>
                )}
              </Paper>
            </Grid>
          </Grid>
        );
      default:
        return null;
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Mission Planning
      </Typography>
      
      <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
        {steps.map((label) => (
          <Step key={label}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>

      <Box sx={{ mb: 3 }}>
        {renderStepContent(activeStep)}
      </Box>

      <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
        <Button
          disabled={activeStep === 0}
          onClick={handleBack}
        >
          Back
        </Button>
        <Box>
          {activeStep === steps.length - 1 ? (
            <Button
              variant="contained"
              startIcon={<PlayArrow />}
              onClick={handleCreateMission}
              disabled={!mission.name || !mission.droneId || !mission.surveyAreaId}
            >
              Create Mission
            </Button>
          ) : (
            <Button
              variant="contained"
              onClick={handleNext}
              disabled={activeStep === 0 && !mission.surveyAreaId}
            >
              Next
            </Button>
          )}
        </Box>
      </Box>

      {/* Save Area Dialog */}
      <Dialog open={showSaveAreaDialog} onClose={() => setShowSaveAreaDialog(false)}>
        <DialogTitle>Save Survey Area</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Area Name"
            fullWidth
            variant="outlined"
            value={areaName}
            onChange={(e) => setAreaName(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowSaveAreaDialog(false)}>Cancel</Button>
          <Button onClick={handleSaveArea} variant="contained">Save</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default MissionPlanning;