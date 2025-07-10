import React, { useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from './store';
import { verifyAuthToken } from './utils/authCheck';
import { logout } from './store/slices/authSlice';

// Import pages directly to avoid chunk loading issues
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import MissionPlanning from './pages/MissionPlanning';
import MissionMonitoring from './pages/MissionMonitoring';
import FleetManagement from './pages/FleetManagement';
import Reports from './pages/Reports';
import SurveyAreas from './pages/SurveyAreas';
import NotFound from './pages/NotFound';

// Layout components
import MainLayout from './components/layouts/MainLayout';
import AuthLayout from './components/layouts/AuthLayout';

const App: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  
  // Create a theme
  const theme = createTheme({
    palette: {
      mode: 'light',
      primary: {
        main: '#1976d2',
      },
      secondary: {
        main: '#dc004e',
      },
    },
  });

  // Get authentication state from Redux store
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);
  
  // Verify token on app load
  useEffect(() => {
    const checkAuth = async () => {
      if (isAuthenticated) {
        const isValid = await verifyAuthToken();
        if (!isValid) {
          dispatch(logout());
        }
      }
    };
    
    checkAuth();
  }, [dispatch, isAuthenticated]);

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Routes>
          {/* Public routes */}
          <Route path="/" element={<AuthLayout />}>
            <Route index element={<Navigate to="/login" replace />} />
            <Route path="login" element={<Login />} />
            <Route path="register" element={<Register />} />
          </Route>

          {/* Protected routes */}
          <Route
            path="/app"
            element={isAuthenticated ? <MainLayout /> : <Navigate to="/login" replace />}
          >
            <Route index element={<Navigate to="/app/dashboard" replace />} />
            <Route path="dashboard" element={<Dashboard />} />
            <Route path="missions">
              <Route index element={<MissionPlanning />} />
              <Route path="monitor/:id" element={<MissionMonitoring />} />
            </Route>
            <Route path="fleet" element={<FleetManagement />} />
            <Route path="survey-areas" element={<SurveyAreas />} />
            <Route path="mission-planning" element={<MissionPlanning />} />
            <Route path="mission-monitoring" element={<MissionMonitoring />} />
            <Route path="reports" element={<Reports />} />
          </Route>

          {/* 404 route */}
          <Route path="*" element={<NotFound />} />
        </Routes>
    </ThemeProvider>
  );
};

export default App;